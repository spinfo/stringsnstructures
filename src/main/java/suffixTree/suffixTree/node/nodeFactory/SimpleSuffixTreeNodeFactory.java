package suffixTree.suffixTree.node.nodeFactory;

import suffixTree.suffixTree.SuffixTree;
import suffixTree.suffixTree.node.SimpleSuffixTreeNode;
import suffixTree.suffixTree.node.info.NodeInfo;

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