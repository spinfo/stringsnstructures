package modules.lfgroups;

import java.io.BufferedReader;
import java.io.PipedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.parallelization.CallbackReceiver;
import models.NamedFieldMatrix;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

/**
 * DISCLAIMER: This is still work in progress. TODO Remove disclaimer after it
 * isn't
 */
public class LFGroupBuildingModule extends ModuleImpl {

	private static final Logger LOGGER = Logger.getLogger(LFGroupBuildingModule.class.getName());

	private static final String INPUT_SUCCESSOR_MATRIX_ID = "Successor Matrix";
	private static final String INPUT_PAIR_LIST_ID = "List of Pairs";

	private static final String OUTPUT_ID = "Test Output";

	public static final String PROPERTYKEY_CSV_DELIMITER = "csv delimiter";
	public static final String PROPERTYKEY_PAIR_LIST_CUTOFF_VALUE = "Pair list cutoff value";
	private String csvInputDelimiter;
	private int pairListCutoffValue;

	// A regex to split the pair list input on
	private static final Pattern PAIRS_INPUT_LINE = Pattern.compile("([^-]*)-([^-]*): ([0-9]+)");

	public LFGroupBuildingModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		this.setDescription(
				"Takes a successor matrix and a list from the matrix bitwise operation module and forms lexical-functional groups out of the matrix' entries.");

		// setup properties
		this.getPropertyDescriptions().put(PROPERTYKEY_CSV_DELIMITER, "Delimiter of the input csv cells.");
		this.getPropertyDefaultValues().put(PROPERTYKEY_CSV_DELIMITER, ";");
		this.getPropertyDescriptions().put(PROPERTYKEY_PAIR_LIST_CUTOFF_VALUE,
				"At or below which value reading of the pair list should stop.");
		this.getPropertyDefaultValues().put(PROPERTYKEY_PAIR_LIST_CUTOFF_VALUE, "5");

