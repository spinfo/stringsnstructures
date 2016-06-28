package modules.vectorization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import common.DoubleComparator;
import common.StringUnescaper;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class VectorAberrationCalculatorModule extends ModuleImpl {

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_EXPONENT = "exponent";
	public static final String PROPERTYKEY_SORT = "sort output";
	public static final String PROPERTYKEY_IN_CSV_DELIMITER_REGEX = "input CSV delimiter regex";
	public static final String PROPERTYKEY_OUT_CSV_DELIMITER = "output CSV delimiter";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "csv";
	private static final String ID_OUTPUT = "output";

	// Local variables
	private double exponent = 0.0d;
	private boolean sort = true;
	private String inputCsvDelimiterRegex;
	private String outputCsvDelimiter;

	public VectorAberrationCalculatorModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("Calculates aberration for elements within the input vectors, optionally re-sorting them afterwards.");

		// Add module category


		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_EXPONENT, "Exponent for aberration amplification [double]; Aberration is taken times 2^E. Takes effect if value is above zero.");
		this.getPropertyDescriptions().put(PROPERTYKEY_SORT, "Sort output values (low to high). If true, the CSV header line will be omitted on output. [true/false]");
		this.getPropertyDescriptions().put(PROPERTYKEY_IN_CSV_DELIMITER_REGEX, "Input CSV delimiter regex.");
		this.getPropertyDescriptions().put(PROPERTYKEY_OUT_CSV_DELIMITER, "Output CSV delimiter (escaped chars will be unescaped).");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Vector Aberration Calculator");
		this.getPropertyDefaultValues().put(PROPERTYKEY_EXPONENT, "0.0");
		this.getPropertyDefaultValues().put(PROPERTYKEY_SORT, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_IN_CSV_DELIMITER_REGEX, "[\\,;]");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUT_CSV_DELIMITER, ",");

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
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "Output; JSON-encoded Map of Sets (Map&lt;String,Set&lt;Double&gt;&gt;).", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {

		// Construct scanner instances for input segmentation
		Scanner inputScanner = new Scanner(this.getInputPorts().get(ID_INPUT).getInputReader());
		inputScanner.useDelimiter("\\R+");
		
		// Read csv header line (except when we will sort the output anyway)
		if (!this.sort) {
			String[] head = null;
			if (inputScanner.hasNext()) {
				head = inputScanner.next().split(this.inputCsvDelimiterRegex);
			} else {
				inputScanner.close();
				this.closeAllOutputs();
				throw new Exception("The CSV input is zero lines; aborting.");
			}

			// Output CSV head
			for (int i = 0; i < head.length; i++) {
				this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(head[i] + this.outputCsvDelimiter);
			}
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");
		} else if (inputScanner.hasNext()) {
			// Silently drop the header line
			inputScanner.next();
		} else {
			inputScanner.close();
			this.closeAllOutputs();
			throw new Exception("The CSV input is zero lines; aborting.");
		}
			
		
		// Construct comparator for later use
		Comparator<Double> comparator = new DoubleComparator();
		
		// Input read loop
		while (inputScanner.hasNext()) {
			// Determine next segment
			String dataLine = inputScanner.next();
			
			// Explode data line
			String[] data = dataLine.split(this.inputCsvDelimiterRegex);
			
			// Check whether the row has at least one true data field
			if (data.length<2){
				inputScanner.close();
				throw new Exception("I happened upon an empty data row -- rekon something is wrong here.");
			}
			
			// Determine type the current dataset belongs to (first field of row)
			String type = data[0];
			
			// Output type
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(type+this.outputCsvDelimiter);
			
			// Keep track of sum
			double sum = 0d;

			// Array for parsed double values
			double[] dataDouble = new double[data.length-1];
			
			// Process the remaining fields of the current row
			for (int i=1; i<data.length; i++) {
				double value = Double.parseDouble(data[i]);
				dataDouble[i-1] = value;
				sum += value;
			}
			
			// Sort values if option is selected
			if (this.sort){
				List<Double> sortedList = new ArrayList<Double>();
				for (int i=1; i<data.length; i++) {
					sortedList.add(Double.parseDouble(data[i]));
				}
				sortedList.sort(comparator);
				for (int i=0; i<dataDouble.length; i++){
					dataDouble[i] = sortedList.get(i);
				}
			}
			
			// Calculate average (data-length-1 to exclude the label field from average calculation)
			double average = sum/new Double(dataDouble.length).doubleValue();
			
			// Calculate aberration values
			for (int i=0; i<dataDouble.length; i++) {
				double value = dataDouble[i];
				double aberration = value - average;
				// Apply exponent if it is greater than one
				if (this.exponent > 0d)
					aberration = aberration * Math.pow(2d, this.exponent);
				// Output aberration value
				this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(aberration+this.outputCsvDelimiter);
			}
			
			// End of line
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");

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
			this.exponent = Double.parseDouble(exponentString);

		String sortString = this.getProperties().getProperty(PROPERTYKEY_SORT,
				this.getPropertyDefaultValues().get(PROPERTYKEY_SORT));
		if (sortString != null && !sortString.isEmpty())
			this.sort = Boolean.parseBoolean(sortString);
		
		this.inputCsvDelimiterRegex = this.getProperties().getProperty(PROPERTYKEY_IN_CSV_DELIMITER_REGEX,
					this.getPropertyDefaultValues().get(PROPERTYKEY_IN_CSV_DELIMITER_REGEX));
		
		String value = this.getProperties().getProperty(PROPERTYKEY_OUT_CSV_DELIMITER,
				this.getPropertyDefaultValues().get(PROPERTYKEY_OUT_CSV_DELIMITER));
		if (value != null)
			this.outputCsvDelimiter = StringUnescaper.unescape_perl_string(value);

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
