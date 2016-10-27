package modules.segmentation;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import common.parallelization.CallbackReceiver;

/**
 * Module that takes a list of segmented strings as input
 * and outputs a segment neighbour-co-occurrence matrix.
 * @author Marcel Boeing
 *
 */
import base.workbench.ModuleRunner;

public class SegmentMatrixModule extends ModuleImpl {

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(SegmentMatrixModule.class, args);
	}

	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_DELIMITER_INPUT_SEGMENT = "segment input delimiter regex";
	public static final String PROPERTYKEY_DELIMITER_INPUT_STRING = "string input delimiter regex";
	public static final String PROPERTYKEY_DELIMITER_OUTPUT_CSVDELIMITER = "CSV output delimiter";
	public static final String PROPERTYKEY_OMIT_ZERO_VALUES = "omit zero values";
	public static final String PROPERTYKEY_OMIT_EMPTY_ROWS_AND_COLUMNS = "omit empty rows and cols";
	
	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "input";
	private static final String ID_OUTPUT = "output";
	
	// Local variables
	private String inputdelimiter_segment;
	private String inputdelimiter_string;
	private String outputdelimiter_csv;
	private boolean omitZeroValues;
	private boolean omitEmptyRowsAndColumns;

	public SegmentMatrixModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Takes a list of segmented strings as input and outputs a segment right-neighbour-occurrence matrix.");
		
		// Add module category


		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_INPUT_SEGMENT, "Regular expression to use as segmentation delimiter for the segments of the string.");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_INPUT_STRING, "Regular expression to use as segmentation delimiter for the strings.");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT_CSVDELIMITER, "String to use as segmentation delimiter between CSV elements.");
		this.getPropertyDescriptions().put(PROPERTYKEY_OMIT_ZERO_VALUES, "Omit any value that is zero on output [true|false].");
		this.getPropertyDescriptions().put(PROPERTYKEY_OMIT_EMPTY_ROWS_AND_COLUMNS, "Omit any row or column that has only zero values on output [true|false].");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Segment Matrix"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT_SEGMENT, "\\|");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT_STRING, "\\n");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT_CSVDELIMITER, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OMIT_ZERO_VALUES, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OMIT_EMPTY_ROWS_AND_COLUMNS, "true");
		
		// Define I/O
		InputPort inputPort = new InputPort(ID_INPUT, "Segment list.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "CSV output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		
	}

	@Override
	public boolean process() throws Exception {
		
		/*
		 * Read input and construct matrix
		 */
		
		// Construct scanner instance for input segmentation (strings)
		Scanner stringInputScanner = new Scanner(this.getInputPorts().get(ID_INPUT).getInputReader());
		stringInputScanner.useDelimiter(this.inputdelimiter_string);
		
		// Prepare co-occurrence matrix
		Map<String,Set<String>> coOccurrenceMatrix = new TreeMap<String,Set<String>>();
		
		// Input read loop
		while (stringInputScanner.hasNext()){
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				stringInputScanner.close();
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}

			// Split next string into segments
			String[] segments = stringInputScanner.next().split(this.inputdelimiter_segment, -1);
			
			// Loop over segments
			for (int i=0; i<segments.length; i++){
				
				// Check whether segment is present in matrix (and add it otherwise)
				if (!coOccurrenceMatrix.containsKey(segments[i])){
					coOccurrenceMatrix.put(segments[i], new TreeSet<String>());
				}
				// Check if a _next_ segment exists and whether it is present in the current segment's co-occurrence map (and add it otherwise)
				if ((i+1)<segments.length && !coOccurrenceMatrix.get(segments[i]).contains(segments[i+1])){
					coOccurrenceMatrix.get(segments[i]).add(segments[i+1]);
				}
				
				// TODO Right now, the matrix will contain only the information, whether a segment-type follows another. This might be extended in the future if need arises.
			}
		}

		//Close input scanner.
		stringInputScanner.close();
		
		/*
		 * Eliminate empty rows and columns
		 */
		
		Set<String> rowsNotEmpty = new TreeSet<String>();
		Set<String> colsNotEmpty = new TreeSet<String>();
		if (this.omitEmptyRowsAndColumns){
			Iterator<String> keys = coOccurrenceMatrix.keySet().iterator();
			while (keys.hasNext()){
				String key = keys.next();
				Set<String> line = coOccurrenceMatrix.get(key);
				if (line.isEmpty())
					continue;
				else {
					colsNotEmpty.addAll(line);
					rowsNotEmpty.add(key);
				}
			}
		}
		
		/*
		 * Output to CSV
		 */
		
		// Output matrix in CSV -- header line
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.outputdelimiter_csv);
		Iterator<String> keys = coOccurrenceMatrix.keySet().iterator();
		while (keys.hasNext()){
			String key = keys.next();
			if (this.omitEmptyRowsAndColumns && !colsNotEmpty.contains(key))
				continue;
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(key+this.outputdelimiter_csv);
		}
		
		// Newline
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");
		
		// Output matrix in CSV -- data lines
		keys = coOccurrenceMatrix.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			if (this.omitEmptyRowsAndColumns && !rowsNotEmpty.contains(key))
				continue;
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(key);
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.outputdelimiter_csv);
			
			// Loop over every element of the matrix again
			Iterator<String> keys2 = coOccurrenceMatrix.keySet().iterator();
			while (keys2.hasNext()){
				String key2 = keys2.next();
				if (! (this.omitEmptyRowsAndColumns && !colsNotEmpty.contains(key2)) ){
					if (coOccurrenceMatrix.get(key).contains(key2))
						this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("1");
					else if (!this.omitZeroValues)
						this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("0");
					this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.outputdelimiter_csv);
				}
				
			}
			// Newline
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
		this.inputdelimiter_segment = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_INPUT_SEGMENT, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT_SEGMENT));
		this.inputdelimiter_string = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_INPUT_STRING, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT_STRING));
		this.outputdelimiter_csv = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT_CSVDELIMITER, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT_CSVDELIMITER));
		String omitZeroValuesString = this.getProperties().getProperty(PROPERTYKEY_OMIT_ZERO_VALUES, this.getPropertyDefaultValues().get(PROPERTYKEY_OMIT_ZERO_VALUES));
		if (omitZeroValuesString != null)
			this.omitZeroValues = Boolean.parseBoolean(omitZeroValuesString);
		String omitEmptyRowsAndColsString = this.getProperties().getProperty(PROPERTYKEY_OMIT_EMPTY_ROWS_AND_COLUMNS, this.getPropertyDefaultValues().get(PROPERTYKEY_OMIT_EMPTY_ROWS_AND_COLUMNS));
		if (omitEmptyRowsAndColsString != null)
			this.omitEmptyRowsAndColumns = Boolean.parseBoolean(omitEmptyRowsAndColsString);
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
