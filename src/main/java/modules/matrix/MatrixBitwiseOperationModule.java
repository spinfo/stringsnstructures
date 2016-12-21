package modules.matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

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
	// Note: For these operations the order of operands is unimportant, adding
	// an asymmetrical operation would require some changes in the processing
	private static enum Operation {
		AND, OR, XOR
	};

	private static final String PROPERTYKEY_OPERATION = "operation";
	private Operation operation;

	// A boolean deciding whether to operate on columns or rows of a table and a
	// property for the user to choose that
	private static final String PROPERTYKEY_USE_ROWS = "Operate on rows";
	private boolean useRows;

	// Properties for the input and output separator
	private static final String PROPERTYKEY_INPUT_SEPARATOR = "Input separator";
	private static final String PROPERTYKEY_OUTPUT_SEPARATOR = "Output separator";
	private String inputSeparator;
	private String outputSeparator;

	// Properties for controlling which rows/columns are compared
	private static final String PROPERTYKEY_OPERATE_REFLEXIVE = "Reflexive";
	private boolean reflexive;

	// The value zero as a double
	private static final Double ZERO_D = new Double(0.0);

	// Bitsets are initialised lazily and kept in this map
	private Map<String, BitSet> bitsets;

	// The input matrix may be accessed from some private methods
	NamedFieldMatrix inMatrix;

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

		// set the bitsets member which will be used to store bitsets when
		// created
		bitsets = new HashMap<>();

		try {
			// read the input matrix to operate on and determine whether row or
			// column names will be operated on
			inMatrix = NamedFieldMatrix.parseCSV(inputReader, inputSeparator);
			Set<String> names;
			if (useRows) {
				names = inMatrix.getRowNames();
			} else {
				names = inMatrix.getColumnNames();
			}

			// build a matrix containing the result of applying the operation to
			// each pair of BitSets
			NamedFieldMatrix outMatrix = new NamedFieldMatrix();
			BitSet operand1 = null;
			BitSet operand2 = null;
			BitSet product = null;
			Double value = null;
			for (String name1 : names) {

				operand1 = getOrCreateBitSet(name1);

				for (String name2 : names) {
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

					// actually compare the two bitsets and save the amount of
					// bits set in the result to the output matrix
					operand2 = getOrCreateBitSet(name2);
					product = performOperation(operand1, operand2, operation);
					outMatrix.setValue(name1, name2, (double) product.cardinality());
				}
			}
			
			// these data structures might have gotten big and may be
			// harvested directly after processing finished.
			inMatrix = null;
			bitsets = null;

			// write the output
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

	// get the BitSet associated with the given row or column name. If it
	// doesn't exist already create it from the given matrix
	private BitSet getOrCreateBitSet(String name) {
		// try to find the BitSet in those already created
		BitSet result = bitsets.get(name);

		// if it doesn't exist, create it
		if (result == null) {
			double[] values;

			// decide on whether to use rows or columns
			if (useRows) {
				values = inMatrix.getRow(name);
			} else {
				values = inMatrix.getColumn(name);
			}

			// create the BitSet from the matrix' values
			result = new BitSet(values.length);
			for (int i = 0; i < values.length; i++) {
				if (!ZERO_D.equals(values[i])) {
					result.set(i);
				}
			}

			// save the new BitSet
			bitsets.put(name, result);
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

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// A string for testing user inputs on
		String value;

		// set own properties
		value = this.getProperties().getProperty(PROPERTYKEY_USE_ROWS,
				this.getPropertyDefaultValues().get(PROPERTYKEY_USE_ROWS));
		useRows = Boolean.parseBoolean(value);

		value = this.getProperties().getProperty(PROPERTYKEY_OPERATE_REFLEXIVE,
				this.getPropertyDefaultValues().get(PROPERTYKEY_OPERATE_REFLEXIVE));
		reflexive = Boolean.parseBoolean(value);

		value = this.getProperties().getProperty(PROPERTYKEY_OPERATION,
				this.getPropertyDefaultValues().get(PROPERTYKEY_OPERATION));
		if (!(value == null) && !value.isEmpty()) {
			operation = Operation.valueOf(value);
		}

		inputSeparator = this.getProperties().getProperty(PROPERTYKEY_INPUT_SEPARATOR,
				this.getPropertyDefaultValues().get(PROPERTYKEY_INPUT_SEPARATOR));
		outputSeparator = this.getProperties().getProperty(PROPERTYKEY_OUTPUT_SEPARATOR,
				this.getPropertyDefaultValues().get(PROPERTYKEY_OUTPUT_SEPARATOR));

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
