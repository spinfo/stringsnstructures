package modules.suffixTreeModuleWrapper;

import java.io.BufferedReader;
import java.util.Properties;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.suffixTreeV2.BaseSuffixTree;
import modules.suffixTreeV2.GST;
import modules.suffixTreeV2.ResultLabelListListener;
import modules.suffixTreeV2.TreeWalker;

/**
 * Module Reads from KWIP modules output into a suffix tree. Constructs a
 * representation of that tree, that can be used as input for clustering.
 * 
 * Alternatively reads a simple String of '$'-separated sentences and does the
 * same.
 * 
 * Alternative outputs are available: Either a plain list of labels or one with
 * added information for the label's occurrences (e.g. child count).
 * 
 * @author David Neugebauer
 */
public class GeneralisedSuffixTreeModuleV2 extends modules.ModuleImpl {

//	private static final Logger LOGGER = Logger.getLogger(GeneralisedSuffixTreeMain.class.getName());

	// Variables for the module
	private static final String MODULE_NAME = "GeneralisedSuffixTreeModule";
	private static final String MODULE_DESCRIPTION = "Module Rreads from KWIP modules output into a suffix tree. Constructs a "
			+ "representation of that tree, that can be used as input for clustering.";

	// Variables describing I/O
	private static final String INPUT_TEXT_ID = "plain";
	private static final String INPUT_TEXT_DESC = "[text/plain] Takes a plaintext representation of the KWIP result.";
	private static final String INPUT_UNITS_ID = "units";
	private static final String INPUT_UNITS_DESC = "[text/plain] Takes a unit list (numbers of available types) from the KWIP result";
//	private static final String OUTPUT_JSON_ID = "json";
//	private static final String OUTPUT_JSON_DESC = "[text/json] A json representation of the tree build, suitable for clustering.";
//	private static final String OUTPUT_XML_ID = "xml";
//	private static final String OUTPUT_XML_DESC = "[bytestream] An xml representation of the tree build, suitbale for clustering.";
	private static final String OUTPUT_LIST_ID = "label list";
	private static final String OUTPUT_LIST_DESC = "[text/plain] A list of labels separated by newline";
//	private static final String OUTPUT_LABEL_DATA_ID = "label data";
//	private static final String OUTPUT_LABEL_DATA_DESC = "[text/csv] Prints a csv table with label information.";
//	private static final String OUTPUT_DOT_FILE_ID = "dot file";
//	private static final String OUTPUT_DOT_FILE_DESC = "Prints a graphical representation of the tree as a .dot file.";
//	private static final String OUTPUT_SUCCESSORS_MATRIX_ID = "successor label matrix";
//	private static final String OUTPUT_SUCCESSORS_MATRIX_DESC = "[text/csv] A matrix with labels on the y-axis, successor strings on the x-axis and counts in the field.";

	// Container to hold units if provided
//	private ArrayList<Integer> unitList = null;

	// Variables for input processing
//	private static final Pattern NEWLINE_PATTERN = Pattern.compile("\r\n|\n|\r");
//	private static final char TERMINATOR = '$';

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
	public GeneralisedSuffixTreeModuleV2(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
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

	/**
	 * This builds a generalised suffix tree and constructs output for all
	 * connected output ports
	 */
	@Override
	public boolean process() throws Exception {

		BufferedReader textReader = new BufferedReader(super.getInputPorts().get(INPUT_TEXT_ID).getInputReader());
		
		BaseSuffixTree suffixTree = GST.buildGST(textReader);
		
		// output a simple list of labels
		final OutputPort labelsOut = this.getOutputPorts().get(OUTPUT_LIST_ID);
		if (labelsOut.isConnected()) {
			final ResultLabelListListener listener = new ResultLabelListListener(suffixTree);
			TreeWalker.walk(suffixTree.getRoot(), suffixTree, listener);
			for(String label : listener.getLabels()) {
				labelsOut.outputToAllCharPipes(label + System.lineSeparator());
			}
			labelsOut.close();
		}
		
		return true;
	}

	// this is normally done in the constructor, but was moved here to
	// remove clutter from it
	private void setupOutputPorts() {
//		OutputPort outputJsonPort = new OutputPort(OUTPUT_JSON_ID, OUTPUT_JSON_DESC, this);
//		outputJsonPort.addSupportedPipe(CharPipe.class);
//		super.addOutputPort(outputJsonPort);
//
//		OutputPort outputXmlPort = new OutputPort(OUTPUT_XML_ID, OUTPUT_XML_DESC, this);
//		outputXmlPort.addSupportedPipe(BytePipe.class);
//		super.addOutputPort(outputXmlPort);

		OutputPort outputListPort = new OutputPort(OUTPUT_LIST_ID, OUTPUT_LIST_DESC, this);
		outputListPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputListPort);

//		OutputPort outputLabelDataPort = new OutputPort(OUTPUT_LABEL_DATA_ID, OUTPUT_LABEL_DATA_DESC, this);
//		outputLabelDataPort.addSupportedPipe(CharPipe.class);
//		super.addOutputPort(outputLabelDataPort);
//
//		OutputPort outputDotFilePort = new OutputPort(OUTPUT_DOT_FILE_ID, OUTPUT_DOT_FILE_DESC, this);
//		outputDotFilePort.addSupportedPipe(CharPipe.class);
//		super.addOutputPort(outputDotFilePort);
//
//		OutputPort outputSuccessorsMatrixPort = new OutputPort(OUTPUT_SUCCESSORS_MATRIX_ID,
//				OUTPUT_SUCCESSORS_MATRIX_DESC, this);
//		outputSuccessorsMatrixPort.addSupportedPipe(CharPipe.class);
//		super.addOutputPort(outputSuccessorsMatrixPort);
	}

}
