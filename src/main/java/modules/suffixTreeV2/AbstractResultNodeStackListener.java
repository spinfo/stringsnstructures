package modules.suffixTreeV2;

import java.io.IOException;
import java.util.Stack;

public abstract class AbstractResultNodeStackListener implements ITreeWalkerListener {

	// the SuffixTree being traversed
	private final BaseSuffixTree tree;

	// a node stack to push the nodes on as we traverse the tree
	private final Stack<Node> nodes;

	/**
	 * An initialiser for subclasses to setup all necessary variables.
	 * 
	 * @param suffixTree
	 *            the suffixTree to traverse
	 */
	public AbstractResultNodeStackListener(BaseSuffixTree tree) {
		this.tree = tree;
		this.nodes = new Stack<Node>();
	}

	/**
	 * On the entry action, the node is simply pushed on the stack. Thereby, if
	 * the tree is traversed in depth-first order, the stack contains the full
	 * path to the current node on the exit action.
	 */
	@Override
	public void entryaction(int nodeNr, int level) throws IOException {
		nodes.push(this.tree.getNode(nodeNr));
	}

	/**
	 * On the exit action the node on top of the stack is removed from the stack
	 * and given to the child class for processing. Also, the leaf nodes below
	 * this node are propagated to the parent, such that they are present when
	 * the parent is the current node and need's to be processed.
	 */
	@Override
	public void exitaction(int nodeNr, int level) throws IOException {
		// get the current node, it's parent is on top of the stack after
		// removing the current node
		final Node node = nodes.pop();
		final Node parent;

		// invariant check, compare the stacked node to the one given by nodeNr
		// and complain if they do not equal.
		if (!node.equals(tree.getNode(nodeNr))) {
			throw new IllegalStateException("Differing nodes encountered on tree listener.");
		}

		// let the child class do it's work
		process(node, level);

		if (nodeNr != tree.getRoot()) {
			parent = nodes.peek();

			// propagate leaf nodes from this node to the parent if any are
			// present
			parent.getLeaves().addAll(node.getLeaves());

			// if the node is itself a terminal node, it is noted in the
			// parent's leaves as well
			if (node.isTerminal()) {
				parent.getLeaves().add(node);
			}
		}
	}

	// exposes the stack to the child class
	protected Stack<Node> getNodes() {
		return nodes;
	}

	// the child class uses this to process the node on top of the stack
	public abstract void process(Node node, int leve);

}
