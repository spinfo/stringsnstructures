package modules.matrix;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import models.NamedFieldMatrix;
import common.logicBits.LogOp;


public class MatrixDynamicMorphClustering {
	
	/*
	 * (there are three main data structures:
	 * 1. a dynamic (i.e. extensible) property matrix; the properties are
	 * vectors representing adjacency.
	 * The matrix must be extensible as new entries may be entered after
	 * comparison based on bitwise operations for bit(set)s not included
	 * in comparison.
	 * 2. a dynamic distance matrix which represents results of above
	 * vector comparison
	 * 3. an agglomerative (bottom up builded) neighbor hood tree
	 * )
	 * 
1. sortiere fallend nach Zahl Kontextelemente
2. Schleife: seligiere top down element a
3. Schleife bottom up:
	suche zwei Elemente b und c, deren Summe Kontextelemente >=
	Kontextelemente top-down

4: Evaluiere bitMatch:
	evaluiere results: (a AND b) OR(a AND c)  == a
	Evaluationskriterium: Ausgewogenheit, d.h. bestes b und c, dann wenn Differenz (Zahl gesetzte bits) |b| und |c|  möglichst nahe 0


5. Füge gesplittete Elemente in Liste ein
6. Füge ein auch in fallend sortierte Liste
7. Iteriere Verfahren (auch) für neueingefügte Elemente (das Vorgehen ist binär aufteilend).

	 * 
	 */

	// The value zero as a double
	private static final Double ZERO_D = new Double(0.0);
	
	private static Stack<MatrixDynamicMorphClusteringEntryBitValue> best,actual;
	
	private BitSet generateBitVector(NamedFieldMatrix namedFieldMatrix,int row) {
		double[] values;

		values = namedFieldMatrix.getRow(row);
		
		// create the BitSet from the matrix' values
		BitSet result = new BitSet(values.length);
		for (int i = 0; i < values.length; i++) {
			if (!ZERO_D.equals(values[i])) {
				result.set(i);
				}
			}//
		return result;
		}//generateBitVector
	
	
	ArrayList<MatrixDynamicMorphClusteringEntryBitValue> generateSorted(NamedFieldMatrix namedFieldMatrix)
		{
		ArrayList<MatrixDynamicMorphClusteringEntryBitValue> contextBitSetList=
				new ArrayList<MatrixDynamicMorphClusteringEntryBitValue>();
		
		for (int i=0;i<namedFieldMatrix.getRowAmount();i++){
			
			
			BitSet bitSet = this.generateBitVector(namedFieldMatrix,i);
			//TODO save list of bits
			
			contextBitSetList.add(new MatrixDynamicMorphClusteringEntryBitValue(
					i,bitSet.cardinality(),bitSet));
		}
		
		// sort
		Collections.sort(contextBitSetList, 
				new Comparator <MatrixDynamicMorphClusteringEntryBitValue>(){
				 @Override
				    public int compare(MatrixDynamicMorphClusteringEntryBitValue a,
				    		MatrixDynamicMorphClusteringEntryBitValue b) {
					 		if (a.value > b.value)return 1;
					 		else if (a.value == b.value) return 0;
					 		else return-1;				        
				    }// compare
		});//sort
		
		// print for test
		for (int i=0;i<contextBitSetList.size();i++){
			System.out.println(contextBitSetList.get(i).rowIndex+ "  "+
					contextBitSetList.get(i).value);
		}

		return contextBitSetList;
	}
	
	// contextBitSetList contains entries which belong to differenung
	// morphological classes, e.g. regiere, regierst, regierbar, regierung
	// or porto, porta, porti, portiamo
	// these entries match with contexts of other entries which are clearly verbal,
	// adjectival or nominal
	// generateSpecificContextEntries proposes different forms of polyfunctional entries
	// 
	private void generateSpecificContextEntries
	(ArrayList<MatrixDynamicMorphClusteringEntryBitValue> contextBitSetList){
		
		// contextBitSetList is sorted after descending number of contexts
		// it is checked whether there are different elements with context contained
		// in selected element
		for (int i=0;i<contextBitSetList.size();i++){
			MatrixDynamicMorphClusteringEntryBitValue selectedElement=
					contextBitSetList.get(i);
					check(contextBitSetList,selectedElement,i,0);
			
		}
		// enter in contextBitSetList
		
	}
	
