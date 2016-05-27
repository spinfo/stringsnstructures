package modules.treeIndexes;

// Java I/O imports.
import java.io.PipedReader;

// Java utilities imports.
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

// Google Gson imports.
import com.google.gson.Gson;

// Workbench specific imports.
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import models.Dot2TreeNodes;
import models.Dot2TreeLeafNode;
import models.Dot2TreeInnerNode;

public class TreeIndexController extends ModuleImpl {
	// Property keys:
	private static final String PROPERTYKEY_FREQOUT = "Print out tree frequencies?";
	// End keys.

	// Variables:
	
	// Boolean variable which decides whether or not display the tree frequencies.
	private boolean freqOut;
	
	// String variable holding the tree frequency output.
	private String freqOutString = "";
	
	// HashMap to save the properties of each inner node of the tree.
	private HashMap<Integer, IndexProperties> indexProperties;
	
	//sequence properties output string
	private String seqPropertiesOutput;
	
	//sorted ArrayList of properties for each node by path length
	private ArrayList<IndexProperties> seqPropertiesSorted;
	
	private ArrayList<Integer> averagePathLength;
	private double avPathLen;
	
	// total number of leaves of the tree
	private int totalNumOfLeaves;
	
	// total number of inner nodes for all sub trees
	private HashMap<String, Integer> subTreeInnerNodes;
	
	// Variable holding the longestPath (= height of the tree).
	private int longestPath;
	
	// Variables for calculating the cophenetic index.
	private HashMap<String,IndexCophenetic> copheneticIndex;
	private ArrayList<Integer> copheneticIndexBinomList;
	private double avCopheneticIndex;
	
	// Variable holding cophenetic index.
	private int copheneticIndexVal;
	
	// HashMap holding cophenetic indexes for subtrees.
	HashMap<String, Integer> subCophTrees;
	
	// This variable saves the frequency spectrum of singletons, doublet and triplets 
	private TreeMap<Integer, Integer> freqSpectrum;
	
	//variables for calculating the Sackin index
	private HashMap<String, IndexSackin> sackinIndex;
	private ArrayList<Integer> sackinIndexLeavesList;
	private double avSackinIndex; 
	
	// Variable holding the Sackin index.
	private double sackinIndexVal;
	
	// HashMap holding Sackin indexes for subtrees.
	HashMap<String, Integer> subSackinTrees;
	
	//sackin variance
	//private double sackinVar;
	
	//normalized Sackin index
	//private double sackinIndexNorm;
	
	/* Variable holding an root node from the imported tree. 
	 * It will actually reference a Dot2TreeNodes object.
	 */ 
	private Dot2TreeInnerNode rootNode;
	
	// Reference to Gson object.
	private Gson gson;
	
	// Define I/O identifiers.
	private final String INPUTID = "input";
	private final String OUTPUTID = "output";
	private final String FREQOUTID = "treeFreqOut";
	
	//end variables
	
	//constructors:
	public TreeIndexController(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// Add property descriptions.
		this.getPropertyDescriptions().put(PROPERTYKEY_FREQOUT, "\"true\": show tree frequencies</br>" + 
				"\"false\": do not show tree frequencies");
		
		// Add module category.
		this.setCategory("TreeStructure");
				
		// Add property defaults.
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Tree Index Properties");
		this.getPropertyDefaultValues().put(PROPERTYKEY_FREQOUT, "false");
		
		// set up newickOutput.
		this.seqPropertiesOutput = new String();
		
		// Define I/O.
		InputPort inputPort = new InputPort(INPUTID, "[Json] tree input from the </br>Dot2GST converter module", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUTID, "[plain text] sequence properties output</br>in table like form", this);
		outputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputTreePort = new OutputPort(FREQOUTID, "[tsv] tree frequencies</br>as tsv table", this);
		outputTreePort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		super.addOutputPort(outputTreePort);
		
	}
	//end constructors
	
	//setters:
		
