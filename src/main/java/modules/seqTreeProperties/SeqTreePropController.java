package modules.seqTreeProperties;

import java.util.Properties;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.PipedReader;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.seqSuffixTrie2SuffixTree.SeqReducedTrieNode;

import com.google.gson.Gson;

import common.parallelization.CallbackReceiver;

//TODO: instead of different node classes I need to create different node classes which implement the interface "common.TreeNode"

/**
 * Reads trees from I/O pipe via JSON format.
 * JSON format must represent reduced suffix tries or trees.
 * Extracts tree properties such as 
 * 	- height of the tree (h = length of the longest path)
 * 	- average length of all paths
 * 	- ratio of subtree sites 
 * 	- average of subtree sites
 * 	- Sackin index and other tree balance measures for non-binary trees
 * 
 * Writes output in form of a string into the char output pipe.
 * 
 * @author Christopher Kraus
 * 
 */

public class SeqTreePropController extends ModuleImpl {
		
	//property keys:
		/*no property keys*/
	//end keys

	//variables:
	
	//HashMap to save the properties of each inner node of the tree 
	private HashMap<String, SeqProperties> seqProperties;
	
	//sequence properties output string
	private String seqPropertiesOutput;
	
	//sorted ArrayList of properties for each node by path length
	private ArrayList<SeqProperties> seqPropertiesSorted;
	
	private ArrayList<Integer> averagePathLength;
	private double avPathLen;
	
	private ArrayList<Double> averagePathRatio;
	private double avPathRatio; 
	
	//longestPath = height of the tree
	private int longestPath;
	
	//variables for calculating the Sackin index
	private int totalNumOfLeaves;
	private ArrayList<SeqSackinIndex> sackinIndex;
	
	//sackin index
	private double sackinIndexVal;
	
	//sackin variance
	private double sackinVar;
	
	//normalized Sackin index
	private double sackinIndexNorm;
	
	private SeqReducedTrieNode mainNode;
	private Gson gson;
	private SeqPropertyNode rootNode;
	private final String INPUTID = "input";
	private final String OUTPUTID = "output";
	//private HashMap<String, SeqPropertyNode> seqNodes;
	//end variables
	
