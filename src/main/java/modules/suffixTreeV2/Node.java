package modules.suffixTreeV2;

import java.util.ArrayList;
import java.util.TreeMap;


class Node {

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
	ArrayList<PositionInfo> positionList;
	public TreeMap<Character, Integer> next = new TreeMap<Character, Integer>();

	// cstr
	public Node(int start, int end, int nr) {
		
		this.positionList=new ArrayList<PositionInfo>();
		this.addPos(start, end, nr);
					
	}// Node
	
	boolean isTerminal() {
		return ((this.next==null) || (this.next.size()==0));
	}
	
	void addPos(int start,int end, int textNr){				
		// start
		this.positionList.add(new PositionInfo(start));
		// end
		if (end==SuffixTree.oo)this.positionList.add(ST.OO);else this.positionList.add(new PositionInfo(end));
		// textNr
		this.positionList.add(new PositionInfo(textNr));
	}
	
	// getter methods for start, end and textNr, remind triple order of list elements
	int getStart(int posInList /* first or last*/) {
		return this.positionList.get(posInList).val;
	}
	
	int getEnd(int posInList /* first or last*/) {
		return this.positionList.get(posInList+1).val;
	}
	
	int getTextNr(int posInList /* first or last*/) {
		return this.positionList.get(posInList+2).val;
	}
	
	void setStart(int posInList, int val){
		this.positionList.get(posInList).val=val;
	}
	
	// setter methods for start, end and textNr
	void setEnd(int posInList, int val){
		this.positionList.get(posInList+1).val=val;
	}

	public int edgeLength() {
		return Math.min(this.getEnd(0),SuffixTree.position + 1) - this.getStart(0);
	}
}// Node
