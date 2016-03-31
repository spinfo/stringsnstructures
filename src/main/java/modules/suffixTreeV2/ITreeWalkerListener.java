package modules.suffixTreeV2;

import java.io.IOException;

public interface ITreeWalkerListener {

	/**
	 * The action to take on the current node when the TreeWalker enters it.
	 * (Before the node's children are processed.)
	 * 
	 * @param nodeNr
	 *            the number of the current node
	 * @param level
	 *            the level of the node below the node that the TreeWalker
	 *            started to walk on
	 */
	void entryaction(int nodeNr, int level) throws IOException;

	/**
	 * The action to take on the current node when the TreeWalker exits it.
	 * (After the node's children are processed.)
	 * 
	 * @param nodeNr
	 *            the number of the current node
	 * @param level
	 *            the level of the node below the node that the TreeWalker
	 *            started to walk on
	 * @throws Exception
	 *             if the implementation throws one.
	 */
	void exitaction(int nodeNr, int level) throws IOException;

}
