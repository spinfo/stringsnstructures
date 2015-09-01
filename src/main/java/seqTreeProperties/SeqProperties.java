package seqTreeProperties;

public class SeqProperties {
	//variables:
	private String nodeName; 
	private String sequence;
	private double pathRatio;
	private int pathLength;
	
	//end variables
	
	//constructors:
		
	public SeqProperties (String name, String seq, int len) {
		nodeName = name;
		sequence = seq;
		pathRatio = 1;
		pathLength = len;
	}
	
	public SeqProperties (String name, String seq, int len, double ratio) {
		nodeName = name;
		sequence = seq;
		pathLength = len;
		pathRatio = ratio;
	}
	//end constructors
	
	//methods:
	
	//setters:
	
	public void setSequence (String seq) {
		sequence = seq;
	}
	
	public void setPathRatio (double ratio) {
		pathRatio = ratio;
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
	
	public double getPathRatio () {
		return pathRatio;
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
