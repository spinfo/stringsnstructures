package modules.tree_building.suffixTreeClustering.data;

public class Token {
	String text;
	int contextStart;
	int contextEnd;
	private int typePosition;
	private int source;

	public void setContextEnd(int contextEnd) {
		this.contextEnd = contextEnd;
	}

	public void setContextStart(int contextStart) {
		this.contextStart = contextStart;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setTypePosition(int pos) {
		this.typePosition = pos;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public String getText() {
		return text;
	}

	public int getContextEnd() {
		return contextEnd;
	}

	public int getContextStart() {
		return contextStart;
	}

	public int getSource() {
		return source;
	}

	public int getTypePosition() {
		return typePosition;
	}

	@Override
	public String toString() {
		return String.format("Token '%s' from %s to %s (position: %s)", text, contextStart,
				contextEnd, typePosition);
	}
}