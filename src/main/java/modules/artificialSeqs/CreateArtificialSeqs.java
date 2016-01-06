package modules.artificialSeqs;

import java.util.Properties;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.ModuleImpl;
import modules.OutputPort;

/**
 * Creates a random sequence of 'ATGC' of defined length
 * 
 * @author Christopher Kraus
 *
 */

public class CreateArtificialSeqs extends modules.ModuleImpl {
	//property keys:
	public static final String PROPERTYKEY_SEQLEN = "Length of the randomly composed DNA sequence";
		
	//variables:
	private String seqString;
	private int seqLength;
	private final String OUTPUTID = "output";

	//constructors:
	public CreateArtificialSeqs(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		//Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_SEQLEN,
				"Length of the randomly composed DNA sequence");
				
		//Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"Artificial Sequence Generator");
		this.getPropertyDefaultValues().put(PROPERTYKEY_SEQLEN,
				"1024");
		
		// Define I/O
		OutputPort outputPort = new OutputPort(OUTPUTID, "Generated sequences.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputPort);
		
		// Add module description
		this.setDescription("Creates a randomly composed DNA sequences of defined length.");
		// Add module category
		this.setCategory("Generators");
	}
		
	//end constructors 
	
	//methods:
	public int currentLen () {
		return seqString.length();
	}
		
	//getters:
	public int getSeqLength() {
		return seqLength;
	}
	
	public String getSeqString() {
		return seqString;
	}
	
	//setters:
	public void setInitialSeqString(String s) {
		seqString = s;
	}
	
	public void setSeqString(String s) {
		seqString += s;
	}
	
	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		if (this.getProperties().containsKey(PROPERTYKEY_SEQLEN))
			this.seqLength = Integer.parseInt(this.getProperties().getProperty(
					PROPERTYKEY_SEQLEN));
		super.applyProperties();
	}

	@Override
	public boolean process() throws Exception {
		try {
			//create random string
			int character;
			character = (int) (Math.random()*4);
			switch (character) {
				case 0: this.setInitialSeqString("A");
					break;
				case 1: this.setInitialSeqString("T");
					break;
				case 2: this.setInitialSeqString("G");
					break;
				case 3: this.setInitialSeqString("C");
					break;
			}

			for (int i = 0; i < (this.getSeqLength() - 1); i++) {
				character = (int) (Math.random()*4);
				switch (character) {
					case 0: this.setSeqString("A");
						break;
					case 1: this.setSeqString("T");
						break;
					case 2: this.setSeqString("G");
						break;
					case 3: this.setSeqString("C");
						break;
				}
			}
			
			//write random sequence
			this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(this.getSeqString());
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		// close outputs 
		this.closeAllOutputs();
		
		//success
		return true;
	}
}
