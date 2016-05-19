package modules.dot2tree;

// Java utility imports.
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;

// Java I/O imports.
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.PipedInputStream;

//Workbench specific imports.
import modules.CharPipe;
import modules.BytePipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import common.parallelization.CallbackReceiver;

// Workbench GSTXmlNode import.
import common.GSTXmlStreamReader;
import models.GSTXmlNode;

/**
 * This module reads plain text dot format from I/O pipe.
 * It parses form the obtained information inner nodes, leaf nodes, 
 * edges and suffix links. The output will be a (generalized) suffix tree.
 * 
 * This module writes output in form of nested objects representing the
 * tree via a byte pipe. 
 * 
 * Alternatively, output can also be compacted in form of JSON format by 
 * usage of the google GSON library.
 * 
 * @author Christopher Kraus
 * 
 */

public class Dot2TreeController extends ModuleImpl {
	// Enumerators:
	
	private enum DotTags {
			ROOT, LEAFSTART, LEAFEND, INTERNAL, EDGE, SUFFIXLINK, UNDEFINED
	}
	// End enumerators.
	
	// Property keys:
		/* Currently additional property keys not required. */
	// End property keys.
	
	// Variables:
	
	// Dot document status.
	DotTags DotStat = DotTags.UNDEFINED;
	
	// Dot2TreeNodes root object.
	// Create the root of the tree and incorporate all nodes beneath.
	Dot2TreeInnerNode rootNode;
	
	// GSTXmlStreamReader object.
	GSTXmlStreamReader treeXmlStreamReader;
	
	// GST XML input stream.
	private PipedInputStream suffixTreeInStream;
	
	// GST XML TreeMap holding each node.
	private TreeMap <Integer, GSTXmlNode> gstXmlNodes;
	
	// TreeMap to save all found nodes in order of their node 
	// numbers in form of Dot2TreeNodes objects.
	private TreeMap <Integer, Dot2TreeNodes> dot2TreeNodesMap;
	
	// Variable holding each line of the input.
	private String inputString;
	
	// IDs for I/O pipelines.
	private final String INPUTDOTID = "dot input";
	private final String INPUTXMLID = "xml input";
	private final String OUTPUTID = "output";
		
	// End variables.
	
	// Constructors:
	
	public Dot2TreeController (CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super (callbackReceiver, properties);
		
		// Add module description
		this.setDescription("This module converts dot format from a (generalized) suffix tree into a data structure</br>"
				+ "allowes tree walking and tree structure analysis.");
				
		// Property descriptions.
			/* currently no further properties included */
		
		// Initialize module specific fields.
		this.dot2TreeNodesMap = new TreeMap <Integer, Dot2TreeNodes>();
		this.gstXmlNodes = new TreeMap <Integer, GSTXmlNode>();
		
		// Add module category
		this.setCategory("Format conversion");
				
		// Property defaults.
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Dot2GST converter");
		
		// Initialize I/O pipelines.
		InputPort inputDotPort = new InputPort(INPUTDOTID, "[dot format] Dot output from the</br>GST builder module.", this);
		inputDotPort.addSupportedPipe(CharPipe.class);
		
		InputPort inputXmlPort = new InputPort(INPUTXMLID, "[xml format] Xml output from the</br>GST builder module.", this);
		inputXmlPort.addSupportedPipe(BytePipe.class);
		
		OutputPort outputPort = new OutputPort (OUTPUTID, "[byte/binary or JSON]</br>Data structure representing"
				+ " a (generalized) suffix tree", this);
		outputPort.addSupportedPipe(BytePipe.class);
		outputPort.addSupportedPipe(CharPipe.class);
		
		super.addInputPort(inputDotPort);
		super.addInputPort(inputXmlPort);
		super.addOutputPort(outputPort);
		
	}
	
	// End constructors.
	
	// Methods:
	
	// Setters:
	
	// End setters.
	
	// Getters:
	
	// End getters.
	
