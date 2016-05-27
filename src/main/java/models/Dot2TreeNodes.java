package models;

/**
 * Helper class which stores information for each individual (leaf/inner) node.
 * Including linkage to other inner/leaf nodes and suffix link information.
 * 
 * @author Christopher Kraus
 *
 */


public class Dot2TreeNodes {

	// Variables:
	
	// The integer given to this particular node.
	private int nodeNumber; 
	
	// Number of leaves beneath this particular node (zero in case of leaf nodes).
	private int nodeFreq;
	
	// The tree depth of this node.
	private int nodeDepth; 
	
	// The string defined as label of this node.
	private String nodeLabel; 
	
	// String holding the edge label leading to this node.
	private String nodeEdgeLabel;
	
	
	
	// End variables
	
	
	// Constructors:
	public Dot2TreeNodes(int number) {
		
		this.nodeNumber = number;
				
	}
	

	public Dot2TreeNodes(int number, int frequency, String label) {
		
		this.nodeNumber = number;
		this.nodeFreq = frequency;
		this.nodeLabel = label;
		this.nodeEdgeLabel = "";
				
	}
	
	public Dot2TreeNodes(int number, int frequency, String label, String edgeLabel) {
		
		this.nodeNumber = number;
		this.nodeFreq = frequency;
		this.nodeLabel = label;
		this.nodeEdgeLabel = edgeLabel;
				
	}
	
	// End constructors
	
	// Methods:
	
	// Setters:
	
	public void setNodeNumber (int num) {
		this.nodeNumber = num;
	}
	
	public void setNodeFreq (int freq) {
		this.nodeFreq = freq;
	}
	
	public void setNodeLabel (String label) {
		this.nodeLabel = label;
	}
	
	public void setEdgeLabel (String edgeLabel) {
		this.nodeEdgeLabel = edgeLabel;
	}
	
	public void setNodeDepth (int depth) {
		this.nodeDepth = depth;
	}
	
	// End setters
	
	// Getters:
	
	public int getNodeNumber ()  {
		return this.nodeNumber;
	}
	
	public int getNodeFreq ()  {
		return this.nodeFreq;
	}
	
	public int getNodeDepth () {
		return this.nodeDepth;
	}
	
	public String getEdgeLabel () {
		return this.nodeEdgeLabel;
	}
	
	public String getNodeLabel () {
		return this.nodeLabel;
	}
	
	// End getters
	
	// End methods
}
