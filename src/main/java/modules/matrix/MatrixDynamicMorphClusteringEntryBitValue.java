package modules.matrix;

import java.util.BitSet;

class MatrixDynamicMorphClusteringEntryBitValue {
	int rowIndex;
	int value;// number bits set in context
	BitSet bitSet;
	
	MatrixDynamicMorphClusteringEntryBitValue(int row,int val,BitSet bitSet){
		this.rowIndex=row;
		this.value=val;
		this.bitSet=bitSet;
	}
	
}
