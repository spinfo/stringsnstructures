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
	// positionList is an ArrayList of PositionInfo which contains three elements for
	// nonterminal nodes (first:start, second:end, third:textNr, triples) 
	// and n * three elements(triple) for terminal nodes, 
	// i.e. n = number of texts ending in the terminal;
	// Special case for end of leaves: end value element is equal for all leaves for a given text;
	// value of this element is oo, i.e. maximal value; at the end, when '$' is reached, value is 
	// replaced by actual position value
	private ArrayList<PositionInfo> positionList;
	TreeMap<Character, Integer> next = new TreeMap<Character, Integer>();

	// cstr
	public Node(int start, int end, int nr) {
		
		this.positionList=new ArrayList<PositionInfo>(4);
		this.addPos(start, end, nr);
					
	}// Node
	
	public boolean isTerminal() {
		return ((this.next==null) || (this.next.size()==0));
	}
	
	void addPos(int start,int end, int textNr){				
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
	}
	
	// getter methods for start, end and textNr
	// triple ordering of elements is hidden from client
	int getStart(int pos /* first or last*/) {
		return this.positionList.get(getPositionListIndex(pos)).val;
	}
	
	int getEnd(int pos /* first or last*/) {
		return this.positionList.get(getPositionListIndex(pos)+1).val;
	}
	
	int getTextNr(int pos /* first or last*/) {
		return this.positionList.get(getPositionListIndex(pos)+2).val;
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
	
	// Returns the actual number of positions noted for this node
	// regardless of their triple ordering
	public int getPositionsAmount() {
		if (this.positionList.size() % 3 != 0) {
			throw new IllegalStateException("Wrong number of elements for position list. Must always contain a multiple of 3 elements.");
		}
		return (int) (this.positionList.size() / 3);
	}

	public int edgeLength() {
		return Math.min(this.getEnd(0),SuffixTree.position + 1) - this.getStart(0);
	}
	
	// get the actual index in positionList by multiplying with the
	// number of elements, that are noted for each position
	private int getPositionListIndex(int pos) {
		return pos * 3;
	}
	
	// return the node index of the node reached by following the edge
	// that begins with edgeBegin, return null if no such node exists
	public Integer getNext(char edgeBegin) {
		return this.next.get(edgeBegin);
	}
}// Node
