package modules.suffixTree.applications;

import modules.suffixTree.SuffixTree;
import modules.suffixTree.node.GeneralisedSuffixTreeNode;

/**
 * This abstract listener provides behaviour for depth-first traversal of a
 * tree, where all children of a given node must be present while processing the
 * node on the exitaction.
 * 
 * Tree's nodes are altered during the entryaction and the tree's references to
 * the nodes is deleted on the exitaction.
 * 
 * Tree nodes are considered to be GeneralisedSuffixTreeNodes.
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
	 * On entry of a node that node is pushed on the stack and it's leaf count
	 * is increased.
	 */
	@Override
	public void entryaction(int nodeNr, int level) throws Exception {
		this.nodeStack.push(nodeNr);
		// if the node has position information on the entry action it is a leaf
		// node
		final GeneralisedSuffixTreeNode node = (GeneralisedSuffixTreeNode) this.suffixTree.nodes[nodeNr];
		if (node.getStartPositionInformation().size() > 0) {
			node.sumOfLeaves = 1;
		}
	}

	/**
	 * This should be called on the last line of the exit action.
	 * 
	 * NOTE: The tree's reference to the current node is deleted before this
	 * method exits. It is then no longer available from the tree's node list.
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
		// this pops the current node from the stack. After that the parent of
		// the current node is on top of the stack
		final int stackedNodeNr = nodeStack.pop();
		if (nodeNr != stackedNodeNr) {
			throw new Exception("Differing node nrs encountered on tree listener.");
		}

		// Push all TextStartPositionInformations of the current node to the
		// parent and add this node's leaf sum to that of the parent
		final GeneralisedSuffixTreeNode parentNode = getCurrentNode();
		if (parentNode != null) {
			parentNode.getStartPositionInformation().addAll(node.getStartPositionInformation());
			parentNode.sumOfLeaves += node.sumOfLeaves;
		}

		// remove the suffix tree's reference to the node processed
		this.suffixTree.nodes[nodeNr] = null;
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
	 * 
	 * @return the node on top or null if none is present
	 */
	protected GeneralisedSuffixTreeNode getCurrentNode() {
		if (nodeStack.empty()) {
			return null;
		} else {
			return (GeneralisedSuffixTreeNode) suffixTree.nodes[nodeStack.peek()];
		}
	}

	/**
	 * Convenience method to peek at the node currently at the top of the stack
	 * and return it's parent node.
	 * 
	 * @return the top node's parent or null if no top node or no parent is
	 *         present
	 */
	protected GeneralisedSuffixTreeNode getCurrentNodeParent() {
		GeneralisedSuffixTreeNode result = null;
		if (!nodeStack.empty()) {
			int top = nodeStack.pop();
			// after the top node is popped, the next node on the stack is the
			// parent
			result = getCurrentNode();
			// restore previous order
			nodeStack.push(top);
		}
		return result;
	}

	/**
	 * Convenience method to peek at the node currently at the top of the stack
	 * and count it's siblings.
	 * 
	 * @return the amount of the current node's siblings
	 */
	protected int getCurrentNodeSiblingCount() throws Exception {
		int result = 0;
		final GeneralisedSuffixTreeNode parent = getCurrentNodeParent();
		if (parent != null) {
			result = parent.children.size() - 1;
		}
		// if there is a parent there has to have been a child an thus
		// parent.children cannot be < 1
		if (result < 0) {
			throw new Exception("Wrong number of children for node. Is the node stack stacked in the right order?");
		}
		return result;
	}
}
