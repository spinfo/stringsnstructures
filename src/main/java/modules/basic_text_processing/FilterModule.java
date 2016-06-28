package modules.basic_text_processing;

import java.io.BufferedReader;
import java.util.Properties;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import common.parallelization.CallbackReceiver;

public class FilterModule extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_MINLENGTH = "minlength";
	public static final String PROPERTYKEY_MAXLENGTH = "maxlength";
	
	// Define I/O IDs (must be unique for every input or output)
	private final String INPUT1ID = "input1";
	private final String OUTPUTNORMID = "output-normal";
	
	// local vars
	int minlength;
	int maxlength;

	public FilterModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Filters out strings not matching the specified minimum or maximum length.");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_MINLENGTH, "minimum length of ...");
		this.getPropertyDescriptions().put(PROPERTYKEY_MAXLENGTH, "maximum length of ...");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(PROPERTYKEY_MINLENGTH, "1");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MAXLENGTH, "30");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Filter Module"); // Property key for module name is defined in parent class
		
		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~).
		 * Every port can support a range of pipe types (currently
		 * byte or character pipes). Output ports can provide data
		 * to multiple pipe instances at once, input ports can
		 * in contrast only obtain data from one pipe instance.
		 */
		InputPort inputPort1 = new InputPort(INPUT1ID, "Plain text character input.", this);
		inputPort1.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUTNORMID, "Plain text character output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort1);
		super.addOutputPort(outputPort);
		
	}

	@Override
	public boolean process() throws Exception {
		
		// Reader
		BufferedReader inputReader = new BufferedReader(this.getInputPorts().get(INPUT1ID).getInputReader());
		
		// Loop until no more data can be read from input 1
		String line;
		while ((line = inputReader.readLine()) != null){
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// Process data
			int length = line.split("[ ]").length;
			if (length >= minlength && length <= maxlength){
				// Write to outputs
				this.getOutputPorts().get(OUTPUTNORMID).outputToAllCharPipes(line.concat("$\n"));
			}
			
			
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
		if (this.getProperties().containsKey(PROPERTYKEY_MINLENGTH) || this.getPropertyDefaultValues().containsKey(PROPERTYKEY_MINLENGTH))
			this.minlength = Integer.parseInt(this.getProperties().getProperty(PROPERTYKEY_MINLENGTH, this.getPropertyDefaultValues().get(PROPERTYKEY_MINLENGTH)));
		if (this.getProperties().containsKey(PROPERTYKEY_MAXLENGTH) || this.getPropertyDefaultValues().containsKey(PROPERTYKEY_MAXLENGTH))
			this.maxlength = Integer.parseInt(this.getProperties().getProperty(PROPERTYKEY_MAXLENGTH, this.getPropertyDefaultValues().get(PROPERTYKEY_MAXLENGTH)));
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
