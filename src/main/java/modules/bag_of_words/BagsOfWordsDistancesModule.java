package modules.bag_of_words;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

/**
 * Module to determine the distance between Bags of Words.
 * 
 * Currently supports Levenshtein distance, i.e. output is the number of
 * substitutions, deletions or additions of words that would be needed to
 * transform one Bag of Words into another.
 */
public class BagsOfWordsDistancesModule extends ModuleImpl {

	private static final Logger LOGGER = Logger.getLogger(BagsOfWordsDistancesModule.class.getName());

	// Strings identifying/describing in- and output pipes
	private final static String INPUT_ID = "json";
	private final static String OUTPUT_ID = "json";
	private final static String INPUT_DESC = "[text/json] TreeMap<Integer,TreeMap<String,Integer>>";
	private final static String OUTPUT_DESC = "[text/json] TreeMap<Integer,TreeMap<Integer,Float>>";

	// Types for deserializing the input and serializing output
	private final static Type INPUT_TYPE = new TypeToken<TreeMap<Integer, TreeMap<String, Integer>>>() {
	}.getType();
	private final static Type OUTPUT_TYPE = new TypeToken<TreeMap<Integer, TreeMap<Integer, Float>>>() {
	}.getType();

	// Name and description of this module for the User
	private final static String MODULE_NAME = "BagsOfWordsDistancesModule";
	private final static String MODULE_DESCRIPTION = "<p>Module to determine the distance between Bags of Words.</p>"
			+ "<p>Currently supports Levenshtein distance, i.e. output is the number of "
			+ "substitutions, deletions or additions of words that would be needed to "
			+ "transform one Bag of Words into another.<p>";

	// Property describing whether the distance should be normalized
	private final static String PROPERTYKEY_NORMALIZE_DISTANCE = "Normalize distance";
	private final static String DESCRIPTION_NORMALIZE_DISTANCE = "Divides each distance by it's possible maximum for the two BoWs.";
	private final static String DEFAUL_NORMALIZE_DISTANCE = "false";
	private boolean normalizeDistance;

	// Property describing if words with a certain tf-idf value should be
	// disregard
	private final static String PROPERTYKEY_TFIDF_MIN = "Filter on TF-IDF min";
	private final static String DESCRIPTION_TFIDF_MIN = "When computing distances disregards words with tf-idf below this value. Disabled on \"0.0\"";
	private final static String DEFAULT_TFIDF_MIN = "0.8";
	private Float tfIdfMin;

	public BagsOfWordsDistancesModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		// Call parent constructor
		super(callbackReceiver, properties);

