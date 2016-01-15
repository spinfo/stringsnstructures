package modules.suffixTreeModuleWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import modules.BytePipe;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.suffixTree.suffixMain.GeneralisedSuffixTreeMain;
import modules.suffixTree.suffixTree.applications.ResultToLabelListListener;
import modules.suffixTree.suffixTree.applications.ResultToJsonListener;
import modules.suffixTree.suffixTree.applications.ResultToLabelFreqListListener;
import modules.suffixTree.suffixTree.applications.SuffixTreeAppl;
import modules.suffixTree.suffixTree.applications.TreeWalker;
import modules.suffixTree.suffixTree.node.activePoint.ExtActivePoint;
import modules.suffixTree.suffixTree.node.info.End;
import modules.suffixTree.suffixTree.node.nodeFactory.GeneralisedSuffixTreeNodeFactory;

import common.parallelization.CallbackReceiver;

/**
 * Module Reads from KWIP modules output into a suffix tree. Constructs a
 * representation of that tree, that can be used as input for clustering.
 * 
 * Alternatively reads a simple String of '$'-separated sentences and does the
 * same.
 * 
 * @author David Neugebauer
 */
public class GeneralisedSuffixTreeModule extends modules.ModuleImpl {

	private static final Logger LOGGER = Logger.getLogger(GeneralisedSuffixTreeMain.class.getName());

	// Variables for the module
	private static final String MODULE_NAME = "GeneralisedSuffixTreeModule";
	private static final String MODULE_DESCRIPTION = "Module Rreads from KWIP modules output into a suffix tree. Constructs a "
			+ "representation of that tree, that can be used as input for clustering.";

	// Variables describing I/O
	private static final String INPUT_TEXT_ID = "plain";
	private static final String INPUT_TEXT_DESC = "[text/plain] Takes a plaintext representation of the KWIP result.";
	private static final String INPUT_UNITS_ID = "units";
	private static final String INPUT_UNITS_DESC = "[text/plain] Takes a unit list (numbers of available types) from the KWIP result";
	private static final String OUTPUT_JSON_ID = "json";
	private static final String OUTPUT_JSON_DESC = "[text/json] A json representation of the tree build, suitable for clustering.";
	private static final String OUTPUT_XML_ID = "xml";
	private static final String OUTPUT_XML_DESC = "[bytestream] An xml representation of the tree build, suitbale for clustering.";
	private static final String OUTPUT_LIST_ID = "label list";
	private static final String OUTPUT_LIST_DESC = "[text/plain] A list of labels separated by newline";
	private static final String OUTPUT_FREQ_LIST_ID = "label frequencies list";
	private static final String OUTPUT_FREQ_LIST_DESC = "[text/plain] A list of labels with frequencies separated by newline.";

	// Container to hold units if provided
	private ArrayList<Integer> unitList = null;

	// Variables for input processing
	private static final Pattern NEWLINE_PATTERN = Pattern.compile("\r\n|\n|\r");
	private static final char TERMINATOR = '$';

	/**
	 * Constructor
	 * 
	 * @param callbackReceiver
	 *            callback receiver
	 * @param properties
	 *            module properties
	 * @throws Exception
	 *             thrown upon error
	 */
	public GeneralisedSuffixTreeModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		// Call parent constructor
		super(callbackReceiver, properties);

