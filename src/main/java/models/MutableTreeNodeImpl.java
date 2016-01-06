package models;

import java.util.HashMap;
import java.util.Map;

public class MutableTreeNodeImpl implements MutableTreeNode {

	private String nodeValue;
	private int nodeCounter;
	private Map<String, TreeNode> childNodes;
	
	public MutableTreeNodeImpl() {
		this("");
	}
	
	public MutableTreeNodeImpl(String nodeValue) {
		this(nodeValue,0);
	}
	
	public MutableTreeNodeImpl(String nodeValue, int nodeCounter) {
		this(nodeValue,nodeCounter,new HashMap<String, TreeNode>());
		this.nodeValue = nodeValue;
		this.nodeCounter = nodeCounter;
	}
	
	public MutableTreeNodeImpl(String nodeValue, int nodeCounter,
			Map<String, TreeNode> childNodes) {
		super();
		this.nodeValue = nodeValue;
		this.nodeCounter = nodeCounter;
		this.childNodes = childNodes;
	}

	@Override
	public String getNodeValue() {
		return nodeValue;
	}

	@Override
	public int getNodeCounter() {
		return nodeCounter;
	}

	@Override
	public Map<String, TreeNode> getChildNodes() {
		return childNodes;
	}

	@Override
	public void setNodeValue(String nodeValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNodeCounter(int nodeCounter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int incNodeCounter() {
		// TODO Auto-generated method stub
		return 0;
	}

}
