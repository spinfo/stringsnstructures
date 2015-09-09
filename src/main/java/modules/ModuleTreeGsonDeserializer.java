package modules;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Iterator;
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
		SerializableModuleTreeNode serializableNode = gson.fromJson(json, SerializableModuleTreeNode.class);

		try {
			
			// Determine class of the root module
			Class<?> moduleClass = Class.forName(serializableNode.getModuleCanonicalClassName());
			
			// Determine the constructor of that class
			Constructor<?> ctor = moduleClass.getConstructor(CallbackReceiver.class, Properties.class);
			
			// Instantiate module and attach it to the module tree 
			Module rootModule = (Module) ctor.newInstance(new Object[] { moduleNetwork, serializableNode.getProperties() });
			moduleNetwork.setRootModule(rootModule);
			
			// Recursively do the same with each child node
			Iterator<SerializableModuleTreeNode> children = serializableNode.getChildren().iterator();
			while (children.hasNext()){
				this.attachToModuleTree(rootModule, moduleNetwork, children.next());
			}
			
		} catch (Exception e) {
			Logger.getLogger("").log(Level.WARNING, "Error deserializing module tree object from JSON.", e);
			e.printStackTrace();
		}
		
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
	private void attachToModuleTree(Module parent, ModuleNetwork moduleNetwork, SerializableModuleTreeNode nodeToAttach) throws Exception {
		// Determine class of the module
		Class<?> moduleClass = Class.forName(nodeToAttach.getModuleCanonicalClassName());
					
		// Determine the constructor of that class
		Constructor<?> ctor = moduleClass.getConstructor(CallbackReceiver.class, Properties.class);
					
		// Instantiate module and attach it to the module tree 
		Module module = (Module) ctor.newInstance(new Object[] { moduleNetwork, nodeToAttach.getProperties() });
		moduleNetwork.addConnection(module, parent);
		
		// Recursively do the same with each child node
		Iterator<SerializableModuleTreeNode> children = nodeToAttach.getChildren().iterator();
		while (children.hasNext()){
			this.attachToModuleTree(module, moduleNetwork, children.next());
		}
	}
	
}
