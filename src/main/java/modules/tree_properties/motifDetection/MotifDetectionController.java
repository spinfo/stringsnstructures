package modules.tree_properties.motifDetection;

//Java utility imports.
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * This module transverses a Generalized Suffix Tree (GST) bottom up
 * and detects nodes which are linked by suffix-links to one-another.
 * Such nodes which include a common starting edge (constant identical edge) and ...
 * 
 * @author christopher
 *
 */


public class MotifDetectionController extends ModuleImpl {
	
	// Enumerators:	
	private enum DotTags {
			ROOT, LEAFSTART, LEAFEND, INTERNAL, EDGE, SUFFIXLINK, UNDEFINED
	}
	// End enumerators.
	
	// Property keys:
	public static final String PROPERTYKEY_MAXCOMBINATORICS = "Maximum number of trials";
	
	public static final String PROPERTYKEY_MINALPHA = "Minimum length for alpha";
	
	public static final String PROPERTYKEY_MINDELTA = "Minim length for delta";
	// End property keys.
	
	// Variables:
	
	// This variable defines the minimum length of an identical starting string alpha.
	private int minAlpha;
	
	// This variable defines maximum amount of tries allowed to identify parents with common strings alpha.
	private int maxTrials;
	
	// This variable defines the minimum length of the string delta.
	private int minDeltaLen;
	
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
		
	// This ArrayList holds all motif candidates.
	private ArrayList <MotifCandidates> motifCandidatesRes;
	
	// Variables for alpha, delta and N-Set comparisons.
	private HashMap <String, CompareSets> deltaCompared;
	
	private HashMap <String, CompareSets> nSetCompared;
	
	// Variable holding each line of the input.
	private String inputString;
	
	// IDs for I/O pipelines.
	private final String INPUTDOTID = "dot input";
	private final String INPUTXMLID = "xml input";
	private final String OUTPUTID = "output";
		
	// End variables.
	
	// Constructors:
	public MotifDetectionController (CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super (callbackReceiver, properties);
		
		// Module description.
		this.setDescription("This module transverses a Generalized Suffix Tree (GST) bottom up<br>"
			+ "and detects nodes which are linked by suffix-links to one-another. <br>"
			+ "Such nodes which include a common starting edge (constant identical edge) and ...<br>"
			+ "<b>Requirements:</b><br>"
			+ "<em>pending...</em>");
		
		// Property descriptions.
		this.getPropertyDescriptions().put(PROPERTYKEY_MAXCOMBINATORICS, 
				"Maximal tries allowed to find linked parents with alpha edge.");
		this.getPropertyDescriptions().put(PROPERTYKEY_MINALPHA, 
				"Minimal length for identical string alpha allowed.");
		this.getPropertyDescriptions().put(PROPERTYKEY_MINDELTA, 
				"Minimal length for identical string delta allowed.");
		
		// Initialize module specific fields.
		
		this.deltaCompared = new HashMap <String, CompareSets> ();
		
		this.nSetCompared = new HashMap <String, CompareSets> ();
		
		// Reverse order for the dot2TreeNodesMap to iterate in ascending order (not descending).
		this.dot2TreeNodesMap = new TreeMap <Integer, Dot2TreeNodes>(Collections.reverseOrder());
		
		this.gstXmlNodes = new TreeMap <Integer, GSTXmlNode>();
		
		// Property defaults.
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Motif Detection");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MAXCOMBINATORICS, "16");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MINALPHA, "3");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MINDELTA, "2");
		
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
		
		// Write the output in table (tsv) form.
		if (this.getOutputPorts().get(OUTPUTID).isConnected()) 
			this.writeOutput();
		
		// Close outputs.
		this.closeAllOutputs();
		
		// Success.
		return true;
	}
	
