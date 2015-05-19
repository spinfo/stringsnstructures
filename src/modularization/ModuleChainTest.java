package modularization;

import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

import parser.oanc.OANC;
import parser.oanc.OANCXMLParser;

public class ModuleChainTest {

	@Test
	public void test() throws Exception {
		
		String oancLoc0 = "/home/marcel/Daten/OANC/OANC-1.0.1-UTF8/data/written_1/journal/slate/1/";
		//String oancLoc1 = "/home/marcel/Daten/OANC/OANC-1.0.1-UTF8/data/written_1/journal/slate/2/";
		String outputFileLocation = "/tmp/test.txt";
		
		// Set up first mudule chain
		ModuleChain mc1 = new ModuleChain();
		
		// Prepare OANC module
		Properties oancProperties = new Properties();
		oancProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "OANC");
		oancProperties.setProperty(OANC.PROPERTYKEY_OANCLOCATION, oancLoc0);
		//oancProperties.setProperty(OANC.PROPERTYKEY_OANCLOCATION+"1", oancLoc1);
		OANC oanc = new OANC(mc1,oancProperties);
		
		// Prepare FileWriter module
		Properties fileWriterProperties = new Properties();
		fileWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "FileWriter");
		fileWriterProperties.setProperty(FileWriterModule.PROPERTYKEY_OUTPUTFILE, outputFileLocation);
		FileWriterModule fileWriter = new FileWriterModule(mc1,fileWriterProperties);
		
		// Append modules to chain
		mc1.appendModule(oanc);
		mc1.appendModule(fileWriter);
		
		// Print chain
		System.out.println(mc1.prettyPrint());
		
		// Set up second mudule chain
		ModuleChain mc2 = new ModuleChain();
		
		// Prepare OANC parser module
		Properties oancParserProperties = new Properties();
		oancParserProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "OANC-Parser");
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_ADDSTARTSYMBOL, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_ADDTERMINALSYMBOL, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_CONVERTTOLOWERCASE, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_KEEPPUNCTUATION, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_OUTPUTANNOTATEDJSON, Boolean.toString(true));
		OANCXMLParser oancParser = new OANCXMLParser(mc2,oancParserProperties);
		
		// Prepare FileReader module
		Properties fileReaderProperties = new Properties();
		fileReaderProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "FileReader");
		fileReaderProperties.setProperty(FileReaderModule.PROPERTYKEY_INPUTFILE, outputFileLocation);
		FileReaderModule fileReader = new FileReaderModule(mc2,fileReaderProperties);
		
		// Prepare ConsoleWriter module
		Properties consoleWriterProperties = new Properties();
		consoleWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "ConsoleWriter");
		ConsoleWriterModule consoleWriter = new ConsoleWriterModule(mc2,consoleWriterProperties);
		
		// Append modules to chain
		mc2.appendModule(fileReader);
		mc2.appendModule(oancParser);
		mc2.appendModule(consoleWriter);
		
		// Print chain
		System.out.println(mc2.prettyPrint());
		
		
		// run chain #1
		System.out.println("Attempting to run chain #1");
		mc1.runChain();
		
		// run chain #2
		System.out.println("Attempting to run chain #2");
		mc2.runChain();
		
		assertTrue(true);
	}

}
