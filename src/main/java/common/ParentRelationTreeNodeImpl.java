package common;

import java.util.HashMap;
import java.util.Iterator;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		//return this.toString("");
		return this.nodeValue.concat(":"+this.nodeCounter);
	}
	
	public String toString(String prefix){
		StringBuffer sb = new StringBuffer(prefix.concat(this.nodeValue.concat(":"+this.nodeCounter)));
		Iterator<TreeNode> children = this.childNodes.values().iterator();
		while(children.hasNext()){
			ParentRelationTreeNodeImpl child = (ParentRelationTreeNodeImpl) children.next();
			sb.append("\n"+child.toString(prefix+"\t"));
		}
		return sb.toString();
	}
	
	

}
