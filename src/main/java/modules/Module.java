package modules;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import common.parallelization.CallbackProcess;

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
	 * Returns a list of available input ports.
	 * @return List of ports
	 */
	public List<InputPort> getInputPorts();
	
	/**
	 * Returns a list of available output ports.
	 * @return List of ports
	 */
	public List<OutputPort> getOutputPorts();
	
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
	 * Outputs the description of the module.
	 * @return Description
	 */
	public String getDescription();
	
	/**
	 * Sets the description of the module.
	 * @param desc Description
	 */
	public void setDescription(String desc);
	
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
	 * Returns a map containing available default values for properties of this module.
	 * @return
	 */
	public Map<String,String> getPropertyDefaultValues();
	
	/**
	 * Returns a code indicating the status of the module (see static vars in this class)
	 * @return status code
	 */
	public int getStatus();
	
	/**
	 * Applies all relevant properties to this instance. Subclasses should
	 * override this, apply the properties they use themselves and call
	 * super().applyProperties() afterwards.
	 * 
	 * @throws Exception
	 *             when something goes wrong (property cannot be applied etc.)
	 */
	public void applyProperties() throws Exception;
	
	/**
	 * Resets all outputs.
	 * @throws IOException
	 */
	public void resetOutputs() throws IOException;

}
