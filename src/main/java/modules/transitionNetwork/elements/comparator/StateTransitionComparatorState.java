package modules.transitionNetwork.elements.comparator;

import java.util.Comparator;

import modules.transitionNetwork.elements.StateTransitionElement;

public class StateTransitionComparatorState implements Comparator<StateTransitionElement> {
	@Override
	public int compare(StateTransitionElement e1, StateTransitionElement e2) {
		if (e1.toStateElement == e2.toStateElement)
			return 0;
		return -1;
	}

}
