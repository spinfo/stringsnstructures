package modules.examples;

import java.util.Properties;

import base.workbench.ModuleRunner;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.ModuleImpl;
import modules.OutputPort;

/**
 * Creates a random string of defined length
 * 
 * @author Christopher Kraus
 *
 */

public class ExampleRandString extends modules.ModuleImpl {
	
	//Add property keys:
	public static final String PROPERTYKEY_STRLEN = "Length of the random String";
			
	//Add properties variables:
	private int strLength;
		
	//Add variables:
	private String string;
	
	//Add I/O labels
	private final String OUTPUTID = "output";
	
	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(ExampleRandString.class, args);
	}
	
	//Add constructors:
	public ExampleRandString(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		//Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_STRLEN,
				"Length of the randomly composed String");
				
		//Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"Random String Example Module");
		
		this.getPropertyDefaultValues().put(PROPERTYKEY_STRLEN,
				"5");
		
		//Define I/O
		OutputPort outputPort = new OutputPort(OUTPUTID, "Generated string.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputPort);
		
		//Add module description
		this.setDescription("Creates a random string of defined length.");
		
		// You can override the automatic category selection (for example if a module is to be shown in "deprecated")
		//this.setCategory("Examples");
	} 
	
	//Add methods:
	
	//Add setters:
		
	public void setSeqString(String s) {
		this.string += s;
	}
		
	//Add getters:
	
	public String getString() {
		return this.string;
	}
	
	//Add "applyProperties() method
	@Override
	public void applyProperties() throws Exception {
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();
		
		// Apply own properties
		if (this.getProperties().containsKey(PROPERTYKEY_STRLEN))
			this.strLength = Integer.parseInt(this.getProperties().getProperty(
					PROPERTYKEY_STRLEN));
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}
	
	//Add "process()" method
	@Override
	public boolean process() throws Exception {
	
		//create random string
		int charNum;
		charNum = (int) (Math.random()*2);
		switch (charNum) {
			case 0: this.string = "A";
				break;
			case 1: this.string = "B";
				break;
		}

		for (int i = 0; i < (this.strLength - 1); i++) {
			charNum = (int) (Math.random()*2);
			switch (charNum) {
				case 0: this.string += "A"; //adding "A" via concatenation
					break;
				case 1: this.setSeqString("B"); //adding "B" via setter method
					break;
			}
		}
		
		//write random string to output pipe with a try block
		this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(this.string);
		
		// close outputs 
		this.closeAllOutputs();
		
		//success
		return true;
	}

}
