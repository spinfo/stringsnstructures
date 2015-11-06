package modules.bagOfWords;

/**
 * POJO to hold a sentenceNr as well as a start and end index to identify a segment of text
 */
public class TextAnchor {

	// The sentence nr, that this anchor refers to
	private int sentenceNr;

	// Start index inside the sentence
	private int start;

	// End index inside the sentence
	private int end;

	/**
	 * Default empty constructor, returns a new TextAnchor without any
	 * attributes
	 */
	public TextAnchor() {
	}

	/**
	 * Complete constructor, returns a TextAnchor with all relevant attributes
	 * set.
	 * 
	 * @param sentenceNr
	 *            The sentence nr, that this anchor refers to
	 * @param start
	 *            Start index inside the sentence
	 * @param end
	 *            End index inside the sentence
	 */
	public TextAnchor(int sentenceNr, int start, int end) {
		this.sentenceNr = sentenceNr;
		this.start = start;
		this.end = end;
	}

	/**
	 * Attempts to parse a single string as a text anchor representation. Such a
	 * string consists of three integers separated by comma, e.g.: "1, 2, 3",
	 * where 1: sentence Nr, 2: start index, 3: end index. Leading and trailing
	 * whitespace is ignored.
	 * 
	 * @param input
	 *            The string to parse
	 * @return a new TextAnchor with all attributes set or null if the input
	 *         could not be parsed
	 */
	public static TextAnchor parse(String input) {
		TextAnchor result;
		final String[] numbers = input.trim().split(",", 4);
		try {
			final int sentenceNr = Integer.parseInt(numbers[0].trim());
			final int startIdx = Integer.parseInt(numbers[1].trim());
			final int endIdx = Integer.parseInt(numbers[2].trim());
			result = new TextAnchor(sentenceNr, startIdx, endIdx);
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException exception) {
			result = null;
		}
		return result;
	}

	public int getSentenceNr() {
		return sentenceNr;
	}

	public void setSentenceNr(int sentenceNr) {
		this.sentenceNr = sentenceNr;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

}