	private void writeOutput () throws Exception {
		String header = "alpha\tdelta\talphaSet\tN-Set\tdeltaSet\n";
		this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(header);
		Iterator <MotifCandidates> it = this.motifCandidatesRes.iterator();
		while (it.hasNext()) {
			MotifCandidates motifCandidateRes = it.next();
			
			// Print alpha.
			String line = motifCandidateRes.getAlphaEdge() + "\t";
			
			// Print delta.
			
			for (Map.Entry<Integer, String> delta : motifCandidateRes.getDelta().entrySet()) {
				line += delta.getKey() + ";";
				line += delta.getValue() + ";";
			}
			line +=  "\t";
			
			// Print alpha set.
			Iterator <Map.Entry<Integer, Dot2TreeInnerNodesParent>> alphaSetIt = 
					motifCandidateRes.getAlphaSet().entrySet().iterator();
			
			while (alphaSetIt.hasNext()) {
				Map.Entry<Integer, Dot2TreeInnerNodesParent> alphaSetPair = alphaSetIt.next();
				line += alphaSetPair.getKey() + ";";
				line += alphaSetPair.getValue().getEdgeLabel() + ";";
			}
			line += "\t";
			
			// Print N-set.
			Iterator <Map.Entry<Integer, String>> nSetIt = motifCandidateRes.getNset().entrySet().iterator();
						
			while (nSetIt.hasNext()) {
				Map.Entry<Integer, String> nSetPair = nSetIt.next();
				line += nSetPair.getKey() + ";";
				line += nSetPair.getValue() + ";";
			}
			line += "\t";
			
			// Print delta set.
			Iterator <Map.Entry<Integer, Dot2TreeInnerNodesParent>> deltaSetIt = 
					motifCandidateRes.getDeltaSet().entrySet().iterator();
			
			while (deltaSetIt.hasNext()) {
				Map.Entry<Integer, Dot2TreeInnerNodesParent> deltaSetPair = deltaSetIt.next();
				line += deltaSetPair.getKey() + ";";
				line += deltaSetPair.getValue().getEdgeLabel() + ";";
			}
			line += "\n";
			
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
		this.motifCandidatesRes = new ArrayList <MotifCandidates> ();
		
		// Iterate over all nodes.
		Iterator <Map.Entry<Integer,Dot2TreeNodes>> it = this.dot2TreeNodesMap.entrySet().iterator();
		
		while(it.hasNext()) {
			Map.Entry <Integer, Dot2TreeNodes> pair = it.next();
			
			// Analyze only internal nodes which have suffix links.
			if (pair.getValue().getClass().equals(Dot2TreeInnerNodesParent.class) 
					&& !((Dot2TreeInnerNodesParent) pair.getValue()).getAllSuffixLinks().isEmpty() ) {
				
				int suffixLink = ((Dot2TreeInnerNodesParent) pair.getValue()).getAllSuffixLinks().get(0);
				
				// Following a suffix link to the root is not allowed.
				if ( !(suffixLink == 1) 
						&& this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length() >= this.minAlpha) {
					
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
		
		// Get parents of the startNode and the suffixLink.
		int startNodeParent = ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNode)).getParent();
		int suffixLinkParent = ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getParent();
		
		// Check whether both parents have the same edgeLabels and whether they are directly linked. 
		// TODO: Make sure non of the parents can't be the root node.
		if ( this.dot2TreeNodesMap.get(startNodeParent).getEdgeLabel().equals(
			this.dot2TreeNodesMap.get(suffixLinkParent).getEdgeLabel())
			&& !((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeParent)).getAllSuffixLinks().isEmpty()
			&& (
					(((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeParent)).getAllSuffixLinks().get(0) 
							== suffixLinkParent)
					|| ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLinkParent)).getAllSuffixLinks().get(0) 
						== startNodeParent)
			){
			
			// Now explore the other descendants of startNodeParent. Are they also internal nodes? Are they linked?
			
			// Save all children of suffixLinkParent which are linked to children of startNodeParent.
			ArrayList <Integer> suffixLinkParentChildren = new ArrayList <Integer> (); 
			
			// Save all children of startNodeParent which are linked to the children of suffixLinkParent.
			ArrayList <Integer> startNodeParentChildren = new ArrayList <Integer> ();
			
			// Iterate over the children.
			Iterator<Map.Entry<Integer, Dot2TreeInnerNode>> childIt = 
					((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeParent))
					.getAllChildNodes().entrySet().iterator();
			
			while (childIt.hasNext()) {
				Map.Entry<Integer, Dot2TreeInnerNode> childPair = childIt.next();
				
				// Check whether any child of suffixLinkParent is linked to any child of startNodeParent.
				
				if ( ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLinkParent))
						.getAllChildNodes().containsKey(childPair.getValue().getAllSuffixLinks().get(0))) {
					
					suffixLinkParentChildren.add(childPair.getValue().getAllSuffixLinks().get(0));
					startNodeParentChildren.add(childPair.getKey());
				}
			}
			
			// Check whether more than one linked child pair was retrieved and the continue otherwise stop the search.
			if (suffixLinkParentChildren.size() < 2 && startNodeParentChildren.size() < 2) 
				return false;
			
			// If everything went well thus far, create a new motif Candidate.
			MotifCandidates newMotifCandidate = new MotifCandidates (
					(Dot2TreeInnerNodesParent)this.dot2TreeNodesMap.get(startNodeParent),
					(Dot2TreeInnerNodesParent)this.dot2TreeNodesMap.get(suffixLinkParent));
			
			// Do a string comparison for startNode and suffixLink to define delta and N-set.
			char [] startNodeChars = this.dot2TreeNodesMap.get(startNode).getEdgeLabel().toCharArray();
			char [] suffixLinkChars = this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().toCharArray();
			
