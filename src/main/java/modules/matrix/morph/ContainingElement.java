package modules.matrix.morph;

import java.util.BitSet;

import modules.matrix.MatrixBitWiseOperationTreeNodeElement;

public class ContainingElement {
	MatrixBitWiseOperationTreeNodeElement containing; 
	MatrixBitWiseOperationTreeNodeElement contained; BitSet containedBitSet;
	boolean unDone;
	
	// cstr
	ContainingElement(MatrixBitWiseOperationTreeNodeElement el1, 
		MatrixBitWiseOperationTreeNodeElement el2, BitSet containing){
		this.containing=el1;
		this.contained=el2;
		this.containedBitSet=containing;
		this.unDone=true;
		
	}
	
}
