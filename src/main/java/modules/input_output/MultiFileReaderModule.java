package modules.input_output;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class MultiFileReaderModule extends ModuleImpl {

	// Property keys
	public static final String PROPERTYKEY_ENCODING = "Encoding";
	public static final String PROPERTYKEY_BUFFERLENGTH = "Buffer length";
	public static final String PROPERTYKEY_REGEX = "RegEx file name filter";


	// Local variables
	private final String ID_INPUT = "input";
	private final String ID_OUTPUT = "output";
	private File file;
	private String encoding;
	private int bufferLength = 8192;
	private String regex = null;

	public MultiFileReaderModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		// description and default value for the regular expression
		this.getPropertyDescriptions().put(PROPERTYKEY_REGEX, "Regular expression to match file names (including suffix).");
		this.getPropertyDefaultValues().put(PROPERTYKEY_REGEX, ".+");

		// Define I/O
		InputPort inputPort = new InputPort(ID_INPUT,
				"JSON-formatted list of file paths (output from FileFinderModule).", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT,
				"Char output of concatenated file contents.", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add description for properties
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_ENCODING,
						"The text encoding of the input files (if applicable, else set to empty string)");
		this.getPropertyDescriptions().put(PROPERTYKEY_BUFFERLENGTH, "Length of the I/O buffer");

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Multi File Reader");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ENCODING, "UTF-8");
		this.getPropertyDefaultValues().put(PROPERTYKEY_BUFFERLENGTH, "8192");

		// Add module description
		this.setDescription("Reads a (JSON formatted) list of files.");
		
		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
	}

	@Override
	public boolean process() throws Exception {
		
		/*
		 * read file list from JSON input
		 */
		
		// Read JSON from input & parse it
		JsonArray jsonPaths = new JsonParser().parse(this.getInputPorts().get(ID_INPUT).getInputReader()).getAsJsonArray();
		
		for (int i = 0; i < jsonPaths.size(); i++){
			/*
			 * write to both output channels (stream/writer)
			 */
			this.file = new File(jsonPaths.get(i).getAsJsonObject().get("path").getAsString());
			if (!this.file.exists() || !this.file.getName().matches(this.regex)) continue;
			
			StringBuilder sb = new StringBuilder();

			try {
				// Instantiate a new input stream
				InputStream fileInputStream = new FileInputStream(this.file);

				// Instantiate input reader if an encoding has been set
				Reader fileReader = null;
				if (this.encoding != null && !this.encoding.isEmpty()) {
					fileReader = new InputStreamReader(fileInputStream, encoding);

					// Define input buffer
					char[] buffer = new char[this.bufferLength];

					// Read file data into buffer and output to writer
					int readChars = fileReader.read(buffer);
					while (readChars != -1) {

						// Auf Unterbrechersignal pruefen
						if (Thread.interrupted()) {
							fileReader.close();
							fileInputStream.close();
							this.closeAllOutputs();
							throw new InterruptedException(
									"Thread has been interrupted.");
						}
						this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(new String(buffer));
						readChars = fileReader.read(buffer);
					}
					
					// close relevant I/O instances
					fileReader.close();
				}

				// close relevant I/O instances
				fileInputStream.close();
				
				//write to output port
				this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(sb.toString() + "\n");
			} catch (IOException e) {
				/*
				 * The inputstream does not seem to be connected or another
				 * I/O-error occurred
				 */
			}
		}
		
		//close output port
		this.getOutputPorts().get(ID_OUTPUT).close();
		
		return true;
	}

	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		
		if (this.getProperties().containsKey(PROPERTYKEY_ENCODING))
			this.encoding = this.getProperties().getProperty(
					PROPERTYKEY_ENCODING);
		
		if (this.getProperties().containsKey(PROPERTYKEY_BUFFERLENGTH))
			this.bufferLength = Integer.parseInt(this.getProperties()
					.getProperty(PROPERTYKEY_BUFFERLENGTH));
		
		this.regex = this.getProperties().getProperty(PROPERTYKEY_REGEX,
				this.getPropertyDefaultValues().get(PROPERTYKEY_REGEX));
		
		super.applyProperties();
	}
	
}
