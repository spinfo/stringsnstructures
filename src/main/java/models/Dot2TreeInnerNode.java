package models;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * Helper class which inherits from Dot2TreeNodes.
 * This helper class holds additional information important for inner nodes;
 * including a whole tree structure for all child nodes (including inner and leaf nodes)
 * and a list/map of all suffix links.
 * 
 * @author christopher
 *
 */


public class Dot2TreeInnerNode extends Dot2TreeNodes {
	
	// Variables:
	
	// In this case all suffix links are saved in form of the node numbers of the linked nodes to the current. 
	private ArrayList <Integer> nodeSuffixLinks;
		
	// HashMap holding the references to all inner nodes beneath.
	private HashMap<Integer, Dot2TreeInnerNode> dot2TreeInnerNode;
	
	// HashMap holding the references to all leaves.
	private HashMap<Integer, Dot2TreeLeafNode> dot2TreeLeaves;
		
	// End variables.
	
	
	// Constructors:
	
	public Dot2TreeInnerNode (int number) {
		
		super(number);
		this.dot2TreeInnerNode = new HashMap<Integer, Dot2TreeInnerNode>();
		this.dot2TreeLeaves = new HashMap<Integer, Dot2TreeLeafNode> ();
		this.nodeSuffixLinks = new ArrayList <Integer>();
		
	}
	
	public Dot2TreeInnerNode (int number, int frequency, String label) {
		
		super(number, frequency, label);
		this.dot2TreeInnerNode = new HashMap<Integer, Dot2TreeInnerNode>();
		this.dot2TreeLeaves = new HashMap<Integer, Dot2TreeLeafNode> ();
		this.nodeSuffixLinks = new ArrayList <Integer>();
		
	}
	
	public Dot2TreeInnerNode (int number, int frequency, String label, String edgeLabel) {
		
		super(number, frequency, label, edgeLabel);
		this.dot2TreeInnerNode = new HashMap<Integer, Dot2TreeInnerNode>();
		this.dot2TreeLeaves = new HashMap<Integer, Dot2TreeLeafNode> ();
		this.nodeSuffixLinks = new ArrayList<Integer>();
		
	}
	
	// End constructors.
	
	// Methods:
	
	// Setters:
	
	// In this case all suffix links are saved in form of a single level HashMap (no depth-iteration allowed). 
	public void setSuffixLinks (Integer suffixLink) {
		this.nodeSuffixLinks.add(suffixLink);
	}
	
	public void addInnerNode (Integer nodeNumber, Dot2TreeInnerNode node) {
		this.dot2TreeInnerNode.put(nodeNumber, node);
	}
	
	public void addLeaf (int nodeNumber, Dot2TreeLeafNode node) {
		this.dot2TreeLeaves.put(nodeNumber, node);
	}
	
	// End setters.
	
	// Getters:
	
	// Return all child nodes.
	public HashMap<Integer, Dot2TreeInnerNode> getAllChildNodes () {
		return this.dot2TreeInnerNode;
	}
	
	// Return the whole map of suffix links.
	public ArrayList <Integer> getAllSuffixLinks () {
		return this.nodeSuffixLinks;
	}
	
	public HashMap <Integer,Dot2TreeLeafNode> getAllLeaves() {
		return this.dot2TreeLeaves;
	}
	// End methods.

}
