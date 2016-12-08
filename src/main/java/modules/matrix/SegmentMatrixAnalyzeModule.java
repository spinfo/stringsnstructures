package modules.matrix;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import common.parallelization.CallbackReceiver;
import models.NamedFieldMatrix;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class SegmentMatrixAnalyzeModule extends ModuleImpl {

	private static final Logger LOGGER = Logger.getLogger(SegmentMatrixAnalyzeModule.class.getName());

	// I/O ids
	private static final String INPUT_SEGMENT_MATRIX_ID = "segment matrix";
	private static final String INPUT_SEGMENTATION_CANDIDATES_ID = "segmentation candidates";
	private static final String OUTPUT_ID = "output";

	// property ids
	private static final String PROPERTYKEY_INPUT_CSV_DELIM = "matrix input csv delimiter";
	private static final String PROPERTYKEY_CANDIDATE_SEGMENTS_DELIM = "delimiter between candidate's segments";
	private static final String PROPERTYKEY_CANDIDATES_DELIM = "delimiter between candidate's";

	// property values
	private String inputCsvDelim;
	private String candidateSegmentsDelim;
	private String candidatesDelim;

	public SegmentMatrixAnalyzeModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// TODO: Remove setting of module category once no longer experimental
		this.setCategory("experimental");

		// TODO: Change module description and maybe name once this module is
		// productive
		this.setDescription("For now this module just demonstrates how the SegmentMatrix can be queried for values.");
		this.setName("SegmentMatrixAnalyzeModule");

		// define property descriptions
		this.getPropertyDescriptions().put(PROPERTYKEY_INPUT_CSV_DELIM, "Csv delimiter of the input matrix.");
		this.getPropertyDescriptions().put(PROPERTYKEY_CANDIDATE_SEGMENTS_DELIM,
				"Opposing split candidates contain segments split by this delimiter.");
		this.getPropertyDescriptions().put(PROPERTYKEY_CANDIDATES_DELIM,
				"Opposing split candidates are on single lines, delimited by this sign.");

		// define property defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, this.getName());
		this.getPropertyDefaultValues().put(PROPERTYKEY_INPUT_CSV_DELIM, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_CANDIDATE_SEGMENTS_DELIM, Pattern.quote("|"));
		this.getPropertyDefaultValues().put(PROPERTYKEY_CANDIDATES_DELIM, ";");

		// define module I/O
		InputPort distanceMatrixIn = new InputPort(INPUT_SEGMENT_MATRIX_ID,
				"[text/csv] NamedFieldMatrix from SegmentMatrixModule", this);
		distanceMatrixIn.addSupportedPipe(CharPipe.class);
		super.addInputPort(distanceMatrixIn);

		InputPort segmentationCandidatesIn = new InputPort(INPUT_SEGMENTATION_CANDIDATES_ID,
				"[text/plain] A list of segmentation candidates, one per row.", this);
		segmentationCandidatesIn.addSupportedPipe(CharPipe.class);
		super.addInputPort(segmentationCandidatesIn);

		// TODO: Change output description once the output is no longer only a
		// demonstration of querying the matrix
		OutputPort out = new OutputPort(OUTPUT_ID, "[text/plain] A list of labels with their distance in the matrix.",
				this);
		out.addSupportedPipe(CharPipe.class);
		super.addOutputPort(out);
	}

	@Override
	public boolean process() throws Exception {

		boolean result = true;

		try {
			// the output port to write to
			OutputPort out = this.getOutputPorts().get(OUTPUT_ID);

			// first read the segment matrix from the input
			final NamedFieldMatrix segmentMatrix = readSegmentMatrix();

			// then traverse the other input containing segmentation
			// candidates
			final BufferedReader candidatesReader = new BufferedReader(
					this.getInputPorts().get(INPUT_SEGMENTATION_CANDIDATES_ID).getInputReader());

			// line is a string of binary split candidates, e.g.:
			// "a|bcd;ab|cd;abc|d"
			// we will iterate over candidates and their splits/segments
			String line = candidatesReader.readLine().trim();
			String[] candidates;
			String[] segments;
			double value;
			while (line != null) {
				candidates = line.split(candidatesDelim);
				for (String candidate : candidates) {

					// a single candidate contains two segments, eg: "a|bcd" has
					// segments "a", "bcd"
					segments = candidate.split(candidateSegmentsDelim);

					if (segments.length != 2) {
						LOGGER.warning("Not a binary segmentation: '" + candidate + "'");
						continue;
					}

					// Demonstration: Query the input matrix for a value:
					value = segmentMatrix.getValue(segments[0], segments[1]);

					// Output candidates and values
					out.outputToAllCharPipes(String.format("%s-%s: %f\n", segments[0], segments[1], value));
				}
				out.outputToAllCharPipes("---\n");

				// setup next loop
				line = candidatesReader.readLine();
			}

		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			this.closeAllOutputs();
		}

		return result;
	}

	private NamedFieldMatrix readSegmentMatrix() throws Exception {
		InputPort matrixIn = this.getInputPorts().get(INPUT_SEGMENT_MATRIX_ID);
		Reader matrixReader = matrixIn.getInputReader();

		return NamedFieldMatrix.parseCSV(matrixReader, this.inputCsvDelim);
	}

	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();

		Properties props = this.getProperties();
		Map<String, String> defaults = this.getPropertyDefaultValues();

		this.inputCsvDelim = props.getProperty(PROPERTYKEY_INPUT_CSV_DELIM, defaults.get(PROPERTYKEY_CANDIDATES_DELIM));
		this.candidateSegmentsDelim = props.getProperty(PROPERTYKEY_CANDIDATE_SEGMENTS_DELIM,
				defaults.get(PROPERTYKEY_CANDIDATE_SEGMENTS_DELIM));
		this.candidatesDelim = props.getProperty(PROPERTYKEY_CANDIDATES_DELIM,
				defaults.get(PROPERTYKEY_CANDIDATES_DELIM));

		super.applyProperties();
	}

}
