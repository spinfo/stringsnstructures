package seqSuffixTrie2SuffixTree;

import java.util.Properties;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Iterator;

import treeBuilder.Knoten; //necessary to read the objects form JSON
import modularization.CharPipe;
import java.io.PipedReader;
import modularization.ModuleImpl;
import parallelization.CallbackReceiver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This module reads JSON from an input pipe.
 * This module reduces suffix tries which then appear afterwards as suffix trees.
 * The output is defined by the class "" (see package seqSuffixTrie2SuffixTree).
 * The output format is JSON.
 * @author Christopher Kraus
 */

public class SeqSuffixTrie2SuffixTreeController extends ModuleImpl {

	//property keys:
	
		/*no property keys! the output is always JSON!*/
	
	//end keys

	//variables:
	
	private Knoten mainNode;
	private Gson gson;
	private SeqReducedTrieNode rootNode;
	//private HashMap<String, SeqReducedTrieNode> seqNodes;
	//end variables
	
	//constructors:
	public SeqSuffixTrie2SuffixTreeController (CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// Add property descriptions
			/*no property keys! the output is always JSON!*/
		
		// Add property defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "seqSuffixTrie2SuffixTree");
			/*no further property keys! the output is always JSON!*/
				
		// Define I/O
		this.getSupportedInputs().add(CharPipe.class);
		this.getSupportedOutputs().add(CharPipe.class);
		
	}
	//end constructors
	
	//setters:
		
	public void setGson(PipedReader reader) {
		gson = new Gson();
		mainNode = gson.fromJson(reader, Knoten.class);
	}
	
	//end setters
	
	//getters:
	
	//end getters
	
	@Override
	public boolean process() throws Exception {
		
		//create mainNode by reading JSON input
		this.setGson(this.getInputCharPipe().getInput());
					
		//iterate over the tree and get parameters
		this.iterateMainNode();
			
		// write JSON to output
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Iterator<CharPipe> charPipes = this.getOutputCharPipes().iterator();
		while (charPipes.hasNext()){
			gson.toJson(rootNode, charPipes.next().getOutput());
		}
							
		// Close outputs (important!)
		this.closeAllOutputs();
		
		// Done
		return true;
	}
	
	@Override
	public void applyProperties() throws Exception {
		
		// Apply own properties
			/*no properties, no property keys! The output and the processing is not to be changed by the user.*/
		// Apply parent object's properties
		super.applyProperties();
	}
	
	public void iterateMainNode() {
		
		// instantiate new root node
		rootNode = new SeqReducedTrieNode("^", mainNode.getZaehler()); 
					
		Iterator<Entry<String, Knoten>> it = mainNode.getKinder().entrySet().iterator();
		
		while (it.hasNext()) {
			HashMap.Entry<String, Knoten> pair = (HashMap.Entry<String, Knoten>)it.next();
			
			if(pair.getValue().getKinder().isEmpty()) {
				//end node on first level reached. Create terminal node at tree height 0.
				SeqReducedTrieNode node = new SeqReducedTrieNode(pair.getKey(), pair.getValue().getZaehler());
				rootNode.addNode(pair.getKey(), node);
			} else {
					if (pair.getValue().getKinder().size() == 1) {
							SeqReducedTrieNode node = new SeqReducedTrieNode(pair.getKey(), pair.getValue().getZaehler());
															
							SeqReducedTrieNode childNode = deepIteration(true, pair.getValue(), node);
							
							//get node directly beneath childnode
							rootNode.addNode(childNode.getValue(), childNode); 
							
					} else if(pair.getValue().getKinder().size() > 1) {
							Iterator<Entry<String, Knoten>> subIt = pair.getValue().getKinder().entrySet().iterator();
							
							SeqReducedTrieNode node = new SeqReducedTrieNode(pair.getKey(), pair.getValue().getZaehler());
							
							while (subIt.hasNext()) {
								HashMap.Entry<String, Knoten> subPair = (HashMap.Entry<String, Knoten>)subIt.next();
								SeqReducedTrieNode subNode = new SeqReducedTrieNode(subPair.getKey(), subPair.getValue().getZaehler());
								SeqReducedTrieNode childNode;
								if (subPair.getValue().getKinder().size() == 1) {
								childNode = deepIteration(true, subPair.getValue(), subNode);
								} else {
								childNode = deepIteration(false, subPair.getValue(), subNode);
								}
								
								node.addNode(childNode.getValue(), childNode);
								subIt.remove(); // avoids a ConcurrentModificationException
							}
							
							rootNode.addNode(node.getValue(), node);
					}
			}
		    it.remove(); // avoids a ConcurrentModificationException
		}
	}
	
