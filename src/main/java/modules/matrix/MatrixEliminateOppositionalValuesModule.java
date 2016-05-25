package modules.matrix;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class MatrixEliminateOppositionalValuesModule extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_DELIMITER_INPUT_REGEX = "input delimiter regex";
	public static final String PROPERTYKEY_DELIMITER_OUTPUT = "output delimiter";
	
	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT_MATRIX = "matrix";
	private static final String ID_INPUT_SUMS = "column sums";
	private static final String ID_OUTPUT = "output";
	
	// Local variables
	private String inputdelimiter;
	private String outputdelimiter;
	private String emptyFieldValue = "";

	public MatrixEliminateOppositionalValuesModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Eliminates matrix values that stand in opposition based on the specified column sums (columns with larger sums are given precedence over those with smaller ones).");
		
		// Add module category
		this.setCategory("Matrix");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_INPUT_REGEX, "Regular expression to use as segmentation delimiter for CSV input.");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT, "String to insert as segmentation delimiter into the output.");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Eliminate Opposing Matrix Values"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT_REGEX, "[\\,;]");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT, ";");
		
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

		// Set for sum tupels: Highest sum value will be the first element
		Set<TypeSumTupel> sumTupelSet = new TreeSet<TypeSumTupel>();
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
			sumTupelSet.add(new TypeSumTupel(line[0], Double.parseDouble(line[1])));
		}
		lineScanner.close();

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
		for (int i=0; i<headerNames.length; i++)
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(headerNames[i]+this.outputdelimiter);
		
		// Output line break
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");
		

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

			// First field is label
			String label;
			try {
				label = fieldScanner.next();
			} catch (Exception e) {
				fieldScanner.close();
				Logger.getLogger("").log(Level.WARNING,
						"Module '" + this.getName() + "' encountered an empty line.");
				continue;
			}
			
			// Add fields to field list
			while (fieldScanner.hasNext()) {
				// Read next field
				String field = fieldScanner.next();
				dataFields.add(field);
			}
			fieldScanner.close();

			// Loop over sum tupel list and eliminate opposing values
			Iterator<TypeSumTupel> sumTupels = sumTupelSet.iterator();
			while (sumTupels.hasNext()) {
				TypeSumTupel sumTupel = sumTupels.next();

				// List for element indices to remove
				Set<Integer> indicesToRemove = new TreeSet<Integer>();
				
				// Variable to mark if the current line has a positive value in the column that matches the current tupel's name.
				boolean lineIsRelevantToTupelName = false;

				// Loop over data fields
				for (int i=0; i<dataFields.size(); i++) {

					// Read next field
					String field = dataFields.get(i);
					
					// Skip empty field
					if (field == null || field.isEmpty())
						continue;
					
					// Parse value
					Double value = Double.parseDouble(field);
					
					// Skip zero values
					if (value == 0)
						continue;
					
					// Check whether data field matches the current tupel's name.
					if (headerNames[i+1].equals(sumTupel.getType()))
						lineIsRelevantToTupelName = true;
					
					// Check if rowlabel+columnlabel stands in opposition with the current tupel's name
					else if ((headerNames[i+1]+label).endsWith(sumTupel.getType()))
						indicesToRemove.add(i);

				}
				fieldScanner.close();
				
				// Remove opposing data fields
				if (lineIsRelevantToTupelName)
					for (int i = 0; i < dataFields.size(); i++) {
						if (indicesToRemove.contains(i))
							dataFields.set(i, this.emptyFieldValue);
					}
				
			}
			
			// Output row label
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(label+this.outputdelimiter);
			
			// Output data fields
			for (int i=0; i<dataFields.size(); i++){
				this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(dataFields.get(i)+this.outputdelimiter);
			}
			
			// Output line break
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");

		}

		lineScanner.close();

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
		this.inputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_INPUT_REGEX, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT_REGEX));
		this.outputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT));
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
