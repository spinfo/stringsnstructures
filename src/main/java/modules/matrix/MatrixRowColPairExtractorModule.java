package modules.matrix;

import java.io.StringReader;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

import base.workbench.ModuleWorkbenchController;
import common.StringUnescaper;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class MatrixRowColPairExtractorModule extends ModuleImpl {

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_DELIMITER_INPUT_REGEX = "input delimiter regex";
	public static final String PROPERTYKEY_DELIMITER_OUTPUT_INNER = "output delimiter (inner)";
	public static final String PROPERTYKEY_DELIMITER_OUTPUT_OUTER = "output delimiter (outer)";
	public static final String PROPERTYKEY_MATCH = "match regex";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "csv input";
	private static final String ID_OUTPUT = "output";

	// Local variables
	private String inputdelimiter;
	private String outputdelimiterOuter;
	private String outputdelimiterInner;
	private String regex;

	public MatrixRowColPairExtractorModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription(
				"Lets you specify a regex and extracts every row/column combination that has a matching value.");

		// Add module category


		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_INPUT_REGEX,
				"Regular expression to use as segmentation delimiter for CSV input.");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT_INNER,
				"String to insert as segmentation delimiter between row- and column-values (escaped values will be unescaped).");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT_OUTER,
				"String to insert as segmentation delimiter between the output row-column-pairs (escaped values will be unescaped).");
		this.getPropertyDescriptions().put(PROPERTYKEY_MATCH, "Regex that describes a positive match.");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Matrix Row/Col Pair Extractor");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT_REGEX, "[\\,;]");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT_INNER, "|");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT_OUTER, "\\n");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MATCH, "[^0]+");

		// Define I/O
		InputPort inputPort = new InputPort(ID_INPUT, "CSV input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "text output.", this);
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
			fieldScanner.useDelimiter(this.inputdelimiter);
			String rowLabel = fieldScanner.next();
			int index = 1;
			while (fieldScanner.hasNext()) {
				String field = fieldScanner.next();
				if (field != null && pattern.matcher(field).matches())
					this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(rowLabel + this.outputdelimiterInner + headerNames[index] + this.outputdelimiterOuter);
				index++;
			}
			fieldScanner.close();
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
		this.inputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_INPUT_REGEX,
				this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT_REGEX));
		this.outputdelimiterInner = StringUnescaper.unescape_perl_string(this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT_INNER,
				this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT_INNER)));
		this.outputdelimiterOuter = StringUnescaper.unescape_perl_string(this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT_OUTER,
				this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT_OUTER)));
		this.regex = this.getProperties().getProperty(PROPERTYKEY_MATCH,
				this.getPropertyDefaultValues().get(PROPERTYKEY_MATCH));

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
