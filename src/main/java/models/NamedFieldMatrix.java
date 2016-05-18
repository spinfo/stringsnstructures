package models;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * A dynamically resizing two-dimensional Array whose fields can be adressed by
 * pairs of Strings as well as pairs of numbers.
 * 
 * Supports output to a csv-Table and can be read from a csv-Table.
 */
public class NamedFieldMatrix {

	// the actual values in a 2-dimensional list
	private double[][] values;

	// current maximum of allocated elements
	private int colMax = 100;
	private int rowMax = 100;

	// current amount of columns and rows set, equals the next index to set
	private int colAmount = 0;
	private int rowAmount = 0;

	// maps rows of the table to a row name and vice versa
	private final Map<Integer, String> rowsToRowNames;
	private final Map<String, Integer> rowNamesToRows;

	// maps columns of the table to a column name no and vice versa
	private final Map<Integer, String> colsToColNames;
	private final Map<String, Integer> colNamesToCols;

	private String delimiter = ",";

	public NamedFieldMatrix() {
		this.values = new double[rowMax][];

		this.rowNamesToRows = new TreeMap<String, Integer>();
		this.colNamesToCols = new TreeMap<String, Integer>();

		this.rowsToRowNames = new TreeMap<Integer, String>();
		this.colsToColNames = new TreeMap<Integer, String>();
	}

	/**
	 * Adds value to the current value of the field designated by rowName and
	 * columnName. Returns the new value.
	 * 
	 * @param rowName
	 *            Name of row
	 * @param columnName
	 *            Name of column
	 * @param value
	 *            Value
	 * @return new value
	 */
	public double addValue(String rowName, String columnName, double value) {
		int row = getOrAddRow(rowName);
		int col = getOrAddColumn(columnName);

		double previousValue = values[row][col];
		values[row][col] = previousValue + value;

		return values[row][col];
	}

	/**
	 * Set the value of the field designated by rowName and columnName. Add a
	 * new field if none exists for that combination.
	 * 
	 * @param rowName
	 *            The name of the field's row
	 * @param columnName
	 *            The name of the field's column
	 * @param value
	 *            The value to set
	 * @return The previous value of the field
	 */
	public double setValue(String rowName, String columnName, double value) {
		int row = getOrAddRow(rowName);
		int col = getOrAddColumn(columnName);

		double previousValue = values[row][col];
		values[row][col] = value;

		return previousValue;
	}

	/**
	 * Return the value of the field designated by rowName and column name.
	 * 
	 * @param rowName
	 *            The rowName of the field
	 * @param columnName
	 *            The column name of the field
	 * @return The value of the field designated by rowName and columnName or
	 *         null if the field does not exist
	 */
	public Double getValue(String rowName, String columnName) {
		Integer row = rowNamesToRows.get(rowName);
		Integer col = colNamesToCols.get(columnName);

		if (row == null || col == null) {
			return null;
		} else {
			return values[row][col];
		}
	}

	/**
	 * Get a value by it's row and column index.
	 * 
	 * @param row
	 *            The row index.
	 * @param col
	 *            The column index.
	 * @return The value specified by the indices or null if none exists.
	 */
	public Double getValue(int row, int col) {
		if (row >= rowAmount || col >= colAmount) {
			return null;
		} else {
			return values[row][col];
		}
	}

	/**
	 * Get a copy of a row by it's name.
	 * 
	 * @param rowName
	 *            The name of the row.
	 * @return A copy of the row fit in size to the amount of columns set.
	 * @throws IllegalArgumentException
	 *             If there is no row with that name.
	 */
	public double[] getRow(String rowName) throws IllegalArgumentException {
		Integer row = rowNamesToRows.get(rowName);
		if (row == null) {
			throw new IllegalArgumentException("No row for name: " + rowName);
		} else {
			return getRow(row);
		}
	}

