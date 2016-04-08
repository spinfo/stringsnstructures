package modules.suffixTreeV2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Stack;

import modules.OutputPort;

public class ResultEdgeSegmentsListener implements ITreeWalkerListener {

	// the separator is exposed read-only
	public static final String SEPARATOR = "|";

	private final BaseSuffixTree tree;

	private final OutputPort out;

	private final Stack<String> edges;

	public ResultEdgeSegmentsListener(BaseSuffixTree tree, OutputPort out) {
		this.tree = tree;
		this.out = out;
		this.edges = new Stack<String>();
	}

	/**
	 * On the entry action, the current edge string is pushed on the edges'
	 * stack. This ensures, that on the exit action the full path up to the
	 * (then) current node will be present on the stack.
	 * 
	 * NOTE: This assumes depth-first traversal of the tree in the tree walker.
	 * 
	 * @param nodeNr
	 *            the node whose edge is to be pushed on the stack.
	 * @param level
	 *            (irrelevant here, required by interface)
	 */
	@Override
	public void entryaction(int nodeNr, int level) throws IOException {
		if (!(this.tree.getRoot() == nodeNr)) {
			this.edges.push(this.tree.edgeString(nodeNr));
		}
	}

	/**
	 * On the exit action the edges' stack is assumed to contain all edges up to
	 * the current node in the tree. If the current node is a terminal node and
	 * the path up to this point is a full input text (not a suffix) the edges
	 * of the path are written to output separate by
	 * <code>this.class.SEPARATOR</code>.
	 * 
	 * NOTE: This assumes depth-first traversal of the tree in the tree walker.
	 * 
	 * @param nodeNr
	 *            the node whose edges are to be printed.
	 * @param level
	 *            (irrelevant here, required by interface)
	 */
	@Override
	public void exitaction(int nodeNr, int level) throws IOException {
		// ignore root
		if (tree.getRoot() == nodeNr)
			return;

		final Node node = tree.getNode(nodeNr);

		if (node.isTerminal()) {
			final String path = String.join("", edges);
			// check that the node is not only terminal but represents at least
			// one full input text (i.e. is not a suffix)
			for (int i = 0; i < node.getPositionsAmount(); i++) {
				// string equality checking of the path at the terminal node
				// is necessary because for the starting (inner) nodes only
				// one position's textNr is noted. Thus there is no way to
				// simply check for integer equality of the text's begin and the
				// starting node's begin
				if (path.equals(tree.getInputText(node.getTextNr(i)))) {
					// actually write the output
					out.outputToAllCharPipes(String.join(SEPARATOR, edges) + System.lineSeparator());
					break;
				}
			}
		}

		edges.pop();
	}

	/**
	 * Allows to check if processing of the tree has finished. (Simply checks if
	 * each edge that was pushed to the stack was popped as well.)
	 * 
	 * @return true or false
	 */
	public boolean hasCompleted() {
		return edges.empty();
	}

}
