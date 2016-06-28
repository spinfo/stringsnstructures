package modularization;

import static org.junit.Assert.fail;

import java.util.Properties;

import modules.CharPipe;
import modules.ModuleImpl;
import modules.ModuleNetwork;
import modules.ModuleNetworkGsonSerializer;
import modules.ModuleTreeGsonDeserializer;
import modules.input_output.ConsoleWriterModule;
import modules.input_output.FileFinderModule;
import modules.input_output.FileWriterModule;
import modules.parser.oanc.OANCXMLParser;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class ModuleNetworkSerializationTest {

	@Test
	public void test() {

		try {

			// Set up module tree
			ModuleNetwork moduleNetwork = new ModuleNetwork();

			// Prepare FileFinderModule module
			Properties oancProperties = new Properties();
			oancProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "FileFinderModule");
			oancProperties.setProperty(FileFinderModule.PROPERTYKEY_PATHTOSEARCH,
					"/tmp/oanc");
			FileFinderModule fileFinderModule = new FileFinderModule(moduleNetwork, oancProperties);

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

			// Prepare FileFinderModule parser module
			Properties oancParserProperties = new Properties();
			oancParserProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME,
					"FileFinderModule-Parser");
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
			moduleNetwork.addModule(fileFinderModule);
			moduleNetwork.addModule(oancParser);
			moduleNetwork.addModule(fileWriter);
			moduleNetwork.addModule(consoleWriter);

			// Connect module ports
			fileFinderModule.getOutputPorts().get("file list").addPipe(new CharPipe(), oancParser.getInputPorts().get("input"));
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

			// Validate JSON
			JsonParser parser = new JsonParser();
			try {
				parser.parse(json);
				parser.parse(json2);
				System.out.println("Successful parsed module network serialization test output.");
			} catch (JsonSyntaxException e){
				fail("Could not parse result.");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
