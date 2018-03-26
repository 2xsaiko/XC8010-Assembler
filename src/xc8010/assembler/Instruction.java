package xc8010.assembler;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.System.exit;

public class Instruction {

    public static final ArrayList<String> relativeMap;
    private static final HashMap<String, HashMap<AddressingMode, Integer>> opcodeMap;
    private static final HashMap<AddressingMode, ISerializer> serializers;

    static {
        relativeMap = new ArrayList<>();
        relativeMap.add("bcc");
        relativeMap.add("bcs");
        relativeMap.add("beq");
        relativeMap.add("bmi");
        relativeMap.add("bne");
        relativeMap.add("bpl");
        relativeMap.add("bvc");
        relativeMap.add("bvs");
        relativeMap.add("bra");

        opcodeMap = new HashMap<>();
        registerOpcode("brk", AddressingMode.IMPLIED, 0x00);
        registerOpcode("ora", AddressingMode.INDIRECT_INDEXED, 0x01);
        registerOpcode("nxt", AddressingMode.IMPLIED, 0x02);
        registerOpcode("ora", AddressingMode.STACK_RELATIVE, 0x03);
        registerOpcode("tsb", AddressingMode.ZERO_PAGE, 0x04);
        registerOpcode("ora", AddressingMode.ZERO_PAGE, 0x05);
        registerOpcode("asl", AddressingMode.ZERO_PAGE, 0x06);
        registerOpcode("ora", AddressingMode.RETURN_STACK_RELATIVE, 0x07);
        registerOpcode("php", AddressingMode.IMPLIED, 0x08);
        registerOpcode("ora", AddressingMode.IMMEDIATE, 0x09);
        registerOpcode("asl", AddressingMode.ACCUMULATOR, 0x0A);
        registerOpcode("rhi", AddressingMode.IMPLIED, 0x0B);
        registerOpcode("tsb", AddressingMode.ABSOLUTE, 0x0C);
        registerOpcode("ora", AddressingMode.ABSOLUTE, 0x0D);
        registerOpcode("asl", AddressingMode.ABSOLUTE, 0x0E);
        registerOpcode("mul", AddressingMode.ZERO_PAGE, 0x0F);
        registerOpcode("bpl", AddressingMode.RELATIVE, 0x10);
        registerOpcode("ora", AddressingMode.INDEXED_INDIRECT, 0x11);
        registerOpcode("ora", AddressingMode.INDIRECT_ABSOLUTE, 0x12);
        registerOpcode("ora", AddressingMode.INDIRECT_STACK_RELATIVE, 0x13);
        registerOpcode("trb", AddressingMode.ZERO_PAGE, 0x14);
        registerOpcode("ora", AddressingMode.ZERO_PAGE_INDEXED_X, 0x15);
        registerOpcode("asl", AddressingMode.ZERO_PAGE_INDEXED_X, 0x16);
        registerOpcode("ora", AddressingMode.INDIRECT_RETURN_STACK_RELATIVE, 0x17);
        registerOpcode("clc", AddressingMode.IMPLIED, 0x18);
        registerOpcode("ora", AddressingMode.ABSOLUTE_INDEXED_Y, 0x19);
        registerOpcode("inc", AddressingMode.ACCUMULATOR, 0x1A);
        registerOpcode("rhx", AddressingMode.IMPLIED, 0x1B);
        registerOpcode("trb", AddressingMode.ABSOLUTE, 0x1C);
        registerOpcode("ora", AddressingMode.ABSOLUTE_INDEXED_X, 0x1D);
        registerOpcode("asl", AddressingMode.ABSOLUTE_INDEXED_X, 0x1E);
        registerOpcode("mul", AddressingMode.ZERO_PAGE_INDEXED_X, 0x1F);
        registerOpcode("jsr", AddressingMode.ABSOLUTE, 0x20);
        registerOpcode("and", AddressingMode.INDEXED_INDIRECT, 0x21);
        registerOpcode("ent", AddressingMode.IMPLIED, 0x22);
        registerOpcode("ent", AddressingMode.ABSOLUTE, 0x22);
        registerOpcode("and", AddressingMode.STACK_RELATIVE, 0x23);
        registerOpcode("bit", AddressingMode.ZERO_PAGE, 0x24);
        registerOpcode("and", AddressingMode.ZERO_PAGE, 0x25);
        registerOpcode("rol", AddressingMode.ZERO_PAGE, 0x26);
        registerOpcode("and", AddressingMode.RETURN_STACK_RELATIVE, 0x27);
        registerOpcode("plp", AddressingMode.IMPLIED, 0x28);
        registerOpcode("and", AddressingMode.IMMEDIATE, 0x29);
        registerOpcode("rol", AddressingMode.ACCUMULATOR, 0x2A);
        registerOpcode("rli", AddressingMode.IMPLIED, 0x2B);
        registerOpcode("bit", AddressingMode.ABSOLUTE, 0x2C);
        registerOpcode("and", AddressingMode.ABSOLUTE, 0x2D);
        registerOpcode("rol", AddressingMode.ABSOLUTE, 0x2E);
        registerOpcode("mul", AddressingMode.ABSOLUTE, 0x2F);
        registerOpcode("bmi", AddressingMode.RELATIVE, 0x30);
        registerOpcode("and", AddressingMode.INDIRECT_INDEXED, 0x31);
        registerOpcode("and", AddressingMode.INDIRECT_ABSOLUTE, 0x32);
        registerOpcode("and", AddressingMode.INDIRECT_STACK_RELATIVE, 0x33);
        registerOpcode("bit", AddressingMode.ZERO_PAGE_INDEXED_X, 0x34);
        registerOpcode("and", AddressingMode.ZERO_PAGE_INDEXED_X, 0x35);
        registerOpcode("rol", AddressingMode.ZERO_PAGE_INDEXED_X, 0x36);
        registerOpcode("and", AddressingMode.INDIRECT_RETURN_STACK_RELATIVE, 0x37);
        registerOpcode("sec", AddressingMode.IMPLIED, 0x38);
        registerOpcode("and", AddressingMode.ABSOLUTE_INDEXED_Y, 0x39);
        registerOpcode("dec", AddressingMode.ACCUMULATOR, 0x3A);
        registerOpcode("rlx", AddressingMode.IMPLIED, 0x3B);
        registerOpcode("bit", AddressingMode.ABSOLUTE_INDEXED_X, 0x3C);
        registerOpcode("and", AddressingMode.ABSOLUTE_INDEXED_X, 0x3D);
        registerOpcode("rol", AddressingMode.ABSOLUTE_INDEXED_X, 0x3E);
        registerOpcode("mul", AddressingMode.ABSOLUTE_INDEXED_X, 0x3F);
        registerOpcode("nxa", AddressingMode.IMPLIED, 0x42);
        registerOpcode("eor", AddressingMode.STACK_RELATIVE, 0x43);
        registerOpcode("eor", AddressingMode.ZERO_PAGE, 0x45);
        registerOpcode("eor", AddressingMode.RETURN_STACK_RELATIVE, 0x47);
        registerOpcode("pha", AddressingMode.IMPLIED, 0x48);
        registerOpcode("eor", AddressingMode.IMMEDIATE, 0x49);
        registerOpcode("eor", AddressingMode.IMMEDIATEB, 0x49);
        registerOpcode("rha", AddressingMode.IMPLIED, 0x4B);
        registerOpcode("jmp", AddressingMode.ABSOLUTE, 0x4C);
        registerOpcode("bvc", AddressingMode.RELATIVE, 0x50);
        registerOpcode("phy", AddressingMode.IMPLIED, 0x5A);
        registerOpcode("rhy", AddressingMode.IMPLIED, 0x5B);
        registerOpcode("txi", AddressingMode.IMPLIED, 0x5C);
        registerOpcode("rts", AddressingMode.IMPLIED, 0x60);
        registerOpcode("adc", AddressingMode.STACK_RELATIVE, 0x63);
        registerOpcode("stz", AddressingMode.ZERO_PAGE, 0x64);
        registerOpcode("adc", AddressingMode.ZERO_PAGE, 0x65);
        registerOpcode("adc", AddressingMode.RETURN_STACK_RELATIVE, 0x67);
        registerOpcode("pla", AddressingMode.IMPLIED, 0x68);
        registerOpcode("adc", AddressingMode.IMMEDIATE, 0x69);
        registerOpcode("adc", AddressingMode.IMMEDIATEB, 0x69);
        registerOpcode("bvs", AddressingMode.RELATIVE, 0x70);
        registerOpcode("ror", AddressingMode.ACCUMULATOR, 0x6A);
        registerOpcode("rla", AddressingMode.IMPLIED, 0x6B);
        registerOpcode("adc", AddressingMode.ABSOLUTE, 0x6D);
        registerOpcode("stz", AddressingMode.ZERO_PAGE_INDEXED_X, 0x74);
        registerOpcode("adc", AddressingMode.ABSOLUTE_INDEXED_Y, 0x79);
        registerOpcode("ply", AddressingMode.IMPLIED, 0x7A);
        registerOpcode("rly", AddressingMode.IMPLIED, 0x7B);
        registerOpcode("jmp", AddressingMode.INDEXED_INDIRECT, 0x7C);
        registerOpcode("adc", AddressingMode.ABSOLUTE_INDEXED_X, 0x7D);
        registerOpcode("div", AddressingMode.ABSOLUTE_INDEXED_X, 0x7F);
        registerOpcode("bra", AddressingMode.RELATIVE, 0x80);
        registerOpcode("sty", AddressingMode.ZERO_PAGE, 0x84);
        registerOpcode("sta", AddressingMode.ZERO_PAGE, 0x85);
        registerOpcode("stx", AddressingMode.ZERO_PAGE, 0x86);
        registerOpcode("dey", AddressingMode.IMPLIED, 0x88);
        registerOpcode("bit", AddressingMode.IMMEDIATE, 0x89);
        registerOpcode("txa", AddressingMode.IMPLIED, 0x8A);
        registerOpcode("txr", AddressingMode.IMPLIED, 0x8B);
        registerOpcode("sty", AddressingMode.ABSOLUTE, 0x8C);
        registerOpcode("sta", AddressingMode.ABSOLUTE, 0x8D);
        registerOpcode("stx", AddressingMode.ABSOLUTE, 0x8E);
        registerOpcode("zea", AddressingMode.IMPLIED, 0x8F);
        registerOpcode("bcc", AddressingMode.RELATIVE, 0x90);
        registerOpcode("sta", AddressingMode.INDIRECT_INDEXED, 0x91);
        registerOpcode("sta", AddressingMode.INDIRECT_ABSOLUTE, 0x92);
        registerOpcode("sta", AddressingMode.INDIRECT_STACK_RELATIVE, 0x93);
        registerOpcode("sty", AddressingMode.ZERO_PAGE_INDEXED_X, 0x94);
        registerOpcode("sta", AddressingMode.ZERO_PAGE_INDEXED_X, 0x95);
        registerOpcode("tya", AddressingMode.IMPLIED, 0x98);
        registerOpcode("sta", AddressingMode.ABSOLUTE_INDEXED_Y, 0x99);
        registerOpcode("txs", AddressingMode.IMPLIED, 0x9A);
        registerOpcode("stz", AddressingMode.ABSOLUTE, 0x9C);
        registerOpcode("sta", AddressingMode.ABSOLUTE_INDEXED_X, 0x9D);
        registerOpcode("stz", AddressingMode.ABSOLUTE_INDEXED_X, 0x9E);
        registerOpcode("sea", AddressingMode.IMPLIED, 0x9F);
        registerOpcode("ldy", AddressingMode.IMMEDIATE, 0xA0);
        registerOpcode("ldy", AddressingMode.IMMEDIATEB, 0xA0);
        registerOpcode("ldx", AddressingMode.IMMEDIATE, 0xA2);
        registerOpcode("ldx", AddressingMode.IMMEDIATEB, 0xA2);
        registerOpcode("lda", AddressingMode.STACK_RELATIVE, 0xA3);
        registerOpcode("lda", AddressingMode.ZERO_PAGE, 0xA5);
        registerOpcode("lda", AddressingMode.RETURN_STACK_RELATIVE, 0xA7);
        registerOpcode("tay", AddressingMode.IMPLIED, 0xA8);
        registerOpcode("lda", AddressingMode.IMMEDIATE, 0xA9);
        registerOpcode("lda", AddressingMode.IMMEDIATEB, 0xA9);
        registerOpcode("tax", AddressingMode.IMPLIED, 0xAA);
        registerOpcode("trx", AddressingMode.IMPLIED, 0xAB);
        registerOpcode("lda", AddressingMode.ABSOLUTE, 0xAD);
        registerOpcode("ldx", AddressingMode.ABSOLUTE, 0xAE);
        registerOpcode("bcs", AddressingMode.RELATIVE, 0xB0);
        registerOpcode("lda", AddressingMode.INDIRECT_INDEXED, 0xB1);
        registerOpcode("lda", AddressingMode.INDIRECT_STACK_RELATIVE, 0xB3);
        registerOpcode("lda", AddressingMode.ABSOLUTE_INDEXED_Y, 0xB9);
        registerOpcode("tsx", AddressingMode.IMPLIED, 0xBA);
        registerOpcode("lda", AddressingMode.ABSOLUTE_INDEXED_X, 0xBD);
        registerOpcode("ldx", AddressingMode.ABSOLUTE_INDEXED_Y, 0xBE);
        registerOpcode("cpy", AddressingMode.IMMEDIATE, 0xC0);
        registerOpcode("cpy", AddressingMode.IMMEDIATEB, 0xC0);
        registerOpcode("rep", AddressingMode.IMMEDIATEB, 0xC2);
        registerOpcode("cmp", AddressingMode.STACK_RELATIVE, 0xC3);
        registerOpcode("dec", AddressingMode.ZERO_PAGE, 0xC6);
        registerOpcode("cmp", AddressingMode.RETURN_STACK_RELATIVE, 0xC7);
        registerOpcode("iny", AddressingMode.IMPLIED, 0xC8);
        registerOpcode("cmp", AddressingMode.IMMEDIATE, 0xC9);
        registerOpcode("cmp", AddressingMode.IMMEDIATEB, 0xC9);
        registerOpcode("dex", AddressingMode.IMPLIED, 0xCA);
        registerOpcode("wai", AddressingMode.IMPLIED, 0xCB);
        registerOpcode("pld", AddressingMode.IMPLIED, 0xCF);
        registerOpcode("bne", AddressingMode.RELATIVE, 0xD0);
        registerOpcode("phx", AddressingMode.IMPLIED, 0xDA);
        registerOpcode("stp", AddressingMode.IMPLIED, 0xDB);
        registerOpcode("tix", AddressingMode.IMPLIED, 0xDC);
        registerOpcode("cmp", AddressingMode.ABSOLUTE_INDEXED_X, 0xDD);
        registerOpcode("dec", AddressingMode.ABSOLUTE_INDEXED_X, 0xDE);
        registerOpcode("phd", AddressingMode.IMPLIED, 0xDF);
        registerOpcode("sep", AddressingMode.IMMEDIATEB, 0xE2);
        registerOpcode("sbc", AddressingMode.STACK_RELATIVE, 0xE3);
        registerOpcode("inc", AddressingMode.ZERO_PAGE, 0xE6);
        registerOpcode("sbc", AddressingMode.RETURN_STACK_RELATIVE, 0xE7);
        registerOpcode("xba", AddressingMode.IMPLIED, 0xEB);
        registerOpcode("inc", AddressingMode.ABSOLUTE, 0xEE);
        registerOpcode("mmu", AddressingMode.ZERO_PAGE, 0xEF);
        registerOpcode("beq", AddressingMode.RELATIVE, 0xF0);
        registerOpcode("plx", AddressingMode.IMPLIED, 0xFA);
        registerOpcode("xce", AddressingMode.IMPLIED, 0xFB);
        registerOpcode("inc", AddressingMode.ABSOLUTE_INDEXED_X, 0xFE);

        serializers = new HashMap<>();
        serializers.put(AddressingMode.IMMEDIATE, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0].substring(1));
            list.add((byte) (val & 0xFF));
            list.add((byte) (val >> 8));
        });
        serializers.put(AddressingMode.IMMEDIATEB, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0].substring(1));
            list.add((byte) (val & 0xFF));
        });
        serializers.put(AddressingMode.ZERO_PAGE, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0]);
            list.add((byte) (val & 0xFF));
        });
        serializers.put(AddressingMode.ZERO_PAGE_INDEXED_X, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0]);
            list.add((byte) (val & 0xFF));
        });
        serializers.put(AddressingMode.ABSOLUTE, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0]);
            list.add((byte) (val & 0xFF));
            list.add((byte) (val >> 8));
        });
        serializers.put(AddressingMode.INDIRECT_ABSOLUTE, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0].substring(1, 4));
            list.add((byte) (val & 0xFF));
        });
        serializers.put(AddressingMode.ABSOLUTE_INDEXED_X, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0]);
            list.add((byte) (val & 0xFF));
            list.add((byte) (val >> 8));
        });
        serializers.put(AddressingMode.ABSOLUTE_INDEXED_Y, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0]);
            list.add((byte) (val & 0xFF));
            list.add((byte) (val >> 8));
        });
        serializers.put(AddressingMode.RELATIVE, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0]);
            val -= cptr + list.size() + 1;
            list.add((byte) val);
        });
        serializers.put(AddressingMode.INDEXED_INDIRECT, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0].substring(1));
            list.add((byte) (val & 0xFF));
        });
        serializers.put(AddressingMode.INDIRECT_INDEXED, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0].substring(1, 4));
            list.add((byte) (val & 0xFF));
        });
        serializers.put(AddressingMode.STACK_RELATIVE, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0]);
            list.add((byte) (val & 0xFF));
        });
        serializers.put(AddressingMode.RETURN_STACK_RELATIVE, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0]);
            list.add((byte) (val & 0xFF));
        });
        serializers.put(AddressingMode.INDIRECT_STACK_RELATIVE, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0].substring(1));
            list.add((byte) (val & 0xFF));
        });
        serializers.put(AddressingMode.INDIRECT_RETURN_STACK_RELATIVE, (insn, cptr, list) -> {
            int val = Assembler.parseInt(insn.arguments[0].substring(1));
            list.add((byte) (val & 0xFF));
        });
        serializers.put(AddressingMode.IMPLIED, (insn, cptr, list) -> {
        });
        serializers.put(AddressingMode.ACCUMULATOR, (insn, cptr, list) -> {
        });
    }

    public final String id;
    public final String[] arguments;

    private Instruction(String id, String[] arguments) {
        this.id = id;
        this.arguments = arguments;
    }

    public static Instruction fromString(String insnLine, int cptr, HashMap<String, Integer> labels) {
        ArrayList<String> sortedVarList = new ArrayList<>();
        HashMap<String, Integer> allVars = new HashMap<>();
        if (Assembler.vars != null) {
            allVars.putAll(Assembler.vars);
        }
        if (labels != null) {
            allVars.putAll(labels);
        }
        sortedVarList.addAll(allVars.keySet());
        sortedVarList.sort((o1, o2) -> o2.compareTo(o1));
        String t = insnLine.trim();
        String id;
        String[] arguments;

        // strip out labels
        int end = t.indexOf(':');
        if (end > 1 && t.charAt(end - 1) == '\\') end = -1;
        t = t.substring(end + 1).trim();

        end = t.indexOf(' ');
        if (end == -1) {
            id = t;
            arguments = new String[0];
        } else {
            ArrayList<String> args = new ArrayList<>();
            id = t.substring(0, end);
            t = t.substring(end + 1).trim();
            String crt = "";
            boolean literal = false;
            for (char c : t.toCharArray()) {
                if (literal) {
                    literal = false;
                    crt += c;
                    continue;
                } else if (c == '\\') {
                    literal = true;
                    continue;
                }
                if (c == ',') {
                    args.add(crt.trim());
                    crt = "";
                    continue;
                }
                crt += c;
            }
            args.add(crt.trim());
            arguments = new String[args.size()];
            args.toArray(arguments);
            Macro macro = Assembler.macros.get(id);
            if (!sortedVarList.isEmpty())
                for (int i = 0; i < arguments.length; i++) {
                    if (i == 0 && ".set".equals(id)) continue;
                    if ("db".equals(id) && arguments[i].startsWith("'") && arguments[i].endsWith("'")) continue;
                    if (macro != null) {
                        if (macro.noReplace[i]) continue;
                    }
                    String arg = arguments[i];
                    int ind = Integer.MAX_VALUE;
                    int len = 0;
                    int ct = 0;
                    for (String string : sortedVarList) {
                        int j;
                        if ((j = arg.indexOf(string)) < ind && j != -1) {
                            ind = j;
                            len = string.length();
                            ct = allVars.get(string);
                        }
                    }
                    if (ind != Integer.MAX_VALUE) {
                        arg = arg.substring(0, ind) + Assembler.toString(ct, true) + arg.substring(ind + len);
                        arguments[i] = arg;
                    }
                }
        }
        return new Instruction(id.toLowerCase(), arguments);

    }

    public static Instruction fromString(String insnLine) {
        return fromString(insnLine, 0, null);
    }

    private static byte[] toBytes(ArrayList<Byte> data) {
        byte[] dres = new byte[data.size()];
        int i = 0;
        for (byte b : data) {
            dres[i++] = b;
        }
        return dres;
    }

    private static void registerOpcode(String insnId, AddressingMode am, int dat) {
        HashMap<AddressingMode, Integer> lvl2 = opcodeMap.get(insnId);
        if (lvl2 == null) {
            lvl2 = new HashMap<>();
            opcodeMap.put(insnId, lvl2);
        }
        if (lvl2.containsKey(am)) {
            System.out.printf("Duplicate addressing mode %s for instruction %s%n", am, insnId);
            exit(1);
        }
        lvl2.put(am, dat);
    }

    private static int getOpcode(String insnId, AddressingMode am) {
        HashMap<AddressingMode, Integer> lvl2 = opcodeMap.get(insnId);
        if (lvl2 == null) {
            System.out.printf("Invalid opcode %s (addressing mode %s)", insnId, am);
            exit(1);
        }
        if (!lvl2.containsKey(am)) {
            System.out.printf("Invalid addressing mode %s for instruction %s%n", am, insnId);
            exit(1);
        }
        return lvl2.get(am);
    }

    public byte[] getData(int cptr) {
        if (id.isEmpty())
            return new byte[0];
        ArrayList<Byte> data = new ArrayList<>(4);
        if (i("db")) {
            for (String arg : arguments) {
                if (arg.startsWith("'") && arg.endsWith("'")) {
                    byte[] bytes = Assembler.stringBytes(arg.substring(1, arg.length() - 1));
                    for (byte b : bytes) {
                        data.add(b);
                    }
                } else {
                    int i;
                    if (arg.startsWith("^")) {
                        i = Assembler.parseInt(arg.substring(1)) >> 8;
                    } else {
                        i = Assembler.parseInt(arg) & 0xFF;
                    }
                    data.add((byte) i);
                }
            }
        } else if (i(".set")) {
            Assembler.vars.put(arguments[0], Assembler.parseInt(arguments[1]));
            return new byte[0];
        } else {
            AddressingMode am = AddressingMode.getAddressingMode(this);
            int opcode = getOpcode(id, am);
            data.add((byte) opcode);
            ISerializer s = serializers.get(am);
            if (s == null) {
                System.err.printf("Unimplemented addressing mode %s", am);
                exit(1);
            }
            s.accept(this, cptr, data);
        }
        return toBytes(data);
    }

    private boolean i(String expected) {
        return expected.equals(id);
    }

}
