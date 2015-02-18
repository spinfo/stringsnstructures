package suffixTree.suffixMain;

import java.util.logging.Logger;

import suffixTree.suffixTree.SuffixTree;
import suffixTree.suffixTree.applications.SuffixTreeAppl;
import suffixTree.suffixTree.node.activePoint.ExtActivePoint;
import suffixTree.suffixTree.node.info.End;
import suffixTree.suffixTree.node.nodeFactory.SimpleSuffixTreeNodeFactory;
import util.LoggerConfigurator;

public class SimpleSuffixTreeMain {
	private static final Logger LOGGER = Logger.getGlobal();
	// .getLogger(SimpleSuffixTreeMain.class.getName());

	SuffixTreeAppl st;

	public SimpleSuffixTreeMain(String text) throws Exception {
		LOGGER.fine("SimpleSuffixTreeMain cstr text: " + text);

		SuffixTree.oo = new End(Integer.MAX_VALUE / 2);
		st = new SuffixTreeAppl(text.length(),
				new SimpleSuffixTreeNodeFactory());

		// phases
		st.phases(text, 0, text.length(), null);
		System.out.println("\n");
		if (st.search("anana$", 0, 1/* root */))
			LOGGER.info("\nsearch true");
		else
			LOGGER.info("\nsearch false");
		System.out.println();
		ExtActivePoint activePoint = st.longestPath("nanyx", 0, 1, 0, false);
		if (activePoint == null)
			LOGGER.warning("activePoint null");
		else
			LOGGER.finest("activePoint active_node: " + activePoint.active_node
					+ " active_edge: " + activePoint.active_edge
					+ " active_length: " + activePoint.active_length
					+ " phase: " + activePoint.phase);
		st.printTree("SuffixTree", -1, -1, -1);
		st.printText();
	}

	public static void main(String... args) throws Exception {
		LoggerConfigurator.configLogger(SimpleSuffixTreeMain.class.getName());

		LOGGER.info("SimpleSuffixTreeMain Start");

		String text = "abcabxabcd$"; // "babxba$";//"banana$";
		new SimpleSuffixTreeMain(text);

		LOGGER.info("SimpleSuffixTreeMain End");
	}
}