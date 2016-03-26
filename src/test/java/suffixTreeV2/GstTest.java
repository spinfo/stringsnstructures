package suffixTreeV2;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.Test;

import modules.suffixTreeV2.BaseSuffixTree;
import modules.suffixTreeV2.GST;
import modules.suffixTreeV2.Node;
import modules.suffixTreeV2.SuffixTree;

public class GstTest {
	
	private static final Logger LOGGER = Logger.getLogger(GstTest.class.getName());

	@Test
	public void testSimpleInputs() {
		SuffixTree tree = null;
		String[][] expectedPaths = null;
		String input = null;

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
		
		// test the example from http://stackoverflow.com/a/9513423/1879728
		tree = buildTree("abcabxabcd$");
		expectedPaths = new String[][] {
			{ "$" },
			{ "ab", "xabcd$" },
			{ "ab", "c", "d$" },
			{ "ab", "c", "abxabcd$" },
			{ "b", "xabcd$" },
			{ "b", "c", "d$" },
			{ "b", "c", "abxabcd$" },
			{ "c", "abxabcd$" },
			{ "c", "d$" },
			{ "d$" },
			{ "xabcd$" }
		};
		checkTree(tree, expectedPaths, 17);
		
		// test multiple repetitions with some additions
		tree = buildTree("aab$bba$aabccd$aabbba$bbaaab$");
		expectedPaths = new String[][] {
			{ "$" },
			{ "a", "$" },
			{ "a", "a", "ab$" },
			{ "a", "a", "b", "$" },
			{ "a", "a", "b", "bba$" },
			{ "a", "a", "b", "ccd$" },
			{ "a", "b", "$" },
			{ "a", "b", "bba$" },
			{ "a", "b", "ccd$" },
			{ "b", "$" },
			{ "b", "a", "$"},
			{ "b", "a", "aab$" },
			{ "b", "b", "a", "$" },
			{ "b", "b", "a", "aab$" },
			{ "b", "b", "ba$" },
			{ "b", "ccd$" },
			{ "c", "cd$" },
			{ "c", "d$" },
			{ "d$" }
		};
		checkTree(tree, expectedPaths, 29);
		
		// reverting the order of words should not change the paths
		tree = buildTree("bbaaab$aabbba$aabccd$bba$aab$");
		checkTree(tree, expectedPaths, 29);
		
		// shuffling the words should not change the paths either
		tree = buildTree("aab$aabbba$bbaaab$bba$aabccd$");
		checkTree(tree, expectedPaths, 29);

		// another simple test
		tree = buildTree("petra$peter$");
		expectedPaths = new String[][] {
			{ "$" },
			{ "pet", "ra$" },
			{ "pet", "er$" },
			{ "e", "r$" },
			{ "e", "t", "er$" },
			{ "e", "t", "ra$" },
			{ "t", "er$" },
			{ "t", "ra$" },
			{ "r", "$" },
			{ "r", "a$" },
			{ "a$" }
		};
		checkTree(tree, expectedPaths, 17);

		// test all combinations of length 3 for an alphabet of 4
		input = "aaa$aab$aac$aad$aba$abb$abc$abd$aca$acb$acc$acd$ada$adb$adc$add$baa$bab$bac$bad$bba$bbb$bbc$bbd$bca$bcb$bcc$bcd$bda$bdb$bdc$bdd$caa$cab$cac$cad$cba$cbb$cbc$cbd$cca$ccb$ccc$ccd$cda$cdb$cdc$cdd$daa$dab$dac$dad$dba$dbb$dbc$dbd$dca$dcb$dcc$dcd$dda$ddb$ddc$ddd$";
		tree = buildTree(input);
		expectedPaths = new String[][] {
			{"$"},
			// permutations of length 1 & 2
			{"a", "$"}, {"b", "$"}, {"c", "$"}, {"d", "$"},
			{"a", "a", "$"}, {"a", "b", "$"}, {"a", "c", "$"}, {"a", "d", "$"}, {"b", "a", "$"}, {"b", "b", "$"}, {"b", "c", "$"}, {"b", "d", "$"}, {"c", "a", "$"}, {"c", "b", "$"}, {"c", "c", "$"}, {"c", "d", "$"}, {"d", "a", "$"}, {"d", "b", "$"}, {"d", "c", "$"}, {"d", "d", "$"},
			// permutations of length 3
            {"a", "a", "a$"}, {"a", "a", "b$"}, {"a", "a", "c$"}, {"a", "a", "d$"}, {"a", "b", "a$"}, {"a", "b", "b$"}, {"a", "b", "c$"}, {"a", "b", "d$"}, {"a", "c", "a$"}, {"a", "c", "b$"}, {"a", "c", "c$"}, {"a", "c", "d$"}, {"a", "d", "a$"}, {"a", "d", "b$"}, {"a", "d", "c$"}, {"a", "d", "d$"},
            {"b", "a", "a$"}, {"b", "a", "b$"}, {"b", "a", "c$"}, {"b", "a", "d$"}, {"b", "b", "a$"}, {"b", "b", "b$"}, {"b", "b", "c$"}, {"b", "b", "d$"}, {"b", "c", "a$"}, {"b", "c", "b$"}, {"b", "c", "c$"}, {"b", "c", "d$"}, {"b", "d", "a$"}, {"b", "d", "b$"}, {"b", "d", "c$"}, {"b", "d", "d$"},
            {"c", "a", "a$"}, {"c", "a", "b$"}, {"c", "a", "c$"}, {"c", "a", "d$"}, {"c", "b", "a$"}, {"c", "b", "b$"}, {"c", "b", "c$"}, {"c", "b", "d$"}, {"c", "c", "a$"}, {"c", "c", "b$"}, {"c", "c", "c$"}, {"c", "c", "d$"}, {"c", "d", "a$"}, {"c", "d", "b$"}, {"c", "d", "c$"}, {"c", "d", "d$"},
            {"d", "a", "a$"}, {"d", "a", "b$"}, {"d", "a", "c$"}, {"d", "a", "d$"}, {"d", "b", "a$"}, {"d", "b", "b$"}, {"d", "b", "c$"}, {"d", "b", "d$"}, {"d", "c", "a$"}, {"d", "c", "b$"}, {"d", "c", "c$"}, {"d", "c", "d$"}, {"d", "d", "a$"}, {"d", "d", "b$"}, {"d", "d", "c$"}, {"d", "d", "d$"}
		};
		checkTree(tree, expectedPaths, 106); // one root, 5 on 1st level (a,b,c,d,$), (4*5) on 2nd level, (4*4*5) on 3rd

		// test words not sharing any letter (tree of depth 1)
		tree = buildTree("ABCDEF$abcdef$ghijkl$mnopqr$STUVWX$");
		expectedPaths = new String[][] {
			{ "$" },
			{ "ABCDEF$" }, { "BCDEF$" }, { "CDEF$" }, { "DEF$" }, { "EF$" }, { "F$" },  
			{ "abcdef$" }, { "bcdef$" }, { "cdef$" }, { "def$" }, { "ef$" }, { "f$" }, 
			{ "ghijkl$" }, { "hijkl$" }, { "ijkl$" }, { "jkl$" }, { "kl$" }, { "l$" }, 
			{ "mnopqr$" }, { "nopqr$" }, { "opqr$" }, { "pqr$" }, { "qr$" }, { "r$" }, 
			{ "STUVWX$" }, { "TUVWX$" }, { "UVWX$" }, { "VWX$" }, { "WX$" }, { "X$" }, 
		};
		checkTree(tree, expectedPaths, 32); // 6 words of length 5 plus one root and one single '$'
		
		// a sequence with repeats and a unique starting letter
		// from: http://docs.seqan.de/seqan/1.2/streeSentinel.png
		tree = buildTree("mississippi$");
		expectedPaths = new String[][] {
			{ "$" },
			{ "mississippi$" },
			{ "i", "$" },
			{ "i", "ppi$" },
			{ "i", "ssi", "ppi$" },
			{ "i", "ssi", "ssippi$" },
			{ "p", "i$" },
			{ "p", "pi$" },
			{ "s", "i", "ppi$" },
			{ "s", "i", "ssippi$"},
			{ "s", "si", "ppi$" },
			{ "s", "si", "ssippi$" }
		};
		checkTree(tree, expectedPaths, 19);

		// seven words with some overlaps of a few letters length
		tree = buildTree("romane$romanus$romulus$rubens$ruber$rubicon$rubicundus$");
		expectedPaths = new String[][] {
			{ "$" },
			{ "r", "$" },
			{ "r", "om", "an", "e$" },
			{ "r", "om", "an", "us$" },
			{ "r", "om", "ulus$" },
			{ "r", "ub", "e", "r$" },
			{ "r", "ub", "e", "ns$" },
			{ "r", "ub", "ic", "on$" },
			{ "r", "ub", "ic", "undus$" },
			{ "o", "m", "an", "e$" },
			{ "o", "m", "an", "us$" },
			{ "o", "m", "ulus$" },
			{ "o", "n$" },
			{ "m", "an", "e$" },
			{ "m", "an", "us$" },
			{ "m", "ulus$" },
			{ "an", "e$" },
			{ "an", "us$" },
			{ "n", "$" },
			{ "n", "e$" },
			{ "n", "dus$" },
			{ "n", "us$" },
			{ "n", "s$" },
			{ "e", "$" },
			{ "e", "ns$" },
			{ "e", "r$" },
			{ "u", "b", "e", "ns$" },
			{ "u", "b", "e", "r$" },
			{ "u", "b", "ic", "on$" },
			{ "u", "b", "ic", "undus$" },
			{ "u", "s$" },
			{ "u", "lus$" },
			{ "u", "ndus$" },
			{ "s$" },
			{ "lus$" },
			{ "b", "e", "ns$" },
			{ "b", "e", "r$" },
			{ "b", "ic", "on$" },
			{ "b", "ic", "undus$" },
			{ "ic", "on$" },
			{ "ic", "undus$" },
			{ "c", "on$" },
			{ "c", "undus$" },
			{ "dus$" }
		};
		checkTree(tree, expectedPaths, 68);
	}
	
