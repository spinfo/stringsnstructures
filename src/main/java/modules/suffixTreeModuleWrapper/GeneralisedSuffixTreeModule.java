package modules.suffixTreeModuleWrapper;

import java.io.IOException;
import java.util.ArrayList;
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
import modules.suffixTree.applications.ResultToJsonListener;
import modules.suffixTree.applications.ResultToLabelListListener;
import modules.suffixTree.applications.ResutlToGstLabelDataListener;
import modules.suffixTree.applications.SuffixTreeAppl;
import modules.suffixTree.applications.TreeWalker;
import modules.suffixTree.main.GeneralisedSuffixTreeMain;
import modules.suffixTree.node.End;
import modules.suffixTree.node.ExtActivePoint;
import modules.suffixTree.node.nodeFactory.GeneralisedSuffixTreeNodeFactory;
import common.parallelization.CallbackReceiver;
import models.GstLabelData;

/**
 * Module Reads from KWIP modules output into a suffix tree. Constructs a
 * representation of that tree, that can be used as input for clustering.
 * 
 * Alternatively reads a simple String of '$'-separated sentences and does the
 * same.
 * 
 * Alternative outputs are available: Either a plain list of labels or one with
 * added information for the label's occurences (e.g. child count).
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
	private static final String OUTPUT_LABEL_DATA_ID = "label data";
	private static final String OUTPUT_LABEL_DATA_DESC = "[text/csv] Prints a csv table with label information.";

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

		this.setupOutputPorts();
	}

	@Override
	public boolean process() throws Exception {
		boolean result = true;

		// Ports for output the production of which will alter the tree.
		// NOTE: Only one of these outputs can be chosen.
		// We check this early before the tree is produced as that may take some
		// time.
		final OutputPort jsonOut = this.getOutputPorts().get(OUTPUT_JSON_ID);
		final OutputPort xmlOut = this.getOutputPorts().get(OUTPUT_XML_ID);
		final OutputPort labelDataOut = this.getOutputPorts().get(OUTPUT_LABEL_DATA_ID);

		int treeModifyingOutputs = 0;
		if (jsonOut.isConnected()) treeModifyingOutputs += 1;
		if (xmlOut.isConnected()) treeModifyingOutputs += 1;
		if (labelDataOut.isConnected()) treeModifyingOutputs += 1;
		if (treeModifyingOutputs > 1) throw new Exception("Only one of these Outputs can be chosen: xml, json, label-data.");

		try {
			// take the current time
			long startTime = System.nanoTime();

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

			// stop time taken for building the tree
			long treeFinished = System.nanoTime();

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
			// construct the JSON output
			if (jsonOut.isConnected()) {
				writeJsonOutput(suffixTreeAppl, jsonOut);
			}
			// construct the XML output
			if (xmlOut.isConnected()) {
				final String output = GeneralisedSuffixTreeMain.persistSuffixTreeToXmlString(suffixTreeAppl);
				xmlOut.outputToAllBytePipes(output.getBytes());
			} else {
				LOGGER.info("No port for xml connected, not producing xml output.");
			}
			// construct the label-data output
			if (labelDataOut.isConnected()) {
				final ResutlToGstLabelDataListener listener = new ResutlToGstLabelDataListener(suffixTreeAppl);
				TreeWalker.walk(suffixTreeAppl.getRoot(), suffixTreeAppl, listener);
				this.writeGstLabelData(listener.getLabelsToGstData(), labelDataOut);
			} else {
				LOGGER.info("No port for label data connected, output skipped.");
			}

			// log time taken for building the tree and generating output
			long timeTaken = treeFinished - startTime;
			LOGGER.info("Building the tree took: " + timeTaken + "ns (~ " + (timeTaken / 1000000000) + "s)");
			timeTaken = System.nanoTime() - treeFinished;
			LOGGER.info("Writing output took: " + timeTaken + "ns (~ " + (timeTaken / 1000000000) + "s)");

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
	private void writeJsonOutput(SuffixTreeAppl suffixTreeAppl, OutputPort outputPort) throws Exception {
		// Initialize and use a new TreeWalkerListener, that directly writes to
		// the connected outputPort
		final ResultToJsonListener listener = new ResultToJsonListener(suffixTreeAppl, outputPort);
		TreeWalker.walk(suffixTreeAppl.getRoot(), suffixTreeAppl, listener);

		// close the listener
		listener.finishWriting();
	}

	private void writeGstLabelData(final Map<String, GstLabelData> labelsToData, OutputPort outputPort)
			throws IOException {
		final StringBuilder sb = new StringBuilder();
		GstLabelData data = null;
		// output the header
		sb.append(GstLabelData.getCsvHeader());
		sb.append("\n");
		// output the labels line by line
		for (final String label : labelsToData.keySet()) {
			data = labelsToData.get(label);
			data.toCsv(sb);
			sb.append("\n");

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

	// this is normally done in the constructor, but was moved here to
	// remove clutter from it
	private void setupOutputPorts() {
		OutputPort outputJsonPort = new OutputPort(OUTPUT_JSON_ID, OUTPUT_JSON_DESC, this);
		outputJsonPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputJsonPort);

		OutputPort outputXmlPort = new OutputPort(OUTPUT_XML_ID, OUTPUT_XML_DESC, this);
		outputXmlPort.addSupportedPipe(BytePipe.class);
		super.addOutputPort(outputXmlPort);

		OutputPort outputListPort = new OutputPort(OUTPUT_LIST_ID, OUTPUT_LIST_DESC, this);
		outputListPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputListPort);

		OutputPort outputLabelDataPort = new OutputPort(OUTPUT_LABEL_DATA_ID, OUTPUT_LABEL_DATA_DESC, this);
		outputLabelDataPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputLabelDataPort);
	}
}
