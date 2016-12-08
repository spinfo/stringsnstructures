package modules.matrix;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import base.workbench.ModuleWorkbenchController;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class CosineDistanceModule extends ModuleImpl {

	// Identifiers for the term to find contexts of
	public static final String PROPERTYKEY_TERMS = "terms";
	public static final String PROPERTYKEY_DELIMITER = "CSV delimiter";

	// Identifiers for inputs and outputs
	public static final String INPUT_ID = "input vectors csv";
	public static final String OUTPUT_ID = "output distances";
	
	private String terms;
	private String delimiter;

	public CosineDistanceModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// the module's name, description and category
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Cosine Distance Module");
		this.setDescription("Calculates the cosine distance between vectors and outputs a CSV distance matrix.");

		// description and default value for the regular expression
		this.getPropertyDescriptions().put(PROPERTYKEY_TERMS, "comma separated list of terms to calculate distances of (empty = every term)");
		this.getPropertyDefaultValues().put(PROPERTYKEY_TERMS, "");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER, "CSV delimiter");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER, ";");

		// setup I/O
		InputPort input = new InputPort(INPUT_ID, "[CSV representing Map<String,Map<String, Double>>] Input vectors CSV.", this);
		OutputPort output = new OutputPort(OUTPUT_ID, "[CSV] Matrix of vector distances.",
				this);
		
		input.addSupportedPipe(CharPipe.class);
		output.addSupportedPipe(CharPipe.class);

		this.addInputPort(input);
		this.addOutputPort(output);
	}

	@Override
	public boolean process() throws Exception {
		boolean result = true;

		// Input will be read line by line
		BufferedReader reader = null;
		String line = null;

		// Possible output ports
		OutputPort out = this.getOutputPorts().get(OUTPUT_ID);
		
		boolean processedHeader = false;
		String header = null;
		//Map<String, Map<String, Double>> distances = new HashMap<String, Map<String, Double>>();
		Map<String, Double[]> vectors = new TreeMap<String, Double[]>();
		Set<String> selection = null;

		try {
			reader = new BufferedReader(super.getInputPorts().get(INPUT_ID).getInputReader());

			//read vectors
			while ((line = reader.readLine()) != null) {
				
				if (!processedHeader){
					header = line;
					if (terms.length() != 0){
						header = delimiter + terms.replaceAll("\\,", delimiter) + delimiter;
						selection = new TreeSet<String>(Arrays.asList(terms.split(",")));
					} else {
						selection = new TreeSet<String>(Arrays.asList(header.split(delimiter)));
					}
					processedHeader = true;
					System.out.println(selection);
					continue;
				}
				
				String[] split = line.split(delimiter);
				String currTerm = split[0];
				split = Arrays.copyOfRange(split, 1, split.length-1);
				Double[] currVector = new Double[split.length];
				
				for (int i = 0; i < split.length; i++) {
					currVector[i] = Double.valueOf(split[i]);
				}
				
				vectors.put(currTerm, currVector);
			}
			
			selection.retainAll(vectors.keySet());
			
			////calculate distances and build matrix 
			//prepare header
			StringBuilder sb = new StringBuilder();
			sb.append(delimiter);
			for (String s : selection) sb.append(s + delimiter);
			out.outputToAllCharPipes(sb.toString() + ModuleWorkbenchController.LINEBREAK);
			sb = new StringBuilder();
			
			//calculate
			for (Entry<String, Double[]> e : vectors.entrySet()){
				sb.append(e.getKey());
				sb.append(delimiter);
				for (String s : selection){
					sb.append(distance(e.getValue(), vectors.get(s)));
					sb.append(delimiter);
				}
				out.outputToAllCharPipes(sb.toString() + ModuleWorkbenchController.LINEBREAK);
				sb = new StringBuilder();
			}

		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
			this.closeAllOutputs();
		}

		return result;
	}
	
	
	private double distance(Double[] vector1, Double[] vector2) {
        double sum = 0.0;
        for(int i = 0; i < vector1.length; i++)
           sum = sum + Math.pow((vector1[i] - vector2[i]), 2.0);
        return Math.sqrt(sum);
    }
	

	@Override
	public void applyProperties() throws Exception {
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		this.terms = this.getProperties().getProperty(PROPERTYKEY_TERMS,
				this.getPropertyDefaultValues().get(PROPERTYKEY_TERMS));
		
		this.delimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER,
				this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER));
		
		super.applyProperties();
	}
	
}
