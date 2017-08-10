package modules.matrix;



public class MatrixBitWiseOperationTreeWalker {
	
	// treewalker
		public static void walk(MatrixBitWiseOperationTreeNodeElement node, IMatrixBitWiseOperationTreeWalkerListener listener,
				int level)  {
			
			if (node ==null) return;
			else {
				listener.entryAction(node, listener, level);
				walk(node.child1,listener,level+1);
				walk(node.child2,listener,level+1);
				listener.exitAction(node, listener,level);
			}
		}

}
