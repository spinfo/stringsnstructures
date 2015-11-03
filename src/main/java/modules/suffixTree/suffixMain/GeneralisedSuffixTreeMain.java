package modules.suffixTree.suffixMain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import modules.suffixTree.suffixTree.applications.ResultSuffixTreeNodeStack;
import modules.suffixTree.suffixTree.applications.ResultToXmlListener;
import modules.suffixTree.suffixTree.applications.SuffixTreeAppl;
import modules.suffixTree.suffixTree.applications.TreeWalker;
import modules.suffixTree.suffixTree.applications.XmlPrintWriter;
import modules.suffixTree.suffixTree.node.activePoint.ExtActivePoint;
import modules.suffixTree.suffixTree.node.info.End;
import modules.suffixTree.suffixTree.node.nodeFactory.GeneralisedSuffixTreeNodeFactory;

import common.LoggerConfigurator;
import common.TextInfo;

public class GeneralisedSuffixTreeMain {

	// test is true if text is NOT read from file but given by hand
	private static boolean test = false;
	private static final Logger LOGGER = Logger.getGlobal();
	private static final char TERMINATOR = '$';
	// .getLogger(GeneralisedSuffixTreeMain.class.getName());

	public static SuffixTreeAppl st;
	private ArrayList<Integer> unitList = new ArrayList<Integer>();
	private ArrayList<String> typeList = new ArrayList<String>();
	private static int nrTypes = 0;
	private String text;
	private static String in;

	/**
	 * Private constructor to restrict usage to static public methods.
	 **/
	private GeneralisedSuffixTreeMain() {

		if (test) {
			text = in;
			LOGGER.info("test: " + text);
		} else {
			LOGGER.info("GeneralisedSuffixTreeMain Path: " + TextInfo.getWorkspacePath());
			readCorpusAndUnitListFromFile();
		}
		int start = 0, end;
		ExtActivePoint activePoint;
		String nextText;
		// Hint: if texts input is alphabetically sorted, it may consist of
		// types and its tokens (here: texts, each terminated by the TERMINATOR
		// char).
		// The tokens (texts) are numbered (continuously), and the last token of
		// a type is numbered by an integer called unit. All unit integers are
		// stored a unit list.

		// create suffix tree for first text
		// look for terminator symbol
		end = text.indexOf(TERMINATOR, start);
		if (end != -1) {

			LOGGER.finer("GeneralisedSuffixTreeMain: first suffix tree: start: " + start + " end " + TERMINATOR + ": "
					+ end + " substring: " + text.substring(start, end + 1));
			st = new SuffixTreeAppl(text.length(), new GeneralisedSuffixTreeNodeFactory());

			LOGGER.info("GeneralisedSuffixTreeMain cstr text: " + text + "   " + st.textNr);
			st.unit = 0;
			st.oo = new End(Integer.MAX_VALUE / 2);
			// phases, first text, ExtActivePoint null
			st.phases(text, start, end + 1, null/* ExtActivePoint */);

			st.printTree("SuffixTree", -1, -1, -1);

			start = end + 1;

			LOGGER.finer("GeneralisedSuffixTreeMain: vor while text: " + text + " start: " + start);

			// next texts (ending in terminator symbol), add to suffix tree in phase n
			while ((end = text.indexOf(TERMINATOR, start)) != -1) {
				st.textNr++;
				// units are integers which mark texts; each unit number
				// marks the end of texts corresponding to types in
				// (alphabetically) ordered input
				if ((!test) && (unitList.get(st.unit) == st.textNr)) {
					st.unit++;
					LOGGER.finer("unit: " + st.unit + "  textNr: " + st.textNr + " type "
							+ typeList.get(st.unit));
				}

				nextText = text.substring(start, end + 1);
				LOGGER.finer("GeneralisedSuffixTreeMain:  start: " + start + " end " + TERMINATOR + ": " + end
						+ " nextText: " + nextText + "  textNr:  " + st.textNr);

				activePoint = st.longestPath(nextText, 0/* phase */, 1/* node */, start/* active_edge */,
						true/* generalized suffix tree */);
				if (activePoint == null) {
					LOGGER.warning(" GeneralisedSuffixTreeMain activePoint null");
					break;
				} else {
					LOGGER.finer("GeneralisedSuffixTreeMain activePoint active_node: " + activePoint.active_node
							+ " active_edge: " + activePoint.active_edge + " active_length: "
							+ activePoint.active_length + " start: " + start + " phase: " + activePoint.phase);
				}
				// new End element
				st.oo = new End(Integer.MAX_VALUE / 2);
				st.phases(text, start + activePoint.phase, end + 1, activePoint);

				LOGGER.fine("GeneralisedSuffixTreeMain start: " + start + " end: " + end);

				start = end + 1;
			}
		} else {
			LOGGER.warning("Did not find terminator char: " + TERMINATOR);
		}

		st.printTree("Generalized SuffixTree", -1, -1, -1);

		if (!test) {
			persistSuffixTreeToXmlFile();
		}
	}

	private void readCorpusAndUnitListFromFile() {
		try (BufferedReader brText = new BufferedReader(new FileReader(TextInfo.getKwipPath()))) {

			text = brText.readLine();
			LOGGER.info("Text read:\n" + text);

			String line, type;
			BufferedReader brInt = new BufferedReader(new FileReader(TextInfo.getKwipUnitPath()));
			BufferedReader brType = new BufferedReader(new FileReader(TextInfo.getKwipTypePath()));
			while ((line = brInt.readLine()) != null) {
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

	public static void persistSuffixTreeToXmlFile() {

		ResultSuffixTreeNodeStack.setSuffixTree(st);
		try {
			final String writePath = TextInfo.getSuffixTreePath();
			XmlPrintWriter out = new XmlPrintWriter(new FileWriter(writePath));
			out.printTag("output", true, 0, true);
			out.printTag("units", true, 1, false);
			out.printInt(nrTypes);
			out.printTag("units", false, 0, true);

			out.printTag("nodes", true, 1, false);
			out.printInt(st.getCurrentNode());
			out.printTag("nodes", false, 0, true);

			ResultToXmlListener listener = new ResultToXmlListener(out);
			TreeWalker treeWalker = new TreeWalker();
			treeWalker.walk(st.getRoot(), st, listener);
			LOGGER.fine("rootnr: " + st.getRoot());
			out.printTag("output", false, 0, true);
			LOGGER.info("Writing finished. Wrote to: " + writePath);
			out.close();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/**
	 * The default run method reads a text defined by TextInfo, processes it
	 * into a SuffixTree, then writes multiple .dot files representing the tree
	 * graphically as well as an xml-representation. All Input and Output is
	 * configured by the config file behind TextInfo.
	 */
	public static void run() {

		LoggerConfigurator.configGlobal();

		LOGGER.entering(GeneralisedSuffixTreeMain.class.getName(), "run");

		new GeneralisedSuffixTreeMain();
		LOGGER.exiting(GeneralisedSuffixTreeMain.class.getName(), "run");
	}

	public static void test(String input) {
		in = input;
		test = true;
		run();
	}
}
