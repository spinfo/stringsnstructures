package modules.transitionNetwork.List;

import java.util.ArrayList;
import java.util.Comparator;

// Class of E is an AbstractElement in most cases
public class TNArrayList<E> extends ArrayList<E> {

	private static final long serialVersionUID = -2117613876524895627L;

	public int find(E e, Comparator<E> comparator) {
		for (int i = 0; i < this.size(); i++) {
			if (comparator.compare(this.get(i), e) == 0)
				return i;
		}
		return -1;
	}

}
