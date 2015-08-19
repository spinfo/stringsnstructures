package seqTreeProperties;

import java.util.Properties;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import treeBuilder.Knoten; //necessary to read the objects form JSON
import modularization.CharPipe;
import java.io.PipedReader;
import modularization.ModuleImpl;
import parallelization.CallbackReceiver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Reads trees from I/O pipe via JSON format.
 * Extracts tree properties such as 
 * 	- height of the tree (h = length of the longest path)
 * 	- average length of all paths
 * 	- ratio of subtree sites 
 * 	- average of subtree sites
 * 	- Sackin index and other tree balance measures for non-binary trees
 * 
 * @author Christopher Kraus
 * 
 */

public class SeqTreePropController extends ModuleImpl {
		
		//property keys:
		//public static final String PROPERTYKEY_PROP = "PROP";
		//end keys
	
		//variables:
		/*private int treeHeight;
		private double ratioSubtree;
		private double averageSubtree;
		private int sackinIndex;*/
		
		private String json = "";
		private Knoten mainNode;
		private Gson gson;
		private SeqPropertyNode rootNode;
		//private HashMap<String, SeqPropertyNode> seqNodes;
		//end variables
		
		//constructors:
		public SeqTreePropController(CallbackReceiver callbackReceiver,
				Properties properties) throws Exception {
			super(callbackReceiver, properties);

			// Add property descriptions
			//this.getPropertyDescriptions().put(PROPERTYKEY_REGEX, "Regular expression to search for");
			
			// Add property defaults
			this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Sequence Tree Properties");
			//this.getPropertyDefaultValues().put(PROPERTYKEY_REGEX, "[aeiu]");
			
			// Define I/O
			this.getSupportedInputs().add(CharPipe.class);
			this.getSupportedOutputs().add(CharPipe.class);
			
		}
		//end constructors
		
		//setters:
		public void addJson(String str) {
			json += str;
		}
		
		public void setGson(PipedReader reader) {
			gson = new Gson();
			mainNode = gson.fromJson(reader, Knoten.class);
		}
		//end setters
		
		//getters:
		public String getJson() {
			return json;
		}
		//end getters
		
		@Override
		public boolean process() throws Exception {
			
			//create mainNode by reading JSON input
			this.setGson(this.getInputCharPipe().getInput());
						
			//iterate over the tree and get paraStringmeters
			this.iterateMainNode();
			
			
			// Write to outputs
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
			/*if (this.getProperties().containsKey(PROPERTYKEY_REGEX))
				this.regex = this.getProperties().getProperty(PROPERTYKEY_REGEX, this.getPropertyDefaultValues().get(PROPERTYKEY_REGEX));
			if (this.getProperties().containsKey(PROPERTYKEY_REPLACEMENT))
				this.replacement = this.getProperties().getProperty(PROPERTYKEY_REPLACEMENT, this.getPropertyDefaultValues().get(PROPERTYKEY_REPLACEMENT));
			*/
			// Apply parent object's properties
			super.applyProperties();
		}
		
		public void iterateMainNode() {
			
			//set root for SeqPropertyNode seqNodes
			
			rootNode = new SeqPropertyNode("^", 1);
			//seqNodes = new HashMap<String, SeqPropertyNode> ();
			//seqNodes.put("^", rootNode);
			
			Iterator<Entry<String, Knoten>> it = mainNode.getKinder().entrySet().iterator();
			
			while (it.hasNext()) {
				HashMap.Entry<String, Knoten> pair = (HashMap.Entry<String, Knoten>)it.next();
				//endNode.add(deepIteration(pair.getValue()));
				if(pair.getValue().getKinder().isEmpty()) {
					//end node on first level reached. Create terminal node at tree height 0.
					SeqPropertyNode node = new SeqPropertyNode(pair.getKey(), pair.getValue().getZaehler());
					rootNode.addNode(pair.getKey(), node);
				} else {
						if (pair.getValue().getKinder().size() == 1) {
								SeqPropertyNode node = new SeqPropertyNode(pair.getKey(), pair.getValue().getZaehler());
								SeqPropertyNode childNode = deepIteration(pair.getValue(), node);
								//node.addNode(childNode.getValue(), childNode);
								rootNode.addNode(node.getValue(), childNode.getNodeHash().get(node.getValue())); //get node directly beneath childnode
						} else if(pair.getValue().getKinder().size() > 1) {
								Iterator<Entry<String, Knoten>> subIt = pair.getValue().getKinder().entrySet().iterator();
								SeqPropertyNode node = new SeqPropertyNode(pair.getKey(), pair.getValue().getZaehler());
								while (subIt.hasNext()) {
									HashMap.Entry<String, Knoten> subPair = (HashMap.Entry<String, Knoten>)subIt.next();
									SeqPropertyNode subNode = new SeqPropertyNode(subPair.getKey(), subPair.getValue().getZaehler());
									SeqPropertyNode childNode = deepIteration(subPair.getValue(), subNode);
									//subNode.addNode(childNode.getValue(), childNode);
									node.addNode(childNode.getValue(), childNode);
									subIt.remove(); // avoids a ConcurrentModificationException
								}
								rootNode.addNode(node.getValue(), node);
						}
				}
			    it.remove(); // avoids a ConcurrentModificationException
			}
		}
		
		private SeqPropertyNode deepIteration(Knoten Node, SeqPropertyNode propNode) {
			Knoten currentNode = Node;
			SeqPropertyNode lastPropNode = propNode;
			SeqPropertyNode currPropNode = new SeqPropertyNode(currentNode.getName(), currentNode.getZaehler());
			// reaching a terminal node adds the sequence to the previous node
			if (currentNode.getKinder().isEmpty()) {
				return lastPropNode;
			} else {
				Iterator<Entry<String, Knoten>> deepIt = currentNode.getKinder().entrySet().iterator();
				while (deepIt.hasNext()) {
					HashMap.Entry<String, Knoten> deepPair = (HashMap.Entry<String, Knoten>)deepIt.next();
					//SeqPropertyNode currPropNode = new SeqPropertyNode(deepPair.getKey(), deepPair.getValue().getZaehler());
					if(deepPair.getValue().getKinder().size() == 0) {
						SeqPropertyNode newPropNode = new SeqPropertyNode(deepPair.getKey(),deepPair.getValue().getZaehler());
						currPropNode.addNode(newPropNode.getValue(), newPropNode);
						//return currPropNode;
						//lastPropNode.addNode(currPropNode.getValue(), currPropNode);
					} else if(deepPair.getValue().getKinder().size() == 1) { //if there is only one node beneath then merge with previous
						SeqPropertyNode newPropNode = deepIteration(deepPair.getValue(),currPropNode);
						//TODO: here is the problem I cannot merge nodes yet, as beneath layers are invisible to me
						//currPropNode.concatValue(newPropNode.getValue());
						lastPropNode.addNode(newPropNode.getValue(), newPropNode);
						//return lastPropNode;
					} else if (deepPair.getValue().getKinder().size() > 1) { // if there are more nodes remember the zaehler value
						SeqPropertyNode newNode = deepIteration(deepPair.getValue(),currPropNode);
						currPropNode.addNode(newNode.getValue(), newNode);
					}
				}
				deepIt.remove(); // avoids a ConcurrentModificationException
				if ((currPropNode.getNodeHash().size() == 1) || (currPropNode.getNodeHash().size() == 0)) {
					lastPropNode.addNode(currPropNode.getValue(), currPropNode);
					return lastPropNode;
				} else {
					return currPropNode;
				}
			}
		}
}
