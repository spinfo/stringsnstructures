package modules.bagOfWords;

import java.lang.reflect.Type;
import java.util.Properties;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.suffixTree.output.NodeRepresentation;
import modules.suffixTree.output.PatternInfoRepresentation;
import modules.suffixTree.output.SuffixTreeRepresentation;

/**
 * The module reads a JSON representation of a GeneralisedSuffixTree and
 * constructs bags of words from it. It assumes that the tree's pattern nrs
 * stand in for sentences and treats each node's label as a word. The ouput then is a
 * JSON-serialized TreeMap<Integer,TreeMap<String,Integer>> mapping
 * patternNrs to maps of the contained label instances with a count in the pattern.
 * 
 * @author David Neugebauer
 */
public class BagsOfWordsModule extends ModuleImpl {

	// Strings identifying/describing in- and output pipes
	private final static String INPUT_ID = "json";
	private final static String OUTPUT_ID = "json";
	private final static String INPUT_DESC = "[text/json] SuffixTreeRepresentation";
	private final static String OUTPUT_DESC = "[text/json] TreeMap<Integer,TreeMap<String,Integer>>";

	// Types for serializing and deserializing
	private final static Type INPUT_TYPE = new TypeToken<SuffixTreeRepresentation>() {
	}.getType();
	private final static Type OUTPUT_TYPE = new TypeToken<TreeMap<Integer, TreeMap<String, Integer>>>() {
	}.getType();

	// Name and description of this module for the User
	private final static String MODULE_NAME = "BagsOfWords";
	private final static String MODULE_DESCRIPTION = "The module reads a JSON representation of a GeneralisedSuffixTree and"
			+ " constructs bags of words from it. It assumes that the tree's pattern nrs"
			+ " stand in for sentences and treats each node's label as a word. The ouput then is a"
			+ " JSON-serialized TreeMap<Integer,TreeMap<String,Integer>> mapping"
			+ " patternNrs to maps of the contained label instances with a count in the pattern.";

	public BagsOfWordsModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

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
	}

	@Override
	public boolean process() throws Exception {
		boolean result = true;

		// A Gson object to serialize and deserialize with
		final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

		try {
			// read the whole text once, deserialize into tree's representation
			final String text = this.readStringFromInputPort(this.getInputPorts().get(INPUT_ID));
			final SuffixTreeRepresentation treeRepresentation = GSON.fromJson(text, INPUT_TYPE);

			// A map holding the collected pattern's labels
			final TreeMap<Integer, TreeMap<String, Integer>> sentenceNrsToCounts = new TreeMap<Integer, TreeMap<String, Integer>>();
			// Each node represents a suffix, called label
			for (NodeRepresentation node : treeRepresentation.getNodes()) {
				final String label = node.getLabel();

				// traverse the label's occurences, i.e. it's patternInfos
				for (PatternInfoRepresentation patternInfo : node.getPatternInfos()) {
					// get the count map for the pattern that the label's
					// occurence belongs to
					final TreeMap<String, Integer> labelCounts = sentenceNrsToCounts
							.getOrDefault(patternInfo.getPatternNr(), new TreeMap<String, Integer>());
					// increment count and write to the counts map
					final int count = labelCounts.getOrDefault(label, 0);
					labelCounts.put(label, count + 1);
					// add or re-add the counts map to the table
					sentenceNrsToCounts.put(patternInfo.getPatternNr(), labelCounts);
				}
			}

			// serialize everything to json and flush it to the output ports
			final String jsonOut = GSON.toJson(sentenceNrsToCounts, OUTPUT_TYPE);
			this.getOutputPorts().get(OUTPUT_ID).outputToAllCharPipes(jsonOut);

		} catch (Exception exception) {
			result = false;
			throw exception;
		} finally {
			this.closeAllOutputs();
		}

		return result;
	}

}