		// Set the modules name and description
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, MODULE_NAME);
		this.setDescription(MODULE_DESCRIPTION);

		// Setup I/O, reads from and writes to CharPipe
		InputPort inputPort = new InputPort(INPUT_ID, INPUT_DESC, this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUT_ID, OUTPUT_DESC, this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

		// Setup properties
		this.getPropertyDescriptions().put(PROPERTYKEY_NORMALIZE_DISTANCE, DESCRIPTION_NORMALIZE_DISTANCE);
		this.getPropertyDefaultValues().put(PROPERTYKEY_NORMALIZE_DISTANCE, DEFAUL_NORMALIZE_DISTANCE);
		this.getPropertyDescriptions().put(PROPERTYKEY_TFIDF_MIN, DESCRIPTION_TFIDF_MIN);
		this.getPropertyDefaultValues().put(PROPERTYKEY_TFIDF_MIN, DEFAULT_TFIDF_MIN);
	}

	@Override
	public boolean process() throws Exception {
		boolean result = true;

		// read the whole text once
		final String input = this.readStringFromInputPort(this.getInputPorts().get(INPUT_ID));

		// the output: an empty result map mapping sentence Nrs to a map holding
		// the distance of this sentence to each other sentence
		final Map<Integer, Map<Integer, Float>> sentenceNrsToDistances = new TreeMap<Integer, Map<Integer, Float>>();

		try {
			// deserialize the input
			final Gson gson = new GsonBuilder().setPrettyPrinting().create();
			final TreeMap<Integer, Map<String, Integer>> sentenceNrsToBagOfWords = gson.fromJson(input, INPUT_TYPE);
			final Set<Integer> sentenceNrs = sentenceNrsToBagOfWords.keySet();

			// make sure that the bags of words are ready to use
			if (sentenceNrs.size() == 0) {
				throw new Exception("No bags of words given");
			}

			// if tf-idf filtering is wished for, compute inverse document
			// frequencies for each term
			Map<String, Double> inverseDocumentFrequencies = null;
			if (this.tfIdfMin > 0.0) {
				// get all terms frequencies by merging all bags of words into
				// one bag
				final Map<String, Integer> termFrequencies = new TreeMap<String, Integer>();
				for (Integer sentenceNr : sentenceNrs) {
					Map<String, Integer> bag = sentenceNrsToBagOfWords.get(sentenceNr);
					BagOfWordsHelper.merge(termFrequencies, bag);
				}
				// from term frequencies compute idf-values, taking the amount
				// of documents to be the amount of sentences
				inverseDocumentFrequencies = BagOfWordsHelper.inverseDocumentFrequencies(termFrequencies,
						sentenceNrs.size());
				// replace all bags of words with copies filtered by the minimum
				// TF-IDF value
				Map<String, Integer> bow = null;
				for (int sentenceNr : sentenceNrs) {
					bow = sentenceNrsToBagOfWords.get(sentenceNr);
					bow = BagOfWordsHelper.tfIdfMinFilter(bow, inverseDocumentFrequencies, tfIdfMin);
					sentenceNrsToBagOfWords.put(sentenceNr, bow);
				}
			}

			// bags that are compared
			// word => count in sentence
			Map<String, Integer> thisBag;
			Map<String, Integer> otherBag;

			// distances that are produced by comparison
			// sentenceNr => distance
			Map<Integer, Float> thisDistances;
			Map<Integer, Float> otherDistances;

			// first loop over sentences to compare
			for (Integer thisSentence : sentenceNrs) {
				thisDistances = sentenceNrsToDistances.getOrDefault(thisSentence, new TreeMap<Integer, Float>());

				// second loop over sentences to compare with
				for (Integer otherSentence : sentenceNrs) {
					// move on if there is already a distance for this
					// combination or the sentences are the same
					if ((thisSentence == otherSentence) || (thisDistances.get(otherSentence) != null)) {
						continue;
					}
					// get bags of words and paranoia check that they exist
					thisBag = sentenceNrsToBagOfWords.get(thisSentence);
					otherBag = sentenceNrsToBagOfWords.get(otherSentence);
					if (thisBag != null && otherBag != null) {
						// actually compare bags
						final Float distance;
						if (this.normalizeDistance) {
							distance = BagOfWordsHelper.normalizedLevenshteinDistance(thisBag, otherBag);
						} else {
							distance = BagOfWordsHelper.levenshteinDistance(thisBag, otherBag);
						}

						// write distance to both distance maps and write maps
						// back
						otherDistances = sentenceNrsToDistances.getOrDefault(otherSentence,
								new TreeMap<Integer, Float>());
						thisDistances.put(otherSentence, distance);
						otherDistances.put(thisSentence, distance);
						sentenceNrsToDistances.put(thisSentence, thisDistances);
						sentenceNrsToDistances.put(otherSentence, otherDistances);
					} else {
						LOGGER.warning(
								"Missing Bag of Words for sentence pair: " + thisSentence + ", " + otherSentence);
					}

				}
			}
			// serialize and flush to output
			final String jsonOut = gson.toJson(sentenceNrsToDistances, OUTPUT_TYPE);
			this.getOutputPorts().get(OUTPUT_ID).outputToAllCharPipes(jsonOut);
		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			this.closeAllOutputs();
		}

		return result;
	}

	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();

		if (this.getProperties().containsKey(PROPERTYKEY_NORMALIZE_DISTANCE)) {
			this.normalizeDistance = Boolean
					.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_NORMALIZE_DISTANCE));
			this.tfIdfMin = Float.parseFloat(this.getProperties().getProperty(PROPERTYKEY_TFIDF_MIN));
		}

		super.applyProperties();
	}
}
