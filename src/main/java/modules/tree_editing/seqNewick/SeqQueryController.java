package modules.tree_editing.seqNewick;


import java.util.Properties;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Iterator;
import java.io.PipedReader;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.format_conversion.seqNewickExporter.SeqNewickNodeV2;

import com.google.gson.Gson;

import common.parallelization.CallbackReceiver;


/**
 * Reads JSON format from I/O pipe.
 * JSON format must be in the format contributed by the package "modules.treeBuilder.TreeBuilderV2Module.java".
 * This module will write a String variable to the char pipe.
 * Output format will be in Newick format for tree visualization purposes.
 * @author christopher
 *
 */
public class SeqQueryController extends ModuleImpl {
	//property keys:
	public static final String PROPERTYKEY_NEWICK = "Newick branch length";
	public static final String PROPERTYKEY_EDGELABEL = "Path label";
	//end keys

	//variables:

	private final String INPUTID = "JSON";
	private final String OUTPUTID = "Newick";
	private String newickOutput;
	private boolean standardOut;
	private String queryPathLabel;
	
	//private TreeNode mainNode;
	private SeqNewickNodeV2 mainNode;
	
	private Gson gson;
	private SeqNewickNodeV2 rootNode;
	
	//end variables
	
	//constructors:
	public SeqQueryController (CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// Add property descriptions
		this.getPropertyDescriptions().put(PROPERTYKEY_EDGELABEL, 
					"Enter Path Label as String<\br>"
					+ "Path label must befin with \"^\"</br>"
					+ "Example:<pre><code>^ATGCGC</code></pre>");
		
		this.getPropertyDescriptions().put(PROPERTYKEY_NEWICK, 
				"Choose branch length in Newick:</br>"
				+ "\"true\" = by string.</br>"
				+ "\"false\" = by node occurence");
		
		// Add module category

		
		// Add property defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "SeqQuery");
		this.getPropertyDefaultValues().put(PROPERTYKEY_EDGELABEL, "^");
		this.getPropertyDefaultValues().put(PROPERTYKEY_NEWICK, "true");
		
		// set up newickOutput
		this.newickOutput = new String();
		
		// Define I/O
		InputPort inputPort = new InputPort(INPUTID, "[JSON treeBuilder v3]</br>Suffix tree.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUTID, "[plain text] Newick suffix tree.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		
	}
	//end constructors
	
	//setters:
		
	public void setGson(PipedReader reader) {
		
		gson = new Gson();
		mainNode = gson.fromJson(reader, SeqNewickNodeV2.class);
	}
	
	public void setRootNewickNode(String val, int count) {
		String nodeVal = val;
		int counter = count;
		int newCount = nodeVal.length();
		if (standardOut) {
			String newNode = nodeVal + ":" + Integer.toString(newCount) + ",";
			newickOutput += newNode;
		} else {
			String newNode = nodeVal +  ":" + Integer.toString(counter) + ",";
			newickOutput += newNode;
		}
	}
	public void addRootNewickEnd() {
		newickOutput = newickOutput.substring(0, newickOutput.length()-1); //remove last comma at the end of last node
		String endParenthesis = ");";
		newickOutput += endParenthesis;
	}
	
	public void setConcatNewickNode(String val, int count) {
		String nodeVal = val;
		int counter = count;
		int newCount = nodeVal.length();
		if (standardOut) {
			String newNode = nodeVal + ")" + ":" + Integer.toString(newCount) + ",";
			newickOutput += newNode;
		} else {
			String newNode = nodeVal + ")" +  ":" + Integer.toString(counter) + ",";
			newickOutput += newNode;
		}
	}
	
	public void setInnerNewickNode(String val, int count) {
		String nodeVal = val;
		int counter = count;
		int newCount = nodeVal.length();
		if (standardOut) {
			String newNode = ")" + nodeVal + ":" + Integer.toString(newCount) + ",";
			newickOutput += newNode;
		} else {
			String newNode = ")" + nodeVal + ":" + Integer.toString(counter) + ",";
			newickOutput += newNode;
		}
	}
			
	public void setTermNewickNode(String val, int count) {
		String nodeVal = val;
		int counter = count;
		int newCount = nodeVal.length();
		if (standardOut) {
			String newNode = "(" + nodeVal + ")" + ":" + Integer.toString(newCount) + ",";
			newickOutput += newNode;
		} else {
			String newNode ="(" + nodeVal + ")" + ":" + Integer.toString(counter) + ",";
			newickOutput += newNode;
		}
	}

	public void setLastTermNewickNode(String val, int count) {
		String nodeVal = val;
		int counter = count;
		int newCount = nodeVal.length();
		if (standardOut) {
			String newNode = "(" + nodeVal + ")" + ":" + Integer.toString(newCount);
			newickOutput += newNode;
		} else {
			String newNode ="(" + nodeVal + ")" + ":" + Integer.toString(counter);
			newickOutput += newNode;
		}
	}
	
	public void addLeadingParenthesisNewick() {
		String newParenthesis = "(";
		newickOutput += newParenthesis;
	}
	
	//remove last open parenthesis
	public void removeLeadingParenthesisNewick() {
		newickOutput = newickOutput.substring(0, newickOutput.length()-1); 
	}
	
	//remove last comma from output
	public void removeLastCommaNewick() {
		newickOutput = newickOutput.substring(0, newickOutput.length()-1);
	}
	
	//end setters
	
	//getters:
	public String getNewick () {
		return newickOutput;
	}
	
	//end getters
	
	@Override
	public boolean process() throws Exception {
		
		//create mainNode by reading JSON input
		this.setGson(this.getInputPorts().get(INPUTID).getInputReader());
		
		// Query suffix tree.
		SeqNewickNodeV2 qNode = this.querySuffixTree();
		
		//create Newick output format
		this.iterateNewickQueryNode(qNode);
		
		// write Newick to output
		this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(this.getNewick()); 
					
		// Close outputs (important!)
		this.closeAllOutputs();
		
		// Done
		return true;
	}
	
	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		
		// Apply own properties
		if (this.getProperties().containsKey(PROPERTYKEY_NEWICK))
			this.standardOut = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_NEWICK, this.getPropertyDefaultValues().get(PROPERTYKEY_NEWICK)));
		
		if (this.getProperties().containsKey(PROPERTYKEY_EDGELABEL))
			this.queryPathLabel = this.getProperties().getProperty(PROPERTYKEY_EDGELABEL, this.getPropertyDefaultValues().get(PROPERTYKEY_EDGELABEL));
		
		// Apply parent object's properties
		super.applyProperties();
	}
	
	// Query suffix tree.
	public SeqNewickNodeV2 querySuffixTree() {

		SeqNewickNodeV2 resultNode = new SeqNewickNodeV2("^", this.mainNode.getNodeCounter());
		resultNode = this.mainNode;
		
		// In the unlikely event that the root is the target to the query, return the root node.
	 	if (this.queryPathLabel.equals("^")) {
	 		// TODO: Add a warning here!
	 		return resultNode;
	 	}
	 	
		
	 	// Iterate over the suffix tree.
		for (int i = 1; i < (this.queryPathLabel.length() + 1); i ++ ) {
			if (this.mainNode.getChildNodes().containsKey(this.queryPathLabel.substring(1, i))) {
				
				resultNode = queryIteration(this.queryPathLabel.substring(1, i), i , mainNode.getChildNodes().get(this.queryPathLabel.substring(1, i)) );
				break;
			}
		}
		
		// Return the starting node of the target sub tree.
		return resultNode;
	}
	
	// Iterate subtrees of the suffix tree.
	public SeqNewickNodeV2 queryIteration (String edgeLabel, int currEdgePos, SeqNewickNodeV2 node) {
		
		SeqNewickNodeV2 resultNode = node;
		
		// We reached the desired target node.
		if (this.queryPathLabel.substring(currEdgePos - 1).equals(edgeLabel)) {
			return node;
		}
		
		// Continue searching for sub nodes.
		for (int i = currEdgePos; i < (this.queryPathLabel.length() + 1); i ++ ) {
			
			if (node.getChildNodes().containsKey(this.queryPathLabel.substring(currEdgePos, i))) {
				resultNode = queryIteration(this.queryPathLabel.substring(currEdgePos, i), i , node.getChildNodes().get(this.queryPathLabel.substring(currEdgePos, i)) );
				break;
			}
		}
		return resultNode;
	}
	
	//creation of a Newick tree
	public void iterateNewickQueryNode( SeqNewickNodeV2 qNode) { 

		// instantiate new root node
		rootNode = new SeqNewickNodeV2(this.queryPathLabel, qNode.getNodeCounter()); 
		
		// add root node to Newick format
		addLeadingParenthesisNewick();
		setRootNewickNode(rootNode.getValue(), rootNode.getCounter()); 
		
		Iterator<Entry<String, SeqNewickNodeV2>> it = qNode.getChildNodes().entrySet().iterator();
		
		while (it.hasNext()) {
			HashMap.Entry<String, SeqNewickNodeV2> pair = (HashMap.Entry<String, SeqNewickNodeV2>)it.next();
			
			if(pair.getValue().getChildNodes().isEmpty()) {
				//end node on first level reached. Create terminal node at tree height 0.
				SeqNewickNodeV2 node = new SeqNewickNodeV2(pair.getKey(), pair.getValue().getNodeCounter());
				setTermNewickNode(node.getValue(), node.getCounter()); //new Newick terminal node
				rootNode.addNode(pair.getKey(), node);
			} else {
					if (pair.getValue().getChildNodes().size() == 1) {
							SeqNewickNodeV2 node = new SeqNewickNodeV2(pair.getKey(), pair.getValue().getNodeCounter());
							
							// add child node to Newick format
							addLeadingParenthesisNewick(); 
							
							SeqNewickNodeV2 childNode;
							
							if (it.hasNext()) {
								childNode = deepNewickIteration(false, pair.getKey(), pair.getValue(), node);
							} else {
								childNode = deepNewickIteration(true, pair.getKey(), pair.getValue(), node);
							}
							
							// add child node to Newick format
							setConcatNewickNode(childNode.getValue(), childNode.getCounter());
							
							//get node directly beneath childnode
							rootNode.addNode(childNode.getValue(), childNode); 
							
					} else if(pair.getValue().getChildNodes().size() > 1) {
							Iterator<Entry<String, SeqNewickNodeV2>> subIt = pair.getValue().getChildNodes().entrySet().iterator();
							
							SeqNewickNodeV2 node = new SeqNewickNodeV2(pair.getKey(), pair.getValue().getNodeCounter());
							
							//add node to Newick format
							addLeadingParenthesisNewick(); 
							
							while (subIt.hasNext()) {
								HashMap.Entry<String, SeqNewickNodeV2> subPair = (HashMap.Entry<String, SeqNewickNodeV2>)subIt.next();
								SeqNewickNodeV2 subNode = new SeqNewickNodeV2(subPair.getKey(), subPair.getValue().getNodeCounter());
								
								boolean lastTerm; 
								
								if(subIt.hasNext()) {
									lastTerm = false;
								} else {
									lastTerm = true;
								}
							
								// add child node to Newick format
								addLeadingParenthesisNewick(); 
								
								// in case the child node does not have another child remove the first parenthesis
								if (subPair.getValue().getChildNodes().isEmpty()) {
									removeLeadingParenthesisNewick();
								}
								
								SeqNewickNodeV2 childNode = deepNewickIteration(lastTerm, subPair.getKey(), subPair.getValue(), subNode);						
															
								if (childNode.getNodeHash().size() > 0) {
									// add child node to Newick format
									setInnerNewickNode(childNode.getValue(), childNode.getCounter());
								}
								
								if (lastTerm && !(childNode.getNodeHash().size() == 0)) { // avoid additional comma error in Newick output by placing commas only in between nodes
									removeLastCommaNewick();
								}
								
								node.addNode(childNode.getValue(), childNode);
								subIt.remove(); // avoids a ConcurrentModificationException
							}
							//add child node to Newick format
							setInnerNewickNode(node.getValue(), node.getCounter());
							
							rootNode.addNode(node.getValue(), node);
					}
			}
		    it.remove(); // avoids a ConcurrentModificationException
		}
		//end the Newick entry for the whole tree
		addRootNewickEnd();
	}
	
	private SeqNewickNodeV2 deepNewickIteration(boolean term, String nodeKey, SeqNewickNodeV2 Node, SeqNewickNodeV2 propNode) {
		boolean lastTerm = term;
		
		String currNodeValue = nodeKey;
		
		SeqNewickNodeV2 currentNode = Node;
		
		SeqNewickNodeV2 currPropNode = new SeqNewickNodeV2(currNodeValue, currentNode.getNodeCounter());
		
		// reaching a terminal node adds the sequence to the previous node
		if (currentNode.getChildNodes().isEmpty()) {
			if (lastTerm) {
				setLastTermNewickNode(currPropNode.getValue(), currPropNode.getCounter()); //new Newick terminal node
			} else {
				setTermNewickNode(currPropNode.getValue(), currPropNode.getCounter()); //new Newick terminal node
			}
			return currPropNode;
		} else {
			Iterator<Entry<String, SeqNewickNodeV2>> deepIt = currentNode.getChildNodes().entrySet().iterator();
			while (deepIt.hasNext()) {
				HashMap.Entry<String, SeqNewickNodeV2> deepPair = (HashMap.Entry<String, SeqNewickNodeV2>)deepIt.next();
				
				if (deepIt.hasNext()) {
					lastTerm = false;
				} else {
					lastTerm = true;
				}
				
				if(deepPair.getValue().getChildNodes().size() == 0) {
					
					SeqNewickNodeV2 newPropNode = new SeqNewickNodeV2(deepPair.getKey(),deepPair.getValue().getNodeCounter());				
					
					// new Newick terminal node
					if (lastTerm) {
						setLastTermNewickNode(newPropNode.getValue(), newPropNode.getCounter());
					} else {
						setTermNewickNode(newPropNode.getValue(), newPropNode.getCounter());
					}
					currPropNode.addNode(newPropNode.getValue(), newPropNode);
					
				} 
				
				 //if child has one grand child
				 else if(deepPair.getValue().getChildNodes().size() == 1) { 
					
					//add a leading Parenthesis to the Newick output
					addLeadingParenthesisNewick(); 
					
					//Let's continue with the child
					SeqNewickNodeV2 newPropNode = deepNewickIteration(lastTerm, deepPair.getKey(), deepPair.getValue(), currPropNode); 
					
					//write content of inner node
					setInnerNewickNode(newPropNode.getValue(), newPropNode.getCounter());
					
					//avoid additional comma error at the end of an inner node
					if (lastTerm) {
						removeLastCommaNewick();
					}
					
					//add new node to current node
					currPropNode.addNode(newPropNode.getValue(), newPropNode);
					
				} 
				  
				  // if child has several grand children
				  else if (deepPair.getValue().getChildNodes().size() > 1) { 
					
					addLeadingParenthesisNewick(); //add a leading Parenthesis to the Newick output
					SeqNewickNodeV2 newNode = deepNewickIteration(lastTerm, deepPair.getKey(), deepPair.getValue(),currPropNode);
					setInnerNewickNode(newNode.getValue(), newNode.getCounter()); //write content of inner node
					
					//avoid additional comma error at the end of an inner node
					if (lastTerm) {
						removeLastCommaNewick();
					}
					
					currPropNode.addNode(newNode.getValue(), newNode);
					
				}
			}
			deepIt.remove(); // avoids a ConcurrentModificationException
			return currPropNode;
		}
	}

}
