package modules.tree_building.suffixTree;

import java.io.IOException;
import java.util.ListIterator;
import java.util.Stack;

import modules.OutputPort;
import modules.transitionNetwork.TransitionNetwork;
import modules.transitionNetwork.elements.StateElement;
import modules.transitionNetwork.elements.StateTransitionElement;
import modules.transitionNetwork.elements.SuffixElement;

public class ResultToFiniteStateMachineListener implements ITreeWalkerListener {

	// the OutputPort to write to
	private final OutputPort outputPort;

	// the suffix tree this will work on
	private final BaseSuffixTree tree;

	private boolean inverted = true;

	private TransitionNetwork tn;

	private Stack<Integer> nodeNrs = null;
	private boolean afterBacktrack = true;
	private int nodeNrsStackPos;

	private int lengthOfPath;

	public ResultToFiniteStateMachineListener(BaseSuffixTree suffixTree, OutputPort outputPort, boolean inverted) {
		this.tree = suffixTree;
		this.outputPort = outputPort;
		this.nodeNrs = new Stack<Integer>();
		this.inverted = inverted;
		this.tn = new TransitionNetwork(suffixTree.text, this.inverted);
	}

	public ResultToFiniteStateMachineListener(BaseSuffixTree suffixTree, OutputPort outputPort) {
		this(suffixTree, outputPort, true);
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
		if (this.afterBacktrack) {
			this.afterBacktrack = false;
			nodeNrsStackPos = this.nodeNrs.size() - 1;
		}
		this.nodeNrs.push(nodeNr);
		this.lengthOfPath = this.lengthOfPath + tree.getNode(nodeNr).getEnd(0) - tree.getNode(nodeNr).getStart(0);
	}

	/**
	 * NOTE: This assumes depth-first traversal of the tree in the tree walker.
	 * 
	 * @param nodeNr
	 *            the node whose edges are to be printed.
	 * @param level
	 *            (irrelevant here, required by interface)
	 */
	@Override
	public void exitaction(int nodeNr, int level) throws IOException {
		this.afterBacktrack = true;

		if (nodeIsLeafOfWholeInputText(nodeNr, this.lengthOfPath)) {
			processLeavesOfInputTexts(nodeNr);
		}

		this.lengthOfPath = this.lengthOfPath - (tree.getNode(nodeNr).getEnd(0) - tree.getNode(nodeNr).getStart(0));
		this.nodeNrs.pop();
	}

	public void processLeavesOfInputTexts(int nodeNr) throws IOException {

		ListIterator<Integer> it = nodeNrs.listIterator(this.nodeNrsStackPos);

		// the index of the node in the Transition Network equals the node's
		// nodeNr in the tree
		int nodeIndex = 0;
		if (it.hasNext())
			nodeIndex = it.next();

		while (it.hasNext()) {

			// get or insert the node's corresponding state element if
			// it doesn't exist
			int posInStates = this.tn.addStateElement(new StateElement(nodeIndex));
			StateElement stateElement = this.tn.states.get(posInStates);

			// the child of this loop's node is the next element on the node
			// stack
			int childNr = it.next();

			// generate StateTransition to model the transition to the child
			// node
			StateTransitionElement stateTransitionElement = new StateTransitionElement();
			int childPosInStateElementList = this.tn.addStateElement(new StateElement(childNr));
			stateTransitionElement.toStateElement = childPosInStateElementList;
			stateElement.toStateTransitions.add(stateTransitionElement);

			// generate Suffix Element and link it to the network and the
			// transition
			// TODO: Repeat this for all positions reported by the node?
			int suffixStart = this.tree.nodes[childNr].getStart(0);
			int suffixEnd = this.tree.nodes[childNr].getEnd(0);
			SuffixElement suffixElement = new SuffixElement(suffixStart, suffixEnd);
			int posInSuffixes = this.tn.addSuffixElement(suffixElement);
			stateTransitionElement.toSuffixElement = posInSuffixes;

			// this loop's child will be next loop's parent
			nodeIndex = childNr;
		}

		tn.writeTN(outputPort);
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
