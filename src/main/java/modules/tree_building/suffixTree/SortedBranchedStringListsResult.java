package modules.tree_building.suffixTree;

import java.util.ArrayList;

public class SortedBranchedStringListsResult {
	
	// the method generateSortedBranchedStringList() of class xxx
	// produces one or two result lists of type ArrayList<BranchedStringBufferElement>; 
	// SortedBranchedStringListsResult is a helper class to return one or two
	// result lists to calling process method in GeneralizedSuffixTreesMorphologyModule
	
	public ArrayList<BranchedStringBufferElement>firstBranchedStringBufferElementList, 
	secondBranchedStringBufferElementList; 
	
	//ctr
	public SortedBranchedStringListsResult(){
		firstBranchedStringBufferElementList=null;
		secondBranchedStringBufferElementList=null;
	}

}
