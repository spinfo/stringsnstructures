package modules;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.ClassUtils;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import common.parallelization.CallbackReceiver;

public class ModuleTreeGsonDeserializer implements JsonDeserializer<ModuleNetwork> {

	@Override
	public ModuleNetwork deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		
		// Set up module tree
		ModuleNetwork moduleNetwork = new ModuleNetwork();
		
		// Instantiate new JSON parser
		Gson gson = new Gson();
		
		// Get the serializable object from the JSON input
		SerializableModule[] serializableModuleList = gson.fromJson(json, new SerializableModule[0].getClass());
		
		// Keep track of the modules and ports
		Map<Integer,Module> moduleIdMap = new HashMap<Integer,Module>();
		Map<Integer,Port> inputPortIdMap = new HashMap<Integer,Port>();
		Map<Integer,Port> outputPortIdMap = new HashMap<Integer,Port>();
		Map<Integer,Integer> portConnectionMap = new HashMap<Integer,Integer>(); // Key: InputPort-hashcode, Value: OutputPort-hashcode
		Map<Integer,String> inputPortPipeClassMap = new HashMap<Integer,String>(); // Key: InputPort-hashcode, Value: Canonical class name of pipe

		for (int i=0; i<serializableModuleList.length; i++){
			try {
				
				// Determine the next serializable module within the list
				SerializableModule serializableModule = serializableModuleList[i];
				
				// Determine class of the root module
				Class<?> moduleClass = Class.forName(serializableModule.getModuleCanonicalClassName());
				
				// Determine the constructor of that class
				Constructor<?> constructor = moduleClass.getConstructor(CallbackReceiver.class, Properties.class);
				
				// Instantiate module and attach it to the module tree 
				Module newModuleInstance = (Module) constructor.newInstance(new Object[] { moduleNetwork, serializableModule.getProperties() });
				
				// Add module to corresponding id list
				moduleIdMap.put(serializableModule.getModuleInstanceHashCode(), newModuleInstance);
				
				// Add module input ports to corresponding id list
				Iterator<SerializablePort> serializableInputPorts = serializableModule.getSerializableInputPortList().values().iterator();
				while (serializableInputPorts.hasNext()){
					SerializablePort serializableInputPort = serializableInputPorts.next();
					inputPortIdMap.put(serializableInputPort.getInstanceHashCode(), newModuleInstance.getInputPorts().get(serializableInputPort.getName()));
					
					// Check whether the input port is connected
					if (!serializableInputPort.getConnectedPipesDestinationHashCodes().isEmpty()){
						// Map connection
						portConnectionMap.put(serializableInputPort.getInstanceHashCode(), serializableInputPort.getConnectedPipesDestinationHashCodes().keySet().iterator().next());
						inputPortPipeClassMap.put(serializableInputPort.getInstanceHashCode(), serializableInputPort.getConnectedPipesDestinationHashCodes().values().iterator().next());
					}
					
				}
				
				// Add module output ports to corresponding id list
				Iterator<SerializablePort> serializableOutputPorts = serializableModule.getSerializableOutputPortList().values().iterator();
				while (serializableOutputPorts.hasNext()){
					SerializablePort serializableOutputPort = serializableOutputPorts.next();
					outputPortIdMap.put(serializableOutputPort.getInstanceHashCode(), newModuleInstance.getOutputPorts().get(serializableOutputPort.getName()));
				} 

				
				// Add module to network
				moduleNetwork.addModule(newModuleInstance);
				
			} catch (Exception e) {
				Logger.getLogger("").log(Level.WARNING, "Error deserializing module tree object from JSON.", e);
				e.printStackTrace();
			}
		}
		
		// When all modules are present, restore the connections between the ports (we will just loop over the established port connection map for this)
		Iterator<Integer> inputPortIds = portConnectionMap.keySet().iterator();
		while (inputPortIds.hasNext()){
			
			// Determine the input port
			Integer inputPortId = inputPortIds.next();
			Port inputPort = inputPortIdMap.get(inputPortId);
			
			// Determine the output port it is connected to
			Integer outputPortId = portConnectionMap.get(inputPortId);
			Port outputPort = outputPortIdMap.get(outputPortId);
			
			// Next up there are some exceptions we need to handle locally (on
			// appearance they will provoke a JsonParseException though)
			try {

				// Determine the pipe class through which they are connected
				Class<?> pipeClass = ClassUtils.getClass(inputPortPipeClassMap
						.get(inputPortId));

				// Check whether all values are present (and throw an exception
				// if they are not)
				if (inputPort == null || outputPort == null
						|| pipeClass == null)
					throw new JsonParseException(
							"The port connection mapping seems to be inconsistent. Please check the serialized input.");

				// All is well, instantiate the pipe ...
				Constructor<?> constructor = pipeClass.getConstructor();
				Pipe pipe = (Pipe) constructor.newInstance(new Object[] {});

				// ... and connect the ports with it
				inputPort.addPipe(pipe, outputPort);
				outputPort.addPipe(pipe, inputPort);

			} catch (Exception e) {
				// If something goes wrong, throw an appropriate exception
				throw new JsonParseException(
						"Error reestablishing the deserialized intermodule connections.",
						e);
			}
		}
		
		// Return the new module tree object
		return moduleNetwork;
	}
	
}
