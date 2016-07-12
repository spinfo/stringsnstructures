package suffixTree;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import modules.tree_building.suffixTree.SuffixTree;

public class GstTest {

	@Test
	public void testSimpleInputs() {
		SuffixTree tree = null;
		String[][] expectedPaths = null;
		String input = null;

		// test two different strings repeating a single letter
		tree = GstTestHelper.buildAndCheckTree("aa$bb$");
		expectedPaths = new String[][] { { "$" }, { "a", "$" }, { "a", "a$" }, { "b", "$" }, { "b", "b$" } };
		GstTestHelper.checkPaths(tree, expectedPaths, 8);

		// repeating the strings should result in the same paths
		tree = GstTestHelper.buildAndCheckTree("aa$bb$aa$bb$aa$");
		GstTestHelper.checkPaths(tree, expectedPaths, 8);

		// test the caterpillar
		tree = GstTestHelper.buildAndCheckTree("aaaaaaaa$");
		expectedPaths = new String[][] { { "$" }, { "a", "$" }, { "a", "a", "$" }, { "a", "a", "a", "$" },
				{ "a", "a", "a", "a", "$" }, { "a", "a", "a", "a", "a", "$" }, { "a", "a", "a", "a", "a", "a", "$" },
				{ "a", "a", "a", "a", "a", "a", "a", "$" }, { "a", "a", "a", "a", "a", "a", "a", "a$" } };
		GstTestHelper.checkPaths(tree, expectedPaths, 17);

		// test the example from http://stackoverflow.com/a/9513423/1879728
		tree = GstTestHelper.buildAndCheckTree("abcabxabcd$");
		expectedPaths = new String[][] { { "$" }, { "ab", "xabcd$" }, { "ab", "c", "d$" }, { "ab", "c", "abxabcd$" },
				{ "b", "xabcd$" }, { "b", "c", "d$" }, { "b", "c", "abxabcd$" }, { "c", "abxabcd$" }, { "c", "d$" },
				{ "d$" }, { "xabcd$" } };
		GstTestHelper.checkPaths(tree, expectedPaths, 17);

		// test multiple repetitions with some additions
		tree = GstTestHelper.buildAndCheckTree("aab$bba$aabccd$aabbba$bbaaab$");
		expectedPaths = new String[][] { { "$" }, { "a", "$" }, { "a", "a", "ab$" }, { "a", "a", "b", "$" },
				{ "a", "a", "b", "bba$" }, { "a", "a", "b", "ccd$" }, { "a", "b", "$" }, { "a", "b", "bba$" },
				{ "a", "b", "ccd$" }, { "b", "$" }, { "b", "a", "$" }, { "b", "a", "aab$" }, { "b", "b", "a", "$" },
				{ "b", "b", "a", "aab$" }, { "b", "b", "ba$" }, { "b", "ccd$" }, { "c", "cd$" }, { "c", "d$" },
				{ "d$" } };
		GstTestHelper.checkPaths(tree, expectedPaths, 29);

		// reverting the order of words should not change the paths
		tree = GstTestHelper.buildAndCheckTree("bbaaab$aabbba$aabccd$bba$aab$");
		GstTestHelper.checkPaths(tree, expectedPaths, 29);

		// shuffling the words should not change the paths either
		tree = GstTestHelper.buildAndCheckTree("aab$aabbba$bbaaab$bba$aabccd$");
		GstTestHelper.checkPaths(tree, expectedPaths, 29);

		// another simple test
		tree = GstTestHelper.buildAndCheckTree("petra$peter$");
		expectedPaths = new String[][] { { "$" }, { "pet", "ra$" }, { "pet", "er$" }, { "e", "r$" },
				{ "e", "t", "er$" }, { "e", "t", "ra$" }, { "t", "er$" }, { "t", "ra$" }, { "r", "$" }, { "r", "a$" },
				{ "a$" } };
		GstTestHelper.checkPaths(tree, expectedPaths, 17);

		// test all combinations of length 3 for an alphabet of 4
		input = "aaa$aab$aac$aad$aba$abb$abc$abd$aca$acb$acc$acd$ada$adb$adc$add$baa$bab$bac$bad$bba$bbb$bbc$bbd$bca$bcb$bcc$bcd$bda$bdb$bdc$bdd$caa$cab$cac$cad$cba$cbb$cbc$cbd$cca$ccb$ccc$ccd$cda$cdb$cdc$cdd$daa$dab$dac$dad$dba$dbb$dbc$dbd$dca$dcb$dcc$dcd$dda$ddb$ddc$ddd$";
		tree = GstTestHelper.buildAndCheckTree(input);
		expectedPaths = new String[][] { { "$" },
				// permutations of length 1 & 2
				{ "a", "$" }, { "b", "$" }, { "c", "$" }, { "d", "$" }, { "a", "a", "$" }, { "a", "b", "$" },
				{ "a", "c", "$" }, { "a", "d", "$" }, { "b", "a", "$" }, { "b", "b", "$" }, { "b", "c", "$" },
				{ "b", "d", "$" }, { "c", "a", "$" }, { "c", "b", "$" }, { "c", "c", "$" }, { "c", "d", "$" },
				{ "d", "a", "$" }, { "d", "b", "$" }, { "d", "c", "$" }, { "d", "d", "$" },
				// permutations of length 3
				{ "a", "a", "a$" }, { "a", "a", "b$" }, { "a", "a", "c$" }, { "a", "a", "d$" }, { "a", "b", "a$" },
				{ "a", "b", "b$" }, { "a", "b", "c$" }, { "a", "b", "d$" }, { "a", "c", "a$" }, { "a", "c", "b$" },
				{ "a", "c", "c$" }, { "a", "c", "d$" }, { "a", "d", "a$" }, { "a", "d", "b$" }, { "a", "d", "c$" },
				{ "a", "d", "d$" }, { "b", "a", "a$" }, { "b", "a", "b$" }, { "b", "a", "c$" }, { "b", "a", "d$" },
				{ "b", "b", "a$" }, { "b", "b", "b$" }, { "b", "b", "c$" }, { "b", "b", "d$" }, { "b", "c", "a$" },
				{ "b", "c", "b$" }, { "b", "c", "c$" }, { "b", "c", "d$" }, { "b", "d", "a$" }, { "b", "d", "b$" },
				{ "b", "d", "c$" }, { "b", "d", "d$" }, { "c", "a", "a$" }, { "c", "a", "b$" }, { "c", "a", "c$" },
				{ "c", "a", "d$" }, { "c", "b", "a$" }, { "c", "b", "b$" }, { "c", "b", "c$" }, { "c", "b", "d$" },
				{ "c", "c", "a$" }, { "c", "c", "b$" }, { "c", "c", "c$" }, { "c", "c", "d$" }, { "c", "d", "a$" },
				{ "c", "d", "b$" }, { "c", "d", "c$" }, { "c", "d", "d$" }, { "d", "a", "a$" }, { "d", "a", "b$" },
				{ "d", "a", "c$" }, { "d", "a", "d$" }, { "d", "b", "a$" }, { "d", "b", "b$" }, { "d", "b", "c$" },
				{ "d", "b", "d$" }, { "d", "c", "a$" }, { "d", "c", "b$" }, { "d", "c", "c$" }, { "d", "c", "d$" },
				{ "d", "d", "a$" }, { "d", "d", "b$" }, { "d", "d", "c$" }, { "d", "d", "d$" } };
		GstTestHelper.checkPaths(tree, expectedPaths, 106); // one root, 5 on
															// 1st level
		// (a,b,c,d,$), (4*5) on 2nd
		// level, (4*4*5) on 3rd

		// test words not sharing any letter (tree of depth 1)
		tree = GstTestHelper.buildAndCheckTree("ABCDEF$abcdef$ghijkl$mnopqr$STUVWX$");
		expectedPaths = new String[][] { { "$" }, { "ABCDEF$" }, { "BCDEF$" }, { "CDEF$" }, { "DEF$" }, { "EF$" },
				{ "F$" }, { "abcdef$" }, { "bcdef$" }, { "cdef$" }, { "def$" }, { "ef$" }, { "f$" }, { "ghijkl$" },
				{ "hijkl$" }, { "ijkl$" }, { "jkl$" }, { "kl$" }, { "l$" }, { "mnopqr$" }, { "nopqr$" }, { "opqr$" },
				{ "pqr$" }, { "qr$" }, { "r$" }, { "STUVWX$" }, { "TUVWX$" }, { "UVWX$" }, { "VWX$" }, { "WX$" },
				{ "X$" }, };
		GstTestHelper.checkPaths(tree, expectedPaths, 32); // 6 words of length
															// 5 plus one
		// root and one single '$'

		// a sequence with repeats and a unique starting letter
		// from: http://docs.seqan.de/seqan/1.2/streeSentinel.png
		tree = GstTestHelper.buildAndCheckTree("mississippi$");
		expectedPaths = new String[][] { { "$" }, { "mississippi$" }, { "i", "$" }, { "i", "ppi$" },
				{ "i", "ssi", "ppi$" }, { "i", "ssi", "ssippi$" }, { "p", "i$" }, { "p", "pi$" }, { "s", "i", "ppi$" },
				{ "s", "i", "ssippi$" }, { "s", "si", "ppi$" }, { "s", "si", "ssippi$" } };
		GstTestHelper.checkPaths(tree, expectedPaths, 19);

		// seven words with some overlaps of a few letters length
		tree = GstTestHelper.buildAndCheckTree("romane$romanus$romulus$rubens$ruber$rubicon$rubicundus$");
		expectedPaths = new String[][] { { "$" }, { "r", "$" }, { "r", "om", "an", "e$" }, { "r", "om", "an", "us$" },
				{ "r", "om", "ulus$" }, { "r", "ub", "e", "r$" }, { "r", "ub", "e", "ns$" }, { "r", "ub", "ic", "on$" },
				{ "r", "ub", "ic", "undus$" }, { "o", "m", "an", "e$" }, { "o", "m", "an", "us$" },
				{ "o", "m", "ulus$" }, { "o", "n$" }, { "m", "an", "e$" }, { "m", "an", "us$" }, { "m", "ulus$" },
				{ "an", "e$" }, { "an", "us$" }, { "n", "$" }, { "n", "e$" }, { "n", "dus$" }, { "n", "us$" },
				{ "n", "s$" }, { "e", "$" }, { "e", "ns$" }, { "e", "r$" }, { "u", "b", "e", "ns$" },
				{ "u", "b", "e", "r$" }, { "u", "b", "ic", "on$" }, { "u", "b", "ic", "undus$" }, { "u", "s$" },
				{ "u", "lus$" }, { "u", "ndus$" }, { "s$" }, { "lus$" }, { "b", "e", "ns$" }, { "b", "e", "r$" },
				{ "b", "ic", "on$" }, { "b", "ic", "undus$" }, { "ic", "on$" }, { "ic", "undus$" }, { "c", "on$" },
				{ "c", "undus$" }, { "dus$" } };
		GstTestHelper.checkPaths(tree, expectedPaths, 68);
	}

