package modules.suffixTreeV2;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public abstract class AbstractResultNodeStackListener implements ITreeWalkerListener {

	// the SuffixTree being traversed
	private final BaseSuffixTree tree;

	// a node stack to push the nodes on as we traverse the tree
	private final Stack<Node> nodes;

	// keep a stack of all edge strings for the whole path to be readily
	// available
	private final Stack<String> edges;

	// while iterating the tree keep track of the current path's length
	private int pathLength = 0;

	/**
	 * An initialiser for subclasses to setup all necessary variables.
	 * 
	 * @param tree
	 *            the suffixTree to traverse
	 */
	public AbstractResultNodeStackListener(BaseSuffixTree tree) {
		this.tree = tree;
		this.nodes = new Stack<Node>();
		this.edges = new Stack<String>();
	}

	/**
	 * On the entry action, the node is pushed on the stack as well as the
	 * node's edge string. Thereby, if the tree is traversed in depth-first
	 * order, the stacks contain the full path of nodes and edges to the current
	 * node on the exit action.
	 * 
	 * Also the current path's length is noted in the node, such that the input
	 * string's text producing the node could be retrieved by the node's
	 * position and the path length for the node.
	 */
	@Override
	public void entryaction(int nodeNr, int level) throws IOException {
		nodes.push(tree.getNode(nodeNr));

		final String edge = tree.edgeString(nodeNr);

		edges.push(edge);
		pathLength += edge.length();
		tree.getNode(nodeNr).setPathLength(pathLength);
	}

	/**
	 * On the exit action the node on top of the stack is removed from the stack
	 * and given to the child class for processing. Also, the leaf nodes below
	 * this node are given to the parent, such that they are present when the
	 * parent is the current node and need's to be processed.
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
		process(nodeNr, Collections.unmodifiableList(nodes), pathLength, level);

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

		// fully reverse the entry actions effects
		final String edge = edges.pop();
		this.pathLength -= edge.length();
	}

	// exposes the stack of nodes to the child class read-only
	protected List<Node> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	// exposes the stack of edges to the child class read-only
	// TODO This should be needed soon. But make sure it really is used.
	protected List<String> getEdges() {
		return Collections.unmodifiableList(edges);
	}

	/**
	 * The child class implements this method to do it's actual work after the
	 * node's have been enriched by the abstract listener.
	 * 
	 * @param currentNode
	 *            The number of the node currently processed.
	 * @param path
	 *            All nodes in the path leading to but not including the current
	 *            Node.
	 * @param pathLength
	 *            The amount of chars in the path's edge strings leading to the
	 *            current node.
	 * @param level
	 *            The node's level within the tree.
	 * 
	 */
	public abstract void process(int nodeNr, List<Node> path, int pathLength, int level);

}
