package main;

import keyWordInPhrase.KeyWordInPhrase;
import preprocess.PreprocessMain;
import suffixTree.suffixMain.GeneralisedSuffixTreeMain;
import suffixTreeClustering.main.SuffixTreeClusteringMain;

public class Application {

	public static void main(String[] args) {
		PreprocessMain.run();
		KeyWordInPhrase.run();
		GeneralisedSuffixTreeMain.run();
		SuffixTreeClusteringMain.run();
	}
}
