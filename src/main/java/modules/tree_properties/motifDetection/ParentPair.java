package modules.tree_properties.motifDetection;

public class ParentPair {
	
	// Variables:
	
	// This boolean variable indicates whether the search was a success.
	private boolean outcome;
	
	// These variables hold the nodeNumbers of the start pair.
	private int startNode;
	private int suffixLink;
	
	// This variable holds the nodeNumber of the ancestor of the node.
	private int startNodeAncestor;
	
	// This variable holds the nodeNumber of the ancestor of the suffixLink.
	private int suffixLinkAncestor;
		
	// These variables hold the information about the distance (in nodes) from the 
	// ancestors to the startNode or suffixLink.
	private int startNodeDistance;
	private int suffixLinkDistance;
	
	// End variables.
	
	// Constructors:
	
	public ParentPair (int startNode, int nodeAncestor, int nodeDistance, 
			int suffixLink, int sLAncestor, int sLDistance) {
		
		this.startNode = startNode;
		this.startNodeAncestor = nodeAncestor;
		this.startNodeDistance = nodeDistance;
		
		this.suffixLink = suffixLink;
		this.suffixLinkAncestor = sLAncestor;
		this.suffixLinkDistance = sLDistance;
	}
	
	// End constructors.
	
	// Methods:
		
	// Setters:
	
	/**
	 * This setter sets the boolean value for the outcome 
	 * of the search.
	 * @param boolean outcome
	 */
	public void setOutcome (boolean outcome) {
		this.outcome = outcome;
	}
	
	/**
	 * This setter sets the startNode.
	 * @param int startNode
	 */
	public void setStartNode (int startNode) {
		this.startNode = startNode;
	}
	
	/**
	 * This setter sets the startNodeAncestor.
	 * @param int nodeAncestor
	 */
	public void setStartNodeAncestor (int nodeAncestor) {
		this.startNodeAncestor = nodeAncestor;
	}
	
	/**
	 * This setter sets the suffixLink.
	 * @param int suffixLink
	 */
	public void setSuffixLinkNode (int suffixLink) {
		this.suffixLink = suffixLink;
	}
	
	/**
	 * This setter sets the startNodeAncestor.
	 * @param int nodeAncestor
	 */
	public void setSuffixLinkAncestor (int sLAncestor) {
		this.suffixLinkAncestor = sLAncestor;
	}
	
	// End setters.
	
	// Getters:
	
	/**
	 * This getter retrieves the outcome of a search.
	 * @return boolean this.outcome
	 */
	public boolean getOutcome () {
		return this.outcome;
	}
	
	/**
	 * This getter returns the startNode.
	 * @return int this.startNode
	 */
	public int getStartNode () {
		return this.startNode;
	}
	
	/**
	 * This getter returns the startNodeAncestor.
	 * @return int this.startNodeAncestor
	 */
	public int getStartNodeAncestor () {
		return this.startNodeAncestor;
	}
	
	/**
	 * This getters returns the startNodeDistance.
	 * @return int this.startNodeDistance.
	 */
	public int getStartNodeDistance () {
		return this.startNodeDistance;
	}
	
	/**
	 * This getter returns the startNode.
	 * @return int this.startNode
	 */
	public int getSuffixLink () {
		return this.suffixLink;
	}
	
	/**
	 * This getter returns the startNodeAncestor.
	 * @return int this.startNodeAncestor
	 */
	public int getSuffixLinkAncestor () {
		return this.suffixLinkAncestor;
	}
	
	/**
	 * This getter returns the suffixLinkDistance.
	 * @return int this.suffixLinkDistance
	 */
	public int getSuffixLinkDistance () {
		return this.suffixLinkDistance;
	}
	
	// End getters.
	
	/**
	 * This method increments the startNodeDistance by 1.
	 */
	public void increStartNodeDistance () {
		this.startNodeDistance ++;
	}
	
	/**
	 * This method increments the suffixLinkDistance by 1.
	 */
	public void increSuffixLinkDistance () {
		this.suffixLinkDistance ++;
	}
	
	// End methods.

}
