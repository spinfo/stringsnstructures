package modules.input_output;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.ModuleImpl;
import modules.OutputPort;

public class ConsoleReaderModule extends ModuleImpl {

	// Property keys
	public static final String PROPERTYKEY_ENCODING = "Encoding";
	public static final String PROPERTYKEY_BUFFERLENGTH = "Buffer length";

	// Local variables
	private final String OUTPUTID = "output";
	private String encoding;
	private int bufferLength = 8192;

	public ConsoleReaderModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// set I/O -- no inputs allowed here (we'll read from stdin)
		OutputPort outputPort = new OutputPort(OUTPUTID, "Character output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputPort);

		// Add description for properties
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_ENCODING,
						"The text encoding of the input (if applicable, else set to empty string)");
		this.getPropertyDescriptions().put(PROPERTYKEY_BUFFERLENGTH,
				"Length of the I/O buffer");

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"Console Reader");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ENCODING, "UTF-8");
		this.getPropertyDefaultValues().put(PROPERTYKEY_BUFFERLENGTH, "8192");

		// Add module description
		this.setDescription("Reads contents from stdin and relays it via character stream.");

	}

	@Override
	public boolean process() throws Exception {

		// Instantiate a new input stream
		InputStream inputStream = System.in;

		// Instantiate input reader if an encoding has been set
		Reader reader = null;
		if (this.encoding != null && !this.encoding.isEmpty()) {
			reader = new InputStreamReader(inputStream, encoding);

			// Define input buffer
			char[] buffer = new char[this.bufferLength];

			// Read file data into buffer and output to writer
			int readChars = reader.read(buffer);
			while (readChars != -1) {

				// Auf Unterbrechersignal pruefen
				if (Thread.interrupted()) {
					reader.close();
					inputStream.close();
					this.closeAllOutputs();
					throw new InterruptedException("Thread has been interrupted.");
				}
				this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(buffer, 0, readChars);
				readChars = reader.read(buffer);
			}

			// close relevant I/O instances
			reader.close();
		}

		// close relevant I/O instances
		inputStream.close();

		this.getOutputPorts().get(OUTPUTID).close();

		// Success
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
		super.applyProperties();
	}

}
