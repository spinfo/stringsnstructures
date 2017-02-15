package misc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import modules.tree_building.suffixTreeClustering.data.Type;

public class EqualityHashcodeTest {

	private Type test1_0_1;
	private Type test1_0_2;
	private Type test2_0_1;
	private Type test1_1_1;
	private Type test2_1_1;

	@Before
	public void setUp() {
		test1_0_1 = new Type();
		test1_0_1.setID(0);
		test1_0_1.setTypeString("test1");

		test1_0_2 = new Type();
		test1_0_2.setID(0);
		test1_0_2.setTypeString("test1");

		test2_0_1 = new Type();
		test2_0_1.setID(0);
		test2_0_1.setTypeString("test2");

		test1_1_1 = new Type();
		test1_1_1.setID(1);
		test1_1_1.setTypeString("test1");

		test2_1_1 = new Type();
		test2_1_1.setID(1);
		test2_1_1.setTypeString("test2");
	}

	@Test
	public void testEquals() {
		Assert.assertTrue(test1_0_1.equals(test1_0_2));
		Assert.assertTrue(test1_0_2.equals(test1_0_1));
		Assert.assertFalse(test1_0_1.equals(test2_1_1));

		Assert.assertEquals(test1_0_1, test1_0_2);
		Assert.assertNotEquals(test1_0_1, test2_0_1);
		Assert.assertNotEquals(test1_0_1, test1_1_1);
		Assert.assertNotEquals(test1_0_2, test2_0_1);
		Assert.assertNotEquals(test1_0_2, test1_1_1);
	}

	@Test
	public void testHashCode() {
		Assert.assertTrue(test1_0_1.hashCode() == test1_0_2.hashCode());
		Assert.assertFalse(test1_0_1.hashCode() == test1_1_1.hashCode());
		Assert.assertFalse(test1_0_1.hashCode() == test2_0_1.hashCode());
		Assert.assertFalse(test1_0_1.hashCode() == test2_1_1.hashCode());
	}

	@Test
	public void testCollection() {

		Set<Type> set1 = new HashSet<Type>();
		Assert.assertTrue(set1.add(test1_0_1));
		Assert.assertFalse(set1.add(test1_0_2)); // already contained
		Assert.assertTrue(set1.add(test1_1_1));
		Assert.assertTrue(set1.add(test2_0_1));
		Assert.assertTrue(set1.add(test2_1_1));

		Assert.assertTrue(set1.contains(test1_0_1));

		set1.remove(test1_0_1);

		Assert.assertFalse(set1.contains(test1_0_1));

		Assert.assertFalse(set1.contains(test1_0_2));
	}

	@Test
	public void testMap() {
		Map<Type, Integer> map = new HashMap<Type, Integer>();
		Assert.assertEquals(null, map.put(test1_0_1, 0));
		Assert.assertEquals(0, (int) map.put(test1_0_2, 1));
		Assert.assertEquals(null, map.put(test1_1_1, 2));
		Assert.assertEquals(null, map.put(test2_0_1, 3));
		Assert.assertEquals(null, map.put(test2_1_1, 4));

		Assert.assertTrue(map.containsKey(test1_0_1));
		Assert.assertTrue(map.containsKey(test1_0_2));

		Assert.assertEquals(1, (int) map.get(test1_0_1));
		Assert.assertEquals(1, (int) map.get(test1_0_2));
	}
}