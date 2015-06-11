package suffixTree.suffixTree.node;

import java.io.PrintWriter;
import java.util.TreeMap;

import suffixTree.suffixTree.node.info.NodeInfo;

public abstract class Node {

	/*
	 * There is no need to create an "Edge" class. Information about the edge is
	 * stored right in the node. [start; end) interval specifies the edge, by
	 * which the node is connected to its parent node.
	 */
	public static int leafCount = -1;

	public int link;

	public NodeInfo nodeInfo;
	//
	public TreeMap<Character, Integer> children = new TreeMap<Character, Integer>();

	public Node(NodeInfo nodeInfo) {
		this.nodeInfo = nodeInfo;
	}

	public abstract void addStartPositionOfSuffix(Object startPositionOfSuffix);

	public abstract Object getStartPositionOfSuffix();

	public abstract void printStartPositionOfSuffix(PrintWriter out);

	public int edgeLength(int position) {
		return Math.min(this.nodeInfo.getEnd(), position + 1)
				- nodeInfo.getStart();
	}

	public int getStart() {
		return this.nodeInfo.getStart();
	}

	public void setStart(int start) {
		this.nodeInfo.setStart(start);
	}

	public int getEnd(int position) {
		return Math.min(this.nodeInfo.getEnd(), position);
	}

	public NodeInfo getNodeInfo() {
		return this.nodeInfo;
	}
}