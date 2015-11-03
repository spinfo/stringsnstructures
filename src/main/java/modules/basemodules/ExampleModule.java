package modules.basemodules;

import java.util.Properties;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import common.parallelization.CallbackReceiver;

public class ExampleModule extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_REGEX = "regex";
	public static final String PROPERTYKEY_REPLACEMENT = "replacement";
	
	// Define I/O IDs (must be unique for every input or output)
	private final String INPUT1ID = "input1";
	private final String INPUT2ID = "input2";
	private final String OUTPUTNORMID = "output-normal";
	private final String OUTPUTCAPSID = "output-caps";
	
	// Local variables
	private String regex;
	private String replacement;

	public ExampleModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Example module. Entwines two inputs and replaces parts via regex.");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_REGEX, "Regular expression to search for");
		this.getPropertyDescriptions().put(PROPERTYKEY_REPLACEMENT, "Replacement for found strings");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Example Module"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_REGEX, "[aeiu]");
		this.getPropertyDefaultValues().put(PROPERTYKEY_REPLACEMENT, "o");
		
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
		InputPort inputPort2 = new InputPort(INPUT2ID, "Plain text character input (will be inserted at every other character position from input 1; length beyond that of input 1 will be ignored).", this);
		inputPort2.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUTNORMID, "Plain text character output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		OutputPort capsOutputPort = new OutputPort(OUTPUTCAPSID, "Plain text character output (all uppercase).", this);
		capsOutputPort.addSupportedPipe(CharPipe.class);
		
		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort1);
		super.addInputPort(inputPort2);
		super.addOutputPort(outputPort);
		super.addOutputPort(capsOutputPort);
		
	}

	@Override
	public boolean process() throws Exception {
		
		/*
		 * This module doesn't do much useful processing.
		 * It reads from two inputs, entwines both of them
		 * with each other and replaces characters via
		 * a regex taken from the specified property.
		 * Just used to exemplify a basic module. 
		 */
		
		// Variables used for input data
		int bufferSize = 1024;
		char[] bufferInput1 = new char[bufferSize];
		char[] bufferInput2 = new char[bufferSize];
		
		// Read first chunk of data from both inputs
		int readCharsInput1 = this.getInputPorts().get(INPUT1ID).read(bufferInput1, 0, bufferSize);
		int readCharsInput2 = -1;
		if (this.getInputPorts().get(INPUT2ID).isConnected())
			readCharsInput2 = this.getInputPorts().get(INPUT2ID).read(bufferInput2, 0, bufferSize);
		
		// Loop until no more data can be read from input 1
		while (readCharsInput1 != -1){
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// Convert char array to string buffer
			StringBuffer input1Chunk = new StringBuffer(new String(bufferInput1).substring(0, readCharsInput1));
			
			// Check whether input 2 provided data
			if (readCharsInput2 != -1){
				
				// Loop over input 2 buffer
				for (int i=0; i<readCharsInput2 && i<readCharsInput1; i++){
					// Insert character
					input1Chunk.insert(i*2, bufferInput2[i]);
				}
			}
			
			// Process data
			String outputChunk = input1Chunk.toString().replaceAll(this.regex, this.replacement);
			
			// Write to outputs
			this.getOutputPorts().get(OUTPUTNORMID).outputToAllCharPipes(outputChunk);
			this.getOutputPorts().get(OUTPUTCAPSID).outputToAllCharPipes(outputChunk.toUpperCase());
			
			// Read next chunk of data from both inputs
			readCharsInput1 = this.getInputPorts().get(INPUT1ID).read(bufferInput1, 0, bufferSize);
			if (readCharsInput2 != -1)
				readCharsInput2 = this.getInputPorts().get(INPUT2ID).read(bufferInput2, 0, bufferSize);
		}
		
		// Close outputs (important!)
		this.closeAllOutputs();
		
		/*
		 * NOTE: A module must not close its inputs itself -- this is done by the module providing them
		 */
		
		// Done
		return true;
	}
	
	@Override
	public void applyProperties() throws Exception {
		
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();
		
		// Apply own properties
		this.regex = this.getProperties().getProperty(PROPERTYKEY_REGEX, this.getPropertyDefaultValues().get(PROPERTYKEY_REGEX));
		this.replacement = this.getProperties().getProperty(PROPERTYKEY_REPLACEMENT, this.getPropertyDefaultValues().get(PROPERTYKEY_REPLACEMENT));
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
