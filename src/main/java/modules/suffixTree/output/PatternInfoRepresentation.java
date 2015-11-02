package modules.suffixTree.output;

public class PatternInfoRepresentation {

	// Represents the unit nr that this pattern belongs to
	private Integer typeNr;

	// Represents the pattern nr that this object represents
	private Integer patternNr;

	// Represents the the start position of this pattern in the text that was
	// the input for the suffix tree
	private Integer startPos;

	public PatternInfoRepresentation() {
		this.typeNr = null;
		this.patternNr = null;
		this.startPos = null;
	}
	
	public boolean isComplete() {
		return ((this.typeNr != null) && (this.patternNr != null) && (this.startPos != null));
	}
	
	/**
	 * @return the typeNr
	 */
	public Integer getTypeNr() {
		return typeNr;
	}

	/**
	 * @param typeNr the typeNr to set
	 */
	public void setTypeNr(Integer typeNr) {
		this.typeNr = typeNr;
	}

	/**
	 * @return the patternNr
	 */
	public Integer getPatternNr() {
		return patternNr;
	}

	/**
	 * @param patternNr the patternNr to set
	 */
	public void setPatternNr(Integer patternNr) {
		this.patternNr = patternNr;
	}

	/**
	 * @return the startPos
	 */
	public Integer getStartPos() {
		return startPos;
	}

	/**
	 * @param startPos the startPos to set
	 */
	public void setStartPos(Integer startPos) {
		this.startPos = startPos;
	}
}
