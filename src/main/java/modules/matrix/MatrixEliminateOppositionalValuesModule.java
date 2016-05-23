package modules.matrix;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class MatrixEliminateOppositionalValuesModule extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_DELIMITER_INPUT_REGEX = "input delimiter regex";
	public static final String PROPERTYKEY_DELIMITER_OUTPUT = "output delimiter";
	public static final String PROPERTYKEY_INPUTHASHEADERLINE = "input has header line";
	
	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT_MATRIX = "matrix";
	private static final String ID_INPUT_SUMS = "column sums";
	private static final String ID_OUTPUT = "output";
	
	// Local variables
	private String inputdelimiter;
	private String outputdelimiter;
	private boolean inputHasHeaderLine;

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
		this.getPropertyDescriptions().put(PROPERTYKEY_INPUTHASHEADERLINE, "First line of input is header line [true/false].");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Eliminate Opposing Matrix Values"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT_REGEX, "[\\,;]");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INPUTHASHEADERLINE, "false");
		
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
		
		// Construct scanner instances for input segmentation
		Scanner lineScanner = new Scanner(this.getInputPorts().get(ID_INPUT_MATRIX).getInputReader());
		lineScanner.useDelimiter("\\R+");
		
		// Array for header names
		String[] headerNames = null;
		
		// Array for result values
		double[] sums = null;
		
		if (this.inputHasHeaderLine)
			// Read header line
			if (lineScanner.hasNext()){
				headerNames = lineScanner.next().split(this.inputdelimiter);
				sums = new double[headerNames.length-1];
			} else {
				lineScanner.close();
				this.closeAllOutputs();
				throw new Exception("No input.");
			}
		
		// Input read loop
		while (lineScanner.hasNext()){
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				lineScanner.close();
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// Determine next line
			String line = lineScanner.next();
			List<String> fieldList = new ArrayList<String>();
			Scanner fieldScanner = new Scanner(new StringReader(line));
			fieldScanner.useDelimiter(this.inputdelimiter);
			while(fieldScanner.hasNext()){
				fieldList.add(fieldScanner.next());
			}
			fieldScanner.close();
			
			if (sums == null)
				sums = new double[fieldList.size()-1];
						
			// Check length
			else if (sums.length != fieldList.size()-1){
				lineScanner.close();
				this.closeAllOutputs();
				throw new Exception("Number of data fields is inconsistent ("+sums.length+" != "+(fieldList.size()-1)+").");
			}
			
			// Add data values
			for (int i=1; i<fieldList.size(); i++){
				if (fieldList.get(i) != null && !fieldList.get(i).isEmpty())
					try {
						sums[i-1] = sums[i-1]+Double.parseDouble(fieldList.get(i));
					} catch (NumberFormatException e) {
						String error = "This value does not seem to be a number.";
						if (!this.inputHasHeaderLine)
							error = error.concat(" If this matrix comes with a header line, please set '"+PROPERTYKEY_INPUTHASHEADERLINE+"' to 'true' in the module options.");
						throw new Exception(error,e);
					}
			}
		}

		lineScanner.close();
		
		// Output values
		for (int i=0; i<sums.length; i++){
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(headerNames[i+1]+this.outputdelimiter+sums[i]+this.outputdelimiter+"\n");
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
		this.inputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_INPUT_REGEX, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT_REGEX));
		this.outputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT));
		
		String value = this.getProperties().getProperty(PROPERTYKEY_INPUTHASHEADERLINE, this.getPropertyDefaultValues().get(PROPERTYKEY_INPUTHASHEADERLINE));
		if (value != null && !value.isEmpty())
			this.inputHasHeaderLine = Boolean.parseBoolean(value);
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
