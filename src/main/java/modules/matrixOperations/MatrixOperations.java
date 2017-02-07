package modules.matrixOperations;

// Project specific imports.
import models.NamedFieldMatrix;
import common.parallelization.CallbackReceiver;
import models.NamedFieldMatrix;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import base.workbench.ModuleRunner;

// Java imports.
import java.io.BufferedReader;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.ArrayList;


public class MatrixOperations extends ModuleImpl {
	

	private static Logger LOGGER = Logger.getLogger(MatrixOperations.class.getName());
	
	// Property keys.
	
	private static final String PROPERTYKEY_DELIMITER = "Delimiter character"; 
	private static final String PROPETYKEY_QUOTES = "Quote character";
	//private static final String PROPERTYKEY_HEADER = "First row as headers";
	
	// I/O ports.
	
	private static final String ID_INPUT="Input";
	private static final String ID_OUTPUT="Output";
	
	// Variables.
	
	// Save the properties in these private variables.
	private String delimiter;
	private String quotes;
	//private boolean isHeader;
	
	// Save the input matrix named field matrix.
	
	private NamedFieldMatrix matrix;
	
	// Save the calculated distance matrix as two dimensional double array.
	private double[][] hamMatrix;
	
	// Save the column names as string array.
	private String[] colNames;
	
	// Save the row names as string array.
	private String[] rowNames;
	
	// Constructors.
	
	public MatrixOperations(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("<p>Segments two inputs and entwines them.</p><p>Among other things, you can use it<br/><ul><li>as a template to base your own modules on,</li><li>to review basic practices, like I/O,</li><li>and to get an overview of the standard implementations needed.</li></ul></p>");
		
		// You can override the automatic category selection (for example if a module is to be shown in "deprecated")
		this.setCategory("experimental");

		// the module's name is defined as a property
		// Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Named Field Matrix Operations");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER, "ASCII character used to delimit each column.");
		this.getPropertyDescriptions().put(PROPETYKEY_QUOTES, "ASCII character used to signal usage of quotations.");
		//this.getPropertyDescriptions().put(PROPERTYKEY_HEADER, "Signal whether the first row is a header line (true) or not (false).");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER, ",");
		this.getPropertyDefaultValues().put(PROPETYKEY_QUOTES, "\"");
		//this.getPropertyDefaultValues().put(PROPERTYKEY_HEADER, "true");
		
		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~).
		 * Every port can support a range of pipe types (currently
		 * byte or character pipes). Output ports can provide data
		 * to multiple pipe instances at once, input ports can
		 * in contrast only obtain data from one pipe instance.
		 */
		InputPort inputPort = new InputPort(ID_INPUT, "Named field matrix input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "Distance matrix output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		// Add I/O ports to instance.
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
	}

	
	
	// End constructors.
	
	// Methods.
	
	@Override
	public boolean process() throws Exception {

		// Initialize matrix.
		this.matrix = new NamedFieldMatrix();
		
		try {
			BufferedReader reader = new BufferedReader(getInputPorts().get(ID_INPUT).getInputReader());

			String line;
			
			LOGGER.info("Starting to fill the matrix.");
			
			// Read csv information line wise.
			while ((line = reader.readLine()) != null) {
				
				// Save the information in the matrix and extend it.
				NamedFieldMatrix.parseCSV(line, this.delimiter);
				
			}
			
			LOGGER.info("Matrix filled.");

			
			LOGGER.info("Starting to calculate Hamming distances.");
			// Calculate the Hamming distances line-wise and save the result in form of 
			// a distance matrix.
			// Attention we start with the second row, since the first is the header line.
			for (int i = 1; i < matrix.getRowAmount() - 1 ; i ++) {
				for (int j = i + 1; j < this.matrix.getRowAmount(); j ++) {
					if (! (i == j) ) {
						this.hamMatrix[i][j] = this.matrix.getHammingDistanceForRows(i, j);
					} else {
						this.hamMatrix[i][j] = 0d;
					}
				}
			}
			
			LOGGER.info("Hamming distances calculated.");
			
			// Save the header line for the hamming distance matrix.
			this.colNames = (String[]) this.matrix.getColumnNames().toArray();
			
			// Save the rowNames for the hamming distance matrix.
			this.rowNames = (String[]) this.matrix.getRowNames().toArray();
			
			OutputPort hamOut = getOutputPorts().get(ID_OUTPUT);

			if (hamOut.isConnected()) {
								
				for (int i = 0; i < this.matrix.getRowAmount(); i ++)
					hamOut.outputToAllCharPipes(toCsvLine(i));
			}
			
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		} finally {
			this.closeAllOutputs();
		}

		return true;
	}
	
	private String toCsvLine(int row) {
		
		if (row == 0) {
			String out = this.colNames[0];
			for (int i = 1; i < this.matrix.getColumnsAmount(); i++)
				out += this.colNames.toString() + ";" ;
			return out;
		} else {
			String out = this.rowNames[row];
			for (int i = 1; i < this.matrix.getColumnsAmount(); i ++) {
				out += this.hamMatrix[row][i] + ";" ;
			}
			return out;
		}
		
	}
	
	@Override
	public void applyProperties() throws Exception {
		
		// Set defaults for properties not yet set.
		super.setDefaultsIfMissing();
		
		// Apply own properties.
		this.delimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER));
		this.quotes = this.getProperties().getProperty(PROPETYKEY_QUOTES, this.getPropertyDefaultValues().get(PROPETYKEY_QUOTES));
		//this.isHeader = this.getProperties().getProperty(PROPERTYKEY_HEADER, this.getPropertyDefaultValues().get(PROPERTYKEY_HEADER)); 
		
		// Apply parent object's properties.
		super.applyProperties();
	}
	
	// End methods.

}
