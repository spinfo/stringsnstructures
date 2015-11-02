package modules.suffixTree.suffixTree.applications;

import java.util.ArrayList;
import java.util.logging.Logger;

import modules.suffixTree.suffixTree.applications.event.MyEntryEvent;
import modules.suffixTree.suffixTree.applications.event.MyExitEvent;
import modules.suffixTree.suffixTree.node.GeneralisedSuffixTreeNode;
import modules.suffixTree.suffixTree.node.textStartPosInfo.TextStartPosInfo;

/**
 * A TreeWalkerListener that outputs the trees nodes directly to an
 * XmlPrintWriter.
 */
public class ResultToXmlListener implements ITreeWalkerListener {

	private static final Logger LOGGER = Logger.getGlobal();
	// .getLogger(ResultToXmlListener.class.getName());

	XmlPrintWriter out;

	// cstr
	public ResultToXmlListener(XmlPrintWriter o) {
		this.out = o;
	}

	@Override
	public void entryaction(MyEntryEvent e) {
		if ((Integer) e.getSource() != 1)
			LOGGER.info("Listener Entry node:" + e.getSource() + "  "
					+ ResultSuffixTreeNodeStack.suffixTree.edgeString((Integer) e.getSource()));
		ResultSuffixTreeNodeStack.stack.push((Integer) e.getSource());
	}

	@Override
	public void exitaction(MyExitEvent e) {
		LOGGER.info("Listener Exit node:" + e.getSource());
		// if
		// (ResultSuffixTreeNodeStack.printSuffixTree.st.nodes[(Integer)e.getSource()].children.values().isEmpty())
		String label = ResultSuffixTreeNodeStack.writeStack();

		int nrOfNode = ResultSuffixTreeNodeStack.stack.pop();
		GeneralisedSuffixTreeNode node = ((GeneralisedSuffixTreeNode) ResultSuffixTreeNodeStack.suffixTree.nodes[nrOfNode]);
		ArrayList<TextStartPosInfo> nodeList = node.getStartPositionOfSuffix();
		if (!ResultSuffixTreeNodeStack.stack.empty()) {
			int mother = ResultSuffixTreeNodeStack.stack.peek();
			LOGGER.info("Listener Exit mother:" + mother);
			GeneralisedSuffixTreeNode motherNode = ((GeneralisedSuffixTreeNode) ResultSuffixTreeNodeStack.suffixTree.nodes[mother]);

			motherNode.getStartPositionOfSuffix().addAll(nodeList);
			LOGGER.info("Listener freq mother: " + motherNode.getStartPositionOfSuffix().size() + " freq node "
					+ nodeList.size());
		}
		// node(nr)
		out.printTag("node", true, 1, true);
		out.printTag("number", true, 2, false);
		out.printInt(nrOfNode);
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