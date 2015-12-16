package modules.suffixTree.suffixTree.applications;

import modules.suffixTree.suffixTree.SuffixTree;

public class TreeWalker {

	public void walk(int nodeNr, SuffixTree st, ITreeWalkerListener listener) {
		listener.entryaction(nodeNr);
		for (int childNr : st.nodes[nodeNr].children.values()) {
			walk(childNr, st, listener);
		}
		// generate exitEvent
		listener.exitaction(nodeNr);
	}
}