	@Test
	public void testTypeContexts() {
		SuffixTree tree = null;
		List<Integer> contextEndIndices = null;
		List<Integer> expectedTypeContexts = null;
		String input = null;

		// The input string is a KWIP output for "aa cc$bb cc$bb dd$". This has types 'aa', 'bb', 'cc' and 'dd'
		// type contexts accordingly are:
		// type 0: 'aa' => in text 1
		// type 1: 'bb' => in text 2,3
		// type 2: 'cc' => in texts 1,2
		// type 3: 'dd' => in text 3
		// The input then is the concatenation of texts 1 + 2,3 + 1,2 + 3:
		input = "aa cc$bb cc$bb dd$aa cc$bb cc$bb dd$";
		// the type context end indices then give ranges for that set of sentences
		// 'aa' => context ends with sentence 1
		// 'bb' => context ends with sentence 3
		// 'cc' => context ends with sentence 5
		// 'dd' => context ends with sentence 6
		contextEndIndices = Arrays.asList(1, 2, 5, 6);
		tree = buildTree(input, contextEndIndices);
	
		// the suffix ' cc' then should exist once in the contexts of 'aa' (type 0) and 'bb' (type 1)
		// and twice within it's own context (type 2)
		expectedTypeContexts = Arrays.asList(0, 1, 2, 2);
		checkTypeContexts(tree, " cc$", expectedTypeContexts);
		
		// the same should be true for 'cc$' without leading blank
		checkTypeContexts(tree, "cc$", expectedTypeContexts);
		
		// check tree nodes for consistency
		for(int i = tree.getRoot()+1; i < tree.getNodeAmount(); i++) {
			checkNodeConsistency(tree, tree.getNode(i));;
		}
	}
	
