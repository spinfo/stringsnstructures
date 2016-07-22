package modules.tree_properties.branchLenghtGroups;

// Workbench internal imports.
import models.Dot2TreeInnerNode;
/**
 * This helper class extends Dot2TreeInnerNode.java and adds 
 * the functionality that each internal node keeps track of 
 * its direct parent (another internal node).
 * 
 * @author christopher
 *
 */

public class Dot2TreeInnerNodesParent extends Dot2TreeInnerNode {
	
	// Variables:
	private int parentNodeNumber;
	
	// Constructors:
	
	public Dot2TreeInnerNodesParent (int number, int frequency, String label) {
		super(number, frequency, label);
	}
	
	public Dot2TreeInnerNodesParent (int number, int frequency, String label, int pNumber) {
		super(number, frequency, label);
		this.parentNodeNumber = pNumber;
	}
	
	// End constructors.
	
	// Setters:
	
	/**
	 * This method sets the number for the parental internal node.
	 * @param pNumber
	 */
	public void setParent (int pNumber) {
		this.parentNodeNumber = pNumber;
	}
	
	// End setters.
	
	// Getters:
	
	/**
	 * This method returns the number of the parental (internal) node.
	 * @return int parentNodeNumber
	 */
	public int getParent ()  {
		return this.parentNodeNumber;
	}
	
	// End getters.

}
