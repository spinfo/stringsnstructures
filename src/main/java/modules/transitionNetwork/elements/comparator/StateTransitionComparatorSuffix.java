package modules.transitionNetwork.elements.comparator;

import java.util.Comparator;

import modules.transitionNetwork.elements.StateTransitionElement;

public class StateTransitionComparatorSuffix implements Comparator<StateTransitionElement>{
	@Override public int compare(StateTransitionElement e1,StateTransitionElement e2){
		if (e1.toSuffixElement ==e2.toSuffixElement) return 0;
		return -1;
	}

}
