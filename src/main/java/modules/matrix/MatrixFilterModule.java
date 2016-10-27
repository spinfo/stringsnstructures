package modules.matrix;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import base.workbench.ModuleWorkbenchController;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import base.workbench.ModuleRunner;

public class MatrixFilterModule extends ModuleImpl {

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(MatrixFilterModule.class, args);
	}


	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_CSV_FIELD_DELIMITER = "csv field delimiter";
	public static final String PROPERTYKEY_MATCH = "match regex";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "csv input";
	private static final String ID_OUTPUT = "csv output";

	// Local variables
	private String csvFieldDelimiter;
	private String regex;

	public MatrixFilterModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription(
				"Filters the input CSV matrix according to the specified regular expression (nonmatching elements are eliminated).");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_CSV_FIELD_DELIMITER,
				"CSV field delimiter [string].");
		this.getPropertyDescriptions().put(PROPERTYKEY_MATCH, "Regex that describes a positive match.");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Matrix Filter");
		this.getPropertyDefaultValues().put(PROPERTYKEY_CSV_FIELD_DELIMITER, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MATCH, "[^0]+");

		// Define I/O
		InputPort inputPort = new InputPort(ID_INPUT, "CSV input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "CSV output.", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {

		// Prepare regex pattern
		Pattern pattern = Pattern.compile(this.regex);

		// Construct scanner instances for input segmentation
		Scanner lineScanner = new Scanner(this.getInputPorts().get(ID_INPUT).getInputReader());
		lineScanner.useDelimiter(ModuleWorkbenchController.LINEBREAKREGEX);

		// Variable for filtered matrix
		Map<String,Map<String,String>> filteredMatrix = new TreeMap<String,Map<String,String>>();
		
		// Variable for remaining matrix columns after filtering (important if the two matrix axis have different labels)
		Set<String> remainingColumnLabelSet = new TreeSet<String>();
		
		// Header line variable
		String[] columnLabelArray;
		
		// Read header line
		if (lineScanner.hasNext()) {
			columnLabelArray = lineScanner.next().split(this.csvFieldDelimiter);
		} else {
			lineScanner.close();
			this.closeAllOutputs();
			throw new Exception("No input.");
		}

		// Input read loop
		while (lineScanner.hasNext()) {

			// Check for interrupt signal
			if (Thread.interrupted()) {
				lineScanner.close();
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}

			// Determine next line
			String line = lineScanner.next();
			Scanner fieldScanner = new Scanner(new StringReader(line));
			fieldScanner.useDelimiter(this.csvFieldDelimiter);
			String rowLabel = fieldScanner.next();
			int index = 1;
			while (fieldScanner.hasNext()) {
				String field = fieldScanner.next();
				if (field != null && pattern.matcher(field).matches()){
					// Create matrix line if not yet existent
					if (!filteredMatrix.containsKey(rowLabel))
						filteredMatrix.put(rowLabel, new TreeMap<String,String>());
					// Add matching data field to matrix line
					filteredMatrix.get(rowLabel).put(columnLabelArray[index], field);
					remainingColumnLabelSet.add(columnLabelArray[index]);
				}
				index++;
			}
			fieldScanner.close();
		}
		lineScanner.close();
		
		/*
		 *  Output filtered matrix
		 */
		
		// Header line
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.csvFieldDelimiter);
		Iterator<String> remainingColumnLabels = remainingColumnLabelSet.iterator();
		while(remainingColumnLabels.hasNext()){
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(remainingColumnLabels.next()+this.csvFieldDelimiter);
		}
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");
		
		// Data lines
		Iterator<String> matrixRowLabels = filteredMatrix.keySet().iterator();
		while (matrixRowLabels.hasNext()){
			String matrixRowLabel = matrixRowLabels.next();
			// Output row label
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(matrixRowLabel+this.csvFieldDelimiter);
			Map<String, String> matrixRow = filteredMatrix.get(matrixRowLabel);
			// Output one data field for each remaining column label
			remainingColumnLabels = remainingColumnLabelSet.iterator();
			while(remainingColumnLabels.hasNext()){
				String value = matrixRow.get(remainingColumnLabels.next());
				if (value == null)
					value = "";
				this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(value+this.csvFieldDelimiter);
			}
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
		this.csvFieldDelimiter = this.getProperties().getProperty(PROPERTYKEY_CSV_FIELD_DELIMITER,
				this.getPropertyDefaultValues().get(PROPERTYKEY_CSV_FIELD_DELIMITER));
		this.regex = this.getProperties().getProperty(PROPERTYKEY_MATCH,
				this.getPropertyDefaultValues().get(PROPERTYKEY_MATCH));

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