	/**
	 * Get a copy of a row by it's index.
	 * 
	 * @param row
	 *            The index of the row.
	 * @return A copy of the row fit in size to the amount of columns set.
	 * @throws IllegalArgumentException
	 *             if there is no row with that index.
	 */
	public double[] getRow(int row) throws IllegalArgumentException {
		if (row >= rowAmount) {
			throw new IllegalArgumentException("Row not set: " + row);
		}
		return Arrays.copyOf(values[row], colAmount);
	}

	/**
	 * Gets a column by it's name.
	 * 
	 * @param columnName
	 *            The name of the column.
	 * @return a new array with all values in the specified column.
	 * @throws IllegalArgumentException
	 *             if no column with the specified name exists.
	 */
	public double[] getColumn(String columnName) throws IllegalArgumentException {
		// get col number and fail if none exists
		Integer col = colNamesToCols.get(columnName);
		if (col == null) {
			throw new IllegalArgumentException("No column for name: " + columnName);
		}
		return getColumn(col);
	}

	/**
	 * Gets a column by it's index.
	 * 
	 * @param col
	 *            The index of the column.
	 * @return a new array with all values in the specified column.
	 * @throws IllegalArgumentException
	 *             if the column is not set
	 */
	public double[] getColumn(int col) throws IllegalArgumentException {
		if (col >= colAmount) {
			throw new IllegalArgumentException("Col not set: " + col);
		}
		// simply copy the column
		double[] result = new double[rowAmount];
		for (int i = 0; i < rowAmount; i++) {
			result[i] = values[i][col];
		}
		return result;
	}

	/**
	 * Gets the number of rows currently set.
	 * 
	 * @return number of rows
	 */
	public int getRowAmount() {
		return rowAmount;
	}

	/**
	 * Gets the number of columns currently set.
	 * 
	 * @return number of columns
	 */
	public int getColumnsAmount() {
		return colAmount;
	}

	/**
	 * Get the output delimiter currently set.
	 */
	public String getDelimiter() {
		return delimiter;
	}

	/**
	 * Set the output delimiter.
	 * 
	 * @param delimiter
	 *            The delimiter to set.
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Return the number of the row for the provided name if present.
	 * 
	 * @param rowName
	 *            the name of the row
	 * @return the row number or null if the rowName is not part of the table.
	 */
	public int getRowNo(String rowName) {
		return rowNamesToRows.get(rowName);
	}

	/**
	 * Return the number of the column for the provided name if present.
	 * 
	 * @param columnName
	 *            the name of the column
	 * @return the column number or null if the columnName is not part of the
	 *         table.
	 */
	public int getColumnNo(String columnName) {
		return colNamesToCols.get(columnName);
	}

	/**
	 * Return the name of the column if present.
	 * 
	 * @param colNo
	 *            The index of the column.
	 * @return The name of the column or null if the columnNo is not part of the
	 *         table.
	 */
	public String getColumnName(int colNo) {
		return colsToColNames.get(colNo);
	}

	/**
	 * Return the name of the row if present.
	 * 
	 * @param rowNo
	 *            The index of the row.
	 * @return The name of the row or null if the rowNo is not part of the
	 *         table.
	 */
	public String getRowName(int rowNo) {
		return rowsToRowNames.get(rowNo);
	}

	/**
	 * Format the table's header as csv.
	 * 
	 * @return The comma separated values in the first header row.
	 */
	public String csvHeader() {
		StringBuilder sb = new StringBuilder();
		// the first header field is empty
		sb.append(delimiter);
		for (int col = 0; col < colAmount; col++) {
			sb.append(colsToColNames.get(col));
			sb.append(delimiter);
		}
		sb.setLength(sb.length() - 1);
		sb.append('\n');
		return sb.toString();
	}

	/**
	 * Returns the row as a csv representation.
	 * 
	 * @param row
	 *            The row to format as csv.
	 * @return A csv representation of the row
	 * @throws IllegalArgumentException
	 *             If row doesn't mach a row in the table.
	 */
	public String csvLine(int row) throws IllegalArgumentException {
		if (row >= rowAmount) {
			throw new IllegalArgumentException("No row for index: " + row);
		}
		StringBuilder sb = new StringBuilder();
		// write the row header
		sb.append(rowsToRowNames.get(row));
		sb.append(delimiter);
		// write values
		for (int col = 0; col < colAmount; col++) {
			// Write only non-zero values
			if (values[row][col] != 0) {
				sb.append(values[row][col]);
			}
			sb.append(delimiter);
		}
		sb.setLength(sb.length() - 1);
		sb.append('\n');
		return sb.toString();
	}

