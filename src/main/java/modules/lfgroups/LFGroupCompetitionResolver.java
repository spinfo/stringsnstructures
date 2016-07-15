package modules.lfgroups;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

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
	// TODO: Should not be in this class itself.
	static void resolveCompetition(LFGroup integrating, LFGroup integrated) {
		// condition 1: All of the integrating group's lexicals must be the
		// starts of some lexicals of the integrated group
		Map<String, Set<String>> matchingLexicalsWithEnds = new TreeMap<>();
		for (String lexical : integrating.lexicals) {
			Set<String> matchingLexicalEnds = integrated.getLexicalEndsBeginningWith(lexical);
			if (!matchingLexicalEnds.isEmpty()) {
				matchingLexicalsWithEnds.put(lexical, matchingLexicalEnds);
			}
		}
		if (matchingLexicalsWithEnds.keySet().isEmpty()
				|| !matchingLexicalsWithEnds.keySet().equals(integrating.lexicals)) {
			// TODO: Should not happen. Delete messages after testing?
			if (!matchingLexicalsWithEnds.isEmpty()) {
				// TODO: Turn to warning again?
				LOGGER.warning("Matching lexical begins without possibility to integrate. Some info:");
				LOGGER.warning("Lexicals: " + matchingLexicalsWithEnds);
				LOGGER.warning("Should equal integrating: " + integrating.lexicals);

				// throw new IllegalStateException("Matching lexicalal begins
				// without possibility to integrate.");
			}
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

		Set<String> lexicalBegins = matchingLexicalsWithEnds.keySet();
		System.out.println("Integrating by: " + matchingLexicalsWithEnds + " with: " + matchingFunctionals);
		for (String lexicalBegin : lexicalBegins) {
			Set<String> lexicalEnds = matchingLexicalsWithEnds.get(lexicalBegin);
			// for each lexical begin we count the number of
			// replacements made
			int splitsMade = 0;
			for (String lexicalEnd : lexicalEnds) {
				for (Functional f : matchingFunctionals.keySet()) {
					// splits from the corresponding functional in the
					// integrated group must be added, so search it
					if (f.get().startsWith(lexicalEnd)) {
						try {
							// TODO: This is ugly and will fail if there is no
							// such element, there should always be one though.
							// This could use a check for multiple matches.
							// There should or even can be none
							Functional other = matchingFunctionals.get(f).stream()
									.filter(o -> o.get().equals(f.get().substring(lexicalEnd.length()))).findFirst()
									.get();
							f.splitOn(other);
							splitsMade += 1;
						} catch (NoSuchElementException e) {
							LOGGER.warning("No matching functional: '" + f.get().substring(lexicalEnd.length()) + "'");
						}
					}
				}
			}
			// if enough splits were made to cover all functionals from
			// the integrated group, the corresponding lexical for that
			// group may be deleted
			if (splitsMade == integrated.functionals.size()) {
				for (String lexicalEnd : lexicalEnds) {
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
				for (String lexicalEnd : lexicalEnds) {
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
