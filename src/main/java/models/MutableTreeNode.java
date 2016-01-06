package models;

public interface MutableTreeNode extends TreeNode {

	public void setNodeValue(String nodeValue);
	public void setNodeCounter(int nodeCounter);
	
	/**
	 * Increments the counter by one and returns the new value.
	 * @return ++counter
	 */
	public int incNodeCounter();
	
}
