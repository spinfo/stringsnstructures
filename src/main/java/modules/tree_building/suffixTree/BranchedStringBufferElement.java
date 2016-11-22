package modules.tree_building.suffixTree;

import java.util.BitSet;

/**
 * @author JR
 * @version 1.0
 */

public class BranchedStringBufferElement extends StringBufferElement{

	
	BitSet bitSet;
	public BranchedStringBufferElement(StringBuffer sb, BitSet bitSet){
		this.stringBuffer= new StringBuffer(sb);
		this.bitSet=bitSet;
		
	}
	
	
	
}
