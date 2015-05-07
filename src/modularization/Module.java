package modularization;

import java.util.List;

/**
 * Defines an abstract view of any processing module.
 * @author Marcel Boeing
 *
 */
public interface Module {
	
	/**
	 * Sets the input object (e.g. BufferedReader, String[], whatever the class supports).
	 * @param input Input object
	 * @throws IncompatibleIOException Thrown if the input class is not supported
	 */
	public void setInput(Object input) throws IncompatibleIOException;
	
	/**
	 * Returns the input object (null if none is set).
	 * @return Input object
	 */
	public Object getInput();
	
	/**
	 * Starts the process.
	 * @return True if the process ended successfully.
	 * @throws Exception When something goes wrong, duh.
	 */
	public boolean process() throws Exception;
	
	/**
	 * Sets the output object (e.g. BufferedReader, String[], File, whatever the class supports).
	 * @param output Output object
	 * @throws IncompatibleIOException Thrown if the output class is not supported
	 */
	public void setOutput(Object output) throws IncompatibleIOException;
	
	/**
	 * Returns the output object (null if none is set).
	 * @return Output object
	 */
	public Object getOutput();

	/**
	 * Returns the supported input formats.
	 * @return List of input formats
	 */
	public List<Class<?>> getInputformats();
	
	/**
	 * Returns the supported output formats.
	 * @return List of output formats
	 */
	public List<Class<?>> getOutputformats();
	
	/**
	 * Indicates whether a given input object is supported.
	 * @param input Input object
	 * @return true if supported
	 */
	public boolean doesSupportInput(Object input);
	
	/**
	 * Indicates whether a given output object is supported.
	 * @param output Output object
	 * @return true if supported
	 */
	public boolean doesSupportOutput(Object output);
	
	/**
	 * Outputs the name of the module.
	 * @return Name
	 */
	public String getName();

}
