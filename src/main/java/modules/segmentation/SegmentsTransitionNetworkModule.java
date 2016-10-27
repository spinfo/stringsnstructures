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
import modules.transitionNetwork.TransitionNetworkArray;

import base.workbench.ModuleRunner;

public class SegmentsTransitionNetworkModule extends ModuleImpl {

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(SegmentsTransitionNetworkModule.class, args);
	}


	private static final String ID_INPUT = "input";
	private static final String ID_OUTPUT = "output";

	public SegmentsTransitionNetworkModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// set description and name
		this.setDescription("Module to convert a list of segmentend strings into a transition network.");
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Segments Transition Network Module");
		
		// set category
		this.setCategory("experimental");

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

			// A string builder to collect the total text of different tokens
			final StringBuilder textBuilder = new StringBuilder();

			// First collect the Strings and mark their split's positions
			while ((line = reader.readLine()) != null) {
				splitPos = line.indexOf('|');
				token = line.substring(0, splitPos) + line.substring(splitPos + 1);
				splits = stringSplits.get(token);

				// if the token is new, create a new splits set and append it to
				// the total text
				if (splits == null) {
					// this needs to be a tree set, because we need it ordered
					splits = new TreeSet<>();
					textBuilder.append(token);
					textBuilder.append('$');
				}

				splits.add(splitPos);
				stringSplits.put(token, splits);
			}
			
			
			// make a network and fill it
			TransitionNetworkArray network = new TransitionNetworkArray(100, 100);
			
			for (String string : stringSplits.keySet()) {
				splits = stringSplits.get(string);
				String[] splitted = splitString(string, splits);
				
				System.out.print(string + ": " + splits + "(");
				for (String s : splitted) {
					System.out.print(s + "-");
				}
				System.out.println(")");
				
				network.addPath(splitted);
			}

			String s = network.print();
			out.outputToAllCharPipes(s);
			
			// DONT COMMIT
			System.out.println(s);
			

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
	private String[] splitString(String s, Set<Integer> splits) {
		int from = 0;
		int idx = 0;
		String[] result = new String[splits.size() + 1];
		
		for (int to : splits) {
			result[idx] = s.substring(from, to);
			
			from = to;
			idx += 1;
		}
		result[idx] = s.substring(from);
		return result;
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}



}
