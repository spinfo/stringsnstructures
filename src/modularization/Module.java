package modularization;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import parallelization.CallbackProcess;

/**
 * Defines an abstract view of any processing module.
 * @author Marcel Boeing
 *
 */
public interface Module extends CallbackProcess {
	
	public static final int STATUSCODE_SUCCESS = 0;
	public static final int STATUSCODE_FAILURE = 1;
	public static final int STATUSCODE_RUNNING = 2;
	public static final int STATUSCODE_NOTYETRUN = 3;
	
	/**
	 * Returns the pipe for character input.
	 * @return Input pipe
	 */
	public CharPipe getInputCharPipe();
	
	/**
	 * Sets the pipe for character input.
	 * @throws NotSupportedException Thrown if module does not support character input
	 */
	public void setInputCharPipe(CharPipe pipe) throws NotSupportedException;
	
	/**
	 * Returns the pipe for byte input.
	 * @return Input pipe
	 */
	public BytePipe getInputBytePipe();
	
	/**
	 * Sets the pipe for byte input.
	 * @throws NotSupportedException Thrown if module does not support byte input
	 */
	public void setInputBytePipe(BytePipe pipe) throws NotSupportedException;
	
	/**
	 * Sets the input pipe.
	 * @throws NotSupportedException Thrown if module does not support the given input
	 */
	public void setInputPipe(Pipe pipe) throws NotSupportedException;
	
	/**
	 * Returns a list of all character output pipes the module currently has.
	 * @return list of output pipes
	 */
	public List<CharPipe> getOutputCharPipes();
	
	/**
	 * Returns a list of all byte output pipes the module currently has.
	 * @return list of output pipes
	 */
	public List<BytePipe> getOutputBytePipes();
	
	/**
	 * Returns a list of all output pipes the module currently has.
	 * @return list of output pipes
	 */
	public List<Pipe> getOutputPipes();
	
	/**
	 * Adds a given pipe to the list of module outputs.
	 * @param pipe Pipe
	 * @return True if successful
	 * @throws NotSupportedException Thrown if module does not support the given output pipe
	 */
	public boolean addOutputPipe(Pipe pipe) throws NotSupportedException;
	
	/**
	 * Removes a given pipe from the list of module outputs.
	 * @param pipe Pipe
	 * @return True if successful
	 */
	public boolean removeOutputPipe(Pipe pipe);
	
	/**
	 * Returns true if given pipe can be used as module input
	 * @param pipe Pipe
	 * @return true if pipe is supported
	 */
	public boolean supportsInputPipe(Pipe pipe);
	
	/**
	 * Returns true if given pipe can be used as module output
	 * @param pipe Pipe
	 * @return true if pipe is supported
	 */
	public boolean supportsOutputPipe(Pipe pipe);
	
	/**
	 * Starts the process.
	 * @return True if the process ended successfully.
	 * @throws Exception When something goes wrong, duh.
	 */
	public boolean process() throws Exception;
	
	/**
	 * Outputs the name of the module.
	 * @return Name
	 */
	public String getName();
	
	/**
	 * Sets the name of the module.
	 * @param name Name
	 */
	public void setName(String name);
	
	/**
	 * Outputs the properties used by this module instance.
	 * @return properties
	 */
	public Properties getProperties();
	
	/**
	 * Sets the properties used by this module instance.
	 * @param properties properties to set
	 * @throws Exception when properties are invalid
	 */
	public void setProperties(Properties properties) throws Exception;
	
	/**
	 * Returns a map containing all valid property keys of this module
	 * with a short description as value.
	 * @return
	 */
	public Map<String,String> getPropertyDescriptions();
	
	/**
	 * Returns a code indicating the status of the module (see static vars in this class)
	 * @return status code
	 */
	public int getStatus();

}
