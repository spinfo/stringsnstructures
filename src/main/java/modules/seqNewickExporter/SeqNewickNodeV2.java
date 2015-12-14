package modules.seqNewickExporter;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import common.ParentRelationTreeNode;
import common.TreeNode;

/**
 * Helper class for Newick Exporter module(s)
 * 
 * @author Christopher Kraus
 *
 */
public class SeqNewickNodeV2 implements ParentRelationTreeNode {
	//variables:
	private String nodeValue; //string saved in the node
	private int nodeCounter; //Zaehler value
	TreeMap<String, TreeNode> childNodes;
	private ParentRelationTreeNode parentNode; //TODO: kick out unnecessary variable implement declaration of ParentRelationTreeNode
	//end variables
	
	//constructors:
	public SeqNewickNodeV2(String value, int counter) {
		nodeValue = value;
		nodeCounter = counter;
		childNodes = new TreeMap<String, TreeNode>();
		this.parentNode = null;
	}
	
	public SeqNewickNodeV2(String value, int counter, SeqNewickNodeV2 node) {
		nodeValue = value;
		nodeCounter = counter;
		childNodes = new TreeMap<String, TreeNode>();
		childNodes.put(value, node);
		this.parentNode = null;
	}
	//end constructors
	
	//methods:
	//setters:
	public void setValue (String val) {
		nodeValue = val;
	}
	
	public void concatValue (String val) {
		nodeValue += val;
	}
	
	public void setCount (int count) {
		nodeCounter = count;
	}
	
	public void addCount (int count) {
		nodeCounter += count;
	}
	
	public void addNode (String value, SeqNewickNodeV2 node) {
		childNodes.put(value, node);
	}
	//end setters
	
	//getters:
	public String getValue ()  {
		return nodeValue;
	}
	
	public int getCounter ()  {
		return nodeCounter;
	}
	
	public Map<String, TreeNode> getNodeHash () {
		return childNodes;
	}
	//end getters
	//end methods

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
	public void setParentNode(ParentRelationTreeNode parentNode) {
		this.parentNode = parentNode;
	}

	@Override
	public SortedMap<String, TreeNode> getChildNodesByPrefix(String prefix) {
		return this.childNodes.subMap(prefix, prefix + Character.MAX_VALUE);
	}

}
