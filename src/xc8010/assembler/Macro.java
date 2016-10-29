package xc8010.assembler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import static java.lang.System.exit;

public class Macro {

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
        margs = new HashMap<>();
        argsi = new ArrayList<>();
        insnList = new ArrayList<>();
        for (int i = 0; i < defs.length; i += 2) {
            String name = defs[i];
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
        System.out.println(s);
    }

    public ArrayList<String> substitute(String... args) {
        HashMap<String, String> vals = (HashMap<String, String>) margs.clone();
        ArrayList<String> list = new ArrayList<>();
        if (args.length != argsi.size()) {
            System.out.printf("Expected array size %s, got %s", argsi.size(), args.length);
            exit(1);
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null)
                vals.put(argsi.get(i), args[i]);
            if (vals.get(argsi.get(i)) == null) {
                System.out.printf("Argument %s needs to have a value!", i);
                exit(1);
            }
        }
        for (String s : insnList) {
            for (Entry<String, String> e : vals.entrySet()) {
                s = s.replace("${" + e.getKey() + "}", e.getValue());
            }
            list.add(s);
        }
        return list;
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
