package common.logicBits;

import java.util.BitSet;

public class LogOp {
	

	public static BitSet AND(BitSet in1,BitSet in2){		
		BitSet res=new BitSet();res.or(in1);res.and(in2);
		return res;
	}	
	

	public static BitSet OR(BitSet in1,BitSet in2){		
		BitSet res=new BitSet();res.or(in1);res.or(in2);
		return res;
	}	
	

	public static BitSet XOR(BitSet in1,BitSet in2){		
		BitSet res=new BitSet();res.or(in1);res.xor(in2);
		return res;
	}	
	
	public static boolean containes(BitSet container,BitSet contained){
		BitSet res= OR(container,contained);
		return res.cardinality()==container.cardinality();
	}

}
