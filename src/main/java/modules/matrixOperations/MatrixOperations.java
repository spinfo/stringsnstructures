package modules.matrixOperations;

// Project specific imports.
import models.NamedFieldMatrix;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

// Java imports.
import java.io.BufferedReader;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This module uses any symmetric matrix and compares its content row wise.
 * Distances in form of Hamming distance are to be calculated.
 * @author christopher
 *
 */

public class MatrixOperations extends ModuleImpl {
	

	private static Logger LOGGER = Logger.getLogger(MatrixOperations.class.getName());
	
	// Property keys.
	
	private static final String PROPERTYKEY_DELIMITER = "Delimiter character"; 
	//private static final String PROPETYKEY_QUOTES = "Quote character";
	private static final String PROPERTYKEY_OUT_DELIMITER = "Delimiter used for the output";
	
	// I/O ports.
	
	private static final String ID_INPUT="Input";
	private static final String ID_OUTPUT="Output";
	
	// Variables.
	
	// Save the properties in these private variables.
	private String delimiter;
	// private String quotes;
	private String outputDelimiter;
	
	// Save the input matrix named field matrix.
	
	private NamedFieldMatrix matrix;
	
	// Save the calculated distance matrix as two dimensional double array.
	private int[][] hamMatrix;
	
	// Save the column names as string array.
	private String[] colNames;
	
	// Save the row names as string array.
	private String[] rowNames;
	
	// Constructors.
	
	public MatrixOperations(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("<h1>Named Field Matrix Hamming distance</h1><p>This module reads a plain Named Field Matrix (csv, tsv etc.)"
				+ "and calculates the Hamming distance row wise.</p>");
		
		// You can override the automatic category selection (for example if a module is to be shown in "deprecated")
		this.setCategory("matrix");

		// the module's name is defined as a property
		// Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Named Field Matrix Hamming distance");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER, "ASCII character used to delimit each column.");
		//this.getPropertyDescriptions().put(PROPETYKEY_QUOTES, "ASCII character used to signal usage of quotations.");
		this.getPropertyDescriptions().put(PROPERTYKEY_OUT_DELIMITER, "<p>Specifies the delimiter used in the CSV<br />table for the output file.</p>");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER, ",");
		//this.getPropertyDefaultValues().put(PROPETYKEY_QUOTES, "\"");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUT_DELIMITER, ";");
		
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
		
		try {
			BufferedReader reader = new BufferedReader(getInputPorts().get(ID_INPUT).getInputReader());

			String line;
			
			LOGGER.info("Starting to fill the matrix.");
						
			String wholeCsv = "";
			
			// Read csv information line wise.
			while ((line = reader.readLine()) != null) {
								
				wholeCsv += line + "\n";
				
			}

			this.matrix = NamedFieldMatrix.parseCSV(wholeCsv, this.delimiter);
			
			// After the matrix is filled the String wholeCsv is obsolete.
			wholeCsv = null;
			
			LOGGER.info("Matrix filled.");
			
			// Initialize the matrix holding the hamming distance values.
			this.hamMatrix = new int [this.matrix.getRowAmount()][this.matrix.getColumnsAmount()];

			
			LOGGER.info("Starting to calculate Hamming distances.");
			// Calculate the Hamming distances line-wise and save the result in form of 
			// a distance matrix.
			// Attention we start with the second row, since the first is the header line.
			for (int i = 0; i < this.matrix.getRowAmount() - 1 ; i ++) {
				for (int j = i + 1; j < this.matrix.getRowAmount(); j ++) {
					if (! (i == j) ) {
						this.hamMatrix[i][j] = this.matrix.getHammingDistanceForRows(i, j);
						// Mirror the matrix.
						this.hamMatrix[j][i] = this.hamMatrix[i][j];
					} else {
						this.hamMatrix[i][j] = 0;
					}
				}
			}
			
			LOGGER.info("Hamming distances calculated.");
			
			// Save the header line for the Hamming distance matrix.
			this.colNames = new String[this.matrix.getColumnsAmount()];
			
			for (int i = 0; i < this.colNames.length; i ++) {
				this.colNames[i] = this.matrix.getColumnName(i);
			}
						
			// Save the rowNames for the Hamming distance matrix.
			this.rowNames = new String[this.matrix.getRowAmount()];
			
			for (int i = 0; i < this.rowNames.length; i ++) {
				this.rowNames[i] = this.matrix.getRowName(i);
			}
			
			OutputPort hamOut = getOutputPorts().get(ID_OUTPUT);

			if (hamOut.isConnected()) {
				// Write an initial empty field.
				hamOut.outputToAllCharPipes(this.outputDelimiter);
				
				for (int i = 0; i < this.matrix.getRowAmount(); i ++) {
					if (i < this.matrix.getRowAmount() - 1)
						hamOut.outputToAllCharPipes(this.colNames[i] + this.outputDelimiter);
					else
						// Avoid printing the last ";".
						hamOut.outputToAllCharPipes(this.colNames[i]);
				}
				
				hamOut.outputToAllCharPipes("\n");
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
		
		String out = this.rowNames[row] + this.outputDelimiter;
		for (int i = 0; i < this.matrix.getColumnsAmount(); i ++) {
			// Avoid printing ";" as last character before new line.
			if (i < this.matrix.getColumnsAmount() - 1 )
				out += this.hamMatrix[row][i] + this.outputDelimiter ;
			else 
				out += this.hamMatrix[row][i];
		}
		out += "\n";
		return out;
		
	}
	
	@Override
	public void applyProperties() throws Exception {
		
		// Set defaults for properties not yet set.
		super.setDefaultsIfMissing();
		
		// Apply own properties.
		this.delimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER));
		// this.quotes = this.getProperties().getProperty(PROPETYKEY_QUOTES, this.getPropertyDefaultValues().get(PROPETYKEY_QUOTES));
		this.outputDelimiter = this.getProperties().getProperty(PROPERTYKEY_OUT_DELIMITER, this.getPropertyDefaultValues().get(PROPERTYKEY_OUT_DELIMITER)); 
		
		// Apply parent object's properties.
		super.applyProperties();
	}
	
	// End methods.

}
