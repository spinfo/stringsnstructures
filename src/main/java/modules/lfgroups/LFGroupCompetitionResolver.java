package modules.lfgroups;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.google.common.collect.Sets;

/**
 * This is a class with purely static methods. It contains all steps necessary
 * to resolve a competition between two LFGroups.
 */
final class LFGroupCompetitionResolver {

	private static final Logger LOGGER = Logger.getLogger(LFGroupCompetitionResolver.class.getName());

	/**
	 * Class needs no instances
	 */
	private LFGroupCompetitionResolver() {
	}

	// TODO: Name should reflect that this is an attempt or should return
	// boolean indicating success or failure
	static void resolveCompetition(LFGroup integrating, LFGroup integrated) {
		// condition 1: All of the integrating group's lexicals must be the
		// starts of some lexicals of the integrated group
		Map<String, Set<String>> matchingLexicalsWithEnds = new TreeMap<>();
		Set<String> commonEnds = null;
		for (String lexical : integrating.lexicals) {
			Set<String> matchingLexicalEnds = integrated.getLexicalEndsBeginningWith(lexical);
			if (!matchingLexicalEnds.isEmpty()) {
				matchingLexicalsWithEnds.put(lexical, matchingLexicalEnds);
				if (commonEnds == null) {
					commonEnds = matchingLexicalEnds;
				} else {
					commonEnds = Sets.intersection(commonEnds, matchingLexicalEnds);
				}
			}
		}
		if (matchingLexicalsWithEnds.keySet().isEmpty() || commonEnds.isEmpty()) {
			return;
		}
		
		if (!matchingLexicalsWithEnds.keySet().equals(integrating.lexicals)) {
			// At this point the groups clearly bear some resemblance due to
			// the matching lexicals, but integration is not possible,
			// because some information would be lost.
			// However some different treatment of groups could be possible
			// at this stage.
			// TODO: Take a second look. What could be done with such
			// groups?
			 LOGGER.warning("Matching lexical begins without possibility to integrate.");
			return;
		}

		// condition 2: The integrated group must have functionals that are
		// ends of the functionals of the integrating group
		Map<Functional, Set<Functional>> matchingFunctionals = new TreeMap<>();
		for (Functional integratedFunctional : integrated.functionals) {
			Set<Functional> integratingFunctionals = integrating.getFunctionalsEndingIn(integratedFunctional.get());
			for (Functional f : integratingFunctionals) {
				Set<Functional> matching = matchingFunctionals.getOrDefault(f, new TreeSet<>());
				matching.add(integratedFunctional);
				matchingFunctionals.put(f, matching);
			}
		}
		if (matchingFunctionals.keySet().isEmpty()) {
			LOGGER.warning(
					"Found matching lexical begins but no matching functional ends. Groups are not actually competing.");
			return;
		}

		// If the conditions above are met, the integration can begin
		LOGGER.info("Integrating by: " + matchingLexicalsWithEnds + " with: " + matchingFunctionals);

		// The following three loops look complicated, but are (I think)
		// necessary. We iterate over the beginning of the lexicals with their
		// respective ends and then look for a functional that begins with the
		// end of the lexical.
		// If such a combination is found, we still have to make sure, that
		// there is a matching functional in the integrated group. Only once
		// that is found, the integration may proceed.
		for (String lexicalBegin : matchingLexicalsWithEnds.keySet()) {
			Set<String> validLexicalEnds = Sets.intersection(commonEnds, matchingLexicalsWithEnds.get(lexicalBegin));
			// for each lexical begin we count the number of
			// replacements made
			int splitsMade = 0;
			for (String lexicalEnd : validLexicalEnds) {
				for (Functional f : matchingFunctionals.keySet()) {
					// splits from the corresponding functional in the
					// integrated group must be added, so search it
					if (f.get().startsWith(lexicalEnd)) {
						String integratedFunctionalNeeded = f.get().substring(lexicalEnd.length());
						for (Functional integratedFunctional : matchingFunctionals.get(f)) {
							if (integratedFunctional.get().equals(integratedFunctionalNeeded)) {
								f.splitOn(integratedFunctional);
								splitsMade += 1;
							}
						}
					}
				}
			}
			// if enough splits were made to cover all functionals from
			// the integrated group, the corresponding lexical for that
			// group may be deleted
			if (splitsMade == integrated.functionals.size()) {
				for (String lexicalEnd : validLexicalEnds) {
					integrated.lexicals.remove(lexicalBegin + lexicalEnd);
					// TODO: Has this to happen here? We should not have to
					// call this at all.
					integrated.makeCombinations();
				}
			} else {
				// For each functional in the integrated group not covered
				// by the integration so far, the integrating group gets a
				// new functional consisting of that functional prepended with
				// the lexical end (now functional begin) in question.
				// This is possible because per condition 1 above all lexicals
				// in the integrating group are starts of ones of the integrated
				// group. Thus no invalid claims about combinations can be made.
				for (String lexicalEnd : validLexicalEnds) {
					for (Functional f : integrated.functionals) {
						List<String> parts = new LinkedList<>(f.parts);
						parts.add(0, lexicalEnd);
						Functional test = new Functional(parts);
						if (!integrating.functionals.contains(test)) {
							integrating.functionals.add(test);
						}
					}
					// now the lexical may be removed from the integrated group
					// as all of it's information is contained in the
					// integrating group.
					integrated.lexicals.remove(lexicalBegin + lexicalEnd);
					// TODO: Has this to happen here? We should not have to
					// call this at all.
					integrated.makeCombinations();
				}
			}
		}
	}

}
