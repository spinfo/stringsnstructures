package modules.bagOfTokens;

import java.lang.reflect.Type;
import java.util.ArrayList;
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

/**
 * The module reads a JSON array of AnchoredTextSegment objects. The ouput is a
 * JSON-serialized TreeMap<Integer,TreeMap<String,Integer>> mapping sentencesNrs
 * to maps of text segments with their count in the sentence.
 * 
 * @author David Neugebauer
 */
public class BagsOfTokensModule extends ModuleImpl {

	// Strings identifying/describing in- and output pipes
	private final static String INPUT_ID = "json input";
	private final static String OUTPUT_ID = "json output";
	private final static String INPUT_DESC = "Array<AnchoredTextSegement>";
	private final static String OUTPUT_DESC = "TreeMap<Integer,TreeMap<String,Integer>>";

	// Types for serializing and deserializing
	private final static Type INPUT_TYPE = new TypeToken<ArrayList<AnchoredTextSegment>>() {
	}.getType();
	private final static Type OUTPUT_TYPE = new TypeToken<TreeMap<Integer, TreeMap<String, Integer>>>() {
	}.getType();

	// Name and description of this module for the User
	private final static String MODULE_NAME = "Bags of Tokens";
	private final static String MODULE_DESCRIPTION = " * The module reads a JSON array of AnchoredTextSegment objects. The ouput is a"
			+ " JSON-serialized TreeMap<Integer,TreeMap<String,Integer>> mapping sentencesNrs"
			+ " to maps of text segments with their count in the sentence.";

	public BagsOfTokensModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

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

		// variables for reading
		final StringBuilder buffer = new StringBuilder();
		final InputPort inputPort = this.getInputPorts().get(INPUT_ID);

		// A Gson object to serialize and deserialize with
		final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

		try {
			// read the whole text once
			int charCode = inputPort.getInputReader().read();
			while (charCode != -1) {
				if (Thread.interrupted()) {
					throw new InterruptedException("Thread has been interrupted.");
				}
				buffer.append((char) charCode);
				charCode = inputPort.getInputReader().read();
			}

			// deserialize the read text into a list of text segments
			final ArrayList<AnchoredTextSegment> segments = GSON.fromJson(buffer.toString(), INPUT_TYPE);

			// From the segments derive a map mapping each sentence number to
			// another map holding the segment string mapped to a count
			final TreeMap<Integer, TreeMap<String, Integer>> sentenceNrsToCounts = new TreeMap<Integer, TreeMap<String, Integer>>();
			for (AnchoredTextSegment segment : segments) {

				for (TextAnchor anchor : segment.getTextAnchors()) {
					// get the map for the current anchors sentence number or
					// construct one
					final TreeMap<String, Integer> counts = sentenceNrsToCounts.getOrDefault(anchor.getSentenceNr(),
							new TreeMap<String, Integer>());
					// increment count and write to the counts map
					final int count = counts.getOrDefault(segment.getSegment(), 0);
					counts.put(segment.getSegment(), count + 1);
					// add or re-add the counts map to the table
					sentenceNrsToCounts.put(anchor.getSentenceNr(), counts);
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
