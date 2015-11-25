package modules.basemodules;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
	public static final String PROPERTYKEY_WORKDIR = "directory";
	
	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "input";
	private static final String ID_OUTPUT_STD = "stdout";
	private static final String ID_OUTPUT_ERR = "stderr";
	
	// Local variables
	private String command;
	private String workDir;

	public ExternalCommandModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Executes an external system command. Can use streamed character or byte input as command stdin.");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_COMMAND, "<html>Command to execute. Please specify the complete path.<br/>"
				+ "Explicit parameters can be separated by using ',' (comma) as a delimiter<br/>"
				+ "(use '\\,' if you want a literal comma). Most of the times with linux/unix<br/>"
				+ "it is best to wrap the command in a shell, in this case meaning that you<br/>"
				+ "prefix your command with '/bin/sh,-c,'. On MS Windows, please use '\\\\'<br/>"
				+ "as path separator.</html>");
		this.getPropertyDescriptions().put(PROPERTYKEY_WORKDIR, "<html>Working directory to execute the command in. Please specify the complete path.<br/>"
				+ "On MS Windows, use '\\\\' as path separator.</html>");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "External Command Module"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_COMMAND, "/bin/sh,-c,(echo '>Seq 1'; cat -)");
		this.getPropertyDefaultValues().put(PROPERTYKEY_WORKDIR, "/tmp");
		
		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~).
		 * Every port can support a range of pipe types (currently
		 * byte or character pipes). Output ports can provide data
		 * to multiple pipe instances at once, input ports can
		 * in contrast only obtain data from one pipe instance.
		 */
		InputPort inputPort = new InputPort(ID_INPUT, "Standard input (character or byte).", this);
		inputPort.addSupportedPipe(CharPipe.class);
		inputPort.addSupportedPipe(BytePipe.class);
		OutputPort standardOutputPort = new OutputPort(ID_OUTPUT_STD, "Standard output (character or byte).", this);
		standardOutputPort.addSupportedPipe(CharPipe.class);
		standardOutputPort.addSupportedPipe(BytePipe.class);
		OutputPort errorOutputPort = new OutputPort(ID_OUTPUT_ERR, "Error output (character).", this);
		errorOutputPort.addSupportedPipe(CharPipe.class);
		
		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(standardOutputPort);
		super.addOutputPort(errorOutputPort);
		
	}

	@Override
	public boolean process() throws Exception {
		
		// Check whether workdir is valid
		File workDirFile = new File(this.workDir);
		if (!workDirFile.exists() || !workDirFile.isDirectory())
			throw new Exception("The specified working directory ("+this.workDir+") is invalid.");
		
		// Parse command string
		String delim = ",";
		String regex = "(?<!\\\\)" + java.util.regex.Pattern.quote(delim);
		String[] commandArray = this.command.split(regex);
		// Replace masked ',' characters
		for (int i=0; i<commandArray.length; i++){
			commandArray[i] = commandArray[i].replaceAll("\\\\\\,",",");
		}
		
		// Run process
		ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
		//Map<String, String> env = processBuilder.environment();
		//env.put("LC_ALL", "en_GB.UTF-8");
		processBuilder.directory(new File(this.workDir));
		Process process = processBuilder.start();

		// Process output buffers
		byte[] processStdoutBuffer = new byte[1024];
		byte[] processStderrBuffer = new byte[1024];
		
		// Determine whether the input port is connected (and we need to feed the input to the executed command)
		if (this.getInputPorts().get(ID_INPUT).isConnected()){
			
			// Determine the class of the connected input
			if (BytePipe.class.isAssignableFrom(this.getInputPorts().get(ID_INPUT).getPipe().getClass())){
				// Read byte input
				byte[] moduleInputBuffer = new byte[1024];
				int moduleInputReadBytes = this.getInputPorts().get(ID_INPUT).getInputStream().read(moduleInputBuffer);
				while (moduleInputReadBytes != -1){
					// Write to process input
					process.getOutputStream().write(moduleInputBuffer, 0, moduleInputReadBytes);
					
					// Check for interrupt signal
					if (Thread.interrupted()) {
						process.destroyForcibly();
						this.closeAllOutputs();
						throw new InterruptedException("Thread has been interrupted.");
					}
					
					// Read from stream
					moduleInputReadBytes = this.getInputPorts().get(ID_INPUT).getInputStream().read(moduleInputBuffer);
					
					// Write available std and error output (needs to be done, else the system command buffer may overflow)
					this.relayProcessOutput(process.getInputStream(), this.getOutputPorts().get(ID_OUTPUT_STD), processStdoutBuffer, false);
					this.relayProcessOutput(process.getErrorStream(), this.getOutputPorts().get(ID_OUTPUT_ERR), processStderrBuffer, false);
				}
				try {
					process.getOutputStream().flush();
					process.getOutputStream().close();
				} catch (IOException e){}
			} else if (CharPipe.class.isAssignableFrom(this.getInputPorts().get(ID_INPUT).getPipe().getClass())) {
				Writer processInputWriter = new OutputStreamWriter(process.getOutputStream());
				// Read char input
				char[] buffer = new char[1024];
				int readChars = this.getInputPorts().get(ID_INPUT).getInputReader().read(buffer);
				while (readChars != -1){
					// Write to process input
					processInputWriter.write(buffer, 0, readChars);
					
					// Check for interrupt signal
					if (Thread.interrupted()) {
						process.destroyForcibly();
						this.closeAllOutputs();
						throw new InterruptedException("Thread has been interrupted.");
					}
					
					// Read from stream
					readChars = this.getInputPorts().get(ID_INPUT).getInputReader().read(buffer);
					
					// Write available std and error output (needs to be done, else the system command buffer may overflow)
					this.relayProcessOutput(process.getInputStream(), this.getOutputPorts().get(ID_OUTPUT_STD), processStdoutBuffer, false);
					this.relayProcessOutput(process.getErrorStream(), this.getOutputPorts().get(ID_OUTPUT_ERR), processStderrBuffer, false);
				}
				try {
					processInputWriter.close();
				} catch (IOException e){}
				
			} else {
				// The connected pipe is of unknown type
				throw new NotSupportedException("The input port's pipe class '"+this.getInputPorts().get(ID_INPUT).getPipe().getClass().getCanonicalName()+"' is unknown -- I do not know how to handle this.");
			}
			
			
		}
		
		// Write std and error output to both byte and char pipes (if connected)
		this.relayProcessOutput(process.getInputStream(), this.getOutputPorts().get(ID_OUTPUT_STD), processStdoutBuffer, true);
		this.relayProcessOutput(process.getErrorStream(), this.getOutputPorts().get(ID_OUTPUT_ERR), processStderrBuffer, true);
		
		// Close outputs (important!)
		this.closeAllOutputs();
		
		// Done
		return true;
	}
	
	/**
	 * Reads a stream and relays it to the specified output.
	 * @param processInputStream Process input stream
	 * @param outputPort Outputport to use
	 * @param buffer Buffer
	 * @param readUntilClose
	 *            If true, the process output stream is read until it closes.If
	 *            false, only the readily available output is read.
	 * @throws IOException Thrown if an I/O error occurs
	 */
	private void relayProcessOutput(InputStream processInputStream, OutputPort outputPort, byte[] buffer, boolean readUntilClose) throws IOException{
		
		// If there is nothing to read yet (and we are not ordered to wait) we can return right away
		if (!readUntilClose && processInputStream.available() < 1)
			return;
		
		// Read available input data into buffer
		int readBytes = -1;
		try {
			readBytes = processInputStream.read(buffer);
		} catch (IOException e){
			// Assume the input stream closed if an IO error occurs
		}
		
		// Loop until the stream closes
		while (readBytes != -1) {
			if (outputPort.supportsPipeClass(BytePipe.class))
				outputPort.outputToAllBytePipes(buffer, 0, readBytes);
			if (outputPort.supportsPipeClass(CharPipe.class))
				outputPort.outputToAllCharPipes(new String(buffer,0,readBytes));
			
			// Read from process stream
			if (readUntilClose || processInputStream.available() > 0)
				try {
					readBytes = processInputStream.read(buffer);
				} catch (IOException e){
					// Assume the input stream closed if an IO error occurs
					return;
				}
			else
				break;
		}
	}
	
	@Override
	public void applyProperties() throws Exception {
		
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();
		
		// Apply own properties
		this.command = this.getProperties().getProperty(PROPERTYKEY_COMMAND, this.getPropertyDefaultValues().get(PROPERTYKEY_COMMAND));
		this.workDir = this.getProperties().getProperty(PROPERTYKEY_WORKDIR, this.getPropertyDefaultValues().get(PROPERTYKEY_WORKDIR));
		
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
