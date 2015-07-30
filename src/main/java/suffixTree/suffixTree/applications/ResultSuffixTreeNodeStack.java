package suffixTree.suffixTree.applications;

import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;


public class ResultSuffixTreeNodeStack {
	
	private static final Logger LOGGER = Logger.getGlobal();
//			.getLogger(ResultSuffixTreeNodeStack.class.getName());

	public static Stack<Integer> stack = new Stack<Integer>();
	
	private static ArrayList<SuffixDescription> resultListOfSuffixesFromTerminalToRoot;
	
	public static SuffixTreeAppl suffixTree = null;

	public static void setSuffixTree(SuffixTreeAppl suffixTreeAppl) {
		suffixTree = suffixTreeAppl;
	}

	public static ArrayList<SuffixDescription>SuffixDescriptionArrayListFactory()
	{
		return new ArrayList<SuffixDescription>();
	}
	
	public static void SetSuffixDescriptionArrayList(
			ArrayList<SuffixDescription> list){
		resultListOfSuffixesFromTerminalToRoot=list;
	}
	
	public static ArrayList<SuffixDescription> GetSuffixDescriptionArrayList() {
		return resultListOfSuffixesFromTerminalToRoot;
	}
	
	public static String writeStack() {
		StringBuffer strBuf = new StringBuffer();
		int node;

		for (int i = 0; i <= stack.size() - 1; i++) {
			node = stack.get(i);

			if (node != 1) {
				LOGGER.info(suffixTree.edgeString(node));
				strBuf.append(suffixTree.edgeString(node));
			}
		}
		LOGGER.info("writeStack result: " + strBuf);
		return strBuf.toString();
	}
	
	
	public static void addStackForSuffixesToList(int nodePosition,
			ArrayList<SuffixDescription> listOfSuffixDescription) {
		
		int node,len;
		len=0;
		System.out.println
		("ResultSuffixTreeNodeStack.writeStackForSuffixesToList entry node "+
		stack.peek());
		
		//for (int i = 0; i < stack.size(); i++) 
		for (int i = stack.size()-1;i>=0; i--) 
			{
			node = stack.get(i);
			//System.out.println
			//("ResultSuffixTreeNodeStack.writeStackForSuffixesToList node "+node+
			//" pathLength "+pathLength+ " len "+len);
			if (node != 1) {
				// length from end
				len=suffixTree.nodes[node].nodeInfo.getEnd()-
					suffixTree.nodes[node].nodeInfo.getStart();
				// determine branching positions for all endings in 
				// generalized suffix trees
				//LOGGER.info("writeStackForSuffixes edgestring: "+
				//		suffixTree.edgeString(node)+ "  len: "+len);
				System.out.println
				("ResultSuffixTreeNodeStack.writeStackForSuffixesToList  node " 
						+ node + " edgestring: "+
					suffixTree.edgeString(node)+ 
					"\nnodePosition "+nodePosition+
					"  len: "+len);
				SuffixDescription suffixDescription=
						new SuffixDescription(nodePosition,node);
				
				listOfSuffixDescription.add(suffixDescription);
				nodePosition=nodePosition-len;
			}
			
			
		}
		
		
		
		
	}
	
	public static void writeListOfSuffixDescription(ArrayList<SuffixDescription> list)
	{   System.out.println("ResultSuffixTreeNodeStack.writeListOfSuffixDescription entry");
		for (SuffixDescription suffDesc: resultListOfSuffixesFromTerminalToRoot){
			System.out.println("ResultSuffixTreeNodeStack.writeListOfSuffixDescription node "+suffDesc.node +
					" position "+ suffDesc.position);
		}
	};
}