package modularization;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.Test;

import parser.oanc.OANC;
import parser.oanc.OANCXMLParser;

public class ModuleTreeTest {

	@Test
	public void test() throws Exception {
		
		String oancLoc0 = System.getProperty("user.home")+File.separator+"Dropbox"+File.separator+"Strings_and_Structures"+File.separator+"ANC"+File.separator+"XCES Format"+File.separator+"written"+File.separator;
		//String oancLoc0 = "/home/marcel/Daten/OANC/OANC-1.0.1-UTF8/data/written_1/journal/slate/8/";
		//String oancLoc0 = "/home/marcel/Daten/OANC/OANC-1.0.1-UTF8/data/spoken/face-to-face/charlotte/";
		String outputFileLocation = System.getProperty("java.io.tmpdir")+File.separator+"test.txt";
		
		// Set up module tree
		ModuleTree mc1 = new ModuleTree();
		
		// Prepare OANC module
		Properties oancProperties = new Properties();
		oancProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "OANC");
		oancProperties.setProperty(OANC.PROPERTYKEY_OANCLOCATION, oancLoc0);
		//oancProperties.setProperty(OANC.PROPERTYKEY_OANCLOCATION+"1", oancLoc1);
		OANC oanc = new OANC(mc1,oancProperties);
		
		mc1.setRootModule(oanc); // Necessary before adding more modules!
		
		// Prepare FileWriter module
		Properties fileWriterProperties = new Properties();
		fileWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "FileWriter");
		fileWriterProperties.setProperty(FileWriterModule.PROPERTYKEY_OUTPUTFILE, outputFileLocation);
		FileWriterModule fileWriter = new FileWriterModule(mc1,fileWriterProperties);
		
		// Prepare OANC parser module
		Properties oancParserProperties = new Properties();
		oancParserProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "OANC-Parser");
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_ADDSTARTSYMBOL, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_ADDTERMINALSYMBOL, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_CONVERTTOLOWERCASE, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_KEEPPUNCTUATION, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_OUTPUTANNOTATEDJSON, Boolean.toString(true));
		OANCXMLParser oancParser = new OANCXMLParser(mc1,oancParserProperties);
		
		// Prepare FileReader module
		Properties fileReaderProperties = new Properties();
		fileReaderProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "FileReader");
		fileReaderProperties.setProperty(FileReaderModule.PROPERTYKEY_INPUTFILE, outputFileLocation);
		FileReaderModule fileReader = new FileReaderModule(mc1,fileReaderProperties);
		
		// Prepare ConsoleWriter module
		Properties consoleWriterProperties = new Properties();
		consoleWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "ConsoleWriter");
		ConsoleWriterModule consoleWriter = new ConsoleWriterModule(mc1,consoleWriterProperties);
		
		// Add modules to tree
		mc1.addModule(consoleWriter, oanc);
		mc1.addModule(oancParser, oanc);
		mc1.addModule(fileWriter, oancParser);
		
		// Print chain
		System.out.println(mc1.prettyPrint());
		
		
		// run chain #1
		System.out.println("Attempting to run chain #1");
		mc1.runModules();
		
		assertTrue(true);
	}

}
