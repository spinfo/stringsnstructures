package modules.suffixTree.applications;

import java.util.List;

import modules.suffixTree.SuffixTree;
import modules.suffixTree.node.GeneralisedSuffixTreeNode;
import modules.suffixTree.node.TextStartPosInfo;

/**
 * A TreeWalkerListener that outputs the trees nodes directly to an
 * XmlPrintWriter.
 * 
 * Extends AbstractNodeStackListener such that information on the child nodes is
 * available on the exit action.
 */
public class ResultToXmlListener extends AbstractNodeStackListener implements ITreeWalkerListener {

	// the XmlPrintWriter to use for writing
	private final XmlPrintWriter out;

	public ResultToXmlListener(SuffixTree suffixTree, XmlPrintWriter xmlPrintWriter) {
		// call parent constructor to setup the nodeStack
		super(suffixTree);

		this.out = xmlPrintWriter;
	}

	@Override
	public void entryaction(int nodeNr, int level) throws Exception {
		super.entryaction(nodeNr, level);
	}

	@Override
	public void exitaction(int nodeNr, int level) throws Exception {
		// get the current node and node label from the superclass
		// that node matches the one identified by param nodeNr
		final String label = super.getCurrentNodeLabel();
		final GeneralisedSuffixTreeNode node = super.getCurrentNode();

		// Retrieve the position information. The superclass has made sure that
		// the position information of all children is included in this list
		final List<TextStartPosInfo> occurenceList = node.getStartPositionInformation();

		// node(nr)
		out.printTag("node", true, 1, true);
		out.printTag("number", true, 2, false);
		out.printInt(nodeNr);
		out.printTag("number", false, 0, true);
		// edge label
		out.printTag("label", true, 2, false);
		out.print(label);
		out.printTag("label", false, 0, true);

		int freq = occurenceList.size();
		out.printTag("frequency", true, 2, false);
		out.print(freq);
		out.printTag("frequency", false, 0, true);
		out.printTag("type", true, 2, true);

		for (int i = 0; i < freq; i++) {
			out.printTag("patternInfo", true, 3, true);
			TextStartPosInfo inf = occurenceList.get(i);

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

		// make sure that the node stack is handled correctly
		super.exitaction(nodeNr, level);
	}
}