package modules.basemodules;

import java.util.Properties;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import common.parallelization.CallbackReceiver;

public class ExampleModule extends ModuleImpl {
	
	// Define property keys
	public static final String PROPERTYKEY_REGEX = "regex";
	public static final String PROPERTYKEY_REPLACEMENT = "replacement";
	
	// Module variables
	private final String INPUTID = "input";
	private final String OUTPUTID = "output";
	private String regex;
	private String replacement;

	public ExampleModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// Add property descriptions
		this.getPropertyDescriptions().put(PROPERTYKEY_REGEX, "Regular expression to search for");
		this.getPropertyDescriptions().put(PROPERTYKEY_REPLACEMENT, "Replacement for found strings");
		
		// Add property defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Example Module");
		this.getPropertyDefaultValues().put(PROPERTYKEY_REGEX, "[aeiu]");
		this.getPropertyDefaultValues().put(PROPERTYKEY_REPLACEMENT, "o");
		
		// Define I/O
		InputPort inputPort = new InputPort("Input", "Plain text character input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort("Output", "Plain text character output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(INPUTID,inputPort);
		super.addOutputPort(OUTPUTID,outputPort);
		
	}

	@Override
	public boolean process() throws Exception {
		
		// Variables used for input data
		int bufferSize = 1024;
		char[] buffer = new char[bufferSize];
		
		// Read first chunk of data
		int readChars = this.getInputPorts().get(INPUTID).read(buffer, 0, bufferSize);
		
		// Loop until no more data can be read
		while (readChars != -1){
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// Convert char array to string
			String inputChunk = new String(buffer).substring(0, readChars);
			
			// Process data
			String outputChunk = inputChunk.replaceAll(this.regex, this.replacement);
			
			// Write to outputs
			this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(outputChunk);
			
			// Read next chunk of data
			readChars = this.getInputPorts().get(INPUTID).read(buffer, 0, bufferSize);
		}
		
		// Close outputs (important!)
		this.closeAllOutputs();
		
		// Done
		return true;
	}
	
	@Override
	public void applyProperties() throws Exception {
		
		// Apply own properties
		if (this.getProperties().containsKey(PROPERTYKEY_REGEX))
			this.regex = this.getProperties().getProperty(PROPERTYKEY_REGEX, this.getPropertyDefaultValues().get(PROPERTYKEY_REGEX));
		if (this.getProperties().containsKey(PROPERTYKEY_REPLACEMENT))
			this.replacement = this.getProperties().getProperty(PROPERTYKEY_REPLACEMENT, this.getPropertyDefaultValues().get(PROPERTYKEY_REPLACEMENT));
		
		// Apply parent object's properties
		super.applyProperties();
	}

}
