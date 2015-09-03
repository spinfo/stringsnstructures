package seqNewickExporter;

import java.util.Properties;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Iterator;

import modularization.CharPipe;
import java.io.PipedReader;
import modularization.ModuleImpl;
import parallelization.CallbackReceiver;
import seqSuffixTrie2SuffixTree.SeqReducedTrieNode;

import com.google.gson.Gson;

/**
 * Reads JSON format from I/O pipe.
 * JSON format must be in the format contributed by the package "seqSuffixTrie2SuffixTree".
 * This module will write a String variable to the char pipe.
 * Output format will be in Newick format for tree visualization purposes.
 * @author christopher
 *
 */
public class SeqNewickExproterController extends ModuleImpl {
	//property keys:
	public static final String PROPERTYKEY_NEWICK = "Newick branch length";
	//end keys

	//variables:
	
	private String newickOutput;
	private boolean standardOut;
	private SeqReducedTrieNode mainNode;
	private Gson gson;
	private SeqNewickNode rootNode;
	
	//end variables
	
	//constructors:
	public SeqNewickExproterController (CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// Add property descriptions
		this.getPropertyDescriptions().put(PROPERTYKEY_NEWICK, "Choose branch length in Newick: true = by string; false = by node occurence");
		
		// Add property defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "seqNewickExporter");
		this.getPropertyDefaultValues().put(PROPERTYKEY_NEWICK, "true");
		
		// set up newickOutput
		this.newickOutput = new String();
		
		// Define I/O
		this.getSupportedInputs().add(CharPipe.class);
		this.getSupportedOutputs().add(CharPipe.class);
		
	}
	//end constructors
	
	//setters:
		
	public void setGson(PipedReader reader) {
		gson = new Gson();
		mainNode = gson.fromJson(reader, SeqReducedTrieNode.class);
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
		this.setGson(this.getInputCharPipe().getInput());
					
		//create Newick output format
		this.iterateNewickMainNode();
		
		// write Newick to output
		this.outputToAllCharPipes(this.getNewick()); 
					
		// Close outputs (important!)
		this.closeAllOutputs();
		
		// Done
		return true;
	}
	
