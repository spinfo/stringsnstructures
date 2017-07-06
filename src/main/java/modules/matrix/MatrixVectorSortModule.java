package modules.matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import base.workbench.ModuleRunner;

public class MatrixVectorSortModule extends ModuleImpl {

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(MatrixVectorSortModule.class, args);
	}

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_DELIMITER_OUTPUT = "output delimiter";
	public static final String PROPERTYKEY_DELIMITER_INPUT = "input delimiter";
	public static final String PROPERTYKEY_OUTPUTFORMAT = "output format";
	public static final String PROPERTYKEY_REVERSEORDER = "reverse order";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "Type Matrix";
	private static final String ID_OUTPUT = "Type Matrix";

	// Local variables
	private String outputdelimiter;
	private String inputdelimiter;
	private String outputformat;
	private boolean reverseOrder;

	public MatrixVectorSortModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		setDescription("Sorts a type-type matrix.");

		// Add property descriptions (obligatory for every property!)
		getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT,
				"String to insert as CSV delimiter (only applicable to CSV output).");
		getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_INPUT, "Delimiter of input csv");
		getPropertyDescriptions().put(PROPERTYKEY_OUTPUTFORMAT, "Desired output format [csv|json].");
		getPropertyDescriptions().put(PROPERTYKEY_REVERSEORDER, "Sort reverse order");

		// Add property defaults (_should_ be provided for every property)
		getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, getClass().getSimpleName()); // Property

		// key for module name is defined in parent class
		getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT, ";");
		getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT, ";");
		getPropertyDefaultValues().put(PROPERTYKEY_OUTPUTFORMAT, "csv");
		getPropertyDefaultValues().put(PROPERTYKEY_REVERSEORDER, "false");

		// Define I/O
		InputPort inputPort = new InputPort(ID_INPUT, "CSV Type Matrix input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "CSV Type Matrix output.", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {

		// Map for result matrix
		Map<String, List<Double>> matrix = new HashMap<String, List<Double>>();

		PipedReader inputReader = getInputPorts().get(ID_INPUT).getInputReader();
		BufferedReader bufferedReader = new BufferedReader(inputReader);

		// Dismiss first line (header)
		bufferedReader.readLine();
		/*
		 * lines will be parallel processed. Each line contains a key and the values
		 * delimited by inputDelimiter. The values will be sorted either in natural
		 * order or reversed order. The result will be added to a matrix.
		 */
		bufferedReader.lines().parallel().filter(line -> !line.isEmpty() || !line.equals("")).forEach(line -> {
			String[] keyValues = line.split(inputdelimiter);
			String[] onlyValues = Arrays.copyOfRange(keyValues, 1, keyValues.length);
			List<Double> sortedValueList = Arrays.stream(onlyValues).map(value -> value.trim())
					.mapToDouble(Double::parseDouble).boxed().sorted(new Comparator<Double>() {
						@Override
						public int compare(Double d1, Double d2) {
							if (reverseOrder) {
								return d1.compareTo(d2);
							}
							return d2.compareTo(d1);
						}
					}).collect(Collectors.toList());
			matrix.put(keyValues[0], sortedValueList);
		});

		// JSON parser
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		if (outputformat.equals("csv")) {

			matrix.forEach((key, values) -> {
				try {
					getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(key + outputdelimiter);
					Iterator<Double> iter = values.iterator();
					while (iter.hasNext()) {
						Double d = iter.next();
						getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(d.toString());
						if (iter.hasNext()) {
							getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(outputdelimiter);
						}
					}
					getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");
				} catch (IOException e) {
					e.printStackTrace();
				}

			});
		} else if (outputformat.equals("json")) {
			getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(gson.toJson(matrix));
		} else
			throw new Exception("Specified output format is unknown.");

		// Close outputs (important!)
		closeAllOutputs();

		// Done
		return true;
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		outputdelimiter = getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT,
				getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT));
		inputdelimiter = getProperties().getProperty(PROPERTYKEY_DELIMITER_INPUT,
				getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT));
		outputformat = getProperties().getProperty(PROPERTYKEY_OUTPUTFORMAT,
				getPropertyDefaultValues().get(PROPERTYKEY_OUTPUTFORMAT));

		String value = getProperties().getProperty(PROPERTYKEY_REVERSEORDER,
				getPropertyDefaultValues().get(PROPERTYKEY_REVERSEORDER));
		if (value != null && !value.isEmpty())
			reverseOrder = Boolean.parseBoolean(value);

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
