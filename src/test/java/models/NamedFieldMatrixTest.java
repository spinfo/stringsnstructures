package models;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class NamedFieldMatrixTest {

	private static String CSV = ",data1,data2,data3,data4\n" + "set1,,0.4,3.5,153.543\n" + "set2,2.1,2.5,1.2,9.41\n"
			+ "set3,14.64,0.136,2.6,9.41\n";

	@Test
	public void testParseCSV() {
		try {
			NamedFieldMatrix matrix = NamedFieldMatrix.parseCSV(CSV, ",");
			Double value = matrix.getValue("set1", "data1");
			assertTrue(value.doubleValue() == 0.0d);
			value = matrix.getValue("set2", "data3");
			assertTrue(value.doubleValue() == 1.2d);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e.getMessage());
		}
	}

	@Test
	public void testAddValue() {
		try {
			NamedFieldMatrix matrix = new NamedFieldMatrix();
			matrix.addValue("set1", "data1", 1.1d);
			matrix.addValue("set1", "data2", 1.2d);
			matrix.addValue("set1", "data3", 1.3d);
			matrix.addValue("set2", "data1", 2.1d);
			matrix.addValue("set2", "data2", 2.2d);
			matrix.addValue("set2", "data3", 2.3d);
			Double value = matrix.getValue("set2", "data2");
			assertTrue(value.doubleValue() == 2.2d);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e.getMessage());
		}
	}

	@Test
	public void csvOutputTest() {
		try {
			NamedFieldMatrix matrix = NamedFieldMatrix.parseCSV(CSV, ",");
			StringBuffer sb = new StringBuffer(matrix.csvHeader());
			for (int i = 0; i < matrix.getRowAmount(); i++) {
				sb.append(matrix.csvLine(i));
			}
			assertTrue(sb.toString().equals(CSV));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e.getMessage());
		}
	}

	@Test
	public void contractionTest() {
		try {
			NamedFieldMatrix matrix = new NamedFieldMatrix();

			// begin with edge case: reducing an empty matrix
			matrix.contract();
			assertTrue("Empty matrix should contract to zero length", matrix.getValues().length == 0);

			// adding values to an emptied matrix should work
			String[] rowLabels = { "row1", "row2", "row3", "row4" };
			String[] colLabels = { "col1", "col2", "col3" };
			for (int i = 0; i < rowLabels.length; i++) {
				for (int j = 0; j < colLabels.length; j++) {
					matrix.addValue(rowLabels[i], colLabels[j], (double) i * j);
				}
			}

			// contracting that matrix again should give us 4 rows of length 3
			matrix.contract();
			assertTrue("Amount of rows should equal the amount entered", matrix.getValues().length == rowLabels.length);
			for (int i = 0; i < rowLabels.length; i++) {
				assertTrue("Amount of columns for row " + i + " should equal the maximum amount entered.",
						matrix.getValues()[i].length == colLabels.length);
			}

			// check that the values are the ones entered above
			double val = 0d;
			for (int i = 0; i < rowLabels.length; i++) {
				for (int j = 0; j < colLabels.length; j++) {
					val = matrix.getValue(rowLabels[i], colLabels[j]);
					assertTrue("Value retrieved should match the one entered", val == ((double) i * j));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e.getMessage());
		}
	}

}
