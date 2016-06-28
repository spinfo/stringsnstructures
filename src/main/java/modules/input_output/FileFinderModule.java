package modules.input_output;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import modules.CharPipe;
import modules.ModuleImpl;
import modules.OutputPort;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import common.RegExFileFilter;
import common.DirectoryFilter;
import common.parallelization.CallbackReceiver;

/**
 * Searches for files with the specified suffix beneath the specified path(s)
 * and outputs their locations in a JSON-encoded file list.
 * 
 * @author marcel
 *
 */
public class FileFinderModule extends ModuleImpl {

	public static final String PROPERTYKEY_PATHTOSEARCH = "path to search";
	public static final String PROPERTYKEY_FILENAMESUFFIX = "file name suffix";
	private final String OUTPUTID = "file list";
	private String[] pathsToSearch;
	private String fileNameSuffix;
	private FileFilter directoryFilter = new DirectoryFilter();
	private FileFilter fileFilter;
	
	public FileFinderModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Searches for files with the specified suffix beneath the specified path(s) and outputs their locations in a JSON-encoded file list.");
		
		// Add module category

		
		// Define I/O
		OutputPort outputPort = new OutputPort(OUTPUTID, "JSON-encoded list of source file locations.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputPort);
		
		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_PATHTOSEARCH, "The path(s) to search for text files (directories are searched recursively). Multiple paths must be separated by semicolons (;).");
		this.getPropertyDescriptions().put(PROPERTYKEY_FILENAMESUFFIX, "File name suffix to search for (e.g. 'txt').");
		
		// Determine system properties (for setting default values that make sense)
		String homedir = System.getProperty("user.home");
		
		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "File Finder");
		this.getPropertyDefaultValues().put(PROPERTYKEY_PATHTOSEARCH, homedir);
		this.getPropertyDefaultValues().put(PROPERTYKEY_FILENAMESUFFIX, "txt");
	}

	/**
	 * Finds all files with the specified file name suffix beneath all paths specified in this instance.
	 * @return File list
	 * @throws Exception Thrown if a file found is not readable or if something else goes wrong
	 */
	private List<File> findFiles() throws Exception {
		
		// List for files found
		List<File> fileList = new ArrayList<File>();
		
		// Loop over paths to search
		for (int i=0; i<this.pathsToSearch.length; i++){
			
			// Determine next path to search
			File path = new File(pathsToSearch[i]);
			
			// Invoke search method for single path
			fileList.addAll(this.findFiles(path));
			
		}
		
		// Return result list
		return fileList;
	}
	
	/**
	 * Finds all files with the specified file name suffix beneath the specified path.
	 * @param path Path to search
	 * @return File list
	 * @throws Exception Thrown if a file found is not readable or if something else goes wrong
	 */
	private List<File> findFiles(File path) throws Exception {
		
		// Check for interrupt
		if (Thread.interrupted()) {
			throw new InterruptedException("The file search of module "+this.getName()+" has been interrupted.");
		}
		
		// List for result
		List<File> fileList = new ArrayList<File>();
			
		// Check whether the specified path is indeed a directory we can read 
		if (!(path.isDirectory() && path.canRead())) {
			throw new Exception("The path " + path.getAbsolutePath() + " cannot be read or is not a directory (unexpectedly).");
		}
		
		// Search for files that match this instance's file name suffix filter
		File[] files = path.listFiles(this.fileFilter);
		for (int i=0; i<files.length; i++){
			// Add find to result list
			fileList.add(files[i]);
		}

		// Loop over subdirectories
		File[] subDirs = path.listFiles(this.directoryFilter);
		for (int i=0; i<subDirs.length; i++){
			// Call this method recursively
			fileList.addAll(this.findFiles(subDirs[i]));
		}
		
		// Return result
		return fileList;
	}

	@Override
	public boolean process() throws Exception {
		if (this.pathsToSearch == null || this.pathsToSearch.length<1)
			return false;
		
		// Search for files that match the specified criteria
		List<File> fileList = this.findFiles();
		
		// Check for interrupt
		if (Thread.interrupted()) {
			this.closeAllOutputs();
			throw new InterruptedException("The thread of module "+this.getName()+" has been interrupted.");
		}
		
		// Instanciate JSON converter
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		// Convert file list to JSON
		String fileListJson = gson.toJson(fileList);
		
		// Write the file list JSON to all output character pipes
		this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(fileListJson);
		
		// Close outputs
		this.closeAllOutputs();
		
		// Processing ended successfully
		return true;
	}

	@Override
	public void applyProperties() throws Exception {
		
		// Set defaults
		super.setDefaultsIfMissing();
		
		// Apply properties if values are present
		if (this.getProperties().containsKey(PROPERTYKEY_PATHTOSEARCH))
			this.pathsToSearch = this.getProperties().get(PROPERTYKEY_PATHTOSEARCH).toString().split(";");
		if (this.getProperties().containsKey(PROPERTYKEY_FILENAMESUFFIX)){
			this.fileNameSuffix = this.getProperties().get(PROPERTYKEY_FILENAMESUFFIX).toString().trim();
			this.fileFilter = new RegExFileFilter(".+\\."+this.fileNameSuffix+"$");
		}
		
		// Call apply for super class
		super.applyProperties();
	}

}
