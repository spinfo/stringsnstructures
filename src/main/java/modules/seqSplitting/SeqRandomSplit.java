package modules.seqSplitting;


public class SeqRandomSplit {
	//variables which hold sequence fragment information
	private int firstPos;
	private int lastPos;
	private double seqDensity;
	private String subSequence; 
	
	//constructors:
	public SeqRandomSplit(String Seq, int first, int second, int totalLength) {
		firstPos = first; 
		lastPos = second;
		subSequence = Seq;
		seqDensity =  subSequence.length()/totalLength;
	}
	
	//end constructors
	
	//methods:
	
	//setters:
	//end setters
	
	//getters:
	public String getSubsequence() {
		return subSequence;
	}
	
	public double getSeqDensity() {
		return seqDensity;
	}
	
	public int getFirstPos() {
		return firstPos;
	}
	
	public int getLastPos() {
		return lastPos;
	}
	
	public int getSubSeqLength() {
		return subSequence.length();
	}
	//end getters
	
		
	//end methods
}