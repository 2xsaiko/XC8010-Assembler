package xc8010.assembler;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Section implements Comparable<Section> {

	private String name;
	private ArrayList<String> instructions;
	private ArrayList<Integer> insnLines;

	public Section(String name) {
		this.name = name;
		instructions = new ArrayList<>();
		insnLines = new ArrayList<>();
	}

	public void putInsn(int ln, String text) {
		insnLines.add(ln);
		instructions.add(text.trim());
	}

	public void repl(int ln, String text) {
		for (int i = 0; i < instructions.size(); i++) {
			int lnc = insnLines.get(i);
			if (lnc == ln) {
				instructions.remove(i);
				if (text.isEmpty())
					text = null;
				instructions.add(i, text);
				break;
			}
		}
	}

	public Iterator<Entry<Integer, String>> iterator() {
		return new Iterator<Map.Entry<Integer, String>>() {

			int pos = 0;

			@Override
			public boolean hasNext() {
				return pos < instructions.size();
			}

			@Override
			public Entry<Integer, String> next() {
				String insn = null;
				while (insn == null) {
					pos++;
					insn = instructions.get(pos - 1);
				}
				return new AbstractMap.SimpleEntry(insnLines.get(pos - 1), insn);
			}
		};
	}

	@Override
	public int compareTo(Section o) {
		if (name.equals(o.name))
			return 0;
		if (".text".equals(name))
			return -1;
		if (".text".equals(o.name))
			return 1;
		if (".data".equals(name))
			return -1;
		if (".data".equals(o.name))
			return 1;
		return name.compareTo(o.name);
	}

}
