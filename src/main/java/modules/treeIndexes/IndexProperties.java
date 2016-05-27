package modules.treeIndexes;

public class IndexProperties {
	//variables:
	private int nodeNumber; 
	private String contEdgeLabel;
	private int leafNum;
	private int treeDepth;
	
	//end variables
	
	//constructors:
		
	public IndexProperties (int num, String edgeLabel, int depth) {
		nodeNumber = num;
		contEdgeLabel = edgeLabel;
		leafNum = 1;
		treeDepth = depth;
	}
	
	
	public IndexProperties (int num, String edgeLabel, int depth, int leaves) {
		nodeNumber = num;
		contEdgeLabel = edgeLabel;
		treeDepth = depth;
		leafNum = leaves;
	}
	//end constructors
	
	//methods:
	
	//setters:
	
	public void setEdgeLabel (String edgeLabel) {
		this.contEdgeLabel = edgeLabel;
	}
	
	public void setLeafNum (int leaves) {
		this.leafNum = leaves;
	}
	
	public void incrementLeaves () {
		this.leafNum ++;
	}
	
	public void increaseLeaves (int leaves) {
		this.leafNum += leaves;
	}
	
	public void setPathLength (int len) {
		this.treeDepth = len;
	}
	
	public void setNodeNumber (int num) {
		this.nodeNumber = num;
	}
	
	//end setters
	
	//getters:
	
	public String getEdgeLabel () {
		return this.contEdgeLabel;
	}
	
	public int getLeafNum () {
		return this.leafNum;
	}
	
	public int getTreeDepth () {
		return this.treeDepth;
	}
	
	public int getNodeNumber () {
		return this.nodeNumber;
	}
	//end getters
	
	//concatenate new sequence to old one
	public void catEdgeLabel (String label) {
		String newSeq = label + this.contEdgeLabel;
		this.contEdgeLabel = newSeq;
	}
	
	//end methods
}
