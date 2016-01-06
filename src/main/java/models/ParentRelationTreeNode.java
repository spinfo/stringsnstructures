package models;

import java.util.SortedMap;

public interface ParentRelationTreeNode extends MutableTreeNode {

	public ParentRelationTreeNode getParentNode();
	public void setParentNode(ParentRelationTreeNode parentNode);
	
	/**
	 * Returns an alphabetically sorted map of all children
	 * whose key starts with the specified prefix.
	 * @param prefix Prefix
	 * @return Map Sorted map
	 */
	public SortedMap<String, TreeNode> getChildNodesByPrefix(String prefix);
	
}
