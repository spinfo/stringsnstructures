package modules.transitionNetwork;

import java.util.TreeMap;

import modules.transitionNetwork.elements.StateElement;
import modules.transitionNetwork.elements.StateTransitionElement;

public class StateElementDistanceCalculator {

	private TransitionNetwork tn;

	// Helper variables for recursive counting of matching states in distance
	// comparison
	private int matchCount = 0;
	private int transitionCount = 0;

	// Helper variable for recursive counting of state child count
	private int childCount = 0;

	public StateElementDistanceCalculator(TransitionNetwork tn) {
		this.tn = tn;
	}

	public double stateElementDistance(StateElement one, StateElement two) {
		if (one.equals(two)) {
			return 0.0;
		}

		matchCount = 0;
		transitionCount = 0;
		computeStateElementDistanceVars(one, two);

		Double result = 1 - (matchCount / (double) transitionCount);

		return result;
	}

	private void computeStateElementDistanceVars(StateElement one, StateElement two) {
		// TODO: If the nodes are matching via another metric, just add their
		// child node count as matches

		TreeMap<String, StateTransitionElement> labelsToTransitions = new TreeMap<>();
		String label;
		StateTransitionElement transitionFound;

		for (StateTransitionElement transition : one.toStateTransitions) {
			label = tn.getSuffixLabel(transition.toSuffixElement);
			labelsToTransitions.put(label, transition);

			transitionCount += 1;
			transitionCount += getChildStateAmount(tn.getState(transition.toStateElement));
		}

		for (StateTransitionElement transition : two.toStateTransitions) {
			label = tn.getSuffixLabel(transition.toSuffixElement);
			transitionFound = labelsToTransitions.get(label);

			transitionCount += 1;
			// If a match is found, note the match and recurse
			if (transitionFound != null) {
				matchCount += 2;
				computeStateElementDistanceVars(tn.getState(transition.toStateElement),
						tn.getState(transitionFound.toStateElement));
			} else {
				transitionCount += getChildStateAmount(tn.getState(transition.toStateElement));
			}
		}

	}

	public int getChildStateAmount(StateElement state) {
		this.childCount = 0;
		computeChildCount(state);
		return this.childCount;
	}

	private void computeChildCount(StateElement state) {
		this.childCount += state.toStateTransitions.size();

		for (StateTransitionElement transition : state.toStateTransitions) {
			computeChildCount(tn.getState(transition.toStateElement));
		}
	}

}
