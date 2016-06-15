package modules.bagOfWords;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Helper class, contains various static convenience methods for Bags of Words
 * 
 * @author david
 */
public class BagOfWordsHelper {

	private static final Logger LOGGER = Logger.getLogger(BagOfWordsHelper.class.getName());

	// this should never be instantiated
	private BagOfWordsHelper() {
	};

	/**
	 * Merges all words from givingBag to the receivingBag, incrementing the
	 * word counts, such that after this operation the receivingBag is a Bag of
	 * all Words in both original bags.
	 * 
	 * The second bag of words is left as is.
	 */
	public static void merge(Map<String, Integer> receivingBag, Map<String, Integer> givingBag) {
		int wordCount = 0;

		for (String term : givingBag.keySet()) {
			wordCount = receivingBag.getOrDefault(term, 0);
			wordCount += givingBag.getOrDefault(term, 0);
			receivingBag.put(term, wordCount);
		}
	}

	/**
	 * Merges all words from givingBag to the receivingBag, incrementing the
	 * word counts, such that after this operation the receivingBag is a Bag of
	 * all Words in both original bags.
	 * 
	 * The second bag of words is left as is.
	 */
	public static void mergeDouble(Map<String, Double> receivingBag, Map<String, Double> givingBag) {
		double wordCount = 0d;

		for (String term : givingBag.keySet()) {
			wordCount = receivingBag.getOrDefault(term, 0d);
			wordCount += givingBag.getOrDefault(term, 0d);
			receivingBag.put(term, wordCount);
		}
	}

	/**
	 * Compute an IDF values for the terms given. IDF here is simply the number
	 * of all
	 * 
	 * @param termFrequencies
	 * @param documentCount
	 * @return
	 */
	public static Map<String, Double> inverseDocumentFrequencies(Map<String, Integer> termFrequencies,
			int documentCount) {
		TreeMap<String, Double> result = new TreeMap<String, Double>();
		int freq = 0;
		double idf = 0.0;
		for (String term : termFrequencies.keySet()) {
			freq = termFrequencies.get(term);
			idf = Math.log10((double) documentCount / (double) freq);
			result.put(term, idf);
		}
		return result;
	}

	/**
	 * Compute an IDF values for the terms given. IDF here is simply the number
	 * of all
	 * 
	 * @param termFrequencies
	 * @param documentCount
	 * @return
	 */
	public static Map<String, Double> inverseDocumentFrequenciesDouble(Map<String, Double> termFrequencies,
			int documentCount) {
		TreeMap<String, Double> result = new TreeMap<String, Double>();
		double freq = 0;
		double idf = 0.0;
		for (String term : termFrequencies.keySet()) {
			freq = termFrequencies.get(term);
			idf = Math.log10((double) documentCount / (double) freq);
			result.put(term, idf);
		}
		return result;
	}

	/**
	 * Produces a new copy of the Bag of Words with all words removed, whose
	 * TF-IDF value is below the given minimum value
	 * 
	 * TF for a term is taken to be the word count in the bag. The IDF value for
	 * a term is provided via paramter.
	 * 
	 * @param bag
	 *            The Bag of Words to copy
	 * @param inverseDocumentFrequencies
	 *            The frequencies over all documents
	 * @param tfIdfMin
	 *            The minimum value below which a term should be filtered out.
	 * @return A new Bag of Words with all words ranking below tfIdfMin removed.
	 */
	public static TreeMap<String, Integer> tfIdfMinFilter(Map<String, Integer> bag,
			Map<String, Double> inverseDocumentFrequencies, float tfIdfMin) {
		final TreeMap<String, Integer> result = new TreeMap<String, Integer>();
		Integer tf = 0;
		Double idf = 0.0;
		Double tfIdf = 0.0;

		for (String term : bag.keySet()) {
			tf = bag.get(term);
			idf = inverseDocumentFrequencies.get(term);
			// compute tfIdf, if tf or idf is missing, add the bag of words to
			// the result and issue a warning
			if (tf != null && idf != null) {
				tfIdf = tf * idf;
				if (tfIdf < tfIdfMin) {
					LOGGER.info("Filtering term '" + term + "' (TF-IDF: " + tfIdf + ")");
				}
			} else {
				LOGGER.warning("tf or idf value missing for term: " + term);
				result.put(term, tf);
			}
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
	public static float levenshteinDistance(Map<String, Integer> bagOne, Map<String, Integer> bagTwo) {
		// Find out the size of the bigger bag of words
		final int maxSize = Math.max(bagOne.size(), bagTwo.size());
		return levenshteinDistance(bagOne, bagTwo, maxSize);
	}
	
	// Compute Levenshtein distance when the size of the biggest bag is already
	// known
	private static float levenshteinDistance(Map<String, Integer> bagOne, Map<String, Integer> bagTwo, int maxSize) {
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
	public static float normalizedLevenshteinDistance(Map<String, Integer> bagOne, Map<String, Integer> bagTwo) {
		final int maxSize = Math.max(bagOne.size(), bagTwo.size());
		final float distance = levenshteinDistance(bagOne, bagTwo, maxSize);
		final float result = (new Float(distance) / new Float(maxSize));
		return result;
	}
}
