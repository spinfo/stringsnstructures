package modules.suffixTreeModuleWrapper;

import java.util.Properties;
import java.util.logging.Logger;

import modules.BytePipe;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.suffixTree.suffixMain.GeneralisedSuffixTreeMain;
import modules.suffixTree.suffixTree.applications.ResultSuffixTreeNodeStack;
import modules.suffixTree.suffixTree.applications.ResultToRepresentationListener;
import modules.suffixTree.suffixTree.applications.SuffixTreeAppl;
import modules.suffixTree.suffixTree.applications.TreeWalker;
import modules.suffixTree.suffixTree.node.activePoint.ExtActivePoint;
import modules.suffixTree.suffixTree.node.info.End;
import modules.suffixTree.suffixTree.node.nodeFactory.GeneralisedSuffixTreeNodeFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.parallelization.CallbackReceiver;

/**
 * Work in Progress.
 * 
 * Currently Reads KWIP processed text only, outputs a generalised suffix tree
 * based on the text. Does not handle units and types at the moment.
 * 
 * @author David Neugebauer
 */
public class GeneralisedSuffixTreeModule extends modules.ModuleImpl {

	private static final Logger LOGGER = Logger.getLogger(GeneralisedSuffixTreeMain.class.getName());

	// Variables for the module
	private static final String MODULE_NAME = "GeneralisedSuffixTreeModule";
	private static final String MODULE_DESCRIPTION = "Reads from KWIP modules output and constructs output suitable for clustering.";

	// Variables describing I/O
	private static final String INPUT_TEXT_ID = "plain";
	private static final String INPUT_TEXT_DESC = "[text/plain] Takes a plaintext representation of the KWIP result.";
	private static final String OUTPUT_JSON_ID = "json";
	private static final String OUTPUT_JSON_DESC = "[text/json] A json representation of the tree build, suitable for clustering.";
	private static final String OUTPUT_XML_ID = "xml";
	private static final String OUTPUT_XML_DESC = "[bytestream] An xml representation of the tree build, suitbale for clustering.";

	// Variables for input processing
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

		// Setup I/O, reads from char input produced by KWIP.
		// json output is to a CharPipe as expected, but
		// xml Output is to a BytePipe for compatibility reasons to the
		// clustering module
		InputPort inputTextPort = new InputPort(INPUT_TEXT_ID, INPUT_TEXT_DESC, this);
		inputTextPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputTextPort);

		OutputPort outputJsonPort = new OutputPort(OUTPUT_JSON_ID, OUTPUT_JSON_DESC, this);
		outputJsonPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputJsonPort);

		OutputPort outputXmlPort = new OutputPort(OUTPUT_XML_ID, OUTPUT_XML_DESC, this);
		outputXmlPort.addSupportedPipe(BytePipe.class);
		super.addOutputPort(outputXmlPort);
	}

	@Override
	public boolean process() throws Exception {
		try {
			// read the whole text once, neccessary to know the text's length
			final String text = readStringFromInputPort(this.getInputPorts().get(INPUT_TEXT_ID));

			// The suffix tree used to read the input is a generalised
			// suffix
			// tree for a text of the length of the input string
			final SuffixTreeAppl suffixTreeAppl = new SuffixTreeAppl(text.length(),
					new GeneralisedSuffixTreeNodeFactory());

			// set some variables to regulate flow in SuffixTree classes
			suffixTreeAppl.unit = 0;
			suffixTreeAppl.oo = new End(Integer.MAX_VALUE / 2);

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
				start = end + 1;
				end = text.indexOf(TERMINATOR, start);
				while (end != -1) {
					// each cycle represents a text read
					suffixTreeAppl.textNr++;

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

					// reset text window for the next cycle
					start = end + 1;
					end = text.indexOf(TERMINATOR, start);
				}
			} else {
				LOGGER.warning("Did not find terminator char: " + TERMINATOR);
			}

			// construct the JSON output and write it to the output port if connected
			final OutputPort jsonOut = this.getOutputPorts().get(OUTPUT_JSON_ID);
			if (jsonOut.isConnected()) {
				final String ouput = generateJsonOutput(suffixTreeAppl);
				jsonOut.outputToAllCharPipes(ouput);
			} else {
				LOGGER.info("No port for json connected, not producing json output.");
			}
			// construct the XML output and write it to the output port if connected
			final OutputPort xmlOut = this.getOutputPorts().get(OUTPUT_XML_ID);
			if (xmlOut.isConnected()) {
				final String output = GeneralisedSuffixTreeMain.persistSuffixTreeToXmlString(suffixTreeAppl);
				xmlOut.outputToAllBytePipes(output.getBytes());
			} else {
				LOGGER.info("No port for xml connected, not producing xml output.");
			}

			// no catch block, this should just crash on error
		} finally {
			this.closeAllOutputs();
		}

		return true;
	}

	/**
	 * Generated JSON output
	 * 
	 * @param suffixTreeAppl
	 *            suffix tree application instance
	 * @return JSON string
	 */
	private String generateJsonOutput(SuffixTreeAppl suffixTreeAppl) {
		// apparently this needs to be statically set for any result listener to
		// work correctly
		ResultSuffixTreeNodeStack.suffixTree = suffixTreeAppl;

		// build an object to hold a representation of the tree for output
		// and add it's nodes via a listener.
		final SuffixTreeRepresentation suffixTreeRepresentation = new SuffixTreeRepresentation();
		final ResultToRepresentationListener listener = new ResultToRepresentationListener(suffixTreeRepresentation);
		final TreeWalker treeWalker = new TreeWalker();
		suffixTreeRepresentation.setUnitCount(0);
		suffixTreeRepresentation.setNodeCount(suffixTreeAppl.getCurrentNode());
		treeWalker.walk(suffixTreeAppl.getRoot(), suffixTreeAppl, listener);

		// serialize the representation to JSON
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		final String output = gson.toJson(suffixTreeRepresentation);
		return output;
	}

	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		super.applyProperties();
	}
}
