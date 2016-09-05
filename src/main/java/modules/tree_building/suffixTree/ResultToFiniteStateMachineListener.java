package modules.tree_building.suffixTree;

import java.io.IOException;
import java.util.ListIterator;
import java.util.Stack;

import modules.transitionNetwork.TransitionNetwork;
import modules.transitionNetwork.elements.StateElement;
import modules.transitionNetwork.elements.StateTransitionElement;
import modules.transitionNetwork.elements.SuffixElement;

public class ResultToFiniteStateMachineListener implements ITreeWalkerListener {

	// the suffix tree this will work on
	private final BaseSuffixTree tree;

	private boolean inverted = true;

	private TransitionNetwork tn;

	private Stack<Integer> nodeNrs = null;

	// this listener needs a second stack that records only those node numbers
	// of the last whole input word that has been processed
	private Stack<Integer> nodeNrsOfLastFullPath = null;

	// the length of the path currently read
	private int lengthOfPath;

	public ResultToFiniteStateMachineListener(BaseSuffixTree suffixTree, boolean inverted) {
		this.tree = suffixTree;
		this.nodeNrs = new Stack<Integer>();
		this.inverted = inverted;
		this.tn = new TransitionNetwork(suffixTree.text, this.inverted);
		this.nodeNrsOfLastFullPath = new Stack<Integer>();
	}

	public ResultToFiniteStateMachineListener(BaseSuffixTree suffixTree) {
		this(suffixTree, true);
	}

	public void setTN(TransitionNetwork tn) {
		this.tn = tn;
	}

	public TransitionNetwork getTN() {
		return this.tn;
	}

	public void setInverted(boolean inverted) {
		this.inverted = inverted;
	}

	@Override
	public void entryaction(int nodeNr, int level) throws IOException {
		this.nodeNrs.push(nodeNr);
		this.lengthOfPath = this.lengthOfPath + tree.getNode(nodeNr).getEnd(0) - tree.getNode(nodeNr).getStart(0);
	}

	@Override
	public void exitaction(int nodeNr, int level) throws IOException {
		// if the current node is a leaf of a whole input text, it gets
		// processed and the the path to it is recorded in a separate stack
		if (nodeIsLeafOfWholeInputText(nodeNr, this.lengthOfPath)) {
			processLeavesOfInputTexts(nodeNr, level);

			this.nodeNrsOfLastFullPath.clear();
			this.nodeNrsOfLastFullPath.addAll(nodeNrs);
		}
		this.lengthOfPath = this.lengthOfPath - (tree.getNode(nodeNr).getEnd(0) - tree.getNode(nodeNr).getStart(0));
		this.nodeNrs.pop();

		// if we are backtracking away from a node that was on the last full
		// path processed, pop that node as well
		if (!nodeNrsOfLastFullPath.isEmpty() && nodeNr == nodeNrsOfLastFullPath.peek()) {
			nodeNrsOfLastFullPath.pop();
		}
	}

	public void processLeavesOfInputTexts(int nodeNr, int level) throws IOException {

		// we need to enter the node stack (i.e. the path that leads to the
		// current node) at that position where a backtrack into the last full
		// path processed by this method occurred
		ListIterator<Integer> it;
		if (this.nodeNrsOfLastFullPath.isEmpty()) {
			it = this.nodeNrs.listIterator();
		} else {
			it = this.nodeNrs.listIterator(nodeNrsOfLastFullPath.size() - 1);
		}

		// the index of the node in the Transition Network equals the node's
		// nodeNr in the tree
		int nodeIndex = 0;
		if (it.hasNext()) {
			nodeIndex = it.next();
		} else {
			throw new IllegalStateException("Node stack empty or not entered at correct position.");
		}

		// loop over the remaining path and generate transition elements to
		// model the transition from one node to another
		while (it.hasNext()) {

			// get or insert the node's corresponding state element if
			// it doesn't exist
			int stateId = this.tn.addStateElement(new StateElement(nodeIndex));
			StateElement stateElement = this.tn.states.get(stateId);

			// the node numer of the next node on the path
			int childNodeIndex = it.next();

			// model the transition to the next state
			StateTransitionElement transition = new StateTransitionElement();

			// the transition leads to the next suffix tree node (i.e. state),
			// but if that node is the last on the stack (i.e. a leaf per
			// definition of this method), model it as a transition to the
			// network's final state instead
			int childStateId;
			if (it.hasNext()) {
				childStateId = this.tn.addStateElement(new StateElement(childNodeIndex));
			} else {
				childStateId = this.tn.getFinalStateId();
			}
			transition.toStateElement = childStateId;
			stateElement.toStateTransitions.add(transition);

			// generate Suffix Element, add it to the network and link to it
			// from the transition
			int suffixStart = this.tree.nodes[childNodeIndex].getStart(0);
			int suffixEnd = this.tree.nodes[childNodeIndex].getEnd(0);
			SuffixElement suffixElement = new SuffixElement(suffixStart, suffixEnd);
			int posInSuffixes = this.tn.addSuffixElement(suffixElement);
			transition.toSuffixElement = posInSuffixes;

			// this loop's child will be next loop's parent
			nodeIndex = childNodeIndex;
		}
	}

	// checks if the given node in this listeners tree corresponds to a whole
	// input text given the current path length.
	private boolean nodeIsLeafOfWholeInputText(int nodeNr, int pathLength) {
		Node node = this.tree.getNode(nodeNr);

		if (!node.isTerminal()) {
			return false;
		}

		for (NodePosition position : node.getPositions()) {
			if (position.getEnd() == tree.getTextBegin(position.getTextNr()) + pathLength) {
				return true;
			}
		}

		return false;
	}

}
