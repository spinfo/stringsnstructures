package suffixTree.suffixTree.applications;

import java.util.logging.Logger;

import suffixTree.suffixTree.applications.event.MyEntryEvent;
import suffixTree.suffixTree.applications.event.MyExitEvent;
import suffixTree.suffixTree.node.Node;


public class ResultListenerSuffixTreePath  implements ITreeWalkerListener{
	
	private static final Logger LOGGER = Logger.getGlobal();
	
	@Override
	public void entryaction(MyEntryEvent e) {
		if ((Integer) e.getSource() != 1)
			LOGGER.info("ResultListenerSuffixTreePath entryaction node:"
					+ e.getSource()
					/*+ "  "
					+ ResultSuffixTreeNodeStack.suffixTree
							.edgeString((Integer) e.getSource())*/);
		ResultSuffixTreeNodeStack.stack.push((Integer) e.getSource());
	}
	
	private int pathLength() {
		int nodeNr,len=0;
		Node node;
		for (int i=0;i<ResultSuffixTreeNodeStack.stack.size();i++){
			nodeNr=ResultSuffixTreeNodeStack.stack.get(i);
			node=ResultSuffixTreeNodeStack.suffixTree.nodes[nodeNr];
			len=len+node.nodeInfo.getEnd()-node.nodeInfo.getStart();
		}
		return len;
	}
	
	@Override
	public void exitaction(MyExitEvent e) {
		int nrOfNode = ResultSuffixTreeNodeStack.stack.peek();	
		LOGGER.info("ResultListenerSuffixTreePath exitaction entry node:"
				+ e.getSource());
		Node node=ResultSuffixTreeNodeStack.suffixTree.nodes[nrOfNode];
		// terminal, save suffixes on path to root
		/*if ((node instanceof GeneralisedSuffixTreeNode) && 
				(node.children.isEmpty()))
			
		{
			LOGGER.info("ResultListenerSuffixTreePath.exitaction children null:"
					+ e.getSource());
			ArrayList<TextStartPosInfo> startPositionOfSuffix=
			((GeneralisedSuffixTreeNode)node).getStartPositionOfSuffix();
			
			int pathLength= pathLength();
			
			// toDo for all tokens in terminal
			for (TextStartPosInfo startPos : startPositionOfSuffix)
			{
				System.out.println("ResultListenerSuffixTreePath.exitaction node "
						+ nrOfNode+ " startPos "+
						startPos.startPositionOfSuffix+						
						" pathLength "+pathLength);
				ResultSuffixTreeNodeStack.addStackForSuffixesToList
				(startPos.startPositionOfSuffix+pathLength-1,
				ResultSuffixTreeNodeStack.GetSuffixDescriptionArrayList());
			}
			
		}
		ResultSuffixTreeNodeStack.stack.pop();
		*/
	}

}
