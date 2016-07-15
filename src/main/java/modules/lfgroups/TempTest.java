package modules.lfgroups;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import models.NamedFieldMatrix;

public class TempTest {

	private static final Logger LOGGER = Logger.getLogger(TempTest.class.getName());

	public static void main(String[] args) {

		NamedFieldMatrix successors;
		// NamedFieldMatrix bitwise;

		try {
			FileReader reader = new FileReader(new File("/media/data/Downloads/lfgroups/matrix.csv"));
			// bitwise = NamedFieldMatrix.parseCSV(reader, ";");
			reader = new FileReader(new File("/media/data/Downloads/lfgroups/morpho-out.csv"));
			successors = NamedFieldMatrix.parseCSV(reader, ";");
		} catch (Exception e) {
			LOGGER.severe("Error while reading input");
			return;
		}

		// TODO: List will be fetched from bitwise matrix module output
		String[][] combinations = { { "laudav", "monu" }, { "lauda", "mone" }, { "laudaba", "laudavera" },
				{ "laudaba", "moneba" }, { "laudaba", "monuera" }, { "laudaver", "monuer" }, { "laudavera", "laudaba" },
				{ "laudavera", "moneba" }, { "laudavera", "monuera" }, { "moneba", "monuera" }, { "monuera", "moneba" },
				{ "laud", "laudaver" } };

		// contains all the combinations not equal to one another
		List<LFGroup> all = createGroupsAndMergeMatches(combinations, successors);
		LOGGER.info("EQUAL groups:");
		for (LFGroup g : all) {
			System.out.println(g.prettyPrint());
		}

		// search for supersets and merge
		List<LFGroup> primaries = eliminateFunctionalSubgroups(all);

		// primaries contains now only those groups not subgroup of another
		LOGGER.info("PRIMARY groups:");
		for (LFGroup g : primaries) {
			System.out.println(g.prettyPrint());
		}

		// 2. DETECT competing groups
		// they are saved as the sum of their middle lex length ordered to all
		// Groups with that lex length, ordering is descending to get at the
		// first groups fast
		Map<Double, HashSet<LFGroupPair>> competitions = new TreeMap<Double, HashSet<LFGroupPair>>(
				Collections.reverseOrder());

		// 2.a) prepare by generating combinations
		for (LFGroup g : all) {
			g.makeCombinations();
		}

		// 2.b) compare each to each other and test for conccurences
		// use a new list of the same size to make sure, that combinations are
		// only tested once
		List<LFGroup> others = new ArrayList<LFGroup>(all.size());
		for (LFGroup current : all) {
			if (others.isEmpty()) {
				others.add(current);
				continue;
			}

			// actual comparison happens here
			for (LFGroup other : others) {
				// two groups compete, if there is at least one element in the
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
		LOGGER.info("RESOLVING COMPETITIONS");
		while (!competitions.keySet().isEmpty()) {
			// The highest ranked is taken to be the first key returned by the
			// iterator. This is guaranteed by how the TreeSet was set up above.
			Double highestRank = competitions.keySet().iterator().next();
			HashSet<LFGroupPair> competingPairs = competitions.remove(highestRank);

			// Regardless of whether there are multiple highest ranked
			// competitions, just use the first one
			Iterator<LFGroupPair> iterator = competingPairs.iterator();
			LFGroupPair competition = iterator.next();
			if (iterator.hasNext()) {
				competitions.put(highestRank, competingPairs);
			}

			// first check if the ranking has changed since the last iteration.
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

		LOGGER.info("AFTER RESOLVING: ");
		for (LFGroup g : all) {
			if (!g.lexicals.isEmpty()) {
				System.out.println(g.prettyPrint());
			}
		}

	}

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

	private static List<String> getColumnNamesIntersection(NamedFieldMatrix matrix, String rowName1, String rowName2) {
		final List<String> result = new ArrayList<>();

		final int row1 = matrix.getRowNo(rowName1);
		final int row2 = matrix.getRowNo(rowName2);
		for (int i = 0; i < matrix.getColumnsAmount(); i++) {
			if (matrix.getValue(row1, i).equals(1.0) && matrix.getValue(row2, i).equals(1.0)) {
				result.add(matrix.getColumnName(i));
			}
		}

		return result;
	}

	private static List<LFGroup> createGroupsAndMergeMatches(String[][] combinations, NamedFieldMatrix successors) {
		List<LFGroup> result = new ArrayList<LFGroup>();

		for (String[] combination : combinations) {
			LFGroup current = createGroupFromMatrix(successors, combination[0], combination[1]);

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

}
