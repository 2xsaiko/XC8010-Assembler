package xc8010.assembler;

import java.util.regex.Pattern;

public enum AddressingMode {
    IMMEDIATE(1, "^#\\$[0-9A-Fa-f]{4}$"),
    IMMEDIATEB(1, "^#\\$[0-9A-Fa-f]{2}$"),
    ABSOLUTE(1, "^\\$[0-9A-Fa-f]{4}$"),
    ZERO_PAGE(1, "^\\$[0-9A-Fa-f]{2}$"),
    IMPLIED(0),
    INDIRECT_ABSOLUTE(1, "^\\(\\$[0-9A-Fa-f]{4}\\)$"),
    ABSOLUTE_INDEXED_X(2, "^\\$[0-9A-Fa-f]{4}$", "^[Xx]$"),
    ABSOLUTE_INDEXED_Y(2, "^\\$[0-9A-Fa-f]{4}$", "^[Yy]$"),
    ZERO_PAGE_INDEXED_X(2, "^\\$[0-9A-Fa-f]{2}$", "^[Xx]$"),
    ZERO_PAGE_INDEXED_Y(2, "^\\$[0-9A-Fa-f]{2}$", "^[Yy]$"),
    INDEXED_INDIRECT(2, "^\\(\\$[0-9A-Fa-f]{4}\\)$", "^[Xx]$"),
    INDIRECT_INDEXED(2, "^\\(\\$[0-9A-Fa-f]{4}\\)$", "^[Yy]$"),
    STACK_RELATIVE(2, "^\\$[0-9A-Fa-f]{2}$", "^[Ss]$"),
    RETURN_STACK_RELATIVE(2, "^\\$[0-9A-Fa-f]{2}$", "^[Rr]$"),
    INDIRECT_STACK_RELATIVE(3, "^\\(\\$[0-9A-Fa-f]{2}$", "^[Ss]\\)$", "^[Yy]$"),
    INDIRECT_RETURN_STACK_RELATIVE(3, "^\\(\\$[0-9A-Fa-f]{2}$", "^[Rr]\\)$", "^[Yy]$"),
    ACCUMULATOR(1, "^[Aa]$"),
    RELATIVE(1),
    UNKNOWN;

    private final int numArgs;
    private final Pattern[] patterns;

    AddressingMode(int numArgs, String... regex) {
        this.numArgs = numArgs;
        this.patterns = new Pattern[numArgs];
        if (regex.length != 0)
            for (int i = 0; i < patterns.length; i++) {
                patterns[i] = Pattern.compile(regex[i]);
            }
    }

    AddressingMode() {
        numArgs = -1;
        patterns = null;
    }

    public static AddressingMode getAddressingMode(Instruction insn) {
        for (AddressingMode am : values()) {
            if (am.numArgs != insn.arguments.length)
                continue;
            boolean flag = true;
            for (int i = 0; i < insn.arguments.length; i++) {
                if (am.patterns[i] != null) flag &= am.patterns[i].matcher(insn.arguments[i]).matches();
                else flag = false;
            }
            if (flag) {
                if (am == ABSOLUTE && Instruction.relativeMap.contains(insn.id)) {
                    return RELATIVE;
                }
                return am;
            }
        }
        return UNKNOWN;
    }

}