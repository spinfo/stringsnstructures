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
	public static final String PROPERTYKEY_MAXCOMBINATORICS = "Minimal length for identical string alpha allowed";
	// End property keys.
	
	// Variables:
	
	// This variable defines the minimum length of an identical starting string alpha.
	private int maxTrials;
	
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
	
	// This ArrayList holds all motif candidates.
	private ArrayList <MotifCandidates> motifCandidatesRes;
	
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
		this.getPropertyDescriptions().put(this.PROPERTYKEY_MAXCOMBINATORICS, "Maximum parallel search strategies");
		
		// Initialize module specific fields.
		
		// Reverse order for the dot2TreeNodesMap to iterate in ascending order (not descending).
		this.dot2TreeNodesMap = new TreeMap <Integer, Dot2TreeNodes>(Collections.reverseOrder());
		
		this.gstXmlNodes = new TreeMap <Integer, GSTXmlNode>();
		
		// Property defaults.
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Motif Detection");
		this.getPropertyDefaultValues().put(this.PROPERTYKEY_MAXCOMBINATORICS, "4");
		
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
		 *     positions in the tree).
		 */

		// Initialize the TreeMap holding the results for the suffix link node search.
		this.suffixLinkSearchRes = new ArrayList <SuffixLinkNodes> ();
		
		// Iterate over all nodes.
		Iterator <Map.Entry<Integer,Dot2TreeNodes>> it = this.dot2TreeNodesMap.entrySet().iterator();
		
		while(it.hasNext()) {
			Map.Entry <Integer, Dot2TreeNodes> pair = it.next();
			
			// Analyze only internal nodes which have suffix links.
			if (pair.getValue().getClass().equals(Dot2TreeInnerNodesParent.class) 
					&& !((Dot2TreeInnerNodesParent) pair.getValue()).getAllSuffixLinks().isEmpty() ) {
				
				int suffixLink = ((Dot2TreeInnerNodesParent) pair.getValue()).getAllSuffixLinks().get(0);
				
				// Following a suffix link to the root is not allowed.
				if ( !(suffixLink == 1) ) {
					
					// Start the backwards iteration process to get the longest cumulative labels.
					// If the search fails continue with the next node.
					if (!backwardsIteration ( pair.getKey(), suffixLink) ) {
						continue;
					}
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
	 * @param startNode
	 * @return MotifCandidates object
	 */
	private Boolean backwardsIteration(int startNode, int suffixLink) {
		
		/*
		 * Pseudo code for the algorithm:
		 * 1.) If internal node N1 exist and has a suffix link (SL) node N2, follow it.
		 * 2.) Follow N2 to its parent P2. Follow N1 to its parent P1. 
		 * 3.) If the edge of P1 and P2 are equal and there is a SL from P1 to P2, or 
		 *     otherwise, define the edge as alpha.
		 *     If they are not equal follow their parent nodes as long as the edges of 
		 *     the parents have a greater length than defined by the minAlpha variable.
		 * 4.) If an alpha was found define delta and the N-set. Save this information as
		 *     MotifCandidate object.
		 * 5.) Continue steps 1 to 4 with the next internal node.
		 */
		
		// Step 1: If the edge label of the parent node and the parent node of the suffix link are identical
		//         report the sets. Otherwise continue iterating.
		if ( this.dot2TreeNodesMap.get(
				((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNode)).getParent())
				.getEdgeLabel().equals(
			this.dot2TreeNodesMap.get(
				((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getParent())
			.getEdgeLabel())
			) {
			// TODO: Stop iteration if sets are established.
		} else {
			// Follow the suffix link to the linked node.
			int [] linkedParents = this.followParents(startNode, suffixLink, 0);
			
			// Abort the search if no alpha could be determined.
			if (linkedParents[0] == 0 && linkedParents[1] == 0) {
				return false;
			} else {
				MotifCandidates newMotifCandidate = new MotifCandidates (
						(Dot2TreeInnerNodesParent)this.dot2TreeNodesMap.get(linkedParents[0]));
				
				// Do a string comparison for startNode and suffixLink to define delta and N-set.
				char [] startNodeChars = this.dot2TreeNodesMap.get(startNode).getEdgeLabel().toCharArray();
				char [] suffixLinkChars = this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().toCharArray();
				
				// Define the longest string.
				int maxChar;
				
				// Define the offSet between both strings.
				int offSet;
				ArrayList <Character> resultDelta = new ArrayList <Character> ();
				
				if (this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length() >
					this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length()) {
					maxChar = this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length();	
					offSet = this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length()
							- this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length();
				} 
				
				else if (this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length() ==
					this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length()) {
					maxChar = this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length();
					offSet = 0;
				} 
				
				else {
					maxChar = this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length();
					offSet = this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length()
							- this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length();
				}
							
				// Compare both Strings char by char from suffix to prefix.
				for (int i = maxChar; i > 1; i --) {
					if (startNodeChars[i] == suffixLinkChars[i]) {
						resultDelta.add(startNodeChars[i]);
					} else {
						
					}
				}
				
				newMotifCandidate.setDelta(resultDelta.toString());
			}
		}
		
		int newSuffixLink = ((Dot2TreeInnerNode) this.dot2TreeNodesMap.get(suffixLink)).getAllSuffixLinks().get(0);
		
		// Follow the suffix links and re-check the coherence between their respective parents.
		return backwardsIteration (suffixLink, newSuffixLink);
		
	}
	
	
	// TODO: Follow up to maximum of 4 recombinatoric events. Right now the algorithm follows only pairwise.
	private int [] followParents(int startNode, int suffixLink, int numberOfIteration) {
		
		int [] resultsArray = new int [2];
		if ( this.dot2TreeNodesMap.get(
				((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNode)).getParent())
				.getEdgeLabel().equals(
			this.dot2TreeNodesMap.get(
				((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getParent())
				.getEdgeLabel())
			) {
			
			resultsArray[0] = this.dot2TreeNodesMap.get(
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNode)).getParent()
					).getNodeNumber();
			resultsArray[1] = this.dot2TreeNodesMap.get(
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getParent()
					).getNodeNumber();
			return resultsArray;
			
		} else {
			
			// Check if all the combinatorial trials were used up.
			// Check whether the parents of startNode and suffixLink have parents which are not root. 
			// If they have root as parent or all trials were used up, return an "zero" array.
			if (numberOfIteration >= this.maxTrials 
				|| (this.dot2TreeNodesMap.get((
					(Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNode)).getParent()))
					))
					.getParent()).getNodeNumber() == 1
				&& this.dot2TreeNodesMap.get((
					(Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getParent()))
					))
					.getParent()).getNodeNumber() == 1)) {
				
				resultsArray[0] = 0;
				resultsArray[1] = 0;
				return resultsArray;
			}
	
			numberOfIteration ++;
			
			// Test the parent of parent of startNode versus the parent of suffixLink.
			if ( this.dot2TreeNodesMap.get((
					(Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNode)).getParent()))
					))
					.getParent())
					.equals(
				this.dot2TreeNodesMap.get(
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getParent())
					.getEdgeLabel())
				) {
				
				// Get the node number of the parent of the parent of startNode.
				resultsArray[0] = this.dot2TreeNodesMap.get((
						(Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(
						((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(
						((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNode)).getParent()))
						))
						.getParent())
						.getNodeNumber();
				
				// Get the node number of the parent of suffixLink.
				resultsArray[1] = this.dot2TreeNodesMap.get(
						((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getParent()
						).getNodeNumber();
				return resultsArray;
				
			}
			
			// Test the parent of the parent of suffixLink versus the parent of startNode.
			if ( this.dot2TreeNodesMap.get((
					(Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getParent()))
					))
					.getParent())
					.equals(
				this.dot2TreeNodesMap.get(
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNode)).getParent())
					.getEdgeLabel())
				) {
				
				// Get the node number of the parent of the parent of startNode.
				resultsArray[0] = this.dot2TreeNodesMap.get((
						(Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(
						((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(
						((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getParent()))
						))
						.getParent())
						.getNodeNumber();
				
				// Get the node number of the parent of suffixLink.
				resultsArray[1] = this.dot2TreeNodesMap.get(
						((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNode)).getParent()
						).getNodeNumber();
				return resultsArray;
				
			}
			
			// If none of the above is true continue with the next iteration.
			return this.followParents(startNode, suffixLink, numberOfIteration);
		}
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
		if (this.getProperties().containsKey(PROPERTYKEY_MAXCOMBINATORICS))
			this.maxTrials = Integer.parseInt(this.getProperties().getProperty(
					PROPERTYKEY_MAXCOMBINATORICS));
		
		// Apply parent object's properties
		super.applyProperties();
	}
	
	// End methods.
	
}