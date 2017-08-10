package modules.matrix;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Map;
//import java.util.Set;
//import java.util.Stack;
import java.util.HashMap;

import models.NamedFieldMatrix;
import common.logicBits.LogOp;


public class MatrixDynamicMorphClustering {
	
	/* the basic idea of this class is: 
	 * 	1. sort elements by (falling) number of contexts
	 *  2. look whether context (element set) consists of (more than one) other context element sets
	 *  (example: port+us, port+um, port+os, port+o, port+as has context element set consiting of
	 *  us, um, os, o, as.
	 *  us, um, os are found in the context (of a nominal form like) hort+us, hort+um, hort+os (and hort+o).
	 *  as
	 *  o and as are found in (verbal forms like) am+o, am+as (nota bene is found in both of them).
	 *  Thus, port+ ... should be splitted in two forms.
	 *  But note, these two forms might be splitted further on.
	 *  
	 * (there are three main data structures:
	 * 1. a dynamic (i.e. extensible) property matrix; the properties are
	 * vectors representing adjacency.
	 * The matrix must be extensible as new entries may be entered after
	 * comparison based on bitwise operations for bit(set)s not included
	 * in comparison.
	 * 2. a dynamic distance matrix which represents results of above
	 * vector comparison
	 * 3. an agglomerative (bottom up built) neighbor hood tree
	 * )
	 * 
	 */

	private PrintWriter writer;
	private NamedFieldMatrix namedFieldMatrix;
	private HashMap<String, Integer> competitionHashMap=null;
	//private ArrayList<MatrixDynamicMorphClusteringEntryBitValue> cardinorderedEntryContextList;
	// The value zero as a double
	private static final Double ZERO_D = new Double(0.0);
	
	//private static Stack<MatrixDynamicMorphClusteringEntryBitValue> best,actual;
	
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
	
	
	
	// generate list from namedFieldmatrix 
	// elements of list are (terminal) nodes of neighbor tree which is built below 
	private ArrayList<MatrixBitWiseOperationTreeNodeElement> generateMatrixBitWiseOperationTreeNodeElementList() 
	{	ArrayList<MatrixBitWiseOperationTreeNodeElement> list= 
		new ArrayList<MatrixBitWiseOperationTreeNodeElement>();
	
	this.writer.println("MatrixDynamicMorphClustering generateMatrixBitWiseOperationTreeNodeElementList Entry");
	
	for (int row=0;row<namedFieldMatrix.getRowAmount();row++){		
		writer.println("generate...TreeNodeElementList row: "+row + " name: "+
				namedFieldMatrix.getRowName(row));
		BitSet bitSet = this.generateBitVector(row);
		//save list of bits		
		list.add(new MatrixBitWiseOperationTreeNodeElement(bitSet,row,null,null//no children here
				));
		}
		return list;
	}
	
	

