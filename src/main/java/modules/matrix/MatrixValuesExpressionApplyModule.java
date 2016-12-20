package modules.matrix;

import java.io.Reader;
import java.util.Properties;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import base.workbench.ModuleRunner;
import common.parallelization.CallbackReceiver;
import models.NamedFieldMatrix;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class MatrixValuesExpressionApplyModule extends ModuleImpl {

	private static final String MODULE_DESC = "Evaluates a JavaScript expression on every cell of the input matrix and outputs a new matrix containing the altered values.";

	// keys for I/O ports
	private static final String INPUT_MATRIX_ID = "Matrix input";
	private static final String OUTPUT_MATRIX_ID = "Matrix output";

	// keys for the module properties: An expression to evaluate and a csv delimiter to use
	private static final String PROPERTYKEY_EXPRESSION = "expression";
	private static final String PROPERTYKEY_CSV_DELIM = "csv delimiter";

	// An Engine to evaluate JavaScript Expressions
	private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("js");

	// A variable name that can be used to refer to the cell value inside an
	// expression
	private static final String CELL_VALUE_VARIABLE = "VAL";

	// A String for the expression that shall be evaluated
	private String expression;
	
	// the csv delimiter used for input/output
	private String csvDelimiter;

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(MatrixValuesExpressionApplyModule.class, args);
	}

	public MatrixValuesExpressionApplyModule(CallbackReceiver callbackReceiver, Properties properties)
			throws Exception {
		// call parent constructor
		super(callbackReceiver, properties);

		// define module description and name
		this.setDescription(MODULE_DESC);
		this.setName("MatrixValuesExpressionApplyModule");
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, this.getName());

		// define the property taking in the expression to evaluate
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_EXPRESSION, "An expression to evaluate on each matrix cell. Use '"
						+ CELL_VALUE_VARIABLE
						+ "' to refer to the cell value. Expression has to result in a result of class Double.");
		this.getPropertyDefaultValues().put(PROPERTYKEY_EXPRESSION, "(VAL != 0 ? 1.0 : 0.0)");
		
		// define the property used for the csv delimiter
		this.getPropertyDescriptions().put(PROPERTYKEY_CSV_DELIM, "The csv delimiter used in input and output.");
		this.getPropertyDefaultValues().put(PROPERTYKEY_CSV_DELIM, ";");

		// define I/O
		InputPort in = new InputPort(INPUT_MATRIX_ID, "[text/csv] A NamedFieldMatrix to evaluate.", this);
		OutputPort out = new OutputPort(OUTPUT_MATRIX_ID, "[text/csv] the evaluated NamedFieldMatrix", this);
		in.addSupportedPipe(CharPipe.class);
		out.addSupportedPipe(CharPipe.class);
		super.addInputPort(in);
		super.addOutputPort(out);
	}

	@Override
	public boolean process() throws Exception {
		boolean result = true;
		
		try {
			// parse input
			Reader inputReader = this.getInputPorts().get(INPUT_MATRIX_ID).getInputReader();
			NamedFieldMatrix matrix = NamedFieldMatrix.parseCSV(inputReader, csvDelimiter);
			
			// prepare output and write header as that will not change
			OutputPort out = this.getOutputPorts().get(OUTPUT_MATRIX_ID);
			matrix.setDelimiter(csvDelimiter);
			out.outputToAllCharPipes(matrix.csvHeader());
			
			// traverse matrix, apply expression and output the line in question
			Double value;
			for (int i = 0; i < matrix.getRowAmount(); i++) {
				for (int j = 0; j < matrix.getColumnsAmount(); j++) {
					value = matrix.getValue(i, j);
					value = evaluateExpression(value, expression);
					matrix.setValue(i, j, value);
				}
				out.outputToAllCharPipes(matrix.csvLine(i));
			}
		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			this.closeAllOutputs();
		}
		
		return result;
	}

	private static Double evaluateExpression(Double cellValue, String expression) {
		// replace variable with value if present
		String fullExpression = expression.replaceAll(CELL_VALUE_VARIABLE, cellValue.toString());

		// generate result of the expression
		Object result;
		try {
			result = SCRIPT_ENGINE.eval(fullExpression);
		} catch (ScriptException e) {
			throw new IllegalArgumentException("Expression '" + expression + "' cannot be evaluated ("
					+ CELL_VALUE_VARIABLE + ": " + cellValue + ")");
		}

		// return the result if it is really a Double
		if (result == null || result.getClass() != Double.class) {
			throw new IllegalArgumentException("Expression '" + expression + "' cannot be evaluated ("
					+ CELL_VALUE_VARIABLE + ": " + cellValue + ")");
		} else {
			return (Double) result;
		}
	}
	
	@Override
	public void applyProperties() throws Exception {
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();
		
		// Apply own properties
		this.csvDelimiter = this.getProperties().getProperty(PROPERTYKEY_CSV_DELIM, this.getPropertyDefaultValues().get(PROPERTYKEY_CSV_DELIM));
		this.expression = this.getProperties().getProperty(PROPERTYKEY_EXPRESSION, this.getPropertyDefaultValues().get(PROPERTYKEY_EXPRESSION));
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
