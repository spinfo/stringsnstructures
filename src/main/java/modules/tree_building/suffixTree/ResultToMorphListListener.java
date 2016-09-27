package modules.tree_building.suffixTree;

/**
 * @author JR
 * @version 1.0
 */

import java.io.IOException;
import java.util.Stack;

public class ResultToMorphListListener  implements ITreeWalkerListener{
	
	// the suffix tree this will work on
	private final BaseSuffixTree tree;
	private boolean inverted = true;
	private Stack<Integer> nodeNrs = null;
	// the length of the path currently read
	private int lengthOfPath;
	
	public ResultToMorphListListener(BaseSuffixTree suffixTree, boolean inverted) {
		this.tree = suffixTree;
	
	}
	
	@Override
	//	copy from ResultToFiniteStateMachineListener
	public void entryaction(int nodeNr, int level) throws IOException {
		this.nodeNrs.push(nodeNr);
		this.lengthOfPath = this.lengthOfPath + 
		this.tree.getNode(nodeNr).getEnd(0) - tree.getNode(nodeNr).getStart(0);
	
}

	@Override
	public void exitaction(int nodeNr, int level) throws IOException {
	// if the current node is a leaf of a whole input text, write out stack
		if (nodeIsLeafOfWholeInputText(nodeNr, this.lengthOfPath)) {
			int anf, end, node;
			System.out.println();
			if (this.inverted)
				{
				for (int i=this.nodeNrs.size()-1;i>=0;i-- )
					{ node=this.nodeNrs.get(i);
					  anf=tree.getNode(node).getStart(0);
					  end=tree.getNode(node).getEnd(0)-1;
					  for(int pos=end;pos>=anf;pos--)
						  System.out.print(this.tree.text[pos]);
					  System.out.print(" ");
					}
					
				
				}
			else {
				for (int i=0;i<this.nodeNrs.size();i++)
				
				{
					node=this.nodeNrs.get(i);
					anf=tree.getNode(node).getStart(0);
					end=tree.getNode(node).getEnd(0)-1;
					 for(int pos=anf;pos<end;pos++)
						  System.out.print(this.tree.text[pos]);
					  System.out.print(" ");
					
				}
				
			};

				
		}
	
		this.nodeNrs.pop();

	}
	
	// checks if the given node in this listeners tree corresponds to a whole
		// input text given the current path length.
		private boolean nodeIsLeafOfWholeInputText(int nodeNr, int pathLength) {
			Node node = this.tree.getNode(nodeNr);

			if (!node.isTerminal()) {
				return false;
			}

			for (NodePosition position : node.getPositions()) {
				if (position.getEnd() == tree.getTextBegin(position.getTextNr()) + pathLength) {
					return true;
				}
			}

			return false;
		}
	
		public void testOut(){
			System.out.println("----------TEST-----------");
		}
}
	
	