	public void setGson(PipedReader reader) {
		gson = new Gson();
		rootNode = gson.fromJson(reader, TreeIndexNodes.class);
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
		
		if (this.freqOut) {
			//write the tree frequencies into the output
			this.getOutputPorts().get(FREQOUTID).outputToAllCharPipes(this.freqOutString);
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
		this.freqOut = Boolean.parseBoolean(this.getProperties().getProperty((PROPERTYKEY_FREQOUT), this.getPropertyDefaultValues().get(PROPERTYKEY_FREQOUT)));
		
		// Apply parent object's properties
		super.applyProperties();
	}
		
	//display all tree properties
	private void displayAllTreeProperties () {
		
		// Create rootNode tree and initialize "indexProperties". 
		this.iterateGST();
		
		//create indexes
		//iterateIndexRootNode();
		
		// Create the Sackin and the cophenetic indexes.
		this.createIndexHashMaps();
		
		// Sort by path length.
		this.sortByPathLength();
		
		// Calculate all tree properties.
		this.calculateProps();
		
		//prepare the output for general parameters and statistics for the whole tree
		seqPropertiesOutput = "Longest Path for inner nodes:\t" + this.longestPath + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Longest Path (for leaves):\t" + (this.longestPath + 1) + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Average length of paths:\t" + this.avPathLen + "\n";
		
		seqPropertiesOutput = seqPropertiesOutput + "Average Sackin index of paths:\t" + this.avSackinIndex + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Average cophenetic index of paths:\t" + this.avCopheneticIndex + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Total number of leaves:\t" + this.totalNumOfLeaves + "\n";
		seqPropertiesOutput = seqPropertiesOutput + "Sackin index:\t" + this.sackinIndexVal + "\n";
		this.seqPropertiesOutput = this.seqPropertiesOutput + "max Sackin index:\t" + ((Math.pow((double)this.totalNumOfLeaves,2)-(double)this.totalNumOfLeaves - 2)/2) + "\n";
		
		seqPropertiesOutput = seqPropertiesOutput + "Cophenetic index:\t" + this.copheneticIndexVal + "\n";
		this.seqPropertiesOutput = this.seqPropertiesOutput + "max cophenetic index:\t" + ((this.totalNumOfLeaves-2)*(this.totalNumOfLeaves-1)*(this.totalNumOfLeaves)/6) + "\n";
		
		//prepare the extracted parameters for subtrees
		seqPropertiesOutput = seqPropertiesOutput + "Sequence\tpath length\tSackin index\tcophenetic index\tnumber of leaves\t"
				+ "number of inner nodes\tmax Sackin index\tmax cophenetic index\n";
		for (IndexProperties i : this.seqPropertiesSorted) {
			if (i.getEdgeLabel() == "^") {
				seqPropertiesOutput = seqPropertiesOutput + i.getNodeName() + "\t" + i.getTreeDepth() 
				+ "\t" + this.sackinIndexVal + "\t" + this.copheneticIndexVal + "\t" + this.totalNumOfLeaves + "\n";
			} else {
				seqPropertiesOutput = seqPropertiesOutput + i.getNodeName() + "\t" + i.getTreeDepth() 
				+ "\t" + this.subSackinTrees.get(i.getNodeName()) + "\t" + this.subCophTrees.get(i.getNodeName()) 
				+ "\t" + i.getLeafNum() + "\t" + this.subTreeInnerNodes.get(i.getNodeName()) + "\t"
				+ ((Math.pow((double)i.getLeafNum(),2)+((double)i.getLeafNum()-2))/2) + "\t"
				+ ((i.getLeafNum()-2)*(i.getLeafNum()-1)*i.getLeafNum()/6)
				+ "\n";
			}
		}
		
		if (this.freqOut) {
			// Put the results for the frequency analysis into the output.
			this.freqOutString = this.freqOutString + "number of terminal branches" + "\tfrequency\n";
			this.freqOutString = this.freqOutString + "1\t" + this.totalNumOfLeaves + "\n";
			for (int i : this.freqSpectrum.keySet()) {
				this.freqOutString = this.freqOutString + "\t" + i + "\t" + freqSpectrum.get(i)  + "\n";
			}
		}
	}
	
	//identification of all properties of the tree
	private void iterateGST() { 

		// Set the node depth (level of the tree) for the root node.
		this.rootNode.setNodeDepth(0);
		
		//define the properties of the root node
		indexProperties = new HashMap<Integer, IndexProperties>();
		indexProperties.put(this.rootNode.getNodeNumber(), new IndexProperties(this.rootNode.getNodeNumber(), this.rootNode.getEdgeLabel(), this.rootNode.getNodeDepth(), 0));
		
		for (Map.Entry<Integer, Dot2TreeNodes> entry : ((Dot2TreeInnerNode)rootNode).getAllChildNodes().entrySet()) {
						
			// First level of the tree reached. Update the node depth for this particular node.
			entry.getValue().setNodeDepth(1);
			
			// Leaf on first level reached.
			if(entry.getValue().getClass().equals(Dot2TreeLeafNode.class)) {
				
				// If the current node is of the class type "Dot2TreeLeafNode" it is a leaf,
				// so continue with the next iteration.
				
				// Update the total number of leaves.
				this.totalNumOfLeaves ++;
				
				// Update the total number of leaves for root node.
				this.indexProperties.get(this.rootNode.getNodeNumber()).incrementLeaves();
				
				// Continue with next node.
				continue;
			
			// Inner node on first level reached.
			} else if (entry.getValue().getClass().equals(Dot2TreeInnerNode.class)) {
				
				// Save the number of returned leaves.
				int returnedLeaves = 0;
				
				// (Very) rare case of a "singleton" inner node (should not exist at all). Even so continue with iteration.
				// TODO: Is such a case really worth the consideration or kick it out completely and give a warning to the logger?
				
				if ( ((Dot2TreeInnerNode)entry.getValue()).getAllChildNodes().size() == 1) {
					
					// Start deep iteration.
					returnedLeaves = deepGSTIteration(entry.getValue(), entry.getValue().getNodeDepth());
					this.indexProperties.put(entry.getValue().getNodeNumber(), new IndexProperties(entry.getValue().getNodeNumber(), 
							entry.getValue().getEdgeLabel(), entry.getValue().getNodeDepth(), returnedLeaves));
												
				} else if (((Dot2TreeInnerNode)entry.getValue()).getAllChildNodes().size() > 1) {
					
					// Create properties for the current inner node.
					indexProperties.put(entry.getValue().getNodeNumber(), new IndexProperties(entry.getValue().getNodeNumber(), 
							entry.getValue().getEdgeLabel(), entry.getValue().getNodeDepth(), entry.getValue().getNodeFreq()));
					
					for (Map.Entry<Integer, Dot2TreeNodes> subEntry : ((Dot2TreeInnerNode)entry.getValue()).getAllChildNodes().entrySet() ) {
						
						// Start deep iteration.
						returnedLeaves += deepGSTIteration(subEntry.getValue(), 2);
						
						// Concatenate the edge label of "subEntry" to the "entry" inner node.
						this.indexProperties.get(subEntry.getValue().getNodeNumber()).catEdgeLabel(entry.getValue().getEdgeLabel());
						
						// Increase number of leaves for "deepEntry".
						this.indexProperties.get(subEntry.getValue().getNodeNumber()).increaseLeaves(returnedLeaves);
						
					}
					
				}
				this.indexProperties.get(this.rootNode.getNodeNumber()).increaseLeaves(returnedLeaves);
			}
			 
		}
	}
	
	private int deepGSTIteration(Dot2TreeNodes currNode, int nodeDepth) {
		
		// Update the current tree depth.
		int currDepth = nodeDepth + 1;
		
		// Update the depth of the current node.
		currNode.setNodeDepth(currDepth);
		
		int returnedLeaves;
		
		// Add the current node properties to the HashMap "indexProperties".
		this.indexProperties.put(currNode.getNodeNumber(), new IndexProperties(currNode.getNodeNumber(),
				currNode.getEdgeLabel(), currNode.getNodeDepth(), currNode.getNodeFreq()));
		
		// Reaching a leaf adds the sequence to the previous node.
		if (currNode.getClass().equals(Dot2TreeLeafNode.class)) {
							
			// Update the total number of leaves.
			this.totalNumOfLeaves ++;
			
			// Return 1.
			return 1; 
		
		} else if (currNode.getClass().equals(Dot2TreeInnerNode.class)) {
			for (Map.Entry<Integer, Dot2TreeNodes> deepEntry : ((Dot2TreeInnerNode)currNode).getAllChildNodes().entrySet()) {
				
				if(((Dot2TreeInnerNode)deepEntry.getValue()).getAllChildNodes().size() == 0) {
					
					// Update the total number of leaves.
					this.totalNumOfLeaves ++;
										
					// Return 1 leaf.
					returnedLeaves = 1;
					
					// Increase number of leaves for this inner node.
					this.indexProperties.get(currNode.getNodeNumber()).increaseLeaves(returnedLeaves);
					
				} else if(((Dot2TreeInnerNode)deepEntry.getValue()).getAllChildNodes().size() == 1) {
					
					// (Very) rare case of a "singleton" inner node (should not exist at all). Even so continue with iteration.
					// TODO: Is such a case really worth the consideration or kick it out completely and give a warning to the logger?
					
					// Concatenate the edge label of the current node to "deepEntry".
					this.indexProperties.get(deepEntry.getValue().getNodeNumber()).catEdgeLabel(currNode.getEdgeLabel());
					
					// Continue with the grand child.
					returnedLeaves = deepGSTIteration(deepEntry.getValue(), currDepth); 
					
					// Increase number of leaves for this inner node.
					this.indexProperties.get(currNode.getNodeNumber()).increaseLeaves(returnedLeaves);
					
					
					
				} else if (((Dot2TreeInnerNode)deepEntry.getValue()).getAllChildNodes().size() > 1) {
					
					// Add the current node properties to the HashMap "indexProperties".
					this.indexProperties.put(currNode.getNodeNumber(), new IndexProperties(currNode.getNodeNumber(),
							currNode.getEdgeLabel(), currNode.getNodeDepth(), currNode.getNodeFreq()));
					
					for (Map.Entry<Integer, Dot2TreeNodes> subDeepEntry : ((Dot2TreeInnerNode)deepEntry.getValue()).getAllChildNodes().entrySet()) {
						
						// Concatenate the edge label of "deepEntry" to the "subDeepEntry" inner node.
						this.indexProperties.get(subDeepEntry.getValue().getNodeNumber()).catEdgeLabel(deepEntry.getValue().getEdgeLabel());
						
						// Continue with the next great grand child.
						returnedLeaves = deepGSTIteration(subDeepEntry.getValue(), currDepth + 1); 
						
						// Increase number of leaves for "deepEntry".
						this.indexProperties.get(deepEntry.getValue().getNodeNumber()).increaseLeaves(returnedLeaves);
						
					}
					
					// Increase number of leaves for this inner node.
					this.indexProperties.get(currNode.getNodeNumber()).increaseLeaves(
							this.indexProperties.get(deepEntry.getValue().getNodeNumber()).getLeafNum());
				}
			}
			
		}
		// Return the number of leaves for the current subtree.
		return this.indexProperties.get(currNode.getNodeNumber()).getLeafNum();
	}
	
	// Create the Sackin and the cophenetic indexes.
	public void createIndexHashMaps() {
		this.sackinIndex = new HashMap<String, IndexSackin> ();
		
		this.copheneticIndex = new HashMap<String,IndexCophenetic>();
		
		for (Map.Entry<Integer, IndexProperties> entry : this.indexProperties.entrySet()) {
					
			// Get the number of descending leaves which equals the number of inner nodes beneath a specific node
			// and save this data as a node for the Sackin index.
			IndexSackin sIndex = new IndexSackin (entry.getValue().getEdgeLabel(), entry.getValue().getLeafNum());
			this.sackinIndex.put(entry.getValue().getEdgeLabel(),sIndex);
			
			// Get the number of descending leaves and save the data as a node for the cophenetic index.
			IndexCophenetic cIndex = new IndexCophenetic (entry.getValue().getEdgeLabel(),entry.getValue().getLeafNum());
			this.copheneticIndex.put(entry.getValue().getEdgeLabel(), cIndex);
		}
	}
	
	
	// Sort the different paths through the tree by their length.
	public void sortByPathLength() {
		
		// Initiate the sorted ArrayList.
		this.seqPropertiesSorted = new ArrayList<IndexProperties>();
				
		// Remember the length of the last path.
		int lastLength = this.indexProperties.get("^").getTreeDepth();
		
		// Instantiate variables to remember information about path length and indexes.
		this.averagePathLength = new ArrayList<Integer>();
		this.sackinIndexLeavesList = new ArrayList<Integer>();
		this.copheneticIndexBinomList = new ArrayList<Integer>();
		
		for (Map.Entry<Integer, IndexProperties> entry : this.indexProperties.entrySet()) {
			
			// Get information to calculate the average path length and the average path ratio.
			this.averagePathLength.add(entry.getValue().getTreeDepth());
			
			this.sackinIndexLeavesList.add(this.sackinIndex.get(entry.getValue().getEdgeLabel()).getNodeNumber());
			
			// Do not include the root node.
			if (!(entry.getValue().getEdgeLabel() == "^"))   
				this.copheneticIndexBinomList.add(this.copheneticIndex.get(entry.getValue().getEdgeLabel()).getBinomialCoeff());
			
			
			
			if (entry.getValue().getTreeDepth() >= lastLength) {
				// Put it to the top.
				this.seqPropertiesSorted.add(0, entry.getValue()); 
				lastLength = entry.getValue().getTreeDepth();
			} else {
				//iterate over the ArrayList seqPropertiesSorted and add this sequence
				for(IndexProperties i : this.seqPropertiesSorted) {

					if (i.getTreeDepth() <= entry.getValue().getTreeDepth()) {
						this.seqPropertiesSorted.add(this.seqPropertiesSorted.indexOf(i), entry.getValue());
						break;
					}
					
					// if this is the smallest value add it to the end of the list
					if (this.seqPropertiesSorted.indexOf(i) == this.seqPropertiesSorted.size() -1 ) { 
						this.seqPropertiesSorted.add(entry.getValue());
						break;
					}
				}
			}
		}
		this.longestPath = lastLength;
	}
	
	// Calculate properties of the tree.
	public void calculateProps() {

		int totalPathLen = 0;
		
		for (Integer i : this.averagePathLength) {
			totalPathLen = totalPathLen + i;  
		}
		
		this.avPathLen = (double) totalPathLen / (double) this.averagePathLength.size();
		
		this.sackinIndexVal = 0;
		
		// Calculate the Sackin index.
		for (int i : this.sackinIndexLeavesList) {
			this.sackinIndexVal = this.sackinIndexVal + i; 
		}
		
		// Calculate the cophenetic index.
		for (int i : this.copheneticIndexBinomList) {
			this.copheneticIndexVal = this.copheneticIndexVal + i;
		}
		
		// Calculate Sackin and cophenetic indexes for subtrees.
		this.subCophTrees = new HashMap<String, Integer> ();
		this.subSackinTrees = new HashMap<String, Integer> ();
		
		// create freqSpectrum object
		this.freqSpectrum = new TreeMap <Integer, Integer> ();
				
		ArrayList <IndexProperties> seqPropertiesSortedInverted = new ArrayList <IndexProperties> ();
		
		for (IndexProperties i : this.seqPropertiesSorted) {
			if (!(i.getEdgeLabel() == "^")) {
				seqPropertiesSortedInverted.add(0, i);
			}
			if (this.freqOut) {
				this.calcFreqSeq(i.getLeafNum());
			}
		}
		
		// String variable holding the current edge label.
		String lastStr = "";
		
		// Integer variable holding the current value for the cophenetic index for a parituclar sub tree.
		int lastCophVal = 0;
		
		// Integer variable holding the current value for the sackin index for a parituclar sub tree.
		int lastSackinVal = 0;
		
		// Integer variable holding the number of internal nodes for the current sub tree.
		int currTreeInnerNodes = 0;
		
		// create subTreeInnerNodes object
		this.subTreeInnerNodes = new HashMap<String,Integer>();
		boolean termNode = true;
		for (IndexProperties j : seqPropertiesSortedInverted) {
			lastStr = j.getEdgeLabel();
			
			for (IndexProperties i : seqPropertiesSortedInverted) {
				/* Increase cophenetic index only if the lastStr is a substring of the current sequence.
				 * This avoids calculating the n choose k value for the root of the tree.
				 */ 
				if (i.getEdgeLabel().length() > lastStr.length() && !(i.getEdgeLabel().equals(lastStr)) 
						&& i.getEdgeLabel().substring(0, lastStr.length()).equals(lastStr)) {
					lastCophVal += this.copheneticIndex.get(i.getNodeName()).getBinomialCoeff();
					termNode = false;
				}
				
				// Increase Sackin index only if the lastStr is a substring of the current sequence OR if it is equal.
				if (i.getEdgeLabel().length() >= lastStr.length() && (i.getEdgeLabel().equals(lastStr) 
						|| i.getEdgeLabel().substring(0, lastStr.length()).equals(lastStr))) {
					lastSackinVal += this.sackinIndex.get(i.getNodeName()).getNodeNumber();
					currTreeInnerNodes ++;
				}
			}
			
			if (termNode) {
				lastCophVal = 0;
			}
			this.subCophTrees.put(lastStr, lastCophVal);
			this.subSackinTrees.put(lastStr,lastSackinVal);
			this.subTreeInnerNodes.put(lastStr,currTreeInnerNodes);
			lastCophVal = 0;
			lastSackinVal = 0;
			currTreeInnerNodes = 0;
			termNode = true;
		}
						
		this.avSackinIndex = (double) this.sackinIndexVal / (double) this.averagePathLength.size();
		this.avCopheneticIndex = (double) this.copheneticIndexVal / (double) this.averagePathLength.size();
	}
	
	// Increase number of singletons, doublets, triplets etc. in dependence of previous results.
	public void calcFreqSeq (int freq)  {
		if (!this.freqSpectrum.containsKey(freq)) {
			this.freqSpectrum.put(freq,1);
		} else {
			this.freqSpectrum.put(freq, this.freqSpectrum.get(freq) + 1);
		}
	}

}
