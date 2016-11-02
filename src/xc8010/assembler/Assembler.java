package xc8010.assembler;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.System.exit;

/**
 * THIS LOOKS LIKE DEATH
 * SPAGHETTI CODE EVERYWHERE
 * KILL IT BEFORE IT LAYS EGGS
 */

public class Assembler {

    public static final char COMMENT_BEGIN = ';';
    public static final char LABEL_MARK = ':';
    public static int startingAddr;
    public static HashMap<String, Integer> vars = new HashMap<>();
    public static HashMap<String, Macro> macros = new HashMap<>();
    private static ArrayList<String> lines = new ArrayList<>();
    private static ArrayList<Section> sections = new ArrayList<>();
    private static HashMap<String, Integer> labels = new HashMap<>();
    private static HashMap<String, Integer> dlabels = new HashMap<>();
    private static File infile;
    private static File outfile;

    public static void main(String[] args) throws IOException {
        setargs(args);
        readFile();
        macros();
        sections();
        dummyLabels();
        labels();
        ByteBuffer buf = assemble();
        save(buf);
    }

    private static void setargs(String[] args) {
        if (args.length < 2) {
            System.out.println("Syntax: <input> <output> [starting-address]");
            System.out.println("                         [ Default: $0400 ]");
            exit(1);
        }
        infile = new File(args[0]);
        outfile = new File(args[1]);
        startingAddr = 0x0400;
        if (args.length > 2) {
            startingAddr = parseInt(args[2]);
        }
    }

