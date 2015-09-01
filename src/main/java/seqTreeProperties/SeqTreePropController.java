package seqTreeProperties;

import java.util.Properties;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

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
		public static final String PROPERTYKEY_OUTPUT = "Output by Newick or JSON format";
		public static final String PROPERTYKEY_NEWICK = "Newick branch length";
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
		private double sackinIndexNorm; //normalized Sackin index
		
		private String newickOutput;
		private boolean standardOut;
		private String outputForm;
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
			this.getPropertyDescriptions().put(PROPERTYKEY_OUTPUT, "Choose output format by typing: \"Newick\";  \"JSON\"; \"Prop\"");
			this.getPropertyDescriptions().put(PROPERTYKEY_NEWICK, "Choose branch length in Newick: true = by string; false = by node occurence");
			
			// Add property defaults
			this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Sequence Tree Properties");
			this.getPropertyDefaultValues().put(PROPERTYKEY_OUTPUT, "Prop");
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
			mainNode = gson.fromJson(reader, Knoten.class);
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
				String newNode = nodeVal + ":" + Integer.toString(newCount) + ")" + ",";
				newickOutput += newNode;
			} else {
				String newNode = nodeVal +  ":" + Integer.toString(counter) + ")" + ",";
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
		
		public void addLeadingParenthesisNewick() {
			String newParenthesis = "(";
			newickOutput += newParenthesis;
		}
		//end setters
		
		//getters:
		public String getNewick () {
			return newickOutput;
		}
		
		public String getSeqProperties() {
			return seqPropertiesOutput;
		}
		//end getters
		
		@Override
		public boolean process() throws Exception {
			
			//create mainNode by reading JSON input
			this.setGson(this.getInputCharPipe().getInput());
						
			//iterate over the tree and get parameters
			if (outputForm.equals("Newick")) {
				this.iterateNewickMainNode();
			} else if (outputForm.equals("JSON")) {
				this.iterateMainNode();
			} else {
				this.displayAllTreeProperties();
			}
			
			
			if (outputForm.equals("Newick")) { // if 'outputForm' equals 'true' then write Newick, else write JSON
				this.outputToAllCharPipes(this.getNewick()); // write Newick to output
			} else if (outputForm.equals("JSON")){
				// write JSON to output
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				Iterator<CharPipe> charPipes = this.getOutputCharPipes().iterator();
				while (charPipes.hasNext()){
					gson.toJson(rootNode, charPipes.next().getOutput());
				}
			} else {
				this.outputToAllCharPipes(this.getSeqProperties());
			}
						
			// Close outputs (important!)
			this.closeAllOutputs();
			
			// Done
			return true;
		}
		
		@Override
		public void applyProperties() throws Exception {
			
			// Apply own properties
			if (this.getProperties().containsKey(PROPERTYKEY_OUTPUT))
				this.outputForm = this.getProperties().getProperty(PROPERTYKEY_OUTPUT, this.getPropertyDefaultValues().get(PROPERTYKEY_OUTPUT));
			if (this.getProperties().containsKey(PROPERTYKEY_NEWICK))
				this.standardOut = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_NEWICK, this.getPropertyDefaultValues().get(PROPERTYKEY_NEWICK)));
			// Apply parent object's properties
			super.applyProperties();
		}
		
		public void iterateMainNode() {
			
			// instantiate new root node
			rootNode = new SeqPropertyNode("^", mainNode.getZaehler()); 
						
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
																
								SeqPropertyNode childNode = deepIteration(true, pair.getValue(), node);
								
								//get node directly beneath childnode
								rootNode.addNode(childNode.getValue(), childNode); 
								
						} else if(pair.getValue().getKinder().size() > 1) {
								Iterator<Entry<String, Knoten>> subIt = pair.getValue().getKinder().entrySet().iterator();
								
								SeqPropertyNode node = new SeqPropertyNode(pair.getKey(), pair.getValue().getZaehler());
								
								while (subIt.hasNext()) {
									HashMap.Entry<String, Knoten> subPair = (HashMap.Entry<String, Knoten>)subIt.next();
									SeqPropertyNode subNode = new SeqPropertyNode(subPair.getKey(), subPair.getValue().getZaehler());
									
									SeqPropertyNode childNode = deepIteration(false, subPair.getValue(), subNode);
									
									node.addNode(childNode.getValue(), childNode);
									subIt.remove(); // avoids a ConcurrentModificationException
								}
								
								rootNode.addNode(node.getValue(), node);
						}
				}
			    it.remove(); // avoids a ConcurrentModificationException
			}
		}
		
		private SeqPropertyNode deepIteration(boolean flag, Knoten Node, SeqPropertyNode propNode) {
			boolean concatFlag = flag;
			Knoten currentNode = Node;
			SeqPropertyNode lastPropNode = propNode;
			SeqPropertyNode currPropNode = new SeqPropertyNode(currentNode.getName(), currentNode.getZaehler());
			
			// reaching a terminal node adds the sequence to the previous node
			if (currentNode.getKinder().isEmpty()) {
				if (concatFlag) {
					return currPropNode;
				} else {
					return lastPropNode;
				}
			} else {
				Iterator<Entry<String, Knoten>> deepIt = currentNode.getKinder().entrySet().iterator();
				while (deepIt.hasNext()) {
					HashMap.Entry<String, Knoten> deepPair = (HashMap.Entry<String, Knoten>)deepIt.next();
					
					if(deepPair.getValue().getKinder().size() == 0) {
						SeqPropertyNode newPropNode = new SeqPropertyNode(deepPair.getKey(),deepPair.getValue().getZaehler());				
						
						if (concatFlag) {
							currPropNode = newPropNode;
						} else {
							currPropNode.addNode(newPropNode.getValue(), newPropNode);
						}
						
						
					} else if(deepPair.getValue().getKinder().size() == 1) { //if child has one grand child
						concatFlag = true;
						if(currentNode.getKinder().size() == 1) {
							currPropNode.concatValue(deepPair.getValue().getName()); // remember the name of the child
						}
						Object[] key = deepPair.getValue().getKinder().keySet().toArray(); //What is the name of the grand child?
												
						SeqPropertyNode newPropNode = deepIteration(concatFlag, deepPair.getValue().getKinder().get(key[0]),currPropNode); //Let's continue with the grand child
												
						if(newPropNode.getNodeHash().size() == 1) { //What the grand child also has only one child?
							Object[] newChild = newPropNode.getNodeHash().keySet().toArray();//What is the name of this new child?
							currPropNode.concatValue(newPropNode.getValue()); //remember the name of the grand child
							currPropNode.addNode(newPropNode.getNodeHash().get(newChild[0]).getValue(), newPropNode.getNodeHash().get(newChild[0]));//continue with this new child
						} else { // so the grand child several or no children, let's remember that
							currPropNode.addNode(newPropNode.getValue(), newPropNode); 
						}
					} else if (deepPair.getValue().getKinder().size() > 1) { // if there are more nodes remember the zaehler value
						concatFlag = false;
						SeqPropertyNode newNode = deepIteration(concatFlag, deepPair.getValue(),currPropNode);
						currPropNode.addNode(newNode.getValue(), newNode);
					}
				}
				deepIt.remove(); // avoids a ConcurrentModificationException
				return currPropNode;
			}
		}
		
		//creation of a Newick tree
		public void iterateNewickMainNode() { 

			// instantiate new root node
			rootNode = new SeqPropertyNode("^", mainNode.getZaehler()); 
			
			// add root node to Newick format
			addLeadingParenthesisNewick();
			setRootNewickNode(rootNode.getValue(), rootNode.getCounter()); 
			
			Iterator<Entry<String, Knoten>> it = mainNode.getKinder().entrySet().iterator();
			
			while (it.hasNext()) {
				HashMap.Entry<String, Knoten> pair = (HashMap.Entry<String, Knoten>)it.next();
				
				if(pair.getValue().getKinder().isEmpty()) {
					//end node on first level reached. Create terminal node at tree height 0.
					SeqPropertyNode node = new SeqPropertyNode(pair.getKey(), pair.getValue().getZaehler());
					setTermNewickNode(node.getValue(), node.getCounter()); //new Newick terminal node
					rootNode.addNode(pair.getKey(), node);
				} else {
						if (pair.getValue().getKinder().size() == 1) {
								SeqPropertyNode node = new SeqPropertyNode(pair.getKey(), pair.getValue().getZaehler());
								
								// add child node to Newick format
								addLeadingParenthesisNewick(); 
								
								SeqPropertyNode childNode = deepNewickIteration(true, pair.getValue(), node);
								
								// add child node to Newick format
								setConcatNewickNode(childNode.getValue(), childNode.getCounter());
								
								//get node directly beneath childnode
								rootNode.addNode(childNode.getValue(), childNode); 
								
						} else if(pair.getValue().getKinder().size() > 1) {
								Iterator<Entry<String, Knoten>> subIt = pair.getValue().getKinder().entrySet().iterator();
								
								SeqPropertyNode node = new SeqPropertyNode(pair.getKey(), pair.getValue().getZaehler());
								
								//add node to Newick format
								addLeadingParenthesisNewick(); 
								
								while (subIt.hasNext()) {
									HashMap.Entry<String, Knoten> subPair = (HashMap.Entry<String, Knoten>)subIt.next();
									SeqPropertyNode subNode = new SeqPropertyNode(subPair.getKey(), subPair.getValue().getZaehler());
									
									//add child node to Newick format
									addLeadingParenthesisNewick(); 
									
									SeqPropertyNode childNode = deepNewickIteration(false, subPair.getValue(), subNode);
									
									//add child node to Newick format
									setInnerNewickNode(childNode.getValue(), childNode.getCounter());
									
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
		
		private SeqPropertyNode deepNewickIteration(boolean flag, Knoten Node, SeqPropertyNode propNode) {
			boolean concatFlag = flag;
			Knoten currentNode = Node;
			SeqPropertyNode lastPropNode = propNode;
			SeqPropertyNode currPropNode = new SeqPropertyNode(currentNode.getName(), currentNode.getZaehler());
			
			// reaching a terminal node adds the sequence to the previous node
			if (currentNode.getKinder().isEmpty()) {
				//setTermNewickNode(currPropNode.getValue(), currPropNode.getCounter()); //new Newick terminal node
				if (concatFlag) {
					String newVal = lastPropNode.getValue();
					newVal += currPropNode.getValue();
					currPropNode.setValue(newVal);
					return currPropNode;
				} else {
					return lastPropNode;
				}
			} else {
				Iterator<Entry<String, Knoten>> deepIt = currentNode.getKinder().entrySet().iterator();
				while (deepIt.hasNext()) {
					HashMap.Entry<String, Knoten> deepPair = (HashMap.Entry<String, Knoten>)deepIt.next();
					
					if(deepPair.getValue().getKinder().size() == 0) {
						SeqPropertyNode newPropNode = new SeqPropertyNode(deepPair.getKey(),deepPair.getValue().getZaehler());				
						
						if (concatFlag) {
							lastPropNode.concatValue(newPropNode.getValue());
							currPropNode = lastPropNode;
						} else {
							setTermNewickNode(newPropNode.getValue(), newPropNode.getCounter()); //new Newick terminal node
							currPropNode.addNode(newPropNode.getValue(), newPropNode);
						}
						
						
					} else if(deepPair.getValue().getKinder().size() == 1) { //if child has one grand child
						boolean lastFlag = concatFlag; // remember the last concatenation status
						concatFlag = true;
						if(currentNode.getKinder().size() == 1) {
							currPropNode.concatValue(deepPair.getValue().getName()); // remember the name of the child
						}
						Object[] key = deepPair.getValue().getKinder().keySet().toArray(); //What is the name of the grand child?
						if (!lastFlag) { // if concatenation was false last time we were at a branching node, so write an inner Newick node
							addLeadingParenthesisNewick(); //add a leading Parenthesis to the Newick output
						}					
						SeqPropertyNode newPropNode = deepNewickIteration(concatFlag, deepPair.getValue().getKinder().get(key[0]),currPropNode); //Let's continue with the grand child
						if (!lastFlag) {
							//currPropNode.concatValue(newPropNode.getValue());
							setConcatNewickNode(newPropNode.getValue(), newPropNode.getCounter()); //write content of inner node
						}
						
						if(newPropNode.getNodeHash().size() == 1) { //What the grand child also has only one child?
							Object[] newChild = newPropNode.getNodeHash().keySet().toArray();//What is the name of this new child?
							currPropNode.concatValue(newPropNode.getValue()); //remember the name of the grand child
							currPropNode.addNode(newPropNode.getNodeHash().get(newChild[0]).getValue(), newPropNode.getNodeHash().get(newChild[0]));//continue with this new child
						} else { // so the grand child several or no children, let's remember that
							currPropNode.addNode(newPropNode.getValue(), newPropNode); 
						}
					} else if (deepPair.getValue().getKinder().size() > 1) { // if there are more nodes remember the zaehler value
						concatFlag = false;
						addLeadingParenthesisNewick(); //add a leading Parenthesis to the Newick output
						SeqPropertyNode newNode = deepNewickIteration(concatFlag, deepPair.getValue(),currPropNode);
						setInnerNewickNode(newNode.getValue(), newNode.getCounter()); //write content of inner node
						currPropNode.addNode(newNode.getValue(), newNode);
						
					}
				}
				deepIt.remove(); // avoids a ConcurrentModificationException
				return currPropNode;
			}
		}
	
	//display all tree properties
	public void displayAllTreeProperties () {
		iteratePropMainNode();
		sortByPathLength();
		calculateProps();
		seqPropertiesOutput = "Longest Path:\t" + longestPath + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Average length of paths:\t" + avPathLen + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Average ratio of paths:\t" + avPathRatio + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Normalized Sackin index:\t" + sackinIndexNorm + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Sequence\tpath length\tpath ratio\n";
		for (SeqProperties i : seqPropertiesSorted) {
			seqPropertiesOutput = seqPropertiesOutput + i.getNodeName() + "\t" + i.getPathLength() + "\t" + i.getPathRatio() + "\n";
		}
	}
	
	//identification of all properties of the tree
	public void iteratePropMainNode() { 

		// instantiate new root node
		rootNode = new SeqPropertyNode("^", mainNode.getZaehler());  
		
		//number of the current leaf of the root node
		String innerNodeName = rootNode.getValue();
		
		//define the properties of the root node
		seqProperties = new HashMap<String, SeqProperties>();
		seqProperties.put(innerNodeName, new SeqProperties(innerNodeName, rootNode.getValue(), 0));
		
		//instantiate new sackin index
		sackinIndex = new ArrayList<SeqSackinIndex> ();
		
		Iterator<Entry<String, Knoten>> it = mainNode.getKinder().entrySet().iterator();
		
		while (it.hasNext()) {
			HashMap.Entry<String, Knoten> pair = (HashMap.Entry<String, Knoten>)it.next();
			
			if(pair.getValue().getKinder().isEmpty()) {
				
				//end node on first level reached. Create terminal node.
				SeqPropertyNode node = new SeqPropertyNode(pair.getKey(), pair.getValue().getZaehler());
				rootNode.addNode(pair.getKey(), node);
				
				//add a leaf to the Sackin index
				totalNumOfLeaves ++;
				SeqSackinIndex index = new SeqSackinIndex(node.getValue(), 1);
				index.catSequence(rootNode.getValue());
				sackinIndex.add(index);

			} else {
				
				if (pair.getValue().getKinder().size() == 1) {
						SeqPropertyNode node = new SeqPropertyNode(pair.getKey(), pair.getValue().getZaehler());
						
						SeqPropertyNode childNode = deepPropIteration(true, pair.getValue(), node, innerNodeName, rootNode.getValue());
											
						//get node directly beneath child node
						rootNode.addNode(childNode.getValue(), childNode); 
						
				} else if(pair.getValue().getKinder().size() > 1) {
					
					//increase the "name" for each iteration
					String pairNodeName = innerNodeName + pair.getValue().getName();
					
					Iterator<Entry<String, Knoten>> subIt = pair.getValue().getKinder().entrySet().iterator();
					
					SeqPropertyNode node = new SeqPropertyNode(pair.getKey(), pair.getValue().getZaehler());
					
					//create properties of inner node
					seqProperties.put(pairNodeName, new SeqProperties(pairNodeName, node.getValue(), seqProperties.get("^").getPathLength() + 1, (((double)node.getCounter())/((double)rootNode.getCounter()))));
					
					while (subIt.hasNext()) {
						
						HashMap.Entry<String, Knoten> subPair = (HashMap.Entry<String, Knoten>)subIt.next();
						SeqPropertyNode subNode = new SeqPropertyNode(subPair.getKey(), subPair.getValue().getZaehler());
						
						//increase the "name" for each iteration
						String subPairNodeName = pairNodeName;
						//create properties for an inner node
						if (subPair.getValue().getKinder().size() > 1 ) {
							subPairNodeName += subPair.getValue().getName();
							seqProperties.put(subPairNodeName, new SeqProperties(subPairNodeName, subNode.getValue(), seqProperties.get(pairNodeName).getPathLength() + 1, (((double)subNode.getCounter())/((double)node.getCounter()))));
						}
						
						SeqPropertyNode childNode = deepPropIteration(false, subPair.getValue(), subNode, subPairNodeName, subPairNodeName);
						
						
						
						node.addNode(childNode.getValue(), childNode);
						subIt.remove(); // avoids a ConcurrentModificationException
					}
					
					rootNode.addNode(node.getValue(), node);
					
				}
			}
			
		    it.remove(); // avoids a ConcurrentModificationException 
		}
	}
	
	private SeqPropertyNode deepPropIteration(boolean flag, Knoten Node, SeqPropertyNode propNode, String propNodeName, String catNodeName) {
		boolean concatFlag = flag;
		Knoten currentNode = Node;
		SeqPropertyNode lastPropNode = propNode;
		SeqPropertyNode currPropNode = new SeqPropertyNode(currentNode.getName(), currentNode.getZaehler());
		String lastPropNodeName = propNodeName;
		
		//variable to save the name of "reduced" concatenated nodes
		String lastconcatNodeName = catNodeName;
		
		// reaching a terminal node adds the sequence to the previous node
		if (currentNode.getKinder().isEmpty()) {
			
			//add a leaf to the Sackin index
			totalNumOfLeaves ++;
			
			
			if (concatFlag) {
				//concatenate last node with just one child node
				String newVal = lastPropNode.getValue();
				newVal += currPropNode.getValue();
				currPropNode.setValue(newVal);
				
				//add new leaf to calculate the Sackin index
				SeqSackinIndex index = new SeqSackinIndex(currPropNode.getValue(), seqProperties.get(lastPropNodeName).getPathLength());
				index.catSequence(lastconcatNodeName);
				sackinIndex.add(index);
				
				return currPropNode;
			} else {
				
				//add new leaf to calculate the Sackin index
				SeqSackinIndex index = new SeqSackinIndex(currPropNode.getValue(), seqProperties.get(lastPropNodeName).getPathLength());
				index.catSequence(lastconcatNodeName);
				sackinIndex.add(index);
				
				return lastPropNode;
			}
		} else {
			Iterator<Entry<String, Knoten>> deepIt = currentNode.getKinder().entrySet().iterator();
			while (deepIt.hasNext()) {
				HashMap.Entry<String, Knoten> deepPair = (HashMap.Entry<String, Knoten>)deepIt.next();
				
				if(deepPair.getValue().getKinder().size() == 0) {
					SeqPropertyNode newPropNode = new SeqPropertyNode(deepPair.getKey(),deepPair.getValue().getZaehler());				
					
					//add a leaf to the Sackin index
					totalNumOfLeaves ++;
					
					if (concatFlag) {
						lastPropNode.concatValue(newPropNode.getValue());
						currPropNode = lastPropNode;
						
						//add new leaf to calculate the Sackin index
						SeqSackinIndex index = new SeqSackinIndex(currPropNode.getValue(), seqProperties.get(lastPropNodeName).getPathLength());
						index.catSequence(lastconcatNodeName);
						sackinIndex.add(index);
						
					} else {
						currPropNode.addNode(newPropNode.getValue(), newPropNode);
						
						//add new leaf to calculate the Sackin index
						SeqSackinIndex index = new SeqSackinIndex(currPropNode.getValue(), seqProperties.get(lastPropNodeName).getPathLength());
						index.catSequence(lastconcatNodeName);
						index.appendSequence(newPropNode.getValue());
						sackinIndex.add(index);
					}
					
					
				} else if(deepPair.getValue().getKinder().size() == 1) { //if child has one grand child
					
					concatFlag = true;
					if(currentNode.getKinder().size() == 1) {
						currPropNode.concatValue(deepPair.getValue().getName()); // remember the name of the child
					}
					Object[] key = deepPair.getValue().getKinder().keySet().toArray(); //What is the name of the grand child?
					
					//remember the name of the last "concat inner node"
					//String concatNodeName = lastconcatNodeName + lastPropNodeName + currPropNode.getValue();
					String concatNodeName = lastconcatNodeName + currPropNode.getValue();
					
					SeqPropertyNode newPropNode = deepPropIteration(concatFlag, deepPair.getValue().getKinder().get(key[0]),currPropNode, lastPropNodeName, concatNodeName); //Let's continue with the grand child
										
					if(newPropNode.getNodeHash().size() == 1) { //What the grand child also has only one child?
						Object[] newChild = newPropNode.getNodeHash().keySet().toArray();//What is the name of this new child?
						currPropNode.concatValue(newPropNode.getValue()); //remember the name of the grand child
						currPropNode.addNode(newPropNode.getNodeHash().get(newChild[0]).getValue(), newPropNode.getNodeHash().get(newChild[0]));//continue with this new child
					} else { // so the grand child several or no children, let's remember that
						currPropNode.addNode(newPropNode.getValue(), newPropNode); 
					}
				} else if (deepPair.getValue().getKinder().size() > 1) { // if there are more nodes remember the zaehler value
					concatFlag = false;
					
					SeqPropertyNode newNode = deepPropIteration(concatFlag, deepPair.getValue(),currPropNode, lastPropNodeName, "");
					
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
				sackinSum = sackinSum + 1/counter;
			}
			counter ++;
		}
		
		//calculate Sackin variance
		double sackinVariance = totalNumOfLeaves * sackinSum;
		
		//calculate normalized Sackin index
		sackinIndexNorm = (sackinIndexReady - sackinVariance) / totalNumOfLeaves;
	}
}