	@Override
	public void applyProperties() throws Exception {
		
		// Apply own properties
		if (this.getProperties().containsKey(PROPERTYKEY_NEWICK))
			this.standardOut = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_NEWICK, this.getPropertyDefaultValues().get(PROPERTYKEY_NEWICK)));
		
		// Apply parent object's properties
		super.applyProperties();
	}
	
	//creation of a Newick tree
	public void iterateNewickMainNode() { 

		// instantiate new root node
		rootNode = new SeqNewickNode("^", mainNode.getCounter()); 
		
		// add root node to Newick format
		addLeadingParenthesisNewick();
		setRootNewickNode(rootNode.getValue(), rootNode.getCounter()); 
		
		Iterator<Entry<String, SeqReducedTrieNode>> it = mainNode.getNodeHash().entrySet().iterator();
		
		while (it.hasNext()) {
			HashMap.Entry<String, SeqReducedTrieNode> pair = (HashMap.Entry<String, SeqReducedTrieNode>)it.next();
			
			if(pair.getValue().getNodeHash().isEmpty()) {
				//end node on first level reached. Create terminal node at tree height 0.
				SeqNewickNode node = new SeqNewickNode(pair.getKey(), pair.getValue().getCounter());
				setTermNewickNode(node.getValue(), node.getCounter()); //new Newick terminal node
				rootNode.addNode(pair.getKey(), node);
			} else {
					if (pair.getValue().getNodeHash().size() == 1) {
							SeqNewickNode node = new SeqNewickNode(pair.getKey(), pair.getValue().getCounter());
							
							// add child node to Newick format
							addLeadingParenthesisNewick(); 
							
							SeqNewickNode childNode;
							
							if (it.hasNext()) {
								childNode = deepNewickIteration(false, pair.getValue(), node);
							} else {
								childNode = deepNewickIteration(true, pair.getValue(), node);
							}
							
							// add child node to Newick format
							setConcatNewickNode(childNode.getValue(), childNode.getCounter());
							
							//get node directly beneath childnode
							rootNode.addNode(childNode.getValue(), childNode); 
							
					} else if(pair.getValue().getNodeHash().size() > 1) {
							Iterator<Entry<String, SeqReducedTrieNode>> subIt = pair.getValue().getNodeHash().entrySet().iterator();
							
							SeqNewickNode node = new SeqNewickNode(pair.getKey(), pair.getValue().getCounter());
							
							//add node to Newick format
							addLeadingParenthesisNewick(); 
							
							while (subIt.hasNext()) {
								HashMap.Entry<String, SeqReducedTrieNode> subPair = (HashMap.Entry<String, SeqReducedTrieNode>)subIt.next();
								SeqNewickNode subNode = new SeqNewickNode(subPair.getKey(), subPair.getValue().getCounter());
								
								boolean lastTerm; 
								
								if(subIt.hasNext()) {
									lastTerm = false;
								} else {
									lastTerm = true;
								}
							
								// add child node to Newick format
								addLeadingParenthesisNewick(); 
								
								// in case the child node does not have another child remove the first parenthesis
								if (subPair.getValue().getNodeHash().isEmpty()) {
									removeLeadingParenthesisNewick();
								}
								
								SeqNewickNode childNode = deepNewickIteration(lastTerm, subPair.getValue(), subNode);						
															
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
	
	private SeqNewickNode deepNewickIteration(boolean term, SeqReducedTrieNode Node, SeqNewickNode propNode) {
		boolean lastTerm = term;
		
		SeqReducedTrieNode currentNode = Node;
		
		SeqNewickNode currPropNode = new SeqNewickNode(currentNode.getValue(), currentNode.getCounter());
		
		// reaching a terminal node adds the sequence to the previous node
		if (currentNode.getNodeHash().isEmpty()) {
			if (lastTerm) {
				setLastTermNewickNode(currPropNode.getValue(), currPropNode.getCounter()); //new Newick terminal node
			} else {
				setTermNewickNode(currPropNode.getValue(), currPropNode.getCounter()); //new Newick terminal node
			}
			return currPropNode;
		} else {
			Iterator<Entry<String, SeqReducedTrieNode>> deepIt = currentNode.getNodeHash().entrySet().iterator();
			while (deepIt.hasNext()) {
				HashMap.Entry<String, SeqReducedTrieNode> deepPair = (HashMap.Entry<String, SeqReducedTrieNode>)deepIt.next();
				
				if (deepIt.hasNext()) {
					lastTerm = false;
				} else {
					lastTerm = true;
				}
				
				if(deepPair.getValue().getNodeHash().size() == 0) {
					
					SeqNewickNode newPropNode = new SeqNewickNode(deepPair.getKey(),deepPair.getValue().getCounter());				
					
					// new Newick terminal node
					if (lastTerm) {
						setLastTermNewickNode(newPropNode.getValue(), newPropNode.getCounter());
					} else {
						setTermNewickNode(newPropNode.getValue(), newPropNode.getCounter());
					}
					currPropNode.addNode(newPropNode.getValue(), newPropNode);
					
				} 
				
				 //if child has one grand child
				 else if(deepPair.getValue().getNodeHash().size() == 1) { 
					
					//add a leading Parenthesis to the Newick output
					addLeadingParenthesisNewick(); 
					
					//Let's continue with the child
					SeqNewickNode newPropNode = deepNewickIteration(lastTerm, deepPair.getValue(), currPropNode); 
					
					//write content of inner node
					setInnerNewickNode(newPropNode.getValue(), newPropNode.getCounter());
					
					//add new node to current node
					currPropNode.addNode(newPropNode.getValue(), newPropNode);
					
				} 
				  
				  // if child has several grand children
				  else if (deepPair.getValue().getNodeHash().size() > 1) { 
					
					addLeadingParenthesisNewick(); //add a leading Parenthesis to the Newick output
					SeqNewickNode newNode = deepNewickIteration(lastTerm, deepPair.getValue(),currPropNode);
					setInnerNewickNode(newNode.getValue(), newNode.getCounter()); //write content of inner node
					
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
