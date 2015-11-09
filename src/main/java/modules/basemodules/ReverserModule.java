package modules.basemodules;

import java.util.Properties;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import common.parallelization.CallbackReceiver;

public class ReverserModule extends ModuleImpl {
	
	// Define I/O IDs (must be unique for every input or output)
	private final String INPUTID = "input";
	private final String OUTPUTID = "reversed";

	public ReverserModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add description
		this.setDescription("Reverses a string input.");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Reverser Module"); // Property key for module name is defined in parent class
		
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

		// Read input, reverse it and output it again. 
		this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(new StringBuffer(super.readStringFromInputPort(this.getInputPorts().get(INPUTID))).reverse().toString());
		
		// Close outputs (important!)
		this.closeAllOutputs();
		
		// Done
		return true;
	}

}
