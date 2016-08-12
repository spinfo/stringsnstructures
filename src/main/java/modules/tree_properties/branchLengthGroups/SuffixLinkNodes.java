package modules.tree_properties.branchLengthGroups;

/**
 * This class saves the results of the backwards iteration following the 
 * suffix links of internal nodes.
 * @author christopher
 *
 */

public class SuffixLinkNodes {
	
	// Variables:
	
	// This variable holds the length of the current suffix link path.
	private int suffixLinkPathLen;
	
	// This variable holds the start of the iteration.
	private int startNodeNumber;
	
	// This variable holds the end of the iteration.
	private int finalNodeNumber;
	
	// This variable holds the concatenated edgeLabels for following suffix links.
	private String concatEdgeLabs;
	
	// End variables.
	
	// Constructors:
	
	public SuffixLinkNodes (int pathLen, int startNum, int endNum, String edgeLab) {
		this.suffixLinkPathLen = pathLen;
		this.startNodeNumber = startNum;
		this.finalNodeNumber = endNum;
		this.concatEdgeLabs = edgeLab;
	}
	
	// End constructors.
	
	// Setters:
	/**
	 * Sets the suffix link path length.
	 * @param int length
	 */
	public void setSuffixLinkPathLen(int length) {
		this.suffixLinkPathLen = length;
	}
	
	/**
	 * Sets the start Node number.
	 * @param int nodeNumber
	 */
	public void setStartNodeNumber (int nodeNum) {
		this.startNodeNumber = nodeNum;
	}
	
	/**
	 * Sets the node number of the last node at the iteration.
	 * @param int this.finalNodeNumber
	 */
	public void setFinalNodeNumber (int nodeNum) {
		this.finalNodeNumber = nodeNum;
	}
	
	/**
	 * Sets the concatenated edge labels after following suffix links.
	 * @param edgeLab
	 */
	public void setConcatEdgeLabs (String edgeLab) {
		this.concatEdgeLabs = edgeLab;
	}
	// End setters.
	
	// Getters:
	
	/**
	 * Returns the suffix link path length.
	 * @return int this.suffixLinkPathLen
	 */
	public int getSuffixLinkPathLen() {
		return this.suffixLinkPathLen;
	}
	
	/**
	 * Returns the start Node number.
	 * @return int this.startNodeNumber
	 */
	public int getStartNodeNumber () {
		return this.startNodeNumber;
	}
	
	/**
	 * Returns the node number of the last node at the iteration.
	 * @return int this.finalNodeNumber
	 */
	public int getFinalNodeNumber () {
		return this.finalNodeNumber;
	}
	
	/**
	 * Returns the concatenated edge labels after following suffix links.
	 * @return String this.concatEdgeLabs
	 */
	public String getConcatEdgeLabs () {
		return this.concatEdgeLabs;
	}
	// End getters.

}
