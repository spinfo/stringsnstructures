package modules.segmentation;

import java.io.BufferedReader;
import java.util.Properties;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import base.workbench.ModuleRunner;

public class SegmentDistanceMatrixModule extends ModuleImpl {

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(SegmentDistanceMatrixModule.class, args);
	}


	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_DELIMITER_INPUT_SEGMENT = "segment input delimiter regex";
	public static final String PROPERTYKEY_DELIMITER_OUTPUT_CSVDELIMITER = "CSV output delimiter (!= ',')";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "input";
	private static final String ID_OUTPUT = "output";

	// Local variables
	private String inputdelimiterSegment;
	private String outputdelimiter_csv;

	public SegmentDistanceMatrixModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription(
				"Takes a list of segmented strings as input and outputs a segment right-neighbour-occurrence matrix.");
		this.setCategory("experimental");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_INPUT_SEGMENT,
				"Regular expression to use as segmentation delimiter for the segments of the string.");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT_CSVDELIMITER,
				"String to use as segmentation delimiter between CSV elements.");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Segment Distance Matrix");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT_SEGMENT, "\\|");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT_CSVDELIMITER, ";");

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
		boolean result = true;

		try {
			BufferedReader reader = new BufferedReader(getInputPorts().get(ID_INPUT).getInputReader());

			String line;
			String[] segments;
			SegmentDistanceMatrix matrix = new SegmentDistanceMatrix();

			while ((line = reader.readLine()) != null) {
				segments = line.split(inputdelimiterSegment);
				matrix.addSegments(segments);
			}

			getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(matrix.print(outputdelimiter_csv));

		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			this.closeAllOutputs();
		}

		return result;
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		this.inputdelimiterSegment = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_INPUT_SEGMENT,
				this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT_SEGMENT));
		this.outputdelimiter_csv = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT_CSVDELIMITER,
				this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT_CSVDELIMITER));

		// make sure that the output delimiter is not the comma, because that is
		// used to separate the distance lists in the fields
		if (this.outputdelimiter_csv == ",") {
			throw new IllegalArgumentException(
					"Cannot use ',' for csv output, bacause it is used for separating distances.");
		}

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
