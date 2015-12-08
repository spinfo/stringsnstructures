package modules.seqTreeProperties;


public class SeqCopheneticIndex {
	//variables:
	private String sequence;
	private int innerNodeNumber;
	private int binomialCoeff;
	
	//end variables
	
	
	//constructors:
		
	public SeqCopheneticIndex (String seq, int num) {
		sequence = seq;
		innerNodeNumber = num;
		binomialCoeff = (num*(num-1))/2;
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
	
	public int getBinomialCoeff () {
		return binomialCoeff;
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