	private SuffixTree buildTree(final String input) {
		return buildTree(input, null);
	}

	private SuffixTree buildTree(final String input, final List<Integer> typeContextNrs) {
		SuffixTree result = null;
		try {
			result = GST.buildGST(new StringReader(input), typeContextNrs);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Building the tree failed with error: " + e.getMessage());
		}
		return result;
	}

	// Checks that the paths (each path is a String[] of edge strings) exist in the given tree and
	// checks, that the number of expected nodes matches the count reported by the tree as well as the
	// actual number of nodes encountered while traversing the path.
	// Together this should mean, that only those paths paths provided exist in the given tree.
	//
	// Additionally every node encountered is checked for consistency for each of it's positions.
	private void checkTree(final BaseSuffixTree tree, final String[][] paths, int expectedNodeCount) {
		LOGGER.info("Checking tree with input: " + tree.getText());
		
		assertNotNull("Tree should exist.", tree);

		// keep a record of all different nodes encountered while checking
		final Set<Node> nodesEncountered = new HashSet<Node>();

		for (final String[] edges : paths) {
			// checking the single path actually happens here
			nodesEncountered.addAll(checkPath(tree, edges));
		}

		assertEquals("Expected and reported count of nodes differ.",
				expectedNodeCount, tree.getNodeAmount());
		assertEquals("Reported node count and number of nodes encountered differ.",
				tree.getNodeAmount(), nodesEncountered.size());
		
		for (Node node : nodesEncountered) {
			checkNodeConsistency(tree, node);
		}
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

		assertTrue("Last node should be a terminal node: " + actualEdge + ".", node.isTerminal());
		return nodesEncountered;
	}
	