    private static void readFile() throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(infile)));
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

    private static void macros() {
        boolean defm = false;
        Macro macro = null;
        String mn = null;
        for (int i = 0; i < lines.size(); i++) {
            String s = lines.get(i);
            if (s.isEmpty())
                continue;
            if (s.startsWith(".macro")) {
                if (s.length() > 7) {
                    if (defm) {
                        System.out.println("Macro already defining!");
                        exit(1);
                    }
                    String ma = s.substring(7);
                    mn = ma;
                    ArrayList<String> args = new ArrayList<>();
                    int sep;
                    if ((sep = ma.indexOf(' ')) != -1) {
                        mn = ma.substring(0, sep).trim();
                        String[] argslist = ma.substring(sep).trim().split(",");
                        for (String arg : argslist) {
                            arg = arg.trim();
                            int eq;
                            if ((eq = arg.indexOf('=')) != -1) {
                                args.add(arg.substring(0, eq).trim());
                                args.add(arg.substring(eq + 1).trim());
                            } else {
                                args.add(arg);
                                args.add(null);
                            }
                        }
                    }
                    if (macros.containsKey(mn)) {
                        System.out.printf("Macro %s already defined!%n", mn);
                        exit(1);
                    }
                    System.out.printf("Macro: %s%n", mn);
                    macro = new Macro(args.toArray(new String[0]));
                    defm = true;
                }
                continue;
            }
            if (defm) {
                if (s.equals(".endm")) {
                    defm = false;
                    macro.compile();
                    macros.put(mn, macro);
                } else {
                    macro.addInsn(s);
                }
            }
        }
    }

    private static void sections() {
        HashMap<String, Section> sectionsMap = new HashMap<>();
        Section currentSection = null;
        boolean defm = false;
        for (int i = 0; i < lines.size(); i++) {
            String s = lines.get(i);
            if (s.isEmpty())
                continue;
            if (s.startsWith(".macro")) {
                defm = true;
                continue;
            }
            if (defm) {
                if (s.equals(".endm")) {
                    defm = false;
                }
            } else {
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
                                System.out.println("INFO: Custom sections will be appended after .data and .text sections.");
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
        }
        sections.sort(null);
        if (!sectionsMap.containsKey(".text")) {
            System.out.println("warning: no `.text' section defined.");
        }
    }

    private static void dummyLabels() {
        Pattern p = Pattern.compile("^[^0-9,\\(\\);:.$]+$");
        System.out.print("Labels:");
        for (Section s : sections) {
            for (Iterator<Map.Entry<Integer, String>> iterator = s.iterator(); iterator.hasNext(); ) {
                Map.Entry<Integer, String> entry = iterator.next();
                int ln = entry.getKey();
                String line = entry.getValue();
                dummyLabels_processLine(ln, line, p);
            }
        }
        System.out.println();
    }

    private static void dummyLabels_processLine(int ln, String line, Pattern p) {
        int colonPos = line.indexOf(LABEL_MARK);
        Instruction insn = Instruction.fromString(line, 0, dlabels);
        Macro macro = macros.get(insn.id);
        if (macro != null) {
            for (String line2 : macro.substitute(insn.arguments)) {
                dummyLabels_processLine(ln, line2, p);
            }
        } else if (colonPos > -1) {
            String labelText = line.substring(0, colonPos);
            if (!p.matcher(labelText).matches()) {
                errorMsg(ln, line, 0, "label name can only contain alphanumerical characters");
                exit(1);
            }
            if (dlabels.containsKey(labelText)) {
                errorMsg(ln, line, labelText.indexOf(' '), "duplicate label");
                exit(1);
            }
            System.out.printf(" %s", labelText);
            dlabels.put(labelText, 0);
        }
    }

    private static void labels() {
        int cptr = startingAddr;
        Pattern p = Pattern.compile("^[^0-9,();:.+\\-\\^$]+$");
        for (Section s : sections) {
            for (Iterator<Map.Entry<Integer, String>> iterator = s.iterator(); iterator.hasNext(); ) {
                Map.Entry<Integer, String> entry = iterator.next();
                int ln = entry.getKey();
                String line = entry.getValue();
                cptr = labels_processLine(cptr, s, p, ln, line);
            }
        }
    }

    private static int labels_processLine(int cptr, Section s, Pattern p, int ln, String line) {
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
            // s.repl(ln, line);
        }
        if (!line.isEmpty()) {
            Instruction insn = Instruction.fromString(line, 0, dlabels);
            Macro macro = macros.get(insn.id);
            if (macro != null) {
                for (String line2 : macro.substitute(insn.arguments)) {
                    cptr = labels_processLine(cptr, s, p, ln, line2);
                }
            } else {
                cptr += insn.getData(0).length;
            }
        }
        return cptr;
    }

    private static ByteBuffer assemble() {
        ByteBuffer buf = ByteBuffer.allocate(65536);
        int cptr = startingAddr;
        for (Section s : sections) {
            for (Iterator<Map.Entry<Integer, String>> iterator = s.iterator(); iterator.hasNext(); ) {
                Map.Entry<Integer, String> entry = iterator.next();
                String line = entry.getValue();
                Instruction insn = Instruction.fromString(line, cptr, labels);
                Macro macro = macros.get(insn.id);
                if (macro != null) {
                    for (String line2 : macro.substitute(insn.arguments)) {
                        Instruction insn2 = Instruction.fromString(line2, cptr, labels);
                        byte[] data = insn2.getData(cptr);
                        buf.put(data);
                        cptr += data.length;
                    }
                } else {
                    byte[] data = insn.getData(cptr);
                    buf.put(data);
                    cptr += data.length;
                }
            }
        }
        while (cptr % 0x80 != 0) {
            buf.put((byte) 0);
            cptr++;
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
        int offset = 0;
        int pos;
        if ((pos = s.indexOf('+')) != -1) {
            offset = parseInt(cut.substring(pos + 1));
            cut = cut.substring(0, pos);
        }
        int radix = 10;
        if (cut.startsWith("$")) {
            cut = cut.substring(1);
            radix = 16;
        }
        int i = 0;
        try {
            i = Integer.parseInt(cut, radix);
        } catch (Exception e) {
            System.err.printf("Could not parse integer '%s'%n", s);
            exit(1);
        }
        return i + offset;
    }

    public static String toString(int i, boolean force16b) {
        String s = Integer.toHexString(i & 0xFFFF);
        while ((s.length() & 1) != 0 || (force16b && s.length() < 4)) {
            s = "0" + s;
        }
        return "$" + s;
    }

}
