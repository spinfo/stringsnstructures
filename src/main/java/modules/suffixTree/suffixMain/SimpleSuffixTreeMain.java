package modules.suffixTree.suffixMain;

import java.util.logging.Logger;

import modules.suffixTree.suffixTree.applications.SuffixTreeAppl;
import modules.suffixTree.suffixTree.node.activePoint.ExtActivePoint;
import modules.suffixTree.suffixTree.node.info.End;
import modules.suffixTree.suffixTree.node.nodeFactory.SimpleSuffixTreeNodeFactory;

import common.LoggerConfigurator;

public class SimpleSuffixTreeMain {
	private static final Logger LOGGER = Logger.getGlobal();
	// .getLogger(SimpleSuffixTreeMain.class.getName());

	SuffixTreeAppl st;

	public SimpleSuffixTreeMain(String text) throws Exception {
		LOGGER.fine("SimpleSuffixTreeMain cstr text: " + text);

		st = new SuffixTreeAppl(text.length(),
				new SimpleSuffixTreeNodeFactory());
		st.oo = new End(Integer.MAX_VALUE / 2);

		// phases
		st.phases(text, 0, text.length(), null);
		if (st.search("anana$", 0, 1/* root */)!=null)
			LOGGER.info("\nsearch found");
		else
			LOGGER.info("\nsearch not found");
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
		LoggerConfigurator.configGlobal();

		LOGGER.info("SimpleSuffixTreeMain Start");

		String text = "abcabxabcd$"; // "babxba$";//"banana$";
		new SimpleSuffixTreeMain(text);

		LOGGER.info("SimpleSuffixTreeMain End");
	}
}