			// Define the longest string.
			int maxChar;
			
			// Define the offSet between both strings.
			int startNodeOffSet;
			int suffixLinkOffSet;
			
			ArrayList <Character> resultDelta = new ArrayList <Character> ();
			
			if (this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length() >
				this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length()) {
				maxChar = this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length();	
				suffixLinkOffSet = this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length()
						- this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length();
				startNodeOffSet = 0;
			} 
			
			else if (this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length() ==
				this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length()) {
				maxChar = this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length();
				startNodeOffSet = 0;
				suffixLinkOffSet = 0;
			} 
			
			else {
				maxChar = this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length();
				startNodeOffSet = this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length()
						- this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length();
				suffixLinkOffSet = 0;
			}
			
			// Define the N-Set.
			ArrayList <Character> startNodeNSet = new ArrayList <Character> ();
			ArrayList <Character> suffixLinkNSet = new ArrayList <Character> ();
						
			// Compare both Strings char by char from suffix to prefix.
			for (int i = maxChar - 1 ; i >= 0; i --) {
				if (i >= startNodeOffSet
					&& i >= suffixLinkOffSet
					&& startNodeChars[i - startNodeOffSet] == suffixLinkChars[i - suffixLinkOffSet]) {
					resultDelta.add(0, startNodeChars[i - startNodeOffSet]);
				} else {
					if (i - startNodeOffSet >= 0) {
						startNodeNSet.add(0, startNodeChars[i - startNodeOffSet]);
					} 
					if (i - suffixLinkOffSet >= 0)  {
						suffixLinkNSet.add(0, suffixLinkChars[i - suffixLinkOffSet]);
					}
				}
			}
			
			// Compare the retrieved delta with a cross comparison of two children of 
			// startNodeParent and suffixLinkParent.
			this.compareSets (startNodeParentChildren, suffixLinkParentChildren, resultDelta, startNodeNSet, suffixLinkNSet);
			
			// Delete all entries from the fields this.deltaCompared and this.nSetCompared which have less than 2 entries.
			// 2 entries should equal two pairs. TODO: Check whether this is truly applicable.
			
			Iterator <Map.Entry<String, CompareSets>> deltaIt = this.deltaCompared.entrySet().iterator();
			
			while (deltaIt.hasNext()) {
				Map.Entry<String, CompareSets> deltaEntry = deltaIt.next();
				if (deltaEntry.getValue().getOccurences() >= 2) {
					for (Map.Entry<Integer, String> entry : deltaEntry.getValue().getAllNodeStrings().entrySet()) {
						// Set the delta for the motif candidate.
						newMotifCandidate.setDelta(entry.getKey(), entry.getValue());
						// Add delta set to motif candidate.
						newMotifCandidate.putDeltaSet(entry.getKey(), 
								((Dot2TreeInnerNodesParent)this.dot2TreeNodesMap.get(entry.getKey())));
					}
				} else { 
					deltaIt.remove();
				}
			}
			
			Iterator <Map.Entry<String, CompareSets>> nSetIt = this.nSetCompared.entrySet().iterator();
			
			while (nSetIt.hasNext()) {
				Map.Entry<String, CompareSets> nSetEntry = nSetIt.next();
				if (nSetEntry.getValue().getOccurences() >= 2) {
					for (Map.Entry<Integer, String> entry : nSetEntry.getValue().getAllNodeStrings().entrySet()) {
						// Add the N-Sets to the motif candidate.
						newMotifCandidate.setNSet(entry.getKey(), entry.getValue());
					}
				} else {
					nSetIt.remove();
				}
					
			}
			
			// Remove the current results from the fields this.deltaCompared and this.nSetCompared.
			this.deltaCompared.clear();
			this.nSetCompared.clear();
			
			// Add the retrieved motif candidate to the list.
			this.motifCandidatesRes.add(newMotifCandidate);
			
