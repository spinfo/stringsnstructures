package modules.input_output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import modules.BytePipe;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.NotSupportedException;
import common.parallelization.CallbackReceiver;

/**
 * Writes any input to file
 * 
 * @author Marcel Boeing
 *
 */
public class FileWriterModule extends ModuleImpl {

	// Property keys
	public static final String PROPERTYKEY_OUTPUTFILE = "outputfile";
	public static final String PROPERTYKEY_USEGZIP = "Use GZIP";
	public static final String PROPERTYKEY_ENCODING = "Encoding";
	public static final String PROPERTYKEY_BUFFERLENGTH = "Buffer length";

	// Local variables
	private final String INPUTID = "input";
	private String filePath;
	private boolean useGzip = false;
	private String encoding;
	private int bufferLength = 8192;

	public FileWriterModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_OUTPUTFILE,
				"Path to the output file");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_USEGZIP,
						"Set to 'true' if the output file is to be compressed using GZIP");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_ENCODING,
						"The text encoding of the output file (if input is a char pipe)");
		this.getPropertyDescriptions().put(PROPERTYKEY_BUFFERLENGTH,
				"Length of the I/O buffer");

		// Determine system properties (for setting default values that make
				// sense)
		String fs = System.getProperty("file.separator");
		String homedir = System.getProperty("user.home");
		
		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"File Writer");
		this.getPropertyDefaultValues().put(PROPERTYKEY_USEGZIP, "false");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ENCODING, "UTF-8");
		this.getPropertyDefaultValues().put(PROPERTYKEY_BUFFERLENGTH, "8192");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTPUTFILE, homedir + fs + "output.txt");

		// Define I/O
		InputPort inputPort = new InputPort(INPUTID,
				"Byte or character input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		inputPort.addSupportedPipe(BytePipe.class);
		super.addInputPort(inputPort);

		// Add module description
		this.setDescription("Writes received input to a file. Can apply GZIP compression.");
		
		// Add module category
		this.setCategory("I/O");
	}

	@Override
	public boolean process() throws Exception {

		// Determine input port
		InputPort inputPort = this.getInputPorts().get(INPUTID);
		
		// Keep track of whether an input has been successfully read
		boolean successfullyReadInput = false;

		/*
		 * try to read from the stream input channel
		 */
		try {
			if (inputPort.getInputStream() != null) {
				// Instantiate a new output stream
				OutputStream fileOutputStream = new FileOutputStream(new File(
						this.filePath));

				// Use GZIP if requested
				if (this.useGzip)
					fileOutputStream = new GZIPOutputStream(fileOutputStream);

				// Define input buffer
				byte[] buffer = new byte[this.bufferLength];

				// Read file data into buffer and write to outputstream
				int readBytes = inputPort.getInputStream().read(buffer);
				while (readBytes != -1) {

					// Check for interrupt signal
					if (Thread.interrupted()) {
						fileOutputStream.close();
						throw new InterruptedException(
								"Thread has been interrupted.");
					}

					fileOutputStream.write(buffer, 0, readBytes);
					readBytes = inputPort.getInputStream().read(buffer);
				}

				// close output stream
				fileOutputStream.close();

				// Log message
				Logger.getLogger(this.getClass().getSimpleName()).log(
						Level.INFO,
						"Wrote byte stream input into " + this.filePath);
				
				// Keep track of whether an input has been successfully read
				successfullyReadInput = true;

			}
		} catch (NotSupportedException e) {

		}

		try {
			if (!successfullyReadInput && inputPort.getInputReader() != null) {
				/*
				 * The inputstream does not seem to be connected -- try
				 * inputreader instead
				 */

				// Instantiate a new output stream
				OutputStream fileOutputStream = new FileOutputStream(new File(
						this.filePath));

				// Use GZIP if requested
				if (this.useGzip)
					fileOutputStream = new GZIPOutputStream(fileOutputStream);

				// Instantiate a new file writer
				Writer fileWriter = new OutputStreamWriter(fileOutputStream,
						this.encoding);

				// Define input buffer
				char[] buffer = new char[this.bufferLength];

				// Read file data into buffer and output to writer
				int readBytes = inputPort.getInputReader().read(buffer);
				while (readBytes != -1) {

					// Check for interrupt signal
					if (Thread.interrupted()) {
						fileWriter.close();
						fileOutputStream.close();
						throw new InterruptedException(
								"Thread has been interrupted.");
					}

					fileWriter.write(buffer, 0, readBytes);
					readBytes = inputPort.getInputReader().read(buffer);
				}

				// close outputs
				fileWriter.close();
				fileOutputStream.close();

				// Log message
				Logger.getLogger(this.getClass().getSimpleName())
						.log(Level.INFO,
								"Wrote character input to " + this.filePath);
				
				// Keep track of whether an input has been successfully read
				successfullyReadInput = true;
			}
		} catch (NotSupportedException e) {

		}

		// If no input has been successfully read throw an exception
		if (!successfullyReadInput)
			throw new Exception(
					"The input of this module does not seem to be connected to anything.");

		// Success
		return true;

	}

	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		
		if (this.getProperties().containsKey(PROPERTYKEY_OUTPUTFILE))
			this.filePath = this.getProperties().getProperty(
					PROPERTYKEY_OUTPUTFILE);
		if (this.getProperties().containsKey(PROPERTYKEY_USEGZIP))
			this.useGzip = Boolean.parseBoolean(this.getProperties()
					.getProperty(PROPERTYKEY_USEGZIP));
		if (this.getProperties().containsKey(PROPERTYKEY_ENCODING))
			this.encoding = this.getProperties().getProperty(
					PROPERTYKEY_ENCODING);
		if (this.getProperties().containsKey(PROPERTYKEY_BUFFERLENGTH))
			this.bufferLength = Integer.parseInt(this.getProperties()
					.getProperty(PROPERTYKEY_BUFFERLENGTH));
		super.applyProperties();
	}

}
