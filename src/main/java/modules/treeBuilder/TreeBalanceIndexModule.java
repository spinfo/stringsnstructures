package modules.treeBuilder;

import java.util.Iterator;
import java.util.Properties;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import com.google.gson.Gson;

import common.parallelization.CallbackReceiver;

public class TreeBalanceIndexModule extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
	//public static final String PROPERTYKEY_DELIMITER_A = "delimiter A";
	
	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "json tree";
	private static final String ID_OUTPUT = "balance index";
	
	// Local variables
	//private String inputdelimiter_a;

	public TreeBalanceIndexModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Calculates a balance index for the specified tree "
				+ "(the tree has to incorporate a node weight counter). "
				+ "The index ranges from above zero (unbalanced) to one "
				+ "(perfectly balanced) and is calculated by comparing "
				+ "the counter values of each node's children with the "
				+ "determined average.");

		// Add property descriptions (obligatory for every property!)
		//this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_A, "Regular expression to use as segmentation delimiter for input A");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "TreeBalanceIndexModule"); // Property key for module name is defined in parent class
		//this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_A, "[\\s]+");
		
		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~).
		 * Every port can support a range of pipe types (currently
		 * byte or character pipes). Output ports can provide data
		 * to multiple pipe instances at once, input ports can
		 * in contrast only obtain data from one pipe instance.
		 */
		InputPort inputPort = new InputPort(ID_INPUT, "JSON encoded suffix tree.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "Tree balance index.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		
	}

	@Override
	public boolean process() throws Exception {
		
		// Read json encoded tree
		Gson gson = new Gson();
		Knoten rootNode = gson.fromJson(this.getInputPorts().get(ID_INPUT).getInputReader(), Knoten.class);
		
		double balanceIndex = calculateBalanceIndex(rootNode);
		
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("BalanceIndex:"+balanceIndex);
		
		// Close outputs (important!)
		this.closeAllOutputs();
		
		
		
		// Done
		return true;
	}
	
	private double calculateBalanceIndex(Knoten node) {
		double balanceIndex = 1d; // Value range is 0<X<=1
		// Check whether node counter is larger than one (not a leaf)
		if (node.getKinder().size() > 1) {
			double counterAverage = new Double(node.getZaehler())
					/ new Double(node.getKinder().size());
			Iterator<Knoten> childNodes = node.getKinder().values().iterator();
			while (childNodes.hasNext()) {
				Knoten childNode = childNodes.next();
				balanceIndex = balanceIndex
						* (childNode.getZaehler() / counterAverage)
						* (1-((1-calculateBalanceIndex(childNode)) / new Double(
								node.getKinder().size())));
			}
		}
		return balanceIndex;
	}
	
	@Override
	public void applyProperties() throws Exception {
		
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
