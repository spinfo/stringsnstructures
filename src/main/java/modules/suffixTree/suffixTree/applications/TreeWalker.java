package modules.suffixTree.suffixTree.applications;

import modules.suffixTree.suffixTree.SuffixTree;
import modules.suffixTree.suffixTree.applications.event.MyEntryEvent;
import modules.suffixTree.suffixTree.applications.event.MyExitEvent;

public class TreeWalker {

	public void walk(int node, SuffixTree st, ITreeWalkerListener listener) {
		MyEntryEvent entryEvent = new MyEntryEvent(node);
		listener.entryaction(entryEvent);
		System.out.println("walk node nr: "+node);
		for (int child : st.nodes[node].children.values()) {
			walk(child, st, listener);
		}
		// generate exitEvent
		MyExitEvent exitEvent = new MyExitEvent(node);
		listener.exitaction(exitEvent);
	}
}
