package modules.format_conversion.treeBuilder2Output;

import java.io.PipedReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.Pipe;
import modules.format_conversion.seqNewickExporter.SeqNewickNodeV2;
import modules.tree_editing.seqSuffixTrie2SuffixTree.SeqReducedTrieNode;
import common.parallelization.CallbackReceiver;

public class TreeBuilder2OutputControllerV2 extends ModuleImpl {
	// Define property keys (every setting has to have a unique key to associate it with)
		/* no property keys */
		
	//variables:

	private final String INPUTID = "input";
	private final String OUTPUTID = "output";
	private SeqNewickNodeV2 mainNode;
	private Gson gson;
	private SeqReducedTrieNode rootNode;
	
	//end variables
	
	//constructors:
	public TreeBuilder2OutputControllerV2 (CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add property descriptions (obligatory for every property!)
		//this.getPropertyDescriptions().put(PROPERTYKEY_REGEX, "Regular expression to search for");
		//this.getPropertyDescriptions().put(PROPERTYKEY_REPLACEMENT, "Replacement for found strings");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "TreeBuilder2OutputV2"); // Property key for module name is defined in parent class
		//this.getPropertyDefaultValues().put(PROPERTYKEY_REGEX, "[aeiu]");
		//this.getPropertyDefaultValues().put(PROPERTYKEY_REPLACEMENT, "o");
		
		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~).
		 * Every port can support a range of pipe types (currently
		 * byte or character pipes). Output ports can provide data
		 * to multiple pipe instances at once, input ports can
		 * in contrast only obtain data from one pipe instance.
		 */
		InputPort inputPort = new InputPort(INPUTID, "JSON-encoded suffix tree.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		
		OutputPort outputPort = new OutputPort(OUTPUTID, "JSON-encoded suffix tree.", this);
		outputPort.addSupportedPipe(CharPipe.class);
				
		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
	
		super.addOutputPort(outputPort);
		
		// Add module description
		this.setDescription("This module converts the treeBuilder output so that it can be read buy other" 
				+ "downstream modules such as seqNewickExporter");
	}
	//end constructors
	
	//setters:
	
	public void setGson(PipedReader reader) {
		gson = new Gson();
		mainNode = gson.fromJson(reader, SeqNewickNodeV2.class);
	}
	
	//end setters
	
	//getters:
	
	//end getters
	@Override
	public boolean process() throws Exception {
		
		//create mainNode by reading JSON input
		this.setGson(this.getInputPorts().get(INPUTID).getInputReader());
					
		//iterate over the tree and get parameters
		this.iterateMainNode();
			
		// write JSON to output
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Iterator<Pipe> charPipes = this.getOutputPorts().get(OUTPUTID).getPipes(CharPipe.class).iterator();
		while (charPipes.hasNext()){
			gson.toJson(rootNode, ((CharPipe)charPipes.next()).getOutput());
		}
							
		// Close outputs (important!)
		this.closeAllOutputs();
		
		// Done
		return true;
	}
	
	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		
		// Apply own properties
			/*no properties, no property keys! The output and the processing is not to be changed by the user.*/
		// Apply parent object's properties
		super.applyProperties();
	}
	
	public void iterateMainNode() {
		
		// instantiate new root node
		rootNode = new SeqReducedTrieNode("^", mainNode.getCounter()); 
					
		Iterator<Entry<String, SeqNewickNodeV2>> it = mainNode.getChildNodes().entrySet().iterator();
		
		while (it.hasNext()) {
			HashMap.Entry<String, SeqNewickNodeV2> pair = (HashMap.Entry<String, SeqNewickNodeV2>)it.next();
			
			if(pair.getValue().getChildNodes().isEmpty()) {
				//end node on first level reached. Create terminal node at tree height 0.
				SeqReducedTrieNode node = new SeqReducedTrieNode(pair.getKey(), pair.getValue().getCounter());
				rootNode.addNode(pair.getKey(), node);
			} else {
					if (pair.getValue().getChildNodes().size() == 1) {
							SeqReducedTrieNode node = new SeqReducedTrieNode(pair.getKey(), pair.getValue().getCounter());
															
							SeqReducedTrieNode childNode = deepIteration(pair.getValue(), pair.getKey(), node);
							
							//get node directly beneath child node
							rootNode.addNode(childNode.getValue(), childNode); 
							
					} else if(pair.getValue().getChildNodes().size() > 1) {
							Iterator<Entry<String, SeqNewickNodeV2>> subIt = pair.getValue().getChildNodes().entrySet().iterator();
							
							SeqReducedTrieNode node = new SeqReducedTrieNode(pair.getKey(), pair.getValue().getCounter());
							
							while (subIt.hasNext()) {
								HashMap.Entry<String, SeqNewickNodeV2> subPair = (HashMap.Entry<String, SeqNewickNodeV2>)subIt.next();
								SeqReducedTrieNode subNode = new SeqReducedTrieNode(subPair.getKey(), subPair.getValue().getCounter());
								SeqReducedTrieNode childNode;
								if (subPair.getValue().getChildNodes().size() == 1) {
								childNode = deepIteration(subPair.getValue(), subPair.getKey(), subNode);
								} else {
								childNode = deepIteration(subPair.getValue(), subPair.getKey(), subNode);
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
	
	private SeqReducedTrieNode deepIteration(SeqNewickNodeV2 Node, String nodeKey, SeqReducedTrieNode propNode) {
		
		SeqNewickNodeV2 currentNode = Node;
		
		String currNodeValue = nodeKey;
		
		SeqReducedTrieNode currPropNode = new SeqReducedTrieNode(currNodeValue, currentNode.getCounter());
		
		// reaching a terminal node
		if (currentNode.getChildNodes().isEmpty()) {
			
			return currPropNode;
			
		} else {
			Iterator<Entry<String, SeqNewickNodeV2>> deepIt = currentNode.getChildNodes().entrySet().iterator();
			while (deepIt.hasNext()) {
				HashMap.Entry<String, SeqNewickNodeV2> deepPair = (HashMap.Entry<String, SeqNewickNodeV2>)deepIt.next();
				
				if(deepPair.getValue().getChildNodes().size() == 0) {
					SeqReducedTrieNode newPropNode = new SeqReducedTrieNode(deepPair.getKey(),deepPair.getValue().getCounter());				
					
					currPropNode.addNode(newPropNode.getValue(), newPropNode);
					
					
				} else if(deepPair.getValue().getChildNodes().size() == 1) { //if child has one grand child
										
					// What is the name of the grand child?
					Object[] key = deepPair.getValue().getChildNodes().keySet().toArray(); 
							
					SeqReducedTrieNode deepPairNode = new SeqReducedTrieNode(deepPair.getKey(),deepPair.getValue().getCounter());	
					
					//Let's continue with the grand child
					SeqReducedTrieNode newPropNode = deepIteration(deepPair.getValue().getChildNodes().get(key[0]), key[0].toString(), deepPairNode); 
								
					currPropNode.addNode(newPropNode.getValue(), newPropNode);
					
					
				// if there are more nodes remember the zaehler value is > 1
				} else if (deepPair.getValue().getChildNodes().size() > 1) { 
					
					SeqReducedTrieNode newNode = deepIteration(deepPair.getValue(), deepPair.getKey(), currPropNode);
					currPropNode.addNode(newNode.getValue(), newNode);
				}
			}
			deepIt.remove(); // avoids a ConcurrentModificationException
			return currPropNode;
		}
	}

}
