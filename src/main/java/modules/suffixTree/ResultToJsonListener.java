package modules.suffixTree;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.stream.JsonWriter;

import modules.OutputPort;

public class ResultToJsonListener extends AbstractResultNodeStackListener {

	private static final Logger LOGGER = Logger.getLogger(ResultToJsonListener.class.getName());

	private static final String ENCODING = java.nio.charset.StandardCharsets.UTF_8.name();

	// the OutputPort to write to
	private final OutputPort outputPort;

	// the suffix tree this will work on
	private final BaseSuffixTree tree;

	// variables needed internally to write the Json representation
	private final ByteArrayOutputStream outputStream;
	private JsonWriter writer;
	private boolean wroteBegin = false;

	public ResultToJsonListener(BaseSuffixTree suffixTree, OutputPort outputPort) {
		super(suffixTree);

		this.tree = suffixTree;
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

	@Override
	public void process(int nodeNr, List<Node> path, int pathLength, int level) throws IOException {
		if (!this.wroteBegin) {
			writeBegin();
		}

		// get the node and label in question
		final Node node = tree.getNode(nodeNr);
		final String label = tree.edgeString(node);

		// output the beginning of the NodeRepresentation
		writer.beginObject();
		writer.name("number").value(nodeNr);
		writer.name("label").value(label);

		// frequency is determined by positions of leaves
		int frequency = 0;

		// write patternInfo Objects: Information about the whole input pattern
		// that the current label appeared in, as given by the node's leaves
		// positions
		writer.name("patternInfos").beginArray();
		for (Node leaf : node.getLeaves()) {
			for (NodePosition position : leaf.getPositions()) {
				writePatternInfo(leaf, position);
				frequency += 1;
			}
		}

		// if the node is itself a leaf node, further patternInfos are written
		// for it's positions
		if (node.isTerminal()) {
			for (NodePosition position : node.getPositions()) {
				writePatternInfo(node, position);
				frequency += 1;
			}
		}
		writer.endArray();

		writer.name("frequency").value(frequency);

		// end writing of the NodeRepresentation and flush everything to the
		// output port
		writer.endObject();
		this.flushOutput();
	}

	private void writePatternInfo(Node leaf, NodePosition position) throws IOException {
		writer.beginObject();

		writer.name("typeNr").value(position.getTypeContextNr());
		writer.name("patternNr").value(position.getTextNr());

		// write the index of the start of the path leading to this leaf's
		// position
		writer.name("startPos").value(position.getEnd() - leaf.getPathLength());

		writer.endObject();
	}

	/**
	 * Finalises writing of the SuffixTreeRepresentation, closes all ports
	 * connected and/or used internally.
	 * 
	 * @throws IOException
	 *             on error
	 */
	public void finishWriting() throws IOException {
		// finalise the node array and object begun in writeBegin()
		this.writer.endArray();
		this.writer.endObject();

		// write everything out
		this.flushOutput();

		// close all opened streams and ports
		this.outputStream.close();
		this.writer.close();
		this.outputPort.close();
	}

	/**
	 * Writes the beginning of the tree and notes that it has already been
	 * written, ensuring that this operation is only called once in the life
	 * cycle of this object
	 * 
	 * @throws IOException
	 *             on error
	 */
	private void writeBegin() throws IOException {
		if (wroteBegin) {
			return;
		}

		writer.beginObject();
		writer.name("unitCount").value(tree.getTypeContextsAmount());
		writer.name("nodeCount").value(tree.getNodeAmount());
		writer.name("nodes");
		writer.beginArray();
		flushOutput();

		wroteBegin = true;
	}

	/**
	 * used internally to output changes in the writer's stream to the
	 * outputPort
	 * 
	 * @throws IOException
	 */
	private void flushOutput() throws IOException {
		writer.flush();
		final String jsonOut = outputStream.toString(ENCODING);
		outputPort.outputToAllCharPipes(jsonOut);
		outputStream.reset();
	}

}
