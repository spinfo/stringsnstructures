package modules.format_conversion;

import java.awt.event.KeyEvent;
import java.lang.Character.UnicodeBlock;
import java.util.Properties;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class TextReducerModule extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_DELIMITER_INPUT = "input token delimiter";
	public static final String PROPERTYKEY_DIRECTION = "direction";
	public static final String PROPERTYKEY_ENCDELIMITERS = "encode delimiters";
	
	// Define I/O IDs
	private static final String ID_INPUT = "input";
	private static final String ID_INPUT_DICT = "dictionary";
	private static final String ID_OUTPUT = "output";
	private static final String ID_OUTPUT_DICT = "dictionary";
	
	// Regex definitions
	public static final String REGEX_WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";
	
	// Local variables
	private String inputdelimiter;
	private String extendedInputdelimiter;
	private boolean encode;
	private boolean encodeDelimiters;

	public TextReducerModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Encodes the input text by replacing each token with a unique character, producing a reduced text and a dictionary. Can also decode (requires dictionary input).");

		// Add property defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Text Reducer");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT, "[\\s\\n\\r]");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DIRECTION, "encode");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ENCDELIMITERS, "false");
		
		// Add property descriptions
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_INPUT, "Part of a regular expression to use as token delimiter (also applicable if decoding when the input delimiters have not been encoded). With the default value<pre>"+this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT)+"</pre>the full regex would be<pre>"+String.format(REGEX_WITH_DELIMITER, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT))+"</pre>This is in order to get both the tokens <i>and</i> the delimiters from the scanner that parses the input. Only single char matches are supported.");
		this.getPropertyDescriptions().put(PROPERTYKEY_DIRECTION, "Direction [encode|decode]. Decoding requires input both on port '"+ID_INPUT+"' and '"+ID_INPUT_DICT+"'");
		this.getPropertyDescriptions().put(PROPERTYKEY_ENCDELIMITERS, "Encode/decode input delimiters same as tokens [true] or keep them as they are [false].");
		
		// Define I/O
		InputPort inputPort = new InputPort(ID_INPUT, "Plain text character input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		InputPort inputPortDict = new InputPort(ID_INPUT_DICT, "Dictionary input.", this);
		inputPortDict.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "Plain text character output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPortDict = new OutputPort(ID_OUTPUT_DICT, "Dictionary output.", this);
		outputPortDict.addSupportedPipe(CharPipe.class);
		
		// Add I/O ports to instance
		super.addInputPort(inputPort);
		super.addInputPort(inputPortDict);
		super.addOutputPort(outputPort);
		super.addOutputPort(outputPortDict);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process() throws Exception {
		
		// Construct scanner instances for input segmentation
		Scanner inputScanner = new Scanner(this.getInputPorts().get(ID_INPUT).getInputReader());
		inputScanner.useDelimiter(this.extendedInputdelimiter);
		
		// Encoding/decoding dictionaries
		LinkedTreeMap<String,String> dictionaryValueEnc;
		LinkedTreeMap<String,String> dictionaryEncValue;
		
		// Instantiate JSON parser
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		// encoding character index (will start with space char index)
		int charIndex = 31;
		
		// If we are decoding, read the dictionary from input, else we create new instances.
		if (this.encode){
			// When encoding, we need both dictionaries
			dictionaryEncValue = new LinkedTreeMap<String,String>();
			dictionaryValueEnc = new LinkedTreeMap<String,String>();
		} else {
			// For decoding, the decoding dictionary suffices
			dictionaryEncValue = gson.fromJson(this.getInputPorts().get(ID_INPUT_DICT).getInputReader(), new LinkedTreeMap<String,String>().getClass());
			dictionaryValueEnc = null;
			// If the dictionary is missing, we cannot continue
			if (dictionaryEncValue == null){
				inputScanner.close();
				this.closeAllOutputs();
				throw new Exception("Cannot read dictionary -- aborting.");
			}
		}
		
		// Input read loop
		while (inputScanner.hasNext()){
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				inputScanner.close();
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// Retrieve next token from input scanner
			String token = inputScanner.next();
			
			// Determine output dependent on encode/decode switch
			String output;
			if (this.encode){
				
				// If delimiters are to be retained as is, check whether read token is a delimiter
				if (!this.encodeDelimiters && token.matches(this.inputdelimiter)){
					output = token;
				} else {
					// Retrieve previously encoded symbol
					output = dictionaryValueEnc.get(token);
					
					// If the current token has not yet been encoded, do so now
					if (output == null){
						do {
							// Increment char index and construct a one-char string as placeholder
							try {
								charIndex = this.incCharIndex(charIndex);
							} catch (Exception e) {
								inputScanner.close();
								this.closeAllOutputs();
								throw new Exception("Too many different token to encode (over "+dictionaryEncValue.size()+"), try reducing variety.",e);
							}
							output = new Character((char)charIndex).toString();
						}
						// Skip those that match delimiters (if those are to be retained) or that already exist as keys in the dictionary
						while ((!this.encodeDelimiters && output.matches(this.inputdelimiter)) || dictionaryEncValue.get(output) != null);
						// Write to dictionaries
						dictionaryValueEnc.put(token, output);
						dictionaryEncValue.put(output, token);
					}
				}
				
			} else if (!this.encodeDelimiters && token.matches(this.inputdelimiter)){
				output = token;
			} else {
				// Retrieve unencoded value from dictionary
				output = dictionaryEncValue.get(token);
			}
			// Write to output
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(output);
		}
		
		// Output decoding dictionary
		this.getOutputPorts().get(ID_OUTPUT_DICT).outputToAllCharPipes(gson.toJson(dictionaryEncValue));

		//Close input scanners.
		inputScanner.close();
		
		// Close outputs
		this.closeAllOutputs();
		
		// Done
		return true;
	}
	
	/**
	 * Increases the specified index to the next higher Unicode code point value that denotes a printable character. 
	 * @param charIndex Index to increment
	 * @return incremented value
	 * @throws Exception Thrown if incremented value is beyond the max code point range
	 */
	private int incCharIndex(int charIndex) throws Exception {
		UnicodeBlock unicodeBlock = null;
		do {
			// Increment code point
			charIndex++;

			// Check if we are beyond the maximum code point
			if (charIndex > Character.MAX_CODE_POINT)
				throw new Exception("Maximum Unicode code point reached -- cannot continue.");
			
			// Determine the Unicode block of that code point
			unicodeBlock = Character.UnicodeBlock.of(charIndex);
		}
		// Iterate until the code point denotes a printable character
		while (Character.isISOControl(charIndex)
				|| charIndex == KeyEvent.CHAR_UNDEFINED
				|| unicodeBlock == null
				|| unicodeBlock == Character.UnicodeBlock.SPECIALS);
		// Return final code point value
		return charIndex;
	}
	
	@Override
	public void applyProperties() throws Exception {
		
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();
		
		// Apply own properties
		String value = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_INPUT, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT));
		if (value != null && !value.isEmpty()){
			this.inputdelimiter = value;
			this.extendedInputdelimiter = String.format(REGEX_WITH_DELIMITER, value);
		}
		
		value = this.getProperties().getProperty(PROPERTYKEY_DIRECTION, this.getPropertyDefaultValues().get(PROPERTYKEY_DIRECTION));
		if (value != null && !value.isEmpty())
			if (value.equalsIgnoreCase("encode"))
				this.encode = true;
			else {
				this.encode = false;
				this.extendedInputdelimiter = "";
			}
		
		value = this.getProperties().getProperty(PROPERTYKEY_ENCDELIMITERS, this.getPropertyDefaultValues().get(PROPERTYKEY_ENCDELIMITERS));
		if (value != null && !value.isEmpty())
			this.encodeDelimiters = Boolean.parseBoolean(value);
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
