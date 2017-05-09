package common.logicBits;

import java.util.BitSet;


public class LogOpXOR implements ILogOp{
	public BitSet logOperation(BitSet in1,BitSet in2){		
		BitSet res=new BitSet();res.xor(in1);res.or(in2);
		return res;
	}
	
}

