package xc8010.assembler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import static java.lang.System.exit;

public class Assembler {

	public static final char COMMENT_BEGIN = ';';
	public static final char LABEL_MARK = ':';

	private static ArrayList<String> lines = new ArrayList<>();
	private static ArrayList<Section> sections = new ArrayList<>();
	private static HashMap<String, Integer> labels = new HashMap<>();
	private static HashMap<String, Integer> dlabels = new HashMap<>();

	public static int startingAddr;
	private static File outfile;

	public static void main(String[] args) throws IOException {
		File f = new File("bootldr.asm");
		outfile = new File("bootldr.bin");
		startingAddr = 0x0400;
		readFile(f);
		sections();
		dummyLabels();
		labels();
		ByteBuffer buf = assemble();
		save(buf);
	}

	private static void readFile(File f) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String line;
		while ((line = r.readLine()) != null) {
			int i = line.indexOf(COMMENT_BEGIN);
			if (i != -1) {
				line = line.substring(0, i);
			}
			line = line.trim();
			lines.add(line);
		}
		r.close();
	}

	private static void sections() {
		HashMap<String, Section> sectionsMap = new HashMap<>();
		Section currentSection = null;
		for (int i = 0; i < lines.size(); i++) {
			String s = lines.get(i);
			if (s.isEmpty())
				continue;
			if (s.startsWith("section")) {
				if (s.length() > 8) {
					String sn = s.substring(8);
					currentSection = sectionsMap.get(sn);
					if (currentSection == null) {
						currentSection = new Section(sn);
						sectionsMap.put(sn, currentSection);
						sections.add(currentSection);
						System.out.printf("Section: %s%n", sn);
						if (!Arrays.asList(".data", ".text").contains(sn))
							System.out.println("Custom sections will be appended after .data and .text sections.");
					}
				} else {
					errorMsg(i, s, 0, "must be followed by an identifier");
					exit(1);
				}
				continue;
			}
			if (currentSection == null) {
				errorMsg(i, s, 0, "section not defined");
				exit(1);
			}
			currentSection.putInsn(i, s);
		}
		sections.sort(null);
		if (!sectionsMap.containsKey(".text")) {
			System.out.println("warning: no `.text' section defined.");
		}
	}

	private static void dummyLabels() {
		Pattern p = Pattern.compile("^[^0-9,\\(\\);:.$]+$");
		for (Section s : sections) {
			for (Iterator<Map.Entry<Integer, String>> iterator = s.iterator(); iterator.hasNext();) {
				Map.Entry<Integer, String> entry = iterator.next();
				int ln = entry.getKey();
				String line = entry.getValue();
				int colonPos = line.indexOf(LABEL_MARK);
				if (colonPos > -1) {
					String labelText = line.substring(0, colonPos);
					if (!p.matcher(labelText).matches()) {
						errorMsg(ln, line, 0, "label name can only contain alphanumerical characters");
						exit(1);
					}
					if (dlabels.containsKey(labelText)) {
						errorMsg(ln, line, labelText.indexOf(' '), "duplicate label");
						exit(1);
					}
					dlabels.put(labelText, 0);
					line = line.substring(colonPos + 1).trim();
				}
			}
		}
	}

	private static void labels() {
		Pattern p = Pattern.compile("^[^0-9,\\(\\);:.$]+$");
		int cptr = startingAddr;
		for (Section s : sections) {
			for (Iterator<Map.Entry<Integer, String>> iterator = s.iterator(); iterator.hasNext();) {
				Map.Entry<Integer, String> entry = iterator.next();
				int ln = entry.getKey();
				String line = entry.getValue();
				int colonPos = line.indexOf(LABEL_MARK);
				if (colonPos > -1) {
					String labelText = line.substring(0, colonPos);
					if (!p.matcher(labelText).matches()) {
						errorMsg(ln, line, 0, "label name can only contain alphanumerical characters");
						exit(1);
					}
					if (labels.containsKey(labelText)) {
						errorMsg(ln, line, labelText.indexOf(' '), "duplicate label");
						exit(1);
					}
					System.out.printf("Label: %s [$%04X]%n", labelText, cptr);
					labels.put(labelText, cptr);
					line = line.substring(colonPos + 1).trim();
					s.repl(ln, line);
				}
				if (!line.isEmpty()) {
					cptr += Instruction.fromString(line, 0, dlabels).getData(0).length;
				}
			}
		}
	}

	private static ByteBuffer assemble() {
		ByteBuffer buf = ByteBuffer.allocate(65536);
		int cptr = startingAddr;
		for (Section s : sections) {
			for (Iterator<Map.Entry<Integer, String>> iterator = s.iterator(); iterator.hasNext();) {
				Map.Entry<Integer, String> entry = iterator.next();
				String line = entry.getValue();
				Instruction insn = Instruction.fromString(line, cptr, labels);
				byte[] data = insn.getData(cptr);
				buf.put(data);
				cptr += data.length;
			}
		}
		return buf;
	}

	private static void save(ByteBuffer buf) {
		int len = buf.position();
		buf.flip();
		try (FileOutputStream fos = new FileOutputStream(outfile)) {
			while (buf.position() < len) {
				fos.write(buf.get());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void errorMsg(int lineNo, String line, int index, String errorMsg) {
		String s = "";
		while (s.length() < index)
			s += " ";
		s += "^";
		System.err.printf("%s%n%s%n%s%nat line %s%n", line, s, errorMsg, lineNo + 1);
	}

	public static byte[] stringBytes(String s) {
		return s.getBytes(Charset.forName("US-ASCII"));
	}

	public static int parseInt(String s) {
		String cut = s;
		if (s.startsWith("$")) {
			cut = s.substring(1);
		} else {
			System.err.printf("Could not parse integer '%s'", s);
			exit(1);
		}
		int i = 0;
		try {
			i = Integer.parseInt(cut, 16);
		} catch (Exception e) {
			System.err.printf("Could not parse integer '%s'", s);
			exit(1);
		}
		return i;
	}

	public static String toString(int i, boolean force16b) {
		String s = Integer.toHexString(i & 0xFFFF);
		while ((s.length() & 1) != 0 || (force16b && s.length() < 4)) {
			s = "0" + s;
		}
		return "$" + s;
	}

}
