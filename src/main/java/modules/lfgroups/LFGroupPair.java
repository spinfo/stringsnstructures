package modules.lfgroups;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * A pair of lexical-functional groups able to detect whether both groups are in
 * competition to each other.
 */
class LFGroupPair {

	final LFGroup one;
	final LFGroup two;

	final Set<String> competing;

	public LFGroupPair(LFGroup one, LFGroup two) {
		this.one = one;
		this.two = two;
		this.competing = Sets.intersection(one.combinations, two.combinations);
	}

	public double getRank() {
		return one.medLexLength() + two.medLexLength();
	}

	public boolean isCompeting() {
		return !competing.isEmpty();
	}

	public void resolveCompetition() {
		LFGroupCompetitionResolver.resolveCompetition(one, two);
		LFGroupCompetitionResolver.resolveCompetition(two, one);
	}
}
