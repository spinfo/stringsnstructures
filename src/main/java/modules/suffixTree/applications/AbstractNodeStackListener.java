package modules.suffixTree.applications;

import modules.suffixTree.SuffixTree;
import modules.suffixTree.node.GeneralisedSuffixTreeNode;

/**
 * This abstract listener provides behavior for depth-first traversal of a tree,
 * where all children of a given node must be present while processing the node
 * on the exitaction.
 * 
 * The client has to make sure, that the TreeWalker used with a listener
 * extending this class does walk the tree in depth-first order.
 * 
 * @author David Neugebauer
 */
public abstract class AbstractNodeStackListener implements ITreeWalkerListener {

	// the SuffixTree being traversed
	private final SuffixTree suffixTree;

	// a node stack to push the nodes on as we traverse the tree
	private final ResultSuffixTreeNodeStack nodeStack;

	/**
	 * an initializer for subclasses to setup all necessary variables
	 * 
	 * @param suffixTree
	 *            the suffixTree to traverse
	 */
	public AbstractNodeStackListener(SuffixTree suffixTree) {
		this.suffixTree = suffixTree;
		this.nodeStack = new ResultSuffixTreeNodeStack(suffixTree);
	}

	/**
	 * On entry of a node that node is simply pushed on the stack
	 */
	@Override
	public void entryaction(int nodeNr, int level) throws Exception {
		this.nodeStack.push(nodeNr);
	}

	/**
	 * This should be called on the last line of the exit action.
	 * 
	 * On exit of a node, the TextStartInformation objects are added to the
	 * parent node, such that when it's own exitaction is processed information
	 * for the leaves of all children are present on the parent node.
	 */
	@Override
	public void exitaction(int nodeNr, int level) throws Exception {
		// get the node currently on top of the stack
		final GeneralisedSuffixTreeNode node = getCurrentNode();

		// if the node nr given and the current node Nr on top of the stack do
		// not match the tree was not traversed in the right order
		final int stackedNodeNr = nodeStack.pop();
		if (nodeNr != stackedNodeNr) {
			throw new Exception("Differing node nrs encountered on tree listener.");
		}

		// Push all TextStartPositionInformations of the current node to the
		// parent
		final GeneralisedSuffixTreeNode parentNode = getCurrentNode();
		if (parentNode != null) {
			parentNode.getStartPositionInformation().addAll(node.getStartPositionInformation());
		}
	}

	/**
	 * Convenience Method to peek at the node currently on top of the Stack and
	 * return it's label.
	 * 
	 * @return the node's label
	 */
	protected String getCurrentNodeLabel() {
		return this.suffixTree.edgeString(this.nodeStack.peek());
	}

	/**
	 * Convenience method to peek at the node currently on top of the stack and
	 * return it.
	 */
	protected GeneralisedSuffixTreeNode getCurrentNode() {
		if (nodeStack.empty()) {
			return null;
		} else {
			return (GeneralisedSuffixTreeNode) suffixTree.nodes[nodeStack.peek()];
		}
	}
}
