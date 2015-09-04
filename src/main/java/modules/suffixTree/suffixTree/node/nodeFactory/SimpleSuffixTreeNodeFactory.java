package modules.suffixTree.suffixTree.node.nodeFactory;

import modules.suffixTree.suffixTree.SuffixTree;
import modules.suffixTree.suffixTree.node.SimpleSuffixTreeNode;
import modules.suffixTree.suffixTree.node.info.NodeInfo;

public class SimpleSuffixTreeNodeFactory extends NodeFactory {

	public SimpleSuffixTreeNode generateNode(NodeInfo nodeInfo) {
		//
		SimpleSuffixTreeNode node = new SimpleSuffixTreeNode(nodeInfo);
		// leaf
		if (nodeInfo.getEnd() == SuffixTree.oo.getEnd()) {
			SuffixTree.leafCount++;
			node.addStartPositionOfSuffix(SuffixTree.leafCount);
		}
		;
		return node;
	}
}