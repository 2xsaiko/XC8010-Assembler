package xc8010.assembler;

import java.util.ArrayList;

public interface ISerializer {

	public void accept(Instruction insn, int cptr, ArrayList<Byte> list);
	
}
