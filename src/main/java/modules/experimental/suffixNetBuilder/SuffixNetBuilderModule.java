package modules.experimental.suffixNetBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import common.parallelization.CallbackReceiver;
import models.ExtensibleTreeNode;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class SuffixNetBuilderModule extends ModuleImpl {

	public static final String NODECOUNTER_KEY = "nodeCounter";
	// Property keys
	public static final String PROPERTYKEY_OUTERINPUTDELIMITER = "Input delimiter (outer)";
	public static final String PROPERTYKEY_INNERINPUTDELIMITER = "Input delimiter (inner)";
	public static final String PROPERTYKEY_INDIVIDUALBRANCHES = "Create individual branches";

	// Local variables
	private boolean individualBranches;
	private String outerInputDelimiter = "\\$";
	private String innerInputDelimiter = "[\\s]+";
	private final String INPUTID = "input";
	private final String OUTPUTID = "output";

	public SuffixNetBuilderModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// Define I/O
		InputPort inputPort = new InputPort(INPUTID, "Plain text input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUTID, "JSON encoded graph.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_INDIVIDUALBRANCHES,
				"Creates individual branches for each sentence.");
		this.getPropertyDescriptions().put(PROPERTYKEY_OUTERINPUTDELIMITER,
				"<p>The <i>outer input delimiter</i> is used to discern strings from each other that will be inserted into the resulting graph independently, resulting into a <i>Generalised Suffix Net</i>.</p><p>The value is interpreted as a <i>Regular Expression</i>, e.g. '$' marks the end of a line and '\\$' means the actual dollar sign; set to '\\z' for single string input.</p>");
		this.getPropertyDescriptions().put(PROPERTYKEY_INNERINPUTDELIMITER,
				"Regular expression to use as inner segmentation delimiter for the input; leave empty for char-by-char segmentation.");

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "SuffixNetBuilder");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INDIVIDUALBRANCHES, "false");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INNERINPUTDELIMITER, "\\s+");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTERINPUTDELIMITER, "\\R+");

		// Add module description
		this.setDescription("Builds a suffix net (for want of a better name) from the text input.");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see modularization.ModuleImpl#applyProperties()
	 */
	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		this.outerInputDelimiter = this.getProperties().getProperty(PROPERTYKEY_OUTERINPUTDELIMITER,
				this.getPropertyDefaultValues().get(PROPERTYKEY_OUTERINPUTDELIMITER));
		this.innerInputDelimiter = this.getProperties().getProperty(PROPERTYKEY_INNERINPUTDELIMITER,
				this.getPropertyDefaultValues().get(PROPERTYKEY_INNERINPUTDELIMITER));
		if (this.getProperties().containsKey(PROPERTYKEY_INDIVIDUALBRANCHES))
			this.individualBranches = Boolean
					.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_INDIVIDUALBRANCHES));
		super.applyProperties();
	}

	@Override
	public boolean process() throws Exception {

		// Create the suffix net root node
		ExtensibleTreeNode rootNode = new ExtensibleTreeNode("^");

		// Variable zur Nachhaltung erstellter Graphenknoten
		Map<String, ExtensibleTreeNode> createdNodes = null;
		if (!this.individualBranches)
			createdNodes = new HashMap<String, ExtensibleTreeNode>();

		// Instantiate outer input scanner
		Scanner outerInputScanner = new Scanner(this.getInputPorts().get(INPUTID).getInputReader());
		outerInputScanner.useDelimiter(this.outerInputDelimiter);

		// Outer input read loop
		while (outerInputScanner.hasNext()) {

			// Check for interrupt signal
			if (Thread.interrupted()) {
				outerInputScanner.close();
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}

			// Continue building the suffix net
			this.buildSuffixNet(outerInputScanner.next().split(this.innerInputDelimiter), rootNode, createdNodes);

		}

		// Close input scanner
		outerInputScanner.close();

		// Initialise JSON serialiser
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.setPrettyPrinting().create();

		// Create JSON
		String json = gson.toJson(rootNode);

		// Write to outputs
		this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(json);

		// Close outputs
		this.closeAllOutputs();

		return true;
	}

	/**
	 * Builds a suffix net based on the given root node.
	 * 
	 * @param token
	 *            Token of strings (will be nodes within the resulting net)
	 * @param rootnode
	 *            Node to start from
	 * @param createdNodes
	 *            Map used to keep track of created nodes. If null, each call
	 *            will create a different individual branch.
	 * @throws Exception
	 */
	private void buildSuffixNet(String[] token, ExtensibleTreeNode rootnode,
			Map<String, ExtensibleTreeNode> createdNodes) throws Exception {

		// Keep track of created nodes
		if (createdNodes == null)
			createdNodes = new HashMap<String, ExtensibleTreeNode>();

		// Set parent node
		ExtensibleTreeNode parentNode = rootnode;

		// Variable for child node
		ExtensibleTreeNode child;

		// Loop over the token array's elements
		for (int i = 0; i < token.length; i++) {

			// Check whether a node with the current token's name already exists
			if (createdNodes.containsKey(token[i])) {

				// If so, retrieve the node
				child = createdNodes.get(token[i]);

				// Check whether that node is already connected to the current
				// parent node
				if (!parentNode.getChildNodes().containsKey(token[i])) {

					// If not, make the connection
					parentNode.getChildNodes().put(token[i], child);

				}
			}

			else {

				// Instantiate new node
				child = new ExtensibleTreeNode(token[i]);

				// Connect it to parent
				parentNode.getChildNodes().put(token[i], child);

				// Remember it as having been created
				createdNodes.put(token[i], child);
			}

			// Increment the counter of the child node
			child.setNodeCounter(child.getNodeCounter() + 1);

			// Set new parent
			parentNode = child;
		}
	}

}
