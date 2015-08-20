package suffixTree.suffixTree.applications;

import suffixTree.suffixMain.GeneralisedSuffixTreeMain;

public class CompressSuffixTree {
	
	private static int nrSuccessors(int node,SuffixTreeAppl tree) {
		return tree.nodes[node].children.size();
	}
	
	private static int checkContext(int suffixNode,SuffixTreeAppl tree1,
			SuffixTreeAppl tree2) {
		int check=0;
		// get successor in tree 1
		for (int successor : tree1.nodes[suffixNode].children.values()) {
			// get label of successor
			String labelOfSuccessor=tree1.edgeString(successor);
			// reverse label and search in reversed tree 2
			String reversedLabelOfSuccessor=
					new StringBuilder(labelOfSuccessor).reverse().toString();
			SearchResult invertResult= tree2.search(reversedLabelOfSuccessor,
					0, tree2.getRoot());	
			// count children of invertResult.node
			if (invertResult==null) return 0;
			else if (invertResult.result==1 ) return 0;
			else check=+ tree2.nodes[invertResult.node].children.size();
					
		}
		System.out.println("checkContext check "+check);
		return check;
	}
	
	private static void selectFunctionalUnits(SuffixTreeAppl normalTree,
			SuffixTreeAppl invertedTree) {
		
		int root= normalTree.getRoot();
		String reversedLabel;
		// traverse children of root, get label strings
		for (int suffix : normalTree.nodes[root].children.values()) {
			String label=normalTree.edgeString(suffix);
			if (label.charAt(0)!='$') {
				System.out.println("selectFunctionalUnits label "+label);
				// reverse label
				if (label.charAt(label.length()-1)!='$')
					reversedLabel=new StringBuilder(label).reverse().toString();
				else reversedLabel= new StringBuilder(label.substring
					(0,label.length()-1)/*delete '$'*/).reverse().toString();
				System.out.println("selectFunctionalUnits reversedLabel "+
						reversedLabel);
				// search reversedSuffix in reversedSuffixTree
				SearchResult invertResult= invertedTree.search(reversedLabel,0,
						invertedTree.getRoot());
				if (invertResult !=null){
					System.out.println("found " +invertResult.result);
				} else System.out.println("not found");
				// End right
				int resultRight=checkContext(suffix,normalTree,invertedTree);
				int resultLeft;
				// left inverted ends with '$', i.e. label starts at beginning
				if((invertResult!=null) && (invertResult.result==3))
					resultLeft= 10; // to do, guessed value
				//
				// Beginning left
				else resultLeft=
				checkContext(invertResult.node,invertedTree,normalTree);
				
				
				
			}
		}
	}
	
	/* suffix tree */
	public static void run(){
		//ArrayList<SuffixDescription> list1,list2;
		SuffixTreeAppl suffixTree,reversedSuffixTree;
		String text= /*"abac$dbx$"*/ "die$der$";
		String reversedText=
		new StringBuilder(text.substring
		(0,text.length()-1)/*delete '$'*/).reverse().append('$').toString();
		
	
		System.out.println("text: "+text+ " reversedText "+reversedText);
		
		//list1=ResultSuffixTreeNodeStack.SuffixDescriptionArrayListFactory();
		//ResultSuffixTreeNodeStack.SetSuffixDescriptionArrayList(list1);
		GeneralisedSuffixTreeMain.test(text);
		suffixTree=GeneralisedSuffixTreeMain.st;
		GeneralisedSuffixTreeMain.test(reversedText);
		reversedSuffixTree=GeneralisedSuffixTreeMain.st;
		
		selectFunctionalUnits(suffixTree,reversedSuffixTree);
		
		//GeneralisedSuffixTreeMain.suffixTreePath();
		
		//System.out.println
		//("vor ResultSuffixTreeNodeStack.writeListOfSuffixDescription");
		//ResultSuffixTreeNodeStack.writeListOfSuffixDescription(list1);
		
		/*
		StringBuffer reverse = new StringBuffer(text).reverse();
		// delete dollar
		reverse.deleteCharAt(0);
		// append dollar
		reverse.append('$');
		String reversedText=reverse.toString();
		//	prefix tree
		list2=ResultSuffixTreeNodeStack.SuffixDescriptionArrayListFactory();
		ResultSuffixTreeNodeStack.SetSuffixDescriptionArrayList(list2);
		GeneralisedSuffixTreeMain.suffixTreePath(GeneralisedSuffixTreeMain.st);
		ResultSuffixTreeNodeStack.writeListOfSuffixDescription(list2);
		*/
	/*

	treewalker ausschreiben
	vergleichen
	suffix tree komprimieren
	 */
	}
	
	
	public static void main(String[] args){
		run();
	}
	
}
