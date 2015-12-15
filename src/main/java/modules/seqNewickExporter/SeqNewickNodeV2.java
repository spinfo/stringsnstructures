package modules.seqNewickExporter;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Helper class for Newick Exporter module(s)
 * 
 * @author Christopher Kraus
 *
 */
public class SeqNewickNodeV2 {
	//variables:
	private String nodeValue; //string saved in the node
	private int nodeCounter; //Zaehler value
	TreeMap<String, SeqNewickNodeV2> childNodes;
	private SeqNewickNodeV2 parentNode;
	//end variables
	
	//constructors:
	public SeqNewickNodeV2(String value) {
		nodeValue = value;
		nodeCounter = 0;
		childNodes = new TreeMap<String, SeqNewickNodeV2>();
		this.parentNode = null;
	}
	
	public SeqNewickNodeV2(String value, int counter) {
		nodeValue = value;
		nodeCounter = counter;
		childNodes = new TreeMap<String, SeqNewickNodeV2>();
		this.parentNode = null;
	}
	
	public SeqNewickNodeV2(String value, int counter, SeqNewickNodeV2 node) {
		nodeValue = value;
		nodeCounter = counter;
		childNodes = new TreeMap<String, SeqNewickNodeV2>();
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
	
	public void setNodeValue(String nodeValue) {
		this.nodeValue = nodeValue;
		
	}
	
	public void setNodeCounter(int nodeCounter) {
		this.nodeCounter = nodeCounter;
	}
	
	public int incNodeCounter() {
		return ++this.nodeCounter;
	}
	
	public void setParentNode(SeqNewickNodeV2 parentNode) {
		this.parentNode = parentNode;
	}
	
	//end setters
	
	//getters:
	public String getValue ()  {
		return nodeValue;
	}
	
	public int getCounter ()  {
		return nodeCounter;
	}
	
	public Map<String, SeqNewickNodeV2> getNodeHash () {
		return childNodes;
	}
	
	public String getNodeValue() {
		return this.nodeValue;
	}
	
	public int getNodeCounter() {
		return this.nodeCounter;
	}
	
	public Map<String, SeqNewickNodeV2> getChildNodes() {
		return this.childNodes;
	}
	
	public SeqNewickNodeV2 getParentNode() {
		return this.parentNode;
	}
	
	public SortedMap<String, SeqNewickNodeV2> getChildNodesByPrefix(String prefix) {
		return this.childNodes.subMap(prefix, prefix + Character.MAX_VALUE);
	}
	
	//end getters
	//end methods
}
