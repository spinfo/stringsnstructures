package modules.tree_building.treeBuilder;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.SortedMap;

import models.ExtensibleTreeNode;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.Pipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.parallelization.CallbackReceiver;

public class TreeBuilderV3Module extends ModuleImpl {

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_OUTERINPUTDELIMITER = "Input delimiter (outer)";
	public static final String PROPERTYKEY_INNERINPUTDELIMITER = "Input delimiter (inner)";
	public static final String PROPERTYKEY_OUTPUTDELIMITER = "Output delimiter";
	public static final String PROPERTYKEY_MAXDEPTH = "Tree depth";
	public static final String PROPERTYKEY_OMITREDUNDANTINFO = "Omit redundant info";
	public static final String PROPERTYKEY_STRUCTURE = "Compact or atomic?";
	// public static final String PROPERTYKEY_MAXTHREADS = "Max. threads";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "text";
	private static final String ID_OUTPUT = "tree";

	// Local variables
	private String outerInputDelimiter = "\\$";
	private String innerInputDelimiter = "[\\s]+";
	private String outputDelimiter = " ";
	private int maxDepth = -1;
	private boolean omitRedundantInformation;
	private boolean compactTree;

	// private int maxThreads;

	public TreeBuilderV3Module(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("<p>This module can be used to construct different types of suffix trees from formatted or non-formatted text input. It uses a custom algorithm and runs in approximate linear time (actually it did turn out that the processing time grows exponentially under certain circumstances, so no guarantee) and uses single thread processing.</p>"
				+ "<p> The type of tree to build is determined by various parameters. With those, you can build suffix trees that are"
				+ "<ul>"
				+ "<li><i>generalised</i> or <i>non-generalised</i>,</li>"
				+ "<li><i>atomic</i> or <i>compact</i> (see [1], p.189f),</li>"
				+ "<li>depth-restricted or complete.</li>"
				+ "</ul></p>"
				+ "<p>Additionally, the tree will also be generated with a variable for each node that counts how many leafes are below. The output is a JSON-encoded tree consisting of <i>ExtensibleTreeNode</i> objects.</p>"
				+ "<ol>"
				+ "<li>Giegerich, Robert, and Stefan Kurtz. &quot;A comparison of imperative and purely functional suffix tree constructions.&quot; Science of Computer Programming 25.2 (1995): 187-218.</li>"
				+ "</ol>");

		// Add module category
		this.setCategory("Tree-building");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_OUTERINPUTDELIMITER,
						"<p>The <i>outer input delimiter</i> is used to discern strings from each other that will be inserted into the resulting tree independently, resulting into a <i>Generalised Suffix Tree</i>.</p><p>The value is interpreted as a <i>Regular Expression</i>, e.g. '$' marks the end of a line and '\\$' means the actual dollar sign; set to '\\z' for single string input.</p>");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_INNERINPUTDELIMITER,
						"Regular expression to use as inner segmentation delimiter for the input; leave empty for char-by-char segmentation.");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_OUTPUTDELIMITER,
						"String to use as segmentation delimiter for the output; must match with the inner input delimiter regex; leave empty for char-by-char segmentation.");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_MAXDEPTH,
						"Maximum depth for the resulting tree; set to -1 for no constraint. A setting of 0 will yield a tree one level deep that contains nodes with a length of one (basically the alphabet of the input, respectively the first element of any document for GSTs).");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_OMITREDUNDANTINFO,
						"Omit redundant information upon creating the tree (do not set nodevalue, since this info is already contained within the parent's child node mapping key).");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_STRUCTURE,
						"Type of suffix tree to output; possible values are 'compact' and 'atomic'.");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "TreeBuilder v3 Module");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INNERINPUTDELIMITER, "[\\s]+");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTERINPUTDELIMITER, "\\$");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTPUTDELIMITER, " ");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MAXDEPTH, "-1");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OMITREDUNDANTINFO, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_STRUCTURE, "compact");

		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~). Every port
		 * can support a range of pipe types (currently byte or character
		 * pipes). Output ports can provide data to multiple pipe instances at
		 * once, input ports can in contrast only obtain data from one pipe
		 * instance.
		 */
		InputPort inputPort = new InputPort(ID_INPUT,
				"Plain text character input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(
				ID_OUTPUT,
				"JSON-encoded suffix tree (nodes based on the TreeNode interface).",
				this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {

		// Initialise tree root node
		ExtensibleTreeNode rootNode = new ExtensibleTreeNode("^");
		rootNode.setNodeCounter(1);
		
		// Initialise parent relation map
		Map<ExtensibleTreeNode,ExtensibleTreeNode> childParentRelationMap = new HashMap<ExtensibleTreeNode,ExtensibleTreeNode>();
		childParentRelationMap.put(rootNode, null);

		// Instantiate outer input scanner
		Scanner outerInputScanner = new Scanner(this.getInputPorts()
				.get(ID_INPUT).getInputReader());
		outerInputScanner.useDelimiter(this.outerInputDelimiter);

		// Outer input read loop
		while (outerInputScanner.hasNext()) {

			// Check for interrupt signal
			if (Thread.interrupted()) {
				outerInputScanner.close();
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}

			// Determine next segment
			String outerInputSegment = outerInputScanner.next();

			// Initialise leaf list
			List<ExtensibleTreeNode> leafList = new ArrayList<ExtensibleTreeNode>();
			leafList.add(rootNode);

			// Instantiate inner input scanner
			Scanner innerInputScanner = new Scanner(new StringReader(
					outerInputSegment));
			innerInputScanner.useDelimiter(this.innerInputDelimiter);

			// Inner input read loop
			while (innerInputScanner.hasNext()) {

				// Check for interrupt signal
				if (Thread.interrupted()) {
					innerInputScanner.close();
					this.closeAllOutputs();
					throw new InterruptedException(
							"Thread has been interrupted.");
				}

				// Determine next segment
				String inputSegment = innerInputScanner.next().concat(
						this.outputDelimiter);

				/*
				 * Each segment is to be attached as a child to each tree node
				 * of last loop's leaf list (or increase the counter of the
				 * respective child node should it already exist) and then add
				 * the created/found child node to next loop's list.
				 */

				List<ExtensibleTreeNode> nextLeafList = new ArrayList<ExtensibleTreeNode>(
						leafList.size() + 1);
				nextLeafList.add(rootNode);

				Iterator<ExtensibleTreeNode> leaves = leafList.iterator();
				while (leaves.hasNext()) {
					// Determine next node
					ExtensibleTreeNode node = leaves.next();

					// Get child node for the read segment
					ExtensibleTreeNode childNode = null;
					if (compactTree) {
						// Determine all child nodes that start with the read
						// segment (amount can only be one or zero)
						SortedMap<String, ExtensibleTreeNode> childNodesThatStartWithSegment = node
								.getChildNodes().subMap(inputSegment,
										inputSegment + Character.MAX_VALUE);
						if (!childNodesThatStartWithSegment.isEmpty()) {
							String childNodeValue = childNodesThatStartWithSegment
									.firstKey();
							childNode = (ExtensibleTreeNode) childNodesThatStartWithSegment
									.get(childNodeValue);
							// If the child node's value is longer than the
							// input segment, we have to insert a split (i.e. a
							// new node)
							if (childNodeValue.length() > inputSegment.length()) {
								// Determine the part of the child node value to
								// detach as a suffix
								String childNodeValueSuffix = childNodeValue
										.substring(inputSegment.length());

								// Determine parent node value
								String parentNodeValue = (String) node
										.getNodeValue();
								// If the value is only present as a map key, we
								// have to search it first
								if (omitRedundantInformation
										&& !node.equals(rootNode)) {
									for (Entry<String, ExtensibleTreeNode> entry : childParentRelationMap.get(node).getChildNodes().entrySet()) {
										if (Objects.equals(node,
												entry.getValue())) {
											parentNodeValue = entry.getKey();
											break;
										}
									}
								}

								// Clip read segment from child node
								if (!omitRedundantInformation)
									childNode
											.setNodeValue(childNodeValueSuffix);
								// Detach child node (will be re-attached with
								// only the suffix as key)
								node.getChildNodes().remove(childNodeValue);

								// Check whether the child node was an only
								// child
								if (node.getChildNodes().size() == 0
										&& !node.equals(rootNode)) {
									// Incorporate read segment into node
									if (!omitRedundantInformation)
										node.setNodeValue(parentNodeValue
												+ inputSegment);
									// Update grandparent map key
									childParentRelationMap.get(node)
											.getChildNodes().remove(
													parentNodeValue);
									childParentRelationMap.get(node)
											.getChildNodes().put(
													parentNodeValue
															+ inputSegment,
													node);
									// Re-attach child node
									node.getChildNodes().put(
											childNodeValueSuffix, childNode);

									// Update child node reference (because the
									// child node will be added to next
									// iteration's leaf list further down)
									childNode = node;
								} else {
									// Create new node for the read segment
									ExtensibleTreeNode newNode = new ExtensibleTreeNode();
									newNode.setNodeCounter(childNode
											.getNodeCounter());
									if (!omitRedundantInformation)
										newNode.setNodeValue(inputSegment);
									// Attach new node to parent node
									node.getChildNodes().put(inputSegment,
											newNode);
									childParentRelationMap.put(newNode, node);
									// Re-attach child node
									newNode.getChildNodes().put(
											childNodeValueSuffix, childNode);
									childParentRelationMap.put(childNode, newNode);

									// Update child node reference (because the
									// child node will be added to next
									// iteration's leaf list further down)
									childNode = newNode;
								}
							}
						}

					} else
						childNode = (ExtensibleTreeNode) node.getChildNodes()
								.get(inputSegment);

					// Check if child node does not yet exist (and if so, create
					// it)
					if (childNode == null) {

						// Merge with parent or construct separate child node
						if (compactTree && !node.equals(rootNode)
								&& node.getChildNodes().size() == 0
								&& !nextLeafList.contains(node)) {

							// Determine node value
							String nodeValue = node.getNodeValue();
							// If the value is only present as a map key, we
							// have to search it first
							if (omitRedundantInformation) {

								for (Entry<String, ExtensibleTreeNode> entry : childParentRelationMap.get(node)
										.getChildNodes().entrySet()) {
									if (Objects.equals(node, entry.getValue())) {
										nodeValue = entry.getKey();
										break;
									}
								}
							}
							childParentRelationMap.get(node)
									.getChildNodes().values().remove(node);
							childParentRelationMap.get(node)
									.getChildNodes().put(
											nodeValue + inputSegment, node);
							if (!omitRedundantInformation)
								node.setNodeValue(nodeValue + inputSegment);
							node.getChildNodes().clear();
							childNode = node;

						} else {
							childNode = new ExtensibleTreeNode();
							childParentRelationMap.put(childNode, node);
							if (!omitRedundantInformation)
								childNode.setNodeValue(inputSegment);
							node.getChildNodes().put(inputSegment, childNode);

							// Increment child node counter
							childNode
									.setNodeCounter(childNode.getNodeCounter() + 1);

							// If there is a new split, increment ancestor node
							// counters
							ExtensibleTreeNode ancestor = node;
							if (ancestor.getChildNodes().size() > 1)
								while (ancestor != null) {
									ancestor.setNodeCounter(ancestor
											.getNodeCounter() + 1);
									ancestor = childParentRelationMap.get(ancestor);
								}
						}
					} else {
						// Merge with parent or construct separate child node
						if (compactTree && !node.equals(rootNode)
								&& node.getChildNodes().size() == 1
								&& !node.equals(childNode)
								&& !nextLeafList.contains(node)) {

							// Determine node value
							String nodeValue = node.getNodeValue();
							// If the value is only present as a map key, we
							// have to
							// search it first
							if (omitRedundantInformation) {

								for (Entry<String, ExtensibleTreeNode> entry : childParentRelationMap.get(node)
										.getChildNodes().entrySet()) {
									if (Objects.equals(node, entry.getValue())) {
										nodeValue = entry.getKey();
										break;
									}
								}
							}
							// Determine child node value
							String childNodeValue = childNode.getNodeValue();
							// If the value is only present as a map key, we
							// have to
							// search it first
							if (omitRedundantInformation) {

								for (Entry<String, ExtensibleTreeNode> entry : node
										.getChildNodes().entrySet()) {
									if (Objects.equals(childNode,
											entry.getValue())) {
										childNodeValue = entry.getKey();
										break;
									}
								}
							}
							childParentRelationMap.get(node)
									.getChildNodes().values().remove(node);
							childParentRelationMap.get(node)
									.getChildNodes().put(
											nodeValue + childNodeValue,
											childNode);
							if (!omitRedundantInformation)
								childNode.setNodeValue(nodeValue
										+ childNodeValue);
							node.getChildNodes().clear();
							childParentRelationMap.put(childNode, childParentRelationMap.get(node));
						}
					}

					// Apply max depth constraint if specified
					if (this.maxDepth < 0
							|| nextLeafList.size() <= this.maxDepth)

						// Append child node to next loop's leaf list
						nextLeafList.add(childNode);
				}

				// Update list reference
				leafList = nextLeafList;
			}
			innerInputScanner.close();

		}
		outerInputScanner.close();

		// Loop over leaves to eliminate parent relations (since they make
		// serialisation impossible)
		/*
		 * Iterator<ExtensibleTreeNode> leaves = leafList.iterator(); while
		 * (leaves.hasNext()){ ExtensibleTreeNode leaf = leaves.next();
		 * ExtensibleTreeNode parent =
		 * (((ExtensibleTreeNode)(leaf.getAttributes(
		 * ).get(attribkey_parentnode)))); while (parent != null){
		 * leaf.getAttributes().put(attribkey_parentnode, null); leaf = parent;
		 * parent =
		 * (((ExtensibleTreeNode)(parent.getAttributes().get(attribkey_parentnode
		 * )))); } }
		 */

		// Initialise JSON serialiser
		GsonBuilder gsonBuilder = new GsonBuilder();
		// gsonBuilder.registerTypeAdapter(ModuleNetwork.class, new
		// ParentRelationTreeNodeSerialiser());
		Gson gson = gsonBuilder.setPrettyPrinting().create();

		// Write to outputs // TODO Not sure if this is really the best
		// performing method. Needs testing.
		Iterator<Pipe> outputPipes = this.getOutputPorts().get(ID_OUTPUT)
				.getPipes(CharPipe.class).iterator();
		while (outputPipes.hasNext()) {
			CharPipe outputPipe = (CharPipe) outputPipes.next();
			gson.toJson(rootNode, outputPipe.getOutput());
		}

		// Close outputs (important!)
		this.closeAllOutputs();

		// Done
		return true;
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		this.outerInputDelimiter = this.getProperties().getProperty(
				PROPERTYKEY_OUTERINPUTDELIMITER,
				this.getPropertyDefaultValues().get(
						PROPERTYKEY_OUTERINPUTDELIMITER));
		this.innerInputDelimiter = this.getProperties().getProperty(
				PROPERTYKEY_INNERINPUTDELIMITER,
				this.getPropertyDefaultValues().get(
						PROPERTYKEY_INNERINPUTDELIMITER));
		this.outputDelimiter = this.getProperties().getProperty(
				PROPERTYKEY_OUTPUTDELIMITER,
				this.getPropertyDefaultValues()
						.get(PROPERTYKEY_OUTPUTDELIMITER));
		if (!(this.outputDelimiter == null && this.innerInputDelimiter == null)
				&& !this.outputDelimiter.matches(this.innerInputDelimiter))
			throw new Exception(
					"The specified output delimiter does not match with the current input delimiter.");
		
		String maxDepthString = this.getProperties().getProperty(
				PROPERTYKEY_MAXDEPTH,
				this.getPropertyDefaultValues().get(PROPERTYKEY_MAXDEPTH));
		if (maxDepthString != null)
			this.maxDepth = Integer.parseInt(maxDepthString);
		 

		String omitRedundantInformationString = this.getProperties()
				.getProperty(
						PROPERTYKEY_OMITREDUNDANTINFO,
						this.getPropertyDefaultValues().get(
								PROPERTYKEY_OMITREDUNDANTINFO));
		if (omitRedundantInformationString != null)
			this.omitRedundantInformation = Boolean
					.parseBoolean(omitRedundantInformationString);

		String treeOrTrieString = this.getProperties().getProperty(
				PROPERTYKEY_STRUCTURE,
				this.getPropertyDefaultValues().get(PROPERTYKEY_STRUCTURE));
		if (treeOrTrieString != null)
			if (treeOrTrieString.equalsIgnoreCase("compact"))
				this.compactTree = true;
			else if (treeOrTrieString.equalsIgnoreCase("atomic"))
				this.compactTree = false;
			else
				throw new Exception("Invalid value for property '"
						+ PROPERTYKEY_STRUCTURE + "'.");

		// this.maxThreads =
		// Integer.parseInt(this.getProperties().getProperty(PROPERTYKEY_MAXTHREADS,
		// this.getPropertyDefaultValues().get(PROPERTYKEY_MAXTHREADS)));

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
