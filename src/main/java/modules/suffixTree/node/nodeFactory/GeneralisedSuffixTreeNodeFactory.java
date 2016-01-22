package modules.suffixTree.node.nodeFactory;

import modules.suffixTree.SuffixTree;
import modules.suffixTree.node.GeneralisedSuffixTreeNode;
import modules.suffixTree.node.NodeInfo;
import modules.suffixTree.node.TextStartPosInfo;

public class GeneralisedSuffixTreeNodeFactory extends NodeFactory {

	public GeneralisedSuffixTreeNode generateNode(NodeInfo nodeInfo, SuffixTree suffixTree) {
		GeneralisedSuffixTreeNode node = new GeneralisedSuffixTreeNode(nodeInfo);
		// leaf
		if (nodeInfo.getEnd() == suffixTree.oo.getEnd()) {
			suffixTree.leafCount++;
			node.addStartPositionInformation(new TextStartPosInfo(suffixTree.unit,
					suffixTree.textNr, suffixTree.leafCount));
		}
		return node;
	}
}