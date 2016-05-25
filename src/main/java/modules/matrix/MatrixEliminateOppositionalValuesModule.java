package modules.matrix;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class MatrixEliminateOppositionalValuesModule extends ModuleImpl {

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_DELIMITER_INPUT_REGEX = "input delimiter regex";
	public static final String PROPERTYKEY_DELIMITER_OUTPUT = "output delimiter";
	public static final String PROPERTYKEY_ZEROVALUE = "zero value";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT_MATRIX = "matrix";
	private static final String ID_INPUT_SUMS = "column sums";
	private static final String ID_OUTPUT = "output";

	// Local variables
	private String inputdelimiter;
	private String outputdelimiter;
	private String emptyFieldValue;

	public MatrixEliminateOppositionalValuesModule(CallbackReceiver callbackReceiver, Properties properties)
			throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription(
				"Eliminates matrix values that stand in opposition based on the specified column sums (columns with larger sums are given precedence over those with smaller ones).");

		// Add module category
		this.setCategory("Matrix");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_INPUT_REGEX,
				"Regular expression to use as segmentation delimiter for CSV input.");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT,
				"String to insert as segmentation delimiter into the output.");
		this.getPropertyDescriptions().put(PROPERTYKEY_ZEROVALUE,
				"String to insert as zero value into the output, replacing an eliminated row/column-value.");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Eliminate Opposing Matrix Values");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT_REGEX, "[\\,;]");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ZEROVALUE, "");

		// Define I/O
		InputPort inputPortMatrix = new InputPort(ID_INPUT_MATRIX, "CSV matrix input.", this);
		inputPortMatrix.addSupportedPipe(CharPipe.class);
		InputPort inputPortSums = new InputPort(ID_INPUT_SUMS, "CSV column sums input.", this);
		inputPortSums.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "CSV output.", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPortMatrix);
		super.addInputPort(inputPortSums);
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {

		// Read matrix sums first

		// List for sum tupels
		List<ColumnSumTupel> columnSumTupelList = new ArrayList<ColumnSumTupel>();
		// Construct scanner instances for sum input segmentation
		Scanner lineScanner = new Scanner(this.getInputPorts().get(ID_INPUT_SUMS).getInputReader());
		lineScanner.useDelimiter("\\R+");
		// Loop over sum input
		while (lineScanner.hasNext()) {
			String[] line = lineScanner.next().split(this.inputdelimiter);
			if (line.length != 2) {
				lineScanner.close();
				throw new Exception("Length of line not as expected: " + line.length);
			}
			columnSumTupelList.add(new ColumnSumTupel(line[0], Double.parseDouble(line[1])));
		}
		lineScanner.close();
		
		// Sort sum list; Highest sum value will be the first element
		columnSumTupelList.sort(new Comparator<ColumnSumTupel>(){
			@Override
			public int compare(ColumnSumTupel o1, ColumnSumTupel o2) {
				return o1.compareTo(o2);
			}});

		// Construct scanner instance for matrix input segmentation
		lineScanner = new Scanner(this.getInputPorts().get(ID_INPUT_MATRIX).getInputReader());
		lineScanner.useDelimiter("\\R+");

		// Array for header names
		String[] headerNames = null;

		// Read header line
		if (lineScanner.hasNext()) {
			headerNames = lineScanner.next().split(this.inputdelimiter);
		} else {
			lineScanner.close();
			this.closeAllOutputs();
			throw new Exception("No input.");
		}

		// Output header line
		for (int i = 0; i < headerNames.length; i++)
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(headerNames[i] + this.outputdelimiter);

		// Output line break
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");

		// Unfortunately we will have to store the data lines upfront because it
		// is necessary to loop over them multiple times.
		List<List<String>> dataLines = new ArrayList<List<String>>();

		// Data lines input read loop
		while (lineScanner.hasNext()) {

			// Check for interrupt signal
			if (Thread.interrupted()) {
				lineScanner.close();
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}

			// Determine next line
			String line = lineScanner.next();

			// List for data field values
			List<String> dataFields = new ArrayList<String>();

			// Scanner to read the data line fields (String.split() is not
			// advisable here because it omits empty fields)
			Scanner fieldScanner = new Scanner(new StringReader(line));
			fieldScanner.useDelimiter(this.inputdelimiter);

			// Add fields to field list
			while (fieldScanner.hasNext()) {
				// Read next field
				String field = fieldScanner.next();
				dataFields.add(field);
			}
			fieldScanner.close();

			dataLines.add(dataFields);
		}
		lineScanner.close();

		// Loop over sum tupel list
		Iterator<ColumnSumTupel> sumTupels = columnSumTupelList.iterator();
		while (sumTupels.hasNext()) {
			ColumnSumTupel sumTupel = sumTupels.next();

			// Determine column index in input matrix
			int index = -1;
			for (int i = 1; i < headerNames.length; i++) {
				if (sumTupel.getColumnName().equals(headerNames[i])) {
					index = i;
					break;
				}
			}
			if (index < 0) {
				throw new Exception(
						"I cannot find the column for '" + sumTupel.getColumnName() + "' in the matrix header line.");
			}

			// List of Strings to match against opposition
			Set<String> matchStrings = new HashSet<String>();

			// Loop over data lines
			for (int i = 0; i < dataLines.size(); i++) {

				// Retrieve data fields
				List<String> dataFields = dataLines.get(i);

				// Check whether the line includes a positive value for the
				// current column
				String value = dataFields.get(index);
				if (value != null && !value.isEmpty() && Double.parseDouble(value) > 0) {
					String label = dataFields.get(0);
					matchStrings.add(label + sumTupel.getColumnName());
				}

			}

			// Now that we have a complete set of Strings to match against for
			// the current column, we can again loop over the data lines, this
			// time deleting all opposing strings
			for (int i = 0; i < dataLines.size(); i++) {

				// Retrieve data fields
				List<String> dataFields = dataLines.get(i);

				// Determine row label
				String label = dataFields.get(0);

				// Check whether any label<->column combination stand in
				// opposition to any of the strings we constructed in the last
				// step
				for (int j = 1; j < dataFields.size(); j++) {
					String value = dataFields.get(j);
					if (value != null && !value.isEmpty() && Double.parseDouble(value) > 0) {

						// If we get a match, we set the field value to zero
						if (j!=index && matchStrings.contains(label + headerNames[j])) {
							dataFields.set(j, this.emptyFieldValue);
						}
					}
				}

			}

		}

		// All work is done, so we can output the result

		for (int i = 0; i < dataLines.size(); i++) {

			// Retrieve data fields
			List<String> dataFields = dataLines.get(i);

			// Output data fields
			for (int j = 0; j < dataFields.size(); j++) {
				this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(dataFields.get(j) + this.outputdelimiter);
			}

			// Output line break
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");

		}

		// Close outputs (important!)
		this.closeAllOutputs();

		// Done
		return true;
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		this.inputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_INPUT_REGEX,
				this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT_REGEX));
		this.outputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT,
				this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT));
		this.emptyFieldValue = this.getProperties().getProperty(PROPERTYKEY_ZEROVALUE,
						this.getPropertyDefaultValues().get(PROPERTYKEY_ZEROVALUE));

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
