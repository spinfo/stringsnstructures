package modules.tree_building.suffixTree;

import java.util.Comparator;

public class BranchedStringElementComparator  implements Comparator<BranchedStringElement>{

	@Override 
	public int compare(BranchedStringElement be1, BranchedStringElement be2){
		return be1.stringBuffer.toString().compareTo(be2.stringBuffer.toString());
	}
}
