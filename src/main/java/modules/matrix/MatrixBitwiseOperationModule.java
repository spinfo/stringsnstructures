package modules.matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import common.parallelization.CallbackReceiver;
import models.NamedFieldMatrix;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

/**
 * Module interprets either rows or columns of an input matrix as binary bitsets
 * and performs symmetrical operations (AND, OR, XOR) on these bitsets. Output
 * is again a matrix showing how many bits are set for each combination of
 * rows/columns after the operation is performed.
 */
import base.workbench.ModuleRunner;

public class MatrixBitwiseOperationModule extends ModuleImpl {

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(MatrixBitwiseOperationModule.class, args);
	}


	private static final String MODULE_DESC = "Module interprets either rows or columns of an input matrix as binary bitsets"
			+ " and performs symmetrical operations (AND, OR, XOR) on these bitsets. Output"
			+ " is again a matrix showing how many bits are set for each combination of"
			+ " rows/columns after the operation is performed.";

	private final static String INPUT_ID = "Input Matrix";
	private final static String INPUT_DESC = "[text/csv] An input csv table. Header row and first column are expected to contain Strings as labels. All other fields are assumed to be blank or contain numerical values.";

	private final static String OUTPUT_MATRIX_ID = "Output Matrix";
	private final static String OUTPUT_MATRIX_DESC = "[text/csv] A symmetrical csv table mapping row/column headings to each other and containing the amounts of bits sets after the the operation specified was applied.";

	private final static String OUTPUT_LIST_ID = "List Output";
	private final static String OUTPUT_LIST_DESC = "[text/plain] A list of row/column mappings with the amount of bits set after the operation was applied, sorted by that count.";

	// An enum, and property to specify the operation to apply
	// Note: For these operation the order of operation is unimportant, adding
	// an asymmetrical operation would require some changes in the processing
	private static enum Operation {
		AND, OR, XOR
	};

	private static final String PROPERTYKEY_OPERATION = "operation";

	// A boolean deciding whether to operate on columns or rows of a table and a
	// property for the user to choose that
	private static final String PROPERTYKEY_USE_ROWS = "Operate on rows";

	// Properties for the input and output separator
	private static final String PROPERTYKEY_INPUT_SEPARATOR = "Input separator";
	private static final String PROPERTYKEY_OUTPUT_SEPARATOR = "Output separator";

	// Properties for controlling which rows/columns are compared
	private static final String PROPERTYKEY_OPERATE_REFLEXIVE = "Reflexive";

	public MatrixBitwiseOperationModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// set module name and description
		this.setName("Matrix Bitwise Operation Module");
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, this.getName());
		this.setDescription(MODULE_DESC);


		// setup i/o
		InputPort input = new InputPort(INPUT_ID, INPUT_DESC, this);
		input.addSupportedPipe(CharPipe.class);
		super.addInputPort(input);

		OutputPort matrixOutput = new OutputPort(OUTPUT_MATRIX_ID, OUTPUT_MATRIX_DESC, this);
		matrixOutput.addSupportedPipe(CharPipe.class);
		super.addOutputPort(matrixOutput);

		OutputPort listOutput = new OutputPort(OUTPUT_LIST_ID, OUTPUT_LIST_DESC, this);
		listOutput.addSupportedPipe(CharPipe.class);
		super.addOutputPort(listOutput);

		// setup properties
		this.getPropertyDescriptions().put(PROPERTYKEY_OPERATION, "Which operation to perform, one of: AND, OR, XOR.");
		this.getPropertyDescriptions().put(PROPERTYKEY_USE_ROWS,
				"Whether to operate on rows (true) or columns (false).");
		this.getPropertyDescriptions().put(PROPERTYKEY_INPUT_SEPARATOR,
				"Which input separator to use for the input csv table.");
		this.getPropertyDescriptions().put(PROPERTYKEY_OUTPUT_SEPARATOR,
				"Which Output separator to use for the csv table output.");
		this.getPropertyDescriptions().put(PROPERTYKEY_OPERATE_REFLEXIVE,
				"Whether the operation should be applied to a row/col with itself.");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OPERATION, "AND");
		this.getPropertyDefaultValues().put(PROPERTYKEY_USE_ROWS, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INPUT_SEPARATOR, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTPUT_SEPARATOR, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OPERATE_REFLEXIVE, "false");

		this.setDefaultsIfMissing();
	}

	public boolean process() throws Exception {
		boolean result = true;

		// a reader to read input line by line
		BufferedReader inputReader = new BufferedReader(getInputPorts().get(INPUT_ID).getInputReader());

		try {
			// determine all necessary flags from properties
			final boolean useRows = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_USE_ROWS));
			final boolean reflexive = Boolean
					.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_OPERATE_REFLEXIVE));
			final Operation operation = Operation.valueOf((this.getProperties().getProperty(PROPERTYKEY_OPERATION)));
			final String inputSeparator = this.getProperties().getProperty(PROPERTYKEY_INPUT_SEPARATOR);
			final String outputSeparator = this.getProperties().getProperty(PROPERTYKEY_OUTPUT_SEPARATOR);

			// read the input
			final Map<String, BitSet> bitsets;
			if (useRows) {
				bitsets = readInputRows(inputReader, inputSeparator);
			} else {
				bitsets = readInputCols(inputReader, inputSeparator);
			}
			inputReader.close();

			// build a matrix containing the result of applying the operation to
			// each pair of BitSets
			NamedFieldMatrix outMatrix = new NamedFieldMatrix();
			BitSet operand1 = null;
			BitSet operand2 = null;
			BitSet product = null;
			Double value = null;
			for (String name1 : bitsets.keySet()) {
				operand1 = bitsets.get(name1);
				for (String name2 : bitsets.keySet()) {
					// If this combination was already calculated in a
					// previous iteration, just copy the value
					// this works as long as all possible operations are
					// symmetrical
					value = outMatrix.getValue(name2, name1);
					if (value != null) {
						outMatrix.setValue(name1, name2, value);
						continue;
					}
					// don't compare a BitSet to itself unless instructed
					if (!reflexive && name1.equals(name2)) {
						// set to zero to get a symmetrical matrix
						outMatrix.setValue(name1, name2, 0.0);
						continue;
					}

					operand2 = bitsets.get(name2);
					product = performOperation(operand1, operand2, operation);
					outMatrix.setValue(name1, name2, (double) countBitsSet(product));
				}
			}

			// actually write the output
			OutputPort matrixOut = this.getOutputPorts().get(OUTPUT_MATRIX_ID);
			if (matrixOut.isConnected()) {
				writeMatrixOutput(outMatrix, matrixOut, outputSeparator);
			}

			OutputPort listOut = this.getOutputPorts().get(OUTPUT_LIST_ID);
			if (listOut.isConnected()) {
				writeListOutput(outMatrix, listOut);
			}

		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			if (inputReader != null) {
				inputReader.close();
			}
			this.closeAllOutputs();
		}

		return result;
	}

	// convert each row of the input table to a BitSet mapped to it's row
	// heading
	private static Map<String, BitSet> readInputRows(BufferedReader reader, String inputSeparator) throws Exception {
		Map<String, BitSet> result = new TreeMap<String, BitSet>();

		// first parse the header row and discard it, only saving the amount of
		// splits noticed to check for consistency
		final int colAmount = reader.readLine().split(inputSeparator, -1).length;

		// keep count of the row currently read for error messages
		int i = 1;

		// each line is a row of fields which will be converted to a BitSet
		String line = "";
		String[] fields = null;
		BitSet bitset = null;
		String rowName = null;
		while ((line = reader.readLine()) != null) {
			fields = line.split(inputSeparator, -1);
			if (fields.length != colAmount) {
				throw new Exception("Bad input at line " + i + ": amount of fields is " + fields.length + " but "
						+ colAmount + " columns were read." + " ~ " + line);
			}
			bitset = new BitSet(colAmount);
			rowName = fields[0];
			// iterate and convert fields
			for (int j = 1; j < fields.length; j++) {
				if (parseField(fields[j])) {
					bitset.set(j - 1);
				}
			}
			// commit the result
			result.put(rowName, bitset);
		}

		return result;
	}

	// convert each column of the input table to a BitSet mapped to it's column
	// heading
	private static Map<String, BitSet> readInputCols(BufferedReader reader, String inputSeparator) throws Exception {
		Map<String, BitSet> result = new TreeMap<String, BitSet>();

		// get a list of column names from the table header
		final String[] colNames = reader.readLine().split(inputSeparator, -1);

		String line = null;
		String[] fields = null;
		String colName = null;
		BitSet bitset;
		int i = 1;
		while ((line = reader.readLine()) != null) {
			fields = line.split(inputSeparator, -1);
			if (fields.length != colNames.length) {
				throw new Exception("Bad input at line " + i + ": amount of fields is " + fields.length + " but "
						+ colNames.length + " columns were read." + " ~ " + line);
			}
			// row name not needed, so j = 1
			for (int j = 1; j < fields.length; j++) {
				colName = colNames[j];
				bitset = result.getOrDefault(colName, new BitSet());
				if (parseField(fields[j])) {
					bitset.set(i - 1);
				}
				// commit result
				result.put(colName, bitset);
			}
			i++;
		}

		return result;
	}

	// parse a numerical input field, return true if it contains a numerical
	// value > 0, and false if it is empty
	private static boolean parseField(String field) {
		if (!StringUtils.isBlank(field) && (Double.parseDouble(field) > 0.0)) {
			return true;
		}
		return false;
	}

	// return the amount of bits, that are set in the given BitSet
	private static int countBitsSet(BitSet bitset) {
		int result = 0;
		for (int i = 0; i < bitset.length(); i++) {
			if (bitset.get(i)) {
				result += 1;
			}
		}
		return result;
	}

	// performs the operation on both BitSets and returns a new BitSet
	// containing the result
	private static BitSet performOperation(BitSet op1, BitSet op2, Operation op) {
		// we need a clone for the operation because it is destructive
		BitSet result = (BitSet) op1.clone();

		switch (op) {
		case AND:
			result.and(op2);
			break;
		case OR:
			result.or(op2);
			break;
		case XOR:
			result.xor(op2);
			break;
		default:
			throw new IllegalStateException("Unknown bitwise operation: " + op);
		}

		return result;
	}

	private static void writeMatrixOutput(NamedFieldMatrix matrix, OutputPort out, String separator)
			throws IOException {
		matrix.setDelimiter(separator);
		out.outputToAllCharPipes(matrix.csvHeader());
		for (int i = 0; i < matrix.getRowAmount(); i++) {
			out.outputToAllCharPipes(matrix.csvLine(i));
		}
	}

	private static void writeListOutput(NamedFieldMatrix matrix, OutputPort out) throws IOException {
		TreeMap<Integer, List<String>> items = new TreeMap<Integer, List<String>>(Collections.reverseOrder());
		StringBuilder sb = new StringBuilder();

		Double value = null;
		String rowName = null;
		List<String> tokens = null;
		for (int i = 0; i < matrix.getRowAmount(); i++) {
			rowName = matrix.getRowName(i);

			for (int j = 0; j < matrix.getColumnsAmount(); j++) {
				value = matrix.getValue(i, j);

				if (value != null && value > 0) {
					tokens = items.getOrDefault(value.intValue(), new LinkedList<String>());

					sb.append(rowName);
					sb.append('-');
					sb.append(matrix.getColumnName(j));

					tokens.add(sb.toString());
					items.put(value.intValue(), tokens);
					sb.setLength(0);
				}
			}
		}

		for (int count : items.keySet()) {
			for (String nameCombination : items.get(count)) {
				out.outputToAllCharPipes(nameCombination + ": " + count);
				out.outputToAllCharPipes("\n");
			}
		}
	}

}
