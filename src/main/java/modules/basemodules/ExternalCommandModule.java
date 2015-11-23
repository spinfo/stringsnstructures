package modules.basemodules;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import modules.BytePipe;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.NotSupportedException;
import modules.OutputPort;

import common.parallelization.CallbackReceiver;

public class ExternalCommandModule extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_COMMAND = "command";
	
	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "input";
	private static final String ID_OUTPUT = "output";
	
	// Local variables
	private String command;

	public ExternalCommandModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Executes an external system command. Can use streamed character or byte input as command stdin.");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_COMMAND, "Command to execute. Please specify the complete path and parameters as used on the system console. On MS Windows, use '\\\\' as path separator.");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "External Command Module"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_COMMAND, "/bin/ls -l /tmp");
		
		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~).
		 * Every port can support a range of pipe types (currently
		 * byte or character pipes). Output ports can provide data
		 * to multiple pipe instances at once, input ports can
		 * in contrast only obtain data from one pipe instance.
		 */
		InputPort inputPort = new InputPort(ID_INPUT, "Character or byte input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		inputPort.addSupportedPipe(BytePipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "Character or byte output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		outputPort.addSupportedPipe(BytePipe.class);
		
		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		
	}

	@Override
	public boolean process() throws Exception {
		
		Process process = Runtime.getRuntime().exec(this.command);
		
		// Determine whether the input port is connected (and we need to feed the input to the executed command)
		if (this.getInputPorts().get(ID_INPUT).isConnected()){
			
			// Determine the class of the connected input
			if (BytePipe.class.isAssignableFrom(this.getInputPorts().get(ID_INPUT).getPipe().getClass())){
				// Read byte input
				byte[] buffer = new byte[1024];
				int readBytes = this.getInputPorts().get(ID_INPUT).getInputStream().read(buffer);
				while (readBytes != -1){
					process.getOutputStream().write(buffer, 0, readBytes);
					readBytes = this.getInputPorts().get(ID_INPUT).getInputStream().read(buffer);
				}
				process.getOutputStream().flush();
				process.getOutputStream().close();
			} else if (CharPipe.class.isAssignableFrom(this.getInputPorts().get(ID_INPUT).getPipe().getClass())) {
				Writer processInputWriter = new OutputStreamWriter(process.getOutputStream());
				// Read char input
				char[] buffer = new char[1024];
				int readChars = this.getInputPorts().get(ID_INPUT).getInputReader().read(buffer);
				while (readChars != -1){
					processInputWriter.write(buffer, 0, readChars);
					readChars = this.getInputPorts().get(ID_INPUT).getInputReader().read(buffer);
				}
				processInputWriter.flush();
				processInputWriter.close();
				
			} else {
				// The connected pipe is of unknown type
				throw new NotSupportedException("The input port's pipe class '"+this.getInputPorts().get(ID_INPUT).getPipe().getClass().getCanonicalName()+"' is unknown -- I do not know how to handle this.");
			}
			
			
		}
		
		// Write output to both byte and char pipes (if connected)
		byte[] buffer = new byte[1024];
		int readBytes = process.getInputStream().read(buffer);
		while (readBytes != -1) {
			this.getOutputPorts().get(ID_OUTPUT)
					.outputToAllBytePipes(buffer, 0, readBytes);
			this.getOutputPorts()
					.get(ID_OUTPUT)
					.outputToAllCharPipes(
							new String(buffer).substring(0, readBytes));
			readBytes = process.getInputStream().read(buffer);
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
		this.command = this.getProperties().getProperty(PROPERTYKEY_COMMAND, this.getPropertyDefaultValues().get(PROPERTYKEY_COMMAND));
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
