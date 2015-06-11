package suffixTree.suffixTree.applications;

import suffixTree.suffixTree.SuffixTree;
import suffixTree.suffixTree.applications.event.MyEntryEvent;
import suffixTree.suffixTree.applications.event.MyExitEvent;

public class TreeWalker {

	public void walk(int node, SuffixTree st, ITreeWalkerListener listener) {
		MyEntryEvent entryEvent = new MyEntryEvent(node);
		listener.entryaction(entryEvent);

		for (int child : st.nodes[node].children.values()) {
			walk(child, st, listener);
		}
		// generate exitEvent
		MyExitEvent exitEvent = new MyExitEvent(node);
		listener.exitaction(exitEvent);
	}
}
