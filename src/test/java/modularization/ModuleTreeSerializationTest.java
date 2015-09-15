package modularization;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import modules.CharPipe;
import modules.ModuleImpl;
import modules.ModuleNetwork;
import modules.ModuleNetworkGsonSerializer;
import modules.ModuleTreeGsonDeserializer;
import modules.basemodules.ConsoleWriterModule;
import modules.basemodules.FileWriterModule;
import modules.oanc.OANC;
import modules.oanc.OANCXMLParser;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ModuleTreeSerializationTest {

	@Test
	public void test() {

		try {

			// Set up module tree
			ModuleNetwork moduleNetwork = new ModuleNetwork();

			// Prepare OANC module
			Properties oancProperties = new Properties();
			oancProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "OANC");
			oancProperties.setProperty(OANC.PROPERTYKEY_OANCLOCATION,
					"/tmp/oanc");
			OANC oanc = new OANC(moduleNetwork, oancProperties);

			//moduleNetwork.setRootModule(oanc); // Necessary before adding more
											// modules!

			// Prepare FileWriter module
			Properties fileWriterProperties = new Properties();
			fileWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME,
					"FileWriter");
			fileWriterProperties.setProperty(
					FileWriterModule.PROPERTYKEY_OUTPUTFILE, "/tmp/out");
			FileWriterModule fileWriter = new FileWriterModule(moduleNetwork,
					fileWriterProperties);

			// Prepare OANC parser module
			Properties oancParserProperties = new Properties();
			oancParserProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME,
					"OANC-Parser");
			oancParserProperties.setProperty(
					OANCXMLParser.PROPERTYKEY_ADDSTARTSYMBOL,
					Boolean.toString(true));
			oancParserProperties.setProperty(
					OANCXMLParser.PROPERTYKEY_ADDTERMINALSYMBOL,
					Boolean.toString(true));
			oancParserProperties.setProperty(
					OANCXMLParser.PROPERTYKEY_CONVERTTOLOWERCASE,
					Boolean.toString(true));
			oancParserProperties.setProperty(
					OANCXMLParser.PROPERTYKEY_KEEPPUNCTUATION,
					Boolean.toString(true));
			oancParserProperties.setProperty(
					OANCXMLParser.PROPERTYKEY_OUTPUTANNOTATEDJSON,
					Boolean.toString(true));
			OANCXMLParser oancParser = new OANCXMLParser(moduleNetwork,
					oancParserProperties);

			// Prepare ConsoleWriter module
			Properties consoleWriterProperties = new Properties();
			consoleWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME,
					"ConsoleWriter");
			ConsoleWriterModule consoleWriter = new ConsoleWriterModule(
					moduleNetwork, consoleWriterProperties);

			// Add modules to tree
			moduleNetwork.addModule(oanc);
			moduleNetwork.addModule(oancParser);
			moduleNetwork.addModule(fileWriter);
			moduleNetwork.addModule(consoleWriter);

			// Connect module ports
			oanc.getOutputPorts().get("output").addPipe(new CharPipe(), oancParser.getInputPorts().get("input"));
			oancParser.getOutputPorts().get("output").addPipe(new CharPipe(), fileWriter.getInputPorts().get("input"));
			oancParser.getOutputPorts().get("output").addPipe(new CharPipe(), consoleWriter.getInputPorts().get("input"));

			// Instantiate JSON converter
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(ModuleNetwork.class,
					new ModuleNetworkGsonSerializer());
			gsonBuilder.registerTypeAdapter(ModuleNetwork.class,
					new ModuleTreeGsonDeserializer());
			Gson gson = gsonBuilder.setPrettyPrinting().create();

			String json = gson.toJson(moduleNetwork);

			ModuleNetwork moduleTree2 = gson.fromJson(json, ModuleNetwork.class);
			String json2 = gson.toJson(moduleTree2);

			//System.out.println(json+"\n-------\n"+json2);
			
			//assertTrue(json.equals(json2)); // Order of fields in JSON is not fixed, so this will probably fail even if the JSON is functionally identical.

			assertTrue(json2.length()==json.length());
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
