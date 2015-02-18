package suffixTree.suffixTree.node;

import java.io.PrintWriter;

import suffixTree.suffixTree.node.info.NodeInfo;

public class SimpleSuffixTreeNode extends Node {

	private int startPositionOfSuffix;

	public SimpleSuffixTreeNode(NodeInfo nodeInfo) {
		super(nodeInfo);
		// if (nodeInfo.getEnd()==SuffixTree.oo.getEnd())
		// {leafCount++;this.addStartPositionOfSuffix(leafCount);}
	}

	// is called in xxxNodeFactory
	public void addStartPositionOfSuffix(Object startPositionOfSuffix) {
		this.startPositionOfSuffix = (int) startPositionOfSuffix;
	}

	public Object getStartPositionOfSuffix() {
		return this.startPositionOfSuffix;
	}

	public void printStartPositionOfSuffix(PrintWriter out) {
		out.print(this.getStartPositionOfSuffix());
	}
}