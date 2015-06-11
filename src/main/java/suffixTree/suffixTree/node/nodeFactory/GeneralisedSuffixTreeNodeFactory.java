package suffixTree.suffixTree.node.nodeFactory;

import suffixTree.suffixTree.SuffixTree;
import suffixTree.suffixTree.node.GeneralisedSuffixTreeNode;
import suffixTree.suffixTree.node.info.NodeInfo;
import suffixTree.suffixTree.node.textStartPosInfo.TextStartPosInfo;

public class GeneralisedSuffixTreeNodeFactory extends NodeFactory {

	public GeneralisedSuffixTreeNode generateNode(NodeInfo nodeInfo) {
		GeneralisedSuffixTreeNode node = new GeneralisedSuffixTreeNode(nodeInfo);
		// leaf
		if (nodeInfo.getEnd() == SuffixTree.oo.getEnd()) {
			SuffixTree.leafCount++;
			node.addStartPositionOfSuffix(new TextStartPosInfo(SuffixTree.unit,
					SuffixTree.textNr, SuffixTree.leafCount));
		}
		return node;
	}
}