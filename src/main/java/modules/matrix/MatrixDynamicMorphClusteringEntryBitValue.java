package modules.matrix;

import java.util.BitSet;

class MatrixDynamicMorphClusteringEntryBitValue {
	int rowIndex;
	int value;// number bits set in context
	BitSet bitSet;//bitSet as conjunction of disjunctions; disjunction as result
	// of ANDing selected element and local element(s); comjunction
	// of all local (stack) elements
	int exclude;
	
	MatrixDynamicMorphClusteringEntryBitValue(int row,int val,BitSet bitSet){
		this.rowIndex=row;
		this.value=val;
		this.bitSet=bitSet;
		this.exclude=-1;
	}
	
}
