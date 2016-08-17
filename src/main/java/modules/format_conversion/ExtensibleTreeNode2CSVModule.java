package modules.format_conversion;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.Gson;

import common.parallelization.CallbackReceiver;
import models.ExtensibleTreeNode;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class ExtensibleTreeNode2CSVModule extends ModuleImpl {

	public static final String PROPERTYKEY_CSVDELIMITER = "CSV delimiter";
	public static final String PROPERTYKEY_CALCPROBABILITIES = "calculate probabilities";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "input";
	private static final String ID_OUTPUT = "output";

	// Local variables
	private String csvdelimiter;
	private Boolean calculateProbabilities;

	public ExtensibleTreeNode2CSVModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("Converts trees based on the ExtensibleTreeNode into a CSV table.");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_CSVDELIMITER,
				"String to use as CSV field delimiter.");
		this.getPropertyDescriptions().put(PROPERTYKEY_CALCPROBABILITIES,
				"Calculate node transistion probabilities (else, a simple binary value will be used on output). [true/false]");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"ExtensibleTreeNode to CSV converter"); // Property key for module name is defined in
									// parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_CALCPROBABILITIES,
				"true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_CSVDELIMITER, ";");

		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~). Every port
		 * can support a range of pipe types (currently byte or character
		 * pipes). Output ports can provide data to multiple pipe instances at
		 * once, input ports can in contrast only obtain data from one pipe
		 * instance.
		 */
		InputPort inputPort = new InputPort(ID_INPUT,
				"ExtensibleTreeNode tree.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT,
				"GEXF graph.", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {
		
		

		// Instantiate JSON parser
		Gson gson = new Gson();
		
		// Read tree from input & parse it
		ExtensibleTreeNode rootNode = gson.fromJson(this.getInputPorts().get(ID_INPUT).getInputReader(), ExtensibleTreeNode.class);
		
		// Compile set of all nodes of the tree
		Set<ExtensibleTreeNode> allNodes = new TreeSet<ExtensibleTreeNode>();
		this.addNodesToSet(rootNode, allNodes);
		
		// Start CSV header
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.csvdelimiter);
		
		// Iterate over node set a first time to construct the CSV header
		Iterator<ExtensibleTreeNode> nodes = allNodes.iterator();
		while (nodes.hasNext()){
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(nodes.next().getNodeValue()+this.csvdelimiter);
		}
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");
		
		// Iterate over node set a second time to write out the CSV data lines
		nodes = allNodes.iterator();
		while (nodes.hasNext()) {
			ExtensibleTreeNode lineNode = nodes.next();
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(lineNode.getNodeValue() + this.csvdelimiter);
			
			// Iterate over the nodes yet again to write the actual data fields
			Iterator<ExtensibleTreeNode> fieldNodes = allNodes.iterator();
			while (fieldNodes.hasNext()){
				ExtensibleTreeNode fieldNode = fieldNodes.next();
				// Determine whether this field node is a child of the current line node
				if (lineNode.getChildNodes().containsValue(fieldNode)){
					// Output binary or calculated transition probability
					if (this.calculateProbabilities){
						Double probability = new Double(fieldNode.getNodeCounter())/new Double(lineNode.getNodeCounter());
						this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(probability+this.csvdelimiter);
					} else {
						this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(1+this.csvdelimiter);
					}
				} else
					this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(0+this.csvdelimiter);
			}
			
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");
		}
		

		// Close outputs (important!)
		this.closeAllOutputs();

		// Done
		return true;
	}
	
	/**
	 * Adds the specified node and its descendants to the given set.
	 * @param node Node to add (including descendants)
	 * @param nodeSet Set to add to
	 */
	private void addNodesToSet(ExtensibleTreeNode node, Set<ExtensibleTreeNode> nodeSet){
		
		// Add node (duh!)
		nodeSet.add(node);
		
		// Iterate over child nodes and recurse
		Iterator<ExtensibleTreeNode> children = node.getChildNodes().values().iterator();
		while (children.hasNext())
			this.addNodesToSet(children.next(), nodeSet);
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		this.csvdelimiter = this.getProperties().getProperty(
				PROPERTYKEY_CSVDELIMITER,
				this.getPropertyDefaultValues()
						.get(PROPERTYKEY_CSVDELIMITER));
		String value = this.getProperties().getProperty(
				PROPERTYKEY_CALCPROBABILITIES,
				this.getPropertyDefaultValues().get(
						PROPERTYKEY_CALCPROBABILITIES));
		if (value!=null && !value.isEmpty())
			this.calculateProbabilities = Boolean.parseBoolean(value);

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
