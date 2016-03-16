package suffixTreeV2;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import modules.suffixTreeV2.BaseSuffixTree;
import modules.suffixTreeV2.GST;
import modules.suffixTreeV2.Node;
import modules.suffixTreeV2.SuffixTree;

public class GstTest {

	@Test
	public void test() {
		SuffixTree tree = null;
		String[][] expectedPaths = null;

		// test two different strings repeating a single letter
		tree = buildTree("aa$bb$");
		expectedPaths = new String[][] { 
			{ "$" },
			{ "a", "$" },
			{ "a", "a$" },
			{ "b", "$" },
			{ "b", "b$" }
		};
		checkTree(tree, expectedPaths, 8);

		// repeating the strings should result in the same paths
		tree = buildTree("aa$bb$aa$bb$aa$");
		checkTree(tree, expectedPaths, 8);

		// test the caterpillar
		tree = buildTree("aaaaaaaa$");
		expectedPaths = new String[][] { 
			{ "$" },
			{ "a", "$" },
			{ "a", "a", "$" },
			{ "a", "a", "a", "$" },
			{ "a", "a", "a", "a", "$" },
			{ "a", "a", "a", "a", "a", "$" },
			{ "a", "a", "a", "a", "a", "a", "$" },
			{ "a", "a", "a", "a", "a", "a", "a", "$" },
			{ "a", "a", "a", "a", "a", "a", "a", "a$" }
		};
		checkTree(tree, expectedPaths, 17);
	}

	private SuffixTree buildTree(final String input) {
		SuffixTree result = null;
		try {
			result = GST.buildGST(new StringReader(input), null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Building the tree failed with error: " + e.getMessage());
		}
		return result;
	}

	private void checkTree(final BaseSuffixTree tree, final String[][] paths, int expectedNodeCount) {
		assertNotNull("Tree should exist.", tree);

		// keep a record of all different nodes encountered while checking
		final Set<Node> nodesEncountered = new HashSet<Node>();

		for (final String[] edges : paths) {
			// checking the single path actually happens here
			nodesEncountered.addAll(checkPath(tree, edges));
		}

		int actualNodeCount = tree.getNodeAmount();
		assertEquals("Expected and actual count of nodes differ.", expectedNodeCount, actualNodeCount);
		assertEquals("Expected node count and number of nodes encountered differ.", expectedNodeCount,
				nodesEncountered.size());
	}

	// Checks that all edge strings of the path appear in the order specified.
	// Also checks that the last node encountered is a terminal node.
	// Eventually returns all different nodes encountered while following the path.
	private Set<Node> checkPath(final BaseSuffixTree tree, final String[] edges) {
		final Set<Node> nodesEncountered = new HashSet<Node>();
		Node node = tree.getNode(tree.getRoot());
		Integer nextNodeNr = -1;
		char begin = '\0';
		String actualEdge = null;

		nodesEncountered.add(node);

		for (final String expectedEdge : edges) {
			begin = expectedEdge.charAt(0);
			nextNodeNr = node.getNext(begin);

			assertNotNull("There should be a next node for char: " + begin, nextNodeNr);

			node = tree.getNode(nextNodeNr);
			assertNotNull("There should be a node for nodeNr: " + nextNodeNr + " (char: " + begin + ")", node);

			actualEdge = tree.edgeString(nextNodeNr);
			assertNotNull("Edge found should not be null.", actualEdge);
			assertNotEquals("Edge length should not be zero.", actualEdge.length(), 0);
			assertTrue("Expected edge: " + expectedEdge + " should equal actual edge: " + actualEdge,
					expectedEdge.equals(actualEdge));

			nodesEncountered.add(node);
		}

		assertTrue("Last node should be a terminal node.", node.isTerminal());
		return nodesEncountered;
	}

}
