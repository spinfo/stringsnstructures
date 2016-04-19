package modules.suffixTreeV2;

import java.util.List;

import common.XmlPrintWriter;

public class ResultToXmlListener extends AbstractResultNodeStackListener {

	private final XmlPrintWriter out;

	private final BaseSuffixTree tree;
	
	private boolean wroteHeader = false;

	public ResultToXmlListener(BaseSuffixTree tree, XmlPrintWriter writer) {
		// call parent constructor to setup and handle the node stack
		super(tree);

		this.tree = tree;
		this.out = writer;
	}

	/**
	 * Outputs an xml representation of the node.
	 * 
	 * @param nodeNr
	 *            The number of the node currently processed.
	 * @param path
	 *            All nodes in the path leading to but not including the current
	 *            Node.
	 * @param pathLength
	 *            The amount of chars in the path's edge strings leading to the
	 *            current node.
	 * @param level
	 *            The node's level within the tree.
	 */
	@Override
	public void process(int nodeNr, List<Node> path, int pathLength, int level) {
		if (!wroteHeader) {
			writeHeader();
		}

		final Node node = tree.getNode(nodeNr);
		final String label = tree.edgeString(node);
		
		// open <node>
		out.printTag("node", true, 1, true);
		
		// write the node number
		out.printTag("number", true, 2, false);
		out.printInt(nodeNr);
		out.printTag("number", false, 0, true);
		
		// write the label
		out.printTag("label", true, 2, false);
		out.print(label);
		out.printTag("label", false, 0, true);
		
		// open <type>
		out.printTag("type", true, 2, true);
		
		// write <patternInfo>-Tags: Information about the whole input pattern
		// that the current label appeared in, as given by the node's leaves positions
		for(Node leaf : node.getLeaves()) {
			for(NodePosition position : leaf.getPositions()) {
				writePatternInfo(leaf, position);
			}
		}

		// if the node is itself a leaf node, further patternInfos are written for it's
		// positions
		if (node.isTerminal()) {
			for(NodePosition position: node.getPositions()) {
				writePatternInfo(node, position);
			}
		}
		
		// closing: </type></node>
		out.printTag("type", false, 2, true);
		out.printTag("node", false, 1, true);
	}
	
	private void writePatternInfo(Node leaf, NodePosition position) {
		out.printTag("patternInfo", true, 3, true);
		
		// write the id of the type context
		out.printTag("typeNr", true, 4, false);
		out.printInt(position.getTypeContextNr());
		out.printTag("typeNr", false, 0, true);
		
		// write the id of the input text
		out.printTag("pattern", true, 4, false);
		out.printInt(position.getTextNr());
		out.printTag("pattern", false, 0, true);
		
		// write the index of the start of the path lead to this leaf's position
		out.printTag("startpos", true, 4, false);
		out.printInt(position.getEnd() - leaf.getPathLength());
		out.printTag("startpos", false, 0, true);
		
		out.printTag("patternInfo", false, 3, true);
	}
	
	/**
	 * write the XML-Header
	 */
	private void writeHeader() {
		out.printTag("output", true, 0, true);
		out.printTag("units", true, 1, false);
		out.printInt(tree.getTypeContextsAmount());
		out.printTag("units", false, 0, true);

		out.printTag("nodes", true, 1, false);
		out.printInt(tree.getNodeAmount());
		out.printTag("nodes", false, 0, true);
		
		// set the flag such that this header is not written again
		wroteHeader = true;
	}
	
	/**
	 * Closes all tags as well as the provided writer. 
	 */
	public void finishWriting() {
		out.printTag("output", false, 0, true);
		out.close();
	}
	

}
