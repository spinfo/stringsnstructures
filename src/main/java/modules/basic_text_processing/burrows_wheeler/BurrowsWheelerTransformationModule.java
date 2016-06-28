package modules.basic_text_processing.burrows_wheeler;

import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;

import base.workbench.ModuleWorkbenchController;
import common.StringUnescaper;
import common.StringUtil;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class BurrowsWheelerTransformationModule extends ModuleImpl {

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_DELIMITER_INPUT_REGEX = "input delimiter regex";
	public static final String PROPERTYKEY_DELIMITER_OUTPUT = "output delimiter";
	public static final String PROPERTYKEY_REVERSE = "reverse transform";
	public static final String PROPERTYKEY_END_CHAR = "string end char";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "input";
	private static final String ID_OUTPUT = "output";

	// Local variables
	private String inputdelimiter;
	private String outputdelimiter;
	private boolean reverse;
	private char stringEndChar;

	public BurrowsWheelerTransformationModule(CallbackReceiver callbackReceiver, Properties properties)
			throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription(
				"Performs the Burrows-Wheeler Transformation (see https://en.wikipedia.org/wiki/Burrows-Wheeler_transform)");

		// Add module category
		this.setCategory("Basic text processing");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_INPUT_REGEX,
				"Regular expression to use as string delimiter.");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT,
				"String to insert as string delimiter into the output (will be unescaped).");
		this.getPropertyDescriptions().put(PROPERTYKEY_REVERSE,
				"Conduct reverse Burrows-Wheeler Transformation [true|false].");
		this.getPropertyDescriptions().put(PROPERTYKEY_END_CHAR,
				"String end character; needed for reverse transformation.");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Burrows-Wheeler Transform");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT_REGEX, ModuleWorkbenchController.LINEBREAKREGEX);
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT, "\\n");
		this.getPropertyDefaultValues().put(PROPERTYKEY_REVERSE, "false");
		this.getPropertyDefaultValues().put(PROPERTYKEY_END_CHAR, "$");


		// Define I/O
		InputPort inputPort = new InputPort(ID_INPUT, "input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "output.", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {
		
		// Construct scanner instance for input reading
		Scanner lineScanner = new Scanner(this.getInputPorts().get(ID_INPUT).getInputReader());
		lineScanner.useDelimiter(this.inputdelimiter);

		// Data lines input read loop
		while (lineScanner.hasNext()) {

			// Check for interrupt signal
			if (Thread.interrupted()) {
				lineScanner.close();
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			String segment = lineScanner.next();
			String output;
			if (this.reverse)
				output = this.bwt_reverse(segment, this.stringEndChar);
			else
				output = this.bwt(segment);
			
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(output+this.outputdelimiter);
			

		}
		
		// Close scanner instance
		lineScanner.close();

		// Close outputs (important!)
		this.closeAllOutputs();

		// Done
		return true;
	}
	
	private String bwt(String str){
		// Return if input is empty
		if (str == null || str.isEmpty())
			return str;
		
		// Create table for BWT
		String[] table = new String[str.length()];
		table[0] = str;
		for (int i=1; i<str.length(); i++){
			table[i] = StringUtil.shift(table[i-1]);
		}
		
		// Sort table
		Arrays.sort(table);
		
		char[] returnValue = new char[str.length()];
		for (int i=0; i<table.length; i++)
			returnValue[i] = table[i].charAt(str.length()-1);
		
		return new String(returnValue);
	}
	
	private String bwt_reverse(String rts, char stringEndChar){
		
		// Create table for BWT
		String[] table = new String[rts.length()];
		
		// Fill and sort table
		for (int i=0; i<rts.length(); i++){
			for (int j=0; j<rts.length(); j++)
				if (table[j] != null)
					table[j] = rts.charAt(j)+table[j];
				else
					table[j] = rts.charAt(j)+"";
			Arrays.sort(table);
		}
		
		// Determine table row that end with end char
		for (int i=0; i<rts.length(); i++){
			if (table[i].charAt(rts.length()-1) == stringEndChar)
				return table[i];
		}
		
		return null;
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		this.inputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_INPUT_REGEX,
				this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT_REGEX));
		this.outputdelimiter = StringUnescaper.unescape_perl_string(this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT,
				this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT)));
		String value = this.getProperties().getProperty(PROPERTYKEY_REVERSE,
				this.getPropertyDefaultValues().get(PROPERTYKEY_REVERSE));
		if (value != null && ! value.isEmpty())
			this.reverse = Boolean.parseBoolean(value);
		value = this.getProperties().getProperty(PROPERTYKEY_END_CHAR,
				this.getPropertyDefaultValues().get(PROPERTYKEY_END_CHAR));
		if (value != null && ! value.isEmpty())
			this.stringEndChar = value.charAt(0);

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
