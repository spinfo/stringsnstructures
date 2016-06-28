package modules.input_output;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import common.parallelization.CallbackReceiver;
import modules.BytePipe;
import modules.CharPipe;
import modules.ModuleImpl;
import modules.OutputPort;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class SmbFileReaderModule extends ModuleImpl {

	// Property keys
	public static final String PROPERTYKEY_SMBURL = "SMB URL to inputfile";
	public static final String PROPERTYKEY_USEGZIP = "Use GZIP";
	public static final String PROPERTYKEY_ENCODING = "Encoding";
	public static final String PROPERTYKEY_BUFFERLENGTH = "Buffer length";
	public static final String PROPERTYKEY_SMBUSERNAME = "SMB username";
	public static final String PROPERTYKEY_SMBPASSWORD = "SMB password";
	public static final String PROPERTYKEY_SMBDOMAIN = "SMB domain";

	// Local variables
	private final String OUTPUTID = "output";
	private boolean useGzip = false;
	private String encoding;
	private int bufferLength = 8192;
	private String smbUrl;
	private String smbUsername;
	private String smbPassword;
	private String smbDomain;

	public SmbFileReaderModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// set I/O -- no inputs allowed here (we'll read the file)
		OutputPort outputPort = new OutputPort(OUTPUTID, "Byte or character output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		outputPort.addSupportedPipe(BytePipe.class);
		super.addOutputPort(outputPort);

		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_SMBURL,
				"SMB URL to the input file");
		this.getPropertyDescriptions().put(PROPERTYKEY_USEGZIP,
				"Set to 'true' if the input file is compressed using GZIP");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_ENCODING,
						"The text encoding of the input file (if applicable, else set to empty string)");
		this.getPropertyDescriptions().put(PROPERTYKEY_BUFFERLENGTH,
				"Length of the I/O buffer");
		this.getPropertyDescriptions().put(PROPERTYKEY_SMBUSERNAME,
				"SMB username");
		this.getPropertyDescriptions().put(PROPERTYKEY_SMBPASSWORD,
				"SMB password");
		this.getPropertyDescriptions().put(PROPERTYKEY_SMBDOMAIN, "SMB Domain");

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"SMB File Reader");
		this.getPropertyDefaultValues().put(PROPERTYKEY_SMBURL,
				"smb://sofs2.uni-koeln.de/StringsAndStructures/input.txt");
		this.getPropertyDefaultValues().put(PROPERTYKEY_USEGZIP, "false");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ENCODING, "UTF-8");
		this.getPropertyDefaultValues().put(PROPERTYKEY_BUFFERLENGTH, "8192");
		this.getPropertyDefaultValues().put(PROPERTYKEY_SMBDOMAIN, "WORKGROUP");

		// Add module description
		this.setDescription("Reads contents from a SMB/CIFS share. Can handle GZIP compression.");
		
		// Add module category
		this.setCategory("I/O");
	}

	@Override
	public boolean process() throws Exception {

		// Set authentication
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
				this.smbDomain, this.smbUsername, this.smbPassword);

		// Set input file
		SmbFile smbFile = new SmbFile(this.smbUrl, auth);

		/*
		 * write to both output channels (stream/writer)
		 */
		boolean wroteToStream = false;
		if (!this.getOutputPorts().get(OUTPUTID).getPipes().get(BytePipe.class).isEmpty()) {
			// Instantiate a new input stream
			InputStream fileInputStream = new SmbFileInputStream(smbFile);

			// Use GZIP if requested
			if (this.useGzip)
				fileInputStream = new GZIPInputStream(fileInputStream);

			// Define input buffer
			byte[] buffer = new byte[this.bufferLength];

			// Read file data into buffer and write to outputstream
			int readBytes = fileInputStream.read(buffer);
			while (readBytes != -1) {

				// Auf Unterbrechersignal pruefen
				if (Thread.interrupted()) {
					fileInputStream.close();
					this.closeAllOutputs();
					throw new InterruptedException(
							"Thread has been interrupted.");
				}

				this.getOutputPorts().get(OUTPUTID).outputToAllBytePipes(buffer, 0, readBytes);
				readBytes = fileInputStream.read(buffer);
			}

			// close relevant I/O instances
			fileInputStream.close();
			wroteToStream = true;
		}

		boolean wroteToChars = false;
		if (!this.getOutputPorts().get(OUTPUTID).getPipes().get(CharPipe.class).isEmpty()) {
			// Instantiate a new input stream
			InputStream fileInputStream = new SmbFileInputStream(smbFile);

			// Use GZIP if requested
			if (this.useGzip)
				fileInputStream = new GZIPInputStream(fileInputStream);

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
					this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(buffer, 0, readChars);
					readChars = fileReader.read(buffer);
				}

				// close relevant I/O instances
				fileReader.close();
			}

			// close relevant I/O instances
			fileInputStream.close();
			
			wroteToChars = true;
		}
		
		// Close output port
		this.getOutputPorts().get(OUTPUTID).close();

		if (!wroteToStream && !wroteToChars)
			throw new Exception(
					"Sorry, but I could not write to any output (please connect a module to my output, else I am of little use).");

		// Success
		return true;
	}

	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		
		if (this.getProperties().containsKey(PROPERTYKEY_SMBURL))
			this.smbUrl = this.getProperties().getProperty(PROPERTYKEY_SMBURL);
		if (this.getProperties().containsKey(PROPERTYKEY_USEGZIP))
			this.useGzip = Boolean.parseBoolean(this.getProperties()
					.getProperty(PROPERTYKEY_USEGZIP));
		if (this.getProperties().containsKey(PROPERTYKEY_ENCODING))
			this.encoding = this.getProperties().getProperty(
					PROPERTYKEY_ENCODING);
		if (this.getProperties().containsKey(PROPERTYKEY_BUFFERLENGTH))
			this.bufferLength = Integer.parseInt(this.getProperties()
					.getProperty(PROPERTYKEY_BUFFERLENGTH));
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
