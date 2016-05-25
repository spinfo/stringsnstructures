package models;

import java.util.HashMap;

/**
 * Helper class which inherits from Dot2TreeNodes.
 * This helper class holds additional information important for leaf nodes.
 * 
 * @author christopher
 *
 */

public class Dot2TreeLeafNode extends Dot2TreeNodes {
	// Variables:
	
	// ArrayList holing the corresponding text number for this leaf.
	private HashMap <Integer, int[]> nodeLeafInfo; 
	
	// End variables:
	
	// Constructors:
	public Dot2TreeLeafNode (int number) {
		
		super(number);
		this.nodeLeafInfo = new HashMap <Integer, int[]> ();
		
	}
	
	public Dot2TreeLeafNode (int number, int frequency, String label) {
		
		super(number, frequency, label);
		this.nodeLeafInfo = new HashMap <Integer, int[]> ();
		
	}
	
	public Dot2TreeLeafNode (int number, int frequency, String label, String edgeLabel, int textNumber, int startPos, int endPos) {
		
		super(number, frequency, label, edgeLabel);
		this.nodeLeafInfo = new HashMap <Integer, int[]> ();
		this.nodeLeafInfo.put(textNumber, new int[2]);
		this.nodeLeafInfo.get(textNumber)[0] = startPos;
		this.nodeLeafInfo.get(textNumber)[1] = endPos;
		
	}
	
	// End constructors.
	
	// Methods:
	
	// Setters:
	
	public void setLeafInfo (int textNumber, int startPos, int endPos) {
		this.nodeLeafInfo.put(textNumber, new int[2]);
		this.nodeLeafInfo.get(textNumber)[0] = startPos;
		this.nodeLeafInfo.get(textNumber)[1] = endPos;
	}
	
	// End setters.
	
	// Getters:
	
	public int[] getLeafInfo (int textNumber) {
		int[] returnArray = new int[3];
		returnArray[0] = textNumber;
		returnArray[1] = this.nodeLeafInfo.get(textNumber)[0];
		returnArray[2] = this.nodeLeafInfo.get(textNumber)[1];
		return returnArray;
	}
	
	public HashMap <Integer, int[]> getAllLeafInfo () {
		return this.nodeLeafInfo;
	}
	
	// End getters.
	
	// End methods.

}
