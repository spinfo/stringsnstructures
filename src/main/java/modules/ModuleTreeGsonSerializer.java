package modules;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * JSON(Gson)-serializer for ModuleNetwork objects.
 * @author Marcel Boeing
 *
 */
public class ModuleTreeGsonSerializer implements JsonSerializer<ModuleNetwork> {

	@Override
	public JsonElement serialize(ModuleNetwork arg0, Type arg1,
			JsonSerializationContext arg2) {
		
		SerializableModuleTreeNode serializableRootNode;
		try {
			
			// Construct serializable representation of the module tree's nodes (the root node contains references to its children, those to theirs etc.)
			serializableRootNode = SerializableModuleTreeNode.convertModuleTreeModel(arg0.getModuleTreeModel());
			
			// Instantiate new JSON converter
			Gson gson = new Gson();
			
			// Serialize & return
			return gson.toJsonTree(serializableRootNode);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
