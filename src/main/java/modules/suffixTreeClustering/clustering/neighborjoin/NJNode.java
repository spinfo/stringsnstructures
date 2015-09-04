package modules.suffixTreeClustering.clustering.neighborjoin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import modules.suffixTreeClustering.data.Type;

public class NJNode {

	private NJNode parent;
	private List<NJNode> children;
	private String label;
	private double branchLength;
	private Set<Type> clusteredDocuments;
	private Map<String, NJNode> childrenMap;

	public NJNode(NJNode parent, Type doc) {
		setParent(parent);
		children = new ArrayList<>();
		this.childrenMap = new HashMap<String, NJNode>();
		this.clusteredDocuments = new TreeSet<>();
		clusteredDocuments.add(doc);
		setLabel(doc.getString());
	}

	public NJNode(NJNode parent, String label) {
		setParent(parent);
		children = new ArrayList<>();
		this.childrenMap = new HashMap<String, NJNode>();
		this.clusteredDocuments = new TreeSet<>();
		setLabel(label);
	}

	void setParent(NJNode node) {
		this.parent = node;
	}

	private void setLabel(String label) {
		this.label = label;
	}

	public NJNode getParent() {
		return parent;
	}
	
	public NJNode getChild(int index) {
		return this.children.get(index);
	}

	public double getBranchLength() {
		return branchLength;
	}

	public List<NJNode> getChildren() {
		return children;
	}

	public String getLabel() {
		return label;
	}

	public Set<Type> getClusteredDocuments() {
		return clusteredDocuments;
	}

	public void addChild(NJNode child) {
		this.children.add(child);
		this.childrenMap.put(child.getLabel(), child);
	}

	public NJNode getChildByName(String name) {
		return childrenMap.get(name);
	}

	public void setBranchLength(double branchLength) {
		this.branchLength = branchLength;
	}

	public void removeChild(NJNode node) {
		this.children.remove(node);
	}

	@Override
	public String toString() {
		return String.format("Node %s (Parent: %s, Length: %s)", this.label,
				this.parent.label, this.branchLength);
	}

	public void addDocuments(Set<Type> documents) {
		this.clusteredDocuments.addAll(documents);
	}
}