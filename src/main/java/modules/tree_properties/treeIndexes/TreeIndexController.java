package modules.tree_properties.treeIndexes;

// Java I/O imports.
import java.io.PipedReader;

// Java math imports.
import java.math.BigInteger;

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
		
		// Add module description
				this.setDescription("This modules transverse a Generalized Suffix Tree (GST) depth-first<br>"
						+ "and analysis different properties for each node and subtree:<br>"
						+ "<ul><li>Number of Leaves</li>"
						+ "<li>Path Length(s)</li>"
						+ "<li>Cophenetic index</li>"
						+ "<li>(normalized) Sackin Index</li></ul>"
						+ "<b>Requirements:</b>"
						+ "<ul><li>JSON output from (pre-buffered) \"dot2tree\" conversion module</li>"
						+ "<li>XML output from GST builder module</li></ul>");

		// Add property descriptions.
		this.getPropertyDescriptions().put(PROPERTYKEY_FREQOUT, "\"true\": show tree frequencies</br>" + 
				"\"false\": do not show tree frequencies");
		
		// Add module category.

				
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
		this.rootNode = gson.fromJson(reader, Dot2TreeInnerNode.class);
	}
			
	//end setters
	
	//getters:
		
	//end getters
	
	// Methods for normalization of the Sackin index values.
	
	/**
	 * The method calcSackinNorm() effectively normalizes the retrieved Sackin indexes.
	 * @param n            number of leaves.
	 * @param sackin       calculated non-normalized Sackin index.
	 * @return sackinNorm  double value between 0 and 1
	 */
	private double calcSackinNorm(int n, double sackin) {
		double sackinNorm = (sackin - this.calcSackinMin(n)) 
				/ (this.calcSackinMax(n) - this.calcSackinMin(n));
		return sackinNorm;
	}
	
	/**
	 * 
	 * @param n                number of leaves
	 * @return maxSackinIndex  for n
	 */
	private double calcSackinMax (int n) {
		return ((Math.pow(n, 2) + n - 2)/2);
	}
	
	/**
	 * 
	 * @param n                number of leaves
	 * @return minSackinIndex  for n
	 */
	private double calcSackinMin (int n) {
		double sackinMin = Math.floor(this.log4(n) * Math.pow(4, this.log4(n)))
				+ Math.floor((this.log4(n) + 1) * ( n - Math.pow(4, this.log4(n))))
				+ Math.ceil(1/3 * (n - Math.pow(4, this.log4(n))));
		return sackinMin;
	}
	
	/**
	 * This method calculates the logarithm to the base 4.
	 * @param n           integer
	 * @return logarithm  to the base 4
	 */
	private double log4(int n) {
		return (Math.log(n) / Math.log(4));
	}
	
	/**
	 * Calculation of the maximum cophenetic index for maximally imbalanced trees.
	 * @param   n       integer indicating number of leaves.
	 * @return  result  BigInteger showing the value of a max. cophenetic index.
	 */
	private BigInteger calcMaxCophenetic (int n) {
		
		BigInteger term1 = BigInteger.valueOf(n-2);
		BigInteger term2 = BigInteger.valueOf(n-1);
		BigInteger term3 = BigInteger.valueOf(n);

		BigInteger result = term3.multiply(term2.multiply(term1));
		result = result.divide(BigInteger.valueOf(6));
		return result;
	}
	
	/**
	 * 
	 * @return true
	 * @throws Exception
	 * @see modules.ModuleImpl#process
	 */
	@Override
	public boolean process() throws Exception {
		
		//create mainNode by reading JSON input
		this.setGson(this.getInputPorts().get(INPUTID).getInputReader());
					
		//iterate over the tree and get parameters
		this.displayAllTreeProperties();
				
		if (this.freqOut && this.getOutputPorts().get(FREQOUTID).isConnected()) {
			//write the tree frequencies into the output
			this.getOutputPorts().get(FREQOUTID).outputToAllCharPipes(this.freqOutString);
		}
								
		// Close outputs (important!).
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
	private void displayAllTreeProperties () throws Exception {
		
		// Create rootNode tree and initialize "indexProperties". 
		this.iterateGST();
		
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
		this.seqPropertiesOutput = this.seqPropertiesOutput + "max Sackin index:\t" + this.calcSackinMax(this.totalNumOfLeaves) + "\n";
		this.seqPropertiesOutput = this.seqPropertiesOutput + "normalized Sackin index:\t" + this.calcSackinNorm(this.totalNumOfLeaves, this.sackinIndexVal) + "\n";
		
		seqPropertiesOutput = seqPropertiesOutput + "Cophenetic index:\t" + this.copheneticIndexVal + "\n";
		this.seqPropertiesOutput = this.seqPropertiesOutput + "max cophenetic index:\t" + this.calcMaxCophenetic(this.totalNumOfLeaves) + "\n";
		
		// Prepare the extracted parameters for subtrees.+
		
		seqPropertiesOutput = seqPropertiesOutput + "nodeNumber\tedgeLabel\tpathLength\tnormSackin\tsackinIndex\tcopheneticIndex\tnumberOfLeaves\t"
				+ "numberOfInnerNodes\tmaxSackinIndex\tmaxCopheneticIndex\n";
		
		// Write the current output.
		this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(this.seqPropertiesOutput);
		
		// Flush the String variable this.seqPropertiesOutput to avoid problems with strings larger than MAXINT.
		this.seqPropertiesOutput = "";
		
		for (IndexProperties i : this.seqPropertiesSorted) {
			if (i.getNodeNumber() == 1) {
				seqPropertiesOutput = seqPropertiesOutput + i.getNodeNumber() + "\t" + i.getEdgeLabel() + "\t" + i.getTreeDepth() 
				+ "\t" + this.calcSackinNorm(this.totalNumOfLeaves, this.sackinIndexVal)+ "\t" + this.sackinIndexVal + "\t" + this.copheneticIndexVal + "\t" + this.totalNumOfLeaves + "\n";
			} else {
				seqPropertiesOutput = seqPropertiesOutput + i.getNodeNumber() + "\t" + i.getEdgeLabel() + "\t" + i.getTreeDepth() 
				+ "\t" + this.calcSackinNorm(i.getLeafNum(), this.subSackinTrees.get(i.getEdgeLabel())) 
				+ "\t" + this.subSackinTrees.get(i.getEdgeLabel()) + "\t" + this.subCophTrees.get(i.getEdgeLabel()) 
				+ "\t" + i.getLeafNum() + "\t" + this.subTreeInnerNodes.get(i.getEdgeLabel()) + "\t"
				+ ((Math.pow((double)i.getLeafNum(),2)+((double)i.getLeafNum()-2))/2) + "\t"
				+ this.calcMaxCophenetic(i.getLeafNum())
				+ "\n";
			}
			// Continually write the output.
			this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(this.seqPropertiesOutput);
			
			// Flush this.seqPropertiesOutput.
			this.seqPropertiesOutput = "";
		}
		
		if ( this.freqOut && this.getOutputPorts().get(FREQOUTID).isConnected() ) {
			// Put the results for the frequency analysis into the output.
			this.freqOutString = this.freqOutString + "number of terminal branches" + "\tfrequency\n";
						
			// Write the tree frequencies into the output.
			this.getOutputPorts().get(FREQOUTID).outputToAllCharPipes(this.freqOutString);
			
			// Flush this.freqOutString.
			this.freqOutString = "";
			
			for (int i : this.freqSpectrum.keySet()) {
				this.freqOutString = this.freqOutString + i + "\t" + freqSpectrum.get(i)  + "\n";
				
				// Continually write the output.
				this.getOutputPorts().get(FREQOUTID).outputToAllCharPipes(this.freqOutString);
				
				// Flush this.freqOutString.
				this.freqOutString = "";
			}
		}
	}
	
	//identification of all properties of the tree
	private void iterateGST() { 

		// Set the node depth (level of the tree) for the root node.
		this.rootNode.setNodeDepth(0);
		
		// Set edge label "^" to indicate the root.
		this.rootNode.setEdgeLabel("^");
		
		// Define the properties of the root node.
		indexProperties = new HashMap<Integer, IndexProperties>();
		indexProperties.put(this.rootNode.getNodeNumber(), new IndexProperties(this.rootNode.getNodeNumber(), 
				this.rootNode.getEdgeLabel(), this.rootNode.getNodeDepth(), 0));
		
		
		// Iterate over all leaves beneath the root node.
		for (Map.Entry<Integer, Dot2TreeLeafNode> entry : rootNode.getAllLeaves().entrySet()) {
			
			// First level of the tree reached. Update the node depth for this particular node.
			entry.getValue().setNodeDepth(1);
			
			// Update the total number of leaves.
			this.totalNumOfLeaves ++;
			
			// Update the total number of leaves for root node.
			this.indexProperties.get(this.rootNode.getNodeNumber()).incrementLeaves();
		}
		
		// Iterate over all inner nodes beneath the root node.
		for (Map.Entry<Integer, Dot2TreeInnerNode> entry : ((Dot2TreeInnerNode)rootNode).getAllChildNodes().entrySet()) {
						
			// First level of the tree reached. Update the node depth for this particular node.
			entry.getValue().setNodeDepth(1);
			
			if ( ((Dot2TreeInnerNode)entry.getValue()).getAllChildNodes().size() == 0) {
				
				// Save the number of returned leaves.
				int returnedLeaves = 0;
				
				// First level of the tree reached. Update the node depth for this particular node.
				entry.getValue().setNodeDepth(1);
				
				// Iterate over the one leaf beneath, if there is any.
				for (Map.Entry<Integer, Dot2TreeLeafNode> entryLeaves : ((Dot2TreeInnerNode) entry.getValue()).getAllLeaves().entrySet()) {
					
					// Set tree depth for the leaves of "entry".
					entryLeaves.getValue().setNodeDepth(2);
					
					// Update the total number of leaves.
					this.totalNumOfLeaves ++;
					
					// Update the total number of leaves for root node.
					this.indexProperties.get(this.rootNode.getNodeNumber()).incrementLeaves();
					
					// Increment the number of leaves for the current node.
					returnedLeaves ++;
				}
				
				// Add deepEntry to indexProperties.
				this.indexProperties.put(entry.getValue().getNodeNumber(), new IndexProperties(entry.getValue().getNodeNumber(),
						entry.getValue().getEdgeLabel(), 1, 0));
				
				// Concatenate the edge label of the root to the "entry" inner node.
				this.indexProperties.get(entry.getValue().getNodeNumber()).catEdgeLabel(this.rootNode.getEdgeLabel());
									
				// Increase number of leaves for the inner node "entry".
				this.indexProperties.get(entry.getValue().getNodeNumber()).increaseLeaves(returnedLeaves);
											
			}
										
			if ( ((Dot2TreeInnerNode)entry.getValue()).getAllChildNodes().size() == 1) {
				
				// First level of the tree reached. Update the node depth for this particular node.
				entry.getValue().setNodeDepth(1);
								
				// Add deepEntry to indexProperties.
				this.indexProperties.put(entry.getValue().getNodeNumber(), new IndexProperties(entry.getValue().getNodeNumber(),
						entry.getValue().getEdgeLabel(), 1, 0));
				
				// Concatenate the edge label of the root to the "entry" inner node.
				this.indexProperties.get(entry.getValue().getNodeNumber()).catEdgeLabel(this.rootNode.getEdgeLabel());
				
				// Start deep iteration for inner node, if there is any inner node beneath. Discard the return value.
				if (!entry.getValue().getAllChildNodes().isEmpty())
					deepGSTIteration(entry.getValue(), 1);
									
			} else if (((Dot2TreeInnerNode)entry.getValue()).getAllChildNodes().size() > 1) {
				
				// Save the number of returned leaves.
				int returnedLeaves = 0;
				
				// Create properties for the current inner node.
				indexProperties.put(entry.getValue().getNodeNumber(), new IndexProperties(entry.getValue().getNodeNumber(), 
						entry.getValue().getEdgeLabel(), entry.getValue().getNodeDepth(), 0));
				
				// Iterate over all leaves beneath the inner node of "entry".
				for (Map.Entry<Integer, Dot2TreeLeafNode> entryLeaves : ((Dot2TreeInnerNode)entry.getValue()).getAllLeaves().entrySet()) {
					
					// Set the depth for these leaves.
					entryLeaves.getValue().setNodeDepth(2);
					
					// Update the total number of leaves.
					this.totalNumOfLeaves ++;
					
					// Update the total number of leaves for root node.
					this.indexProperties.get(this.rootNode.getNodeNumber()).incrementLeaves();
					
					// Increment the number of leaves for the current node.
					returnedLeaves ++;
					
				} 
				
				// Concatenate the edge label of the root to the "entry" inner node.
				this.indexProperties.get(entry.getValue().getNodeNumber()).catEdgeLabel(this.rootNode.getEdgeLabel());
				
				
				// Iterate over all inner nodes beneath the inner node of "entry".
				for (Map.Entry<Integer, Dot2TreeInnerNode> subEntry : ((Dot2TreeInnerNode)entry.getValue()).getAllChildNodes().entrySet()) {
					
					// Create new subEntry object in indexProperties.
					this.indexProperties.put(subEntry.getValue().getNodeNumber(), new IndexProperties(subEntry.getValue().getNodeNumber(),
							subEntry.getValue().getEdgeLabel(), 2, 0));
					
					// Concatenate the edge label of "subEntry" to the "entry" inner node.
					this.indexProperties.get(subEntry.getValue().getNodeNumber())
						.catEdgeLabel(this.indexProperties.get(entry.getValue().getNodeNumber()).getEdgeLabel());
					
					// Start deep iteration.
					returnedLeaves += deepGSTIteration(subEntry.getValue(), 2);
															
				}
				
				
				// Increase number of leaves for "entry".
				this.indexProperties.get(entry.getValue().getNodeNumber()).increaseLeaves(returnedLeaves);
				
			} 
		}
	}
	
	private int deepGSTIteration(Dot2TreeNodes currNode, int nodeDepth) {
		
		// Update the current tree depth.
		int currDepth = nodeDepth + 1;
		
		// Update the depth of the current node.
		currNode.setNodeDepth(currDepth);
		
		int returnedLeaves = 0;
		
		IndexProperties testObj = this.indexProperties.get(currNode.getNodeNumber());
		
		if (testObj == null) {
			// Add the current node properties to the HashMap "indexProperties".
			this.indexProperties.put(currNode.getNodeNumber(), new IndexProperties(currNode.getNodeNumber(),
					currNode.getEdgeLabel(), nodeDepth, 0));
		}
						
		// Iterate over all leaves beneath the currNode.
		for (Map.Entry<Integer, Dot2TreeLeafNode> deepEntry : ((Dot2TreeInnerNode)currNode).getAllLeaves().entrySet()) {
			
			// Set the current depth for this leaf.
			deepEntry.getValue().setNodeDepth(currDepth);
			
			// Update the total number of leaves.
			this.totalNumOfLeaves ++;
			
			// Update the total number of leaves for root node.
			this.indexProperties.get(this.rootNode.getNodeNumber()).incrementLeaves();
						
			// Increment returnedLeaves upon encountering another leaf.
			returnedLeaves ++; 
			
		}
		
		// Iterate over all inner nodes beneath the root node.
		for (Map.Entry<Integer, Dot2TreeInnerNode> deepEntry : ((Dot2TreeInnerNode)currNode).getAllChildNodes().entrySet()) {
			
			// Add deepEntry to indexProperties.
			this.indexProperties.put(deepEntry.getValue().getNodeNumber(), new IndexProperties(deepEntry.getValue().getNodeNumber(),
					deepEntry.getValue().getEdgeLabel(), currDepth, 0));
			
			if(((Dot2TreeInnerNode)deepEntry.getValue()).getAllChildNodes().size() == 0) {
				
				// Remember the amount of encountered leaves. 
				int deepLeaves = 0;
				
				// Concatenate the edge label of the current node to "deepEntry".
				this.indexProperties.get(deepEntry.getValue().getNodeNumber())
					.catEdgeLabel(this.indexProperties.get(currNode.getNodeNumber()).getEdgeLabel());
				
				// Continue with the grand child leaves, if they exist.
				for (Map.Entry<Integer, Dot2TreeLeafNode> deepEntryLeaves : ((Dot2TreeInnerNode)deepEntry.getValue()).getAllLeaves().entrySet()) {
					
					// Set the tree depth of the leaves.
					deepEntryLeaves.getValue().setNodeDepth(currDepth + 1); 
					
					// Update the total number of leaves.
					this.totalNumOfLeaves ++;
					
					// Update the total number of leaves for root node.
					this.indexProperties.get(this.rootNode.getNodeNumber()).incrementLeaves();
					
					// Increment the number of encountered leaves.
					deepLeaves ++;
				
				}
				
				// Increase number of encountered leaves for deepEntry.
				this.indexProperties.get(deepEntry.getValue().getNodeNumber()).increaseLeaves(deepLeaves);
				
				// Increase the number of all leaves to return.
				returnedLeaves += deepLeaves;
								
			}
				
			if(((Dot2TreeInnerNode)deepEntry.getValue()).getAllChildNodes().size() == 1) {
				
				// Remember the amount of encountered leaves. 
				int deepLeaves = 0;
				
				// Concatenate the edge label of the current node to "deepEntry".
				this.indexProperties.get(deepEntry.getValue().getNodeNumber())
					.catEdgeLabel(this.indexProperties.get(currNode.getNodeNumber()).getEdgeLabel());
				
				// Continue with the grand child leaves, if they exist.
				for (Map.Entry<Integer, Dot2TreeLeafNode> deepEntryLeaves : ((Dot2TreeInnerNode)deepEntry.getValue()).getAllLeaves().entrySet()) {
					
					// Set the tree depth of the leaves.
					deepEntryLeaves.getValue().setNodeDepth(currDepth + 1); 
					
					// Update the total number of leaves.
					this.totalNumOfLeaves ++;
					
					// Update the total number of leaves for root node.
					this.indexProperties.get(this.rootNode.getNodeNumber()).incrementLeaves();
					
					// Increment the number of encountered leaves.
					deepLeaves ++;
				
				}
								
				// Continue with the grand child inner node, if it exists.
				if (!deepEntry.getValue().getAllLeaves().isEmpty())
					deepLeaves = deepGSTIteration(deepEntry.getValue(), currDepth); 
				
				// Increase the number of all leaves to return.
				returnedLeaves += deepLeaves;
								
			} else if (((Dot2TreeInnerNode)deepEntry.getValue()).getAllChildNodes().size() > 1) {		
				
				// Concatenate the edge label of the current node to "deepEntry".
				this.indexProperties.get(deepEntry.getValue().getNodeNumber())
					.catEdgeLabel(this.indexProperties.get(currNode.getNodeNumber()).getEdgeLabel());
				
				// Remember the amount of encountered leaves. 
				int deepLeaves = 0;
				
				// Iterate over all leaves beneath "deepEntry".
				for (Map.Entry<Integer, Dot2TreeLeafNode> subDeepEntryLeaves : ((Dot2TreeInnerNode)deepEntry.getValue()).getAllLeaves().entrySet()) {
					
					// Set the depth for these leaves.
					subDeepEntryLeaves.getValue().setNodeDepth(currDepth + 1);
					
					// Update the total number of leaves.
					this.totalNumOfLeaves ++;
					
					// Update the total number of leaves for root node.
					this.indexProperties.get(this.rootNode.getNodeNumber()).incrementLeaves();
					
					// Increment the number of encountered leaves.
					deepLeaves ++;
					
				}
				
				// Iterate over all inner nodes beneath "deepEntry".
				for (Map.Entry<Integer, Dot2TreeInnerNode> subDeepEntry : ((Dot2TreeInnerNode)deepEntry.getValue()).getAllChildNodes().entrySet()) {
					
					// Add the current node properties to the HashMap "indexProperties".
					this.indexProperties.put(subDeepEntry.getValue().getNodeNumber(), new IndexProperties(subDeepEntry.getValue().getNodeNumber(),
							subDeepEntry.getValue().getEdgeLabel(), currDepth + 1, 0));
					
					// Concatenate the edge label of "deepEntry" to the "subDeepEntry" inner node.
					this.indexProperties.get(subDeepEntry.getValue().getNodeNumber())
						.catEdgeLabel(this.indexProperties.get(deepEntry.getValue().getNodeNumber()).getEdgeLabel());
					
					// Continue with the next great grand child.
					int subDeepLeaves = deepGSTIteration(subDeepEntry.getValue(), currDepth + 1); 
					
					// Increase number of leaves for "deepEntry".
					this.indexProperties.get(deepEntry.getValue().getNodeNumber()).increaseLeaves(subDeepLeaves);
					
					// Increase the number of all leaves to return.
					deepLeaves += subDeepLeaves;
					
				}
				
				// Increase the number of leaves for "deepEntry" inner node.
				this.indexProperties.get(deepEntry.getValue().getNodeNumber()).increaseLeaves(deepLeaves);
				
				
				// Increase the amount of leaves to return.
				returnedLeaves += deepLeaves;
			}
		}
		
		// Increase the number of leaves for current inner node.
		this.indexProperties.get(currNode.getNodeNumber()).increaseLeaves(returnedLeaves);
		
		// Return the number of leaves for the current subtree.
		return returnedLeaves;
	}
	
	// Create the Sackin and the cophenetic indexes.
	public void createIndexHashMaps() {
		this.sackinIndex = new HashMap<String, IndexSackin>();
		
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
		int lastLength = this.indexProperties.get(1).getTreeDepth();
		
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
					lastCophVal += this.copheneticIndex.get(i.getEdgeLabel()).getBinomialCoeff();
					termNode = false;
				}
				
				// Increase Sackin index only if the lastStr is a substring of the current sequence OR if it is equal.
				if (i.getEdgeLabel().length() >= lastStr.length() && (i.getEdgeLabel().equals(lastStr) 
						|| i.getEdgeLabel().substring(0, lastStr.length()).equals(lastStr))) {
					lastSackinVal += this.sackinIndex.get(i.getEdgeLabel()).getNodeNumber();
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
