package seqTreeProperties;

import java.io.Serializable;
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
	
	//private static final long serialVersionUID = 1L; //I have no idea what to put here
	
	//variables:
	private String nodeValue; //string saved in the node
	private int nodeCounter; //Zaehler value
	HashMap<String, SeqPropertyNode> propNode;
	//end variables
	
	//constructors:
	public SeqPropertyNode(String value, int counter) {
		nodeValue = value;
		nodeCounter = counter;
		propNode = new HashMap<String, SeqPropertyNode>();
	}
	
	public SeqPropertyNode(String value, int counter, SeqPropertyNode node) {
		nodeValue = value;
		nodeCounter = counter;
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
	//end getters
	//end methods
}
