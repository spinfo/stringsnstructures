package modules.suffixTree.suffixTree.applications;

import java.util.TreeSet;

import modules.suffixTree.suffixTree.SuffixTree;

public class ResultToLabelListListener implements ITreeWalkerListener {
	
	private TreeSet<String> labels;
	
	private SuffixTree suffixTree;
	
	public ResultToLabelListListener(SuffixTree suffixTree) {
		this.labels = new TreeSet<String>();
		this.suffixTree = suffixTree;
	}

	@Override
	public void entryaction(int nodeNr, int level) {
		final String label = suffixTree.edgeString(nodeNr);
		labels.add(label);
	}

	@Override
	public void exitaction(int nodeNr, int level) {
	}
	
	public TreeSet<String> getLabels() {
		return labels;
	}

}
