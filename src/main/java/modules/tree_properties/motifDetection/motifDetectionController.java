package modules.tree_properties.motifDetection;

//Java utility imports.
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Comparator;

//Apache utility imports.
import org.apache.commons.lang3.ArrayUtils;

//Java I/O imports.
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.PipedInputStream;

//Workbench specific imports.
import java.util.Properties;
import modules.CharPipe;
import modules.BytePipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.tree_properties.branchLengthGroups.SuffixLinkNodes;
import common.parallelization.CallbackReceiver;

//Workbench GSTXmlNode imports.
import common.GSTXmlStreamReader;
import models.GSTXmlNode;

//Workbench Dot2TreeNodes imports.
import models.Dot2TreeInnerNode;
import models.Dot2TreeInnerNodesParent;
import models.Dot2TreeLeafNode;
import models.Dot2TreeNodes;

/**
 * This modue is experimental.
 * @author christopher
 *
 */


public class motifDetectionController extends ModuleImpl {
	
	// Enumerators:	
	private enum DotTags {
			ROOT, LEAFSTART, LEAFEND, INTERNAL, EDGE, SUFFIXLINK, UNDEFINED
	}
	// End enumerators.
	
	// Property keys:
	public static final String PROPERTYKEY_MINALPHA = "Minimal length for identical string alpha allowed";
	// End property keys.
	
	// Variables:
	
	// This variable defines the minimum length of an identical starting string alpha.
	private int minAlpha;
	
	// Dot document status.
	DotTags DotStat = DotTags.UNDEFINED;
	
	// Dot2TreeNodes root object.
	// Create the root of the tree and incorporate all nodes beneath.
	Dot2TreeInnerNodesParent rootNode;
	
	// GSTXmlStreamReader object.
	GSTXmlStreamReader treeXmlStreamReader;
	
	// GST XML input stream.
	private PipedInputStream suffixTreeInStream;
	
	// GST XML TreeMap holding each node.
	private TreeMap <Integer, GSTXmlNode> gstXmlNodes;
	
	// TreeMap to save all found nodes in order of their node 
	// numbers in form of Dot2TreeNodes objects.
	private TreeMap <Integer, Dot2TreeNodes> dot2TreeNodesMap;
	
	
	// ArrayList holding the results of the backtracking process to 
	// infer the edgeLabels with the longest strings via following 
	// the suffix links.
	private ArrayList <SuffixLinkNodes> suffixLinkSearchRes;
	
	// Variable holding each line of the input.
	private String inputString;
	
	// IDs for I/O pipelines.
	private final String INPUTDOTID = "dot input";
	private final String INPUTXMLID = "xml input";
	private final String OUTPUTID = "output";
		
	// End variables.
	
