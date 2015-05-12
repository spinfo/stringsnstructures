package modularization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

public class FileInputModule extends ModuleImpl {
	
	public static final String PROPERTYKEY_INPUTFILE = "inputfile";
	private File file;

	public FileInputModule() {
		this(null);
	}
	
	public FileInputModule(String filePath) {
		super();
		
		// set input file
		this.file = new File(filePath);
		
		// set I/O -- no other inputs allowed here (we'll read the file)
		this.setInputReader(null);
		this.setInputStream(null);
	}

	@Override
	public boolean process() throws Exception {
		
		/*
		 * write to both outputstreams -- if the first one is not connected
		 * to another module's input, it will throw an exception (that we will
		 * catch and ignore)
		 */
		try {
			// Instanciate a new input stream
			FileInputStream is = new FileInputStream(this.file);
			
			byte[] buffer = new byte[1024];
			int readbytes = is.read(buffer);
			while (readbytes>0){
				this.getOutputStream().write(buffer);
				readbytes = is.read(buffer);
			}
			is.close();
			this.getOutputStream().close();
		} catch (Exception e){
			FileReader ir = new FileReader(this.file);
			char[] buffer = new char[1024];
			while(ir.ready()){
				ir.read(buffer);
				this.getOutputWriter().write(buffer);
			}
			ir.close();
			this.getOutputWriter().close();
		}
		return true;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	@Override
	protected void applyProperties() throws Exception {
		if (this.getProperties().containsKey(PROPERTYKEY_INPUTFILE))
			this.file = new File(this.getProperties().getProperty(PROPERTYKEY_INPUTFILE));
		super.applyProperties();
	}

	@Override
	protected void updateProperties() {
		if (this.file != null)
			this.getProperties().setProperty(PROPERTYKEY_INPUTFILE, this.file.getAbsolutePath());
		else
			this.getProperties().remove(PROPERTYKEY_INPUTFILE);
		super.updateProperties();
	}

}
