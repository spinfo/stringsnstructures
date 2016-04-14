package modules.suffixTreeV2;

/**
 * A simple POJO for the positions of nodes in a suffix tree.
 * 
 * Implements Comparable as well as overrides equals() and hashCode() to work in
 * sets, trees etc. based on it's values instead of by object identity.
 */
public class NodePosition implements Comparable<NodePosition> {

	private int start;
	// The end position for many nodes is modified while the tree is build
	private NodePositionEnd end;
	private int textNr;
	private int typeContextNr;

	public NodePosition(int start, NodePositionEnd end, int textNr, int typeContextNr) {
		this.start = start;
		this.end = end;
		this.textNr = textNr;
		this.typeContextNr = typeContextNr;
	}

	public int getStart() {
		return start;
	}

	protected void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end.val;
	}

	protected void setEnd(NodePositionEnd end) {
		this.end = end;
	}

	public int getTextNr() {
		return textNr;
	}

	protected void setTextNr(int textNr) {
		this.textNr = textNr;
	}

	public int getTypeContextNr() {
		return typeContextNr;
	}

	protected void setTypeContextNr(int typeContextNr) {
		this.typeContextNr = typeContextNr;
	}

	/**
	 * A NodePosition is smaller/greater than another if any of it's values is
	 * smaller/greter than the others. Vlues are checked in the order:
	 * typeContextNr, textNr, star, end If any of those is smaller/greater, the
	 * appropriate result is returned.
	 */
	@Override
	public int compareTo(NodePosition o) {
		if (this.typeContextNr < o.typeContextNr || this.textNr < o.textNr || this.start < o.start
				|| this.end.val < o.end.val) {
			return -1;
		} else if (this.typeContextNr > o.typeContextNr || this.textNr > o.textNr || this.start > o.start
				|| this.end.val > o.end.val) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * A NodePosition's hash is dependent on it's values (including the position's end's value).
	 * 
	 * @return An integer that is a hash over the NodePosition's values.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end.val;
		result = prime * result + start;
		result = prime * result + textNr;
		result = prime * result + typeContextNr;
		return result;
	}

	/**
	 * Two nodes are equal if all of their values (including the end's value) are equal.
	 * 
	 * @return Whether this NodePosition and the other share the same exact values.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodePosition other = (NodePosition) obj;
		if (end.val != other.end.val)
			return false;
		if (start != other.start)
			return false;
		if (textNr != other.textNr)
			return false;
		if (typeContextNr != other.typeContextNr)
			return false;
		return true;
	}

}
