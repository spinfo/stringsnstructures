package modules.suffixTree.node;

import java.io.PrintWriter;
import java.util.TreeMap;

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

	/**
	 * Add information about the start of the suffix that was read producing
	 * this (leaf) node.
	 * 
	 * @param startPositionOfSuffix
	 *            depending on the suffix tree used this can be a simple integer
	 *            or a TextStartPosInfo object
	 */
	public abstract void addStartPositionInformation(Object startPositionOfSuffix);

	/**
	 * Get information about the start of the suffix that was read producing
	 * this (leaf) node
	 * 
	 * @param startPositionOfSuffix
	 *            depending on the suffix tree used this can be a simple integer
	 *            or a List of TextStartPosInfo objects
	 */
	public abstract Object getStartPositionInformation();

	/**
	 * Print information about the start of the suffix that was read producing
	 * this (leaf) node
	 * 
	 * @param out
	 *            the PrintWriter to write to
	 */
	public abstract void printStartPositionInformation(PrintWriter out);

	public int edgeLength(int position) {
		return Math.min(this.nodeInfo.getEnd(), position + 1) - nodeInfo.getStart();
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