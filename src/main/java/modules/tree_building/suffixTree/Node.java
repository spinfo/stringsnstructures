package modules.tree_building.suffixTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class Node {

	/*
	 * There is no need to create an "Edge" class. Information about the
	 * edge is stored right in the node. [start; end) interval specifies
	 * the edge, by which the node is connected to its parent node.
	 */

	int link;

	// List that contains either the position of this node in one single text (inner node) or all
	// positions of this node (terminal node)
	private List<NodePosition> positions;
	
	// the edges to the next nodes (represented by a node nr in the tree)
	TreeMap<Character, Integer> next = new TreeMap<Character, Integer>();
	
	// A data field that may be used by clients to link a node to all it's leaf nodes.
	// This field is never used in the construction of the suffix tree and can
	// be ignored for the simple purpose of building and using a suffix tree in a normal way.
	private Set<Node> leaves = new HashSet<Node>();
	
	// A data field that may be used to set the length of the path up to this node.
	// This field is never used in the construction of the suffix tree and can
	// be ignored for the simple purpose of building and using a suffix tree in a normal way.
	private int pathLength = -1;

	// cstr
	public Node(int start, int end, int nr, int typeContextNr, BaseSuffixTree tree) {
		this.positions=new ArrayList<NodePosition>(1);
		this.addPos(start, end, nr, typeContextNr, tree);
	}// Node
	
	public boolean isTerminal() {
		return ((this.next==null) || (this.next.size()==0));
	}
	
	// How positions are kept track of is hidden from the client. This methods adds new
	// NodePosition elements and decides what End the current position should be set to
	void addPos(int start,int end, int textNr, int typeContext, BaseSuffixTree tree){
		// make sure that the position added is never equal to the last position set
		if (this.getPositionsAmount() > 0){
			int lastPos = this.getPositionsAmount() - 1;
			if ((this.getStart(lastPos) == start) && (this.getEnd(lastPos) == end) 
					&& (this.getTextNr(lastPos) == textNr)) {
				throw new IllegalStateException("addPos equal entry start: " + start + " end: " + end + " textNr: " + textNr);
			}
		}
		// decied which end value is to be used
		final NodePositionEnd endPosition;
		if (end==BaseSuffixTree.oo) {
			endPosition = tree.getEnd();
		} else {
			endPosition = new NodePositionEnd(end);
		}
		// actually add the position
		this.positions.add(new NodePosition(start, endPosition, textNr, typeContext));
	}
	
	// getter methods for start, end and textNr
	public int getStart(int pos) {
		return this.positions.get(pos).getStart();
	}
	
	public int getEnd(int pos) {
		return this.positions.get(pos).getEnd();
	}
	
	public int getTextNr(int pos) {
		return this.positions.get(pos).getTextNr();
	}
	
	public int getTypeContext(int pos) {
		return this.positions.get(pos).getTypeContextNr();
	}
	
	// setter methods for start, end and textNr
	void setStart(int pos, int val){
		this.positions.get(pos).setStart(val);
	}
	
	void setEnd(int pos, int val){
		this.positions.get(pos).setEnd(new NodePositionEnd(val));
	}
	
	void setTextNr(int pos, int val) {
		this.positions.get(pos).setTextNr(val);
	}
	
	void setTypeContextNr(int pos, int val) {
		this.positions.get(pos).setTypeContextNr(val);
	}
	
	// if a node is split and if it represents more than one text, all start positions in
	// the position list of a node must be updated by active_length
	void updateStartPositions(int active_length) {
		for (int i = 0; i < this.getPositionsAmount(); i++) {
			this.setStart(i, this.getStart(i) + active_length);
		}
	}

	// Returns the number of positions noted for this node
	public int getPositionsAmount() {
		return this.positions.size();
	}

	// return the edge length of the node in the tree
	public int edgeLength(BaseSuffixTree tree) {
		return Math.min(this.getEnd(0),tree.position + 1) - this.getStart(0);
	}
	
	// return the beginnings of edges starting at this node
	public Set<Character> getEdgeBegins() {
		return this.next.keySet();
	}
	
	// return the node index of the node reached by following the edge
	// that begins with edgeBegin, return null if no such node exists
	public Integer getNext(char edgeBegin) {
		return this.next.get(edgeBegin);
	}
	
	/**
	 * Publicly the list of positions of a node is exposed read-only.
	 * 
	 * @return An unmodifiable view on the positions of this node.
	 */
	public List<NodePosition> getPositions() {
		return Collections.unmodifiableList(this.positions);
	}
	
	/**
	 * @return The leaves set for this node or an empty set if none were set.
	 */
	public Set<Node> getLeaves() {
		return this.leaves;
	}
	
	/**
	 * Set the path length up to this node to the specified value.
	 * @param length path length
	 */
	public void setPathLength(int length) {
		this.pathLength = length;
	}

	/**
	 * @return The pathLength set for this node or -1 if none was set.
	 */
	public int getPathLength() {
		return pathLength;
	}
}// Node
