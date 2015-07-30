package main;

import keyWordInPhrase.KeyWordInPhrase;
import preprocess.PreprocessMain;
import suffixTree.suffixMain.GeneralisedSuffixTreeMain;
import suffixTree.suffixTree.applications.CompressSuffixTree;
import suffixTreeClustering.main.SuffixTreeClusteringMain;

public class Application {

	public static void main(String[] args) {
		PreprocessMain.run();
		KeyWordInPhrase.run();
		CompressSuffixTree.run();
		//GeneralisedSuffixTreeMain.test("abc$");
		//System.out.println("st: "+GeneralisedSuffixTreeMain.st);
		//GeneralisedSuffixTreeMain.persistSuffixTree();
		//SuffixTreeClusteringMain.run();
	}
}
