package modules.suffixTree.suffixTree.applications;

import java.util.ArrayList;

import modules.suffixTree.suffixTree.node.GeneralisedSuffixTreeNode;
import modules.suffixTree.suffixTree.node.textStartPosInfo.TextStartPosInfo;

/**
 * A TreeWalkerListener that outputs the trees nodes directly to an
 * XmlPrintWriter.
 */
public class ResultToXmlListener implements ITreeWalkerListener {

	XmlPrintWriter out;
	
	// a node stack that the listener can collect nodes on
	private final ResultSuffixTreeNodeStack nodeStack;

	// cstr
	public ResultToXmlListener(XmlPrintWriter o, ResultSuffixTreeNodeStack nodeStack) {
		this.out = o;
		this.nodeStack = nodeStack;
	}

	/**
	 * This simply pushes the node number of the current node on a stack for
	 * later processing on the exitaction.
	 * 
	 * The reason for this seems to be, that the ResultSuffixTreeNodeStack can
	 * elegantly get a representation of a node's label on the exitaction.
	 */
	@Override
	public void entryaction(int nodeNr, int level) {
		this.nodeStack.push(nodeNr);
	}

	@Override
	public void exitaction(int nodeNr, int level) {
		final SuffixTreeAppl suffixTreeAppl = this.nodeStack.getSuffixTreeAppl();
		
		String label = this.nodeStack.writeStack();

		if (this.nodeStack.empty()) {
			return;
		}
		int stackedNodeNr = this.nodeStack.pop();
		GeneralisedSuffixTreeNode node = ((GeneralisedSuffixTreeNode) suffixTreeAppl.nodes[stackedNodeNr]);
		ArrayList<TextStartPosInfo> nodeList = node.getStartPositionOfSuffix();
		if (!this.nodeStack.empty()) {
			int mother = this.nodeStack.peek();
			GeneralisedSuffixTreeNode motherNode = ((GeneralisedSuffixTreeNode) suffixTreeAppl.nodes[mother]);

			motherNode.getStartPositionOfSuffix().addAll(nodeList);

		}
		// node(nr)
		out.printTag("node", true, 1, true);
		out.printTag("number", true, 2, false);
		out.printInt(stackedNodeNr);
		out.printTag("number", false, 0, true);
		// edge label
		out.printTag("label", true, 2, false);
		out.print(label);
		out.printTag("label", false, 0, true);

		int freq = nodeList.size();
		out.printTag("frequency", true, 2, false);
		out.print(freq);
		out.printTag("frequency", false, 0, true);
		out.printTag("type", true, 2, true);

		for (int i = 0; i < freq; i++) {
			out.printTag("patternInfo", true, 3, true);
			TextStartPosInfo inf = nodeList.get(i);

			out.printTag("typeNr", true, 4, false);
			out.printInt(inf.unit);
			out.printTag("typeNr", false, 0, true);
			out.printTag("pattern", true, 4, false);
			out.printInt(inf.text);
			out.printTag("pattern", false, 0, true);
			out.printTag("startpos", true, 4, false);
			out.printInt(inf.startPositionOfSuffix);
			out.printTag("startpos", false, 0, true);
			out.printTag("patternInfo", false, 3, true);
		}
		out.printTag("type", false, 2, true);
		out.printTag("node", false, 1, true);
	}
}