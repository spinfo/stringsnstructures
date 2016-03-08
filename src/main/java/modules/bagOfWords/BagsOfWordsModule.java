package modules.bagOfWords;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.parallelization.CallbackReceiver;
import models.SuffixTreeRepresentation;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

/**
 * This module reads either a) A JSON representation of a GeneralisedSuffixTree.
 * It then constructs bags of words from it. It assumes that the tree's pattern
 * nrs stand in for sentence numbers and treats each node's label as a word. b)
 * A simple List of newline separated sentences. It then simply splits the words
 * on whitespace.
 * 
 * The output in either case is a JSON-serialized TreeMap<Integer,TreeMap
 * <String,Integer>> mapping sentence numbers to maps of "words" with a count in
 * the sentence.
 * 
 * @author David Neugebauer
 */
public class BagsOfWordsModule extends ModuleImpl {

	private final static Logger LOGGER = Logger.getLogger(BagsOfWordsModule.class.getName());

	// Strings identifying/describing in- and output pipes
	private final static String INPUT_GST_ID = "GST";
	private final static String INPUT_GST_DESC = "[text/json] Suffix Tree Representation (class: SuffixTreeRepresentation)";
	private final static String INPUT_SIMPLE_ID = "Simple";
	private final static String INPUT_SIMPLE_DESC = "[text/plain] A newline separated List of sentences.";
	private final static String INPUT_TEXT_ID = "Simple labels";
	private final static String INPUT_TEXT_DESC = "[text/plain] A newline separated List of labels.";
	private final static String OUTPUT_BOW_ID = "BoW";
	private final static String OUTPUT_BOW_DESC = "[text/json] Bags of Words (class: TreeMap<Integer,TreeMap<String,Integer>>)";

	// Types for serializing and deserializing
	private final static Type INPUT_TYPE = new TypeToken<SuffixTreeRepresentation>() {
	}.getType();
	private final static Type OUTPUT_TYPE = new TypeToken<TreeMap<Integer, TreeMap<String, Integer>>>() {
	}.getType();

	// Name and description of this module for the User
	private final static String MODULE_NAME = "BagsOfWords";
	private final static String MODULE_DESCRIPTION = "This module reads either<br/>"
			+ "<ol><li>A JSON representation of a GeneralisedSuffixTree.<br/>It then constructs bags of words from it. It assumes that the tree's pattern nrs stand in for sentence numbers and treats each node's label as a word.</li>"
			+ "<li>A simple List of newline separated sentences.<br/>It then simply splits the words on whitespace.</li></ol>" + "<br/>"
			+ "The output in either case is a JSON-serialized TreeMap&lt;Integer,TreeMap&lt;String,Integer&gt;&gt; mapping sentence numbers to maps of \"words\" with a count in the sentence.";

	public BagsOfWordsModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Set the modules name and description
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, MODULE_NAME);
		this.setDescription(MODULE_DESCRIPTION);

		// Setup I/O, reads from and writes to CharPipe
		InputPort inputGstPort = new InputPort(INPUT_GST_ID, INPUT_GST_DESC, this);
		inputGstPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputGstPort);

		InputPort inputSimplePort = new InputPort(INPUT_SIMPLE_ID, INPUT_SIMPLE_DESC, this);
		inputSimplePort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputSimplePort);
		
		InputPort inputSimpleText = new InputPort(INPUT_TEXT_ID, INPUT_TEXT_DESC, this);
		inputSimplePort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputSimpleText);
		
		OutputPort outputPort = new OutputPort(OUTPUT_BOW_ID, OUTPUT_BOW_DESC, this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputPort);
	}

	@Override
	public boolean process() throws Exception {
		boolean result = true;

		// A Gson object to serialize and deserialize with
		final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

		try {

			// the result of calling the module may only be set once
			final String text;
			final Map<Integer, TreeMap<String, Integer>> bagsOfWords;

			// two input sources are possible
			final InputPort gstPort = this.getInputPorts().get(INPUT_GST_ID);
			final InputPort simplePort = this.getInputPorts().get(INPUT_SIMPLE_ID);
			final InputPort simpleText = this.getInputPorts().get(INPUT_TEXT_ID);

			// parse the input and delegate building of bags of words to the
			// appropriate function
			if (gstPort.isConnected() && simplePort.isConnected()) {
				throw new Exception("Either gst input or simple input has to be connected, not both.");
			} else if (gstPort.isConnected()) {
				text = this.readStringFromInputPort(gstPort);
				final SuffixTreeRepresentation treeRepresentation = GSON.fromJson(text, INPUT_TYPE);
				bagsOfWords = BagOfWordsFactory.build(treeRepresentation);
			} else if (simplePort.isConnected()) {
				text = this.readStringFromInputPort(simplePort);
				final String[] sentences = Pattern.compile("\r\n|\n|\r").split(text);
				bagsOfWords = BagOfWordsFactory.build(sentences);
			} else if (simpleText.isConnected()) {
				text = this.readStringFromInputPort(simpleText);
				final String[] labels = Pattern.compile("\r\n|\n|\r").split(text);
				bagsOfWords = BagOfWordsFactory.build(labels);
			} 
			
			else {
				throw new Exception("Either gst input or simple input has to be connected.");
			}

			LOGGER.info("Finished building " + bagsOfWords.size() + "  Bags of Words.");

			// serialize the bags of words to json and flush it to the output
			// ports
			final String jsonOut = GSON.toJson(bagsOfWords, OUTPUT_TYPE);
			this.getOutputPorts().get(OUTPUT_BOW_ID).outputToAllCharPipes(jsonOut);

		} catch (Exception exception) {
			result = false;
			throw exception;
		} finally {
			this.closeAllOutputs();
		}

		return result;
	}

}
