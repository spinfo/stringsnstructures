package modules.tree_building.suffixTreeModuleWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import common.XmlPrintWriter;
import common.parallelization.CallbackReceiver;
import models.GstLabelData;
import modules.BytePipe;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.transitionNetwork.TransitionNetwork;
import modules.tree_building.suffixTree.GST;
import modules.tree_building.suffixTree.ResultEdgeSegmentsListener;
import modules.tree_building.suffixTree.ResultToFiniteStateMachineListener;
import modules.tree_building.suffixTree.ResultLabelListListener;
import modules.tree_building.suffixTree.ResultToGstLabelDataListener;
import modules.tree_building.suffixTree.ResultToJsonListener;
import modules.tree_building.suffixTree.ResultToXmlListener;
import modules.tree_building.suffixTree.SuffixTree;
import modules.tree_building.suffixTree.TreeWalker;

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
public class GeneralisedSuffixTreeModule extends modules.ModuleImpl {

	// Variables for the module
	private static final String MODULE_NAME = "GeneralisedSuffixTreeModule";
	private static final String MODULE_DESCRIPTION = "Module Rreads from KWIP modules output into a suffix tree. Constructs a "
			+ "representation of that tree, that can be used as input for clustering.";

	// Variables describing I/O
	private static final String INPUT_TEXT_ID = "plain";
	private static final String INPUT_TEXT_DESC = "[text/plain] Takes a plaintext representation of the KWIP result.";
	private static final String INPUT_TYPE_CONTEXT_ID = "type context nrs";
	private static final String INPUT_TYPE_CONTEXT_NRS_DESC = "[text/plain] Takes a list of numbers of available type contexts from the KWIP result";
	private static final String OUTPUT_JSON_ID = "json";
	private static final String OUTPUT_JSON_DESC = "[text/json] A json representation of the tree build, suitable for clustering.";
	private static final String OUTPUT_XML_ID = "xml";
	private static final String OUTPUT_XML_DESC = "[bytestream] An xml representation of the tree build, suitbale for clustering.";
	private static final String OUTPUT_LIST_ID = "label list";
	private static final String OUTPUT_LIST_DESC = "[text/plain] A list of labels separated by newline";
	private static final String OUTPUT_LABEL_DATA_ID = "label data";
	private static final String OUTPUT_LABEL_DATA_DESC = "[text/csv] Prints a csv table with label information.";
	private static final String OUTPUT_DOT_FILE_ID = "dot file";
	private static final String OUTPUT_DOT_FILE_DESC = "Prints a graphical representation of the tree as a graphviz .dot file.";
	private static final String OUTPUT_EDGE_SEGMENTS_ID = "edge segments";
	private static final String OUTPUT_EDGE_SEGMENTS_DESC = "For each input text the output is that path in the tree split into it's edges.";

	private static final String OUTPUT_FOR_TN_ID = "tn";
	private static final String OUTPUT_FOR_TN_DESC = "[bytestream] A forTN representation of the tree build, suitbale for clustering.";

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

		// Setup I/O, reads from char input produced by KWIP.
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

			// read in the list of type context end numbers if the port is
			// connected, else leave it null
			List<Integer> contextNrs = null;
			final InputPort contextNrsIn = this.getInputPorts().get(INPUT_TYPE_CONTEXT_ID);
			if (contextNrsIn.isConnected()) {
				contextNrs = new ArrayList<Integer>();
				final BufferedReader contextNrsReader = new BufferedReader(contextNrsIn.getInputReader());
				String line = null;

				while ((line = contextNrsReader.readLine()) != null) {
					contextNrs.add(Integer.parseInt(line));
				}
			}

			// actually build the tree
			final BufferedReader textReader = new BufferedReader(
					this.getInputPorts().get(INPUT_TEXT_ID).getInputReader());
			final SuffixTree suffixTree = GST.buildGST(textReader, contextNrs);

			// output a simple list of labels
			final OutputPort labelsOut = this.getOutputPorts().get(OUTPUT_LIST_ID);
			if (labelsOut.isConnected()) {
				final ResultLabelListListener listener = new ResultLabelListListener(suffixTree);
				TreeWalker.walk(suffixTree.getRoot(), suffixTree, listener);

				for (String label : listener.getLabels()) {
					labelsOut.outputToAllCharPipes(label + System.lineSeparator());
				}
			}

			// output a graphical representation as a graphviz .dot file
			final OutputPort dotOut = this.getOutputPorts().get(OUTPUT_DOT_FILE_ID);
			if (dotOut.isConnected()) {
				final CharPipe dotOutPipe = (CharPipe) dotOut.getPipes().get(CharPipe.class).get(0);
				final PrintWriter writer = new PrintWriter(dotOutPipe.getOutput());
				suffixTree.printTree(writer);
				dotOut.close();
			}

