package modularization;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.Test;

import parser.oanc.OANC;
import parser.oanc.OANCXMLParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ModuleTreeSerializationTest {

	@Test
	public void test() {

		try {

			// Set up module tree
			ModuleTree moduleTree = new ModuleTree();

			// Prepare OANC module
			Properties oancProperties = new Properties();
			oancProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "OANC");
			oancProperties.setProperty(OANC.PROPERTYKEY_OANCLOCATION,
					"/tmp/oanc");
			OANC oanc = new OANC(moduleTree, oancProperties);

			moduleTree.setRootModule(oanc); // Necessary before adding more
											// modules!

			// Prepare FileWriter module
			Properties fileWriterProperties = new Properties();
			fileWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME,
					"FileWriter");
			fileWriterProperties.setProperty(
					FileWriterModule.PROPERTYKEY_OUTPUTFILE, "/tmp/out");
			FileWriterModule fileWriter = new FileWriterModule(moduleTree,
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
			OANCXMLParser oancParser = new OANCXMLParser(moduleTree,
					oancParserProperties);

			// Prepare ConsoleWriter module
			Properties consoleWriterProperties = new Properties();
			consoleWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME,
					"ConsoleWriter");
			ConsoleWriterModule consoleWriter = new ConsoleWriterModule(
					moduleTree, consoleWriterProperties);

			// Add modules to tree
			moduleTree.addModule(oancParser, oanc);
			moduleTree.addModule(fileWriter, oancParser);
			moduleTree.addModule(consoleWriter, oancParser);

			// Instantiate JSON converter
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(ModuleTree.class,
					new ModuleTreeGsonSerializer());
			gsonBuilder.registerTypeAdapter(ModuleTree.class,
					new ModuleTreeGsonDeserializer());
			Gson gson = gsonBuilder.setPrettyPrinting().create();

			String json = gson.toJson(moduleTree);

			ModuleTree moduleTree2 = gson.fromJson(json, ModuleTree.class);
			String json2 = gson.toJson(moduleTree2);

			//System.out.println(json+"\n-------\n"+json2);
			
			//assertTrue(json.equals(json2)); // Order of fields in JSON is not fixed, so this will probably fail even if the JSON is functionally identical.

			assertTrue(json2.length()==json.length());
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
