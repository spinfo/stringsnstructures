package common;

import java.util.HashMap;
import java.util.Map;

public class ParentRelationTreeNodeImpl implements ParentRelationTreeNode {
	
	private String nodeValue;
	private int nodeCounter;
	private ParentRelationTreeNode parentNode;
	private Map<String, TreeNode> childNodes = new HashMap<String, TreeNode>();

	public ParentRelationTreeNodeImpl(String nodeValue, ParentRelationTreeNode parentNode) {
		super();
		this.nodeValue = nodeValue;
		this.parentNode = parentNode;
	}

	@Override
	public String getNodeValue() {
		return this.nodeValue;
	}

	@Override
	public int getNodeCounter() {
		return this.nodeCounter;
	}

	@Override
	public Map<String, TreeNode> getChildNodes() {
		return this.childNodes;
	}

	@Override
	public ParentRelationTreeNode getParentNode() {
		return this.parentNode;
	}

	@Override
	public void setNodeValue(String nodeValue) {
		this.nodeValue = nodeValue;
	}

	@Override
	public void setNodeCounter(int nodeCounter) {
		this.nodeCounter = nodeCounter;
	}

	@Override
	public int incNodeCounter() {
		return ++this.nodeCounter;
	}

	@Override
	public void setParentNode(ParentRelationTreeNode parentNode) {
		this.parentNode = parentNode;
	}

}
