package modules.transitionNetwork.List;

import java.util.ArrayList;
import java.util.Comparator;

import modules.transitionNetwork.elements.AbstractElement;

public class TNArrayList<E> extends ArrayList{
	
	public int find (AbstractElement e,Comparator comparator){
		for (int i=0;i<this.size();i++){
			if (comparator.compare(this.get(i),e) == 0) return i;
		}
		return -1;
	}

}
