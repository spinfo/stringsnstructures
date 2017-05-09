package common.logicBits;


import java.util.BitSet;

public class LogOpAND implements ILogOp{
	public BitSet logOperation(BitSet in1,BitSet in2){
		
		BitSet res=new BitSet();res=in1;res.and(in2);
		return res;
	}
	

}
