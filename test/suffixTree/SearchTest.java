package suffixTree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import suffixTree.suffixTree.SuffixTree;
import suffixTree.suffixTree.applications.SuffixTreeAppl;
import suffixTree.suffixTree.node.Node;
import suffixTree.suffixTree.node.activePoint.ExtActivePoint;
import suffixTree.suffixTree.node.info.End;
import suffixTree.suffixTree.node.nodeFactory.GeneralisedSuffixTreeNodeFactory;
import util.TextInfo;

public class SearchTest {

	@Test
	public void test() {
		int startPos = 0;
		int startNode = st.getRoot();

		String search = "Test";

		boolean found = st.search(search, startPos, startNode);

		Assert.assertTrue("Didn't find search text", found);

		Node[] nodes = st.nodes;

		char[] text = st.text;
		System.out.println(text);

		int leafCount = SuffixTree.leafCount;
		int end = SuffixTree.oo.getEnd();
		int textNr = SuffixTree.textNr;
		int unit = SuffixTree.unit;

		System.out.println(String.format(
				"Leaf count: %s\nEnd: %s\nTextNr: %s\nUnit: %s\n", leafCount,
				end, textNr, unit));

		for (Node node : nodes) {
			if (null != node)
				System.out.println(node.edgeLength(startNode));
		}
	}

	private SuffixTreeAppl st;
	private List<Integer> unitList = new ArrayList<Integer>();
	private List<String> typeList = new ArrayList<String>();
	private String text;

	@Before
	public void setUpSuffixTree() throws Exception {
		readCorpusAndUnitListFromFile();

		int start = 0, end;
		SuffixTreeAppl.unit = 0;
		ExtActivePoint activePoint;
		String nextText;

		if ((end = text.indexOf('$', start)) != -1) {

			// --------------------------------------
			SuffixTree.oo = new End(Integer.MAX_VALUE / 2);
			st = new SuffixTreeAppl(text.length(),
					new GeneralisedSuffixTreeNodeFactory());

			st.phases(text, start, end + 1, null);

			st.printTree("SuffixTree", -1, -1, -1);

			start = end + 1;

			// next texts (ending in terminator symbol), add to suffix tree in
			// phase n
			while ((end = text.indexOf('$', start)) != -1) {
				SuffixTreeAppl.textNr++;
				// units are integers which mark texts; each unit number
				// marks the end of texts corresponding to types in
				// (alphabetically) ordered input
				if (unitList.get(SuffixTreeAppl.unit) == SuffixTreeAppl.textNr) {
					SuffixTreeAppl.unit++;

				}
				nextText = text.substring(start, end + 1);

				activePoint = st.longestPath(nextText, 0, 1, start, true);
				if (activePoint == null) {
					System.err
							.println(" GeneralisedSuffixTreeMain activePoint null");
					break;
				}
				SuffixTree.oo = new End(Integer.MAX_VALUE / 2);
				st.phases(text, start + activePoint.phase, end + 1, activePoint);

				start = end + 1;
			}
		}
		st.printTree("Generalized SuffixTree", -1, -1, -1);
	}

	private void readCorpusAndUnitListFromFile() {
		try (BufferedReader brText = new BufferedReader(new FileReader(
				TextInfo.getKwipPath()))) {

			text = brText.readLine();

			String line, type;

			BufferedReader brInt = new BufferedReader(new FileReader(
					TextInfo.getKwipUnitPath()));

			while ((line = brInt.readLine()) != null) {
				unitList.add(Integer.parseInt(line));
			}
			brInt.close();

			BufferedReader brType = new BufferedReader(new FileReader(
					TextInfo.getKwipTypePath()));
			while ((type = brType.readLine()) != null) {
				typeList.add(type);
			}
			brType.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}