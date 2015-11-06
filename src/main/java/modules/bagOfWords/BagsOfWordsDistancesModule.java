package modules.bagOfWords;

import java.lang.reflect.Type;
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
	private final static String MODULE_DESCRIPTION = "Module to determine the distance between Bags of Words."
			+ " Currently supports Levenshtein distance, i.e. output is the number of"
			+ " substitutions, deletions or additions of words that would be needed to"
			+ " transform one Bag of Words into another.";
	
	// Property describing whether the distance should be normalized
	private final static String PROPERTYKEY_NORMALIZE_DISTANCE = "Normalize distance";
	private final static String DESCRIPTION_NORMALIZE_DISTANCE = "Divides each distance by it's possible maximum for the two BoWs.";
	private final static String DEFAUL_NORMALIZE_DISTANCE = "false";
	private boolean normalizeDistance;

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

		// Setup property determining whether the distance should be normalized
		this.getPropertyDescriptions().put(PROPERTYKEY_NORMALIZE_DISTANCE, DESCRIPTION_NORMALIZE_DISTANCE);
		this.getPropertyDefaultValues().put(PROPERTYKEY_NORMALIZE_DISTANCE, DEFAUL_NORMALIZE_DISTANCE);
	}

	@Override
	public boolean process() throws Exception {
		boolean result = true;

		// read the whole text once
		final String input = this.readStringFromInputPort(this.getInputPorts().get(INPUT_ID));

		// the output: an empty result map mapping sentence Nrs to a map holding
		// the distance of this sentence to each other sentence
		final TreeMap<Integer, TreeMap<Integer, Float>> sentenceNrsToDistances = new TreeMap<Integer, TreeMap<Integer, Float>>();

		try {
			// deserialize the input
			final Gson gson = new GsonBuilder().setPrettyPrinting().create();
			final TreeMap<Integer, TreeMap<String, Integer>> sentenceNrsToBagOfWords = gson.fromJson(input, INPUT_TYPE);
			final Set<Integer> sentenceNrs = sentenceNrsToBagOfWords.keySet();

			// bags that are compared
			// word => count in sentence
			TreeMap<String, Integer> thisBag;
			TreeMap<String, Integer> otherBag;

			// distances that are produced by comparison
			// sentenceNr => distance
			TreeMap<Integer, Float> thisDistances;
			TreeMap<Integer, Float> otherDistances;

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
							distance = normalizedLevenshteinDistance(thisBag, otherBag);
						} else {
							distance = levenshteinDistance(thisBag, otherBag);
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

	/**
	 * Assumes that both bags of words are simple bags of words and do not
	 * contain word counts. Then computes the number of operations needed to
	 * transform one Bag of Words into another. Removal, addition and
	 * substitution of a word each have a count of 1.
	 * 
	 * The input parameters bagOne and bagTwo are interchangeable.
	 * 
	 * @param bagOne
	 *            a TreeMap mapping Strings to counts
	 * @param bagTwo
	 *            a TreeMap mapping Strings to counts
	 * @return The distance computed, always a positive integer value
	 */
	public static float levenshteinDistance(TreeMap<String, Integer> bagOne, TreeMap<String, Integer> bagTwo) {
		// Find out the size of the bigger bag of words
		final int maxSize = Math.max(bagOne.size(), bagTwo.size());
		return levenshteinDistance(bagOne, bagTwo, maxSize);
	}

	// Compute Levenshtein distance when the size of the biggest bag is already
	// known
	private static float levenshteinDistance(TreeMap<String, Integer> bagOne, TreeMap<String, Integer> bagTwo,
			int maxSize) {
		// Find the amount of matches
		int matchCount = 0;
		for (String string : bagOne.keySet()) {
			if (bagTwo.containsKey(string)) {
				matchCount += 1;
			}
		}
		// The operations needed to transform each map into the other is the
		// length of the bigger bag minus the number of matches
		return (float) (maxSize - matchCount);
	}

	/**
	 * Compute the Levenshtein distance, but divide it by it's upper bound, i.e.
	 * the size of the bigger bag of words.
	 * 
	 * @param bagOne
	 *            a TreeMap mapping Strings to counts
	 * @param bagTwo
	 *            a TreeMap mapping Strings to counts
	 * @return The distance computed, floating point number between 0 and 1.0
	 */
	public static float normalizedLevenshteinDistance(TreeMap<String, Integer> bagOne,
			TreeMap<String, Integer> bagTwo) {
		final int maxSize = Math.max(bagOne.size(), bagTwo.size());
		final float distance = levenshteinDistance(bagOne, bagTwo, maxSize);
		final float result = (new Float(distance) / new Float(maxSize));
		return result;
	}
	
	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();

		if (this.getProperties().containsKey(PROPERTYKEY_NORMALIZE_DISTANCE)) {
			this.normalizeDistance = Boolean
					.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_NORMALIZE_DISTANCE));
		}

		super.applyProperties();
	}
}
