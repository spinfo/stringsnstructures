package seqSuffixTrie2SuffixTree;

import java.util.HashMap;

/**
 * This class file stores information for reduced trie nodes.
 * @author Christopher Kraus
 */


public class SeqReducedTrieNode {
	
	//variables:
	private String nodeValue; //string saved in the node
	private int nodeCounter; //Zaehler value
	HashMap<String, SeqReducedTrieNode> propNode;
	//end variables
	
	//constructors:
	public SeqReducedTrieNode(String value, int counter) {
		nodeValue = value;
		nodeCounter = counter;
		propNode = new HashMap<String, SeqReducedTrieNode>();
	}
	
	public SeqReducedTrieNode(String value, int counter, SeqReducedTrieNode node) {
		nodeValue = value;
		nodeCounter = counter;
		propNode = new HashMap<String, SeqReducedTrieNode>();
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
	
	public void addNode (String value, SeqReducedTrieNode node) {
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
	
	public HashMap<String, SeqReducedTrieNode> getNodeHash () {
		return propNode;
	}
	//end getters
	//end methods
}
