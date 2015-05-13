package modularization;

import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

import parallelization.CallbackReceiverImpl;
import parser.oanc.OANC;
import parser.oanc.OANCXMLParser;

public class ModuleChainTest {

	@Test
	public void test() throws Exception {
		
		String oancLoc0 = "/home/marcel/Daten/OANC/OANC-1.0.1-UTF8/data/written_1/journal/slate/1/";
		String oancLoc1 = "/home/marcel/Daten/OANC/OANC-1.0.1-UTF8/data/written_1/journal/slate/2/";
		String outputFileLocation = "/tmp/test.txt";
		
		ModuleChain mc = new ModuleChain();
		
		// Prepare OANC module
		Properties oancProperties = new Properties();
		oancProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "OANC");
		oancProperties.setProperty(OANC.PROPERTYKEY_OANCLOCATION+"0", oancLoc0);
		oancProperties.setProperty(OANC.PROPERTYKEY_OANCLOCATION+"1", oancLoc1);
		OANC oanc = new OANC(mc,oancProperties);
		
		// Prepare OANC parser module
		Properties oancParserProperties = new Properties();
		oancParserProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "OANC-Parser");
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_ADDSTARTSYMBOL, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_ADDTERMINALSYMBOL, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_CONVERTTOLOWERCASE, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_KEEPPUNCTUATION, Boolean.toString(true));
		OANCXMLParser oancParser = new OANCXMLParser(mc,oancParserProperties);
		
		// Prepare FileWriter module
		Properties fileWriterProperties = new Properties();
		fileWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "FileWriter");
		fileWriterProperties.setProperty(FileWriterModule.PROPERTYKEY_OUTPUTFILE, outputFileLocation);
		FileWriterModule fileWriter = new FileWriterModule(mc,fileWriterProperties);
		
		
		mc.appendModule(oanc);
		mc.appendModule(oancParser);
		mc.appendModule(fileWriter);
		
		System.out.println(mc.prettyPrint());
		
		System.out.println("Attempting to run chain");
		mc.runChain();
		System.out.println("chain started");
		
		assertTrue(true);
	}

}
