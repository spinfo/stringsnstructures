package modules.tree_building.suffixTree;

import java.io.IOException;

import modules.tree_building.suffixTree.BaseSuffixTree;;

public class TreeWalker {

	// this is only used statically and never instantiated
	private TreeWalker() {
	}

	/**
	 * Walks the tree's nodes recursively (depth-first) and executes the
	 * listener's actions for each node.
	 * 
	 * @param startNodeNr
	 *            The start with
	 * @param suffixTree
	 *            The SuffixTree to walk on
	 * @param listener
	 *            The listener defining the actions to take on each node
	 * @throws IOException on error
	 */
	public static void walk(int startNodeNr, BaseSuffixTree suffixTree, ITreeWalkerListener listener) throws IOException {
		walk(startNodeNr, suffixTree, listener, 0/*level*/);
	}

	/**
	 * Walks the tree's nodes recursively (depth-first) and executes the
	 * listener's actions for each node.
	 * @param startNodeNr The start with
	 * @param suffixTree The SuffixTree to walk on
	 * @param listener The listener defining the actions to take on each node
	 * @param level level
	 * @throws IOException on error
	 */
	
	private static void walk(int startNodeNr, BaseSuffixTree suffixTree, ITreeWalkerListener listener, int level)
			throws IOException {
		listener.entryaction(startNodeNr, level);

		for (int childNodeNr : suffixTree.nodes[startNodeNr].next.values()) {
			walk(childNodeNr, suffixTree, listener, level + 1);
		}

		listener.exitaction(startNodeNr, level);
	}

}
