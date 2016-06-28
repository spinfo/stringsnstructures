package modules.examples;

import java.util.Properties;
import java.util.Scanner;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import common.parallelization.CallbackReceiver;

public class ExampleModule extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_DELIMITER_A = "delimiter A";
	public static final String PROPERTYKEY_DELIMITER_B = "delimiter B";
	public static final String PROPERTYKEY_DELIMITER_OUTPUT = "delimiter out";
	
	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT_A = "input A";
	private static final String ID_INPUT_B = "input B";
	private static final String ID_OUTPUT_ENTWINED = "entwined";
	private static final String ID_OUTPUT_ENTWINED_CAPITALISED = "capitals";
	
	// Local variables
	private String inputdelimiter_a;
	private String inputdelimiter_b;
	private String outputdelimiter;

	public ExampleModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("<p>Segments two inputs and entwines them.</p><p>Among other things, you can use it<br/><ul><li>as a template to base your own modules on,</li><li>to review basic practices, like I/O,</li><li>and to get an overview of the standard implementations needed.</li></ul></p>");
		
		// Add module category
		this.setCategory("Examples");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_A, "Regular expression to use as segmentation delimiter for input A");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_B, "Regular expression to use as segmentation delimiter for input B");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT, "String to insert as segmentation delimiter into the output");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Example Module"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_A, "[\\s]+");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_B, "[\\s]+");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT, "\t");
		
		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~).
		 * Every port can support a range of pipe types (currently
		 * byte or character pipes). Output ports can provide data
		 * to multiple pipe instances at once, input ports can
		 * in contrast only obtain data from one pipe instance.
		 */
		InputPort inputPort1 = new InputPort(ID_INPUT_A, "Plain text character input A.", this);
		inputPort1.addSupportedPipe(CharPipe.class);
		InputPort inputPort2 = new InputPort(ID_INPUT_B, "Plain text character input B.", this);
		inputPort2.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT_ENTWINED, "Plain text character output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		OutputPort capsOutputPort = new OutputPort(ID_OUTPUT_ENTWINED_CAPITALISED, "Plain text character output (all uppercase).", this);
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
		 * It reads from two inputs, segments them via
		 * the specified delimiters and entwines the
		 * result.
		 * Just used to exemplify a basic module. 
		 */
		
		// Construct scanner instances for input segmentation
		Scanner inputAScanner = new Scanner(this.getInputPorts().get(ID_INPUT_A).getInputReader());
		inputAScanner.useDelimiter(this.inputdelimiter_a);
		Scanner inputBScanner = new Scanner(this.getInputPorts().get(ID_INPUT_B).getInputReader());
		inputBScanner.useDelimiter(this.inputdelimiter_b);
		
		// Input read loop
		while (true){
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				inputAScanner.close();
				inputBScanner.close();
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// Check whether input A has more segments
			if (inputAScanner.hasNext()){
				// Determine next segment
				String inputASegment = inputAScanner.next();
				// Write to outputs
				this.getOutputPorts().get(ID_OUTPUT_ENTWINED).outputToAllCharPipes(inputASegment.concat(outputdelimiter));
				this.getOutputPorts().get(ID_OUTPUT_ENTWINED_CAPITALISED).outputToAllCharPipes(inputASegment.concat(outputdelimiter).toUpperCase());
			}
			
			// Check whether input B has more segments
			if (inputBScanner.hasNext()){
				// Determine next segment
				String inputBSegment = inputBScanner.next();
				// Write to outputs
				this.getOutputPorts().get(ID_OUTPUT_ENTWINED).outputToAllCharPipes(inputBSegment.concat(outputdelimiter));
				this.getOutputPorts().get(ID_OUTPUT_ENTWINED_CAPITALISED).outputToAllCharPipes(inputBSegment.concat(outputdelimiter).toUpperCase());
			}
			
			
			// If none of the inputs has any more segments, break the loop
			if (!(inputAScanner.hasNext() && inputBScanner.hasNext()))
				break;
		}

		/*
		 * Close input scanners. NOTE: A module should not attempt to close its
		 * inputs before the module providing it has done so itself! Please
		 * either leave open any readers that would close the underlying
		 * this.getInputPorts().get().getInputReader() (or getInputStream()
		 * respectively) or only do so after you can be sure that the providing
		 * module has already closed them (like in this instance).
		 */
		inputAScanner.close();
		inputBScanner.close();
		
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
		this.inputdelimiter_a = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_A, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_A));
		this.inputdelimiter_b = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_B, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_B));
		this.outputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT));
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
