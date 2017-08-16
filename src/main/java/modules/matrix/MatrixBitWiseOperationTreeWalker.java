package modules.matrix;

import java.io.PrintWriter;

import models.NamedFieldMatrix;

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
		
		
		// quick and dirty walker, must bee beautified with listener above
		
		private static void walk_for_write(MatrixBitWiseOperationTreeNodeElement node,
			NamedFieldMatrix m, PrintWriter p){
			if (node ==null) return;
			else if (node.child1==null){
				String morph=m.getRowName(node.fromNamedFieldMatrixRow);
				 p.println("Morphological classelement: "+morph);
			}
		}
		
		public static void walk_for_classes(MatrixBitWiseOperationTreeNodeElement node,
				NamedFieldMatrix m,
				PrintWriter p)  {
			
			if (node ==null) return;
			else {
				
				int andCardinality=node.and.cardinality();
				int orCardinality=node.or.cardinality();
				if ((andCardinality==0)||(orCardinality/andCardinality<0.5)){
				    p.println("Morphological class: ");
					walk_for_classes(node.child1,m,p);
					walk_for_classes(node.child2,m,p);
				} else
				{
					walk_for_write(node,m,p);
				}
				
			}
		}

}
