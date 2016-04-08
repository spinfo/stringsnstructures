package modules.basemodules;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class ComparisonModule extends ModuleImpl {

	private static final String MODULE_NAME = "Comparison Module";
	private static final String MODULE_DESCRIPTION = "Module reads from two input ports and outputs "
			+ "1) lines only in the first input, "
			+ "2) lines only in the second input, "
			+ "3) lines in both inputs.";

	// variables for input and their identifiers
	private final InputPort input1;
	private final InputPort input2;
	private static final String INPUT_1_ID = "input 1";
	private static final String INPUT_2_ID = "input 2";
	
	// variables for output and their identifiers
	private final OutputPort outputOnlyIn1;
	private final OutputPort outputOnlyIn2;
	private final OutputPort outputBoth;
	private static final String OUTPUT_ONLY_1 = "in 1st only";
	private static final String OUTPUT_ONLY_2 = "in 2nd only";
	private static final String OUTPUT_BOTH = "in both";
	
	// the separator to split all inputs on and a key to identify the property
	// by which the regexp for the separator may be set
	// the default for the separator are newlines
	private Pattern separator;
	private static final String PROPERTYKEY_SEPARATOR = "Separator";
	private static final String PROPERTY_DEFAULT_SEPARATOR = "\r\n|\n|\r";
	
	public ComparisonModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module category
		this.setCategory("Basic text processing");
		
		// Set the modules name and description
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, MODULE_NAME);
		this.setDescription(MODULE_DESCRIPTION);
		
		// setup the property for the separator, defaults to to newlines
		this.getPropertyDefaultValues().put(PROPERTYKEY_SEPARATOR, PROPERTY_DEFAULT_SEPARATOR);
		this.getPropertyDescriptions().put(PROPERTYKEY_SEPARATOR, "A separator to split input on. Defaults to newlines.");
		
		// setup inputs
		this.input1 = new InputPort(INPUT_1_ID, "the first input", this);
		this.input2 = new InputPort(INPUT_2_ID, "the second input", this);
		
		for(InputPort input : Arrays.asList(input1, input2)) {
			input.addSupportedPipe(CharPipe.class);
			super.addInputPort(input);
		}
		
		// setup outputs
		this.outputOnlyIn1 = new OutputPort(OUTPUT_ONLY_1, "Lines exclusive to input 1", this);
		this.outputOnlyIn2 = new OutputPort(OUTPUT_ONLY_2, "Lines exclusive to input 2", this);
		this.outputBoth = new OutputPort(OUTPUT_BOTH, "Lines occurring in both inputs", this);
		
		for(OutputPort output : Arrays.asList(outputOnlyIn1, outputOnlyIn2, outputBoth)) {
			output.addSupportedPipe(CharPipe.class);
			super.addOutputPort(output);
		}
	}

	@Override
	public boolean process() throws Exception {
		
		// split input into tokens
		final String[] tokens1 = separator.split(super.readStringFromInputPort(input1));
		final String[] tokens2 = separator.split(super.readStringFromInputPort(input2));
		
		// put tokens into sets
		final Set<String> set1 = new TreeSet<String>(Arrays.asList(tokens1));
		final Set<String> set2 = new TreeSet<String>(Arrays.asList(tokens2));
		
		// a set for the output
		Set<String> outSet = null;
		
		// output tokens exclusive to input 1
		if (outputOnlyIn1.isConnected()) {
			outSet = new TreeSet<String>();

			outSet.addAll(set1);
			outSet.removeAll(set2);

			writeSet(outSet, outputOnlyIn1);
		}

		// output tokens exclusive to input 2
		if (outputOnlyIn2.isConnected()) {
			outSet = new TreeSet<String>();

			outSet.addAll(set2);
			outSet.removeAll(set1);

			writeSet(outSet, outputOnlyIn2);
		}
		
		// output tokens in both inputs
		if (outputBoth.isConnected()) {
			outSet = new TreeSet<String>();
			
			outSet.addAll(set1);
			outSet.retainAll(set2);
			
			writeSet(outSet, outputBoth);
		}
		
		// finishing up
		this.closeAllOutputs();
		return true;
	}
	
	// write all Strings in set to the output port and close it afterwards
	private void writeSet(Set<String> set, OutputPort out) throws IOException {
		for(String elem : set) {
			out.outputToAllCharPipes(elem + System.lineSeparator());
		}
		out.close();
	}
	
	@Override
	public void applyProperties() throws Exception {
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Compile the provided separator regex into a Pattern
		if (this.getProperties().getProperty(PROPERTYKEY_SEPARATOR) != null) {
			this.separator = Pattern.compile(this.getProperties().getProperty(PROPERTYKEY_SEPARATOR));
		}

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