			// output a list of edge segments
			final OutputPort edgeSegmentsOut = this.getOutputPorts().get(OUTPUT_EDGE_SEGMENTS_ID);
			if (edgeSegmentsOut.isConnected()) {
				final ResultEdgeSegmentsListener listener = new ResultEdgeSegmentsListener(suffixTree, edgeSegmentsOut);
				TreeWalker.walk(suffixTree.getRoot(), suffixTree, listener);
				if (!listener.hasCompleted()) {
					throw new IllegalStateException("Listener did not finish correctly. Result may be wrong.");
				}
				edgeSegmentsOut.close();
			}

			// output the transition network
			final OutputPort transitionNetworkOut = this.getOutputPorts().get(OUTPUT_FOR_TN_ID);
			if (transitionNetworkOut.isConnected()) {
				final ResultToFiniteStateMachineListener listener = new ResultToFiniteStateMachineListener(suffixTree);
				TreeWalker.walk(suffixTree.getRoot(), suffixTree, listener);
				TransitionNetwork tn = listener.getTN();
				tn.writeTN(transitionNetworkOut);
				transitionNetworkOut.close();
			}

			// output an XML-Representation of the tree
			final OutputPort xmlOut = this.getOutputPorts().get(OUTPUT_XML_ID);
			if (xmlOut.isConnected()) {
				final StringWriter sw = new StringWriter();
				final ResultToXmlListener listener = new ResultToXmlListener(suffixTree, new XmlPrintWriter(sw));
				TreeWalker.walk(suffixTree.getRoot(), suffixTree, listener);
				listener.finishWriting();
				xmlOut.outputToAllBytePipes(sw.toString().getBytes());
				xmlOut.close();
			}

			final OutputPort jsonOut = this.getOutputPorts().get(OUTPUT_JSON_ID);
			if (jsonOut.isConnected()) {
				final ResultToJsonListener listener = new ResultToJsonListener(suffixTree, jsonOut);
				TreeWalker.walk(suffixTree.getRoot(), suffixTree, listener);
				listener.finishWriting();
				jsonOut.close();
			}

			// output the label data csv table
			final OutputPort labelDataOut = this.getOutputPorts().get(OUTPUT_LABEL_DATA_ID);
			if (labelDataOut.isConnected()) {
				final ResultToGstLabelDataListener listener = new ResultToGstLabelDataListener(suffixTree);
				TreeWalker.walk(suffixTree.getRoot(), suffixTree, listener);
				writeGstLabelData(listener.getLabelsToGstData().values(), labelDataOut);
				labelDataOut.close();
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
		OutputPort outputJsonPort = new OutputPort(OUTPUT_JSON_ID, OUTPUT_JSON_DESC, this);
		outputJsonPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputJsonPort);

		// xml Output goes to a BytePipe for compatibility to the clustering
		// module
		OutputPort outputXmlPort = new OutputPort(OUTPUT_XML_ID, OUTPUT_XML_DESC, this);
		outputXmlPort.addSupportedPipe(BytePipe.class);
		super.addOutputPort(outputXmlPort);

		OutputPort outputListPort = new OutputPort(OUTPUT_LIST_ID, OUTPUT_LIST_DESC, this);
		outputListPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputListPort);

		OutputPort outputLabelDataPort = new OutputPort(OUTPUT_LABEL_DATA_ID, OUTPUT_LABEL_DATA_DESC, this);
		outputLabelDataPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputLabelDataPort);

		OutputPort outputDotFilePort = new OutputPort(OUTPUT_DOT_FILE_ID, OUTPUT_DOT_FILE_DESC, this);
		outputDotFilePort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputDotFilePort);

		OutputPort outputEdgeSegmentsPort = new OutputPort(OUTPUT_EDGE_SEGMENTS_ID, OUTPUT_EDGE_SEGMENTS_DESC, this);
		outputEdgeSegmentsPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputEdgeSegmentsPort);

		OutputPort outputForTnPort = new OutputPort(OUTPUT_FOR_TN_ID, OUTPUT_FOR_TN_DESC, this);
		outputForTnPort.addSupportedPipe(CharPipe.class);
		super.addOutputPort(outputForTnPort);

	}

	/**
	 * This just writes all entries in the provided set of GstLabelData objects
	 * to the specified output port's char pipes.
	 * 
	 * @param labelsToData
	 *            The data objects to write.
	 * @param outputPort
	 *            The output port to write to.
	 * @throws IOException
	 *             If something goes wrong with IO.
	 */
	private void writeGstLabelData(final Collection<GstLabelData> dataset, OutputPort outputPort) throws IOException {
		final StringBuilder sb = new StringBuilder();
		// output the header
		sb.append(GstLabelData.getCsvHeader());
		sb.append(System.lineSeparator());
		// output the labels line by line
		for (GstLabelData data : dataset) {
			data.toCsv(sb);
			sb.append(System.lineSeparator());

			// output the line and reset string builder
			outputPort.outputToAllCharPipes(sb.toString());
			sb.setLength(0);
		}
	}

}
