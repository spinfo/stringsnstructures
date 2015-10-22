package modules.bagOfTokens;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class AnchoredTextSegment {

	// The segment of text, that the object represents
	private String segment;

	// A list of TextAnchors representing where the segment appears
	private ArrayList<TextAnchor> textAnchors;

	// A pattern for newlines, when parsing a segment representation
	private static final Pattern NEWLINE = Pattern.compile("\r\n|\n|\r");

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
	 */
	public AnchoredTextSegment(String segment) {
		this();
		this.segment = segment;
	}

	/**
	 * Adds a single TextAnchor to this segment
	 * 
	 * @param anchor
	 */
	public void addAnchor(TextAnchor anchor) {
		this.textAnchors.add(anchor);
	}

	/**
	 * Attempts to parse a single string as an anchored text segment
	 * representation. This is a multiline string with the segment on the first
	 * line followed by any number of lines (at least one), representing
	 * TextAnchor instances.
	 * 
	 * @param input
	 *            The string to parse
	 * @return a new AnchoredTextSegment with at least one TextAnchor or null if
	 *         the input could not be parsed
	 */
	public static AnchoredTextSegment parse(String input) {
		AnchoredTextSegment result = null;
		// split the input and process each line
		final String[] lines = NEWLINE.split(input);
		// process the first line as the segment String
		if (lines.length > 0 && lines[0].length() > 0) {
			result = new AnchoredTextSegment(lines[0]);
		}
		// if the segment exists, every other line is treated as a TextAnchor
		if (result != null) {
			for (int i = 1; i < lines.length; i++) {
				final String line = lines[i];
				final TextAnchor anchor = TextAnchor.parse(line);
				if (anchor != null) {
					result.addAnchor(anchor);
				} else {
					// abort parsing if parsing an anchor failed
					result = null;
					break;
				}
			}
			// check if the segment has at least one anchor, if not, abort
			if (result != null && result.getTextAnchors().isEmpty()) {
				result = null;
			}
		}
		return result;
	}

	/**
	 * @return The text, that this segment represents
	 */
	public String getSegment() {
		return segment;
	}
	
	/**
	 * An alias for .getSegment()
	 * @return The text, that this segment represents
	 */
	public String text() {
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
