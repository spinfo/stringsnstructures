package modules.morphology;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.workbench.ModuleRunner;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class MorphologyCheckModule extends ModuleImpl {

	private static final String INPUT_CORRECT_MORPHEMES_ID = "Correct Morpheme Groups";
	private static final String INPUT_MORPHEME_CANDIDATES_ID = "Morpheme Candidate groups";

	private static final String OUTPUT_ID = "output";

	private static final String MODULE_DESC = "A module to check groups of comma separated morphemes (one group per line, comma separated) against groups of correct morphemes (one group per line, comma separated, optionally starting with a label followed by ':')";

	private static final Pattern LABEL_PATTERN = Pattern.compile("^([^:]+):.*");

	private static final String GROUPS_DELIM = ",";

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(MorphologyCheckModule.class, args);
	}

	public MorphologyCheckModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		// Call parent constructor
		super(callbackReceiver, properties);

		// define module description and name
		this.setDescription(MODULE_DESC);
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Morphology Check Module");

		// define Input/Output ports
		InputPort inputCorrect = new InputPort(INPUT_CORRECT_MORPHEMES_ID,
				"groups of correct morphemes to check against (one group per line, comma separated, optionally starting with a label followed by ':')",
				this);
		InputPort inputCandidates = new InputPort(INPUT_MORPHEME_CANDIDATES_ID,
				"groups of morpheme candidates (one group per line, comma separated)", this);
		OutputPort output = new OutputPort(OUTPUT_ID,
				"An evaluation of the morpheme candidates group, one evaluation per candidate input line.", this);
		inputCorrect.addSupportedPipe(CharPipe.class);
		inputCandidates.addSupportedPipe(CharPipe.class);
		output.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputCorrect);
		super.addInputPort(inputCandidates);
		super.addOutputPort(output);

	}

	@Override
	public boolean process() throws Exception {
		boolean result = true;

		try {
			OutputPort out = this.getOutputPorts().get(OUTPUT_ID);
			
			BufferedReader correctGroupsReader = new BufferedReader(
					this.getInputPorts().get(INPUT_CORRECT_MORPHEMES_ID).getInputReader());
			BufferedReader candidateGroupsReader = new BufferedReader(
					this.getInputPorts().get(INPUT_MORPHEME_CANDIDATES_ID).getInputReader());

			// first build a Hashmap over the correct morphemes ordering them to
			// their group labels
			Map<String, Set<String>> correctMorphemes = new HashMap<>();
			Set<String> allLabels = new HashSet<>();
			Set<String> labels;
			String inputLine;
			String label;
			Integer altLabel = 0;
			Matcher labelMatcher;
			while ((inputLine = correctGroupsReader.readLine()) != null) {
				// find out the current Label name from the input line
				labelMatcher = LABEL_PATTERN.matcher(inputLine);
				if (labelMatcher.matches()) {
					label = labelMatcher.group(1);
					inputLine = inputLine.substring(labelMatcher.end(1));
				} else {
					label = altLabel.toString();
					altLabel += 1;
				}
				allLabels.add(label);

				// map each morpheme to the current label
				for (String token : inputLine.split(GROUPS_DELIM)) {
					token = token.trim();

					if (token.isEmpty()) {
						continue;
					}

					labels = correctMorphemes.getOrDefault(token, new HashSet<>());
					labels.add(label);
					correctMorphemes.put(token, labels);
				}
			}

			// parse candidate groups and output some statistics for each
			String[] candidates;
			inputLine = null;
			labels = null;
			Map<String, Integer> labelCounts = new HashMap<>();
			while ((inputLine = candidateGroupsReader.readLine()) != null) {
				// prepare variables for counting
				int count = 0;
				int hits = 0;
				for (String l : allLabels) {
					labelCounts.put(l, 0);
				}
				
				// get candidates and ignore groups with only one candidate
				candidates = inputLine.split(GROUPS_DELIM);
				if (candidates.length <= 1) {
					continue;
				}

				// loop over groups and count
				for (String candidate : candidates) {
					candidate = candidate.trim();

					if (candidate.isEmpty()) {
						continue;
					}

					count += 1;
					labels = correctMorphemes.get(candidate);
					if (labels != null) {
						hits += 1;
						for (String l : labels) {
							labelCounts.put(l, labelCounts.get(l) + 1);
						}
					}
				}
				
				// output the evaluation
				StringBuilder outStr = new StringBuilder();
				outStr.append(String.format("%.8f, (", (hits / (float) count)));
				for (String l : labelCounts.keySet()) {
					outStr.append(String.format("%s: %.8f, ", l, (labelCounts.get(l) / (float) count)));
				}
				outStr.setLength(outStr.length() - 2);
				outStr.append(") - [");
				outStr.append(inputLine);
				outStr.append("]\n");
				out.outputToAllCharPipes(outStr.toString());
				outStr.setLength(0);
			}

		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			this.closeAllOutputs();
		}

		return result;
	}

}
