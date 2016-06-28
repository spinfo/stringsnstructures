package modules.tree_properties.seqTreeProperties;

import java.util.HashMap;

/**
 * height of the tree (h = length of the longest path)
 * 	- average length of all paths
 * 	- ratio of subtree sites 
 * 	- average of subtree sites
 * 	- Sackin index and other tree balance measures for non-binary trees
 * @author Christopher Kraus
 *
 */


public class SeqPropertyNode {
	
	//variables:
	private String nodeValue; //string saved in the node
	private int nodeCounter; //Zaehler value
	private int nodeDepth; // remember the tree depth of this node
	HashMap<String, SeqPropertyNode> propNode;
	//end variables
	
	//constructors:
	public SeqPropertyNode(String value, int counter, int depth) {
		nodeValue = value;
		nodeCounter = counter;
		nodeDepth = depth;
		propNode = new HashMap<String, SeqPropertyNode>();
	}
	
	public SeqPropertyNode(String value, int counter, SeqPropertyNode node, int depth) {
		nodeValue = value;
		nodeCounter = counter;
		nodeDepth = depth;
		propNode = new HashMap<String, SeqPropertyNode>();
		propNode.put(value, node);
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
	
	public void addNode (String value, SeqPropertyNode node) {
		propNode.put(value, node);
	}
	
	public void setNodeDepth (int depth) {
		nodeDepth = depth;
	}
	//end setters
	
	//getters:
	public String getValue ()  {
		return nodeValue;
	}
	
	public int getCounter ()  {
		return nodeCounter;
	}
	
	public HashMap<String, SeqPropertyNode> getNodeHash () {
		return propNode;
	}
	
	public int getNodeDepth () {
		return nodeDepth;
	}
	//end getters
	//end methods
}
