package modules.matrix;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.bag_of_words.BagOfWordsHelper;

import base.workbench.ModuleRunner;

public class BowTypeMatrixModule extends ModuleImpl {

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(BowTypeMatrixModule.class, args);
	}

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_DELIMITER_OUTPUT = "output delimiter";
	public static final String PROPERTYKEY_ZEROVALUE = "empty value";
	public static final String PROPERTYKEY_OUTPUTFORMAT = "output format";
	public static final String PROPERTYKEY_APPLYTFIDF = "apply TF-iDF";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "BoW";
	private static final String ID_OUTPUT = "Type Matrix";

	// Local variables
	private String outputdelimiter;
	private String emptyFieldValue;
	private String outputformat;
	private boolean applyTfidf;

	public BowTypeMatrixModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("Creates a type-type matrix from Bag of Words data.");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT,
				"String to insert as CSV delimiter (only applicable to CSV output).");
		this.getPropertyDescriptions().put(PROPERTYKEY_ZEROVALUE,
				"String to insert as empty value into the output (only applicable to CSV output).");
		this.getPropertyDescriptions().put(PROPERTYKEY_OUTPUTFORMAT, "Desired output format [csv|json].");
		this.getPropertyDescriptions().put(PROPERTYKEY_APPLYTFIDF,
				"Multiply the token values with their <i>Inverse Document Frequencies</i> before calculating the type sum [true|false].");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "BoW Type Matrix"); // Property
																								// key
																								// for
																								// module
																								// name
																								// is
																								// defined
																								// in
																								// parent
																								// class
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ZEROVALUE, "0");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTPUTFORMAT, "csv");
		this.getPropertyDefaultValues().put(PROPERTYKEY_APPLYTFIDF, "false");

		// Define I/O
		InputPort inputPort = new InputPort(ID_INPUT, "JSON BoW data input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "CSV Type Matrix output.", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process() throws Exception {

		// Map for result matrix
		Map<String, Map<String, Double>> matrix = new TreeMap<String, Map<String, Double>>();

		// JSON parser
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// Read and parse input
		Map<Double, Map<String, Double>> bowMap = new HashMap<Double, Map<String, Double>>();
		bowMap = gson.fromJson(this.getInputPorts().get(ID_INPUT).getInputReader(), bowMap.getClass());

		// Prepare iDF map
		Map<String, Double> inverseDocumentFrequencies = null;

		// Calculate inverse document frequencies if TF-iDF is to be applied
		if (this.applyTfidf) {

			Map<String, Double> termFrequencies = new TreeMap<String, Double>();

			// Iterate over input BoW's
			Iterator<Map<String, Double>> bows = bowMap.values().iterator();
			while (bows.hasNext()) {
				BagOfWordsHelper.mergeDouble(termFrequencies, bows.next());
			}

			// Calculate inverse Document frequencies
			inverseDocumentFrequencies = BagOfWordsHelper.inverseDocumentFrequenciesDouble(termFrequencies,
					bowMap.size());
		}

		// Iterate over input BoW's
		Iterator<Map<String, Double>> bows = bowMap.values().iterator();
		while (bows.hasNext()) {
			Map<String, Double> bow = bows.next();

			// Iterate over tokens within current BoW
			Iterator<String> tokens = bow.keySet().iterator();
			while (tokens.hasNext()) {

				// Determine next token
				String token = tokens.next();

				// Determine existing matrix line
				Map<String, Double> matrixLine = matrix.getOrDefault(token, new TreeMap<String, Double>());

				// Iterate over tokens a second time
				Iterator<String> tokens2 = bow.keySet().iterator();
				while (tokens2.hasNext()) {
					// Determine next token
					String token2 = tokens2.next();
					// ... and its value

					Double value = bow.get(token2);

					if (token.equals(token2) && value == 0) {
						matrixLine.put(token2, value);
						continue;
					}

					if (token.equals(token2) && value == 1) {
						// Apply TF-iDF
						if (this.applyTfidf) {
							value = inverseDocumentFrequencies.get(token2);
						} else {
							value = matrixLine.getOrDefault(token2, 0d);
						}
		
						// Add value to matrix line
						matrixLine.put(token2, value);				
						continue;
					}
					
					if (token.equals(token2) && value > 1) {
						value--;
					}

					// Add value to existing one
					value += matrixLine.getOrDefault(token2, 0d);

					// Apply TF-iDF
					if (this.applyTfidf)
						value = value * inverseDocumentFrequencies.get(token2);

					// Add value to matrix line
					matrixLine.put(token2, value);
				}
				// Add line to matrix
				matrix.put(token, matrixLine);
			}
		}

		if (this.outputformat.equals("csv")) {
			// Output CSV header
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.outputdelimiter);
			Iterator<String> types = matrix.keySet().iterator();
			while (types.hasNext()) {
				String nextType = types.next();

				// rows should not end with delimiter
				// subsequent modules recognize that as new (empty) column
				if (types.hasNext()) {
					this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(nextType + this.outputdelimiter);
				} else {
					this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(nextType);
				}
			}
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");

			// Output matrix
			types = matrix.keySet().iterator();
			while (types.hasNext()) {
				String type = types.next();

				// rows should not end with delimiter
				// subsequent modules recognize that as new (empty) column
				if (types.hasNext()) {
					this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(type + this.outputdelimiter);
				} else {
					this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(type);
				}
				Iterator<String> types2 = matrix.keySet().iterator();
				while (types2.hasNext()) {
					String type2 = types2.next();
					if (matrix.get(type).containsKey(type2))
						this.getOutputPorts().get(ID_OUTPUT)
								.outputToAllCharPipes(matrix.get(type).get(type2).toString());
					else
						this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.emptyFieldValue);

					if (types2.hasNext()) {
						this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.outputdelimiter);
					}
				}
				this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");
			}
		} else if (this.outputformat.equals("json")) {
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(gson.toJson(matrix));
		} else
			throw new Exception("Specified output format is unknown.");

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
		this.outputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT,
				this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT));
		this.emptyFieldValue = this.getProperties().getProperty(PROPERTYKEY_ZEROVALUE,
				this.getPropertyDefaultValues().get(PROPERTYKEY_ZEROVALUE));
		this.outputformat = this.getProperties().getProperty(PROPERTYKEY_OUTPUTFORMAT,
				this.getPropertyDefaultValues().get(PROPERTYKEY_OUTPUTFORMAT));

		String value = this.getProperties().getProperty(PROPERTYKEY_APPLYTFIDF,
				this.getPropertyDefaultValues().get(PROPERTYKEY_APPLYTFIDF));
		if (value != null && !value.isEmpty())
			this.applyTfidf = Boolean.parseBoolean(value);

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
