package modules.tree_properties.motifDetection;

import java.util.TreeMap;

public class CompareSets {
	// Variables:
	
	// HashMap holding the nodeNumber and the string.
	private TreeMap <Integer, String> nodeString;
	
	// HashMap holding the number of occurrences for each string.
	private String thisString;
	private int occurString;
	
	// End variables.
	
	// Constructors:
	public CompareSets (String newString, int nodeNumber, int occurrences) {
		
		this.nodeString = new TreeMap <Integer, String> ();
		this.thisString = newString;
		this.occurString = occurrences;
		
		this.nodeString.put(nodeNumber, newString);
		
	}
	
	// End constructors.
	
	// Methods:
	
	// Setters:
	
	/**
	 * This setter adds a new Node Number and String pair.
	 * @param int nodeNumber
	 * @param String str
	 */
	public void setNodeString (int nodeNumber, String str) {
		this.nodeString.put(nodeNumber, str);
	}
	
	/**
	 * This getter returns the String thisString.
	 * @return String thisString
	 */
	public String setThisString () {
		return this.thisString;
	}
		
	// End setters.
	
	// Getters:
	
	/**
	 * This getter returns a specific number of occurrences for String str.
	 * @return int numberOfOccurences
	 */
	public int getOccurences () {
		return this.occurString;
	}
	
	/**
	 * This getter returns all entries for the nodes and strings in form of a TreeMap.
	 * @return TreeMap <Integer, String> this.nodeString
	 */
	public TreeMap <Integer, String> getAllNodeStrings () {
		return this.nodeString;
	}
	// End getters.
	
	/**
	 * This method increments the occurrences for a String str.
	 * @param String str
	 */
	public void increOccur () {
		this.occurString++;
	}
	
	// End methods.
}
