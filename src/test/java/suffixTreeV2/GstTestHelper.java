package suffixTreeV2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import modules.suffixTreeV2.BaseSuffixTree;
import modules.suffixTreeV2.GST;
import modules.suffixTreeV2.Node;
import modules.suffixTreeV2.SuffixTree;

/**
 * Helper class that provides static test methods related to the creation and
 * use of a Generalised Suffix Tree
 */
class GstTestHelper {

	/**
	 * Builds a generalised suffix tree for the input string and triggers some
	 * basic checks for tree consistency.
	 * 
	 * @param input
	 *            the input text for the GST
	 * @return A Generalised Suffix Tree over the specified input text
	 */
	protected static SuffixTree buildAndCheckTree(final String input) {
		return buildAndCheckTree(input, null);
	}

	/**
	 * Builds a generalised suffix tree for the input string and triggers some
	 * basic checks for tree consistency.
	 * 
	 * @param input
	 *            the input text for the GST
	 * @param typeContextNrs
	 *            a list of integers indicating with which textNr the context
	 *            for a type ends
	 * @return A Generalised Suffix Tree over the specified input text
	 */
	protected static SuffixTree buildAndCheckTree(final String input, final List<Integer> typeContextNrs) {
		SuffixTree result = null;
		try {
			result = GST.buildGST(new StringReader(input), typeContextNrs);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Building the tree failed with error: " + e.getMessage());
		}

		generalTreeCheck(result, input);

		return result;
	}

	/**
	 * Performs some basic checks, that a tree built with GST is internally
	 * consistent
	 * 
	 * @param tree
	 *            The BaseSuffixTree to check.
	 * @param expectedInput
	 *            The input the tree is expected to report as having been read.
	 */
	protected static void generalTreeCheck(BaseSuffixTree tree, String expectedInput) {
		final String input = tree.getText();

		// check the input texts
		assertTrue("Input and reported input should match", expectedInput.equals(input));

		// check nodes
		for (int i = tree.getRoot(); i < tree.getNodeAmount(); i++) {
			checkNodeConsistency(tree, tree.getNode(i));
		}

		// check text numbers and noted texts
		int expectedNextBegin = 0;
		int actualBegin = -1;
		String text = null;
		for (int i = 0; i < tree.textNrsAmount(); i++) {
			actualBegin = tree.getTextBegin(i);
			assertTrue("Beginning of text " + i + " should continue at end of the last text, (is " + actualBegin
					+ ", should be: " + expectedNextBegin + ")", actualBegin == expectedNextBegin);
			expectedNextBegin = tree.getTextEnd(i) + 1;
			text = tree.getInputText(i);
			assertTrue("Every text should end on terminator symbol '$'.", text.charAt(text.length() - 1) == '$');
			assertTrue(text.equals(expectedInput.substring(actualBegin, expectedNextBegin)));
		}
		// check that the next text would begin at the input end, i.e. that
		// inputTexts exhaust the input
		assertEquals(expectedNextBegin, input.length());

		// check that the amount of texts matches those of the separators
		assertEquals(StringUtils.countMatches(input, "$"), tree.textNrsAmount());
	}

	/**
	 * Checks that the paths (each path is a String[] of edge strings) exist in
	 * the given tree and checks, that the number of expected nodes matches the
	 * count reported by the tree as well as the actual number of nodes
	 * encountered while traversing the path. Together this should mean, that
	 * only those paths paths provided exist in the given tree.
	 * 
	 * @param tree
	 *            The tree to check.
	 * @param paths
	 *            The paths to check for.
	 * @param expectedNodeCount
	 *            The number of unique nodes that the tree should have.
	 */
	protected static void checkPaths(final BaseSuffixTree tree, final String[][] paths, int expectedNodeCount) {
		assertNotNull("Tree should exist.", tree);

		// keep a record of all different nodes encountered while checking
		final Set<Node> nodesEncountered = new HashSet<Node>();

		for (final String[] edges : paths) {
			// checking the single path actually happens here
			nodesEncountered.addAll(checkPath(tree, edges));
		}

		assertEquals("Expected and reported count of nodes differ.", expectedNodeCount, tree.getNodeAmount());
		assertEquals("Reported node count and number of nodes encountered differ.", tree.getNodeAmount(),
				nodesEncountered.size());
	}

	/**
	 * Checks that all edge strings of the path appear in the order specified.
	 * Also checks that the last node encountered is a terminal node. Eventually
	 * returns all different nodes encountered while following the path.
	 * 
	 * @param tree
	 *            The tree to check.
	 * @param edges
	 *            A String[] of edges to check fot.
	 * @return The unique nodes encountered while traversing the tree.
	 */
	protected static Set<Node> checkPath(final BaseSuffixTree tree, final String[] edges) {
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

		assertTrue("Last node should be a terminal node: " + actualEdge + ".", node.isTerminal());
		return nodesEncountered;
	}