	private void checkConflictingElements(ArrayList<MatrixBitWiseOperationTreeNodeElement>list,int listSize, 
	MatrixBitWiseOperationCompetition competition,MatrixBitWiseOperationTreeNodeElement partialroot1,
	MatrixBitWiseOperationTreeNodeElement partialroot2){
		
		for (int i=0;i<listSize;i++){
			MatrixBitWiseOperationTreeNodeElement element_i=list.get(i);
			if(element_i.root==partialroot1){
				//System.out.println("partailroot1 found");
				for (int j=0;j<listSize;j++){
					MatrixBitWiseOperationTreeNodeElement element_j=list.get(j);
					if(element_j.root==partialroot2){
						//System.out.println("partailroot2 found");
						try {
							if(competition.checkcompetition(
								this.namedFieldMatrix.getRowName(element_i.fromNamedFieldMatrixRow),
								this.namedFieldMatrix.getRowName(element_j.fromNamedFieldMatrixRow),
								this.writer)) {
								
								//int x=10/0;
							
							}
						}
							catch(Exception e){System.out.println(" error checkConflictingElements" );};
					}
				}
			}
			
		}
		
		
	}//checkConflictingElements
	
	
	private int evaluate(MatrixBitWiseOperationTreeNodeElement in1,MatrixBitWiseOperationTreeNodeElement in2){
		BitSet common=LogOp.AND(in1.contextBitSet, in2.contextBitSet);
		
		// 17-07-31 inclusion is better here
		BitSet dif1=LogOp.XOR(common,in1.contextBitSet);
		BitSet dif2=LogOp.XOR(common,in2.contextBitSet);
		int minor=Integer.min(dif1.cardinality(), dif2.cardinality());
		//BitSet dif = LogOp.XOR(in1.contextBitSet, in2.contextBitSet);
		// result is difference of sun of common and sum of different bits
		
		// inclusion welcome
		// return common.cardinality()-dif.cardinality();
		return common.cardinality()-minor;
	}
	
	
	private boolean searchBestPairForTree_1(ArrayList<MatrixBitWiseOperationTreeNodeElement> list, 
			int listSize,MatrixBitWiseOperationCompetition competition){
			int bestVal=Integer.MAX_VALUE*-1;int best_i=0,best_j=0;
			
			for (int i=0;i<listSize-1;i++){
				MatrixBitWiseOperationTreeNodeElement element_i=list.get(i);
				// 
				for (int j=i+1;j<listSize;j++){
					MatrixBitWiseOperationTreeNodeElement element_j=list.get(j);
					// different roots 
					if (element_i.root != element_j.root) {
						int val= evaluate(element_i,element_j);
						if (val> bestVal){							
							bestVal=val;best_i=i;best_j=j;
							
				
								
						}
						
					}
				}
				
			}
			// generate new mother element which is added to list, ORING , mother, children
			if (bestVal>Integer.MAX_VALUE*-1){
				MatrixBitWiseOperationTreeNodeElement bestChild1=list.get(best_i);
				MatrixBitWiseOperationTreeNodeElement bestChild2=list.get(best_j);
				MatrixBitWiseOperationTreeNodeElement partialroot1=bestChild1.root;
				MatrixBitWiseOperationTreeNodeElement partialroot2=bestChild2.root;
				int y=0;
				if ((partialroot1==null)||(partialroot2==null))  y=10/0;
				MatrixBitWiseOperationTreeNodeElement mother= 
						new MatrixBitWiseOperationTreeNodeElement(LogOp.OR(bestChild1.contextBitSet, 
							bestChild2.contextBitSet),0, // row TODO
							partialroot1,partialroot2);
							partialroot1.mother=mother;
							partialroot2.mother=mother;
							// difference in mother, may be used for defining cut for class building TODO
				
							
							
				checkConflictingElements(list,listSize,competition,partialroot1,partialroot2);			
				// reset root in partial tree with root mother	
				mother.walk(mother,mother);		
				list.add(mother);//??
				
				
				// 
				this.writer.println("searchBestPairForTree_1 bestChild1: "+
						this.namedFieldMatrix.getRowName(bestChild1.fromNamedFieldMatrixRow)+
						" bestChild2: "+
							this.namedFieldMatrix.getRowName(bestChild2.fromNamedFieldMatrixRow));
				this.writer.println("val: "+ bestVal+ " i: "+best_i+ " j: "+best_j);
				
				return true;
			}
			else return false;
		}// searchBestPairForTree_1
	
		
	private boolean searchBestPairForTree_0(ArrayList<MatrixBitWiseOperationTreeNodeElement> list, 
		int listSize){
		int bestVal=Integer.MAX_VALUE*-1;int best_i=0,best_j=0;
		
		for (int i=0;i<listSize-1;i++){
			MatrixBitWiseOperationTreeNodeElement element_i=list.get(i);
			// not already in tree
			if (element_i.mother==null) {
				for (int j=i+1;j<listSize;j++){
					MatrixBitWiseOperationTreeNodeElement element_j=list.get(j);
					if (element_j.mother==null) {
						int val= evaluate(element_i,element_j);
						if (val> bestVal){							
							bestVal=val;best_i=i;best_j=j;
							
							// note difference TODO
							
						}
						
					}
				}
			}
		}
		// generate new mother element which is added to list, ORING , mother, children
		if (bestVal>Integer.MAX_VALUE*-1){
			MatrixBitWiseOperationTreeNodeElement bestChild1=list.get(best_i);
			MatrixBitWiseOperationTreeNodeElement bestChild2=list.get(best_j);
			MatrixBitWiseOperationTreeNodeElement mother= 
					new MatrixBitWiseOperationTreeNodeElement(LogOp.OR(bestChild1.contextBitSet, 
						bestChild2.contextBitSet),0,bestChild1,bestChild2);
						bestChild1.mother=mother;
						bestChild2.mother=mother;
						// difference in mother, may be used for defining cut for class building TODO
			list.add(mother);
			this.writer.println("searchBestPairForTree_0 bestChild1: "+
					this.namedFieldMatrix.getRowName(bestChild1.fromNamedFieldMatrixRow)+
					" bestChild2: "+
						this.namedFieldMatrix.getRowName(bestChild2.fromNamedFieldMatrixRow));
			this.writer.println("val: "+ bestVal+ " i: "+best_i+ " j: "+best_j);
			
			return true;
		}
		else return false;
	}// searchBestPairForTree_0
	
