package modules.suffixTree.suffixTree.applications;

import java.util.TreeSet;

public class ResultToLabelListListener implements ITreeWalkerListener {
	
	private TreeSet<String> labels;
	
	private SuffixTreeAppl suffixTree;
	
	public ResultToLabelListListener(SuffixTreeAppl suffixTree) {
		this.labels = new TreeSet<String>();
		this.suffixTree = suffixTree;
	}

	@Override
	public void entryaction(int nodeNr, int level) {
		if (nodeNr != suffixTree.getRoot()) {
			final String label = suffixTree.edgeString(nodeNr);
			labels.add(label);
		}
	}

	@Override
	public void exitaction(int nodeNr, int level) {
	}
	
	public TreeSet<String> getLabels() {
		return labels;
	}

}
