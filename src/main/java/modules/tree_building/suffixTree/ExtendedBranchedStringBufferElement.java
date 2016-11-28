package modules.tree_building.suffixTree;

import java.util.BitSet;

public class ExtendedBranchedStringBufferElement extends BranchedStringBufferElement {
	
	
	BitSet leftRightBitSet, rightLeftBitSet;
	
	public ExtendedBranchedStringBufferElement(StringBuffer sb,
			BitSet res,BitSet leftRightBitSet,BitSet rightLeftBitSet){
		super(sb,res);
		this.leftRightBitSet=leftRightBitSet;
		this.rightLeftBitSet=rightLeftBitSet;
		
	}
	

}
