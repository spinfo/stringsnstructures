package modules.vectorAnalysis;

import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import common.parallelization.CallbackReceiver;

public class VectorAnalysisModule extends ModuleImpl {

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_EXPONENT = "exponent";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "csv";
	private static final String ID_OUTPUT = "output";

	// Local variables
	private int exponent = 1;

	public VectorAnalysisModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("<p>Analyses and re-sorts vectors.</p>");

		// Add module category
		this.setCategory("Experimental/WiP");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_EXPONENT, "Exponent for contrast amplification");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Vector Analysis Module");
		this.getPropertyDefaultValues().put(PROPERTYKEY_EXPONENT, "1");

		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~). Every port
		 * can support a range of pipe types (currently byte or character
		 * pipes). Output ports can provide data to multiple pipe instances at
		 * once, input ports can in contrast only obtain data from one pipe
		 * instance.
		 */
		InputPort inputPort = new InputPort(ID_INPUT, "Vector input; expects comma separated values.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "Output.", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {

		// Construct scanner instances for input segmentation
		Scanner inputScanner = new Scanner(this.getInputPorts().get(ID_INPUT).getInputReader());
		inputScanner.useDelimiter("\\n");

		// Read csv head (first line)
		if (inputScanner.hasNext()){
			String headerLine = inputScanner.next();
			StringTokenizer tokenizer = new StringTokenizer(headerLine,",");
			while (tokenizer.hasMoreTokens()) {
				// TODO process types/headers
			}
		} else {
			inputScanner.close();
			this.closeAllOutputs();
			throw new Exception("There does not seem to be any input data; I cannot continue.");
		}
		
		// Input read loop
		while (inputScanner.hasNext()) {
			// Determine next segment
			String dataLine = inputScanner.next();
			
			StringTokenizer tokenizer = new StringTokenizer(dataLine,",");
			while (tokenizer.hasMoreTokens()) {
				// TODO process data
			}
			
			// Write to outputs

		}

		// Close input scanner
		inputScanner.close();

		// Close outputs (important!)
		this.closeAllOutputs();

		// Done
		return true;
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		String exponentString = this.getProperties().getProperty(PROPERTYKEY_EXPONENT,
				this.getPropertyDefaultValues().get(PROPERTYKEY_EXPONENT));
		if (exponentString != null && !exponentString.isEmpty())
			this.exponent = Integer.parseInt(exponentString);

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
