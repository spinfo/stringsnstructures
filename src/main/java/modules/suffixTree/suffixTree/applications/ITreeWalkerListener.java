package modules.suffixTree.suffixTree.applications;

import java.util.EventListener;

public interface ITreeWalkerListener extends EventListener {

	void entryaction(int nodeNr);

	void exitaction(int nodeNr);
}
