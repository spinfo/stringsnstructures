package modules.word_2_vec;

import java.util.Properties;

import base.workbench.ModuleRunner;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.ModuleImpl;
import modules.OutputPort;

public class Word2VecGenerator extends ModuleImpl {
	
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(Word2VecGenerator.class, args);
	}

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_MODEL_NAME = "modelName";
	public static final String PROPERTYKEY_USE_CBOW = "use CBOW?";
	public static final String PROPERTYKEY_MIN_FREQ_WORDS = "minWordFreq";
	public static final String PROPERTYKEY_DIMENSIONS_N = "dimensions n";
	public static final String PROPERTYKEY_WINDOW_SIZE = "window size";
	public static final String PROPERTYKEY_NUMBER_OF_ITERATIONS = "number of iterations";
	public static final String PROPERTYKEY_INPUT_FILE_PATH = "inputFilePath";

	// Define I/O IDs (must be unique for every input or output)
	private final String outputId = "word2vec model";

	// Local variables
	private int windowSize;
	private String modelName;
	private boolean useCBOW;
	private int minWordFrequency;
	private int dimensions;
	private int numberOfIterations;
	private String inputFilePath;

	public Word2VecGenerator(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription(
				"Implementation of Word2Vec. Uses the deeplearning4J-Framework. This module reads an existing model, generates a new model and  serializes the result.");

		this.getPropertyDescriptions().put(PROPERTYKEY_MODEL_NAME,
				"Name of the model that should be generated. If the name already exists, the module loads this existing word2vec-model.");
		this.getPropertyDescriptions().put(PROPERTYKEY_USE_CBOW,
				"If true, uses CBOW to generate model. If false, uses SkipGram.");
		this.getPropertyDescriptions().put(PROPERTYKEY_MIN_FREQ_WORDS,
				"Words that occur less than this number, will be ignored.");
		this.getPropertyDescriptions().put(PROPERTYKEY_DIMENSIONS_N, "Dimensions of the Word2Vec-Model.");
		this.getPropertyDescriptions().put(PROPERTYKEY_WINDOW_SIZE, "Size of the sliding window (default: 5).");
		this.getPropertyDescriptions().put(PROPERTYKEY_NUMBER_OF_ITERATIONS, "Number of iterations in training.");
		this.getPropertyDescriptions().put(PROPERTYKEY_INPUT_FILE_PATH, "Path to the corpus");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Word2VecGenerator module");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MODEL_NAME, "model");
		this.getPropertyDefaultValues().put(PROPERTYKEY_USE_CBOW, "false");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MIN_FREQ_WORDS, "5");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DIMENSIONS_N, "300");
		this.getPropertyDefaultValues().put(PROPERTYKEY_WINDOW_SIZE, "5");
		this.getPropertyDefaultValues().put(PROPERTYKEY_WINDOW_SIZE, "5");
		this.getPropertyDefaultValues().put(PROPERTYKEY_NUMBER_OF_ITERATIONS, "5");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INPUT_FILE_PATH, "/data/corpus");

		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~). Every port
		 * can support a range of pipe types (currently byte or character
		 * pipes). Output ports can provide data to multiple pipe instances at
		 * once, input ports can in contrast only obtain data from one pipe
		 * instance.
		 */
		OutputPort outputPort = new OutputPort(outputId, "word2vec-model.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		// Add I/O ports to instance (don't forget...)
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {
		boolean success = true;
		try {
			Word2VecModelWrapper word2vecWrapper = new Word2VecModelWrapper(
					modelName, 
					useCBOW, 
					dimensions,
					minWordFrequency, 
					windowSize, 
					numberOfIterations, 
					inputFilePath);
			word2vecWrapper.serializeWord2VecModel();
		} catch (Exception exception) {
			success = false;
			throw exception;
		} finally {
			this.getOutputPorts().get(outputId).outputToAllCharPipes(modelName);
			this.closeAllOutputs();
		}

		return success;
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		this.modelName = this.getProperties().getProperty(PROPERTYKEY_MODEL_NAME);
		this.useCBOW = Boolean.getBoolean((String) this.getProperties().getOrDefault(PROPERTYKEY_USE_CBOW, "false"));
		this.minWordFrequency = Integer.parseInt((String) this.getProperties().getOrDefault(PROPERTYKEY_MIN_FREQ_WORDS, "5"));
		this.dimensions = Integer.parseInt((String) this.getProperties().getOrDefault(PROPERTYKEY_DIMENSIONS_N, "300"));
		this.windowSize = Integer.parseInt((String) this.getProperties().getOrDefault(PROPERTYKEY_WINDOW_SIZE, "5"));
		this.numberOfIterations = Integer.parseInt((String) this.getProperties().getOrDefault(PROPERTYKEY_NUMBER_OF_ITERATIONS, "5"));
		this.inputFilePath = (String) this.getProperties().getOrDefault(PROPERTYKEY_INPUT_FILE_PATH, "");

		super.applyProperties();
	}

}
