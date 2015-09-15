package modules;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * JSON(Gson)-serializer for ModuleNetwork objects.
 * @author Marcel Boeing
 *
 */
public class ModuleNetworkGsonSerializer implements JsonSerializer<ModuleNetwork> {

	@Override
	public JsonElement serialize(ModuleNetwork moduleNetwork, Type type,
			JsonSerializationContext context) {
		
		try {
			
			List<SerializableModule> serializableModules = new ArrayList<SerializableModule>();
			
			Iterator<Module> modules = moduleNetwork.getModuleList().iterator();
			while (modules.hasNext()){
				serializableModules.add(new SerializableModule(modules.next()));
			}
			
			// Instantiate new JSON converter
			Gson gson = new Gson();
			
			// Serialize & return
			return gson.toJsonTree(serializableModules);
			
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("").warning("Sorry, but there has been an error serializing the module network: "+e.getMessage());
			return null;
		}
	}

}
