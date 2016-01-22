package modules.suffixTree.applications;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import java.util.logging.Logger;

import com.google.gson.stream.JsonWriter;

import modules.OutputPort;
import modules.suffixTree.SuffixTree;
import modules.suffixTree.node.GeneralisedSuffixTreeNode;
import modules.suffixTree.node.TextStartPosInfo;

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

	// the suffixTree that this class will serialize
	private final SuffixTree suffixTree;

	// variables needed internally to write the Json representation
	private final ByteArrayOutputStream outputStream;
	private JsonWriter writer;
	private boolean wroteBegin = false;

	public ResultToJsonListener(SuffixTree suffixTree, OutputPort outputPort) {
		this.suffixTree = suffixTree;
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
	 * Before any node's are processed this ensures that the beginning of
	 * the tree is written.
	 */
	@Override
	public void entryaction(int nodeNr, int level) throws IOException {
		if (!this.wroteBegin)
			writeBegin();
	}

	/**
	 * Generates representation objects for the suffix tree's node and adds them
	 * to the suffix tree representation.
	 */
	@Override
	public void exitaction(int nodeNr, int level) throws IOException {

		// Output the node's label, root gets a blank label
		final String label;
		if (nodeNr == suffixTree.getRoot()) {
			label = "";
		} else {
			label = suffixTree.edgeString(nodeNr);
		}

		// For the rest of the information we need to retrieve the node
		final GeneralisedSuffixTreeNode node = ((GeneralisedSuffixTreeNode) suffixTree.nodes[nodeNr]);
		final ArrayList<TextStartPosInfo> nodeList = node.getStartPositionOfSuffix();
		final int frequency = nodeList.size();

		// output the beginning of the NodeRepresentation
		writer.beginObject();
		writer.name("number").value(nodeNr);
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
		writer.name("unitCount").value(suffixTree.unitCount);
		writer.name("nodeCount").value(suffixTree.getCurrentNode());
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
