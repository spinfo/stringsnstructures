package modules.suffixTree.applications;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import modules.suffixTree.SuffixTree;
import modules.suffixTree.node.Node;

public class ResultToLabelChildCountListListener implements ITreeWalkerListener {

	private final Map<String, List<Integer>> labelsToChildCounts;

	private final SuffixTree suffixTree;

	public ResultToLabelChildCountListListener(SuffixTree suffixTree) {
		this.labelsToChildCounts = new TreeMap<String, List<Integer>>();
		this.suffixTree = suffixTree;
	}

	@Override
	public void entryaction(int nodeNr, int level) throws IOException {
		// first get the label, it is "" for the root node
		final String label = suffixTree.edgeString(nodeNr);
		// then get the frequency and add it to the list of frequencies
		// this is a linked list because memory is important here and this
		// usually only needs to be added to then output
		final List<Integer> childCounts = labelsToChildCounts.getOrDefault(label, new LinkedList<Integer>());
		// For the rest of the information we need to retrieve the node
		final Node node = suffixTree.nodes[nodeNr];
		childCounts.add(node.children.size());
		labelsToChildCounts.put(label, childCounts);
	}

	@Override
	public void exitaction(int nodeNr, int level) throws IOException {
		// do nothing
	}
	
	public Map<String, List<Integer>> getLabelsToChildCounts() {
		return labelsToChildCounts;
	}

}
