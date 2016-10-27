package modules.basic_text_processing;

import java.io.BufferedReader;
import java.util.Properties;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import common.parallelization.CallbackReceiver;

import base.workbench.ModuleRunner;

public class ReverserModule extends ModuleImpl {

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(ReverserModule.class, args);
	}

	
	// Define I/O IDs (must be unique for every input or output)
	private static final String INPUTID = "input";
	private static final String OUTPUTID = "reversed";

	// Define a property for line-by-line reversal with switch to turn that on
	private static final String PROPERTYKEY_LINE_BY_LINE = "Reverse each line";
	private boolean reverseLineByLine = false;
	
	

	public ReverserModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add description
		this.setDescription("Reverses a string input.");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Reverser Module"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_LINE_BY_LINE, "false");
		

		// Add property descriptions
		this.getPropertyDescriptions().put(PROPERTYKEY_LINE_BY_LINE, "Reverse line by line instead of reverting the whole input.");
		
		
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
		final InputPort in = this.getInputPorts().get(INPUTID);
		final OutputPort out = this.getOutputPorts().get(OUTPUTID);
		final StringBuffer sb = new StringBuffer();

		// Read input, reverse it and output it again. Either line by line or the whole string.
		if (this.reverseLineByLine) {
			final BufferedReader reader = new BufferedReader(in.getInputReader());
			String line = null;

			while((line = reader.readLine()) != null) {
				sb.append(line);
				out.outputToAllCharPipes(sb.reverse().toString() + System.lineSeparator());
				sb.setLength(0);
			}
			reader.close();
		} else 
		
		{
			sb.append(super.readStringFromInputPort(in));
			out.outputToAllCharPipes(sb.reverse().toString());
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
		final String lineByLineValue = this.getProperties().getProperty(PROPERTYKEY_LINE_BY_LINE); 
		this.reverseLineByLine = Boolean.parseBoolean(lineByLineValue);

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
