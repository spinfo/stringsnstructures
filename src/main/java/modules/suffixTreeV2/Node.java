package modules.suffixTreeV2;

import java.util.ArrayList;
import java.util.TreeMap;


public class Node {

	/*
	 * There is no need to create an "Edge" class. Information about the
	 * edge is stored right in the node. [start; end) interval specifies
	 * the edge, by which the node is connected to its parent node.
	 */

	int link;
	// positionList is an ArrayList of PositionInfo which contains four elements for
	// nonterminal nodes (first:start, second:end, third:textNr, fourth: typeContext) 
	// and n * four elements (quadruple) for terminal nodes, 
	// i.e. n = number of texts ending in the terminal;
	// Special case for end of leaves: end value element is equal for all leaves for a given text;
	// value of this element is oo, i.e. maximal value; at the end, when '$' is reached, value is 
	// replaced by actual position value
	private ArrayList<PositionInfo> positionList;
	TreeMap<Character, Integer> next = new TreeMap<Character, Integer>();

	// cstr
	public Node(int start, int end, int nr, int typeContextNr) {
		this.positionList=new ArrayList<PositionInfo>(4);
		this.addPos(start, end, nr, typeContextNr);
	}// Node
	
	public boolean isTerminal() {
		return ((this.next==null) || (this.next.size()==0));
	}
	
	// This should be the only place where new positionInfo objects are added to
	// the positionInfo list. This ensures, that all four fields are actually filled
	// and can later be retrieved by indexing to the n*4th position.
	void addPos(int start,int end, int textNr, int typeContext){
		// make sure that the position added is never equal to the last position set
		if (this.getPositionsAmount() > 0){
			int lastPos = this.getPositionsAmount() - 1;
			if ((this.getStart(lastPos) == start) && (this.getEnd(lastPos) == end) 
					&& (this.getTextNr(lastPos) == textNr)) {
				throw new IllegalStateException("addPos equal entry start: " + start + " end: " + end + " textNr: " + textNr);
			}
		}
		// start
		this.positionList.add(new PositionInfo(start));
		// end
		if (end==SuffixTree.oo) {
			this.positionList.add(GST.OO);
		} else {
			this.positionList.add(new PositionInfo(end));
		}
		// textNr
		this.positionList.add(new PositionInfo(textNr));
		// typeContextNr
		this.positionList.add(new PositionInfo(typeContext));
	}
	
	// getter methods for start, end and textNr
	// quadruple ordering of elements is hidden from client
	public int getStart(int pos) {
		return this.positionList.get(getPositionListIndex(pos)).val;
	}
	
	public int getEnd(int pos) {
		return this.positionList.get(getPositionListIndex(pos)+1).val;
	}
	
	public int getTextNr(int pos) {
		return this.positionList.get(getPositionListIndex(pos)+2).val;
	}
	
	public int getTypeContext(int pos) {
		return this.positionList.get(getPositionListIndex(pos)+3).val;
	}
	
	// setter methods for start, end and textNr
	void setStart(int pos, int val){
		this.positionList.get(getPositionListIndex(pos)).val=val;
	}
	
	void setEnd(int pos, int val){
		this.positionList.get(getPositionListIndex(pos)+1).val=val;
	}
	
	void setTextNr(int pos, int val) {
		this.positionList.get(getPositionListIndex(pos)+2).val=val;
	}
	
	void setTypeContextNr(int pos, int val) {
		this.positionList.get(getPositionListIndex(pos)+3).val=val;
	}
	
	// if a node is split and if it represents more than one text, all start positions in
	// the position list of a node must be updated by active_length
	void updateStartPositions(int active_length) {
		for (int i = 0; i < this.getPositionsAmount(); i++) {
			this.setStart(i, this.getStart(i) + active_length);
		}
	}

	// Returns the actual number of positions noted for this node
	// regardless of their triple ordering
	public int getPositionsAmount() {
		if (this.positionList.size() % 4 != 0) {
			throw new IllegalStateException("Wrong number of elements for position list. Must always contain a multiple of 4 elements.");
		}
		return (int) (this.positionList.size() / 4);
	}

	public int edgeLength() {
		return Math.min(this.getEnd(0),SuffixTree.position + 1) - this.getStart(0);
	}
	
	// get the actual index in positionList by multiplying with the
	// number of elements, that are noted for each position
	private int getPositionListIndex(final int pos) {
		return pos * 4;
	}
	
	// return the node index of the node reached by following the edge
	// that begins with edgeBegin, return null if no such node exists
	public Integer getNext(char edgeBegin) {
		return this.next.get(edgeBegin);
	}
}// Node