	/**
	 * get the current row if it exists or add a new one: i.e. note it's name in
	 * the mappings and make sure that it is backed by an appropriately sized
	 * array in values[newRowNo]
	 * 
	 * @param rowName
	 *            Name of row
	 * @return row index
	 */
	private int getOrAddRow(String rowName) {
		Integer row = rowNamesToRows.get(rowName);
		// If the row is new, add it
		if (row == null) {
			if (rowAmount >= rowMax - 1) {
				yResize();
			}
			row = rowAmount;
			values[row] = new double[colMax];
			rowNamesToRows.put(rowName, row);
			rowsToRowNames.put(row, rowName);
			rowAmount += 1;
		}
		return row;
	}

	/**
	 * Just add the column and make sure that it is accessible via the column
	 * name, resize columns if necessary
	 * 
	 * @param columnName
	 *            Name of column
	 * @return column index
	 */
	private int getOrAddColumn(String columnName) {
		Integer col = colNamesToCols.get(columnName);
		// If the column is new, add it
		if (col == null) {
			if (colAmount >= colMax) {
				xResize();
			}
			col = colAmount;
			colNamesToCols.put(columnName, col);
			colsToColNames.put(col, columnName);
			colAmount += 1;
		}
		return col;
	}

	/**
	 * Increase the amount of allocated rows
	 */
	private void yResize() {
		if (rowMax < 100000) {
			rowMax *= 2;
		} else {
			rowMax += 50000;
		}
		rowMax *= 2;
		values = Arrays.copyOf(values, rowMax);
	}

	/**
	 * Increase the amount of allocated columns
	 */
	private void xResize() {
		if (colMax < 100000) {
			colMax *= 2;
		} else {
			colMax += 50000;
		}
		for (int i = 0; i < rowAmount; i++) {
			double[] row = values[i];
			if (row != null) {
				values[i] = Arrays.copyOf(row, colMax);
			}
		}
	}

	/**
	 * Reads CSV data from specified string and returns a NamedFieldMatrix
	 * object instance.
	 * 
	 * @param csvString
	 *            String containing CSV formatted data
	 * @return NamedFieldMatrix instance
	 * @throws Exception
	 *             Thrown if the CSV input cannot be parsed
	 */
	public static NamedFieldMatrix parseCSV(String csvString) throws Exception {
		return NamedFieldMatrix.parseCSV(new StringReader(csvString));
	}

	/**
	 * Reads CSV data from specified reader and returns a NamedFieldMatrix
	 * object instance.
	 * 
	 * @param csvReader
	 *            Reader instance providing CSV formatted data
	 * @return NamedFieldMatrix instance
	 * @throws Exception
	 *             Thrown if the CSV input cannot be parsed
	 */
	public static NamedFieldMatrix parseCSV(Reader csvReader) throws Exception {

		// Instantiate matrix
		NamedFieldMatrix matrix = new NamedFieldMatrix();

		// Use scanner for input
		Scanner input = new Scanner(csvReader);
		input.useDelimiter("\\n");

		// Read csv head row
		String[] colNames = null;
		if (input.hasNext()) {
			colNames = input.next().split(",");
		} else {
			input.close();
			throw new IOException("Cannot parse CSV data -- no head row found.");
		}

		// Read data rows
		while (input.hasNext()) {
			String[] data = input.next().split(",");
			// Store data into matrix (assuming the first column contains the
			// dataset names)
			for (int i = 1; i < data.length && i < colNames.length; i++) {
				Double value = 0d; // Default value for empty string input
				if (data[i] != null && !data[i].isEmpty())
					value = Double.parseDouble(data[i]);
				matrix.addValue(data[0], colNames[i], value);
			}
		}

		input.close();
		return matrix;
	}

}
