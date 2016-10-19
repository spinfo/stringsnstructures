package modules.segmentation;

import java.io.BufferedReader;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class SegmentCombinerModule extends ModuleImpl {

	private static final String ID_INPUT = "input";
	private static final String ID_OUTPUT = "output";

	private static final String SPLIT_DELIM = "|";

	public SegmentCombinerModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// set description and name
		this.setDescription(
				"Module to convert a list of binary segmentend strings into a list of unique string with all splits marked.");
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Segment Combiner Module");

		// Define I/O
		InputPort inputPort = new InputPort(ID_INPUT, "Binary splits", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "Multiple splits", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
	}

	@Override
	public boolean process() throws Exception {

		boolean result = true;

		// Variables for I/O
		BufferedReader reader = null;
		final InputPort in = this.getInputPorts().get(ID_INPUT);
		final OutputPort out = this.getOutputPorts().get(ID_OUTPUT);
		String line;

		try {
			reader = new BufferedReader(in.getInputReader());

			// A map used to collect the strings and their positions
			Map<String, Set<Integer>> stringSplits = new TreeMap<>();

			// Variables representing each String and it's splits
			String token = null;
			int splitPos = 0;
			Set<Integer> splits = null;

			// First collect the Strings and mark their split's positions
			while ((line = reader.readLine()) != null) {
				splitPos = line.indexOf(SPLIT_DELIM);
				token = line.substring(0, splitPos) + line.substring(splitPos + 1);

				// the splits set needs to be a tree set because we later need
				// the ordering
				splits = stringSplits.getOrDefault(token, new TreeSet<>());

				splits.add(splitPos);
				stringSplits.put(token, splits);
			}
			
			// Output the combined splits
			String output;
			for (String str : stringSplits.keySet()) {
				splits = stringSplits.get(str);
				output = stringWithSplits(str, splits);
				out.outputToAllCharPipes(output);
				out.outputToAllCharPipes("\n");
			}

		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			this.closeAllOutputs();
			if (reader != null) {
				reader.close();
			}
		}

		return result;
	}

	// split String s on the specified positions. Assumes an ordered set.
	private String stringWithSplits(String s, Set<Integer> splits) {
		int from = 0;

		StringBuilder sb = new StringBuilder();

		for (int to : splits) {
			sb.append(s.substring(from, to));
			sb.append(SPLIT_DELIM);

			from = to;
		}
		sb.append(s.substring(from));
		return sb.toString();
	}

	@Override
	public void applyProperties() throws Exception {
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
