package modules.tree_properties.motifDetection;

// Java util imports.
import java.util.TreeMap;

// Module specific imports.
import modules.tree_properties.branchLengthGroups.Dot2TreeInnerNodesParent;

public class MotifCandidates {
	
	// Variables:
	
	// This variable holds the information about the starting edge (alpha).
	private String alphaEdge;
	
	// This TreeMap holds the so-called alpha-set (all nodes which edge is alpha).
	private TreeMap <Integer, Dot2TreeInnerNodesParent> alphaSet;
	
	// This variable holds the information about the ending suffix (delta).
	private String deltaSuffix;
	
	// This TreeMap holds the so-called delta-set (all nodes which edge end on the suffix delta).
	private TreeMap <Integer, Dot2TreeInnerNodesParent> deltaSet;
	
	// This TreeMap holds the information about the different N-strings with their respective node-numbers.
	private TreeMap <Integer, String> nSetStrings;
	
	// End variables.
	
	// Constructors:
	public MotifCandidates (Dot2TreeInnerNodesParent node) {
		
		// Define the alphaEdge.
		alphaEdge = node.getEdgeLabel();
		
		// Instantiate all TreeMaps.
		this.alphaSet = new TreeMap <Integer, Dot2TreeInnerNodesParent> ();
		this.deltaSet = new TreeMap <Integer, Dot2TreeInnerNodesParent> ();
		this.nSetStrings = new TreeMap <Integer, String> ();
		
		// Initialize the alphaSet.
		this.alphaSet.put(node.getNodeNumber(), node);
		
	}
		
	// End constructors.
	
	// Methods:
	
	// Getters:
	
	/**
	 * This getter returns the edge label alpha for a so-called alpha set.
	 * @return String alphaEdge
	 */
	public String getAlphaEdge () {
		return this.alphaEdge;
	}
	
	/**
	 * This getter returns the alpha set.
	 * @return TreeMap <Integer, Dot2TreeInnerNodesParent> alphaSet
	 */
	public TreeMap <Integer, Dot2TreeInnerNodesParent> getAlphaSet() {
		return this.alphaSet;
	}
	
	/**
	 * This getter returns the delta set.
	 * @return TreeMap <Integer, Dot2TreeInnerNodesParent> deltaSet
	 */
	public TreeMap <Integer, Dot2TreeInnerNodesParent> getDeltaSet() {
		return this.deltaSet;
	}
	
	/**
	 * This getter returns the string of the suffix delta.
	 * @return String deltaSuffix
	 */
	public String getDelta ()  {
		return this.deltaSuffix;
	}
	
	/**
	 * This getter returns the N-set.
	 * @return TreeMap <Integer, String> nSetStrings
	 */
	public TreeMap <Integer, String> getNset () {
		return this.nSetStrings;
	}
	
	// End getters.
	
	// Setters:
	
	/**
	 * This setter sets the string of the suffix delta.
	 * @param String delta
	 */
	public void setDelta (String delta)  {
		this.deltaSuffix = delta;
	}
	
	/**
	 * This setter adds a node to the current alphaSet.
	 * @param int nodeNumber
	 * @param Dot2TreeInnerNodesParent node
	 */
	public void putAlphaSet (int nodeNumber, Dot2TreeInnerNodesParent node) {
		this.alphaSet.put(nodeNumber, node);
	}
	
	/**
	 * This setter adds a node to the current deltaSet.
	 * @param int nodeNumber
	 * @param Dot2TreeInnerNodesParent node
	 */
	public void putDeltaSet (int nodeNumber, Dot2TreeInnerNodesParent node) {
		this.deltaSet.put(nodeNumber, node);
	}
	
	/**
	 * This setter adds an additional string to the N-set.
	 * @param int nodeNumber
	 * @param String nString
	 */
	public void setNSet (int nodeNumber, String nString) {
		this.nSetStrings.put(nodeNumber, nString);
	}
	
	// End setters.
	
	// End Methods.

}
