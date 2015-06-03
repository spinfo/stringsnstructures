package modularization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import parallelization.CallbackReceiver;

/**
 * Writes any input to file
 * @author Marcel Boeing
 *
 */
public class FileWriterModule extends ModuleImpl {
	
	public static final String PROPERTYKEY_OUTPUTFILE = "outputfile";
	private String filePath;
	
	public FileWriterModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_OUTPUTFILE, "Path to the output file");
		
		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "File Writer");
		
		// Define I/O
		this.getSupportedInputs().add(BytePipe.class);
		this.getSupportedInputs().add(CharPipe.class);
		
		Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Initialized module "+this.getProperties().getProperty(ModuleImpl.PROPERTYKEY_NAME));
	}

	@Override
	public boolean process() throws Exception {
		
		/*
		 * read from both input channels (reader/inputstream) -- if the first one
		 * is not connected to another module's output, it will throw an exception
		 * (that we will catch)
		 */
		try {
			// Instantiate a new output stream
			FileOutputStream fileOutputStream = new FileOutputStream(new File(this.filePath));
			
			// Define input buffer
			byte[] buffer = new byte[1024];
			
			// Read file data into buffer and write to outputstream
			int readBytes = this.getInputBytePipe().getInput().read(buffer);
			while (readBytes != -1){
				fileOutputStream.write(buffer, 0, readBytes);
				readBytes = this.getInputBytePipe().getInput().read(buffer);
			}
			
			// close output stream
			fileOutputStream.close();
			
			// Log message
			Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Streamed output into "+this.filePath);
			
		} catch (Exception e){
			/* The inputstream does not seem to be connected -- try inputreader instead */
			
			// Instantiate a new writer
			FileWriter fileWriter = new FileWriter(new File(this.filePath));
			
			// Define input buffer
			char[] buffer = new char[1024];
			
			// Read file data into buffer and output to writer
			int readBytes = this.getInputCharPipe().getInput().read(buffer);
			while(readBytes != -1){
				fileWriter.write(buffer, 0, readBytes);
				readBytes = this.getInputCharPipe().getInput().read(buffer);
			}
			
			// close output writer
			fileWriter.close();
			
			// Log message
			Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Wrote output to "+this.filePath);
		}
		
		// Success
		return true;
	}

	@Override
	public void applyProperties() throws Exception {
		if (this.getProperties().containsKey(PROPERTYKEY_OUTPUTFILE))
			this.filePath = this.getProperties().getProperty(PROPERTYKEY_OUTPUTFILE);
		super.applyProperties();
	}

}