	private void check(ArrayList<MatrixDynamicMorphClusteringEntryBitValue> contextBitSetList,
			MatrixDynamicMorphClusteringEntryBitValue selectedElement,
			int i/*value of outer calling loop s.above*/,int depth){
		
		//BitSet selectedElementBitSet=getBitSet(selectedElement);
		for (int j=contextBitSetList.size();j>i;j--){
			MatrixDynamicMorphClusteringEntryBitValue localElement=
					contextBitSetList.get(i);	
			
			switch(bitVectorCompare(selectedElement,localElement))
			{
				case -1: 
				   // not contained,continue
				   break; 
				case 0: 
				   // contained, but not completely contained, recursion
				   check(contextBitSetList,selectedElement,i,depth+1);
				   break; 
				case 1: 
					// completely contained, check recursion depth
					// compare with result already found
				    return; 
				default:
				// nothing, error
				  ;
			}
			
		}
		
	}
	
	
	
	private int bitVectorCompare(MatrixDynamicMorphClusteringEntryBitValue selectedElement,
			MatrixDynamicMorphClusteringEntryBitValue localElement) {
		
		
		// not contained
		BitSet res=LogOp.AND(selectedElement.bitSet,localElement.bitSet);
		if (res.isEmpty()) return -1;
		else if (!isDisjunct(localElement.bitSet)) return -1;
		else if (isCompletelyContained(localElement.bitSet)){
			checkNewBetter();
			return 1;
		}
			else return 0; // continue with recursion		
	}
	
	private boolean isDisjunct(BitSet localBitSet){
		for (int i=0;i<actual.size();i++){
			
			BitSet res= LogOp.XOR(localBitSet,actual.get(i).bitSet);
			if(res.cardinality()<2)return false;
		}
		return true;
	}
	
	private boolean isCompletelyContained(BitSet selectedElementBitSet){
		
		BitSet res=new BitSet();
		for (int i=0;i<actual.size();i++){			
			res= LogOp.OR(res,actual.get(i).bitSet);			
		}
		if (LogOp.XOR(res, selectedElementBitSet).cardinality()==0)return true;		
		else return false;
	}
	
	private void checkNewBetter(){
		if (best==null) best= actual;
		else {
			// better more elements than less
			if (actual.size()>best.size()) best=actual;
			else if (actual.size()==best.size()){
				if (harmony(actual)<harmony(best))best=actual;
			}
		}
	}
		
	private double harmony(Stack<MatrixDynamicMorphClusteringEntryBitValue>stack){
		int sum=0; double medium;double variation=0;
		for (int i=0;i<stack.size();i++){
			sum=sum+stack.get(i).value;
		}
		medium=sum/stack.size();
		for (int i=0;i<stack.size();i++){
			variation=variation + (Math.abs(stack.get(i).value-medium));
		}
		return variation;
	}
	
	private void extendNamedFieldMatrix(NamedFieldMatrix namedFieldMatrix, 
			Stack<MatrixDynamicMorphClusteringEntryBitValue>stack,
			MatrixDynamicMorphClusteringEntryBitValue selectedElement){
		
		// copy values from namedFieldmatrix from elment.i to new row(s)
		// number of new rows in namedFieldMatrix
		String rowName= namedFieldMatrix.getRowName(selectedElement.rowIndex);
		for (int i=0;i<stack.size();i++){
			//columns
			// generate new row to add to namedFieldmatrix
			for (int j=0;j<stack.get(i).bitSet.size();j++){
				// bit is set, get value
				if (stack.get(i).bitSet.get(j)){
					double value= namedFieldMatrix.getValue(selectedElement.rowIndex,j);
					//????löschen row ???
					String columnName=
					namedFieldMatrix.getColumnName(j);
					namedFieldMatrix.addValue(rowName+"-"+String.valueOf(i), columnName, value);
				}
			}
			
		}		
		
	}
	
	
	// TO DO stacks to be constructed (actual, best)
	// TO DO define central method restruct from which all is called here internally
	// TO DO documentation
	// TO check
	// 		after: distance matrix
	// 		clustering
	
	public NamedFieldMatrix restruct(NamedFieldMatrix namedFieldMatrix){
		
		// generateSorted
		
		// generateSpecificContextEntries
		
		// extendNamedFieldMatrix
		return null;
	}
	
}
