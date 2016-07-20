package modules.basic_text_processing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import common.StringAlphabeticalComparator;
import common.StringLengthComparator;
import common.StringUnescaper;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class TextSorterModule extends ModuleImpl {

	// Identifiers for the Regex to filter on and whether it should be escaped
	public static final String PROPERTYKEY_INPUT_DELIMITER_REGEX = "input delimiter";
	public static final String PROPERTYKEY_OUTPUT_DELIMITER = "output delimiter";
	public static final String PROPERTYKEY_SORT_ORDER = "order";
	public static final String PROPERTYKEY_SORT_BY = "sort by";
	public static final String PROPERTYKEY_UNIQUE = "unique";

	// Identifiers for inputs and outputs
	public static final String INPUT = "input";
	public static final String OUTPUT = "output";

	// Variables for the workflow
	private String inputDelimiterRegex = null;
	private String outputDelimiter = null;
	private boolean ascendingOrder = false;
	private String sortBy = null;
	private boolean unique = false;

	public TextSorterModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// the module's name, description and category
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Text Sorter");
		this.setDescription("Sorts input text (segmented by specifiable pattern) according to alphabetical value or string length.");

		// descriptions and default values
		this.getPropertyDescriptions().put(PROPERTYKEY_INPUT_DELIMITER_REGEX, "Regular expression used as an input text segment delimiter.");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INPUT_DELIMITER_REGEX, "\\R+");
		this.getPropertyDescriptions().put(PROPERTYKEY_OUTPUT_DELIMITER, "Output delimiter string (values will be unescaped).");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTPUT_DELIMITER, "\\n");
		this.getPropertyDescriptions().put(PROPERTYKEY_SORT_ORDER, "Sort order [ascending|descending].");
		this.getPropertyDefaultValues().put(PROPERTYKEY_SORT_ORDER, "ascending");
		this.getPropertyDescriptions().put(PROPERTYKEY_SORT_BY, "Attribute to sort elementy by [alphabet|length].");
		this.getPropertyDefaultValues().put(PROPERTYKEY_SORT_BY, "length");
		this.getPropertyDescriptions().put(PROPERTYKEY_UNIQUE, "Whether to exclude duplicate segments from the list.");
		this.getPropertyDefaultValues().put(PROPERTYKEY_UNIQUE, "false");

		// setup I/O
		InputPort input = new InputPort(INPUT, "[text/plain] Input text to sort.", this);
		OutputPort output = new OutputPort(OUTPUT, "[text/plain] Sorted output text.", this);

		input.addSupportedPipe(CharPipe.class);
		output.addSupportedPipe(CharPipe.class);

		this.addInputPort(input);
		this.addOutputPort(output);
	}

	@Override
	public boolean process() throws Exception {
		
		// Initialize comparator
		Comparator<String> comparator = null;
		if (this.sortBy.equalsIgnoreCase("alphabet"))
			comparator = new StringAlphabeticalComparator(this.ascendingOrder);
		else if (this.sortBy.equalsIgnoreCase("length"))
			comparator = new StringLengthComparator(this.ascendingOrder);
		else
			throw new Exception("Invalid value for property '"+PROPERTYKEY_SORT_BY+"'.");

		// Create list to store segments in
		List<String> segmentList = new ArrayList<String>();
		
		// Initialize input scanner
		Scanner inputScanner = new Scanner(this.getInputPorts().get(INPUT).getInputReader());
		inputScanner.useDelimiter(inputDelimiterRegex);
		
		// Read input
		while (inputScanner.hasNext()){
			segmentList.add(inputScanner.next());
		}
		
		// Close input scanner
		inputScanner.close();
		
		// Sort list
		segmentList.sort(comparator);
		
		// Output iterator is taken from a set for unique values or from the normal list otherwise
		Iterator<String> segments;
		if (this.unique) {
			// use a linked hash set to preserve order
			segments = new LinkedHashSet<String>(segmentList).iterator();
		} else {
			segments = segmentList.iterator();
		}
		
		// Output list elements
		while (segments.hasNext())
			this.getOutputPorts().get(OUTPUT).outputToAllCharPipes(segments.next()+this.outputDelimiter);
		
		this.closeAllOutputs();
		
		return true;
	}

	@Override
	public void applyProperties() throws Exception {
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		this.inputDelimiterRegex = this.getProperties().getProperty(PROPERTYKEY_INPUT_DELIMITER_REGEX,
				this.getPropertyDefaultValues().get(PROPERTYKEY_INPUT_DELIMITER_REGEX));
		
		String propertyValue = this.getProperties().getProperty(PROPERTYKEY_OUTPUT_DELIMITER,
				this.getPropertyDefaultValues().get(PROPERTYKEY_OUTPUT_DELIMITER));
		this.outputDelimiter = StringUnescaper.unescape_perl_string(propertyValue);
		
		propertyValue = this.getProperties().getProperty(PROPERTYKEY_SORT_ORDER,
				this.getPropertyDefaultValues().get(PROPERTYKEY_SORT_ORDER));
		this.ascendingOrder = (propertyValue != null && propertyValue.equalsIgnoreCase("ascending"));
		
		this.sortBy = this.getProperties().getProperty(PROPERTYKEY_SORT_BY,
				this.getPropertyDefaultValues().get(PROPERTYKEY_SORT_BY));
		
		propertyValue = this.getProperties().getProperty(PROPERTYKEY_UNIQUE);
		this.unique = Boolean.parseBoolean(propertyValue);

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
