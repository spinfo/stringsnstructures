package suffixTree;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import modules.tree_building.suffixTree.AbstractResultNodeStackListener;
import modules.tree_building.suffixTree.BaseSuffixTree;
import modules.tree_building.suffixTree.GST;
import modules.tree_building.suffixTree.Node;
import modules.tree_building.suffixTree.NodePosition;
import modules.tree_building.suffixTree.TreeWalker;

public class AbstractResultNodeStackListenerTest {

	/**
	 * Inner class used to test the abstract class's mechanisms.
	 */
	private class TestListener extends AbstractResultNodeStackListener {

		final BaseSuffixTree tree;

		// the amount of nodes processed by this listener
		int nodesProcessed = 0;

		TestListener(BaseSuffixTree tree) {
			super(tree);
			this.tree = tree;
		}

		public void process(int nodeNr, List<Node> path, int pathLength, int level) {
			final Node node = tree.getNode(nodeNr);

			// test that the level reported by the listener matches the amount
			// of nodes on the stack (assumes that listening started on the
			// root)
			assertEquals(path.size(), level);

			// test that the path length matches the actual sum of all edges
			// length
			String edges = path.stream().map(n -> tree.edgeString(n)).reduce("", String::concat);
			edges += tree.edgeString(node);
			assertEquals(edges.length(), pathLength);

			// check that each node is a child of the previous node on the stack
			// up until the current node
			Node current = null;
			Node expectedNext = null;
			Node next = null;
			char edgeBegin = '\0';
			for (int i = 0; i < path.size() - 1; i++) {
				current = path.get(i);
				expectedNext = path.get(i + 1);
				assertNotNull(current);
				assertNotNull(expectedNext);

				edgeBegin = tree.edgeString(expectedNext).charAt(0);
				next = tree.getNode(current.getNext(edgeBegin));

				assertNotNull(next);
				assertEquals(expectedNext, next);
			}

			// the last node on the stack should be the parent of the node being
			// processed
			if (path.size() > 0) {
				edgeBegin = tree.edgeString(node).charAt(0);
				assertEquals(tree.getNode(path.get(path.size() - 1).getNext(edgeBegin)), node);
			}
			// if no node is left on the stack, root is the node being processed
			else {
				assertEquals(tree.getNode(tree.getRoot()), node);
			}

			// compare the set of leaves found by travelling down the edges to
			// the set aggregated by the node stack listener
			final Set<Node> expectedLeaves = findLeaves(node);
			assertTrue(expectedLeaves.equals(node.getLeaves()));

			// Check that the path length can be used to correctly identify the
			// path leading to the current node. For every node position
			// reconstruct the path from input text, node position and path
			// length, than traverse the tree and compare the node encountered
			// to the current node.
			if (nodeNr != tree.getRoot()) {
				String pathToCurrent = null;
				Node atEndOfPath = null;
				for (NodePosition position : node.getPositions()) {
					pathToCurrent = tree.getText().substring(position.getEnd() - node.getPathLength(),
							position.getEnd());
					atEndOfPath = GstTestHelper.checkPathExists(tree, pathToCurrent);
					assertTrue(node.equals(atEndOfPath));
				}
			} else {
				// the root node should simply have a path length of zero
				assertEquals(0, node.getPathLength());
			}

			// increment the amount of nodes processed for external checking
			nodesProcessed += 1;
		}

		// Setup the recursive call to find all leaves of a node.
		private Set<Node> findLeaves(Node node) {
			Set<Node> result = new HashSet<Node>();
			findLeaves(node, result, node);
			return result;
		}

		// Recursively travel to the leaves of the current node and put all
		// leaves into the provided set.
		private void findLeaves(Node current, Set<Node> leaves, Node initial) {
			for (char c : current.getEdgeBegins()) {
				findLeaves(tree.getNode(current.getNext(c)), leaves, initial);
			}
			if (current.isTerminal() && current != initial) {
				leaves.add(current);
			}
		}

	}

	@Test
	public void test() {
		final String input = "aa bb acd$bb acd aa$Petra liest das Buch$Maria liest das Buch$mississippi$romane$romanus$romulus$rubens$ruber$rubicon$rubicundus$";
		BaseSuffixTree tree = null;

		// just build the generalised suffix tree
		try {
			tree = GST.buildGST(new BufferedReader(new StringReader(input)), null);
		} catch (Exception e) {
			fail("Failed to build the generalised suffix tree.");
		}

		TestListener listener = new TestListener(tree);

		// the main testing is done by walking the tree and triggering the tests
		// in TestListener.process()
		try {
			TreeWalker.walk(tree.getRoot(), tree, listener);
		} catch (IOException e) {
			fail("Mysteriously this raised an IOException without IO happening.");
		}

		// test that all nodes were processed
		assertEquals(tree.getNodeAmount(), listener.nodesProcessed);
	}

}
