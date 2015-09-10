package modules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A serializable representation of a module
 * @author Marcel Boeing
 *
 */
public class SerializableModule {

	private Properties properties;
	private int moduleInstanceHashCode;
	private String moduleCanonicalClassName;
	private Map<String,SerializablePort> serializableInputPortList;

	private Map<String,SerializablePort> serializableOutputPortList;
	
	public SerializableModule() {
	}
	
	public SerializableModule(Module module) {
		this.reflectModule(module);
	}
	
	public void reflectModule(Module module){
		this.properties = module.getProperties();
		this.setModuleInstanceHashCode(module.hashCode());
		this.moduleCanonicalClassName = module.getClass().getCanonicalName();
		
		// Input ports
		this.serializableInputPortList = new HashMap<String,SerializablePort>();
		Iterator<InputPort> inputPorts = module.getInputPorts().values().iterator();
		while (inputPorts.hasNext()){
			InputPort inputPort = inputPorts.next();
			
			SerializablePort serializablePort = new SerializablePort();
			serializablePort.setName(inputPort.getName());
			serializablePort.setInstanceHashCode(inputPort.hashCode());
			serializablePort.setConnectedPipesDestinationHashCodes(new HashMap<String,Integer>());
			
			Pipe pipe = inputPort.getPipe();
			if (pipe != null){
				serializablePort.getConnectedPipesDestinationHashCodes().put(pipe.getClass().getCanonicalName(), inputPort.getConnectedPort().hashCode());
			}
			
			this.serializableInputPortList.put(inputPort.getName(), serializablePort);
		}
		
		// Output ports
		this.serializableOutputPortList = new HashMap<String,SerializablePort>();
		Iterator<OutputPort> outputPorts = module.getOutputPorts().values().iterator();
		while (outputPorts.hasNext()){
			OutputPort outputPort = outputPorts.next();
			
			SerializablePort serializablePort = new SerializablePort();
			serializablePort.setName(outputPort.getName());
			serializablePort.setInstanceHashCode(outputPort.hashCode());
			serializablePort.setConnectedPipesDestinationHashCodes(new HashMap<String,Integer>());
			
			Iterator<List<Pipe>> outputPipeLists = outputPort.getPipes().values().iterator();
			while (outputPipeLists.hasNext()){
				List<Pipe> outputPipeList = outputPipeLists.next();
				Iterator<Pipe> outputPipes = outputPipeList.iterator();
				while (outputPipes.hasNext()){
					Pipe outputPipe = outputPipes.next();
					serializablePort.getConnectedPipesDestinationHashCodes().put(outputPipe.getClass().getCanonicalName(), outputPort.getConnectedPort(outputPipe).hashCode());
				}
			}
			
			this.serializableOutputPortList.put(outputPort.getName(), serializablePort);
		}
	}

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * @return the moduleCanonicalClassName
	 */
	public String getModuleCanonicalClassName() {
		return moduleCanonicalClassName;
	}

	/**
	 * @param moduleCanonicalClassName the moduleCanonicalClassName to set
	 */
	public void setModuleCanonicalClassName(String moduleCanonicalClassName) {
		this.moduleCanonicalClassName = moduleCanonicalClassName;
	}

	/**
	 * @return the moduleInstanceHashCode
	 */
	public int getModuleInstanceHashCode() {
		return moduleInstanceHashCode;
	}

	/**
	 * @param moduleInstanceHashCode the moduleInstanceHashCode to set
	 */
	public void setModuleInstanceHashCode(int moduleInstanceHashCode) {
		this.moduleInstanceHashCode = moduleInstanceHashCode;
	}
	/**
	 * @return the serializableInputPortList
	 */
	protected Map<String, SerializablePort> getSerializableInputPortList() {
		return serializableInputPortList;
	}

	/**
	 * @return the serializableOutputPortList
	 */
	protected Map<String, SerializablePort> getSerializableOutputPortList() {
		return serializableOutputPortList;
	}

}
