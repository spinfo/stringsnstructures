package modules.suffixTree.suffixTree.applications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import modules.suffixTree.suffixTree.node.GeneralisedSuffixTreeNode;
import modules.suffixTree.suffixTree.node.textStartPosInfo.TextStartPosInfo;

public class ResultToLabelFreqListListener implements ITreeWalkerListener {

	private final Map<String, List<Integer>> labelsToFrequencies;

	private final SuffixTreeAppl suffixTree;

	public ResultToLabelFreqListListener(SuffixTreeAppl suffixTree) {
		this.labelsToFrequencies = new TreeMap<String, List<Integer>>();
		this.suffixTree = suffixTree;
	}

	@Override
	public void entryaction(int nodeNr, int level) throws IOException {
		// first get the label, it is "" for the root node
		final String label;
		if (nodeNr == suffixTree.getRoot()) {
			label = "";
		} else {
			label = suffixTree.edgeString(nodeNr);
		}
		// then get the frequency and add it to the list of frequencies
		// this is a linked list because memory is important here and this
		// usually only needs to be added to then output
		final List<Integer> frequencies = labelsToFrequencies.getOrDefault(label, new LinkedList<Integer>());
		// For the rest of the information we need to retrieve the node
		final GeneralisedSuffixTreeNode node = ((GeneralisedSuffixTreeNode) suffixTree.nodes[nodeNr]);
		final ArrayList<TextStartPosInfo> nodeList = node.getStartPositionOfSuffix();
		// add everything and return
		frequencies.add(nodeList.size());
		labelsToFrequencies.put(label, frequencies);
	}

	@Override
	public void exitaction(int nodeNr, int level) throws IOException {
		// do nothing
	}
	
	public Map<String, List<Integer>> getLabelsToFrequencies() {
		return labelsToFrequencies;
	}

}
