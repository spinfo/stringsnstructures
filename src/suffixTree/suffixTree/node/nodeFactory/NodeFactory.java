package suffixTree.suffixTree.node.nodeFactory;

import suffixTree.suffixTree.node.Node;
import suffixTree.suffixTree.node.info.NodeInfo;

public abstract class NodeFactory {
	public abstract Node generateNode(NodeInfo nodeInfo);
}