	@Test
	public void testBigTree() {
		final InputStream stream = getClass().getClassLoader().getResourceAsStream("line1K");
		assertNotNull(stream);
		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			final String input = reader.readLine();
			GstTestHelper.buildAndCheckTree(input);
		} catch (UnsupportedEncodingException e) {
			fail("Unsupported encoding: UTF-8. This is really weird and should never actually happen.");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Failed to read the test file.");
		}
	}

	@Test
	public void testTypeContexts() {
		SuffixTree tree = null;
		List<Integer> contextEndIndices = null;
		List<Integer> expectedTypeContexts = null;
		String input = null;

		// The input string is a KWIP output for "aa cc$bb cc$bb dd$". This has
		// types 'aa', 'bb', 'cc' and 'dd'
		// type contexts accordingly are:
		// type 0: 'aa' => in text 1
		// type 1: 'bb' => in text 2,3
		// type 2: 'cc' => in texts 1,2
		// type 3: 'dd' => in text 3
		// The input then is the concatenation of texts 1 + 2,3 + 1,2 + 3:
		input = "aa cc$bb cc$bb dd$aa cc$bb cc$bb dd$";
		// the type context end indices then give ranges for that set of
		// sentences
		// 'aa' => context ends with sentence 1
		// 'bb' => context ends with sentence 3
		// 'cc' => context ends with sentence 5
		// 'dd' => context ends with sentence 6
		contextEndIndices = Arrays.asList(1, 3, 5, 6);
		tree = GstTestHelper.buildAndCheckTree(input, contextEndIndices);

		// the amount of type contexts reported by the tree after building
		// should be correct
		assertEquals(4, tree.getTypeContextsAmount());

		// the suffix ' cc' then should exist once in the contexts of 'aa' (type
		// 0) and 'bb' (type 1)
		// and twice within it's own context (type 2)
		expectedTypeContexts = Arrays.asList(0, 1, 2, 2);
		GstTestHelper.checkTypeContexts(tree, " cc$", expectedTypeContexts);

		// the same should be true for 'cc$' without leading blank as well as
		// just "c"
		GstTestHelper.checkTypeContexts(tree, "cc$", expectedTypeContexts);
		GstTestHelper.checkTypeContexts(tree, "c$", expectedTypeContexts);

		// the suffix 'dd' exists once in the context of 'bb' (type 1) and once
		// in it's own context (type 3).
		expectedTypeContexts = Arrays.asList(1, 3);
		GstTestHelper.checkTypeContexts(tree, " dd$", expectedTypeContexts);
		GstTestHelper.checkTypeContexts(tree, "dd$", expectedTypeContexts);

		// "a cc" should report one position for 'aa' (type 0) and one for 'cc'
		// (type 2)
		GstTestHelper.checkTypeContexts(tree, "a cc$", Arrays.asList(0, 2));
	}
	
	@Test
	public void testFindPattern() {
		String input = "banana$New York$";
		SuffixTree tree = GstTestHelper.buildAndCheckTree(input);

		assertTrue(tree.findPattern("ba", tree.getRoot()));

		assertTrue(tree.findPattern("banana$"));
		assertTrue(tree.findPattern("banana"));
		assertTrue(tree.findPattern("banan"));
		assertTrue(tree.findPattern("bana"));
		assertTrue(tree.findPattern("ban"));
		assertTrue(tree.findPattern("ba"));
		assertTrue(tree.findPattern("b"));
		assertTrue(tree.findPattern("anana$"));
		assertTrue(tree.findPattern("nana$"));
		assertTrue(tree.findPattern("ana$"));
		assertTrue(tree.findPattern("na$"));
		assertTrue(tree.findPattern("a$"));
		assertTrue(tree.findPattern("$"));
		assertTrue(tree.findPattern("anana"));
		assertTrue(tree.findPattern("nan"));
		assertTrue(tree.findPattern("a"));
		
		assertTrue(tree.findPattern("New York$"));
		assertTrue(tree.findPattern("New York"));
		assertTrue(tree.findPattern("New Yor"));
		assertTrue(tree.findPattern("New Yo"));
		assertTrue(tree.findPattern("New Y"));
		assertTrue(tree.findPattern("New "));
		assertTrue(tree.findPattern("New"));
		assertTrue(tree.findPattern("Ne"));
		assertTrue(tree.findPattern("N"));
		assertTrue(tree.findPattern("ew York$"));
		assertTrue(tree.findPattern("w York$"));
		assertTrue(tree.findPattern(" York$"));
		assertTrue(tree.findPattern("York$"));
		assertTrue(tree.findPattern("ork$"));
		assertTrue(tree.findPattern("rk$"));
		assertTrue(tree.findPattern("k$"));
		assertTrue(tree.findPattern("ew York"));
		assertTrue(tree.findPattern("w Yor"));
		assertTrue(tree.findPattern(" Yo"));
		assertTrue(tree.findPattern("Y"));
		
		assertFalse(tree.findPattern("bananas"));
		assertFalse(tree.findPattern("New Yorks"));
		assertFalse(tree.findPattern("bas"));
		assertFalse(tree.findPattern("ananas"));
		assertFalse(tree.findPattern("na$N"));
		assertFalse(tree.findPattern("$N"));
	}

}
