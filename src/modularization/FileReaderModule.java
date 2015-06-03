package modularization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Properties;

import parallelization.CallbackReceiver;

public class FileReaderModule extends ModuleImpl {
	
	public static final String PROPERTYKEY_INPUTFILE = "inputfile";
	private File file;
	
	public FileReaderModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver,properties);
		
		// set I/O -- no inputs allowed here (we'll read the file)
		this.getSupportedOutputs().add(BytePipe.class);
		this.getSupportedOutputs().add(CharPipe.class);
		
		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_INPUTFILE, "Path to the input file");
		
		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "File Reader");
	}

	@Override
	public boolean process() throws Exception {
		
		/*
		 * write to both output channels (stream/writer)
		 */
		try {
			// Instantiate a new input stream
			FileInputStream fileInputStream = new FileInputStream(this.file);
			
			// Define input buffer
			byte[] buffer = new byte[1024];
			
			// Read file data into buffer and write to outputstream
			int readBytes = fileInputStream.read(buffer);
			while (readBytes != -1){
				this.outputToAllBytePipes(buffer, 0, readBytes);
				readBytes = fileInputStream.read(buffer);
			}
			
			// close relevant I/O instances
			fileInputStream.close();
			this.closeAllOutputStreams();
			
		} catch (Exception e){
			/* The inputstream does not seem to be connected or another I/O-error occurred */
			throw e;
		}
		
		try {
			// Instantiate a new reader
			FileReader fileReader = new FileReader(this.file);

			// Define input buffer
			char[] buffer = new char[1024];

			// Read file data into buffer and output to writer
			int readChars = fileReader.read(buffer);
			while (readChars != -1) {
				this.outputToAllCharPipes(buffer, 0, readChars);
				readChars = fileReader.read(buffer);
			}

			// close relevant I/O instances
			fileReader.close();
			this.closeAllOutputWriters();
		} catch (Exception e){
			/* The inputstream does not seem to be connected or another I/O-error occurred */
			throw e;
		}
		
		// Success
		return true;
	}

	@Override
	public void applyProperties() throws Exception {
		if (this.getProperties().containsKey(PROPERTYKEY_INPUTFILE))
			this.file = new File(this.getProperties().getProperty(PROPERTYKEY_INPUTFILE));
		super.applyProperties();
	}

}
