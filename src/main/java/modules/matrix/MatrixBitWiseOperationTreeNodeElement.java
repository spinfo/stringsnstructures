package modules.matrix;

import java.util.BitSet;

public class MatrixBitWiseOperationTreeNodeElement {
	MatrixBitWiseOperationTreeNodeElement child1, child2,mother,root;
	public BitSet contextBitSet;
	int fromNamedFieldMatrixRow;
	
	//cstr
	public MatrixBitWiseOperationTreeNodeElement(BitSet context,int row,
			MatrixBitWiseOperationTreeNodeElement child1,
			MatrixBitWiseOperationTreeNodeElement child2){
		
		this.contextBitSet=context;
		this.fromNamedFieldMatrixRow=row;
		this.child1=child1;
		this.child2=child2;	
		this.mother=null;
		this.root=this; //root maybe root of partial tree, 
		// s. use in MatrixDynamicMorphClustering class
		// first it is set to itself; thus there is no need to check whether root is null
		// e.g. in MatrixDynamicMorphClustering.searchBestPairForTree_1.
	}// cstr
	
	
	
	// this type of treewalker is too specialized. It might be replaced by
	// a treewalker with an interface typed parameter or (better) with an interface for
	// a listener which listens to events generated by an entryAction or a exitaction
	
	public void walk(MatrixBitWiseOperationTreeNodeElement treeNode, 
			MatrixBitWiseOperationTreeNodeElement node){
		
		if (treeNode !=null){
			//entryAction
			//---------------
			// too specialized !
			// set root of partial tree (i.e. node)
			treeNode.root=node;
			// depth first
			walk(treeNode.child1,node);
			// bredth second
			walk(treeNode.child2,node);
			// exitAction
		}
		
	}
	
	


}
