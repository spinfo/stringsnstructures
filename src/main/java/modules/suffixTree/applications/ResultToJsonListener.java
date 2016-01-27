package modules.suffixTree.applications;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
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
 * 
 * Extends AbstractNodeStackListener such that information on the child nodes is
 * available on the exit action.
 */
public class ResultToJsonListener extends AbstractNodeStackListener implements ITreeWalkerListener {

	private static final Logger LOGGER = Logger.getLogger(ResultToJsonListener.class.getName());

	private static final String ENCODING = java.nio.charset.StandardCharsets.UTF_8.name();

	// the OutputPort to write to
	private final OutputPort outputPort;
	
	// the suffix tree this will work on
	private final SuffixTree suffixTree;

	// variables needed internally to write the Json representation
	private final ByteArrayOutputStream outputStream;
	private JsonWriter writer;
	private boolean wroteBegin = false;

	public ResultToJsonListener(SuffixTree suffixTree, OutputPort outputPort) {
		super(suffixTree);
		
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
	public void entryaction(int nodeNr, int level) throws Exception {
		super.entryaction(nodeNr, level);

		if (!this.wroteBegin) {
			writeBegin();
		}
	}

	/**
	 * Generates representation objects for the suffix tree's node and adds them
	 * to the suffix tree representation.
	 */
	@Override
	public void exitaction(int nodeNr, int level) throws Exception {
		// get the current node and node label from the superclass
		// that node matches the one identified by param nodeNr
		final String label = super.getCurrentNodeLabel();
		final GeneralisedSuffixTreeNode node = super.getCurrentNode();
		
		// Retrieve the position information. The superclass has made sure that
		// the position information of all children is included in this list
		final List<TextStartPosInfo> nodeList = node.getStartPositionInformation();
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
		
		// make sure that the node stack is handled correctly
		super.exitaction(nodeNr, level);
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
