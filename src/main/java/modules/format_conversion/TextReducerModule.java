package modules.format_conversion;

import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

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
	public static final String PROPERTYKEY_DELIMITER_OUTPUT = "output token delimiter";
	public static final String PROPERTYKEY_DIRECTION = "direction";
	
	// Define I/O IDs
	private static final String ID_INPUT = "unencoded";
	private static final String ID_INPUT_DICT = "dictionary";
	private static final String ID_OUTPUT = "output";
	private static final String ID_OUTPUT_DICT = "dictionary";
	
	// Local variables
	private String inputdelimiter;
	private String outputdelimiter;
	private boolean encode;

	public TextReducerModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Reduces text tokens to a single unique character (or the reverse).");

		// Add property descriptions
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_INPUT, "Regular expression to use as token delimiter for input (when decoding, set to '\\z')");
		this.getPropertyDescriptions().put(PROPERTYKEY_DIRECTION, "Direction [encode|decode]. Decoding requires input both on port '"+ID_INPUT+"' and '"+ID_INPUT_DICT+"'");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT, "String to insert as token delimiter into the output");
		
		// Add property defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Text Reducer");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT, "\\s+");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DIRECTION, "encode");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT, " ");
		
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
		inputScanner.useDelimiter(this.inputdelimiter);
		
		// Encoding dictionary
		LinkedTreeMap<String,String> dictionary;
		
		// Instantiate JSON parser
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		// encoding character index (starts with space char index)
		int charIndex = 32;
		
		// If we are decoding, read the dictionary from input, else we create a new instance.
		if (this.encode)
			dictionary = new LinkedTreeMap<String,String>();
		else
			dictionary = gson.fromJson(this.getInputPorts().get(ID_INPUT_DICT).getInputReader(), new LinkedTreeMap<String,String>().getClass());
		
		// Input read loop
		while (inputScanner.hasNext()){
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				inputScanner.close();
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			String token = inputScanner.next();
			
			if (this.encode){
				String output = dictionary.get(token);
				if (output == null){
					output = new Character((char)charIndex).toString();
					dictionary.put(token, output);
					charIndex++;
				}
				this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(output+this.outputdelimiter);
			} else {
				// TODO
			}
		}
		
		// Output dictionary
		this.getOutputPorts().get(ID_OUTPUT_DICT).outputToAllCharPipes(gson.toJson(dictionary));

		//Close input scanners.
		inputScanner.close();
		
		
		
		// Close outputs
		this.closeAllOutputs();
		
		// Done
		return true;
	}
	
	@Override
	public void applyProperties() throws Exception {
		
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();
		
		// Apply own properties
		this.inputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_INPUT, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT));
		this.outputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT));
		
		String value = this.getProperties().getProperty(PROPERTYKEY_DIRECTION, this.getPropertyDefaultValues().get(PROPERTYKEY_DIRECTION));
		if (value != null && !value.isEmpty())
			this.encode = Boolean.parseBoolean(value);
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
