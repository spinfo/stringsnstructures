package modularization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import parallelization.CallbackReceiver;

public class FileWriterModule extends ModuleImpl {
	
	public static final String PROPERTYKEY_OUTPUTFILE = "outputfile";
	private String filePath;
	
	public FileWriterModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		// set I/O -- no other outputs allowed here (we'll write the file instead of piping to another module)
		this.setOutputWriter(null);
		this.setOutputStream(null);
		
		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_OUTPUTFILE, "Path to the output file");
		
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
			// Instanciate a new output stream
			FileOutputStream fileOutputStream = new FileOutputStream(new File(this.filePath));
			
			// Define input buffer
			byte[] buffer = new byte[1024];
			
			// Read file data into buffer and write to outputstream
			int readbytes = this.getInputStream().read(buffer);
			while (readbytes>0){
				fileOutputStream.write(buffer);
				readbytes = this.getInputStream().read(buffer);
			}
			
			// close relevant I/O instances
			fileOutputStream.close();
			this.getInputStream().close();
			
			// Log message
			Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Streamed output into "+this.filePath);
			
		} catch (Exception e){
			/* The inputstream does not seem to be connected -- try reader/writer instead */
			
			// Instanciate a new writer
			FileWriter fileWriter = new FileWriter(new File(this.filePath));
			
			// Define input buffer
			char[] buffer = new char[1024];
			
			// Read file data into buffer and output to writer
			while(this.getInputReader().ready()){
				this.getInputReader().read(buffer);
				fileWriter.write(buffer);
			}
			
			// close relevant I/O instances
			fileWriter.close();
			this.getInputReader().close();
			
			// Log message
			Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Wrote output to "+this.filePath);
		}
		
		// Success
		return true;
	}

	@Override
	protected void applyProperties() throws Exception {
		if (this.getProperties().containsKey(PROPERTYKEY_OUTPUTFILE))
			this.filePath = this.getProperties().getProperty(PROPERTYKEY_OUTPUTFILE);
		super.applyProperties();
	}

}
