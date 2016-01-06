package modules.treeBuilder2Output;

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
import modules.seqSuffixTrie2SuffixTree.SeqReducedTrieNode;
import modules.treeBuilder.Knoten;
import common.parallelization.CallbackReceiver;

public class TreeBuilder2OutputController extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
		/* no property keys */
		
	//variables:

	private final String INPUTID = "input";
	private final String OUTPUTID = "output";
	private Knoten mainNode;
	private Gson gson;
	private SeqReducedTrieNode rootNode;
	
	//end variables
	
	//constructors:
	public TreeBuilder2OutputController (CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add property descriptions (obligatory for every property!)
		//this.getPropertyDescriptions().put(PROPERTYKEY_REGEX, "Regular expression to search for");
		//this.getPropertyDescriptions().put(PROPERTYKEY_REPLACEMENT, "Replacement for found strings");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "TreeBuilder2Output"); // Property key for module name is defined in parent class
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
		// Add module category
		this.setCategory("Format conversion");
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
															
							SeqReducedTrieNode childNode = deepIteration(pair.getValue(), node);
							
							//get node directly beneath child node
							rootNode.addNode(childNode.getValue(), childNode); 
							
					} else if(pair.getValue().getKinder().size() > 1) {
							Iterator<Entry<String, Knoten>> subIt = pair.getValue().getKinder().entrySet().iterator();
							
							SeqReducedTrieNode node = new SeqReducedTrieNode(pair.getKey(), pair.getValue().getZaehler());
							
							while (subIt.hasNext()) {
								HashMap.Entry<String, Knoten> subPair = (HashMap.Entry<String, Knoten>)subIt.next();
								SeqReducedTrieNode subNode = new SeqReducedTrieNode(subPair.getKey(), subPair.getValue().getZaehler());
								SeqReducedTrieNode childNode;
								if (subPair.getValue().getKinder().size() == 1) {
								childNode = deepIteration(subPair.getValue(), subNode);
								} else {
								childNode = deepIteration(subPair.getValue(), subNode);
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
	
	private SeqReducedTrieNode deepIteration(Knoten Node, SeqReducedTrieNode propNode) {
		
		Knoten currentNode = Node;
		
		SeqReducedTrieNode currPropNode = new SeqReducedTrieNode(currentNode.getName(), currentNode.getZaehler());
		
		// reaching a terminal node
		if (currentNode.getKinder().isEmpty()) {
			
			return currPropNode;
			
		} else {
			Iterator<Entry<String, Knoten>> deepIt = currentNode.getKinder().entrySet().iterator();
			while (deepIt.hasNext()) {
				HashMap.Entry<String, Knoten> deepPair = (HashMap.Entry<String, Knoten>)deepIt.next();
				
				if(deepPair.getValue().getKinder().size() == 0) {
					SeqReducedTrieNode newPropNode = new SeqReducedTrieNode(deepPair.getKey(),deepPair.getValue().getZaehler());				
					
					currPropNode.addNode(newPropNode.getValue(), newPropNode);
					
					
				} else if(deepPair.getValue().getKinder().size() == 1) { //if child has one grand child
										
					// What is the name of the grand child?
					Object[] key = deepPair.getValue().getKinder().keySet().toArray(); 
							
					SeqReducedTrieNode deepPairNode = new SeqReducedTrieNode(deepPair.getKey(),deepPair.getValue().getZaehler());	
					
					//Let's continue with the grand child
					SeqReducedTrieNode newPropNode = deepIteration(deepPair.getValue().getKinder().get(key[0]),deepPairNode); 
								
					currPropNode.addNode(newPropNode.getValue(), newPropNode);
					
					
				// if there are more nodes remember the zaehler value is > 1
				} else if (deepPair.getValue().getKinder().size() > 1) { 
					
					SeqReducedTrieNode newNode = deepIteration(deepPair.getValue(),currPropNode);
					currPropNode.addNode(newNode.getValue(), newNode);
				}
			}
			deepIt.remove(); // avoids a ConcurrentModificationException
			return currPropNode;
		}
	}
}
