package modules.basemodules;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.ModuleImpl;
import modules.InputPort;
import modules.OutputPort;
import modules.Pipe;

import java.util.Iterator;
import java.util.Properties;

//Gson import
import com.google.gson.Gson;

public class ExampleGsonSerialization extends ModuleImpl {
	//Add property keys:
	/* no property keys */
			
	//Add properties variables:
	/* no property variables */
		
	//Add variables:
	private String inputString;
	
	//Add I/O labels
	private final String INPUTID = "string";
	private final String OUTPUTID = "JSON";
	
	//Add constructors:
	public ExampleGsonSerialization (CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		//Add description for properties
		/* no property keys available */
				
		//Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"Example Gson Serialization Module");
		
		//Define I/O
		
		InputPort inputPort = new InputPort (INPUTID, "plain text input", this);
		inputPort.addSupportedPipe(CharPipe.class);
		
		OutputPort outputPort = new OutputPort(OUTPUTID, "serialized JSON output", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		
		//Add module description
		this.setDescription("<html><h1>JSON Serialization Example</h1><p>Example Module: Serializing a plain test string.<br/> Output is JSON format.</p></html>");
		
		//Add module category
		this.setCategory("Examples");
	}
	
	//Add methods:
	
	//Add process() method:
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
			
			// Read next charsplitWords
			charCode = this.getInputPorts().get(INPUTID).getInputReader().read(bufferInput, 0, bufferSize);
			
		}
		
		//write JSON to output
		Gson gson = new Gson();
		Iterator<Pipe> charPipes = this.getOutputPorts().get(OUTPUTID).getPipes(CharPipe.class).iterator();
		while (charPipes.hasNext()) {
			gson.toJson(this.inputString, ((CharPipe)charPipes.next()).getOutput());
		}
		
		// close outputs 
		this.closeAllOutputs();
		
		//success
		return true;
	}
	
	//Add applyProperties() method:
	@Override
	public void applyProperties() throws Exception {
		
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();
		
		// Apply own properties
		/* no own properties */
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}



