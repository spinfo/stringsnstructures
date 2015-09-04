package modules.suffixTreeClustering.data;

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

	/*
	 * Nodes are compared by their ID. (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Node o) {
		return this.nodeNumber - o.nodeNumber;
	}

	/**
	 * Get Type by ID.
	 * 
	 * @param number
	 *            - Type ID
	 * @return Type
	 */
	public Type getType(Integer number) {
		for (Type type : this.typesAndFrequencies.keySet()) {
			if (type.getID() == number)
				return type;
		}
		return null;
	}

	/**
	 * Get all Types that visited this node together with the start positions in
	 * text.
	 * 
	 * @return mapping of types and start positions
	 */
	public Map<Type, List<Integer>> getTypes() {
		return typesAndFrequencies;
	}

	/**
	 * Sets the node number as unique ID
	 * 
	 * @param number
	 *            node number
	 */
	public void setNodeNumber(int number) {
		if (this.nodeNumber != null)
			throw new UnsupportedOperationException(
					"Node number is unique and should not be set twice!");
		this.nodeNumber = number;
	}

	/**
	 * Sets the node's path label.
	 * 
	 * @param label
	 *            path label of node
	 */
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

	/**
	 * Get the node's unique number.
	 * 
	 * @return node number
	 */
	public int getNodeNumber() {
		return this.nodeNumber;
	}

	/**
	 * Get number of different types that share this node = document frequency
	 * 
	 * @return document frequency df
	 */
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

	/**
	 * Get all start positions of the suffix of a particular type.
	 * 
	 * @param type
	 * @return List of start positions
	 */
	public List<Integer> getStartPositionsOfType(Type type) {
		return this.typesAndFrequencies.get(type);
	}

	/**
	 * Get the number of times a specific type visited this node.
	 * 
	 * @param type
	 * @return number of visits
	 */
	public Integer getTermfrequencyFor(Type type) {
		if (this.typesAndFrequencies.get(type) != null)
			return this.typesAndFrequencies.get(type).size();
		return null;
	}

	/**
	 * Set the number of total visits for this node.
	 * 
	 * @param freq
	 *            frequency = number of visits
	 */
	public void setFrequency(int freq) {
		this.frequency = freq;
	}

	/**
	 * Get the number of total visits for this node.
	 * 
	 * @return frequency = number of visits
	 */
	public int getFrequency() {
		return frequency;
	}
}
