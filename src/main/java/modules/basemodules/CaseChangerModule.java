package modules.basemodules;

import java.util.Locale;
import java.util.Properties;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import common.parallelization.CallbackReceiver;

public class CaseChangerModule extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_CASE = "Change to";
	public static final String PROPERTYKEY_LOCALE = "Locale";
	
	// Define I/O IDs (must be unique for every input or output)
	private final String INPUT1ID = "input";
	private final String OUTPUTNORMID = "output";
	private boolean toLowercase = true;
	private Locale locale;

	public CaseChangerModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Changes the input to uppercase or lowercase.");
		
		// Add module category
		this.setCategory("Basic text processing");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_CASE, "Case to change the input to. Accepted values are 'lower[case]' or 'upper[case]'.");
		this.getPropertyDescriptions().put(PROPERTYKEY_LOCALE, "Tag of the locale to use for the case change. Accepts a IETF BCP 47 language tag string (can be as simple as 'en-US' or 'de-DE'; for details, see https://tools.ietf.org/html/bcp47).");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Case Changer"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_CASE, "lowercase");
		this.getPropertyDefaultValues().put(PROPERTYKEY_LOCALE, "en-US");
		
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
		
		// Variables used for input data
		int bufferSize = 1024;
		char[] bufferInput1 = new char[bufferSize];
		StringBuffer outputBuffer = new StringBuffer();
		
		// Read first chunk of data from both inputs
		int readCharsInput1 = this.getInputPorts().get(INPUT1ID).read(bufferInput1, 0, bufferSize);
		
		// Loop until no more data can be read from input
		while (readCharsInput1 != -1){
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			if (readCharsInput1<bufferInput1.length)
				outputBuffer.append(new String(bufferInput1).substring(0, readCharsInput1));
			else
				outputBuffer.append(bufferInput1);
			
			// Read next chunk of data from both inputs
			readCharsInput1 = this.getInputPorts().get(INPUT1ID).read(bufferInput1, 0, bufferSize);
		}
		
		String outputString;
		
		if (this.toLowercase)
			outputString = outputBuffer.toString().toLowerCase();
		else
			outputString = outputBuffer.toString().toUpperCase();
		
		// Write to outputs
		this.getOutputPorts().get(OUTPUTNORMID).outputToAllCharPipes(outputString);
		
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
		
		String caseString = this.getProperties().getProperty(PROPERTYKEY_CASE, this.getPropertyDefaultValues().get(PROPERTYKEY_CASE));
		if (caseString != null){
			if (caseString.startsWith("lower"))
				this.toLowercase = true;
			else if (caseString.startsWith("upper"))
				this.toLowercase = false;
			else throw new Exception("Invalid setting for "+PROPERTYKEY_CASE);
		}
		
		String localeString = this.getProperties().getProperty(PROPERTYKEY_LOCALE, this.getPropertyDefaultValues().get(PROPERTYKEY_LOCALE));
		if (localeString != null){
			this.locale = Locale.forLanguageTag(localeString);
			if (this.locale == null)
				throw new Exception("Locale '"+PROPERTYKEY_LOCALE+"' not found.");
		}
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
