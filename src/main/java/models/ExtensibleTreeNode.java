package models;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ExtensibleTreeNode {
	
	private String nodeValue;
	private int nodeCounter = 0;
	private Map<String,ExtensibleTreeNode> childNodes = new TreeMap<String,ExtensibleTreeNode>();
	private Map<String,Object> attributes = new HashMap<String,Object>();
	
	public ExtensibleTreeNode() {
		super();
	}
	public ExtensibleTreeNode(String nodeValue) {
		super();
		this.nodeValue = nodeValue;
	}
	/**
	 * @return the attributes
	 */
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	/**
	 * @return the childNodes
	 */
	public Map<String, ExtensibleTreeNode> getChildNodes() {
		return childNodes;
	}
	/**
	 * @return the nodeCounter
	 */
	public int getNodeCounter() {
		return nodeCounter;
	}
	/**
	 * @return the nodeValue
	 */
	public String getNodeValue() {
		return nodeValue;
	}
	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
	/**
	 * @param childNodes the childNodes to set
	 */
	public void setChildNodes(Map<String, ExtensibleTreeNode> childNodes) {
		this.childNodes = childNodes;
	}
	/**
	 * @param nodeCounter the nodeCounter to set
	 */
	public void setNodeCounter(int nodeCounter) {
		this.nodeCounter = nodeCounter;
	}
	/**
	 * @param nodeValue the nodeValue to set
	 */
	public void setNodeValue(String nodeValue) {
		this.nodeValue = nodeValue;
	}

}
