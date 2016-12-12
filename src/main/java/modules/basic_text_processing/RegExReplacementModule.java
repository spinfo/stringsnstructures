package modules.basic_text_processing;

import java.util.Properties;
import java.util.regex.PatternSyntaxException;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;


import org.apache.commons.lang3.StringEscapeUtils;

import common.parallelization.CallbackReceiver;

import base.workbench.ModuleRunner;

public class RegExReplacementModule extends ModuleImpl {

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(RegExReplacementModule.class, args);
	}

	
	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_REGEX = "regex";
	public static final String PROPERTYKEY_REPLACEMENT = "replacement";
	public static final String PROPERTYKEY_UNESCAPE = "unescape";
	
	// Define I/O IDs (must be unique for every input or output)
	private final String INPUTID = "input";
	private final String OUTPUTID = "output";
	
	// Local variables
	private String regex;
	private String replacement;
	private boolean unescape;

	public RegExReplacementModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add description
		this.setDescription("Regular expression text replacement module. Can use escape characters (e.g. '\\n') and backreferences (marked with '$', e.g. '$1').");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_REGEX, "Regular expression to search for");
		this.getPropertyDescriptions().put(PROPERTYKEY_REPLACEMENT, "Replacement for found strings");
		this.getPropertyDescriptions().put(PROPERTYKEY_UNESCAPE, "Perform unescape operation on the replacement string before using it [true|false]");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "RegEx Replacement Module"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_REGEX, "[aeiu]");
		this.getPropertyDefaultValues().put(PROPERTYKEY_REPLACEMENT, "o");
		this.getPropertyDefaultValues().put(PROPERTYKEY_UNESCAPE, "true");
		
		// Define I/O
		InputPort inputPort = new InputPort(INPUTID, "Plain text character input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUTID, "Plain text character output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		
	}

	@Override
	public boolean process() throws Exception {
		
		// Variables used for input data
		int bufferSize = 1024;
		char[] bufferInput = new char[bufferSize];
		
		// Read first chunk of data from both inputs
		int readCharsInput = this.getInputPorts().get(INPUTID).read(bufferInput, 0, bufferSize);
		
		// Loop until no more data can be read from input 1
		while (readCharsInput != -1){
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// Convert char array to string
			String inputChunk = new String(bufferInput).substring(0, readCharsInput);
			// Process data
			String outputChunk="";
			try {
			 outputChunk = inputChunk.replaceAll(this.regex, this.replacement);
			}
			catch(PatternSyntaxException p) {
				System.out.println("error pattern:"+p.getMessage()+ " pattern: "+
						p.getPattern());
				p.printStackTrace();
				System.exit(3);
				
			};
			
			// Write to output
			this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(outputChunk);
			
			// Read next chunk of data from input
			readCharsInput = this.getInputPorts().get(INPUTID).read(bufferInput, 0, bufferSize);
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
		this.regex = this.getProperties().getProperty(PROPERTYKEY_REGEX, this.getPropertyDefaultValues().get(PROPERTYKEY_REGEX));
		
		this.unescape = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_UNESCAPE, this.getPropertyDefaultValues().get(PROPERTYKEY_UNESCAPE)));
		String replacementString = this.getProperties().getProperty(PROPERTYKEY_REPLACEMENT, this.getPropertyDefaultValues().get(PROPERTYKEY_REPLACEMENT));
		
		if (this.unescape)
			this.replacement = StringEscapeUtils.unescapeJava(replacementString);
		else
			this.replacement = replacementString;
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
