package modules.suffixTree.node.nodeFactory;

import modules.suffixTree.SuffixTree;
import modules.suffixTree.node.NodeInfo;
import modules.suffixTree.node.SimpleSuffixTreeNode;

public class SimpleSuffixTreeNodeFactory extends NodeFactory {

	public SimpleSuffixTreeNode generateNode(NodeInfo nodeInfo, SuffixTree suffixTree) {
		//
		SimpleSuffixTreeNode node = new SimpleSuffixTreeNode(nodeInfo);
		// leaf
		if (nodeInfo.getEnd() == suffixTree.oo.getEnd()) {
			suffixTree.leafCount++;
			node.addStartPositionInformation(suffixTree.leafCount);
		}
		;
		return node;
	}
}