			// Successful identification of a motif candidate.
			return true;
			
		} else {
			// Follow the suffix link to the linked node.
			int [] linkedParents = this.followParents(startNode, suffixLink, 0);
			
			// Abort the search if no alpha could be determined.
			if (linkedParents[0] == 0 && linkedParents[1] == 0) {
				return false;
			} else if (linkedParents[0] != linkedParents[1]) {
								
				// Now explore the other descendants of startNodeParent. Are they also internal nodes? Are they linked?
				
				// Save all children of suffixLinkParent which are linked to children of startNodeParent.
				ArrayList <Integer> suffixLinkParentChildren = new ArrayList <Integer> (); 
				
				// Save all children of startNodeParent which are linked to the children of suffixLinkParent.
				ArrayList <Integer> startNodeParentChildren = new ArrayList <Integer> ();
				
				// Iterate over the children.
				Iterator<Map.Entry<Integer, Dot2TreeInnerNode>> childIt = 
						((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeParent))
						.getAllChildNodes().entrySet().iterator();
				
				while (childIt.hasNext()) {
					Map.Entry<Integer, Dot2TreeInnerNode> childPair = childIt.next();
					
					// Check whether any child of suffixLinkParent is linked to any child of startNodeParent.
					if ( ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLinkParent))
						.getAllChildNodes().containsKey(childPair.getValue().getAllSuffixLinks().get(0)) ) {
						suffixLinkParentChildren.add(childPair.getValue().getAllSuffixLinks().get(0));
						startNodeParentChildren.add(childPair.getKey());
					}
				}
				
				// Check whether more than one linked child pair was retrieved and the continue otherwise stop the search.
				if (suffixLinkParentChildren.size() < 2 && startNodeParentChildren.size() < 2) 
					return false;
				
				// If everything went well thus far, create a new motif Candidate.
				MotifCandidates newMotifCandidate = new MotifCandidates (
						(Dot2TreeInnerNodesParent)this.dot2TreeNodesMap.get(linkedParents[0]),
						(Dot2TreeInnerNodesParent)this.dot2TreeNodesMap.get(linkedParents[1]));
				
				// Do a string comparison for startNode and suffixLink to define delta and N-set.
				char [] startNodeChars = this.dot2TreeNodesMap.get(startNode).getEdgeLabel().toCharArray();
				char [] suffixLinkChars = this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().toCharArray();
				
				// Define the longest string.
				int maxChar;
				
				// Define the offSet between both strings.
				int startNodeOffSet;
				int suffixLinkOffSet;
				
				ArrayList <Character> resultDelta = new ArrayList <Character> ();
				
				if (this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length() >
					this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length()) {
					maxChar = this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length();	
					suffixLinkOffSet = this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length()
							- this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length();
					startNodeOffSet = 0;
				} 
				
				else if (this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length() ==
					this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length()) {
					maxChar = this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length();
					
					startNodeOffSet = 0;
					suffixLinkOffSet = 0;
				} 
				
				else {
					maxChar = this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length();
					startNodeOffSet = this.dot2TreeNodesMap.get(suffixLink).getEdgeLabel().length()
							- this.dot2TreeNodesMap.get(startNode).getEdgeLabel().length();
					suffixLinkOffSet = 0;
				}
				
				// Define the N-Set.
				ArrayList <Character> startNodeNSet = new ArrayList <Character> ();
				ArrayList <Character> suffixLinkNSet = new ArrayList <Character> ();
							
				// Compare both Strings char by char from suffix to prefix.
				for (int i = maxChar - 1 ; i >= 0; i --) {
					if (i >= startNodeOffSet
						&& i >= suffixLinkOffSet
						&& startNodeChars[i - startNodeOffSet] == suffixLinkChars[i - suffixLinkOffSet]) {
						resultDelta.add(0, startNodeChars[i - startNodeOffSet]);
					} else {
						if (i - startNodeOffSet >= 0) {
							startNodeNSet.add(0, startNodeChars[i - startNodeOffSet]);
						} 
						if (i - suffixLinkOffSet >= 0)  {
							suffixLinkNSet.add(0, suffixLinkChars[i - suffixLinkOffSet]);
						}
					}
				}
				
				// Compare the retrieved delta with a cross comparison of two children of 
				// startNodeParent and suffixLinkParent.
				this.compareSets (startNodeParentChildren, suffixLinkParentChildren, resultDelta, startNodeNSet, suffixLinkNSet);
				
				// Delete all entries from the fields this.deltaCompared and this.nSetCompared which have less than 2 entries.
				// 2 entries should equal two pairs. TODO: Check whether this is truly applicable.
				
				Iterator <Map.Entry<String, CompareSets>> deltaIt = this.deltaCompared.entrySet().iterator();
				
				while (deltaIt.hasNext()) {
					Map.Entry<String, CompareSets> deltaEntry = deltaIt.next();
					if (deltaEntry.getValue().getOccurences() > 2) {
						for (Map.Entry<Integer, String> entry : deltaEntry.getValue().getAllNodeStrings().entrySet()) {
							// Set the delta for the motif candidate.
							newMotifCandidate.setDelta(entry.getKey(), entry.getValue());
							// Add delta set to motif candidate.
							newMotifCandidate.putDeltaSet(entry.getKey(), 
									((Dot2TreeInnerNodesParent)this.dot2TreeNodesMap.get(entry.getKey())));
						}
					} else { 
						deltaIt.remove();
					}
				}
				
				Iterator <Map.Entry<String, CompareSets>> nSetIt = this.nSetCompared.entrySet().iterator();
				
				while (nSetIt.hasNext()) {
					Map.Entry<String, CompareSets> nSetEntry = nSetIt.next();
					if (nSetEntry.getValue().getOccurences() > 2) {
						for (Map.Entry<Integer, String> entry : nSetEntry.getValue().getAllNodeStrings().entrySet()) {
							// Add the N-Sets to the motif candidate.
							newMotifCandidate.setNSet(entry.getKey(), entry.getValue());
						}
					} else {
						nSetIt.remove();
					}
						
				}
				
				// Remove the current results from the fields this.deltaCompared and this.nSetCompared.
				this.deltaCompared.clear();
				this.nSetCompared.clear();
				
				// Add the retrieved motif candidate to the list.
				this.motifCandidatesRes.add(newMotifCandidate);
				
				// Successful identification of a motif candidate.
				this.motifCandidatesRes.add(newMotifCandidate);
				
				// Successful identification of a motif candidate.
				return true;
			}
		}
		
		int newSuffixLink;
		
		if (((Dot2TreeInnerNode) this.dot2TreeNodesMap.get(suffixLink)).getAllSuffixLinks().get(0) != 1) {
			newSuffixLink = ((Dot2TreeInnerNode) this.dot2TreeNodesMap.get(suffixLink)).getAllSuffixLinks().get(0);	
		} else {
			return false;
		}
		
		
		// Follow the suffix links and re-check the coherence between their respective parents.
		return backwardsIteration (suffixLink, newSuffixLink);
		
	}
	
	
	// TODO: Follow up to maximum of 4 recombinatoric events. Right now the algorithm follows only pairwise.
	private int [] followParents(int startNode, int suffixLink, int numberOfIteration) {
		
		// Define parents of the current suffixNode and the current suffixLink.
		int startNodeParent = ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNode)).getParent();
		int suffixLinkParent = ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getParent();
		
		// Define returnArray to give back the common ancestors of the previous startNode and the suffixLink.
		int [] resultsArray = new int [2];
		
		// If the startNodeParent and the suffixLinkParent share the same edgeLabel and are linked then
		// return this pair. Otherwise continue searching as long as the edgeLabels for startNode parents
		// are greater or equal to this.minAlpha.
		
		if ( this.dot2TreeNodesMap.get(
				((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNode)).getParent())
				.getEdgeLabel().equals(
			this.dot2TreeNodesMap.get(
				((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLink)).getParent())
				.getEdgeLabel()) 
			&& !((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeParent)).getAllSuffixLinks().isEmpty()
			&& ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeParent)).getAllSuffixLinks().get(0) 
				== suffixLinkParent
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
					
			int startNodeParentParent = ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeParent))
					.getParent();
			
			int suffixLinkParentParent = ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLinkParent))
					.getParent();
			
			// If the trials are used up, or the edgeLabel becomes shorter than this.minAlpha or 
			// either the suffixLinkParentParent equals root or the startNodeParentParent equals root
			// then return an empty array to signal that the search was not successful.
			
			if (numberOfIteration > this.maxTrials
				|| ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNode))
					.getEdgeLabel().length() < this.minAlpha
				|| (startNodeParentParent == 1
				||  suffixLinkParentParent == 1)
				|| (startNodeParentParent == 0
				||  suffixLinkParentParent == 0)
				) {
				
				resultsArray[0] = 0;
				resultsArray[1] = 0;
				return resultsArray;
			}
			
			// Continue searching and keep track of the iterations thus far.
			numberOfIteration ++;
			
			
			// Test first the least time consuming options: 
			// startNodeParent vs suffixLink and startNode vs suffixLinkParent.
			
			// Test the parent of parent of startNode versus the parent of the suffixLink.
			if ( this.dot2TreeNodesMap.get(startNodeParentParent).getEdgeLabel()
					.equals(
				this.dot2TreeNodesMap.get(suffixLinkParent)
					.getEdgeLabel())
				) {
				
				// Get the node number of the parent of the parent of the startNode.
				resultsArray[0] = this.dot2TreeNodesMap.get(startNodeParentParent)
						.getNodeNumber();
				
				// Get the node number of the parent of suffixLink.
				resultsArray[1] = this.dot2TreeNodesMap.get(
						suffixLinkParent).getNodeNumber();
				return resultsArray;
				
			}
			
			// Test the parent of the parent of suffixLink versus the parent of startNode.
			if ( this.dot2TreeNodesMap.get(suffixLinkParentParent).getEdgeLabel()
					.equals(
				this.dot2TreeNodesMap.get(startNodeParent)
					.getEdgeLabel())
				) {
				
				// Get the node number of the parent of the parent of the suffixLink.
				resultsArray[0] = this.dot2TreeNodesMap.get(suffixLinkParentParent)
						.getNodeNumber();
				
				// Get the node number of the parent of startNode.
				resultsArray[1] = this.dot2TreeNodesMap.get(startNodeParent).getNodeNumber();
				return resultsArray;
				
			}
			
			/* 
			 * Test all combinations pairwise. 
			 * 1.) First move upwards to the parents of the startNode and combine them with suffixLink.
			 * 2.) If no target pair was retrieved move upwards the path of the suffixLink.
			 * 3.) If still no target pair was retrieved, update the current startNodeParentParent to be
			 *     the future startNodeParent. Also update the suffixLinkParentParent to be the 
			 *     suffixLinkParent. Continue with step 1.)
			 *     
			 * Continue this procedure until either any of the above described failure requirements are met
			 * or an startNodeParent-suffixLinkParent pair was found.
			 */
			
			// Step 1.)
			
			// Define a new parent pair.
			ParentPair newPair;
			
			newPair = this.followStartNodeParents(startNode, startNodeParentParent, 2, suffixLink, suffixLinkParent, 1);
			
			// If the search failed delete newPair and continue with step 2.
			if (!newPair.getOutcome())
				newPair = null;	
			else {
				resultsArray[0] = newPair.getStartNodeAncestor();
				resultsArray[1] = newPair.getSuffixLinkAncestor();
				return resultsArray;
			}
			
			// Step 2.)
			newPair = this.followSuffixLinkParents(startNode, startNodeParent, 1, suffixLink, suffixLinkParentParent, 2);
			
			// If the search still failed delete newPair and continue with step 3.
			if (!newPair.getOutcome())
				newPair = null;	
			else {
				resultsArray[0] = newPair.getStartNodeAncestor();
				resultsArray[1] = newPair.getSuffixLinkAncestor();
				return resultsArray;
			}
			
			// Step 3.)
			startNodeParent = startNodeParentParent;
			suffixLinkParent = suffixLinkParentParent;
			
			// If none of the above is true continue with the next iteration.
			
			
			
			// Check if parent of parent of startNode is root.
			if ( startNodeParentParent == 1 ) {
				resultsArray[0] = 0;
				resultsArray[1] = 0;
				return resultsArray;
			}
			
			return this.followParents(startNodeParent, suffixLinkParent, numberOfIteration);
		}
	}
	
	private ParentPair followStartNodeParents(int startNode, int startNodeParentParent, int startNodeDistance, 
			int suffixLink, int suffixLinkParent, int suffixLinkDistance) {
		
		// Get startNode ancestor.
		int startNodeAncestor = ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeParentParent)).getParent();
		
		// If startNodeParent has not suffix link, skip this parent.
		if ( ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeAncestor)).getAllSuffixLinks().isEmpty() )  {
			startNodeAncestor = ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeAncestor)).getParent();
		}
		
		// If the edgeLabel of the startNodeAncestor is smaller than alphaMin abort the iterations
		// by returning an empty pair.
		if (
				startNodeAncestor <= 1
				|| ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeAncestor))
					.getEdgeLabel().length() < this.minAlpha
			) {
			
			ParentPair newPair =  new ParentPair (startNode, startNodeAncestor, startNodeDistance, 
					suffixLink, suffixLinkParent, suffixLinkDistance);
			
			// Search failed, hence outcome is false.
			newPair.setOutcome(false);
			
			return newPair;
		}
				
		// If the startNodeAncestor and the suffixLinkParent share the same edgeLabel and are linked
		// then return the identified pair.
		if (
				((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeAncestor)).getEdgeLabel()
					.equals(
							((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLinkParent)).getEdgeLabel()	
				)
					
			&&	((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeAncestor))
					.getAllSuffixLinks().get(0)
				== suffixLinkParent
			) {
			
			ParentPair newPair =  new ParentPair (startNode, startNodeAncestor, startNodeDistance, 
					suffixLink, suffixLinkParent, suffixLinkDistance);
			
			// Search was a success, hence outcome is true.
			newPair.setOutcome(true);
			
			return newPair;
		}
		
		// Nothing found thus initiate the next iteration. Increment the distance between startNode 
		// and startNodeAncestor by 1 internal node.
		startNodeDistance ++;
		
		return followStartNodeParents (startNode, startNodeAncestor, startNodeDistance, 
				suffixLink, suffixLinkParent, suffixLinkDistance);
		
	}
	
	private ParentPair followSuffixLinkParents(int startNode, int startNodeParent, int startNodeDistance, 
			int suffixLink, int suffixLinkParentParent, int suffixLinkDistance) {
			
		// Get suffixLinkNode ancestor.
		int suffixLinkAncestor = ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLinkParentParent)).getParent();
		
		// If startNodeParent has not suffix link, skip this parent.
		if ( ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLinkAncestor)).getAllSuffixLinks().isEmpty() )  {
			suffixLinkAncestor = ((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLinkAncestor)).getParent();
		}
		
		// If the edgeLabel of the suffixLinkAncestor is smaller than alphaMin abort the iterations
		// by returning an empty pair.
		if (
				suffixLinkAncestor <= 1
				||	((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLinkAncestor))
					.getEdgeLabel().length() < this.minAlpha
			) {
			
			ParentPair newPair =  new ParentPair (startNode, startNodeParent, startNodeDistance, 
					suffixLink, suffixLinkAncestor, suffixLinkDistance);
			
			// Search failed, hence outcome is false.
			newPair.setOutcome(false);
			
			return newPair;
		}
		
		// If the suffixLinkAncestor and the startNodeParent share the same edgeLabel and are linked
		// then return the identified pair.
		if (
				((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(suffixLinkAncestor)).getEdgeLabel()
					.equals(
							((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeParent)).getEdgeLabel()	
				)
					
			&&	((Dot2TreeInnerNodesParent) this.dot2TreeNodesMap.get(startNodeParent))
					.getAllSuffixLinks().get(0)
				== suffixLinkAncestor
			) {
			
			ParentPair newPair =  new ParentPair (startNode, startNodeParent, startNodeDistance, 
					suffixLink, suffixLinkAncestor, suffixLinkDistance);
			
			// Search was a success, hence outcome is true.
			newPair.setOutcome(true);
			
			return newPair;
		}
		
		// Nothing found thus initiate the next iteration. Increment the distance between startNode 
		// and startNodeAncestor by 1 internal node.
		suffixLinkDistance ++;
		
		return followStartNodeParents (startNode, startNodeParent, startNodeDistance, 
				suffixLink, suffixLinkAncestor, suffixLinkDistance);
		
	}
	
	private void compareSets (ArrayList <Integer> startNodeParentChildren, 
			ArrayList <Integer> suffixLinkParentChildren, ArrayList <Character> resultDelta, 
			ArrayList <Character> startNodeNSet, ArrayList <Character> suffixLinkNSet) {
				
		for (int child : startNodeParentChildren) {
			ArrayList <Character> childEdgeLabel = new ArrayList <Character>();
			
			// Save the edgeLabel of the child as Character ArrayList.
			 for (char j : this.dot2TreeNodesMap.get(child).getEdgeLabel().toCharArray())
				 childEdgeLabel.add(j);
			
			// Check whether the last character of resultDelta and childEdgeLabel are identical.
			// Check whether resultDelta is longer than childEdgeLabel.
			// Continue with the next child if they are not.
			if ( !childEdgeLabel.get(childEdgeLabel.size() - 1)
					.equals(resultDelta.get(resultDelta.size() - 1)) 
				|| resultDelta.size() > childEdgeLabel.size())
				continue;
			
			// Define offset for childEdgeLabel.
			int deltaOffSet = childEdgeLabel.size() - resultDelta.size(); 
			
			// Define offset for startNodeNSet.
			//int startNodeNSetOffSet = childEdgeLabel.size() - startNodeNSet.size();
			
			// Define a new ArrayLists which holds temporary results.
			ArrayList <Character> newDelta = new ArrayList <Character> ();
			ArrayList <Character> newStartNodeNSet = new ArrayList <Character> ();
			
			// Compare delta and the edgeLabel from last to first element character by character.
			for (int j = childEdgeLabel.size() - 1; j >= 0; j --) {
				if (j - deltaOffSet >= 0 
						&& resultDelta.get(j - deltaOffSet).equals(childEdgeLabel.get(j)))
					newDelta.add(0, resultDelta.get(j - deltaOffSet));
				
				// The newDelta must have a length of at least 2 characters.
				// Compare the startNodeNSet with the childEdgeLabel. Integrate 'N's to show differences.
				else if (j - deltaOffSet < 0 && newDelta.size() >= this.minDeltaLen) {
					if (startNodeNSet.isEmpty() || !startNodeNSet.get(j).equals(childEdgeLabel.get(j))) {
						newStartNodeNSet.add(0, 'N');
					}
					else if (startNodeNSet.get(j).equals(childEdgeLabel.get(j))) {
						newStartNodeNSet.add(0, childEdgeLabel.get(j));
					}
				} else if ( !resultDelta.get(j - deltaOffSet).equals(childEdgeLabel.get(j)) ) {
					if (startNodeNSet.contains(j) && startNodeNSet.get(j).equals(childEdgeLabel.get(j))) {
						newStartNodeNSet.add(0, childEdgeLabel.get(j));
					} else if (!startNodeNSet.contains(j)) {
						newStartNodeNSet.add(0, 'N');
					}
				}
			}
			
			// Add the newStartNodeNSet and the newDelta to this.deltaCompared and nSetCompared.
			
			if (this.deltaCompared.containsKey(newDelta.toString())) 
				this.deltaCompared.get(newDelta.toString()).increOccur();
			else 
				this.deltaCompared.put(newDelta.toString(), 
						new CompareSets(newDelta.toString(), child, 1) );
			
			if (this.nSetCompared.containsKey(newStartNodeNSet.toString()))
				this.nSetCompared.get(newStartNodeNSet.toString()).increOccur();
			else 
				this.nSetCompared.put(newStartNodeNSet.toString(), 
						new CompareSets(newStartNodeNSet.toString(), child, 1) );
		}
		
		for (int child : suffixLinkParentChildren) {
			ArrayList <Character> childEdgeLabel = new ArrayList <Character>();
			
			// Save the edgeLabel of the child as Character ArrayList.
			 for (char j : this.dot2TreeNodesMap.get(child).getEdgeLabel().toCharArray())
				 childEdgeLabel.add(j);
			
			// Check whether the last character of resultDelta and childEdgeLabel are identical.
			// Check whether resultDelta is longer than childEdgeLabel.
			// Continue with the next child if they are not.
			if ( !childEdgeLabel.get(childEdgeLabel.size() - 1 )
					.equals(resultDelta.get(resultDelta.size() - 1)) 
				|| resultDelta.size() > childEdgeLabel.size())
				continue;
			
			// Define offset for childEdgeLabel.
			int deltaOffSet = childEdgeLabel.size() - resultDelta.size(); 
			
			// Define offset for startNodeNSet.
			// int nSetOffSet = childEdgeLabel.size() - suffixLinkNSet.size();
			
			// Define a new ArrayLists which holds temporary results.
			ArrayList <Character> newDelta = new ArrayList <Character> ();
			ArrayList <Character> newSuffixLinkNSet = new ArrayList <Character> ();
			
			// Compare delta and the edgeLabel from last to first element character by character.
			for (int j = childEdgeLabel.size() - 1; j >= 0; j --) {
				if (j - deltaOffSet >= 0 
						&& resultDelta.get(j - deltaOffSet).equals(childEdgeLabel.get(j)))
					newDelta.add(0, resultDelta.get(j -  deltaOffSet));
				
				// The newDelta must have a length of at least 2 characters.
				// Compare the startNodeNSet with the childEdgeLabel. Integrate 'N's to show differences.
				else if (j - deltaOffSet < 0 && newDelta.size() >= this.minDeltaLen) {
					if (suffixLinkNSet.isEmpty() || !suffixLinkNSet.get(j).equals(childEdgeLabel.get(j))) {
						newSuffixLinkNSet.add(0, 'N');
					}
					else if (suffixLinkNSet.get(j).equals(childEdgeLabel.get(j))) {
						newSuffixLinkNSet.add(0, childEdgeLabel.get(j));
					}
				} else if ( !resultDelta.get(j - deltaOffSet).equals(childEdgeLabel.get(j)) ) {
					if (suffixLinkNSet.contains(j) && suffixLinkNSet.get(j).equals(childEdgeLabel.get(j))) {
						newSuffixLinkNSet.add(0, childEdgeLabel.get(j));
					} else if (!suffixLinkNSet.contains(j)) {
						newSuffixLinkNSet.add(0, 'N');
					}
				}
			}
			
			// Add the newStartNodeNSet and the newDelta to this.deltaCompared and this.nSetCompared.
			if (this.deltaCompared.containsKey(newDelta.toString())) 
				this.deltaCompared.get(newDelta.toString()).increOccur();
			else 
				this.deltaCompared.put(newDelta.toString(), 
						new CompareSets(newDelta.toString(), child, 1) );
			
			
			if (this.nSetCompared.containsKey(newSuffixLinkNSet.toString()))
				this.nSetCompared.get(newSuffixLinkNSet.toString()).increOccur();
			else 
				this.nSetCompared.put(newSuffixLinkNSet.toString(), 
						new CompareSets(newSuffixLinkNSet.toString(), child, 1) );
		}
	}
	
	@Override
	public void applyProperties () throws Exception {
		super.setDefaultsIfMissing();
		
		// Apply own properties.
		if (this.getProperties().containsKey(PROPERTYKEY_MAXCOMBINATORICS))
			this.maxTrials = Integer.parseInt(this.getProperties().getProperty(
					PROPERTYKEY_MAXCOMBINATORICS));
		
		if (this.getProperties().containsKey(PROPERTYKEY_MINALPHA))
			this.minAlpha = Integer.parseInt(this.getProperties().getProperty(
					PROPERTYKEY_MINALPHA));
		
		if (this.getProperties().containsKey(PROPERTYKEY_MINDELTA))
			this.minDeltaLen = Integer.parseInt(this.getProperties().getProperty(
					PROPERTYKEY_MINDELTA));
		
		// Apply parent object's properties
		super.applyProperties();
	}
	
	// End methods.
	
}