package seqSplitting;

import java.util.ArrayList;
import java.util.Properties;

import modularization.CharPipe;
import modularization.ModuleImpl;
import parallelization.CallbackReceiver;

/**
 * Reads input sequences from I/O pipes.
 * Splits input sequences randomly.
 * "Words" will be separated by whitespace (space).
 * Writes new "Corpus" to I/O pipes.
 * 
 * @author Christopher Kraus
 *
 */

public class SeqMemory extends modularization.ModuleImpl {
	//variables:
	private int splitNum;
	private String wholeSequence = "";
	private int seqLength;
	private ArrayList <SeqRandomSplit> seqSplits; //create array of sorted fragmented sequences
		
	// end variables
	
	//property keys:
		public static final String PROPERTYKEY_SPLITS = "Number of splits";
		
	//end property keys
		
	//constructors:
	public SeqMemory (CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		// Add property descriptions
		this.getPropertyDescriptions().put(PROPERTYKEY_SPLITS, "Insert an integer");
						
		// Add property defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Random Sequence Splitting");
		this.getPropertyDefaultValues().put(PROPERTYKEY_SPLITS, "100");
						
		// Define I/O
		this.getSupportedInputs().add(CharPipe.class);
		this.getSupportedOutputs().add(CharPipe.class);
				
	}
	//end constructors
	
	//methods:
	
	//setters:
	public void addWholeSeq(String inputSeq) {
		wholeSequence =  wholeSequence.concat(inputSeq);
	}
	
	public void setSeqLength() {
		seqLength = wholeSequence.length();
	}
	
	public void initialzeNewSeqSplits() {
		seqSplits = new ArrayList <SeqRandomSplit> ();
	}
	//end setters
	
	//getters:
	public String getWholeSeq () {
		return wholeSequence;
	}
	
	
	public int getSplitNum() {
		return splitNum;
	}
	//end getters
	
	@Override
	public boolean process() throws Exception {
				
		// Variables used for input data
				int bufferSize = 1024;
				char[] buffer = new char[bufferSize];
				
				// Read first sequence chunk
				int readChars = this.getInputCharPipe().read(buffer, 0, bufferSize);
				
				//String initialChunk = new String(buffer);
				//this.initializeWholeSeq(initialChunk);
				
				// Loop until no more data can be read
				while (readChars != -1){
					
					// Check for interrupt signal
					if (Thread.interrupted()) {
						this.closeAllOutputs();
						throw new InterruptedException("Thread has been interrupted.");
					}
					
					// Convert char array to string
					// 0-readChars
					String inputChunk = new String(buffer).substring(0, readChars);
					
					// Process data
					this.addWholeSeq(inputChunk);
												
					// Read next chunk of data
					readChars = this.getInputCharPipe().read(buffer, 0, bufferSize);
				}
				
				//writing split output as single string with spaces in between
				this.setSeqLength();
				this.initialzeNewSeqSplits();
				this.randomSplit();
				
				//writing split sequence by fragments into the I/O pipe for other modules
				for (int i = 0; i < (seqSplits.size() - 1); i ++) {
					this.outputToAllCharPipes(seqSplits.get(i).getSubsequence());
					this.outputToAllCharPipes(" ");
				}
										
				// Close outputs (important!)
				this.closeAllOutputs();
				
				// Done
				return true;
	}
	
	@Override
	public void applyProperties() throws Exception {
		// Apply own properties
		if (this.getProperties().containsKey(PROPERTYKEY_SPLITS))
				this.splitNum = Integer.parseInt(this.getProperties().getProperty(PROPERTYKEY_SPLITS, 
				this.getPropertyDefaultValues().get(PROPERTYKEY_SPLITS)));
				
		// Apply parent object's properties
		super.applyProperties();
	}

	public void randomSplit() {
		int initSplitPos = (int) ((Math.random()* wholeSequence.length()) / splitNum);
		String fragSeq = wholeSequence.substring(0, initSplitPos);
		seqSplits.add(new SeqRandomSplit(fragSeq, 0, initSplitPos, wholeSequence.length()));
		
		//next split will be at the right side of the last one
		int lastSplitPos = initSplitPos + 1; // move the current first split one to the right
		int newSplitPos = (int) ((Math.random() * (seqLength - initSplitPos)) / (splitNum - 1) + (initSplitPos + 1)); /* define
		a split position which is larger than the last one */
		
		fragSeq = wholeSequence.substring(lastSplitPos, newSplitPos);
		seqSplits.add(new SeqRandomSplit(fragSeq , lastSplitPos ,newSplitPos, wholeSequence.length()));
		
		//iterate over the remaining sequence and always split it to the right side
		for (int i = 2; i < (this.getSplitNum() - 1); i ++) {
			lastSplitPos = newSplitPos + 1;
			newSplitPos = (int) ((Math.random()*(seqLength - newSplitPos)) / (splitNum - i)+ (newSplitPos + 1));
			fragSeq = wholeSequence.substring(lastSplitPos, newSplitPos);
			seqSplits.add(new SeqRandomSplit(fragSeq, lastSplitPos, newSplitPos, wholeSequence.length()));
		}
		
	}
	
	//end methods
}