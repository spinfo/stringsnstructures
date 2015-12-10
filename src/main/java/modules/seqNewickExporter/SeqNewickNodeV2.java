package modules.seqNewickExporter;

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
	TreeMap<String, SeqNewickNodeV2> propNode;
	//end variables
	
	//constructors:
	public SeqNewickNodeV2(String value, int counter) {
		nodeValue = value;
		nodeCounter = counter;
		propNode = new TreeMap<String, SeqNewickNodeV2>();
	}
	
	public SeqNewickNodeV2(String value, int counter, SeqNewickNodeV2 node) {
		nodeValue = value;
		nodeCounter = counter;
		propNode = new TreeMap<String, SeqNewickNodeV2>();
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
	
	public void addNode (String value, SeqNewickNodeV2 node) {
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
	
	public TreeMap<String, SeqNewickNodeV2> getNodeHash () {
		return propNode;
	}
	//end getters
	//end methods

}
