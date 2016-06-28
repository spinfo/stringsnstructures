package modules.bag_of_words;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import models.NodeRepresentation;
import models.PatternInfoRepresentation;
import models.SuffixTreeRepresentation;

/**
 * Helper class, static methods to produce Bags Of Words.
 * 
 * @author david
 *
 */
class BagOfWordsFactory {

	// this class should never be instantiated
	private BagOfWordsFactory() {
	};

	private static final Pattern WHITESPACE = Pattern.compile("[ ]+");

	/**
	 * Builds a Bag of words by splitting the sentence on whitespace and
	 * noting a word count for each resulting words.
	 * 
	 * @param sentence
	 *            The sentence to get the words from
	 * @return A map of all words with a word count in the sentence
	 */
	public static TreeMap<String, Integer> build(String sentence) {
		final TreeMap<String, Integer> bagOfWords = new TreeMap<String, Integer>();

		final String[] words = WHITESPACE.split(sentence);

		int wordCount = 0;
		for (String word : words) {
			if (word.length() > 0) {
				wordCount = bagOfWords.getOrDefault(word, 0);
				wordCount += 1;
				bagOfWords.put(word, wordCount);
			}
		}

		return bagOfWords;
	}

	/**
	 * From a list of sentences build a map mapping sentence numbers to Bags of
	 * Words of those sentences. Sentence numbers are assigned sequentially.
	 * 
	 * @param sentences
	 *            The sentences to convert to Bags of Words
	 * @return A map mapping sentence numbers to bags of Words
	 */
	public static Map<Integer, TreeMap<String, Integer>> build(final String[] sentences) {
		final HashMap<Integer, TreeMap<String, Integer>> bagsOfWords = new HashMap<Integer, TreeMap<String, Integer>>();
		int sentenceNr = 0;

		// traverse sentences and build a list of words
		for (String sentence : sentences) {
			final TreeMap<String, Integer> bag = BagOfWordsFactory.build(sentence);
			// add the produced bag to the result if not empty
			if (bag.size() > 0) {
				bagsOfWords.put(sentenceNr, bag);
				sentenceNr += 1;
			}
		}
		return bagsOfWords;
	}

	/**
	 * From a list of labels build a map mapping label occurences to Bags of
	 * Words of those labels.
	 * 
	 * @param labels
	 *            The labels to convert to Bags of Words
	 * @return A map mapping labels to bags of Words
	 */
	public static Map<Integer, TreeMap<String, Integer>> buildFromLabels(final String[] labels) {
		final HashMap<Integer, TreeMap<String, Integer>> bagsOfWords = new HashMap<Integer, TreeMap<String, Integer>>();
		// traverse sentences and build a list of words
		for (String label : labels) {
			final TreeMap<String, Integer> bag = BagOfWordsFactory.build(label);
			// add the produced bag to the result if not empty
			if (bag.size() > 0) {
				
				Integer i = 1;
				if(!bagsOfWords.containsKey(bag)){
					bagsOfWords.put(i, bag);
				}
				else if(bagsOfWords.containsKey(bag)){
//					int val = map.get(line);
//					val = val + 1;
					bagsOfWords.put(bag.get(bag) + 1, bag);
					
				}
				
				i++;
			}
			
		}
		return bagsOfWords;
	}
	
	
	/**
	 * From a SuffixTreeRepresentation build a map mapping sentences numbers to
	 * bags of words. Sentence numbers are the trees pattern numbers. Words are
	 * taken to be the tree's node's labels.
	 * 
	 * @param treeRepresentation
	 *            The SuffixTreeRepresentation to convert
	 * @return the map mapping bags of words to sentence numbers
	 */
	public static Map<Integer, TreeMap<String, Integer>> build(SuffixTreeRepresentation treeRepresentation) {
		final HashMap<Integer, TreeMap<String, Integer>> result = new HashMap<Integer, TreeMap<String, Integer>>();

		// Each node represents a suffix, called label
		for (NodeRepresentation node : treeRepresentation.getNodes()) {
			final String label = node.getLabel();

			// traverse the label's occurences, i.e. it's patternInfos
			for (PatternInfoRepresentation patternInfo : node.getPatternInfos()) {
				// get the count map for the pattern that the label's
				// occurence belongs to
				final TreeMap<String, Integer> labelCounts = result.getOrDefault(patternInfo.getPatternNr(),
						new TreeMap<String, Integer>());
				// increment count and write to the counts map
				final int count = labelCounts.getOrDefault(label, 0);
				labelCounts.put(label, count + 1);
				// add or re-add the counts map to the table
				result.put(patternInfo.getPatternNr(), labelCounts);
			}
		}
		return result;
	}

}
