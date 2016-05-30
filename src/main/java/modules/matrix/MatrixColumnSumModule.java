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

public class MatrixColumnSumModule extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_DELIMITER_INPUT_REGEX = "input delimiter regex";
	public static final String PROPERTYKEY_DELIMITER_OUTPUT = "output delimiter";
	public static final String PROPERTYKEY_INPUTHASHEADERLINE = "input has header line";
	
	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "csv input";
	private static final String ID_OUTPUT = "output";
	
	// Local variables
	private String inputdelimiter;
	private String outputdelimiter;
	private boolean inputHasHeaderLine;

	public MatrixColumnSumModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Calculates the sum of all numerical values for each column of a given matrix. Outputs a linebreak-separated list of name-sum-pairs (in the same order as the columns were).");
		
		// Add module category
		this.setCategory("Matrix");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_INPUT_REGEX, "Regular expression to use as segmentation delimiter for CSV input.");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT, "String to insert as segmentation delimiter into the output name-sum-pairs.");
		this.getPropertyDescriptions().put(PROPERTYKEY_INPUTHASHEADERLINE, "First line of input is header line [true/false].");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Matrix Column Sum"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT_REGEX, "[\\,;]");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INPUTHASHEADERLINE, "false");
		
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
		
		// Construct scanner instances for input segmentation
		Scanner lineScanner = new Scanner(this.getInputPorts().get(ID_INPUT).getInputReader());
		lineScanner.useDelimiter("\\R+");
		
		// Array for header names
		List<String> headerNames = new ArrayList<String>();
		
		// Array for result values
		double[] sums = null;
		
		if (this.inputHasHeaderLine)
			// Read header line
			if (lineScanner.hasNext()){
				String headerLine = lineScanner.next();
				Scanner headerFieldScanner = new Scanner(new StringReader(headerLine));
				headerFieldScanner.useDelimiter(this.inputdelimiter);
				while (headerFieldScanner.hasNext())
					headerNames.add(headerFieldScanner.next());
				headerFieldScanner.close();
				sums = new double[headerNames.size()-1];
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
			if (line.matches(this.inputdelimiter+".*")) // Check whether the line starts with an empty field (the Scanner would omit that)
				fieldList.add("");
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
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(headerNames.get(i+1)+this.outputdelimiter+sums[i]+this.outputdelimiter+"\n");
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
