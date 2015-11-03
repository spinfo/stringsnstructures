package modules.suffixTree.suffixTree.node.nodeFactory;

import modules.suffixTree.suffixTree.SuffixTree;
import modules.suffixTree.suffixTree.node.GeneralisedSuffixTreeNode;
import modules.suffixTree.suffixTree.node.info.NodeInfo;
import modules.suffixTree.suffixTree.node.textStartPosInfo.TextStartPosInfo;

public class GeneralisedSuffixTreeNodeFactory extends NodeFactory {

	public GeneralisedSuffixTreeNode generateNode(NodeInfo nodeInfo, SuffixTree suffixTree) {
		GeneralisedSuffixTreeNode node = new GeneralisedSuffixTreeNode(nodeInfo);
		// leaf
		if (nodeInfo.getEnd() == suffixTree.oo.getEnd()) {
			suffixTree.leafCount++;
			node.addStartPositionOfSuffix(new TextStartPosInfo(suffixTree.unit,
					suffixTree.textNr, suffixTree.leafCount));
		}
		return node;
	}
}