package modules.suffixTree.suffixTree.applications;

import java.util.ArrayList;

import java.util.logging.Logger;

import models.NodeRepresentation;
import models.PatternInfoRepresentation;
import models.SuffixTreeRepresentation;
import modules.suffixTree.suffixTree.node.GeneralisedSuffixTreeNode;
import modules.suffixTree.suffixTree.node.textStartPosInfo.TextStartPosInfo;

/**
 * A TreeWalkerListener that builds Objects representing the suffix tree's node
 * and pushes them to a representation of the whole tree.
 */
public class ResultToRepresentationListener implements ITreeWalkerListener {

	private static final Logger LOGGER = Logger.getLogger(ResultToRepresentationListener.class.getName());

	// the suffix tree's representation on which this class will add
	// representations of the suffix tree's nodes
	private final SuffixTreeRepresentation suffixTreeRepresentation;
	
	// a node stack that the listener can collect nodes on
	private final ResultSuffixTreeNodeStack nodeStack;

	public ResultToRepresentationListener(SuffixTreeRepresentation suffixTreeRepresentation, ResultSuffixTreeNodeStack nodeStack) {
		this.suffixTreeRepresentation = suffixTreeRepresentation;
		this.nodeStack = nodeStack;
	}

	/**
	 * This simply pushes the node number of the current node on a stack for
	 * later processing on the exitaction.
	 * 
	 * The reason for this seems to be, that the ResultSuffixTreeNodeStack can
	 * elegantly get a representation of a node's label on the exitaction.
	 */
	@Override
	public void entryaction(int nodeNr, int level) {
		this.nodeStack.push(nodeNr);
	}

	/**
	 * Generates representation objects for the suffix tree's node and adds them
	 * to the suffix tree representation.
	 */
	@Override
	public void exitaction(int nodeNr, int level) {
		
		final SuffixTreeAppl suffixTreeAppl = this.nodeStack.getSuffixTreeAppl();

		// the node's label and identifying number are simply retrieved from the
		// node stack. The identifying nodeNr is strictly necessary, so we do
		// not catch the possible EmptyStackException at this point
		final String label = this.nodeStack.writeStack();
		int stackedNodeNr = this.nodeStack.pop();
		stackedNodeNr = nodeNr;

		// For the rest of the information we need to retrieve the node
		final GeneralisedSuffixTreeNode node = ((GeneralisedSuffixTreeNode) suffixTreeAppl.nodes[stackedNodeNr]);
		final ArrayList<TextStartPosInfo> nodeList = node.getStartPositionOfSuffix();
		final int frequency = nodeList.size();

		// TODO write a comment explaining this... (Copied from
		// ResultToXmlListener)
		if (!this.nodeStack.empty()) {
			final int mother = this.nodeStack.peek();
			final GeneralisedSuffixTreeNode motherNode = ((GeneralisedSuffixTreeNode) suffixTreeAppl.nodes[mother]);
			motherNode.getStartPositionOfSuffix().addAll(nodeList);
		}

		// construct and fill an output object for the node
		NodeRepresentation nodeRepresentation = new NodeRepresentation();
		nodeRepresentation.setNumber(stackedNodeNr);
		nodeRepresentation.setLabel(label);
		nodeRepresentation.setFrequency(frequency);

		// construct, fill and add output objects for the node's pattern infos
		for (int i = 0; i < frequency; i++) {
			final PatternInfoRepresentation patternInfoRepresentation = new PatternInfoRepresentation();
			final TextStartPosInfo info = nodeList.get(i);

			patternInfoRepresentation.setTypeNr(info.unit);
			patternInfoRepresentation.setPatternNr(info.text);
			patternInfoRepresentation.setStartPos(info.startPositionOfSuffix);

			if (patternInfoRepresentation.isComplete()) {
				nodeRepresentation.getPatternInfos().add(patternInfoRepresentation);
			} else {
				LOGGER.warning(
						"Ignoring incomplete pattern info representation for node: " + nodeRepresentation.getNumber());
			}
		}

		// add the node's representation to the tree's if the first is complete
		if (nodeRepresentation.isComplete()) {
			this.suffixTreeRepresentation.getNodes().add(nodeRepresentation);
		} else {
			LOGGER.warning("Ignoring incomplete node representation for node: " + nodeRepresentation.getNumber());
		}
	}

}
