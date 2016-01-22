package modules.suffixTree.node;

import java.io.PrintWriter;

public class SimpleSuffixTreeNode extends Node {

	private int startPositionOfSuffix;

	public SimpleSuffixTreeNode(NodeInfo nodeInfo) {
		super(nodeInfo);
		// if (nodeInfo.getEnd()==SuffixTree.oo.getEnd())
		// {leafCount++;this.addStartPositionOfSuffix(leafCount);}
	}

	// is called in NodeFactory for Simple Suffix Tree
	@Override
	public void addStartPositionInformation(Object startPositionOfSuffix) {
		this.startPositionOfSuffix = (int) startPositionOfSuffix;
	}

	@Override
	public Object getStartPositionInformation() {
		return this.startPositionOfSuffix;
	}

	@Override
	public void printStartPositionInformation(PrintWriter out) {
		out.print(this.getStartPositionInformation());
	}
}