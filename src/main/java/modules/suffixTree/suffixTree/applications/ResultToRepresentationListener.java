package modules.suffixTree.suffixTree.applications;

import java.util.ArrayList;
import java.util.logging.Logger;

import modules.suffixTree.output.NodeRepresentation;
import modules.suffixTree.output.PatternInfoRepresentation;
import modules.suffixTree.output.SuffixTreeRepresentation;
import modules.suffixTree.suffixTree.applications.event.MyEntryEvent;
import modules.suffixTree.suffixTree.applications.event.MyExitEvent;
import modules.suffixTree.suffixTree.node.GeneralisedSuffixTreeNode;
import modules.suffixTree.suffixTree.node.textStartPosInfo.TextStartPosInfo;

/**
 * A TreeWalkerListener that builds Objects representing the suffix tree's node
 * and pushes them to a representation of the whole tree.
 */
public class ResultToRepresentationListener implements ITreeWalkerListener {

	private static final Logger LOGGER = Logger.getLogger(ResultToRepresentationListener.class.getName());

	// the suffix tree that this class operates on
	//private final SuffixTreeAppl suffixTreeAppl;

	// the suffix tree's representation on which this class will add
	// representations of the suffix tree's nodes
	private final SuffixTreeRepresentation suffixTreeRepresentation;

	public ResultToRepresentationListener(SuffixTreeRepresentation suffixTreeRepresentation) {
		this.suffixTreeRepresentation = suffixTreeRepresentation;
	}

	@Deprecated
	public ResultToRepresentationListener(SuffixTreeAppl suffixTreeAppl,
			SuffixTreeRepresentation suffixTreeRepresentation) {
		//this.suffixTreeAppl = suffixTreeAppl;
		this.suffixTreeRepresentation = suffixTreeRepresentation;
	}

	/**
	 * This simply pushes the node number of the current node on a stack for
	 * later processing on the exitaction.
	 */
	@Override
	public void entryaction(MyEntryEvent entryEvent) {
		Integer nodeNr = (Integer) entryEvent.getSource();
		ResultSuffixTreeNodeStack.stack.push(nodeNr);
	}

	/**
	 * Generates representation objects for the suffix tree's node and adds them
	 * to the suffix tree representation. Both the suffix tree being operated on
	 * as well as it's general representation are assumed to exist since
	 * initialisation.
	 */
	@Override
	public void exitaction(MyExitEvent exitEvent) {

		// the node's label and identifying number are simply retrieved from the
		// node stack. The identiying nodeNr is strictly neccessary, so we do
		// not catch the possible EmptyStackException at this point
		final String label = ResultSuffixTreeNodeStack.writeStack();
		final int nodeNr = ResultSuffixTreeNodeStack.stack.pop();

		// For the rest of the information we need to retrieve the node
		final GeneralisedSuffixTreeNode node = ((GeneralisedSuffixTreeNode) ResultSuffixTreeNodeStack.suffixTree.nodes[nodeNr]);
		final ArrayList<TextStartPosInfo> nodeList = node.getStartPositionOfSuffix();
		final int frequency = nodeList.size();

		// TODO write a comment explaining this...
		if (!ResultSuffixTreeNodeStack.stack.empty()) {
			final int mother = ResultSuffixTreeNodeStack.stack.peek();
			final GeneralisedSuffixTreeNode motherNode = ((GeneralisedSuffixTreeNode) ResultSuffixTreeNodeStack.suffixTree.nodes[mother]);
			motherNode.getStartPositionOfSuffix().addAll(nodeList);
		}

		// construct and fill an output object for the node
		NodeRepresentation nodeRepresentation = new NodeRepresentation();
		nodeRepresentation.setNumber(nodeNr);
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
				LOGGER.warning("Ignoring incomplete pattern info representation for node: " + nodeRepresentation.getNumber());
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
