package modules.suffixTree.suffixMain;

import java.util.logging.Logger;

import modules.suffixTree.output.SuffixTreeRepresentation;
import modules.suffixTree.suffixTree.applications.ResultSuffixTreeNodeStack;
import modules.suffixTree.suffixTree.applications.ResultToRepresentationListener;
import modules.suffixTree.suffixTree.applications.SuffixTreeAppl;
import modules.suffixTree.suffixTree.applications.TreeWalker;
import modules.suffixTree.suffixTree.node.activePoint.ExtActivePoint;
import modules.suffixTree.suffixTree.node.info.End;
import modules.suffixTree.suffixTree.node.nodeFactory.GeneralisedSuffixTreeNodeFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TmpTest {

	private static Logger LOGGER = Logger.getLogger("test");

	private static char TERMINATOR = '$';

	public static void main(String... strings) {

		String text = "Peter liest ein rotes Buch$Maria liest ein grünes Buch$Paul kauft ein rotes Buch$ Peter liest ein rotes Buch$Maria liest ein grünes Buch$Paul kauft ein rotes Buch$Maria liest ein grünes Buch$Paul kauft ein rotes Buch$ Peter liest ein rotes Buch$Maria liest ein grünes Buch$Maria liest ein grünes Buch$Paul kauft ein rotes Buch$ Peter liest ein rotes Buch$ Peter liest ein rotes Buch$Paul kauft ein rotes Buch$";

		// test = "abab$";

		// start and end indices regulate which portion of the input we are
		// reading at any given moment
		int start = 0;
		int end = text.indexOf(TERMINATOR, start);

//		GeneralisedSuffixTreeMain.test(text);

		if (end != -1) {

			// The suffix tree used to read the input is a generalised suffix
			// tree for a text of the length of the input string
			final SuffixTreeAppl suffixTreeAppl = new SuffixTreeAppl(text.length(),
					new GeneralisedSuffixTreeNodeFactory());

			// set some variables to regulate flow in SuffixTree classes
			suffixTreeAppl.unit = 0;
			suffixTreeAppl.oo = new End(Integer.MAX_VALUE / 2);

			// traverse the first portion of the input string
			// TODO comment explaining why extActivePoint has to be null here
			suffixTreeAppl.phases(text, start, end + 1, null);
			start = end+1;

			// traverse the remaining portions of the input string
			ExtActivePoint extActivePoint;
			String nextText;
			start = end + 1;
			end = text.indexOf(TERMINATOR, start);
			while (end != -1) {
				// each cycle represents a text read
				suffixTreeAppl.textNr++;
				
				// TODO comment explaining what setting the active point does
				nextText = text.substring(start, end + 1);
				extActivePoint = suffixTreeAppl.longestPath(nextText, 0, 1, start, true);
				if (extActivePoint == null) {
					LOGGER.warning(" GeneralisedSuffixTreeMain activePoint null");
					break;
				}
				
				// TODO comment explaining the use of oo and extActivePoint
				// 		why has this to happen here instead of inside phases()
				suffixTreeAppl.oo = new End(Integer.MAX_VALUE / 2);
				suffixTreeAppl.phases(text, start + extActivePoint.phase, end + 1, extActivePoint);
				
				// reset text window for the next cycle
				start = end + 1;
				end = text.indexOf(TERMINATOR, start);
			}

			// apparently this needs to be statically set in order that any
			// result listener works correctly
			ResultSuffixTreeNodeStack.suffixTree = suffixTreeAppl;

			// build an object to hold a representation of the tree for output
			// and add it's nodes via a listener.
			final SuffixTreeRepresentation suffixTreeRepresentation = new SuffixTreeRepresentation();
			final ResultToRepresentationListener listener = new ResultToRepresentationListener(suffixTreeRepresentation);
			final TreeWalker treeWalker = new TreeWalker();
			suffixTreeRepresentation.setUnitCount(0);
			suffixTreeRepresentation.setNodeCount(suffixTreeAppl.getCurrentNode());
			treeWalker.walk(suffixTreeAppl.getRoot(), suffixTreeAppl, listener);

			// serialize the representation to JSON
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			final String output = gson.toJson(suffixTreeRepresentation);
			LOGGER.info(output);
		} else {
			LOGGER.warning("Did not find terminator char: " + TERMINATOR);
		}

	}

}