	//constructors:
	public SeqTreePropController(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// Add property descriptions
			/*no property keys */
		
		// Add property defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Sequence Tree Properties");
		
		
		// set up newickOutput
		this.seqPropertiesOutput = new String();
		
		// Define I/O
		InputPort inputPort = new InputPort(INPUTID, "TODO.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUTID, "TODO.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		
	}
	//end constructors
	
	//setters:
		
	public void setGson(PipedReader reader) {
		gson = new Gson();
		mainNode = gson.fromJson(reader, SeqReducedTrieNode.class);
	}
			
	//end setters
	
	//getters:
			
	public String getSeqProperties() {
		return seqPropertiesOutput;
	}
	
	//end getters
	
	@Override
	public boolean process() throws Exception {
		
		//create mainNode by reading JSON input
		this.setGson(this.getInputPorts().get(INPUTID).getInputReader());
					
		//iterate over the tree and get parameters
		this.displayAllTreeProperties();
		
		//write the tree properties into the output
		this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(this.getSeqProperties());
								
		// Close outputs (important!)
		this.closeAllOutputs();
		
		// Done
		return true;
	}
	
	@Override
	public void applyProperties() throws Exception {
		
		// Apply own properties
			/*no property keys*/
		// Apply parent object's properties
		super.applyProperties();
	}
		
	//display all tree properties
	public void displayAllTreeProperties () {
		iteratePropMainNode();
		sortByPathLength();
		calculateProps();
		seqPropertiesOutput = "Longest Path for inner nodes:\t" + longestPath + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Longest Path (for leaves):\t" + (longestPath + 1) + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Average length of paths:\t" + avPathLen + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Average ratio of paths:\t" + avPathRatio + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Total number of leaves:\t" + totalNumOfLeaves + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Sackin index:\t" + sackinIndexVal + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Sackin variance:\t" + sackinVar + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Normalized Sackin index:\t" + sackinIndexNorm + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Sequence\tpath length\tpath ratio\n";
		for (SeqProperties i : seqPropertiesSorted) {
			seqPropertiesOutput = seqPropertiesOutput + i.getNodeName() + "\t" + i.getPathLength() + "\t" + i.getPathRatio() + "\n";
		}
	}
	
	//identification of all properties of the tree
	public void iteratePropMainNode() { 

		// instantiate new root node
		rootNode = new SeqPropertyNode("^", mainNode.getCounter());  
		
		//number of the current leaf of the root node
		String innerNodeName = rootNode.getValue();
		
		//define the properties of the root node
		seqProperties = new HashMap<String, SeqProperties>();
		seqProperties.put(innerNodeName, new SeqProperties(innerNodeName, rootNode.getValue(), 0));
		
		//instantiate new sackin index
		sackinIndex = new ArrayList<SeqSackinIndex> ();
		
		Iterator<Entry<String, SeqReducedTrieNode>> it = mainNode.getNodeHash().entrySet().iterator();
		
		while (it.hasNext()) {
			HashMap.Entry<String, SeqReducedTrieNode> pair = (HashMap.Entry<String, SeqReducedTrieNode>)it.next();
			
			if(pair.getValue().getNodeHash().isEmpty()) {
				
				//end node on first level reached. Create terminal node.
				SeqPropertyNode node = new SeqPropertyNode(pair.getKey(), pair.getValue().getCounter());
				rootNode.addNode(pair.getKey(), node);
				
				//add a leaf to the Sackin index
				totalNumOfLeaves ++;
				SeqSackinIndex index = new SeqSackinIndex(node.getValue(), 1);
				index.catSequence(rootNode.getValue());
				sackinIndex.add(index);

			} else {
				
				if (pair.getValue().getNodeHash().size() == 1) {
						SeqPropertyNode node = new SeqPropertyNode(pair.getKey(), pair.getValue().getCounter());
						
						SeqPropertyNode childNode = deepPropIteration(pair.getValue(), node, innerNodeName);
											
						//get node directly beneath child node
						rootNode.addNode(childNode.getValue(), childNode); 
						
				} else if(pair.getValue().getNodeHash().size() > 1) {
					
					//increase the "name" for each iteration
					String pairNodeName = innerNodeName + pair.getValue().getValue();
					
					Iterator<Entry<String, SeqReducedTrieNode>> subIt = pair.getValue().getNodeHash().entrySet().iterator();
					
					SeqPropertyNode node = new SeqPropertyNode(pair.getKey(), pair.getValue().getCounter());
					
					//create properties of inner node
					seqProperties.put(pairNodeName, new SeqProperties(pairNodeName, node.getValue(), seqProperties.get("^").getPathLength() + 1, (((double)node.getCounter())/((double)rootNode.getCounter()))));
					
					while (subIt.hasNext()) {
						
						HashMap.Entry<String, SeqReducedTrieNode> subPair = (HashMap.Entry<String, SeqReducedTrieNode>)subIt.next();
						SeqPropertyNode subNode = new SeqPropertyNode(subPair.getKey(), subPair.getValue().getCounter());
						
						//increase the "name" for each iteration
						String subPairNodeName = pairNodeName;
						//create properties for an inner node
						if (subPair.getValue().getNodeHash().size() > 1 ) {
							subPairNodeName += subPair.getValue().getValue();
							seqProperties.put(subPairNodeName, new SeqProperties(subPairNodeName, subNode.getValue(), seqProperties.get(pairNodeName).getPathLength() + 1, (((double)subNode.getCounter())/((double)node.getCounter()))));
						}
						
						SeqPropertyNode childNode = deepPropIteration(subPair.getValue(), subNode, subPairNodeName);
						
						
						
						node.addNode(childNode.getValue(), childNode);
						subIt.remove(); // avoids a ConcurrentModificationException
					}
					
					rootNode.addNode(node.getValue(), node);
					
				}
			}
			
		    it.remove(); // avoids a ConcurrentModificationException 
		}
	}
	
	private SeqPropertyNode deepPropIteration(SeqReducedTrieNode Node, SeqPropertyNode propNode, String propNodeName) {
		
		SeqReducedTrieNode currentNode = Node;
		SeqPropertyNode currPropNode = new SeqPropertyNode(currentNode.getValue(), currentNode.getCounter());
		String lastPropNodeName = propNodeName;
			
		// reaching a terminal node adds the sequence to the previous node
		if (currentNode.getNodeHash().isEmpty()) {
			
			//add a leaf to the Sackin index
			totalNumOfLeaves ++;
		
			//add new leaf to calculate the Sackin index
			SeqSackinIndex index = new SeqSackinIndex(currPropNode.getValue(), seqProperties.get(lastPropNodeName).getPathLength());
			sackinIndex.add(index);
		
			return currPropNode;
		
		} else {
			Iterator<Entry<String, SeqReducedTrieNode>> deepIt = currentNode.getNodeHash().entrySet().iterator();
			while (deepIt.hasNext()) {
				HashMap.Entry<String, SeqReducedTrieNode> deepPair = (HashMap.Entry<String, SeqReducedTrieNode>)deepIt.next();
				
				if(deepPair.getValue().getNodeHash().size() == 0) {
					SeqPropertyNode newPropNode = new SeqPropertyNode(deepPair.getKey(),deepPair.getValue().getCounter());				
					
					//add a leaf to the Sackin index
					totalNumOfLeaves ++;
					
					SeqSackinIndex index = new SeqSackinIndex(currPropNode.getValue(), seqProperties.get(lastPropNodeName).getPathLength());
					sackinIndex.add(index);
					
					currPropNode = newPropNode;
					
				} else if(deepPair.getValue().getNodeHash().size() == 1) { //if child has one grand child
					
					Object[] key = deepPair.getValue().getNodeHash().keySet().toArray(); //What is the name of the grand child?
					
					//Let's continue with the grand child
					SeqPropertyNode newPropNode = deepPropIteration(deepPair.getValue().getNodeHash().get(key[0]),currPropNode, lastPropNodeName); 
										
					if(newPropNode.getNodeHash().size() == 1) { //What the grand child also has only one child?
						Object[] newChild = newPropNode.getNodeHash().keySet().toArray();//What is the name of this new child?
						currPropNode.concatValue(newPropNode.getValue()); //remember the name of the grand child
						currPropNode.addNode(newPropNode.getNodeHash().get(newChild[0]).getValue(), newPropNode.getNodeHash().get(newChild[0]));//continue with this new child
					} else { // so the grand child several or no children, let's remember that
						currPropNode.addNode(newPropNode.getValue(), newPropNode); 
					}
				} else if (deepPair.getValue().getNodeHash().size() > 1) { // if there are more nodes remember the zaehler value
									
					SeqPropertyNode newNode = deepPropIteration(deepPair.getValue(),currPropNode, lastPropNodeName);
					
					//increase the "name" for each iteration
					String innerNodeName = lastPropNodeName + currPropNode.getValue();
					
					//create properties of inner node
					seqProperties.put(innerNodeName, new SeqProperties(innerNodeName, newNode.getValue(), seqProperties.get(lastPropNodeName).getPathLength() + 1, (((double)newNode.getCounter())/((double)currPropNode.getCounter()))));
					//seqProperties.get(innerNodeName).catSequence(currPropNode.getValue());
					
					currPropNode.addNode(newNode.getValue(), newNode);
					
				}
			}
			deepIt.remove(); // avoids a ConcurrentModificationException
			return currPropNode;
		}
	}
	
	//sort the different paths through the tree by their length
	public void sortByPathLength() {
		
		//initiate the sorted ArrayList
		seqPropertiesSorted = new ArrayList<SeqProperties>();
			
		Iterator<Entry<String, SeqProperties>> iter = seqProperties.entrySet().iterator();
		
		//remember the length of the last path
		int lastLength = seqProperties.get("^").getPathLength();
		
		//instantiate variables to remember information about path length and path ratio
		averagePathLength = new ArrayList<Integer>();
		averagePathRatio = new ArrayList<Double>();
		
		while(iter.hasNext()) {
			
			HashMap.Entry<String, SeqProperties> pair = (HashMap.Entry<String, SeqProperties>)iter.next();
			
			//get information to calculate the average path length and the average path ratio
			averagePathLength.add(pair.getValue().getPathLength());
			averagePathRatio.add(pair.getValue().getPathRatio());
			
			if (pair.getValue().getPathLength() >= lastLength) {
				seqPropertiesSorted.add(0, pair.getValue()); //put it to the top
				lastLength = pair.getValue().getPathLength();
			} else {
				//iterate over the ArrayList seqPropertiesSorted and add this sequence
				for(SeqProperties i : seqPropertiesSorted) {

					if (i.getPathLength() <= pair.getValue().getPathLength()) {
						seqPropertiesSorted.add(seqPropertiesSorted.indexOf(i), pair.getValue());
						break;
					}
					
					// if this is the smallest value add it to the end of the list
					if (seqPropertiesSorted.indexOf(i) == seqPropertiesSorted.size() -1 ) { 
						seqPropertiesSorted.add(pair.getValue());
						break;
					}
				}
			}
		}
		longestPath = lastLength;
	}
	
	//calculate properties of the tree
	public void calculateProps() {

		int totalPathLen = 0;
		
		for (Integer i : averagePathLength) {
			totalPathLen = totalPathLen + i;  
		}
		
		avPathLen = (double) totalPathLen / (double) averagePathLength.size();
		
		double totalPathRatio = 0;
		
		for (Double i : averagePathRatio) {
			totalPathRatio = totalPathRatio + i; 
		}
		
		avPathRatio = (double) totalPathRatio / (double) averagePathRatio.size();
		
		//calculate Sackin index
		int counter = 1;
		double sackinSum = 0; 
		int sackinIndexReady = 0;
		for (SeqSackinIndex i : sackinIndex) {
			sackinIndexReady = sackinIndexReady + i.getNodeNumber();
			if (counter > 1) {
				sackinSum = sackinSum + 1/( (double) counter);
			}
			counter ++;
		}

		sackinIndexVal = (double) sackinIndexReady;
		
		//calculate Sackin variance
		double sackinVariance = 2 * totalNumOfLeaves * sackinSum;
		sackinVar = sackinVariance;
		
		//calculate normalized Sackin index
		sackinIndexNorm = (sackinIndexReady - sackinVariance) / totalNumOfLeaves;
	}
}
