package suffixTree.suffixTree.node;

import java.io.PrintWriter;
import java.util.ArrayList;

import suffixTree.suffixTree.node.info.NodeInfo;
import suffixTree.suffixTree.node.textStartPosInfo.TextStartPosInfo;
//import suffixTree.SuffixTree;

public class GeneralisedSuffixTreeNode extends Node {

	private ArrayList<TextStartPosInfo> startPositionOfSuffix;

	public GeneralisedSuffixTreeNode(NodeInfo nodeInfo) {
		super(nodeInfo);
		startPositionOfSuffix = new ArrayList<TextStartPosInfo>();

	}

	// is called in xxxNodeFactory
	public void addStartPositionOfSuffix(Object startPosition) {
		this.startPositionOfSuffix.add((TextStartPosInfo) startPosition);
	}

	public ArrayList<TextStartPosInfo> getStartPositionOfSuffix() {
		return this.startPositionOfSuffix;
	}

	public void printStartPositionOfSuffix(PrintWriter out) {
		for (TextStartPosInfo startPos : this.startPositionOfSuffix) {
			out.print(startPos.text + " " + startPos.startPositionOfSuffix
					+ "\n");
		}
	}
}