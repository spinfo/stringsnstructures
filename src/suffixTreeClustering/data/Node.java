package suffixTreeClustering.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class representing a Node in a Suffix Tree.
 * 
 * @author neumannm
 */
public class Node implements Comparable<Node> {

	private String pathLabel;
	private Integer nodeNumber;
	// how many different documents share this node:
	private Integer df;
	// which type visited this node and with which start positions (list length
	// = frequency):
	private Map<Type, List<Integer>> typesAndFrequencies;
	// how often this node has been visited in total:
	private int frequency;

	public Node() {
		this.typesAndFrequencies = new TreeMap<Type, List<Integer>>();
		df = 0;
	}

	/**
	 * Add a Type (Document) to that Node. Interpretation: That Type visited
	 * this Node during construction of the SuffixTree.
	 * 
	 * @param newType
	 */
	public void addType(Type newType) {
		this.typesAndFrequencies.put(newType, new ArrayList<Integer>());
		df++;
	}

	@Override
	public String toString() {
		return "NodeNr: " + nodeNumber + " :: pathLabel: " + pathLabel
				+ " :: document frequency: " + df;
	}

	@Override
	public int compareTo(Node o) {
		return this.nodeNumber - o.nodeNumber;
	}

	/**
	 * Get Type by ID.
	 * 
	 * @param number
	 *            - Tyoe ID
	 * @return Type
	 */
	public Type getType(Integer number) {
		for (Type type : this.typesAndFrequencies.keySet()) {
			if (type.getID() == number)
				return type;
		}
		return null;
	}

	public Map<Type, List<Integer>> getTypes() {
		return typesAndFrequencies;
	}

	public void setNodeNumber(int number) {
		this.nodeNumber = number;
	}

	public void setPathLabel(String label) {
		this.pathLabel = label;
	}

	/**
	 * Sets the Document Frequency df for this Node. DF is defined as the number
	 * of different documents that visited this Node during construction of the
	 * Suffix Tree.
	 * 
	 * @param df
	 *            - Document Frequency
	 */
	public void setDF(int df) {
		this.df = df;
	}

	public int getNodeNumber() {
		return this.nodeNumber;
	}

	public Integer getDF() {
		return this.df;
	}

	/**
	 * Checks if the given Type visited this Node.
	 * 
	 * @param type
	 * @return true, if type visited this Node, otherwise false
	 */
	public boolean containsType(Type type) {
		return this.typesAndFrequencies.keySet().contains(type);
	}

	public List<Integer> getStartPositionsOfType(Type type) {
		return this.typesAndFrequencies.get(type);
	}

	public Integer getTermfrequencyFor(Type document) {
		if (this.typesAndFrequencies.get(document) != null)
			return this.typesAndFrequencies.get(document).size();
		return null;
	}

	public void setFrequency(int freq) {
		this.frequency = freq;
	}

	public int getFrequency() {
		return frequency;
	}
}
