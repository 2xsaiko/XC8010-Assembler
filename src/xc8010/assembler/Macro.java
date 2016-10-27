package xc8010.assembler;

import java.util.ArrayList;
import java.util.HashMap;

public class Macro {

    private HashMap<String, String> margs;
    private ArrayList<String> vals;
    private boolean compiling;

    public Macro(String... defs) {
        compiling = true;
        if (defs.length % 2 != 0) {
            System.out.println("Macro definition param array must be a multiple of 2");
            System.exit(1);
        }
        margs = new HashMap<>();
        vals = new ArrayList<>();
        for (int i = 0; i < defs.length; i += 2) {
            String name = defs[i];
            String defval = defs[i + 1];
            margs.put(name, defval);
        }
    }

    public void addInsn(String s) {
        vals.add(s);
    }

    public void compile() {
        compiling = false;
    }

    public boolean isCompiling() {
        return compiling;
    }
}
