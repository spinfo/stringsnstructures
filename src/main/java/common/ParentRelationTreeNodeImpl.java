package common;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ParentRelationTreeNodeImpl implements ParentRelationTreeNode {
	
	private String nodeValue;
	private int nodeCounter;
	private ParentRelationTreeNode parentNode;
	private TreeMap<String, TreeNode> childNodes = new TreeMap<String, TreeNode>();

	public ParentRelationTreeNodeImpl(ParentRelationTreeNode parentNode) {
		this(null,parentNode);
	}

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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		//return this.toString("\t");
		return this.nodeValue+":"+this.nodeCounter;
	}
	
	/**
	 * Fancy output containing all child nodes.
	 * @param prefix
	 * @return
	 */
	public String toString(String prefix){
		StringBuffer sb = new StringBuffer();
		//if (this.nodeValue != null && !this.nodeValue.isEmpty())
			sb.append(this.nodeValue+":");
		sb.append(this.nodeCounter);
		Iterator<String> childKeys = this.childNodes.keySet().iterator();
		while(childKeys.hasNext()){
			String childKey = childKeys.next();
			ParentRelationTreeNodeImpl child = (ParentRelationTreeNodeImpl) this.childNodes.get(childKey);
			sb.append("\n"+prefix);
			//if (this.nodeValue == null || this.nodeValue.isEmpty())
				sb.append(childKey+":");
			sb.append(child.toString(prefix+"\t"));
		}
		return sb.toString();
	}
	
	/**
	 * Returns an alphabetically sorted map of all children
	 * whose key starts with the specified prefix.
	 * @param prefix Prefix
	 * @return Map Sorted map
	 */
	@Override
	public SortedMap<String, TreeNode> getChildNodesByPrefix(String prefix) {
	    return this.childNodes.subMap(prefix, prefix + Character.MAX_VALUE);
	}

}
