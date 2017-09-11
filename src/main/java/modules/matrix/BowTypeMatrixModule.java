package modules.matrix;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import common.TFIDFCalculator;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.NotSupportedException;
import modules.OutputPort;

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
	public static final String PROPERTYKEY_IDFLIMIT = "min IDF value";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "BoW";
	private static final String ID_OUTPUT = "Type Matrix";

	// Workbench parameter
	private String outputdelimiter;
	private String emptyFieldValue;
	private String outputformat;
	private boolean applyTfidf;
	private int idfLimit = 0;

	// Local variables
	private Map<String, ConcurrentHashMap<String, Double>> resultMatrix;
	private Map<Double, Map<String, Double>> inputSetenceBOWMap;
	private TFIDFCalculator tfidfCalculator;
	private Gson gsonParser;

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
		this.getPropertyDescriptions().put(PROPERTYKEY_IDFLIMIT,
				"Ignores all vectorfeatures with values smaller than the entered value. If no Filtering is wished, please enter 0. Is only used, when TFIDF is 'true'.");

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
		this.getPropertyDefaultValues().put(PROPERTYKEY_IDFLIMIT, "0");

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
	private void initLocalVariables() throws JsonSyntaxException, JsonIOException, NotSupportedException {
		resultMatrix = new ConcurrentHashMap<String, ConcurrentHashMap<String, Double>>();
		tfidfCalculator = new TFIDFCalculator();
		gsonParser = new GsonBuilder().setPrettyPrinting().create();
		inputSetenceBOWMap = new HashMap<Double, Map<String, Double>>();
		inputSetenceBOWMap = gsonParser.fromJson(this.getInputPorts().get(ID_INPUT).getInputReader(),
				inputSetenceBOWMap.getClass());
	}

	@Override
	public boolean process() throws Exception {
		initLocalVariables();
		generateResultMatrix();
		generateOutput();

		this.closeAllOutputs();
		return true;
	}

	private void generateResultMatrix() {
		inputSetenceBOWMap.entrySet().parallelStream().forEach((sentenceBowEntry) -> {
			sentenceBowEntry.getValue().forEach((token, freq) -> {
				Map<String, Double> neighbours = getWordNeighboursInSentence(sentenceBowEntry.getValue(), token);
				addToResultMatrix(neighbours, token);
				if (applyTfidf) {
					tfidfCalculator.addSentenceNeighboursToBase(neighbours, token);
				}
			});
		});
		if (applyTfidf) {
			tfidfCalculator.calculateTfidf(resultMatrix, idfLimit);
		}
	}

	private void addToResultMatrix(Map<String, Double> neighbours, String token) {
		ConcurrentHashMap<String, Double> matrixEntry = resultMatrix.getOrDefault(token, new ConcurrentHashMap<>());
		addNeighboursToEntry(neighbours, matrixEntry);
		resultMatrix.put(token, matrixEntry);
	}

	private void addNeighboursToEntry(Map<String, Double> neighbours, ConcurrentHashMap<String, Double> matrixEntry) {
		neighbours.forEach((word, freq) -> {
			Double totalFreqValue = matrixEntry.getOrDefault(word, 0d);
			totalFreqValue += freq;
			matrixEntry.put(word, totalFreqValue);
		});
	}

	private Map<String, Double> getWordNeighboursInSentence(Map<String, Double> sentence, String token) {
		HashMap<String, Double> neighbours = new HashMap<String, Double>();
		sentence.forEach((word, wordFrequency) -> {
			if (word.equals(token)) {
				// Don't take word itself as neighbour. Add only as neighbour if
				// word occurred
				// several times
				if (wordFrequency > 1) {
					wordFrequency--;
					neighbours.put(token, wordFrequency);
				}
			} else {
				neighbours.put(word, wordFrequency);
			}
		});
		return neighbours;
	}

	private void generateOutput() throws Exception {
		switch (outputformat) {
		case "csv":
			generateCSV();
			break;
		case "json":
			generateJSON();
			break;
		default:
			throw new Exception("Specified output format is unknown.");
		}
	}

	private void generateJSON() throws IOException {
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(gsonParser.toJson(resultMatrix));
	}

	private void generateCSV() throws IOException {
		// Output CSV header
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.outputdelimiter);
		Iterator<String> types = resultMatrix.keySet().iterator();
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
		types = resultMatrix.keySet().iterator();
		while (types.hasNext()) {
			String type = types.next();

			// rows should not end with delimiter
			// subsequent modules recognize that as new (empty) column

			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(type + this.outputdelimiter);

			Iterator<String> types2 = resultMatrix.keySet().iterator();
			while (types2.hasNext()) {
				String type2 = types2.next();
				if (resultMatrix.get(type).containsKey(type2))
					this.getOutputPorts().get(ID_OUTPUT)
							.outputToAllCharPipes(resultMatrix.get(type).get(type2).toString());
				else
					this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.emptyFieldValue);

				if (types2.hasNext()) {
					this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.outputdelimiter);
				}
			}
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");
		}

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
		String idfFilterValue = this.getProperties().getProperty(PROPERTYKEY_IDFLIMIT,
				this.getPropertyDefaultValues().get(PROPERTYKEY_IDFLIMIT));
		if(idfFilterValue != null && idfFilterValue.matches("\\d+"))
			this.idfLimit = Integer.parseInt(idfFilterValue);
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
