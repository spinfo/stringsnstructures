package modules;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.parallelization.CallbackReceiver;

public abstract class ModuleImpl implements Module {

	public static final String PROPERTYKEY_NAME = "name";
	private CallbackReceiver callbackReceiver;
	private String name;
	private Properties properties = new Properties();
	private Map<String, String> propertyDescriptions = new HashMap<String, String>();
	private Map<String, String> propertyDefaultValues = new HashMap<String, String>();
	private int status = Module.STATUSCODE_NOTYETRUN;
	private String description = "(no description)";
	private Map<String,InputPort> inputPorts;
	private Map<String,OutputPort> outputPorts;

	public ModuleImpl(CallbackReceiver callbackReceiver, Properties properties)
			throws Exception {
		super();
		this.callbackReceiver = callbackReceiver;
		this.setProperties(properties);
		this.getPropertyDescriptions().put(PROPERTYKEY_NAME,
				"The module instance's name");
		// Add default values
		this.getPropertyDefaultValues().put(PROPERTYKEY_NAME, "(unnamed module)");
		
		// IO ports
		this.inputPorts = new HashMap<String,InputPort>();
		this.outputPorts = new HashMap<String,OutputPort>();
	}
	
	/**
	 * Adds the specified input port.
	 * @param port
	 */
	public void addInputPort(String identifier, InputPort port){
		this.inputPorts.put(identifier, port);
	}
	
	/**
	 * Adds the specified output port.
	 * @param port
	 */
	public void addOutputPort(String identifier, OutputPort port){
		this.outputPorts.put(identifier, port);
	}

	@Override
	public void applyProperties() throws Exception {
		if (this.getProperties().containsKey(PROPERTYKEY_NAME))
			this.name = this.getProperties().getProperty(PROPERTYKEY_NAME, "unnamed module");
	}

	/**
	 * Closes all outputs on all output ports.
	 * @throws IOException
	 */
	public void closeAllOutputs() throws IOException {
		Iterator<OutputPort> outputPorts = this.outputPorts.values().iterator();
		while (outputPorts.hasNext()){
			outputPorts.next().close();
		}
	}

	/*
	 * @see parallelization.CallbackProcess#getRueckmeldungsEmpfaenger()
	 */
	@Override
	public CallbackReceiver getCallbackReceiver() {
		return callbackReceiver;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Properties getProperties() {
		return properties;
	}
	
	@Override
	public Map<String, String> getPropertyDescriptions() {
		return propertyDescriptions;
	}
	
	@Override
	public Map<String, String> getPropertyDefaultValues() {
		return propertyDefaultValues;
	}
	
	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public abstract boolean process() throws Exception;

	/*
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		try {
			
			// Update status
			this.status = Module.STATUSCODE_RUNNING;

			// Log message
			Logger.getLogger("").log(
					Level.INFO,
					"Running module "
							+ this.getProperties().getProperty(
									ModuleImpl.PROPERTYKEY_NAME));

			// Run process and determine result
			Boolean result = this.process();

			// Log message
			Logger.getLogger("")
					.log(Level.INFO,
							"Module "
									+ this.getProperties().getProperty(
											ModuleImpl.PROPERTYKEY_NAME)
									+ " finished.");

			// Update status
			if (result)
				this.status = Module.STATUSCODE_SUCCESS;
			else
				this.status = Module.STATUSCODE_FAILURE;

			// Return result
			this.callbackReceiver.receiveCallback(Thread.currentThread(), result);

		} catch (Exception e) {
			this.status = Module.STATUSCODE_FAILURE;
			this.callbackReceiver.receiveException(Thread.currentThread(), e);
		}
	}

	/*
	 * @see
	 * parallelization.CallbackProcess#setRueckmeldungsEmpfaenger(parallelization
	 * .CallbackReceiver)
	 */
	@Override
	public void setCallbackReceiver(CallbackReceiver callbackReceiver) {
		this.callbackReceiver = callbackReceiver;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		if (this.name != null)
			this.getProperties().setProperty(PROPERTYKEY_NAME, name);
		else
			this.getProperties().remove(PROPERTYKEY_NAME);
	}

	@Override
	public void setProperties(Properties properties) throws Exception {
		if (properties == null)
			throw new Exception(this.getClass().getSimpleName()
					+ " cannot handle null value as properties, sorry.");
		this.properties = properties;
		this.applyProperties();
	}

	/* (non-Javadoc)
	 * @see modularization.Module#resetOutputs()
	 */
	@Override
	public void resetOutputs() throws IOException {
		
		// Cycle through all output pipes & reset them
		Iterator<OutputPort> ports = this.getOutputPorts().values().iterator();
		while (ports.hasNext()){
			ports.next().reset();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see modularization.Module#getDescription()
	 */
	@Override
	public String getDescription() {
		return this.description;
	}

	/* (non-Javadoc)
	 * @see modularization.Module#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String desc) {
		this.description = desc;
	}

	/* (non-Javadoc)
	 * @see modules.Module#getInputPorts()
	 */
	@Override
	public Map<String,InputPort> getInputPorts() {
		return this.inputPorts;
	}

	/* (non-Javadoc)
	 * @see modules.Module#getOutputPorts()
	 */
	@Override
	public Map<String,OutputPort> getOutputPorts() {
		return this.outputPorts;
	}

}
