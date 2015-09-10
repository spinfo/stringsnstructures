package modules.basemodules;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import modules.BytePipe;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;

import common.parallelization.CallbackReceiver;

/**
 * Writes any input to file
 * 
 * @author Marcel Boeing
 *
 */
public class SmbFileWriterModule extends ModuleImpl {

	// Property keys
	public static final String PROPERTYKEY_SMBURL = "SMB URL to outputfile";
	public static final String PROPERTYKEY_USEGZIP = "Use GZIP";
	public static final String PROPERTYKEY_ENCODING = "Encoding";
	public static final String PROPERTYKEY_BUFFERLENGTH = "Buffer length";
	public static final String PROPERTYKEY_SMBUSERNAME = "SMB username";
	public static final String PROPERTYKEY_SMBPASSWORD = "SMB password";
	public static final String PROPERTYKEY_SMBDOMAIN = "SMB domain";

	// Local variables
	private final String INPUTID = "input";
	private String smbUrl;
	private boolean useGzip = false;
	private String encoding;
	private int bufferLength = 8192;
	private String smbUsername;
	private String smbPassword;
	private String smbDomain;

	public SmbFileWriterModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_SMBURL,
				"SMB URL to the output file");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_USEGZIP,
						"Set to 'true' if the output file is to be compressed using GZIP");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_ENCODING,
						"The text encoding of the output file (if input is a char pipe)");
		this.getPropertyDescriptions().put(PROPERTYKEY_BUFFERLENGTH,
				"Length of the I/O buffer");
		this.getPropertyDescriptions().put(PROPERTYKEY_SMBUSERNAME,
				"SMB username");
		this.getPropertyDescriptions().put(PROPERTYKEY_SMBPASSWORD,
				"SMB password");
		this.getPropertyDescriptions().put(PROPERTYKEY_SMBDOMAIN, "SMB Domain");

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"SMB File Writer");
		this.getPropertyDefaultValues().put(PROPERTYKEY_SMBURL,
				"smb://sofs2.uni-koeln.de/StringsAndStructures/input.txt");
		this.getPropertyDefaultValues().put(PROPERTYKEY_USEGZIP, "false");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ENCODING, "UTF-8");
		this.getPropertyDefaultValues().put(PROPERTYKEY_BUFFERLENGTH, "8192");
		this.getPropertyDefaultValues().put(PROPERTYKEY_SMBDOMAIN, "WORKGROUP");

		// Define I/O
		InputPort inputPort = new InputPort("Input", "Byte or character input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		inputPort.addSupportedPipe(BytePipe.class);
		super.addInputPort(INPUTID,inputPort);

		// Add module description
		this.setDescription("Writes received input to a SMB/CIFS share. Can apply GZIP compression.");
	}

	@Override
	public boolean process() throws Exception {
		
		// Set authentication
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(this.smbDomain, this.smbUsername, this.smbPassword);

		// Set input file
		SmbFile smbFile = new SmbFile(this.smbUrl, auth);

		/*
		 * read from both input channels (reader/inputstream) -- if the first
		 * one is not connected to another module's output, it will throw an
		 * exception (that we will catch)
		 */
		try {
			// Instantiate a new output stream
			OutputStream fileOutputStream = new SmbFileOutputStream(smbFile);

			// Use GZIP if requested
			if (this.useGzip)
				fileOutputStream = new GZIPOutputStream(fileOutputStream);

			// Define input buffer
			byte[] buffer = new byte[this.bufferLength];

			// Read file data into buffer and write to outputstream
			int readBytes = this.getInputPorts().get(INPUTID).getInputStream().read(buffer);
			while (readBytes != -1) {

				// Check for interrupt signal
				if (Thread.interrupted()) {
					fileOutputStream.close();
					throw new InterruptedException(
							"Thread has been interrupted.");
				}

				fileOutputStream.write(buffer, 0, readBytes);
				readBytes = this.getInputPorts().get(INPUTID).getInputStream().read(buffer);
			}

			// close output stream
			fileOutputStream.close();

			// Log message
			Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO,
					"Wrote byte stream input into " + this.smbUrl);

		} catch (Exception e) {
			/*
			 * The inputstream does not seem to be connected -- try inputreader
			 * instead
			 */

			// Instantiate a new output stream
			OutputStream fileOutputStream = new SmbFileOutputStream(smbFile);

			// Use GZIP if requested
			if (this.useGzip)
				fileOutputStream = new GZIPOutputStream(fileOutputStream);

			// Instantiate a new file writer
			Writer fileWriter = new OutputStreamWriter(fileOutputStream,
					this.encoding);

			// Define input buffer
			char[] buffer = new char[this.bufferLength];

			// Read file data into buffer and output to writer
			int readBytes = this.getInputPorts().get(INPUTID).getInputReader().read(buffer);
			while (readBytes != -1) {

				// Check for interrupt signal
				if (Thread.interrupted()) {
					fileWriter.close();
					fileOutputStream.close();
					throw new InterruptedException(
							"Thread has been interrupted.");
				}

				fileWriter.write(buffer, 0, readBytes);
				readBytes = this.getInputPorts().get(INPUTID).getInputReader().read(buffer);
			}

			// close outputs
			fileWriter.close();
			fileOutputStream.close();

			// Log message
			Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO,
					"Wrote character input to " + this.smbUrl);
		}

		// Success
		return true;

	}

	@Override
	public void applyProperties() throws Exception {
		if (this.getProperties().containsKey(PROPERTYKEY_SMBURL))
			this.smbUrl = this.getProperties().getProperty(
					PROPERTYKEY_SMBURL);
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
		if (this.getProperties().containsKey(PROPERTYKEY_SMBUSERNAME))
			this.smbUsername = this.getProperties().getProperty(
					PROPERTYKEY_SMBUSERNAME);
		if (this.getProperties().containsKey(PROPERTYKEY_SMBPASSWORD))
			this.smbPassword = this.getProperties().getProperty(
					PROPERTYKEY_SMBPASSWORD);
		if (this.getProperties().containsKey(PROPERTYKEY_SMBDOMAIN))
			this.smbDomain = this.getProperties().getProperty(
					PROPERTYKEY_SMBDOMAIN);
		super.applyProperties();
	}

}