	/**
	 * Checks that the suffix tree has a path corresponding to the search term
	 * ending in a terminal node.
	 * 
	 * @param tree
	 *            The tree to traverse.
	 * @param path
	 *            The total path string to check for as a single word without
	 *            edges.
	 * @return The node encountered at the end of the path.
	 */
	protected static Node checkFullPathExists(final BaseSuffixTree tree, final String path) {
		final Node node = checkPathExists(tree, path);
		assertTrue(node.isTerminal());
		return node;
	}

	/**
	 * Checks that the suffix tree has a path corresponding to the search term
	 * ending in some node.
	 * 
	 * @param tree
	 *            The tree to traverse.
	 * @param path
	 *            The total path string to check for as a single word without
	 *            edges.
	 * @return The node encountered at the end of the path.
	 */
	protected static Node checkPathExists(final BaseSuffixTree tree, final String path) {
		final String message = "Expected path: " + path + ": ";

		Integer nextNode = tree.getRoot();
		Node node = tree.getNode(nextNode);
		char[] edge = {};
		int edgeIdx = 0;
		char actual = '\0';

		for (char expected : path.toCharArray()) {
			if (edge.length == edgeIdx) {
				nextNode = node.getNext(expected);
				assertNotNull(message + "Should find a next node for char: " + expected, nextNode);

				node = tree.getNode(nextNode);
				assertNotNull(message + "Should get a node for node number: " + nextNode, node);

				edge = tree.edgeString(nextNode).toCharArray();
				assertTrue(message + "Edge string should not be empty, node " + nextNode, (edge.length > 0));
				edgeIdx = 0;
			}
			actual = edge[edgeIdx];
			assertEquals("Chars not matching. Expected '" + expected + "', got '" + actual + "'.", expected, actual);
			edgeIdx += 1;
		}
		assertEquals("No characters should be left at the end of the last edge.", edge.length, edgeIdx);

		return node;
	}

	/**
	 * Check that a node is internally consistent, e.g. that all it's positions
	 * specify the same text that also matches the reported input text etc.
	 * 
	 * @param tree
	 *            The tree the node is a part of.
	 * @param node
	 *            The node to check.
	 */
	protected static void checkNodeConsistency(final BaseSuffixTree tree, final Node node) {
		// root node does not have positions or an edge string, so it needs no
		// checking
		if (node == tree.getNode(tree.getRoot()))
			return;

		final String text = tree.getText();
		final String expected = tree.edgeString(node);
		String actual = null;

		for (int i = 0; i < node.getPositionsAmount(); i++) {
			// check that each position would show the same edge string
			actual = text.substring(node.getStart(i), node.getEnd(i));
			assertTrue(expected.equals(actual));

			// check that the node's edge string is a substring of the text
			// reported for the positions textNr
			assertTrue(tree.getInputText(node.getTextNr(i)).contains(actual));

			// check that the current position does not equal another of the
			// same node.
			for (int j = 0; j < node.getPositionsAmount(); j++) {
				if (i == j)
					continue;
				assertFalse("Positions should not be the same.", node.getStart(i) == node.getStart(j)
						&& node.getEnd(i) == node.getEnd(j) && node.getTextNr(i) == node.getTextNr(j));
			}
		}

		if (node.isTerminal()) {
			assertTrue("Terminal node's text should end on '$'.", actual.charAt(actual.length() - 1) == '$');
		}
	}

	/**
	 * Checks that the path given by a String ends in a leaf node and ensures
	 * that the leaf has exactly the type context numbers, that are expected.
	 * 
	 * @param tree
	 *            The tree to check in.
	 * @param path
	 *            The path to check for it's reported contexts.
	 * @param expectedTypeContexts
	 *            The internal type context numbers to expect. (with duplicates)
	 */
	protected static void checkTypeContexts(final BaseSuffixTree tree, final String path,
			final List<Integer> expectedTypeContexts) {
		// retrieve the leaf node for the path to be checked
		final Node leaf = checkFullPathExists(tree, path);
		final List<Integer> actualContextNrs = new ArrayList<Integer>();

		// collect type context numbers given in the leaf
		for (int i = 0; i < leaf.getPositionsAmount(); i++) {
			actualContextNrs.add(leaf.getTypeContext(i));
		}
		// sort both lists and check for equality
		Collections.sort(actualContextNrs);
		Collections.sort(expectedTypeContexts);
		assertTrue("The expected type contexts should match the actual ones, but they are: " + actualContextNrs,
				actualContextNrs.equals(expectedTypeContexts));
	}

}
