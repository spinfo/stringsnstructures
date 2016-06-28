package modules.tree_properties.treeIndexes;

public class IndexSackin {
	//variables:
	private String sequence;
	private int innerNodeNumber;
	
	//end variables
	
	
	//constructors:
		
	public IndexSackin (String seq, int num) {
		sequence = seq;
		innerNodeNumber = num;
	}
	
	//end constructors
	
	//methods:
	
	//setters:
	
	public void setSequence (String seq) {
		sequence = seq;
	}
	
	public void setNodeNumber (int num) {
		innerNodeNumber = num;
	}
		
	//end setters
	
	//getters:
	
	public String getSequence () {
		return sequence;
	}
	
	public int getNodeNumber () {
		return innerNodeNumber;
	}
	//end getters
	
	//concatenate new sequence to old one
	public void catSequence (String seq) {
		String newSeq = seq + sequence;
		sequence = newSeq;
	}
	
	public void appendSequence (String seq) {
		sequence += seq;
	}
	
	public void incrementNode () {
		innerNodeNumber ++;
	}
	
	//end methods
}