	@Override
	public boolean process () throws Exception {
		// Initialize inputString.
		this.inputString = "";
		
		// Variables used for input data.
		int bufferSize = 1024;
		char [] bufferInput = new char [bufferSize];
		
		// Read first characters
		int charCode = this.getInputPorts().get(INPUTDOTID).getInputReader().read(bufferInput, 0, bufferSize);
		
		// Loop until no more data can be read.
		while (charCode != -1) {
			
			// Check for interrupt signal.
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// Convert char array to string buffer.
			StringBuffer inputBuffer = new StringBuffer(new String (bufferInput).substring(0, charCode));
			this.inputString += inputBuffer.toString();
			
			// Read next charsplitWords.
			charCode = this.getInputPorts().get(INPUTDOTID).getInputReader().read(bufferInput, 0, bufferSize);
			
		}
		
		// Read form input stream and parse GST XML format.
		
		try {
			this.suffixTreeInStream = this.getInputPorts().get(INPUTXMLID).getInputStream();
			this.treeXmlStreamReader = new GSTXmlStreamReader (this.suffixTreeInStream);
			this.gstXmlNodes = treeXmlStreamReader.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Convert the string variable "inputString" into Dot2TreeNodes objects.
		convertString2TreeNodes ();
		
		// Success.
		return true;
	}
	
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
		Pattern leafPat = Pattern.compile("\\A\t(node\\d+) \\[label=\"(\\d+)([ \\d+ \\d+ \\d+]+)\",.+\\]");
		// Leaf start pattern.
		Pattern leafStartPat = Pattern.compile("\\A\tnode\\d+ \\[");
		// Leaf end pattern.
		Pattern leafEndPat = Pattern.compile("\\A \\d+ \\d+ \\d+\",.+\\]");
		
		// Inner node pattern.
		Pattern innerPat = Pattern.compile("\\A\t(node\\d+) \\[label=\"(\\d+)\",.*\\]");
		
		// Edge pattern.
		Pattern edgePat = Pattern.compile("\\A\t(node)(\\d+) -> (node)(\\d+)\\[label=\"(\\w+)\"");
		
		// Suffix link pattern.
		Pattern suffLinkPat = Pattern.compile("\\A\t(node)(\\d+) -> (node)(\\d+)\\ [");
		
		// Node fields:
		int nodeNumber = 1;
		int nodeFreq;
		ArrayList <Integer> nodeLeafInfo = new ArrayList <Integer> ();
		
		// Node vs node comparison.
		int node1Number;
		int node2Number;
		
		// Loop through the inputString via a BufferedReader line by line.
		BufferedReader bufReader = new BufferedReader(new StringReader(this.inputString));
		
		// Local variable holding the current line.
		String currLine = null;
		
		// Local variable holding leaf node information for parsing.
		String leafInfo = null;
		
		try {
			while( (currLine=bufReader.readLine()) != null )
			{
				
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
				} if (leafStartQ.find()) {
					this.DotStat = DotTags.LEAFSTART;
				} if (leafEndQ.find()) {
					this.DotStat = DotTags.LEAFEND;
				} if (innerQ.find()) {
					this.DotStat = DotTags.INTERNAL;
				} if (edgeQ.find()) {
					this.DotStat = DotTags.EDGE;
				} if (suffLinkQ.find()) {
					this.DotStat = DotTags.SUFFIXLINK;
				}
				
				// Execute actions appropriate for the current search status.
				switch (this.DotStat) {
				case ROOT:
					nodeFreq = this.gstXmlNodes.get(nodeNumber).getNodeFrequency();
					this.rootNode = new Dot2TreeInnerNode(nodeNumber, nodeFreq, "", "");
					this.dot2TreeNodesMap.put(nodeNumber, rootNode);
					break;
					
				case LEAFSTART:
					leafInfo += currLine;
					break;
					
				case LEAFEND:
					// Add the end of the leaf to leafInfo.
					leafInfo += currLine;
					
					// Define an appropriate matcher to get the different groups from the leaf node string.
					Matcher leafQ = leafPat.matcher(leafInfo);
					nodeNumber = Integer.parseInt(leafQ.group(2));
					nodeFreq = this.gstXmlNodes.get(nodeNumber).getNodeFrequency();
					
					// Create new dot2TreeLeafNode object and save it in the TreeMap dot2TreeNodesMap.
					this.dot2TreeNodesMap.put(nodeNumber, new Dot2TreeLeafNode(nodeNumber, nodeFreq, leafQ.group(1)));
					
					// Save information about text number, starting point of occurrence and end point in the ArrayList
					// "nodeLeafInfo" temporarily.
					for (String i : leafQ.group(3).split(" ")) 
						nodeLeafInfo.add(Integer.parseInt(i));
					
					// Delete the first (empty) element of the ArrayList. Due the chosen pattern the string starts with " ".
					// Hence, the first element after splitting for each " " will be an empty string. This first element must be removed.
					nodeLeafInfo.remove(0);
					
					// Fill the fields for the new Dot2TreeLeafNode object with information gathered from the "nodeLeafInfo" ArrayList.
					for (int i = 0; i <= (nodeLeafInfo.size()/3); i ++)
						((Dot2TreeLeafNode) this.dot2TreeNodesMap.get(nodeNumber)).setLeafInfo(
								nodeLeafInfo.get(i*3), nodeLeafInfo.get(i*3+1), nodeLeafInfo.get(i*3+2));
					
					// Reset leafInfo.
					leafInfo = null;
					
					// Reset nodeLeafInfo.
					nodeLeafInfo.clear();
					break;
					
				case INTERNAL:
					nodeNumber = Integer.parseInt(innerQ.group(2));
					nodeFreq = this.gstXmlNodes.get(nodeNumber).getNodeFrequency();
					this.dot2TreeNodesMap.put(nodeNumber, new Dot2TreeInnerNode(nodeNumber, nodeFreq, innerQ.group(1)));
					break;
				
				case EDGE:
					node1Number = Integer.parseInt(edgeQ.group(2));
					node2Number = Integer.parseInt(edgeQ.group(4));
					String edgeLabel = edgeQ.group(5);
					((Dot2TreeInnerNode) this.dot2TreeNodesMap.get(node2Number)).setEdgeLabel(edgeLabel);
					((Dot2TreeInnerNode) this.dot2TreeNodesMap.get(node1Number)).addNode(node2Number,this.dot2TreeNodesMap.get(node2Number));
					break;
				
				case SUFFIXLINK:
					node1Number = Integer.parseInt(suffLinkQ.group(2));
					node2Number = Integer.parseInt(suffLinkQ.group(4));
					((Dot2TreeInnerNode) this.dot2TreeNodesMap.get(node1Number)).setSuffixLinks(node2Number);
					((Dot2TreeInnerNode) this.dot2TreeNodesMap.get(node2Number)).setSuffixLinks(node1Number);
					break;
					
				default: 
					break;
				}
	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// TODO: Move all direct children of root to the root node.
		
	}
	
	@Override
	public void applyProperties () throws Exception {
		super.setDefaultsIfMissing();
		
		// Apply own properties
			/* Custom properties are not available. */
		
		// Apply parent object's properties
		super.applyProperties();
	}
	
	// End methods.
	
	
}