	// Checks that the suffix tree has a path corresponding to the search term ending in
	// a terminal node. Returns that node or fails if it isn't present.
	private final Node checkPathExists(final BaseSuffixTree tree, final String path) {
		final String message = "Expected path: " + path + ": ";

		Integer nextNode = tree.getRoot();
		Node node = tree.getNode(nextNode);
		char[] edge = {};
		int edgeIdx = 0;
		char actual = '\0';

		for (char expected : path.toCharArray()) {
			if(edge.length == edgeIdx) {
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

		assertTrue(node.isTerminal());
		assertEquals("No characters should be left at the end of the last edge.", edge.length, edgeIdx);
		return node;
	}
	
	private void checkNodeConsistency(final BaseSuffixTree tree, Node node) {
		// root node does not have positions or an edge string, so it needs no checking
		if (node == tree.getNode(tree.getRoot())) return;

		final String text = tree.getText();
		final String expected = tree.edgeString(node);
		String actual = null;

		for(int i = 0; i < node.getPositionsAmount(); i++) {
			// check that each position would show the same edge string
			actual = text.substring(node.getStart(i), node.getEnd(i));
			assertTrue(expected.equals(actual));

			// check that the current position does not equal another of the same node.
			for(int j = 0; j < node.getPositionsAmount(); j++) {
				if (i == j) continue;
				assertFalse("Positions should not be the same.",
						node.getStart(i) == node.getStart(j) &&
						node.getEnd(i) == node.getEnd(j) &&
						node.getTextNr(i) == node.getTextNr(j));
			}
		}
	}
	
	// Checks that the path given by a String ends in a leaf node and ensures that the leaf has exactly the
	// type context numbers, that are expected
	private void checkTypeContexts(final BaseSuffixTree tree, final String path, final List<Integer> expectedTypeContexts) {
		// retrieve the leaf node for the path to be checked
		final Node leaf = checkPathExists(tree, path);
		final List<Integer> actualContextNrs = new ArrayList<Integer>();
		
		// collect type context numbers given in the leaf
		for(int i = 0; i < leaf.getPositionsAmount(); i++) {
			actualContextNrs.add(leaf.getTypeContext(i));
		}
		// sort both lists and check for equality
		Collections.sort(actualContextNrs);
		Collections.sort(expectedTypeContexts);
		assertTrue("The expected type contexts should match the actual ones.", actualContextNrs.equals(expectedTypeContexts));
	}

}
