package modules.suffixTreeModuleWrapper;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.suffixTreeV2.GST;
import modules.suffixTreeV2.ResultEdgeSegmentsListener;
import modules.suffixTreeV2.ResultLabelListListener;
import modules.suffixTreeV2.SuffixTree;
import modules.suffixTreeV2.TreeWalker;

import common.parallelization.CallbackReceiver;

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
	private static final String INPUT_TYPE_CONTEXT_ID = "type context nrs";
	private static final String INPUT_TYPE_CONTEXT_NRS_DESC = "[text/plain] Takes a list of numbers of available type contexts from the KWIP result";
//	private static final String OUTPUT_JSON_ID = "json";
//	private static final String OUTPUT_JSON_DESC = "[text/json] A json representation of the tree build, suitable for clustering.";
//	private static final String OUTPUT_XML_ID = "xml";
//	private static final String OUTPUT_XML_DESC = "[bytestream] An xml representation of the tree build, suitbale for clustering.";
	private static final String OUTPUT_LIST_ID = "label list";
	private static final String OUTPUT_LIST_DESC = "[text/plain] A list of labels separated by newline";
//	private static final String OUTPUT_LABEL_DATA_ID = "label data";
//	private static final String OUTPUT_LABEL_DATA_DESC = "[text/csv] Prints a csv table with label information.";
	private static final String OUTPUT_DOT_FILE_ID = "dot file";
	private static final String OUTPUT_DOT_FILE_DESC = "Prints a graphical representation of the tree as a graphviz .dot file.";
//	private static final String OUTPUT_SUCCESSORS_MATRIX_ID = "successor label matrix";
//	private static final String OUTPUT_SUCCESSORS_MATRIX_DESC = "[text/csv] A matrix with labels on the y-axis, successor strings on the x-axis and counts in the field.";
	private static final String OUTPUT_EDGE_SEGMENTS_ID = "edge segments";
	private static final String OUTPUT_EDGE_SEGMENTS_DESC = "For each input text the output is that path in the tree split into it's edges.";

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

		InputPort inputUnitsPort = new InputPort(INPUT_TYPE_CONTEXT_ID, INPUT_TYPE_CONTEXT_NRS_DESC, this);
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
		
		boolean result = true;
		
		try {
		
			// read in the list of type context end numbers if the port is connected, else leave it null
			List<Integer> contextNrs = null;
			final InputPort contextNrsIn = this.getInputPorts().get(INPUT_TYPE_CONTEXT_ID);
			if(contextNrsIn.isConnected()) {
				contextNrs = new ArrayList<Integer>();
				final BufferedReader contextNrsReader = new BufferedReader(contextNrsIn.getInputReader());
				String line = null;
				
				while ((line = contextNrsReader.readLine()) != null) {
					contextNrs.add(Integer.parseInt(line));
				}
			}
			
			// actually build the tree
			final BufferedReader textReader = new BufferedReader(this.getInputPorts().get(INPUT_TEXT_ID).getInputReader());
			final SuffixTree suffixTree = GST.buildGST(textReader, contextNrs);
			
			// output a simple list of labels
			final OutputPort labelsOut = this.getOutputPorts().get(OUTPUT_LIST_ID);
			if (labelsOut.isConnected()) {
				final ResultLabelListListener listener = new ResultLabelListListener(suffixTree);
				TreeWalker.walk(suffixTree.getRoot(), suffixTree, listener);

				for(String label : listener.getLabels()) {
					labelsOut.outputToAllCharPipes(label + System.lineSeparator());
				}
			}
			
			// output a graphical representation as a graphviz .dot file
			final OutputPort dotOut = this.getOutputPorts().get(OUTPUT_DOT_FILE_ID);
			if (dotOut.isConnected()) {
				final CharPipe dotOutPipe = (CharPipe) dotOut.getPipes().get(CharPipe.class).get(0);
				final PrintWriter writer = new PrintWriter(dotOutPipe.getOutput());
				suffixTree.printTree(writer);
			}
			
			// output a list of edge segments
			final OutputPort edgeSegmentsOut = this.getOutputPorts().get(OUTPUT_EDGE_SEGMENTS_ID);
			if (edgeSegmentsOut.isConnected()) {
				final ResultEdgeSegmentsListener listener = new ResultEdgeSegmentsListener(suffixTree, edgeSegmentsOut);
				TreeWalker.walk(suffixTree.getRoot(), suffixTree, listener);
				if (!listener.hasCompleted()) {
					throw new IllegalStateException("Listener did not finish correctly. Result may be wrong.");
				}
			}

		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			this.closeAllOutputs();
		}
		
		return result;
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

		OutputPort outputDotFilePort = new OutputPort(OUTPUT_DOT_FILE_ID, OUTPUT_DOT_FILE_DESC, this);
		outputDotFilePort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputDotFilePort);

//		OutputPort outputSuccessorsMatrixPort = new OutputPort(OUTPUT_SUCCESSORS_MATRIX_ID,
//				OUTPUT_SUCCESSORS_MATRIX_DESC, this);
//		outputSuccessorsMatrixPort.addSupportedPipe(CharPipe.class);
//		super.addOutputPort(outputSuccessorsMatrixPort);
		
		OutputPort outputEdgeSegmentsPort = new OutputPort(OUTPUT_EDGE_SEGMENTS_ID, OUTPUT_EDGE_SEGMENTS_DESC, this);
		outputEdgeSegmentsPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputEdgeSegmentsPort);
	}

}
