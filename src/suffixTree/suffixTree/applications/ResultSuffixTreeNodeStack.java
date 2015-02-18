package suffixTree.suffixTree.applications;

import java.util.Stack;
import java.util.logging.Logger;

public class ResultSuffixTreeNodeStack {
	
	private static final Logger LOGGER = Logger.getGlobal();
//			.getLogger(ResultSuffixTreeNodeStack.class.getName());

	public static Stack<Integer> stack = new Stack<Integer>();

	public static SuffixTreeAppl suffixTree = null;

	public static void setPrintSuffixTree(SuffixTreeAppl suffixTreeAppl) {
		suffixTree = suffixTreeAppl;
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
}