package modules.suffixTree.node.nodeFactory;

import modules.suffixTree.SuffixTree;
import modules.suffixTree.node.Node;
import modules.suffixTree.node.NodeInfo;

public abstract class NodeFactory {
	public abstract Node generateNode(NodeInfo nodeInfo, SuffixTree suffixTree);
}