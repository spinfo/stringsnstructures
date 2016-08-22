package modules.transitionNetwork.elements.comparator;

import java.util.Comparator;

import modules.transitionNetwork.elements.StateElement;

public class StateComparator implements Comparator<StateElement> {
	@Override
	public int compare(StateElement e1, StateElement e2) {
		if (e1.state == e2.state)
			return 0;
		else
			return -1;
	}

}
