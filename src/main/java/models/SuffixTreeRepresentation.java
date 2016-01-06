package models;

import java.util.ArrayList;

public class SuffixTreeRepresentation {

	// The amount of units in this tree
	private Integer unitCount;
	
	// The amount of nodes in this tree
	private Integer nodeCount;
	
	// Representations of the nodes in this tree
	private ArrayList<NodeRepresentation> nodes;
	
	public SuffixTreeRepresentation() {
		this.unitCount = null;
		this.nodeCount = null;
		this.nodes = new ArrayList<NodeRepresentation>();
	}

	/**
	 * @return the unitCount
	 */
	public Integer getUnitCount() {
		return unitCount;
	}

	/**
	 * @param unitCount the unitCount to set
	 */
	public void setUnitCount(Integer unitCount) {
		this.unitCount = unitCount;
	}

	/**
	 * @return the nodeCount
	 */
	public Integer getNodeCount() {
		return nodeCount;
	}

	/**
	 * @param nodeCount the nodeCount to set
	 */
	public void setNodeCount(Integer nodeCount) {
		this.nodeCount = nodeCount;
	}

	/**
	 * @return the nodes
	 */
	public ArrayList<NodeRepresentation> getNodes() {
		return nodes;
	}

	/**
	 * @param nodes the nodes to set
	 */
	public void setNodes(ArrayList<NodeRepresentation> nodes) {
		this.nodes = nodes;
	}
}
