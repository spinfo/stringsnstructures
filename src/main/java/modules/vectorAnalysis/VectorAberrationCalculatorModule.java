package modules.vectorAnalysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import common.parallelization.CallbackReceiver;

public class VectorAberrationCalculatorModule extends ModuleImpl {

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_EXPONENT = "exponent";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "csv";
	private static final String ID_OUTPUT = "output";

	// Local variables
	private double exponent = 0.0d;

	public VectorAberrationCalculatorModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("Calculates aberration for elements within the input vectors, re-sorting them afterwards.");

		// Add module category
		this.setCategory("Experimental/WiP");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_EXPONENT, "Exponent for aberration amplification [double]; Aberration is taken times 2^E. Takes effect if value is above zero.");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Vector Aberration Calculator");
		this.getPropertyDefaultValues().put(PROPERTYKEY_EXPONENT, "0.0");

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
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "Output; JSON-encoded Map of Sets (Map<String,Set<Double>>).", this);
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
		
		// Skip csv head (we will sort the data lines individually anyway, so no sense in keeping track of the edge labels)
		if (inputScanner.hasNext()){
			inputScanner.next();
		} else {
			inputScanner.close();
			this.closeAllOutputs();
			throw new Exception("The CSV input is zero lines; aborting.");
		}
		
		// Map to store the sorted aberration sets for each type
		Map<String,Set<Double>> aberrationValuesMap = new HashMap<String,Set<Double>>();
		
		// Input read loop
		while (inputScanner.hasNext()) {
			// Determine next segment
			String dataLine = inputScanner.next();
			
			// Explode data line
			String[] data = dataLine.split(",");
			
			// Check whether the row has at least one true data field
			if (data.length<2){
				inputScanner.close();
				throw new Exception("I happened upon an empty data row -- rekon something is wrong here.");
			}
			
			// Determine type the current dataset belongs to (first field of row)
			String type = data[0];
			
			// Keep track of sum
			double sum = 0d;

			// Process the remaining fields of the current row
			TreeSet<Double> sortedValues = new TreeSet<Double>();
			for (int i=1; i<data.length; i++) {
				Double value = Double.parseDouble(data[i]);
				sum += value.doubleValue();
				sortedValues.add(value);
			}
			
			// Calculate average
			double average = sum/new Double(data.length).doubleValue();
			
			// Calculate aberration values
			TreeSet<Double> sortedAberrationValues = new TreeSet<Double>();
			Iterator<Double> valueIterator = sortedValues.iterator();
			while (valueIterator.hasNext()) {
				Double value = valueIterator.next();
				Double aberration = value - average;
				// Apply exponent if it is greater than one
				if (this.exponent > 0d)
					aberration = aberration * Math.pow(2d, this.exponent);
				// Store aberration value in set
				sortedAberrationValues.add(aberration);
			}
			
			// Store calculated aberration value set in map
			aberrationValuesMap.put(type, sortedAberrationValues);

		}
		
		// Close input scanner
		inputScanner.close();

		// Output distances
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(gson.toJson(aberrationValuesMap));
		
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

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
