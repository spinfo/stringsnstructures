package modules.input_output;

import java.util.Properties;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import common.parallelization.CallbackReceiver;

public class BufferModule extends ModuleImpl {
	
	// Define I/O IDs (must be unique for every input or output)
	private final String INPUT1ID = "input";
	private final String OUTPUTNORMID = "output";

	public BufferModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Stores input until the pipe closes and only then writes it to the output.");
		
		// Add module category
		this.setCategory("I/O");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Buffer Module"); // Property key for module name is defined in parent class
		
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
		
		// Write to outputs
		this.getOutputPorts().get(OUTPUTNORMID).outputToAllCharPipes(outputBuffer.toString());
		
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
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
