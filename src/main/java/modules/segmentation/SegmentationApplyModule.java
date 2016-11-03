package modules.segmentation;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import base.workbench.ModuleRunner;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

/**
 * Takes a list of newline separated segmented words and applies their
 * segmentation to the input text
 */
public class SegmentationApplyModule extends ModuleImpl {

	private static final Logger LOGGER = Logger.getLogger(SegmentationApplyModule.class.getName());

	private static final String INPUT_SEGMENTED_WORDS_ID = "segmented words";
	private static final String INPUT_TEXT_ID = "text";
	private static final String OUTPUT_ID = "output";

	private static final String PROPERTYKEY_SEGMENT_DELIMITER = "segment delimiter";
	private String segmentDelimiter;

	public SegmentationApplyModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		// Call parent constructor
		super(callbackReceiver, properties);

		// add module description
		this.setDescription(
				"Takes a list of newline separated segmented words and applies their segmentation to the input text");

		// add propertydescription and set defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Segmentation Apply Module");
		this.getPropertyDescriptions().put(PROPERTYKEY_SEGMENT_DELIMITER,
				"delimiter string segmenting the input (and output) words");
		this.getPropertyDefaultValues().put(PROPERTYKEY_SEGMENT_DELIMITER, "|");

		// define I/O
		InputPort segmentedWordsInput = new InputPort(INPUT_SEGMENTED_WORDS_ID,
				"The segmented words used to segment the text input.", this);
		InputPort textInput = new InputPort(INPUT_TEXT_ID, "The text to segment.", this);
		OutputPort outputPort = new OutputPort(OUTPUT_ID,
				"The input text segmented by the segmentation supplied as input.", this);

		segmentedWordsInput.addSupportedPipe(CharPipe.class);
		textInput.addSupportedPipe(CharPipe.class);
		outputPort.addSupportedPipe(CharPipe.class);

		this.addInputPort(segmentedWordsInput);
		this.addInputPort(textInput);
		this.addOutputPort(outputPort);
	}

	@Override
	public boolean process() throws Exception {
		boolean result = true;

		BufferedReader segmentsReader;
		BufferedReader textReader;

		// map words to their segmentation
		Map<String, String> words = new HashMap<>();

		try {
			segmentsReader = new BufferedReader(getInputPorts().get(INPUT_SEGMENTED_WORDS_ID).getInputReader());
			textReader = new BufferedReader(getInputPorts().get(INPUT_TEXT_ID).getInputReader());

			// collect words and map them to their segmented version
			String delimiterRegex = Pattern.quote(this.segmentDelimiter);
			String segmentedWord = null;
			String word;
			while ((segmentedWord = segmentsReader.readLine()) != null) {
				word = segmentedWord.replaceAll(delimiterRegex, "");
				words.put(word, segmentedWord);
			}

			// read the input text and replace every word with it's segmented
			// version
			StringBuilder segmentedLine = new StringBuilder();
			OutputPort out = this.getOutputPorts().get(OUTPUT_ID);
			String line = null;
			String[] textWords;
			segmentedWord = null;
			while ((line = textReader.readLine()) != null) {
				// iterate over words in the input line
				textWords = line.split("\\s");
				for (String textWord : textWords) {
					if (textWord.length() == 0) {
						continue;
					}

					// if no segmented match for the word can be found issue a
					// warning and write the unsegmented version else output the
					// segmented word
					segmentedWord = words.get(textWord);
					if (segmentedWord != null) {
						segmentedLine.append(segmentedWord);
					} else {
						LOGGER.warning("No segmentation for input word: " + textWord);
						segmentedLine.append(textWord);
					}

					// separate output words by a single whitspace char
					segmentedLine.append(' ');
				}
				// do the actual ouput finishing each line with a newline
				segmentedLine.setLength(segmentedLine.length() == 0 ? 0 : segmentedLine.length() - 1);
				segmentedLine.append('\n');
				out.outputToAllCharPipes(segmentedLine.toString());
				segmentedLine.setLength(0);
			}

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

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		this.segmentDelimiter = this.getProperties().getProperty(PROPERTYKEY_SEGMENT_DELIMITER,
				this.getPropertyDefaultValues().get(PROPERTYKEY_SEGMENT_DELIMITER));

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(SegmentJoinerModule.class, args);
	}

}