		// Set the modules name and description
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, MODULE_NAME);
		this.setDescription(MODULE_DESCRIPTION);

		// Add module category
		this.setCategory("Tree-building");

		// Setup I/O, reads from char input produced by KWIP.
		// json output is to a CharPipe as expected, but
		// xml Output is to a BytePipe for compatibility reasons to the
		// clustering module
		InputPort inputTextPort = new InputPort(INPUT_TEXT_ID, INPUT_TEXT_DESC, this);
		inputTextPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputTextPort);

		InputPort inputUnitsPort = new InputPort(INPUT_UNITS_ID, INPUT_UNITS_DESC, this);
		inputUnitsPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputUnitsPort);

		OutputPort outputJsonPort = new OutputPort(OUTPUT_JSON_ID, OUTPUT_JSON_DESC, this);
		outputJsonPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputJsonPort);

		OutputPort outputXmlPort = new OutputPort(OUTPUT_XML_ID, OUTPUT_XML_DESC, this);
		outputXmlPort.addSupportedPipe(BytePipe.class);
		super.addOutputPort(outputXmlPort);

		OutputPort outputListPort = new OutputPort(OUTPUT_LIST_ID, OUTPUT_LIST_DESC, this);
		outputListPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputListPort);

		OutputPort outputFreqListPort = new OutputPort(OUTPUT_FREQ_LIST_ID, OUTPUT_FREQ_LIST_DESC, this);
		outputFreqListPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputFreqListPort);
	}

	@Override
	public boolean process() throws Exception {
		boolean result = true;

		try {
			// read the whole text once, necessary to know the text's length
			final String text = readStringFromInputPort(this.getInputPorts().get(INPUT_TEXT_ID));

			// The suffix tree used to read the input is a generalized
			// suffix tree for a text of the length of the input string
			final SuffixTreeAppl suffixTreeAppl = new SuffixTreeAppl(text.length(),
					new GeneralisedSuffixTreeNodeFactory());

			// set some variables to regulate flow in SuffixTree classes
			suffixTreeAppl.unit = 0;
			suffixTreeAppl.oo = new End(Integer.MAX_VALUE / 2);

			// if a unit list is provided read it and set the suffix tree
			// variables accordingly
			if (this.getInputPorts().get(INPUT_UNITS_ID).isConnected()) {
				this.unitList = readUnitListFromInput(this.getInputPorts().get(INPUT_UNITS_ID));
				if (this.unitList.size() == 0) {
					throw new Exception("Unit list provided but empty.");
				}
				suffixTreeAppl.unitCount = this.unitList.size();
			} else {
				LOGGER.warning("GST: No port for unit list connected. Output might be unsuitable for clustering.");
			}

			// start and end indices regulate which portion of the input we are
			// reading at any given moment
			int start = 0;
			int end = text.indexOf(TERMINATOR, start);

			if (end != -1) {
				// traverse the first portion of the input string
				// TODO comment explaining why extActivePoint has to be null
				// here
				suffixTreeAppl.phases(text, start, end + 1, null);
				start = end + 1;

				// traverse the remaining portions of the input string
				ExtActivePoint extActivePoint;
				String nextText;

				// set end for first text, end indicates termination symbol $
				suffixTreeAppl.oo.setEnd(end);

				start = end + 1;
				end = text.indexOf(TERMINATOR, start);
				while (end != -1) {
					// each cycle represents a text read
					suffixTreeAppl.textNr++;

					// If a unit list from KWIP is available, make sure that the
					// currently read text is counted for the unit, that it was
					// mapped to by KWIP
					if (unitList != null && (unitList.get(suffixTreeAppl.unit) == suffixTreeAppl.textNr)) {
						suffixTreeAppl.unit++;
					}

					// TODO comment explaining what setting the active point
					// does
					nextText = text.substring(start, end + 1);
					extActivePoint = suffixTreeAppl.longestPath(nextText, 0, 1, start, true);
					if (extActivePoint == null) {
						LOGGER.warning(" GeneralisedSuffixTreeMain activePoint null");
						break;
					}

					// TODO comment explaining the use of .oo and extActivePoint
					// why has this to happen here instead of inside phases() ?
					suffixTreeAppl.oo = new End(Integer.MAX_VALUE / 2);
					suffixTreeAppl.phases(text, start + extActivePoint.phase, end + 1, extActivePoint);

					// set end for text read, end indicates termination symbol $
					suffixTreeAppl.oo.setEnd(end);

					// reset text window for the next cycle
					start = end + 1;
					end = text.indexOf(TERMINATOR, start);
				}
			} else {
				LOGGER.warning("Did not find terminator char: " + TERMINATOR);
			}

			// construct the JSON output and write it to the output port if
			// connected
			final OutputPort jsonOut = this.getOutputPorts().get(OUTPUT_JSON_ID);
			if (jsonOut.isConnected()) {
				writeJsonOutput(suffixTreeAppl, jsonOut);
			} else {
				LOGGER.info("No port for json connected, not producing json output.");
			}
			// construct the XML output and write it to the output port if
			// connected
			final OutputPort xmlOut = this.getOutputPorts().get(OUTPUT_XML_ID);
			if (xmlOut.isConnected()) {
				final String output = GeneralisedSuffixTreeMain.persistSuffixTreeToXmlString(suffixTreeAppl);
				xmlOut.outputToAllBytePipes(output.getBytes());
			} else {
				LOGGER.info("No port for xml connected, not producing xml output.");
			}
			// writes output of a list of labels, one label on each line
			final OutputPort listOut = this.getOutputPorts().get(OUTPUT_LIST_ID);
			if (listOut.isConnected()) {
				final ResultToLabelListListener listener = new ResultToLabelListListener(suffixTreeAppl);
				TreeWalker.walk(suffixTreeAppl.getRoot(), suffixTreeAppl, listener);
				final Set<String> labels = listener.getLabels();
				for (String label : labels) {
					listOut.outputToAllCharPipes(label + "\n");
				}
			} else {
				LOGGER.info("No port for plain text label list connected, output skipped.");
			}
			// writes output of a list of labels with frequencies, one label per
			// line
			final OutputPort freqListOut = this.getOutputPorts().get(OUTPUT_FREQ_LIST_ID);
			if (freqListOut.isConnected()) {
				writeLabelFrequencyListOutput(suffixTreeAppl, freqListOut);
			} else {
				LOGGER.info("No port for label frequency list connecte, output skipped.");
			}

		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			this.closeAllOutputs();
		}

		return result;
	}

	/**
	 * Reads a list of units provided by the KWIP module. Expects to read a
	 * newline separated list of Integers from the provided InputPort
	 * 
	 * @param unitsPort
	 *            the InputPort to read the newline separated list of units
	 *            from. (Produced by the KWIP module)
	 * @return An array of Integers that are the unit numbers read. An empty
	 *         array if none were read.
	 * @throws NumberFormatException
	 *             If a line in the input could not be parsed as an Integer
	 */
	private ArrayList<Integer> readUnitListFromInput(InputPort unitsPort) throws Exception {
		ArrayList<Integer> unitsList = new ArrayList<Integer>();

		final String[] inputStrings = NEWLINE_PATTERN.split(readStringFromInputPort(unitsPort));
		for (String str : inputStrings) {
			unitsList.add(Integer.parseInt(str));
		}

		return unitsList;
	}

	/**
	 * Genereates JSON output for the suffixTree and continually writes it to
	 * all char pipes connected to the json OutputPort
	 * 
	 * @param suffixTreeAppl
	 *            the suffixTree to write
	 * @param outputPort
	 *            the OutputPort to write to
	 * @throws IOException
	 */
	private void writeJsonOutput(SuffixTreeAppl suffixTreeAppl, OutputPort outputPort) throws IOException {
		// Initialize and use a new TreeWalkerListener, that directly writes to
		// the connected outputPort
		final ResultToJsonListener listener = new ResultToJsonListener(suffixTreeAppl, outputPort);
		TreeWalker.walk(suffixTreeAppl.getRoot(), suffixTreeAppl, listener);

		// close the listener
		listener.finishWriting();
	}

	/**
	 * Writes a list of labels for the provided suffix tree to the output port
	 * followed by frequencies for these labels.
	 * 
	 * @param suffixTreeAppl
	 *            the tree to get the labels and frequencies from
	 * @param outputPort
	 *            the output port to write to
	 * @throws IOException
	 */
	private void writeLabelFrequencyListOutput(SuffixTreeAppl suffixTreeAppl, OutputPort outputPort)
			throws IOException {
		// traverse the tree to get a map for output
		final ResultToLabelFreqListListener listener = new ResultToLabelFreqListListener(suffixTreeAppl);
		TreeWalker.walk(suffixTreeAppl.getRoot(), suffixTreeAppl, listener);
		final Map<String, List<Integer>> labelsToFrequencies = listener.getLabelsToFrequencies();
		// construcht output line by line
		final StringBuilder sb = new StringBuilder();
		List<Integer> frequencies = null;
		for (String label : labelsToFrequencies.keySet()) {
			sb.append(label);
			sb.append(" ");
			frequencies = labelsToFrequencies.get(label);
			for (Integer frequency : frequencies) {
				sb.append(frequency);
				sb.append(" ");
			}
			sb.setCharAt(sb.length() - 1, '\n');
			// output the line and reset string builder
			outputPort.outputToAllCharPipes(sb.toString());
			sb.setLength(0);
		}
	}

	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		super.applyProperties();
	}
}
