package modules.examples;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.ModuleImpl;
import modules.InputPort;
import modules.OutputPort;

import java.util.Properties;

//Gson import
import com.google.gson.Gson;

public class ExampleGsonDeserialization extends ModuleImpl {
	//Add property keys:
	/* no property keys */
			
	//Add properties variables:
	/* no property variables */
		
	//Add variables:
	private String inputJSON;
	
	//Add I/O labels
	private final String INPUTID = "JSON";
	private final String OUTPUTID = "String";
	
	//Add constructors:
	public ExampleGsonDeserialization (CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		//Add description for properties
		/* no property keys available */
				
		//Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"Example Gson Deserialization Module");
		
		//Define I/O
		
		InputPort inputPort = new InputPort (INPUTID, "serialized JSON input", this);
		inputPort.addSupportedPipe(CharPipe.class);
		
		OutputPort outputPort = new OutputPort(OUTPUTID, "deserialized string output", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		
		//Add module description
		this.setDescription("Example Module: Deserializing a test string in JSON format.<br/> Output is plain text.");
		
		//Add module category
		this.setCategory("Examples");
	}
	//Add methods:

	//Add process() method:
	@Override
	public boolean process() throws Exception {
	
		//Read JSON from input pipe
		Gson gson = new Gson ();
		this.inputJSON = gson.fromJson(this.getInputPorts().get(INPUTID).getInputReader(), String.class);
		
		//Write plain string to output pipe
		this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(this.inputJSON);
		
		//Close outputs 
		this.closeAllOutputs();
		
		//Success
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