		// set name
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "LFGroup Building Module");

		// TODO: remove category, once not experimental anymore
		this.setCategory("experimental");

		// setup I/O
		final InputPort inputSuccessorsMatrix = new InputPort(INPUT_SUCCESSOR_MATRIX_ID,
				"A successors matrix from the Segment Matrix module", this);
		inputSuccessorsMatrix.addSupportedPipe(CharPipe.class);
		this.addInputPort(inputSuccessorsMatrix);

		final InputPort inputPairList = new InputPort(INPUT_PAIR_LIST_ID,
				"A list of pairs output by the MatrixBitwiseOperation Module.", this);
		inputPairList.addSupportedPipe(CharPipe.class);
		this.addInputPort(inputPairList);

		// TODO: Change output port description once the output is determined.
		final OutputPort output = new OutputPort(OUTPUT_ID, "For now just a log output of the groups created", this);
		output.addSupportedPipe(CharPipe.class);
		this.addOutputPort(output);
	}

	@Override
	public boolean process() throws Exception {

		final OutputPort out = this.getOutputPorts().get(OUTPUT_ID);

		boolean result = true;
		PipedReader successorsMatrixReader = null;
		PipedReader pairListReader = null;

		try {
			// read the successors matrix from input
			successorsMatrixReader = this.getInputPorts().get(INPUT_SUCCESSOR_MATRIX_ID).getInputReader();
			NamedFieldMatrix successors = NamedFieldMatrix.parseCSV(successorsMatrixReader, csvInputDelimiter);

			// read the pair list from input
			pairListReader = this.getInputPorts().get(INPUT_PAIR_LIST_ID).getInputReader();
			List<List<String>> pairs = readPredecessorsPairListFromInput(pairListReader, pairListCutoffValue);

			// 1. Read and merge groups
			// 1.a) Read a list of all groups read from the matrix by using the
			// combinations. Equal groups are merged in the process
			out.outputToAllCharPipes("AFTER MERGING\n");
			List<LFGroup> all = createGroupsAndMergeMatches(pairs, successors);
			for (LFGroup g : all) {
				out.outputToAllCharPipes(g.prettyPrint() + "\n");
			}
			// 1.b) eliminate functional subgroups to remove duplicates that are
			// fully contained in another group.
			eliminateFunctionalSubgroups(all);

			// 2. DETECT competing groups
			// they are saved as the sum of their middle lex length ordered to
			// all
			// Groups with that lex length, ordering is descending to get at the
			// first groups fast
			Map<Double, HashSet<LFGroupPair>> competitions = new TreeMap<Double, HashSet<LFGroupPair>>(
					Collections.reverseOrder());

			// 2.a) prepare by generating combinations
			for (LFGroup g : all) {
				g.makeCombinations();
			}

			// 2.b) compare each to each other and test for conccurences
			// use a new list of the same size to make sure, that combinations
			// are
			// only tested once
			List<LFGroup> others = new ArrayList<LFGroup>(all.size());
			for (LFGroup current : all) {
				if (others.isEmpty()) {
					others.add(current);
					continue;
				}

				// actual comparison happens here
				for (LFGroup other : others) {
					// two groups compete, if there is at least one element in
					// the
					// group's combination's intersection
					LFGroupPair pair = new LFGroupPair(current, other);
					if (pair.isCompeting()) {
						HashSet<LFGroupPair> sameRanked = competitions.get(pair.getRank());
						if (sameRanked == null) {
							sameRanked = new HashSet<LFGroupPair>();
						}
						sameRanked.add(pair);
						competitions.put(pair.getRank(), sameRanked);
					}
				}

				others.add(current);
			}

			// 3. RESOLVE competitions
			// repeatedly get the highest ranked competition and remove it
			while (!competitions.keySet().isEmpty()) {
				// The highest ranked is taken to be the first key returned by
				// the
				// iterator. This is guaranteed by how the TreeSet was set up
				// above.
				Double highestRank = competitions.keySet().iterator().next();
				HashSet<LFGroupPair> competingPairs = competitions.remove(highestRank);

				// Regardless of whether there are multiple highest ranked
				// competitions, just use the first one
				Iterator<LFGroupPair> iterator = competingPairs.iterator();
				LFGroupPair competition = iterator.next();
				iterator.remove();
				if (iterator.hasNext()) {
					competitions.put(highestRank, competingPairs);
				}

				// first check if the ranking has changed since the last
				// iteration.
				// If it has, commit the new ranking and start with the highest
				// ranked again (i.e. go to the top of the current loop)
				Double currentRank = competition.getRank();
				if (!currentRank.equals(highestRank)) {
					HashSet<LFGroupPair> sameRanked = competitions.getOrDefault(currentRank, new HashSet<>());
					sameRanked.add(competition);
					competitions.put(currentRank, sameRanked);
				} else {
					competition.resolveCompetition();
					// TODO: Check might be omitted after testing
					if (competition.isCompeting()) {
						LOGGER.warning("Competition not resolved.");
					}
				}
			}

			out.outputToAllCharPipes("AFTER REMOVING COMPETITIONS\n");
			for (LFGroup g : all) {
				if (!g.lexicals.isEmpty()) {
					out.outputToAllCharPipes(g.prettyPrint() + "\n");
				}
			}

		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			closeAllOutputs();
			if (successorsMatrixReader != null)
				successorsMatrixReader.close();
			if (pairListReader != null)
				pairListReader.close();
		}

		return result;
	}

	// TODO: comment
	private static List<String> getColumnNamesIntersection(NamedFieldMatrix matrix, String rowName1, String rowName2) {
		final List<String> result = new ArrayList<>();

		final Integer row1 = matrix.getRowNo(rowName1);
		final Integer row2 = matrix.getRowNo(rowName2);

		if (row1 == null || row2 == null) {
			throw new IllegalArgumentException("Row missing for strings: " + rowName1 + "/" + rowName2);
		}

		for (int i = 0; i < matrix.getColumnsAmount(); i++) {
			if (matrix.getValue(row1, i).equals(1.0) && matrix.getValue(row2, i).equals(1.0)) {
				result.add(matrix.getColumnName(i));
			}
		}

		return result;
	}

	// TODO: Comment
	private static LFGroup createGroupFromMatrix(NamedFieldMatrix matrix, String rowName1, String rowName2) {
		LFGroup result = new LFGroup();

		result.lexicals.add(rowName1);
		result.lexicals.add(rowName2);

		for (String colName : getColumnNamesIntersection(matrix, rowName1, rowName2)) {
			Functional f = new Functional(colName);
			result.functionals.add(f);
		}

		return result;
	}

	// TODO: comment
	private static List<LFGroup> createGroupsAndMergeMatches(List<List<String>> combinations,
			NamedFieldMatrix successors) {
		List<LFGroup> result = new ArrayList<LFGroup>();

		for (List<String> combination : combinations) {
			LFGroup current = createGroupFromMatrix(successors, combination.get(0), combination.get(1));

			// handle the very first group created, it initialises the "all"
			// group of groups
			if (result.isEmpty()) {
				result.add(current);
				continue;
			}

			// first do one scan for exact matches. Once one is found, join the
			// groups and continue
			boolean exactMatch = false;
			for (LFGroup old : result) {
				if (old.functionals.equals(current.functionals)) {
					old.lexicals.addAll(current.lexicals);
					exactMatch = true;
					break;
				}
			}

			if (!exactMatch) {
				result.add(current);
			}
		}

		return result;
	}

	// TODO: Comment
	private static List<LFGroup> eliminateFunctionalSubgroups(List<LFGroup> all) {
		List<LFGroup> toCompare = new ArrayList<LFGroup>();
		List<LFGroup> subsets = new ArrayList<LFGroup>();

		for (LFGroup current : all) {

			if (toCompare.isEmpty()) {
				toCompare.add(current);
				continue;
			}

			for (LFGroup old : toCompare) {
				if (old.functionals.containsAll(current.functionals)) {
					current.lexicals.removeAll(old.lexicals);
					old.functionalSubgroups.add(current);
					subsets.add(current);
					// TODO test if any lexicals remain?
				} else if (current.functionals.containsAll(old.functionals)) {
					// I think this should never happen due to implicit ordering
					// of groups (the combinations list being sorted in
					// descending order of common functionals). Let's see if
					// that's true...
					// TODO: Proof that and remove comment
					throw new IllegalStateException("Should never happen...");
				}
			}

			toCompare.add(current);
		}

		List<LFGroup> result = new ArrayList<LFGroup>(all);
		result.removeAll(subsets);
		return result;
	}

	// Read a list of predecessor pairs from the reader. Stop when the cutoff
	// value is reached.
	// For Example: "mone-laud: 12" is an input line. Reading would stop if 12
	// was at or below the cutoff value.
	private List<List<String>> readPredecessorsPairListFromInput(Reader inputReader, int cutoffValue) throws Exception {
		final List<List<String>> result = new ArrayList<>();
		final BufferedReader reader = new BufferedReader(inputReader);

		Set<String> pairsRead = new HashSet<String>();
		String line = null;
		Matcher matcher = null;
		int pairValue = Integer.MAX_VALUE;
		String one = null;
		String two = null;
		while ((line = reader.readLine()) != null) {
			matcher = PAIRS_INPUT_LINE.matcher(line);
			if (!matcher.matches()) {
				throw new Exception("Bad input line: " + line);
			}
			pairValue = Integer.parseInt(matcher.group(3));
			// only add if we are above the cutoff value
			if (pairValue > cutoffValue) {
				one = matcher.group(1);
				two = matcher.group(2);
				// only add if a duplicate wasn't read before
				if (!pairsRead.contains(two + one)) {
					result.add(Arrays.asList(one, two));
					pairsRead.add(one + two);
				}
			}
		}

		return result;
	}

	@Override
	public void applyProperties() throws Exception {
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		if (this.getProperties().getProperty(PROPERTYKEY_CSV_DELIMITER) != null) {
			this.csvInputDelimiter = this.getProperties().getProperty(PROPERTYKEY_CSV_DELIMITER);
		}
		if (this.getProperties().getProperty(PROPERTYKEY_PAIR_LIST_CUTOFF_VALUE) != null) {
			this.pairListCutoffValue = Integer
					.parseInt(this.getProperties().getProperty(PROPERTYKEY_PAIR_LIST_CUTOFF_VALUE));
		}

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
