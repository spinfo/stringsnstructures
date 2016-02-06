package models;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * POJO to hold data gathered on a label within a generalised suffix tree.
 * 
 * Serializable as csv
 * 
 * Data on multiple occurences of a label is collected in lists. These lists
 * have to preserve order of insertion such that the client may choose to map
 * information to a specific occurence by identifying the index in that list.
 * 
 * TODO: Is LinkedList really more memory efficient for labels than ArrayList?
 */
public class GstLabelData {

	private static final String CSV_HEADER = "label,sibling counts,child counts,occurence counts,leaf counts,levels";

	private String label;

	private List<Integer> siblingCounts = new LinkedList<Integer>();

	private List<Integer> childCounts = new LinkedList<Integer>();

	private List<Integer> occurenceCounts = new LinkedList<Integer>();

	private List<Integer> leafCounts = new LinkedList<Integer>();

	private List<Integer> levels = new LinkedList<Integer>();

	public GstLabelData() {
	}

	/**
	 * @return a line of csv as a representation of this objects values
	 */
	public String toCsv() {
		StringBuilder sb = new StringBuilder();
		this.toCsv(sb);
		return sb.toString();
	}

	/**
	 * Appends a line of csv to the StringBuilder that represents this object
	 * 
	 * @param sb
	 *            the StringBuilder to append to
	 */
	public void toCsv(StringBuilder sb) {
		sb.append("\"");
		sb.append(this.label);
		sb.append("\",");

		List<List<Integer>> lists = Arrays.asList(siblingCounts, childCounts, occurenceCounts, leafCounts, levels);
		for (List<Integer> list : lists) {
			printIntList(list, sb);
			sb.append(",");
		}
	}

	// prints a list of integers separated by spaces to the stringbuilder
	private void printIntList(List<Integer> list, StringBuilder sb) {
		for (int i : list) {
			sb.append(i);
			sb.append(" ");
		}
		sb.setLength(sb.length() - 1);
	}

	/**
	 * Parses row as a line of csv and and returns a GstLabelData object
	 * represented by that row
	 * 
	 * @param row
	 *            The String to interpre as a csv ro
	 * @return a GstLabelData object
	 */
	public static GstLabelData fromCsv(String row) {
		final String[] tokens = row.split(",");
		if (tokens.length != 6) {
			throw new IllegalStateException("Wrong number of tokens on line: " + row);
		}

		final GstLabelData result = new GstLabelData();
		if (tokens[0].startsWith("\"") && tokens[0].endsWith("\"") && tokens[0].length() >= 2) {
			result.setLabel(tokens[0].substring(1, tokens[0].length()-1));
		} else {
			result.setLabel(tokens[0]);
		}
		result.setSiblingCounts(parseListOfInts(tokens[1]));
		result.setChildCounts(parseListOfInts(tokens[2]));
		result.setOccurenceCounts(parseListOfInts(tokens[3]));
		result.setLeafCounts(parseListOfInts(tokens[4]));
		result.setLevels(parseListOfInts(tokens[5]));
		return result;
	}

	// parse a String as a list of integers separated by spaces
	private static List<Integer> parseListOfInts(String input) {
		final List<Integer> result = new LinkedList<Integer>();
		final String[] tokens = input.split(" ");
		for (String token : tokens) {
			result.add(Integer.parseInt(token));
		}
		return result;
	}

	/**
	 * @return the siblingCounts
	 */
	public List<Integer> getSiblingCounts() {
		return siblingCounts;
	}

	/**
	 * @param siblingCounts
	 *            the siblingCounts to set
	 */
	public void setSiblingCounts(List<Integer> siblingCounts) {
		this.siblingCounts = siblingCounts;
	}

	/**
	 * @return the childCounts
	 */
	public List<Integer> getChildCounts() {
		return childCounts;
	}

	/**
	 * @param childCounts
	 *            the childCounts to set
	 */
	public void setChildCounts(List<Integer> childCounts) {
		this.childCounts = childCounts;
	}

	/**
	 * @return the occurenceCounts
	 */
	public List<Integer> getOccurenceCounts() {
		return occurenceCounts;
	}

	/**
	 * @param occurenceCounts
	 *            the occurenceCounts to set
	 */
	public void setOccurenceCounts(List<Integer> occurenceCounts) {
		this.occurenceCounts = occurenceCounts;
	}

	/**
	 * @return the leafCounts
	 */
	public List<Integer> getLeafCounts() {
		return leafCounts;
	}

	/**
	 * @param leafCounts
	 *            the leafCounts to set
	 */
	public void setLeafCounts(List<Integer> leafCounts) {
		this.leafCounts = leafCounts;
	}

	/**
	 * @return the levels
	 */
	public List<Integer> getLevels() {
		return levels;
	}

	/**
	 * @param levels
	 *            the levels to set
	 */
	public void setLevels(List<Integer> levels) {
		this.levels = levels;
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
	 * @return the csvHeader
	 */
	public static String getCsvHeader() {
		return CSV_HEADER;
	}
}
