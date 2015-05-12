package modularization;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.Properties;

import parallelization.CallbackProcess;

/**
 * Defines an abstract view of any processing module.
 * @author Marcel Boeing
 *
 */
public interface Module extends CallbackProcess {
	
	/**
	 * Returns the input reader.
	 * @return Input reader
	 * @throws NotSupportedException Thrown if module does not support input via reader
	 */
	public PipedReader getInputReader() throws NotSupportedException;
	
	/**
	 * Returns the output writer
	 * @return output writer
	 * @throws NotSupportedException Thrown if module does not support output via writer
	 */
	public PipedWriter getOutputWriter() throws NotSupportedException;
	
	/**
	 * Returns the input stream.
	 * @return Input stream
	 * @throws NotSupportedException Thrown if module does not support input via stream
	 */
	public PipedInputStream getInputStream() throws NotSupportedException;
	
	/**
	 * Returns the output stream.
	 * @return Output stream
	 * @throws NotSupportedException Thrown if module does not support output via stream
	 */
	public PipedOutputStream getOutputStream() throws NotSupportedException;
	
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

}
