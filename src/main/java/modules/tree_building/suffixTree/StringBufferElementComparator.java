package modules.tree_building.suffixTree;

import java.util.Comparator;

public class StringBufferElementComparator  implements Comparator<StringBufferElement>{

	@Override 
	public int compare(StringBufferElement be1, StringBufferElement be2){
		return be1.stringBuffer.toString().compareTo(be2.stringBuffer.toString());
	}
}
