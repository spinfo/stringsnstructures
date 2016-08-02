package modules.lfgroups;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * A group of lexicals with functionals together representing a set of
 * combinations of themselves.
 */
class LFGroup {

	Set<String> lexicals = new TreeSet<>();
	TreeSet<Functional> functionals = new TreeSet<>();

	Set<String> combinations = new TreeSet<>();

	Set<LFGroup> functionalSubgroups = new HashSet<>();

	private static final Set<String> EMPTY_STRING_SET = new TreeSet<String>(Arrays.asList(""));

	void makeCombinations() {
		combinations.clear();

		if (lexicals.size() == 0 || functionals.size() == 0) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		for (String pre : lexicals) {
			for (Functional post : functionals) {
				sb.append(pre);
				sb.append(post.get());
				combinations.add(sb.toString());
				sb.setLength(0);
			}
		}
	}

	double medLexLength() {
		if (lexicals.size() == 0) {
			return 0;
		}
		return lexicals.stream().mapToInt(l -> l.length()).sum() / (double) lexicals.size();
	}

	Set<String> getLexicalEndsBeginningWith(String begin) {
		Set<String> result = new TreeSet<>();
		for (String lex : lexicals) {
			if (lex.startsWith(begin)) {
				result.add(lex.substring(begin.length()));
			}
		}
		return result;
	}

	Set<Functional> getFunctionalsEndingIn(String end) {
		Set<Functional> result = new TreeSet<>();
		for (Functional functional : functionals) {
			if (functional.get().endsWith(end)) {
				result.add(functional);
			}
		}
		return result;
	}

	boolean hasLexicalChars() {
		return !(this.lexicals.isEmpty() || EMPTY_STRING_SET.equals(lexicals));
	}

	String prettyPrint() {
		StringBuilder sb = new StringBuilder();

		sb.append(" (" + String.join(", ", lexicals) + ") -> (");
		for (Functional f : functionals) {
			sb.append(f.represent());
			sb.append(", ");
		}
		sb.setLength(sb.length() - 2);
		sb.append(')');
		return sb.toString();
	}
}
