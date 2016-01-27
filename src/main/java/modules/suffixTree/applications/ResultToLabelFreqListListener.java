package modules.suffixTree.applications;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import modules.suffixTree.SuffixTree;
import modules.suffixTree.node.GeneralisedSuffixTreeNode;
import modules.suffixTree.node.TextStartPosInfo;

public class ResultToLabelFreqListListener extends AbstractNodeStackListener implements ITreeWalkerListener {

	private final Map<String, List<Integer>> labelsToFrequencies;

	public ResultToLabelFreqListListener(SuffixTree suffixTree) {
		super(suffixTree);
		this.labelsToFrequencies = new TreeMap<String, List<Integer>>();
	}

	@Override
	public void entryaction(int nodeNr, int level) throws Exception {
		super.entryaction(nodeNr, level);
	}

	@Override
	public void exitaction(int nodeNr, int level) throws Exception {
		// get the current node and node label from the superclass
		// that node matches the one identified by param nodeNr
		final String label = super.getCurrentNodeLabel();
		final GeneralisedSuffixTreeNode node = super.getCurrentNode();
		
		// Retrieve the position information. The superclass has made sure that
		// the position information of all children is included in this list
		final List<TextStartPosInfo> nodeList = node.getStartPositionInformation();
		final int frequency = nodeList.size();
		
		// Add the frequencs to the list of frequencies for this label
		final List<Integer> frequencies = labelsToFrequencies.getOrDefault(label, new LinkedList<Integer>());
		frequencies.add(frequency);
		labelsToFrequencies.put(label, frequencies);
		
		// make sure that the node stack is handled correctly
		super.exitaction(nodeNr, level);
	}
	
	public Map<String, List<Integer>> getLabelsToFrequencies() {
		return labelsToFrequencies;
	}

}
