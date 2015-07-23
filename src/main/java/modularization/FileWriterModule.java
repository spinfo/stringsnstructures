package modularization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import parallelization.CallbackReceiver;

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

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"File Writer");
		this.getPropertyDefaultValues().put(PROPERTYKEY_USEGZIP, "false");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ENCODING, "UTF-8");
		this.getPropertyDefaultValues().put(PROPERTYKEY_BUFFERLENGTH, "8192");

		// Define I/O
		this.getSupportedInputs().add(BytePipe.class);
		this.getSupportedInputs().add(CharPipe.class);

		// Add module description
		this.setDescription("Writes received input to a file. Can apply GZIP compression.");
	}

	@Override
	public boolean process() throws Exception {

		/*
		 * read from both input channels (reader/inputstream) -- if the first
		 * one is not connected to another module's output, it will throw an
		 * exception (that we will catch)
		 */
		try {
			// Instantiate a new output stream
			OutputStream fileOutputStream = new FileOutputStream(new File(
					this.filePath));

			// Use GZIP if requested
			if (this.useGzip)
				fileOutputStream = new GZIPOutputStream(fileOutputStream);

			// Define input buffer
			byte[] buffer = new byte[this.bufferLength];

			// Read file data into buffer and write to outputstream
			int readBytes = this.getInputBytePipe().getInput().read(buffer);
			while (readBytes != -1) {

				// Check for interrupt signal
				if (Thread.interrupted()) {
					fileOutputStream.close();
					throw new InterruptedException(
							"Thread has been interrupted.");
				}

				fileOutputStream.write(buffer, 0, readBytes);
				readBytes = this.getInputBytePipe().getInput().read(buffer);
			}

			// close output stream
			fileOutputStream.close();

			// Log message
			Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO,
					"Wrote byte stream input into " + this.filePath);

		} catch (Exception e) {
			/*
			 * The inputstream does not seem to be connected -- try inputreader
			 * instead
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
			int readBytes = this.getInputCharPipe().getInput().read(buffer);
			while (readBytes != -1) {

				// Check for interrupt signal
				if (Thread.interrupted()) {
					fileWriter.close();
					fileOutputStream.close();
					throw new InterruptedException(
							"Thread has been interrupted.");
				}

				fileWriter.write(buffer, 0, readBytes);
				readBytes = this.getInputCharPipe().getInput().read(buffer);
			}

			// close outputs
			fileWriter.close();
			fileOutputStream.close();

			// Log message
			Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO,
					"Wrote character input to " + this.filePath);
		}

		// Success
		return true;

	}

	@Override
	public void applyProperties() throws Exception {
		if (this.getProperties().containsKey(PROPERTYKEY_OUTPUTFILE))
			this.filePath = this.getProperties().getProperty(
					PROPERTYKEY_OUTPUTFILE);
		if (this.getProperties().containsKey(PROPERTYKEY_USEGZIP))
			this.useGzip = Boolean.parseBoolean(this.getProperties()
					.getProperty(PROPERTYKEY_USEGZIP));
		if (this.getProperties().containsKey(PROPERTYKEY_ENCODING))
			this.encoding = this.getProperties().getProperty(
					PROPERTYKEY_ENCODING);
		else
			this.encoding = this.getPropertyDefaultValues().get(
					PROPERTYKEY_ENCODING);
		if (this.getProperties().containsKey(PROPERTYKEY_BUFFERLENGTH))
			this.bufferLength = Integer.parseInt(this.getProperties()
					.getProperty(PROPERTYKEY_BUFFERLENGTH));
		else if (this.getPropertyDefaultValues().containsKey(
				PROPERTYKEY_BUFFERLENGTH))
			this.bufferLength = Integer.parseInt(this
					.getPropertyDefaultValues().get(PROPERTYKEY_BUFFERLENGTH));
		super.applyProperties();
	}

}
