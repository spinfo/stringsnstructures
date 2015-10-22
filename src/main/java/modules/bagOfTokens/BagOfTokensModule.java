package modules.bagOfTokens;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class BagOfTokensModule extends ModuleImpl {

	// Strings identifying/describing in- and output pipes
	private final static String INPUT_ID = "text input";
	private final static String OUTPUT_ID = "csv output";
	private final static String INPUT_DESC = "Achored Text segements";
	private final static String OUTPUT_DESC = "CSV Plain text character ouput";

	// Name and description of this module for the User
	private final static String MODULE_NAME = "Bag of Tokens";
	private final static String MODULE_DESCRIPTION = "Module";

	// A logger to log stuff
	private static final Logger LOGGER = Logger.getLogger(BagOfTokensModule.class.getSimpleName());
	
	// String to separate columns
	private static final String COL_SEPARATOR = "\t";

	public BagOfTokensModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

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

		// the collected text segments
		ArrayList<AnchoredTextSegment> segments = new ArrayList<AnchoredTextSegment>();

		// variables for reading
		StringBuilder buffer = new StringBuilder();
		final InputPort inputPort = this.getInputPorts().get(INPUT_ID);
		// we keep a two char window in addition to the buffer
		char current = '\0';
		char last = '\0';

		try {
			// read into the buffer until a segment of anchored text ends
			int charCode = inputPort.getInputReader().read();
			while (charCode != -1) {
				current = (char) charCode;

				if (Thread.interrupted()) {
					throw new InterruptedException("Thread has been interrupted.");
				}

				// check if a segment end is reached, i.e. we last read a
				// newline char, we are not inside a two-letter windows line end
				// and the current char is not a tab (a tab would mean, that
				// the current segment in the buffer continues in the new line).
				if ((last == '\n' || last == '\r') && (current != '\n' && current != '\r' && current != '\t')) {
					processSegment(buffer.toString(), segments);
					// reset the buffer
					buffer.setLength(0);
				}
				// append to the current instance in the buffer
				buffer.append((char) charCode);

				// setup variables for next cycle
				last = current;
				charCode = inputPort.getInputReader().read();
			}
			// Process the remaining buffer
			processSegment(buffer.toString(), segments);

			// From the segments derive a map mapping each sentence number to
			// another map (a column in the final table) holding the segment
			// string mapped to a count
			final TreeMap<Integer, TreeMap<String, Integer>> table = new TreeMap<Integer, TreeMap<String, Integer>>();
			for (AnchoredTextSegment segment : segments) {
				for (TextAnchor anchor : segment.getTextAnchors()) {
					// get the map for the current anchors sentence number or
					// construct one
					final TreeMap<String, Integer> column = table.getOrDefault(anchor.getSentenceNr(),
							new TreeMap<String, Integer>());
					// increment count and write to the column
					final int count = column.getOrDefault(segment.text(), 0);
					column.put(segment.text(), count+1);
					// add or re-add the column to the table
					table.put(anchor.getSentenceNr(), column);
				}
			}

			final OutputPort outputPort = this.getOutputPorts().get(OUTPUT_ID);
			final Set<Integer> sentenceNrs = table.keySet();
			// write header line with sentence numbers leaving first field empty
			final StringBuilder line = new StringBuilder(" ");
			for(Integer sentenceNr : sentenceNrs) {
				line.append(COL_SEPARATOR + sentenceNr.toString());
			}
			line.append(System.lineSeparator());
			outputPort.outputToAllCharPipes(line.toString());
			line.setLength(0);
			// repeat writing for every segment text
			for (AnchoredTextSegment segment : segments) {
				line.append(segment.text());
				for (Integer sentenceNr : sentenceNrs) {
					// if no count is present, write a 0
					final Integer count = table.get(sentenceNr).getOrDefault(segment.text(), 0);
					line.append(COL_SEPARATOR + count.toString());
				}
				line.append(System.lineSeparator());
				outputPort.outputToAllCharPipes(line.toString());
				line.setLength(0);
			}

		} catch (Exception exception) {
			result = false;
			throw exception;
		} finally {
			this.closeAllOutputs();
		}

		return result;
	}

	/**
	 * Parses the input String as a AnchoredTextSegment and adds it to the
	 * segment list if that succeeded, otherwise issues a warning
	 * 
	 * @param input
	 *            The String to parse as an AnchoredTextSegment
	 * @param segmentList
	 *            The list to add to on success
	 */
	private void processSegment(String input, ArrayList<AnchoredTextSegment> segmentList) {
		final AnchoredTextSegment segment = AnchoredTextSegment.parse(input);
		if (segment != null) {
			segmentList.add(segment);
		} else {
			LOGGER.warning("Could not parse segment: " + segment);
		}
	}

}
