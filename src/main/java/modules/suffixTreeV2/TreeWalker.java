package modules.suffixTreeV2;

import modules.suffixTreeV2.BaseSuffixTree;;

public class TreeWalker {
	
	// this is only used statically and never instantiated
	private TreeWalker() {}
	
	public static void walk(int startNodeNr, BaseSuffixTree suffixTree, ITreeWalkerListener listener) {
		walk(startNodeNr, suffixTree,listener, 0);
	}
	
	private static void walk(int startNodeNr, BaseSuffixTree suffixTree, ITreeWalkerListener listener, int level) {
		listener.entryaction(startNodeNr, level);

		for(int childNodeNr : suffixTree.nodes[startNodeNr].next.values()) {
			walk(childNodeNr, suffixTree, listener, level + 1);
		}
		
		listener.exitaction(startNodeNr, level);
	}
	
}
