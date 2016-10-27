package modules.format_conversion.plainText2TreeBuilder;

import java.util.Properties;

import modules.Pipe;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import java.util.Iterator;
import java.util.ArrayList;

import com.google.gson.Gson;

import common.parallelization.CallbackReceiver;

/**
 * This module reads plain text from a file reader and converts it into JSON
 * format used by the suffix.tree.builder module
 * 
 * @author christopher
 *
 */

import base.workbench.ModuleRunner;

public class PlainText2TreeBuilderConverter extends ModuleImpl {

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(PlainText2TreeBuilderConverter.class, args);
	}

	//property keys:
	 //public static final String PROPERTYKEY_
	//end property keys
	
	//variables:
	
	private final String INPUTID = "input";
	private final String OUTPUTID = "output";

	private String inputString;

	private ArrayList<ArrayList <PlainText2TreeNodes>> outNodes;
	
	//end variables
	
	//constructors:
	public PlainText2TreeBuilderConverter (CallbackReceiver callbackReceiver, 
			Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		//Add property descriptions
			//no specific property keys
		
		//Add property defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "PlainText2TreeBuilder");
		
		//Define I/O
		InputPort inputPort = new InputPort(INPUTID, "Plain text character input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUTID, "JSON-encoded FileFinderModule data.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
	}
	
	//end constructors
	
	//setters:
	
	//end setters
	
	//getters:
	
	//end getters
	
	//methods:
	
	@Override
	public boolean process() throws Exception {
		
		//initialize inputString
		this.inputString = "";
		
		//Variables used for input data
		int bufferSize = 1024;
		char [] bufferInput = new char [bufferSize];
		
		// Read first characters
		int charCode = this.getInputPorts().get(INPUTID).getInputReader().read(bufferInput, 0, bufferSize);
		
		// Loop until no more data can be read
		while (charCode != -1) {
			
			// Check for interrupt signal 
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// Convert char array to string buffer
			StringBuffer inputBuffer = new StringBuffer(new String (bufferInput).substring(0, charCode));
			this.inputString += inputBuffer.toString();
			
			// Read next charsplitWords = 
			charCode = this.getInputPorts().get(INPUTID).getInputReader().read(bufferInput, 0, bufferSize);
			
		}
		
		
		//conversion from plain text to JSON format
		convertString();
		
		//write JSON to output
		Gson gson = new Gson();
		Iterator<Pipe> charPipes = this.getOutputPorts().get(OUTPUTID).getPipes(CharPipe.class).iterator();
		while (charPipes.hasNext()) {
			gson.toJson(this.outNodes, ((CharPipe)charPipes.next()).getOutput());
		}
		
		//close outputs
		this.closeAllOutputs();
		
		return true;
		
	}
	
	public void convertString() {
		//split string after each space(s)
		String[] splitLines = this.inputString.split("\n");
		String[] splitWords;
		
		//create dummy strings to feed to the constructor to give proper JSON format
		String dummy = "dummy";
		
		//create outNode List holding all lines
		this.outNodes = new ArrayList<ArrayList<PlainText2TreeNodes>>();
		
		for (int i = 0; i < splitLines.length; i ++) {
			splitWords = splitLines[i].split("\\s+");
			ArrayList <PlainText2TreeNodes> nodes = new ArrayList <PlainText2TreeNodes>();
			for (String j : splitWords) {
				 PlainText2TreeNodes plainText2TreeNodes = new PlainText2TreeNodes (j, dummy, dummy);
				 nodes.add(plainText2TreeNodes);
			}
			this.outNodes.add(nodes);
		}
	}
	
	@Override
	public void applyProperties() throws Exception {
		
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();
		
		// Apply own properties
			//this.regex = this.getProperties().getProperty(PROPERTYKEY_REGEX, this.getPropertyDefaultValues().get(PROPERTYKEY_REGEX));
		
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}
	//end methods
	
}
