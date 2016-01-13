package modules.suffixTree.suffixTree.applications;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import java.util.logging.Logger;

import com.google.gson.stream.JsonWriter;

import modules.OutputPort;
import modules.suffixTree.suffixTree.node.GeneralisedSuffixTreeNode;
import modules.suffixTree.suffixTree.node.textStartPosInfo.TextStartPosInfo;

/**
 * A TreeWalkerListener that writes a JSON representation of a
 * <code>SuffixTree</code> directly to an <code>OutputPort</code>.
 * 
 * The contract is, that this object writes json that can be deserialized to an
 * instance of <code>SuffixTreeRepresentation</code>.
 */
public class ResultToJsonListener implements ITreeWalkerListener {

	private static final Logger LOGGER = Logger.getLogger(ResultToJsonListener.class.getName());

	private static final String ENCODING = java.nio.charset.StandardCharsets.UTF_8.name();

	// the OutputPort to write to
	private final OutputPort outputPort;

	// a node stack that the listener can collect nodes on
	private final ResultSuffixTreeNodeStack nodeStack;

	// the suffixTreeAppl that this class will serialize as retrieved from the
	// nodeStack
	private final SuffixTreeAppl suffixTreeAppl;

	// variables needed internally to write the Json representation
	private final ByteArrayOutputStream outputStream;
	private JsonWriter writer;
	private boolean wroteBegin = false;

	public ResultToJsonListener(ResultSuffixTreeNodeStack nodeStack, OutputPort outputPort) {
		this.nodeStack = nodeStack;
		this.suffixTreeAppl = this.nodeStack.getSuffixTreeAppl();
		this.outputPort = outputPort;

		this.outputStream = new ByteArrayOutputStream();
		try {
			this.writer = new JsonWriter(new OutputStreamWriter(this.outputStream, ENCODING));
		} catch (UnsupportedEncodingException e) {
			LOGGER.severe("Encoding " + ENCODING + "not supported.");
			e.printStackTrace();
		}
		this.writer.setIndent("  ");
	}

	/**
	 * This simply pushes the node number of the current node on a stack for
	 * later processing on the exitaction.
	 * 
	 * The reason for this seems to be, that the ResultSuffixTreeNodeStack can
	 * elegantly get a representation of a node's label on the exitaction.
	 * 
	 * Before any node's are processed this also ensures that the beginning of
	 * the tree was already written.
	 */
	@Override
	public void entryaction(int nodeNr, int level) throws IOException {
		if (!this.wroteBegin)
			writeBegin();

		this.nodeStack.push(nodeNr);
	}

	/**
	 * Generates representation objects for the suffix tree's node and adds them
	 * to the suffix tree representation.
	 */
	@Override
	public void exitaction(int nodeNr, int level) throws IOException {

		// the node's label and identifying number are simply retrieved from the
		// node stack. The identifying nodeNr is strictly necessary, so we do
		// not catch the possible EmptyStackException at this point
		final String label = this.nodeStack.writeStack();
		int stackedNodeNr = this.nodeStack.pop();

		if (stackedNodeNr != nodeNr) {
			LOGGER.severe(
					"Stacked node nr does not match the node number given by the TreeWalker. Maybe something went wrong in the nodeStack?");
		}

		// For the rest of the information we need to retrieve the node
		final GeneralisedSuffixTreeNode node = ((GeneralisedSuffixTreeNode) suffixTreeAppl.nodes[stackedNodeNr]);
		final ArrayList<TextStartPosInfo> nodeList = node.getStartPositionOfSuffix();
		final int frequency = nodeList.size();

		// TODO write a comment explaining this... (Copied from
		// ResultToXmlListener)
		if (!this.nodeStack.empty()) {
			final int mother = this.nodeStack.peek();
			final GeneralisedSuffixTreeNode motherNode = ((GeneralisedSuffixTreeNode) suffixTreeAppl.nodes[mother]);
			motherNode.getStartPositionOfSuffix().addAll(nodeList);
		}

		// output the beginning of the NodeRepresentation
		writer.beginObject();
		writer.name("number").value(stackedNodeNr);
		writer.name("label").value(label);
		writer.name("frequency").value(frequency);

		// construct, fill and add output objects for the node's pattern infos
		writer.name("patternInfos").beginArray();
		for (int i = 0; i < frequency; i++) {
			final TextStartPosInfo info = nodeList.get(i);

			writer.beginObject();
			writer.name("typeNr").value(info.unit);
			writer.name("patternNr").value(info.text);
			writer.name("startPos").value(info.startPositionOfSuffix);
			writer.endObject();
		}
		writer.endArray();

		// end writing of the NodeRepresentation and flush everything to the
		// outputport
		writer.endObject();
		this.flushOutput();
	}

	/**
	 * Finalizes writing of the SuffixTreeRepresentation, closes all ports
	 * connected and/or used internally.
	 */
	public void finishWriting() throws IOException {
		// finalize the node array and object begun in writeBegin()
		this.writer.endArray();
		this.writer.endObject();
		
		// write everything out
		this.flushOutput();
		
		// close all opened streams and ports
		this.outputStream.close();
		this.writer.close();
		this.outputPort.close();
	}

	// Writes the beginning of the tree and notes that it has already been
	// written, ensuring that this operation is only called once in the
	// life cycle of this object
	private void writeBegin() throws IOException {
		if (wroteBegin) {
			return;
		}

		writer.beginObject();
		writer.name("unitCount").value(suffixTreeAppl.unitCount);
		writer.name("nodeCount").value(suffixTreeAppl.getCurrentNode());
		writer.name("nodes");
		writer.beginArray();
		flushOutput();

		wroteBegin = true;
	}

	// used internally to output changes in the writer's stream to the
	// outputPort
	private void flushOutput() throws IOException {
		writer.flush();
		final String jsonOut = outputStream.toString(ENCODING);
		outputPort.outputToAllCharPipes(jsonOut);
		outputStream.reset();
	}

}
