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
		
		CallbackReceiverImpl receiver = new CallbackReceiverImpl(){
			
		};
		
		// Prepare OANC module
		Properties oancProperties = new Properties();
		oancProperties.setProperty(OANC.PROPERTYKEY_OANCLOCATION+"0", oancLoc0);
		oancProperties.setProperty(OANC.PROPERTYKEY_OANCLOCATION+"1", oancLoc1);
		OANC oanc = new OANC();
		oanc.setProperties(oancProperties);
		
		// Prepare OANC parser module
		Properties oancParserProperties = new Properties();
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_ADDSTARTSYMBOL, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_ADDTERMINALSYMBOL, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_CONVERTTOLOWERCASE, Boolean.toString(true));
		oancParserProperties.setProperty(OANCXMLParser.PROPERTYKEY_KEEPPUNCTUATION, Boolean.toString(true));
		OANCXMLParser oancParser = new OANCXMLParser();
		oancParser.setProperties(oancParserProperties);
		
		
		ModuleChain mc = new ModuleChain();
		mc.appendModule(oanc, 0);
		mc.appendModule(oancParser, 1);
		
		System.out.println(mc.prettyPrint());
		
		mc.runChain();
		
		assertTrue(true);
	}

}
