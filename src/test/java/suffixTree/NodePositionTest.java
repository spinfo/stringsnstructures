package suffixTree;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;

import org.junit.Test;

import modules.suffixTree.NodePosition;
import modules.suffixTree.NodePositionEnd;

public class NodePositionTest {

	@Test
	public void test() {
		HashSet<NodePosition> hashSet = new HashSet<NodePosition>();
		TreeSet<NodePosition> treeSet = new TreeSet<NodePosition>();
		
		NodePosition position = new NodePosition(0, new NodePositionEnd(0), 0, 0);
		NodePosition same = new NodePosition(0, new NodePositionEnd(0), 0, 0);

		NodePosition diffStart = new NodePosition(7, new NodePositionEnd(0), 0, 0);
		NodePosition diffEnd = new NodePosition(0, new NodePositionEnd(7), 0, 0);
		NodePosition diffTextNr = new NodePosition(0, new NodePositionEnd(0), 7, 0);
		NodePosition diffContext = new NodePosition(0, new NodePositionEnd(0), 0, 7);
		
		// test equals
		// equals should work by object identity as well as by values
		assertTrue(position.equals(same));
		assertTrue(position.equals(position));
		
		// nodes with different values should always differ
		assertFalse(position.equals(diffStart));
		assertFalse(position.equals(diffEnd));
		assertFalse(position.equals(diffTextNr));
		assertFalse(position.equals(diffContext));
		
		// test the comparator
		// inserting an equal position into a set should not actually add the value
		hashSet.add(position);
		treeSet.add(position);
		assertEquals(1, hashSet.size());
		assertEquals(1, treeSet.size());
		hashSet.add(same);
		treeSet.add(same);
		assertEquals(1, hashSet.size());
		assertEquals(1, treeSet.size());
		
		// adding different elements, should change the size though
		hashSet.addAll(Arrays.asList(diffStart, diffEnd, diffTextNr, diffContext));
		treeSet.addAll(Arrays.asList(diffStart, diffEnd, diffTextNr, diffContext));
		assertEquals(5, hashSet.size());
		assertEquals(5, treeSet.size());
	}

}
