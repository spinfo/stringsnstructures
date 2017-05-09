package common.logicBits;

import java.util.BitSet;

public class LogOp {
	

	public static BitSet AND(BitSet in1,BitSet in2){		
		BitSet res=new BitSet();res.and(in1);res.or(in2);
		return res;
	}	
	

	public static BitSet OR(BitSet in1,BitSet in2){		
		BitSet res=new BitSet();res.or(in1);res.or(in2);
		return res;
	}	
	

	public static BitSet XOR(BitSet in1,BitSet in2){		
		BitSet res=new BitSet();res.xor(in1);res.or(in2);
		return res;
	}	

}
