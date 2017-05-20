package modules.matrix;

import java.io.PrintWriter;
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

	private PrintWriter writer;
	private NamedFieldMatrix namedFieldMatrix;
	private ArrayList<MatrixDynamicMorphClusteringEntryBitValue> contextBitSetList;
	// The value zero as a double
	private static final Double ZERO_D = new Double(0.0);
	
	private static Stack<MatrixDynamicMorphClusteringEntryBitValue> best,actual;
	
	private BitSet generateBitVector(int row) {
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
	
	// generates a list of context bits set in falling order
	private ArrayList<MatrixDynamicMorphClusteringEntryBitValue> generateSortedBitsMarked()
		{
		ArrayList<MatrixDynamicMorphClusteringEntryBitValue> list=
				new ArrayList<MatrixDynamicMorphClusteringEntryBitValue>();
		System.out.println("MatrixDynamicMorphClustering generateSortedBitsMarked Entry");
		for (int i=0;i<namedFieldMatrix.getRowAmount();i++){
			
			
			BitSet bitSet = this.generateBitVector(i);
			//TODO save list of bits
			
			list.add(new MatrixDynamicMorphClusteringEntryBitValue(
					i,bitSet.cardinality(),bitSet));
		}
		
		// sort
		Collections.sort(list, 
				new Comparator <MatrixDynamicMorphClusteringEntryBitValue>(){
				 @Override
				    public int compare(MatrixDynamicMorphClusteringEntryBitValue a,
				    		MatrixDynamicMorphClusteringEntryBitValue b) {
					 		if (a.value < b.value)return 1;
					 		else if (a.value == b.value) return 0;
					 		else return-1;				        
				    }// compare
		});//sort
		
		// print for test
		System.out.println("MatrixDynamicMorphClustering generateSortedBitsMarked List");
		for (int i=0;i<list.size();i++){
			System.out.println(namedFieldMatrix.getRowName(list.get(i).rowIndex)+
					"  "+list.get(i).rowIndex+ "  "+
					list.get(i).value);
		}
		System.out.println("MatrixDynamicMorphClustering generateSortedBitsMarked Exit");
		return list;
	}
	
	// contextBitSetList contains entries which belong to differenung
	// morphological classes, e.g. regiere, regierst, regierbar, regierung
	// or porto, porta, porti, portiamo
	// these entries match with contexts of other entries which are clearly verbal,
	// adjectival or nominal
	// generateSpecificContextEntries proposes different forms of polyfunctional entries
	// 
	
	private void generateSpecificContextEntries(){
		writer.println("MatrixDynamicMorphClustering generateSpecificContextEntries Entry");	
	
		// contextBitSetList is sorted after descending number of contexts
		// it is checked whether there are different elements with context contained
		// in selected element
		for (int i=0;i<contextBitSetList.size();i++){
			MatrixDynamicMorphClusteringEntryBitValue selectedElement=
			contextBitSetList.get(i);
			System.out.println
			("generateSpecificContextEntries selectedElement "
			+ namedFieldMatrix.getRowName(selectedElement.rowIndex)+
			" cardinality: "+selectedElement.bitSet.cardinality()+ " ForLoopIndex i: "+i);
			writer.println
			("generateSpecificContextEntries selectedElement "
			+ namedFieldMatrix.getRowName(selectedElement.rowIndex)+
			" cardinality: "+selectedElement.bitSet.cardinality()+" ForLoopIndex i: "+i);
			check(selectedElement,i,contextBitSetList.size()-1);
			if (!this.actual.empty()){
				MatrixDynamicMorphClusteringEntryBitValue top= this.actual.peek();
				if (top != null) 
					writer.println("generateSpecificContextEntries stack best val:"+top.bitSet.cardinality());
					else writer.println("generateSpecificContextEntries stack empty");
					this.actual.clear();
			};
			this.actual.clear();
			this.best.clear();
		}
		// enter in contextBitSetList
		writer.println("MatrixDynamicMorphClustering generateSpecificContextEntries Exit");
	}
	
	//recursive traverse of contextBitSetList;	
	private void check(MatrixDynamicMorphClusteringEntryBitValue selectedElement,
			int i/*value of outer calling loop s.above*/,int posInContextBitSetList){
			
		for (int j=posInContextBitSetList;j>i;j--){
				
				/*System.out.println
				("check selectedElement "
					+ namedFieldMatrix.getRowName(selectedElement.rowIndex)+
					" cardinality: "+selectedElement.bitSet.cardinality()+
					" posInContextBitSetList: "+j);
			
				writer.println
				("MatrixDynamicMorphClustering check entry j: "+j);
				*/
				//	BitSet selectedElementBitSet=getBitSet(selectedElement);
				// select element (named localElement) out of contextBitSetList (in reversed order (from size() to j!))
				// which may be contained (bitwise) in selected element.
		
				
			
				MatrixDynamicMorphClusteringEntryBitValue localElement=
					this.contextBitSetList.get(j);
				/*System.out.println
				("MatrixDynamicMorphClustering generateSpecificContextEntries localElement "
						+ namedFieldMatrix.getRowName(localElement.rowIndex)+
						" cardinality: "+localElement.bitSet.cardinality());
				*/
				// no elements which are marked to be without sharing bits
				if (localElement.exclude!=i)
					bitVectorCompare(selectedElement,localElement,i,j);
				//else i= 10/0;
				;			
			
			};
			checkNewBetter();
			
		
	}
	
	
	// -1 not contained
	// 0  contained, but not completely contained, recursion
	// 1 completely contained, ok
	private void bitVectorCompare(MatrixDynamicMorphClusteringEntryBitValue selectedElement,
			MatrixDynamicMorphClusteringEntryBitValue localElement,int i,
			int posInContextBitSetList) {
		
		// writer.println("bitVectorCompare Entry");
		// not contained
		BitSet selectedElementANDLocalElement=LogOp.AND(selectedElement.bitSet,localElement.bitSet);
		if (selectedElementANDLocalElement.isEmpty()) {
			writer.println("bitVectorCompare isEmpty ");
			//
			localElement.exclude=i;
		}
		else if (!isDisjunct(localElement.bitSet)) {
			writer.println("bitVectorCompare not disjunct ");
		}
		else {
			int resVal= isContained(selectedElement,		
			selectedElementANDLocalElement,i,posInContextBitSetList);
			writer.println("bitVectorCompare resVal: "+resVal);
			
			
		}			
	}
	
	
	private boolean isDisjunct(BitSet localBitSet){
		
			if (actual.empty()) {writer.println("isDisjunct empty stack true"); return true;}
			else if (actual.peek().bitSet.cardinality()==
			LogOp.OR(localBitSet,actual.peek().bitSet).cardinality()){
				writer.println("isDisjunct false");return false;
			}
			else {writer.println("isDisjunct true");return true;}
			
		
	}//isDisjunct
	
	
	// is Contained checks whether new element contributes to context
	// -1 if element does not contribute
	// 0 if element contributes but not completely
	// 1 if elements contributes completely
	private int isContained(MatrixDynamicMorphClusteringEntryBitValue selectedElement,
			BitSet selectedElementANDLocalElementBitSet,int i,int posInContextBitSetList){
		
		if (this.actual.empty()){
			if (selectedElement.bitSet.cardinality()==
					selectedElementANDLocalElementBitSet.cardinality())
			{
				writer.println("isContained completely identical");
				return 2;
			}
				
			writer.println("isContained first element continue");
			this.actual.push
			(new MatrixDynamicMorphClusteringEntryBitValue(posInContextBitSetList, 
				selectedElementANDLocalElementBitSet.cardinality(), 
				selectedElementANDLocalElementBitSet));
				check(selectedElement,
					i/*value of outer calling loop s.above*/,posInContextBitSetList-1);
					
				this.actual.pop();
			return 0;
		}
		
		else {
			BitSet last= this.actual.peek().bitSet;
			BitSet res= LogOp.OR(selectedElementANDLocalElementBitSet,last);
			writer.println("last.cardinality: "+last.cardinality()+" res.cardinality: "+
			res.cardinality());
			// more contexts covered?
			if (res.cardinality() > last.cardinality()) 
				// all covered
				if (res.cardinality()==selectedElement.bitSet.cardinality())
					{ writer.println("isContained all covered");
					checkNewBetter();
					return 1;}
				else {writer.println("isContained not all covered continue");
					this.actual.push
					(new MatrixDynamicMorphClusteringEntryBitValue(posInContextBitSetList, 
						selectedElementANDLocalElementBitSet.cardinality(), res));
					writer.println("Recursion depth after push : "+this.actual.size());
					check(selectedElement,
					i/*value of outer calling loop s.above*/,posInContextBitSetList-1);
					writer.println("Recursion depth before pop : "+this.actual.size());
					this.actual.pop();
					return 0;}
			else {writer.println("isContained element does not contribute"); return -1;
			}
		}		
	}//isContained
	
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
		int sum=0; double medium;double variation=100000000;
		for (int i=0;i<stack.size();i++){
			sum=sum+stack.get(i).value;
		}
		if (stack.size()>0)
		{medium=sum/stack.size();
			for (int i=0;i<stack.size();i++){
				variation=variation + (Math.abs(stack.get(i).value-medium));
			}
		}
		return variation;
	}
	
	private void extendNamedFieldMatrix(Stack<MatrixDynamicMorphClusteringEntryBitValue>stack,
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
	
	private ArrayList<MatrixDynamicMorphClusteringEntryBitValue>generateCommonContextList() {
		
		ArrayList <MatrixDynamicMorphClusteringEntryBitValue> commonContexts=
				new ArrayList <MatrixDynamicMorphClusteringEntryBitValue>();
				
		for (int i=0;i<contextBitSetList.size()-1;i++){
			MatrixDynamicMorphClusteringEntryBitValue selectedElement=
			contextBitSetList.get(i);
			writer.println
			("generateCommonContextList selectedElement "
			+ namedFieldMatrix.getRowName(selectedElement.rowIndex)+
			" cardinality: "+selectedElement.bitSet.cardinality()+ " ForLoopIndex i: "+i);
			if (selectedElement.bitSet.cardinality()<=1) break;
			for (int j=i+1;j<contextBitSetList.size();j++){
				//
				MatrixDynamicMorphClusteringEntryBitValue localElement=
				contextBitSetList.get(j);
				if (localElement.bitSet.cardinality()<=1) break;
				BitSet and=LogOp.AND(selectedElement.bitSet,localElement.bitSet);
				int card=and.cardinality();
				if(card>0){
					writer.println(" not empty i: "+j+" card: "+card+ " "+
							namedFieldMatrix.getRowName(localElement.rowIndex));
					commonContexts.add(new MatrixDynamicMorphClusteringEntryBitValue(
					localElement.rowIndex,and.cardinality(),and));
				}
			}
		}
		// sort
		Collections.sort(commonContexts, 
			new Comparator <MatrixDynamicMorphClusteringEntryBitValue>(){
			 @Override
		    public int compare(MatrixDynamicMorphClusteringEntryBitValue a,
		   		MatrixDynamicMorphClusteringEntryBitValue b) {
		 		if (a.value < b.value)return 1;
		 		else if (a.value == b.value) return 0;
		 		else return-1;				        
			    }// compare
			});//sort
		return commonContexts;
	}//generateCommonContextList
	
	
	
	private void maxDisjunctBAK(ArrayList <MatrixDynamicMorphClusteringEntryBitValue> commonContexts){
		
		MatrixDynamicMorphClusteringEntryBitValue element;
		
		// ?? check all possibilities ???
		for (int p=0;p<commonContexts.size()-1;p++) {
			int oldCardinality,newCardinality=0;BitSet oredGlobal=new BitSet();
			writer.println("maxDisjunct p:"+p);
			System.out.println("maxDisjunct p:"+p);
			if (commonContexts.get(p).exclude==-1){
				while (true) {
					int maxDiff=0, maxI=0,maxJ=0;
					oldCardinality=newCardinality;
					BitSet oredLocal=new BitSet();
					// look for best (max) diff pair which covers well bits set
					// remind: each element of pair was ANDed with element in list
					for (int i=p;i<commonContexts.size()-1;i++)
						if (commonContexts.get(i).exclude==-1){
							for (int j=i+1;j<commonContexts.size();j++){
								int val=
								LogOp.OR(commonContexts.get(i).bitSet,commonContexts.get(j).bitSet).cardinality()
								-LogOp.AND(commonContexts.get(i).bitSet,commonContexts.get(j).bitSet).cardinality();
								if(val>maxDiff){
									maxDiff=val;maxI=i;maxJ=j;
									oredLocal=LogOp.OR(commonContexts.get(i).bitSet,commonContexts.get(j).bitSet);
								}
							}
						}
						// flag element
						element=commonContexts.get(maxI);
						element.exclude=1;
						BitSet res=LogOp.OR(oredGlobal,oredLocal);
						newCardinality=res.cardinality();
						if(newCardinality>oldCardinality) {
							oredGlobal=res;
						}
						else break;
						writer.println("maxDisjunct max: "+maxDiff+ " maxI: "+maxI+ " maxJ: "+maxJ);
				}
			} 
		}
		
	}
	
	
private ArrayList<Integer> disjunct(ArrayList <MatrixDynamicMorphClusteringEntryBitValue>commonContexts,
		int start, BitSet origin, boolean fullCardinality){
		
		ArrayList<Integer> resultList=new ArrayList<Integer>();
		MatrixDynamicMorphClusteringEntryBitValue element;
		int oldCardinality,newCardinality=0;BitSet oredGlobal=new BitSet();
			
		while (true) {
			int maxDiff=0, maxI=0,maxJ=0;
			oldCardinality=newCardinality;
			BitSet oredLocal=new BitSet();
			// look for best (max) diff pair which covers well bits set
			// remind: each element of pair was ANDed with element in list
			for (int i=start;i<commonContexts.size()-1;i++)
				if (commonContexts.get(i).exclude==-1){
					for (int j=i+1;j<commonContexts.size();j++){
						int val=
						LogOp.OR(commonContexts.get(i).bitSet,commonContexts.get(j).bitSet).cardinality()
						-LogOp.AND(commonContexts.get(i).bitSet,commonContexts.get(j).bitSet).cardinality();
						if(val>maxDiff){
							maxDiff=val;maxI=i;maxJ=j;
							oredLocal=LogOp.OR(commonContexts.get(i).bitSet,commonContexts.get(j).bitSet);
						}
					}
				}
				// flag element
				element=commonContexts.get(maxI);
				element.exclude=1;
				BitSet res=LogOp.OR(oredGlobal,oredLocal);
				newCardinality=res.cardinality();
				if(newCardinality<=oldCardinality) {
					// recursion; check whether selected elements may be divided
					if((!fullCardinality) || (newCardinality==origin.cardinality())){
						writer.println("before recursion");
						for (int l=0;l<resultList.size();l++){
							ArrayList<Integer> recursionList=
							disjunct(commonContexts,resultList.get(l)+1,
							commonContexts.get(resultList.get(l)).bitSet, true);
							if ((recursionList==null)||(recursionList.isEmpty()))
								writer.println("no list after recursion");
							else writer.println("divided after recursion");
						}	
						writer.println("after recursion");
					}
					
					break;
				}
				oredGlobal=res;
				// two elements only if empty otherwise add maxJ
				if(resultList.isEmpty()) resultList.add(new Integer(maxI));
				resultList.add(new Integer(maxJ));
				writer.println("maxDisjunct max: "+maxDiff+ " maxI: "+maxI+ " maxJ: "+maxJ);
			}
		return resultList;
		} 
		
		
	
	
	
	
	public NamedFieldMatrix restruct(NamedFieldMatrix nFieldMatrix,
			PrintWriter pwriter){
		this.namedFieldMatrix=nFieldMatrix;
		this.writer=pwriter;
		this.actual=new Stack<MatrixDynamicMorphClusteringEntryBitValue>();
		this.best=new Stack<MatrixDynamicMorphClusteringEntryBitValue>();
		
		writer.println("MatrixDynamicMorphClustering restruct Entry");
		// generates a list of context bits set in falling order
		this.contextBitSetList=this.generateSortedBitsMarked();
		ArrayList <MatrixDynamicMorphClusteringEntryBitValue> commonContexts=
		this.generateCommonContextList();
		// check only
		MatrixDynamicMorphClusteringEntryBitValue element=contextBitSetList.get(0);
		this.disjunct(commonContexts,0,element.bitSet,false);
			
		//this.generateSpecificContextEntries();
		
		// extendNamedFieldMatrix
		writer.println("MatrixDynamicMorphClustering restruct Exit");
		
		return this.namedFieldMatrix;
	}
	
}