	private SeqReducedTrieNode deepIteration(boolean flag, Knoten Node, SeqReducedTrieNode propNode) {
		boolean concatFlag = flag;
		Knoten currentNode = Node;
		SeqReducedTrieNode lastPropNode = propNode;
		SeqReducedTrieNode currPropNode = new SeqReducedTrieNode(currentNode.getName(), currentNode.getZaehler());
		
		// reaching a terminal node adds the sequence to the previous node
		if (currentNode.getKinder().isEmpty()) {
			if (concatFlag) {
				lastPropNode.concatValue(currPropNode.getValue());
				return lastPropNode;
			} else {
				return lastPropNode;
			}
		} else {
			Iterator<Entry<String, Knoten>> deepIt = currentNode.getKinder().entrySet().iterator();
			while (deepIt.hasNext()) {
				HashMap.Entry<String, Knoten> deepPair = (HashMap.Entry<String, Knoten>)deepIt.next();
				
				if(deepPair.getValue().getKinder().size() == 0) {
					SeqReducedTrieNode newPropNode = new SeqReducedTrieNode(deepPair.getKey(),deepPair.getValue().getZaehler());				
					
					if (concatFlag) {
						currPropNode.concatValue(newPropNode.getValue()); //return the modified last node 
					} else {
						currPropNode.addNode(newPropNode.getValue(), newPropNode);
					}
					
					
				} else if(deepPair.getValue().getKinder().size() == 1) { //if child has one grand child
					concatFlag = true;
					
					// What is the name of the grand child?
					Object[] key = deepPair.getValue().getKinder().keySet().toArray(); 
					
					// Is the child of the grand child a terminal node?
					boolean appendNode = false;
					if (deepPair.getValue().getKinder().get(key[0]).getKinder().size() == 1) {
						appendNode = true;
						
						// if there is only one grand child, concatenate its name to the current. However, if there is no grand child skip this step!
						// skipping this step is important to avoid of adding the same character twice if there is a string which can be divided by 2!
						if(currentNode.getKinder().size() == 1) {
							currPropNode.concatValue(deepPair.getValue().getName()); // remember the name of the child
						}
					}
					
					SeqReducedTrieNode deepPairNode = new SeqReducedTrieNode(deepPair.getKey(),deepPair.getValue().getZaehler());	
					
					//Let's continue with the grand child
					SeqReducedTrieNode newPropNode = deepIteration(concatFlag, deepPair.getValue().getKinder().get(key[0]),deepPairNode); 
								
					// Does the grand child have only one child?
					if(newPropNode.getNodeHash().size() == 1) { 
						
						// What is the name of this new child?
						Object[] newChild = newPropNode.getNodeHash().keySet().toArray();
						
						// remember the name of the grand child
						currPropNode.concatValue(newPropNode.getValue()); 
						
						// continue with this new child
						currPropNode.addNode(newPropNode.getNodeHash().get(newChild[0]).getValue(), newPropNode.getNodeHash().get(newChild[0]));
					}
					
					// so the grand child has no children, create a new terminal node
					 else if(newPropNode.getNodeHash().size() == 0) {  
						if (currentNode.getKinder().size() > 1) {
							if (appendNode) { 
								deepPairNode.concatValue(newPropNode.getValue()); 
								currPropNode.addNode(deepPairNode.getValue(), deepPairNode);
							} else {
								currPropNode.addNode(newPropNode.getValue(), newPropNode);
							}
						} else {
							currPropNode.concatValue(newPropNode.getValue());
						}
					 }
					
					// so the grand child has several children, let's remember that
					 else if (newPropNode.getNodeHash().size() > 1) { 
						currPropNode.addNode(newPropNode.getValue(), newPropNode);
					}
					
				// if there are more nodes remember the zaehler value is > 1
				} else if (deepPair.getValue().getKinder().size() > 1) { 
					concatFlag = false;
					SeqReducedTrieNode newNode = deepIteration(concatFlag, deepPair.getValue(),currPropNode);
					currPropNode.addNode(newNode.getValue(), newNode);
				}
			}
			deepIt.remove(); // avoids a ConcurrentModificationException
			return currPropNode;
		}
	}
}
