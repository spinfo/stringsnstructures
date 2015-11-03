package modules.suffixTree.suffixTree.node.nodeFactory;

import modules.suffixTree.suffixTree.SuffixTree;
import modules.suffixTree.suffixTree.node.Node;
import modules.suffixTree.suffixTree.node.info.NodeInfo;

public abstract class NodeFactory {
	public abstract Node generateNode(NodeInfo nodeInfo, SuffixTree suffixTree);
}