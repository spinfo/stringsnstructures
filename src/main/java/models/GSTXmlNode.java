package models;

import java.util.ArrayList;

/**
 * This a model which holds all fields and attributes for each
 * generalized suffix tree (GST) node provided by an XML output.
 * 
 * @author christopher
 *
 */

public class GSTXmlNode {
	
	// Variables:
	
	// Variables are analogous to each of the node associated fields in the XML output of the GST.
	private int nodeNumber;
	
	private String nodeLabel;
	
	/* nodeTypes holds an ArrayList of arrays.
	 * 1. entry: typeNr
	 * 2. entry: pattern
	 * 3. entry: startpos
	 * For further information refer to an XML output of the GST module.	
	 */
	private ArrayList <int[]> nodeTypes;
	
	private int nodeFrequency;
	
	// End variables.
	
	// Constructors:
	public GSTXmlNode (int number) {
		this.nodeNumber = number;
		this.nodeTypes = new ArrayList <int[]> ();
	}
	
	public GSTXmlNode (int number, String label) {
		this.nodeNumber = number;
		this.nodeLabel = label;
		this.nodeTypes = new ArrayList <int[]> ();
	}
	
	public GSTXmlNode (int number, String label, int typeNr, int pattern, int startPos, int nodeFrequency) {
		this.nodeNumber = number;
		this.nodeLabel = label;
		this.nodeTypes = new ArrayList <int[]> ();
		this.nodeTypes.add(new int[3]);
		this.nodeTypes.get(0)[0] = typeNr;
		this.nodeTypes.get(0)[1] = pattern;
		this.nodeTypes.get(0)[2] = startPos;
		this.nodeFrequency = 0;
	}
	// End constructors.
	
	// Methods:
	
	// Setters:
	
	public void setNodeLabel (String label) {
		this.nodeLabel = label;
	}
	
	public void setNodeFrequency (int freq) {
		this.nodeFrequency = freq;
	}
	
	public void setNodeTypes (int typeNr, int pattern, int startPos) {

		// Save additional type information in a new array.
		this.nodeTypes.add(new int[3]);
		
		// Remember the index of this new array in the ArrayList.
		int lastIndex = this.nodeTypes.size() - 1;
		
		// Add the information for this new element of the ArrayList.
		this.nodeTypes.get(lastIndex)[0] = typeNr;
		this.nodeTypes.get(lastIndex)[1] = pattern;
		this.nodeTypes.get(lastIndex)[2] = startPos;
	}
	
	// End setters.
	
	// Getters:
	
	public int getNodeNumber () {
		return this.nodeNumber;
	}
	
	public String getNodeLabel () {
		return this.nodeLabel;
	}
	
	public int getNodeFrequency () {
		return this.nodeFrequency;
	}
	
	public ArrayList<int[]> getNodeTypes () {
		return this.nodeTypes;
	}
	
	// End getters.
	
	// End methods.

}
