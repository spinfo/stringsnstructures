package modularization;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import modules.CharPipe;
import modules.ModuleImpl;
import modules.ModuleNetwork;
import modules.input_output.ConsoleWriterModule;
import modules.input_output.FileFinderModule;
import modules.input_output.FileWriterModule;
import modules.parser.oanc.OANCXMLParser;

import org.junit.Test;

public class ModuleNetworkTest {

	@Test
	public void test() throws Exception {
		
		String oancLoc0 = "src"+File.separator+"test"+File.separator+"data"+File.separator;
		
		File eingabeVerzeichnis = new File(oancLoc0);
		assertTrue(eingabeVerzeichnis.exists() && eingabeVerzeichnis.isDirectory());
		System.out.println("Eingabeverzeichnis "+oancLoc0+" existiert.");
		
		String outputFileLocation = System.getProperty("java.io.tmpdir")+File.separator+"test.txt";
		System.out.println("Schreibe Testausgabe nach "+outputFileLocation);
		
		// Set up module tree
		ModuleNetwork moduleNetwork = new ModuleNetwork();
		
		// Prepare FileFinderModule module
		Properties oancProperties = new Properties();
		oancProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "FileFinder");
		oancProperties.setProperty(FileFinderModule.PROPERTYKEY_PATHTOSEARCH, oancLoc0);
		oancProperties.setProperty(FileFinderModule.PROPERTYKEY_FILENAMESUFFIX, "txt");
		FileFinderModule fileFinderModule = new FileFinderModule(moduleNetwork,oancProperties);
		
		//moduleNetwork.setRootModule(oanc); // Necessary before adding more modules!
		
		// Prepare FileWriter module
		Properties fileWriterProperties = new Properties();
		fileWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "FileWriter");
		fileWriterProperties.setProperty(FileWriterModule.PROPERTYKEY_OUTPUTFILE, outputFileLocation);
		fileWriterProperties.setProperty(FileWriterModule.PROPERTYKEY_ENCODING, "UTF-8");
		FileWriterModule fileWriter = new FileWriterModule(moduleNetwork,fileWriterProperties);
		
		// Prepare FileFinderModule parser module
		Properties oancParserProperties = new Properties();
		oancParserProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "OANC-Parser");
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_ADDSTARTSYMBOL, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_ADDTERMINALSYMBOL, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_CONVERTTOLOWERCASE, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_KEEPPUNCTUATION, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_OUTPUTANNOTATEDJSON, Boolean.toString(true));
		OANCXMLParser oancParser = new OANCXMLParser(moduleNetwork,oancParserProperties);
		
		// Prepare FileReader module
		/*Properties fileReaderProperties = new Properties();
		fileReaderProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "FileReader");
		fileReaderProperties.setProperty(FileReaderModule.PROPERTYKEY_INPUTFILE, outputFileLocation);
		FileReaderModule fileReader = new FileReaderModule(moduleTree,fileReaderProperties);*/
		
		// Prepare ConsoleWriter module
		Properties consoleWriterProperties = new Properties();
		consoleWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "ConsoleWriter");
		ConsoleWriterModule consoleWriter = new ConsoleWriterModule(moduleNetwork,consoleWriterProperties);
		
		// Prepare ExampleModule module
		/*Properties exampleModuleProperties = new Properties();
		exampleModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "Example Module");
		exampleModuleProperties.setProperty(ExampleModule.PROPERTYKEY_REGEX, "[aeiu]");
		exampleModuleProperties.setProperty(ExampleModule.PROPERTYKEY_REPLACEMENT, "o");
		ExampleModule exampleModule = new ExampleModule(moduleTree, exampleModuleProperties);*/
		
		// Add modules to tree
		moduleNetwork.addModule(fileFinderModule);
		moduleNetwork.addModule(oancParser);
		moduleNetwork.addModule(fileWriter);
		moduleNetwork.addModule(consoleWriter);

		// Connect module ports
		moduleNetwork.addConnection(fileFinderModule.getOutputPorts().get("file list"), oancParser.getInputPorts().get("input"), new CharPipe());
		moduleNetwork.addConnection(oancParser.getOutputPorts().get("output"), fileWriter.getInputPorts().get("input"), new CharPipe());
		moduleNetwork.addConnection(oancParser.getOutputPorts().get("output"), consoleWriter.getInputPorts().get("input"), new CharPipe());
		
		// Add modules to tree
		//moduleNetwork.addConnection(oancParser, oanc);
		//moduleNetwork.addConnection(fileWriter, oancParser);
		//moduleNetwork.addConnection(consoleWriter, oancParser);
		//moduleTree.addModule(exampleModule, oancParser);
		//moduleTree.addModule(consoleWriter, exampleModule);
		
		// Print tree
		System.out.println(moduleNetwork.toString());
		
		
		// Run modules in tree
		System.out.println("Attempting to run module tree");
		moduleNetwork.runModules(true);
		
		assertTrue(true);
	}

}
