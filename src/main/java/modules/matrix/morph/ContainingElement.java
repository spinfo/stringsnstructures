package modules.matrix.morph;

import java.util.BitSet;

import modules.matrix.MatrixBitWiseOperationTreeNodeElement;

/* ContainingElement is an element for the list of contained (morphological) classes,
 * e.g. regier as containing verbal and compositional classes (+e, +st and, too, +bar).
 * The contained classes must completely cover the containing class 
 * 
 */

public class ContainingElement {
	public MatrixBitWiseOperationTreeNodeElement containing; 
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
