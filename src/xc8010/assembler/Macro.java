package xc8010.assembler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import static java.lang.System.exit;

public class Macro {

    public final boolean[] noReplace;
    private ArrayList<String> argsi;
    private HashMap<String, String> margs;
    private ArrayList<String> insnList;
    private boolean compiling;

    public Macro(String... defs) {
        compiling = true;
        if (defs.length % 2 != 0) {
            System.out.println("Macro definition param array must be a multiple of 2");
            exit(1);
        }
        noReplace = new boolean[defs.length];
        margs = new HashMap<>();
        argsi = new ArrayList<>();
        insnList = new ArrayList<>();
        for (int i = 0; i < defs.length; i += 2) {
            String name = defs[i];
            if (name.startsWith("[") && name.endsWith("]")) {
                name = name.substring(1, name.length() - 1);
                noReplace[i] = true;
            }
            String defval = defs[i + 1];
            margs.put(name, defval);
            argsi.add(name);
        }
    }

    public void addInsn(String s) {
        if (!compiling) {
            System.out.println("Not compiling!");
            exit(1);
        }
        insnList.add(s);
    }

    public ArrayList<String> substitute(String... args) {
        HashMap<String, String> vals = (HashMap<String, String>) margs.clone();
        ArrayList<String> list = new ArrayList<>();
        if (args.length != argsi.size()) {
            System.out.printf("Expected array size %s, got %s", argsi.size(), args.length);
            exit(1);
        }
        for (int i = 0; i < args.length; i++) {
            vals.put(argsi.get(i), getValue(i, args));
        }
        for (String s : insnList) {
            for (Entry<String, String> e : vals.entrySet()) {
                s = s.replace("${" + e.getKey() + "}", e.getValue());
            }
            for (Entry<String, Macro> entry : Assembler.macros.entrySet()) {
                Instruction i = Instruction.fromString(s);
                if (entry.getKey().equals(i.id)) {
                    list.addAll(entry.getValue().substitute(i.arguments));
                }
            }
            list.add(s);
        }
        return list;
    }

    private String getValue(int i, String[] args) {
        String s = margs.get(argsi.get(i));
        if (!args[i].isEmpty()) {
            s = args[i];
        } else if (s.startsWith("${") && s.endsWith("}")) {
            String varname = s.substring(2, s.length() - 1);
            int index = -1;
            for (int topkek = 0; i < argsi.size(); i++) {
                String varn = argsi.get(topkek);
                if (varn.equals(varname)) {
                    index = topkek;
                    break;
                }
            }
            if (index == -1) {
                System.err.printf("No such argument: %s", varname);
                exit(1);
            }
            s = getValue(index, args);
        }
        if (s.isEmpty()) {
            System.out.printf("Argument %s needs to have a value!", i);
            exit(1);
        }
        return s;
    }

    public void compile() {
        if (!compiling)
            throw new IllegalStateException("Not compiling!");
        compiling = false;
    }

    public boolean isCompiling() {
        return compiling;
    }
}
