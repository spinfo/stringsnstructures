package modules.segmentJoinerModule;

import java.util.Properties;
import java.util.Scanner;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import common.StringUnescaper;
import common.parallelization.CallbackReceiver;

public class SegmentJoinerModule extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_DELIMITER_INPUT_SEGMENT = "segment input delimiter regex";
	public static final String PROPERTYKEY_DELIMITER_INPUT_STRING = "string input delimiter regex";
	public static final String PROPERTYKEY_DELIMITER_OUTPUT_SEGMENT = "segment output delimiter";
	public static final String PROPERTYKEY_DELIMITER_OUTPUT_STRING = "string output delimiter";
	public static final String PROPERTYKEY_DELIMITER_OUTPUT_GROUP = "group output delimiter";
	public static final String PROPERTYKEY_OUTPUT_ORIGINAL = "output original string";
	
	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "input";
	private static final String ID_OUTPUT = "output";
	
	// Local variables
	private String inputdelimiter_segment;
	private String inputdelimiter_string;
	private String outputdelimiter_segment;
	private String outputdelimiter_string;
	private String outputdelimiter_group;
	private boolean outputOriginal;

	public SegmentJoinerModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Takes a segmented string as input and outputs every possible combination of it with two neighboring segments being joined.");
		
		// Add module category
		this.setCategory("Segmentation");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_INPUT_SEGMENT, "Regular expression to use as segmentation delimiter for the segments of the string.");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_INPUT_STRING, "Regular expression to use as segmentation delimiter for the strings.");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT_SEGMENT, "String to insert as segmentation delimiter between segments (does understand escaped sequences and unescapes them).");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT_STRING, "String to insert as segmentation delimiter between strings (does understand escaped sequences and unescapes them).");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT_GROUP, "String to insert as segmentation delimiter between groups (does understand escaped sequences and unescapes them).");
		this.getPropertyDescriptions().put(PROPERTYKEY_OUTPUT_ORIGINAL, "Include the original (non-joined) string in the output (as first element) [true or false].");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Segment Joiner"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT_SEGMENT, "\\|");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT_STRING, "\\n");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT_SEGMENT, "|");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT_STRING, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT_GROUP, "\\n");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTPUT_ORIGINAL, "true");
		
		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~).
		 * Every port can support a range of pipe types (currently
		 * byte or character pipes). Output ports can provide data
		 * to multiple pipe instances at once, input ports can
		 * in contrast only obtain data from one pipe instance.
		 */
		InputPort inputPort = new InputPort(ID_INPUT, "Plain text character input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "Plain text character output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		
	}

	@Override
	public boolean process() throws Exception {
		
		/*
		 * This module doesn't do much useful processing.
		 * It reads from two inputs, segments them via
		 * the specified delimiters and entwines the
		 * result.
		 * Just used to exemplify a basic module. 
		 */
		
		// Construct scanner instance for input segmentation (strings)
		Scanner stringInputScanner = new Scanner(this.getInputPorts().get(ID_INPUT).getInputReader());
		stringInputScanner.useDelimiter(this.inputdelimiter_string);
		
		// Input read loop
		while (stringInputScanner.hasNext()){
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				stringInputScanner.close();
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}

			// Split next string into segments
			String[] segments = stringInputScanner.next().split(this.inputdelimiter_segment);
			
			// Index variable for loop over different segmentation possibilities
			int i=0;
			
			// We can include the original string in the output just by initially decreasing the loop index by one.
			if (this.outputOriginal)
				i=-1;
			
			// Loop over different segmentation possibilities
			for (; i<segments.length-1; i++){
				
				// Loop over segments
				for (int j=0; j<segments.length; j++){
					this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(segments[j]);

					// Conditionally omit delimiter
					if (i!=j && (j+1)<segments.length)
						this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.outputdelimiter_segment);
				}
				// Omit delimiter if this is the last segment
				if ((i+1)<segments.length-1)
					this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.outputdelimiter_string);
			}
			
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.outputdelimiter_group);
		}

		//Close input scanner.
		stringInputScanner.close();
		
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
		this.inputdelimiter_segment = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_INPUT_SEGMENT, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT_SEGMENT));
		this.inputdelimiter_string = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_INPUT_STRING, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT_STRING));
		String outputdelimiter_segment_string = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT_SEGMENT, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT_SEGMENT));
		if (outputdelimiter_segment_string != null)
			this.outputdelimiter_segment = StringUnescaper.unescape_perl_string(outputdelimiter_segment_string);
		String outputdelimiter_string_string = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT_STRING, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT_STRING));
		if (outputdelimiter_string_string != null)
			this.outputdelimiter_string = StringUnescaper.unescape_perl_string(outputdelimiter_string_string);
		String outputdelimiter_group_string = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT_GROUP, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT_GROUP));
		if (outputdelimiter_group_string != null)
			this.outputdelimiter_group = StringUnescaper.unescape_perl_string(outputdelimiter_group_string);
		String outputOriginal_string = this.getProperties().getProperty(PROPERTYKEY_OUTPUT_ORIGINAL, this.getPropertyDefaultValues().get(PROPERTYKEY_OUTPUT_ORIGINAL));
		if (outputOriginal_string != null)
			this.outputOriginal = Boolean.parseBoolean(outputOriginal_string);
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
