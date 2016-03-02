package modules.vectorAnalysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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

public class VectorAnalysisModule extends ModuleImpl {

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_EXPONENT = "exponent";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "csv";
	private static final String ID_OUTPUT = "output";

	// Local variables
	private double exponent = 0.0d;

	public VectorAnalysisModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("<p>Analyses and re-sorts vectors.</p>");

		// Add module category
		this.setCategory("Experimental/WiP");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_EXPONENT, "Exponent for aberration amplification [double]; Aberration is taken times 2^E. Takes effect if value is above zero.");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Vector Analysis Module");
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

		// Minkowski Distance matrix
		Map<String,Map<String,Double>> minkowskiDistanceMatrix = new HashMap<String,Map<String,Double>>();
		
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
		
		/*
		 * Iterate through map, removing the current item from it and comparing
		 * it to the remainder (to avoid comparing a pair twice [A-B and B-A] or
		 * an element to itself).
		 */
		Iterator<Entry<String, Set<Double>>> types = aberrationValuesMap.entrySet().iterator();
		while(types.hasNext()){
			
			// Remove entry
			Entry<String, Set<Double>> entry = types.next();
			types.remove();
			
			// Create result map for current entry
			Map<String,Double> distanceMap = new HashMap<String,Double>();
			
			// Add result map to result matrix
			minkowskiDistanceMatrix.put(entry.getKey(), distanceMap);
			
			// Second level iteration to compare the current entry with the rest
			Iterator<Entry<String, Set<Double>>> remainingTypes = aberrationValuesMap.entrySet().iterator();
			while(remainingTypes.hasNext()){
				
				Entry<String, Set<Double>> comparisonEntry = remainingTypes.next();
				
				// Calculate distance
				double distance = this.calculateMinkowskiDistance(entry.getValue(), comparisonEntry.getValue());
				
				// Store result
				distanceMap.put(comparisonEntry.getKey(), distance);
				
			}
			
		}
		
		// Close input scanner
		inputScanner.close();

		// Output matrix (TODO preliminary; JSON in lieu of a more suitable format)
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(gson.toJson(minkowskiDistanceMatrix));
		
		// Close outputs (important!)
		this.closeAllOutputs();

		// Done
		return true;
	}
	
	/**
	 * Calculates the Minkowski-Distance of two n-dimensional vectors.
	 * @See MERKL, Rainer 2015, Bioinformatik, p.159
	 * @See https://en.wikipedia.org/wiki/Minkowski_distance
	 * @param vectorA First vector
	 * @param vectorB Second vector
	 * @return Minkowski-Distance
	 * @throws Exception Thrown if vectors are null or of different length
	 */
	private double calculateMinkowskiDistance(Set<Double> vectorA, Set<Double> vectorB) throws Exception{
		
		// Check input
		if (vectorA==null || vectorB==null || vectorA.size()!=vectorB.size()){
			throw new Exception("Sets must both be non-null and equal in length.");
		}
		
		// Prepare result variable
		double result = 0d;
		
		// Compute distance
		Iterator<Double> aIterator = vectorA.iterator();
		Iterator<Double> bIterator = vectorB.iterator();
		while(aIterator.hasNext() && bIterator.hasNext()){
			result += Math.pow(Math.abs(aIterator.next()-bIterator.next()),2d);
		}
		result = Math.sqrt(result);
		
		return result;
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
