package modules.generators.artificialSeqs;

import java.util.Properties;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.ModuleImpl;
import modules.OutputPort;

public class CreateArtificialSeqsContent extends modules.ModuleImpl {
	//property keys:
	public static final String PROPERTYKEY_SEQLEN = "Length of the randomly composed DNA sequence";
	public static final String PROPERTYKEY_GCCONTENT = "Amount of letters 'C' and 'G'";
	
	//variables:
	private String seqString;
	private int seqLength;
	private float gcContent;
	private final String OUTPUTID = "output";

	//constructors:
	public CreateArtificialSeqsContent(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		//Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_SEQLEN,
				"Length of the randomly composed DNA sequence");
		this.getPropertyDescriptions().put(PROPERTYKEY_GCCONTENT,"Amount of letters 'C' and 'G'");
				
		//Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"GC adjusted Sequence Generator");
		this.getPropertyDefaultValues().put(PROPERTYKEY_SEQLEN,
				"1024");
		this.getPropertyDefaultValues().put(PROPERTYKEY_GCCONTENT,
				"0.5");
		
		// Define I/O
		OutputPort outputPort = new OutputPort(OUTPUTID, "Generated sequences.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputPort);
		
		// Add module description
		this.setDescription("Creates a randomly composed DNA sequences of defined length. Allows for specification of the amount of Letters 'C' and 'G', in the form of a decimal probability.");
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
	
	public float getGcContent() {
		return gcContent;
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
		if (this.getProperties().containsKey(PROPERTYKEY_GCCONTENT))
			this.gcContent = Float.parseFloat(this.getProperties().getProperty(
					PROPERTYKEY_GCCONTENT));
		super.applyProperties();
	}

	@Override
	public boolean process() throws Exception {
		try {
			//create random string
			float gc;
			
			gc = (float) Math.random();
			
			if (gc < gcContent ) {
				
				int character;
				character = (int) (Math.random()*2);
				switch (character) {
					case 0: this.setInitialSeqString("G");
						break;
					case 1: this.setInitialSeqString("C");
						break;
				}
				
			} else {

				int character;
				character = (int) (Math.random()*2);
				switch (character) {
					case 0: this.setInitialSeqString("A");
						break;
					case 1: this.setInitialSeqString("T");
						break;
				}
				
			}

			for (int i = 0; i < (this.getSeqLength() - 1); i++) {
				
				gc = (float) Math.random();
				
				if (gc < gcContent ) {
					
					int character;
					character = (int) (Math.random()*2);
					switch (character) {
						case 0: this.setSeqString("G");
							break;
						case 1: this.setSeqString("C");
							break;
					}
					
				} else {

					int character;
					character = (int) (Math.random()*2);
					switch (character) {
						case 0: this.setSeqString("A");
							break;
						case 1: this.setSeqString("T");
							break;
					}
					
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