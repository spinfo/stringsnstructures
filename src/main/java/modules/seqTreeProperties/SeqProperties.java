package modules.seqTreeProperties;

public class SeqProperties {
	//variables:
	private String nodeName; 
	private String sequence;
	private int leafNum;
	private int pathLength;
	
	//end variables
	
	//constructors:
		
	public SeqProperties (String name, String seq, int len) {
		nodeName = name;
		sequence = seq;
		leafNum = 1;
		pathLength = len;
	}
	
	
	public SeqProperties (String name, String seq, int len, int leaves) {
		nodeName = name;
		sequence = seq;
		pathLength = len;
		leafNum = leaves;
	}
	//end constructors
	
	//methods:
	
	//setters:
	
	public void setSequence (String seq) {
		sequence = seq;
	}
	
	public void setLeafNum (int leaves) {
		leafNum = leaves;
	}
	
	public void setPathLength (int len) {
		pathLength = len;
	}
	
	public void setNodeName (String name) {
		nodeName = name;
	}
	
	//end setters
	
	//getters:
	
	public String getSequence () {
		return sequence;
	}
	
	public int getLeafNum () {
		return leafNum;
	}
	
	public int getPathLength () {
		return pathLength;
	}
	
	public String getNodeName () {
		return nodeName;
	}
	//end getters
	
	//concatenate new sequence to old one
	public void catSequence (String seq) {
		String newSeq = seq + sequence;
		sequence = newSeq;
	}
	
	//end methods
}
