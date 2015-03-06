package suffixTree.suffixMain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import suffixTree.suffixTree.SuffixTree;
import suffixTree.suffixTree.applications.ResultListener;
import suffixTree.suffixTree.applications.ResultSuffixTreeNodeStack;
import suffixTree.suffixTree.applications.SuffixTreeAppl;
import suffixTree.suffixTree.applications.TreeWalker;
import suffixTree.suffixTree.applications.XmlPrintWriter;
import suffixTree.suffixTree.node.activePoint.ExtActivePoint;
import suffixTree.suffixTree.node.info.End;
import suffixTree.suffixTree.node.nodeFactory.GeneralisedSuffixTreeNodeFactory;
import util.LoggerConfigurator;
import util.TextInfo;

public class GeneralisedSuffixTreeMain {

	private static final Logger LOGGER = Logger.getGlobal();
	// .getLogger(GeneralisedSuffixTreeMain.class.getName());

	private SuffixTreeAppl st;
	private ArrayList<Integer> unitList = new ArrayList<Integer>();
	private ArrayList<String> typeList = new ArrayList<String>();
	private int nrTypes = 0;
	private String text;

	void readCorpusAndUnitListFromFile() {
		try (BufferedReader brText = new BufferedReader(new FileReader(
				TextInfo.getKwipPath()))) {

			text = brText.readLine();
			LOGGER.info("Text read:\n" + text);

			String line, type;
			BufferedReader brInt = new BufferedReader(new FileReader(
					TextInfo.getKwipUnitPath()));
			BufferedReader brType = new BufferedReader(new FileReader(
					TextInfo.getKwipTypePath()));
			while ((line = brInt.readLine()) != null) {
				// LOGGER.fine("Next line: " + line);
				unitList.add(Integer.parseInt(line));
			}
			brInt.close();
			while ((type = brType.readLine()) != null) {
				LOGGER.finest("Type: " + type);
				typeList.add(type);
				nrTypes++;
			}
			brType.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public GeneralisedSuffixTreeMain(boolean test) throws Exception {
		if (!test) {
			LOGGER.info("GeneralisedSuffixTreeMain Path: " + TextInfo.getWorkspacePath());
			readCorpusAndUnitListFromFile();
		}
		int start = 0, end;
		SuffixTreeAppl.unit = 0;
		ExtActivePoint activePoint;
		String nextText;
		// Hint: if texts input is aplphabetically sorted, it may consist of
		// types and its tokens (here: texts, each terminated by '$').
		// The tokens (texts) are numbered (continuously), and the last token of
		// a type is numbered by an integer called unit. All unit integers are
		// stored a unit list.
		// readCorpusAndUnitListFromFile();
		// String text = "aba$";
		// String text = "aba$abc$";
		// String text = "aba$abc$dce$";
		//
		// String text = "abc$dce$";
		// String text = "banana$ananas$";
		// String text = "babxba$xabxa$";//example Pekka Kilpeläinen
		// String text = "aba$aca$";
		// String text = "aaa$banana$";
		// 0 1 2
		// 012345678901234567890123456789
		// text "aba$abc$abdce$abdce$dce$abdcff$";
		LOGGER.info("GeneralisedSuffixTreeMain cstr text: " + text + "   "
				+ SuffixTreeAppl.textNr);
		// create suffix tree for first text
		// look for terminator symbol

		if ((end = text.indexOf('$', start)) != -1) {

			// --------------------------------------
			LOGGER.finer("GeneralisedSuffixTreeMain: first suffix tree: start: "
					+ start
					+ " end $: "
					+ end
					+ " substring: "
					+ text.substring(start, end + 1));
			SuffixTree.oo = new End(Integer.MAX_VALUE / 2);
			st = new SuffixTreeAppl(text.length(),
					new GeneralisedSuffixTreeNodeFactory());
			// phases, first text, ExtActivePoint null
			st.phases(text, start, end + 1, null// ExtActivePoint
			);

			st.printTree("SuffixTree", -1, -1, -1);

			// ----------------------------------------------*/

			start = end + 1;

			LOGGER.finer("GeneralisedSuffixTreeMain: vor while text: " + text
					+ " start: " + start);

			// next texts (ending in terminator symbol), add to suffix tree in
			// phase n
			while ((end = text.indexOf('$', start)) != -1) {
				SuffixTreeAppl.textNr++;
				// units are integers which mark texts; each unit number
				// marks the end of texts corresponding to types in
				// (alphabetically) ordered input
				if (!test) {
					if (unitList.get(SuffixTreeAppl.unit) == SuffixTreeAppl.textNr)

					{
						SuffixTreeAppl.unit++;
						LOGGER.finer("unit: " + SuffixTreeAppl.unit
								+ "  textNr: " + SuffixTreeAppl.textNr
								+ " type " + typeList.get(SuffixTreeAppl.unit));
					}
				}
				nextText = text.substring(start, end + 1);
				LOGGER.finer("GeneralisedSuffixTreeMain:  start: " + start
						+ " end $: " + end + " nextText: " + nextText
						+ "  textNr:  " + SuffixTreeAppl.textNr);

				// -----------------------------------------------------
				activePoint = st.longestPath(nextText, 0/* phase */, 1/* node */,
						start// active_edge
						, true// generalized suffix tree
						);
				if (activePoint == null) {
					LOGGER.warning(" GeneralisedSuffixTreeMain activePoint null");
					break;
				} else
					LOGGER.finer("GeneralisedSuffixTreeMain activePoint active_node: "
							+ activePoint.active_node
							+ " active_edge: "
							+ activePoint.active_edge
							+ " active_length: "
							+ activePoint.active_length
							+ " start: "
							+ start
							+ " phase: " + activePoint.phase);
				// new End element
				SuffixTree.oo = new End(Integer.MAX_VALUE / 2);
				st.phases(text, start + activePoint.phase, end + 1, activePoint);

				LOGGER.fine("GeneralisedSuffixTreeMain start: " + start
						+ " end: " + end);

				// ----------------------------------------*/
				start = end + 1;
			}
		}

		st.printTree("Generalized SuffixTree", -1, -1, -1);
		if (!test) {
			ResultSuffixTreeNodeStack.setPrintSuffixTree(st);
			try {
				XmlPrintWriter out = new XmlPrintWriter(new FileWriter(
						TextInfo.getSuffixTreePath()));
				out.printTag("output", true, 0, true);
				out.printTag("units", true, 1, false);
				out.printInt(nrTypes);
				out.printTag("units", false, 0, true);

				out.printTag("nodes", true, 1, false);
				out.printInt(st.getCurrentNode());
				out.printTag("nodes", false, 0, true);

				ResultListener listener = new ResultListener(out);
				TreeWalker treeWalker = new TreeWalker();
				treeWalker.walk(st.getRoot(), st, listener);
				LOGGER.fine("rootnr: " + st.getRoot());
				out.printTag("output", false, 0, true);
				out.close();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	public static void main(String... args) throws Exception {
		LoggerConfigurator.configGlobal();

		LOGGER.entering(GeneralisedSuffixTreeMain.class.getName(), "main");

		/*
		 * set to true for test, i.e. for input strings, not from file
		 */
		new GeneralisedSuffixTreeMain(false);

		LOGGER.exiting(GeneralisedSuffixTreeMain.class.getName(), "main");
	}
}
