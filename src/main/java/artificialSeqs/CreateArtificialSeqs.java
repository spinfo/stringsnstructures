package artificialSeqs;

import java.util.Properties;
import parallelization.CallbackReceiver;
import modularization.CharPipe;

public class CreateArtificialSeqs extends modularization.ModuleImpl {
	//property keys:
	public static final String PROPERTYKEY_SEQLEN = "Length of the randomly composed DNA sequence";
		
	//variables:
	private String seqString;
	private int seqLength;

	//constructors:
	public CreateArtificialSeqs(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		//Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_SEQLEN,
				"Length of the randomly composed DNA sequence");
				
		//Add default values
		this.getPropertyDefaultValues().put(PROPERTYKEY_SEQLEN,
				"1024");
		
		// Define I/O
		this.getSupportedInputs().add(CharPipe.class);
		
		// Add module description
		this.setDescription("Creates a randomly composed DNA sequences of defined length.");	
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

			for (int i = 0; i < this.getSeqLength(); i++) {
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
			this.outputToAllCharPipes(this.getSeqString());
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//success
		return true;
	}
}