	// Constructors:
	public motifDetectionController (CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super (callbackReceiver, properties);
		
		// Module description.
		this.setDescription("This module transverses a Generalized Suffix Tree (GST) bottom up<br>"
			+ "and detects nodes which are linked by suffix-links to one-another. <br>"
			+ "Such nodes which include a common starting edge (constant identical edge) and <br>"
			+ "<b>Requirements:</b><br>"
			+ "<em>pending...</em>");
		
		// Property descriptions.
			/* No module properties required at the moment.*/
		
		// Initialize module specific fields.
		this.dot2TreeNodesMap = new TreeMap <Integer, Dot2TreeNodes>();
		this.gstXmlNodes = new TreeMap <Integer, GSTXmlNode>();
		
		// Property defaults.
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Motif Detection");
		
		// Initialize I/O pipelines.
		InputPort inputDotPort = new InputPort(INPUTDOTID, "<b>[dot format]</b> Dot output from the<br>GST builder module.", this);
		inputDotPort.addSupportedPipe(CharPipe.class);
		
		InputPort inputXmlPort = new InputPort(INPUTXMLID, "<b>[xml format]</b> Xml output from the<br>GST builder module.", this);
		inputXmlPort.addSupportedPipe(BytePipe.class);
		
		OutputPort outputPort = new OutputPort (OUTPUTID, "<b>[tsv]</b> Table format", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		super.addInputPort(inputDotPort);
		super.addInputPort(inputXmlPort);
		super.addOutputPort(outputPort);
	}
	
	// End constructors.
	
	// Methods:
	
	@Override
	public boolean process () throws Exception {
		// Initialize inputString.
		this.inputString = "";
			
		// Read form input stream and parse GST XML format.
		
		try {
			this.suffixTreeInStream = this.getInputPorts().get(INPUTXMLID).getInputStream();
			this.treeXmlStreamReader = new GSTXmlStreamReader (this.suffixTreeInStream);
			this.gstXmlNodes = treeXmlStreamReader.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Convert the string variable "inputString" into Dot2TreeNodes objects.
		this.convertString2TreeNodes ();
		
		// Follow all suffix links and the length of the branch labels.
		this.bottomUp ();
		
		// Sort and extract longest cumulative branches.
		this.sortSuffixRes();
		
		// Write the output in table (tsv) form.
		if (this.getOutputPorts().get(OUTPUTID).isConnected()) 
			this.writeOutput();
		
		// Close outputs.
		this.closeAllOutputs();
		
		// Success.
		return true;
	}
	
	private void writeOutput () throws Exception {
		String header = "SuffixPathLength\tStartNodeNumber\tEndNodeNumber\tConcatPathEdgeLabel\n";
		this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(header);
		Iterator <SuffixLinkNodes> it = this.suffixLinkSearchRes.iterator();
		while (it.hasNext()) {
			SuffixLinkNodes currSuffixRes = it.next();
			String line = Integer.toString(currSuffixRes.getSuffixLinkPathLen()) + "\t";
			line += Integer.toString(currSuffixRes.getStartNodeNumber()) + "\t";
			line += Integer.toString(currSuffixRes.getFinalNodeNumber()) + "\t";
			line += currSuffixRes.getConcatEdgeLabs() + "\n";
			this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(line);
		}
	}
	
	/**
	 * This method converts the tree information from the dot file into a Dot2TreeNodes TreeMap.
	 * @return void
	 */
	private void convertString2TreeNodes () {
		
		/* Prepare field matcher for: 
		 * 1.) root node 
		 * 2.) leaf nodes
		 * 3.) inner nodes
		 * 4.) suffix links 
		 */
		
		// Define regex patterns.
		
		// Root node pattern.
		Pattern rootPat = Pattern.compile("\\A\t(node)(1) \\[");
		
		// Leaf node pattern.
		Pattern leafPat = Pattern.compile("\\A\t(node\\d+) \\[label=\"(\\d+)(( \\d+ \\d+ \\d+)+)\",.+\\]");
		// Leaf start pattern.
		Pattern leafStartPat = Pattern.compile("\\A\tnode\\d+ \\[label=\"\\d+\\z");
		// Leaf end pattern.
		Pattern leafEndPat = Pattern.compile("\\A \\d+ \\d+ \\d+\",.+\\]");
		
		// Inner node pattern.
		Pattern innerPat = Pattern.compile("\\A\t(node\\d+) \\[label=\"(\\d+)\",.*\\]\\z"); 
			
		// Edge pattern.
		Pattern edgePat = Pattern.compile("\\A\t(node)(\\d+) -> (node)(\\d+)\\[label=\"(.+)\",.*\\]");
		
		// Suffix link pattern.
		Pattern suffLinkPat = Pattern.compile("\\A\t(node)(\\d+) -> (node)(\\d+)\\ \\[label=\"\",.*\\]");
		
		// Node fields:
		int nodeNumber = 1;
		int nodeFreq;
		ArrayList <Integer> nodeLeafInfo = new ArrayList <Integer> ();
		
		// Node vs node comparison.
		int node1Number;
		int node2Number;
			
		// Local variable holding the current line.
		String currLine = null;
		
		// Local variable holding leaf node information for parsing.
		String leafInfo = "";
		
		// Save the truncated end of a string which does not end on a newline sign.
		String nextLine = "";
		
		try {
			
			// Variables used for input data.
			int bufferSize = 1024;
			char [] bufferInput = new char [bufferSize];
			
			// Read first characters
			int charCode = this.getInputPorts().get(INPUTDOTID).getInputReader().read(bufferInput, 0, bufferSize);
			
			while( charCode != -1 )
			{
				// Check for interrupt signal.
				if (Thread.interrupted()) {
					this.closeAllOutputs();
					throw new InterruptedException("Thread has been interrupted.");
				}
				
				// Convert char array to string buffer.
				StringBuffer inputBuffer = new StringBuffer(new String (bufferInput).substring(0, charCode));
				
				// Check whether there was a previous empty nextLine or not.
				if (nextLine.isEmpty()) {
					this.inputString = inputBuffer.toString();
				} else {
					this.inputString = nextLine;
					this.inputString += inputBuffer.toString();
				}
				
				
				BufferedReader bufReader;
				
				// Save the currently read lines.
				String[] currentLines = null;
				
				// Decide whether to pursue the lines line by line, or truncate the last line in case it does not end on
				// a newline character.
				if (this.inputString.substring(this.inputString.length() - 1).equals("\n")) {
					
					// No truncated line needed.
					nextLine = "";
					
					// Loop through the inputString via a BufferedReader line by line.
					bufReader = new BufferedReader(new StringReader(this.inputString));
					
				} else {
					
					currentLines = this.inputString.split("\n");
					
					// Save the last truncated line to nextLine.
					nextLine = currentLines[currentLines.length-1];
					
					// Remove last truncated line.
					currentLines = ArrayUtils.remove(currentLines, currentLines.length-1);
					
					String newLines = "";
					for (String i : currentLines) {
						newLines += i + "\n";
					}
					// Loop through the truncated lines line by line.
					bufReader = new BufferedReader(new StringReader(newLines));
					
					
				}
				
				try {
						
					// Loop through the current input string line by line.
					while ( (currLine = bufReader.readLine()) != null) {
										
						// Define matcher.
						Matcher rootQuery = rootPat.matcher(currLine);
						Matcher leafStartQ = leafStartPat.matcher(currLine);
						Matcher leafEndQ = leafEndPat.matcher(currLine);
						Matcher innerQ = innerPat.matcher(currLine);
						Matcher edgeQ = edgePat.matcher(currLine);
						Matcher suffLinkQ = suffLinkPat.matcher(currLine);
						
						// Search for regular expressions and set search status.
						if (rootQuery.find()) {
							nodeNumber = Integer.parseInt(rootQuery.group(2));
							this.DotStat = DotTags.ROOT;
						} else if (leafStartQ.find()) {
							this.DotStat = DotTags.LEAFSTART;
						} else if (leafEndQ.find()) {
							this.DotStat = DotTags.LEAFEND;
						} else if (innerQ.find()) {
							this.DotStat = DotTags.INTERNAL;
						} else if (edgeQ.find()) {
							this.DotStat = DotTags.EDGE;
						} else if (suffLinkQ.find()) {
							this.DotStat = DotTags.SUFFIXLINK;
						}
						
						// Execute actions appropriate for the current search status.
						switch (this.DotStat) {
						case ROOT:
							nodeFreq = this.gstXmlNodes.get(nodeNumber).getNodeFrequency();
							this.rootNode = new Dot2TreeInnerNodesParent(nodeNumber, nodeFreq, "node1", "");
							
							// Set tree depth for the root node.
							this.rootNode.setNodeDepth(0);
							this.dot2TreeNodesMap.put(nodeNumber, rootNode);
							this.DotStat = DotTags.UNDEFINED;
							break;
							
						case LEAFSTART:
							leafInfo += currLine;
							break;
							
						case LEAFEND:
							// Add the end of the leaf to leafInfo.
							leafInfo += currLine;
							
							// Define an appropriate matcher to get the different groups from the leaf node string.
							Matcher leafQ = leafPat.matcher(leafInfo);
						
							leafQ.find();
							nodeNumber = Integer.parseInt(leafQ.group(2));
							nodeFreq = this.gstXmlNodes.get(nodeNumber).getNodeFrequency();
							
							
							// Create new dot2TreeLeafNode object and save it in the TreeMap dot2TreeNodesMap.
							this.dot2TreeNodesMap.put(nodeNumber, new Dot2TreeLeafNode(nodeNumber, nodeFreq, leafQ.group(1)));
							
							// Save information about text number, starting point of occurrence and end point in the ArrayList
							// "nodeLeafInfo" temporarily.
							String [] tokenArray = leafQ.group(3).split(" ");
							
							/* Start with index i = 1 to skip the first (empty) element of the array "tokenArray". 
							 * Due the chosen pattern the string starts with " ".
							 * Hence, the first element after splitting for each " " will be an empty string. 
							 * This first element must be skipped.
							 */
							for (int i = 1; i < tokenArray.length; i++) 
								nodeLeafInfo.add(Integer.parseInt(tokenArray[i]));
												
							// Fill the fields for the new Dot2TreeLeafNode object with information gathered from the "nodeLeafInfo" ArrayList.
							for (int i = 0; i < (nodeLeafInfo.size()/3); i ++)
								((Dot2TreeLeafNode) this.dot2TreeNodesMap.get(nodeNumber)).setLeafInfo(
										nodeLeafInfo.get(i*3), nodeLeafInfo.get(i*3+1), nodeLeafInfo.get(i*3+2));
							
							// Reset leafInfo.
							leafInfo = "";
							
							// Reset nodeLeafInfo.
							nodeLeafInfo.clear();
							this.DotStat = DotTags.UNDEFINED;
							break;
							
						case INTERNAL:
							nodeNumber = Integer.parseInt(innerQ.group(2));
							nodeFreq = this.gstXmlNodes.get(nodeNumber).getNodeFrequency();
							
							// Attention I used here a different class to represent internal nodes. This class also holds informations 
							// about direct parental nodes.
							this.dot2TreeNodesMap.put(nodeNumber, new Dot2TreeInnerNodesParent (nodeNumber, nodeFreq, innerQ.group(1)));
							this.DotStat = DotTags.UNDEFINED;
							break;
						
						case EDGE:
							node1Number = Integer.parseInt(edgeQ.group(2));
							node2Number = Integer.parseInt(edgeQ.group(4));
							String edgeLabel = edgeQ.group(5);
							this.dot2TreeNodesMap.get(node2Number).setEdgeLabel(edgeLabel);
							if (this.dot2TreeNodesMap.get(node2Number).getClass().equals(Dot2TreeInnerNodesParent.class)) {
								
								// Add edge between two internal nodes.
								((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(node1Number))
									.addInnerNode(node2Number,((Dot2TreeInnerNodesParent)this.dot2TreeNodesMap.get(node2Number)));
								
								// Add the number of the direct parent for node "node2Number".
								((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(node2Number))
									.setParent( ((Dot2TreeInnerNodesParent)this.dot2TreeNodesMap.get(node1Number)).getNodeNumber() );
								
							}
							else if (this.dot2TreeNodesMap.get(node2Number).getClass().equals(Dot2TreeLeafNode.class))
								((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(node1Number))
									.addLeaf(node2Number,((Dot2TreeLeafNode)this.dot2TreeNodesMap.get(node2Number)));
							this.DotStat = DotTags.UNDEFINED;
							break;
						
						case SUFFIXLINK:
							node1Number = Integer.parseInt(suffLinkQ.group(2));
							node2Number = Integer.parseInt(suffLinkQ.group(4));
							((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(node1Number)).setSuffixLinks(node2Number);
													
							this.DotStat = DotTags.UNDEFINED;
							break;
							
						default: 
							break;
						}
					}
				
				// Read next charsplitWords.
				charCode = this.getInputPorts().get(INPUTDOTID).getInputReader().read(bufferInput, 0, bufferSize);
				
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// Delete the current lines in the inputString.
				this.inputString = "";
	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This private method initiates the backtracking process a long the path of suffix links.
	 * @return void
	 */
	private void bottomUp () {
		
		/* The general purpose of the methods bottomUp() and backwardsIteration() is to transverse the GST
		 * bottom up to the root. During that process the algorithm will find identical suffixes leading to 
		 * suffix links. It will follow these suffix links while keeping track of the parental nodes of
		 * visited nodes. If identical start sequences were found (alpha) a alpha set, a delta set and a
		 * N-set will be defined and saves as MotifCandidates object. 
		 * Key-requirements are: 
		 * 1.) A minimum length for alpha must be established.
		 * 2.) A N-set needs to have at least 2 entries (2 parallel occurring branches and nodes at at least 2
		 *     positions in the tree.
		 */
		
		/*
		 * Pseudo code for the algorithm:
		 */
		
		
		// Initialize the TreeMap holding the results for the suffix link node search.
		this.suffixLinkSearchRes = new ArrayList <SuffixLinkNodes> ();
		
		// Iterate over all nodes.
		Iterator<Map.Entry<Integer,Dot2TreeNodes>> it = this.dot2TreeNodesMap.entrySet().iterator();
		
		while(it.hasNext()) {
			Map.Entry<Integer, Dot2TreeNodes> pair = it.next();
			
			// Analyze only internal nodes which have suffix links.
			if (pair.getValue().getClass().equals(Dot2TreeInnerNodesParent.class) 
					&& !((Dot2TreeInnerNodesParent) pair.getValue()).getAllSuffixLinks().isEmpty() ) {
				
				int suffixLink = ((Dot2TreeInnerNodesParent) pair.getValue()).getAllSuffixLinks().get(0);
				
				// Following a suffix link to the root is not allowed.
				if ( !(suffixLink == 1) ) {
					
					// Start the backwards iteration process to get the longest cumulative labels.
					this.suffixLinkSearchRes.add(backwardsIteration ( suffixLink, 
							pair.getValue().getEdgeLabel().length(),
							pair.getKey(),
							pair.getValue().getEdgeLabel()) 
							);
				}
			}
			
			// ATTENTION: ConcurrentModificationException is not of concern, as the 
			// elements of dot2TreeNodesMap will not be modified. Hence, the following command is not necessary.
			// it.remove();
		}	
	}
	
	/**
	 * This method follows the suffix links due to recursion.
	 * It returns a SuffixLinkNodes object which holds
	 * the start node, the end node, and the cumulative length of the 
	 * concatenated edge labels.
	 * @param suffixLink
	 * @param lastResult
	 * @param startNode
	 * @return SuffixLinkNodes object
	 */
	private SuffixLinkNodes backwardsIteration(int suffixLink, int lastResult, int startNode, String edgeLabel) {
		
		// If the suffix link node parent is the root node.
		// Or if the edgeLabel of the parent node is smaller than the defined threshold then stop the iteration
		// and do not add the current edgeLabel length to the results.
		
		if ( ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getAllSuffixLinks().get(0) == 1
				||	this.minAlpha > ((Dot2TreeInnerNodesParent) 
					this.dot2TreeNodesMap.get(suffixLink)).getEdgeLabel().length()) {
			// Retrieve the last node after following the suffix links bottom up.
						SuffixLinkNodes suffixLinkNodes = new SuffixLinkNodes(
								lastResult,
								startNode,
								((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getNodeNumber(),
								edgeLabel
								);
						
						return suffixLinkNodes;
		}
		// If the node to which the suffix link is pointing and the parent of the currentNode are the very same node.
		// Or if the suffix link node has no further suffix links.
		
		if ( ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(
					(((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getAllSuffixLinks().get(0))
					)).getNodeNumber() == 
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getParent()
				|| ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getAllSuffixLinks().isEmpty()
				)  {
			
			// Retrieve the last node after following the suffix links bottom up.
			SuffixLinkNodes suffixLinkNodes = new SuffixLinkNodes(
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getEdgeLabel().length() + lastResult,
					startNode,
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getNodeNumber(),
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getEdgeLabel() + "|" + edgeLabel);
			
			return suffixLinkNodes;
			
		} 
		
		// New concatenated edge label.
		String newEdgeLabel = ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getEdgeLabel() + "|" + edgeLabel;
		
		// Next recursion cycle.
		int newSuffixLink = ((Dot2TreeInnerNode) this.dot2TreeNodesMap.get(suffixLink)).getAllSuffixLinks().get(0);
		
		return backwardsIteration( newSuffixLink, 
				((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getEdgeLabel().length() + lastResult,
				startNode,
				newEdgeLabel);
	}
	
	private void sortSuffixRes () {
		
		// Create a custom comparator to sort the ArrayList "this.suffixLinkSearchRes" form the greatest
		// suffix path length to the smallest.
		class SuffixComparator implements Comparator<SuffixLinkNodes> {
			@Override
			public int compare(SuffixLinkNodes s1, SuffixLinkNodes s2) {
				if (s1.getSuffixLinkPathLen() > s2.getSuffixLinkPathLen()) {
					return -1;
				} else if (s1.getSuffixLinkPathLen() < s2.getSuffixLinkPathLen()) {
					return 1;
				}
				return 0;
			}
		}
		
		// Sort the ArrayList.
		Collections.sort(this.suffixLinkSearchRes, new SuffixComparator());
		
	}
	
	@Override
	public void applyProperties () throws Exception {
		super.setDefaultsIfMissing();
		
		// Apply own properties.
		if (this.getProperties().containsKey(PROPERTYKEY_MINALPHA))
			this.minAlpha = Integer.parseInt(this.getProperties().getProperty(
					PROPERTYKEY_MINALPHA));
		
		// Apply parent object's properties
		super.applyProperties();
	}
	
	// End methods.
	
}