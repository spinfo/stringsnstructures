package modules;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		List<SerializableModule> serializableModuleList = gson.fromJson(json, new ArrayList<SerializableModule>().getClass());
		
		// Keep track of the modules and ports
		Map<Integer,Module> moduleIds = new HashMap<Integer,Module>();
		Map<Integer,Port> inputPortIds = new HashMap<Integer,Port>();
		Map<Integer,Port> outputPortIds = new HashMap<Integer,Port>();

		Iterator<SerializableModule> serializableModules = serializableModuleList.iterator();
		while (serializableModules.hasNext()){
			try {
				
				// Determine the next serializable module within the list
				SerializableModule serializableModule = serializableModules.next();
				
				// Determine class of the root module
				Class<?> moduleClass = Class.forName(serializableModule.getModuleCanonicalClassName());
				
				// Determine the constructor of that class
				Constructor<?> constructor = moduleClass.getConstructor(CallbackReceiver.class, Properties.class);
				
				// Instantiate module and attach it to the module tree 
				Module newModuleInstance = (Module) constructor.newInstance(new Object[] { moduleNetwork, serializableModule.getProperties() });
				
				// Add module to corresponding id list
				moduleIds.put(serializableModule.getModuleInstanceHashCode(), newModuleInstance);
				
				// Add module input ports to corresponding id list
				Iterator<SerializablePort> serializableInputPorts = serializableModule.getSerializableInputPortList().values().iterator();
				while (serializableInputPorts.hasNext()){
					SerializablePort serializableInputPort = serializableInputPorts.next();
					inputPortIds.put(serializableInputPort.getInstanceHashCode(), newModuleInstance.getInputPorts().get(serializableInputPort.getName()));
				}
				
				// Add module output ports to corresponding id list
				Iterator<SerializablePort> serializableOutputPorts = serializableModule.getSerializableOutputPortList().values().iterator();
				while (serializableOutputPorts.hasNext()){
					SerializablePort serializableOutputPort = serializableOutputPorts.next();
					outputPortIds.put(serializableOutputPort.getInstanceHashCode(), newModuleInstance.getOutputPorts().get(serializableOutputPort.getName()));
				} XXX//TODO Ein port kann auf mehrere andere verweisen (oder andersherum) 

				
				// Add module to network
				moduleNetwork.addModule(newModuleInstance);
				
			} catch (Exception e) {
				Logger.getLogger("").log(Level.WARNING, "Error deserializing module tree object from JSON.", e);
				e.printStackTrace();
			}
		}
		
		// TODO connect ports
		
		// Return the new module tree object
		return moduleNetwork;
	}

	/**
	 * Recursively attaches the given serializable module tree node and its children to the module tree.
	 * @param parent
	 * @param moduleNetwork
	 * @param nodeToAttach
	 * @throws Exception
	 */
	private void attachToModuleTree(Module parent, ModuleNetwork moduleNetwork, SerializableModule nodeToAttach) throws Exception {
		// Determine class of the module
		Class<?> moduleClass = Class.forName(nodeToAttach.getModuleCanonicalClassName());
					
		// Determine the constructor of that class
		Constructor<?> ctor = moduleClass.getConstructor(CallbackReceiver.class, Properties.class);
					
		// Instantiate module and attach it to the module tree 
		Module module = (Module) ctor.newInstance(new Object[] { moduleNetwork, nodeToAttach.getProperties() });
		moduleNetwork.addConnection(module, parent);
		
		// Recursively do the same with each child node
		Iterator<SerializableModule> children = nodeToAttach.getChildren().iterator();
		while (children.hasNext()){
			this.attachToModuleTree(module, moduleNetwork, children.next());
		}
	}
	
}
