package modules.suffixTreeModuleWrapper;

import java.util.ArrayList;

public class NodeRepresentation {

	// the unique number of this node in the suffix tree that this
	// representation is a part of
	private Integer number;

	// the concrete text, that this node refers to
	private String label;

	// the amount of visits that were made to this node while the suffix tree
	// was constructed
	private Integer frequency;

	// information about the occurences of this node in the text underlying the
	// suffix tree
	private ArrayList<PatternInfoRepresentation> patternInfos;

	public NodeRepresentation() {
		this.number = null;
		this.label = null;
		this.frequency = null;
		// initialise to capacity of one because we expect most nodes to have
		// only one pattern info
		this.patternInfos = new ArrayList<PatternInfoRepresentation>(1);
	}
	
	public boolean isComplete() {
		return ((this.number != null) && (this.label != null) && (this.frequency != null) && !this.patternInfos.isEmpty());
	}

	/**
	 * @return the number
	 */
	public Integer getNumber() {
		return number;
	}

	/**
	 * @param number
	 *            the number to set
	 */
	public void setNumber(Integer number) {
		this.number = number;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the frequency
	 */
	public Integer getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency
	 *            the frequency to set
	 */
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

	/**
	 * @return the patternInfos
	 */
	public ArrayList<PatternInfoRepresentation> getPatternInfos() {
		return patternInfos;
	}

	/**
	 * @param patternInfos
	 *            the patternInfos to set
	 */
	public void setPatternInfos(ArrayList<PatternInfoRepresentation> patternInfos) {
		this.patternInfos = patternInfos;
	}

}
