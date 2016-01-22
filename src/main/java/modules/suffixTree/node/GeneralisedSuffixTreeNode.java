package modules.suffixTree.node;

import java.io.PrintWriter;
import java.util.ArrayList;

public class GeneralisedSuffixTreeNode extends Node {

	private ArrayList<TextStartPosInfo> textStartPositionInfos;

	public GeneralisedSuffixTreeNode(NodeInfo nodeInfo) {
		super(nodeInfo);
		textStartPositionInfos = new ArrayList<TextStartPosInfo>();

	}

	// is called in the NodeFactory for GST nodes
	public void addStartPositionInformation(Object startPosition) {
		this.textStartPositionInfos.add((TextStartPosInfo) startPosition);
	}

	public ArrayList<TextStartPosInfo> getStartPositionInformation() {
		return this.textStartPositionInfos;
	}

	public void printStartPositionInformation(PrintWriter out) {
		for (TextStartPosInfo startPos : this.textStartPositionInfos) {
			out.print(startPos.text + " " + startPos.startPositionOfSuffix
					+ "\n");
		}
	}
}