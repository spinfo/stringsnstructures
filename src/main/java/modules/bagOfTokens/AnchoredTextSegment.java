package modules.bagOfTokens;

import java.util.ArrayList;

/**
 * POJO to hold a String, that is a segment of a larger text. Instances of the
 * segment are identified by a list of TextAnchor objects.
 */
public class AnchoredTextSegment {

	// The segment of text, that the object represents
	private String segment;

	// A list of TextAnchors representing where the segment appears
	private ArrayList<TextAnchor> textAnchors;

	/**
	 * An AnchoredTextSegment without any attributes set
	 */
	public AnchoredTextSegment() {
		this.textAnchors = new ArrayList<TextAnchor>();
	}

	/**
	 * An AnchoredTextSegment representing the input String.
	 * 
	 * @param segment
	 *            Segment
	 */
	public AnchoredTextSegment(String segment) {
		this();
		this.segment = segment;
	}

	/**
	 * Adds a single TextAnchor to this segment
	 * 
	 * @param anchor
	 *            Anchor
	 */
	public void addAnchor(TextAnchor anchor) {
		this.textAnchors.add(anchor);
	}

	/**
	 * @return The text, that this segment represents
	 */
	public String getSegment() {
		return segment;
	}

	/**
	 * @param segment
	 *            The text, that this object should represent
	 */
	public void setSegment(String segment) {
		this.segment = segment;
	}

	/**
	 * @return The list of anchors representing occurences of the segment string
	 */
	public ArrayList<TextAnchor> getTextAnchors() {
		return textAnchors;
	}

	/**
	 * @param textAnchors
	 *            The list of anchors that should represent the occurences of
	 *            the segment string
	 */
	public void setTextAnchors(ArrayList<TextAnchor> textAnchors) {
		this.textAnchors = textAnchors;
	}

}
