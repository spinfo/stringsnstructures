package modules.vectorAnalysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import common.DoubleComparator;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class VectorMedianCalculatorModule extends ModuleImpl {

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "csv";
	private static final String ID_OUTPUT = "output";

	public VectorMedianCalculatorModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("Calculates median of elements within the input vectors.");

		// Add module category
		this.setCategory("Vectorization");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Vector Median Calculator");

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
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "Output; JSON-encoded Map (Map&lt;String,Double&gt;).", this);
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
		
		// Skip csv head (we will sort the data lines individually anyway, so no sense in keeping track of the edge labels)
		if (inputScanner.hasNext()){
			inputScanner.next();
		} else {
			inputScanner.close();
			this.closeAllOutputs();
			throw new Exception("The CSV input is zero lines; aborting.");
		}
		
		// Map to store the median for each type
		Map<String,Double> medianValuesMap = new HashMap<String,Double>();
		
		// Construct comparator for later use
		Comparator<Double> comparator = new DoubleComparator();
		
		// Input read loop
		while (inputScanner.hasNext()) {
			// Determine next segment
			String dataLine = inputScanner.next();
			
			// Explode data line
			String[] data = dataLine.split("[,;]");
			
			// Check whether the row has at least one true data field
			if (data.length<2){
				inputScanner.close();
				throw new Exception("I happened upon an empty data row -- rekon something is wrong here.");
			}
			
			// Determine type the current dataset belongs to (first field of row)
			String type = data[0];

			// Process the remaining fields of the current row
			List<Double> sortedValues = new ArrayList<Double>();
			for (int i=1; i<data.length; i++) {
				Double value = Double.parseDouble(data[i]);
				sortedValues.add(value);
			}
			sortedValues.sort(comparator);
			
			// Variable to store median in
			Double median;
			
			// Check for special cases
			if (sortedValues.size() == 0){
				// Empty data row, cannot calculate median
				median = Double.NaN;
			}
			
			else if (sortedValues.size() == 1){
				// Only a single data value is present
				median = sortedValues.get(0);
			}
			
			// Check whether the number of values is odd or even
			else if (sortedValues.size()%2==0){
				Double[] sortedValuesArray = sortedValues.toArray(new Double[data.length-1]);
				Double upperMedian = new Double(sortedValuesArray[(sortedValuesArray.length/2)]);
				Double lowerMedian = new Double(sortedValuesArray[(sortedValuesArray.length/2)-1]);
				median = new Double((lowerMedian+upperMedian)/2);
			} else {
				Double[] sortedValuesArray = sortedValues.toArray(new Double[data.length-1]);
				median = new Double(sortedValuesArray[(sortedValues.size()-1)/2]);
			}
			
			// Store calculated median value in map
			medianValuesMap.put(type, median);

		}
		
		// Close input scanner
		inputScanner.close();

		// Output median map
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(gson.toJson(medianValuesMap));
		
		// Close outputs (important!)
		this.closeAllOutputs();

		// Done
		return true;
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