	MatrixBitWiseOperationTreeNodeElement generateWeightedBinaryNeighborhoodTree
	(ArrayList<MatrixBitWiseOperationTreeNodeElement> list){
		this.writer.println(" generateWeightedBinaryNeighborhoodTree Entry");
		
		int listSize=list.size();
		
		while(true) {
			boolean active=true;
			while(active) {
				active=searchBestPairForTree_0(list,listSize);
			
			}
			if(listSize==list.size()) break;
			else listSize=list.size();
		}
		// root of tree is last element added in list
		this.writer.println(" generateWeightedBinaryNeighborhoodTree Exit");
		return list.get(list.size()-1);//root, i.e last of list		
	}//generateWeightedBinaryNeighborhoodTree
	
	
	MatrixBitWiseOperationTreeNodeElement generateBinaryNeighborhoodTree
	(ArrayList<MatrixBitWiseOperationTreeNodeElement> list,
		MatrixBitWiseOperationCompetition competition){
		this.writer.println(" generateBinaryNeighborhoodTree Entry");
		int listSize=list.size();
		boolean active=true;
		while(active) {
				active=searchBestPairForTree_1(list,listSize,competition);			
		}
		// root of tree is last element added in list
		this.writer.println(" generateBinaryNeighborhoodTree Exit");
		return list.get(list.size()-1);//root, i.e last of list		
	}//generateBinaryNeighborhoodTree
	
	
	
	public NamedFieldMatrix restruct(NamedFieldMatrix nFieldMatrix,
		MatrixBitWiseOperationCompetition competition,PrintWriter pwriter){
		this.namedFieldMatrix=nFieldMatrix;
		//this.competitionHashMap=compHashMap;
		this.writer=pwriter;
		//this.actual=new Stack<MatrixDynamicMorphClusteringEntryBitValue>();
		//this.best=new Stack<MatrixDynamicMorphClusteringEntryBitValue>();
		
		writer.println("\n\n----------MatrixDynamicMorphClustering restruct Entry");
		// generates a list of context bits set in falling order
		
		//this.cardinorderedEntryContextList=this.generateLocalcardinorderedEntryContextList();
		
		/******************
		ArrayList <MatrixDynamicMorphClusteringEntryBitValue> contextIntersectionList=
		this.generateSortedContextIntersectionList();
		
		int entryIndex=0;// check only
		MatrixDynamicMorphClusteringEntryBitValue entryElement=
		cardinorderedEntryContextList.get(entryIndex);
		this.complementary(contextIntersectionList,entryIndex,entryElement.bitSet,false);
			
		//this.generateSpecificContextEntries();
		
		// extendNamedFieldMatrix
		********/
		 ArrayList<MatrixBitWiseOperationTreeNodeElement> treeNodeList=
		generateMatrixBitWiseOperationTreeNodeElementList(); 
		//MatrixBitWiseOperationTreeNodeElement root=
		//this.generateWeightedBinaryNeighborhoodTree(treeNodeList,competition);
		 
		// alternative----------------
		MatrixBitWiseOperationTreeNodeElement root1=
				this.generateBinaryNeighborhoodTree(treeNodeList,competition);
		writer.println("MatrixDynamicMorphClustering restruct Exit");
		// end alternative--------
		return this.namedFieldMatrix;
	}
	
}
