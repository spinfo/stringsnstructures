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
//import modules.matrix.morph.ContainingElement;
import modules.matrix.morph.RestructMorphologicalClasses;
import common.logicBits.LogOp;
import modules.matrix.MatrixBitWiseOperationTreeNodeElement;
                      


public class MatrixDynamicMorphClustering {
	
	/* the basic idea of this class is: 
	 * 	1. sort elements by (falling) number of contexts
	 *  2. look whether context (element set) consists of (more than one) other context element sets
	 *  (example: port+us, port+um, port+os, port+o, port+as has context element set consisting of
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
	
	this.writer.println("MatrixDynamicMorphClustering.generateMatrixBitWiseOperationTreeNodeElementList Entry");
	
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
	
	
	// here elements are checked for which different types of segmentation
	// were proposed 
	//
	
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
							String name1=
							this.namedFieldMatrix.getRowName(element_i.fromNamedFieldMatrixRow);
							String name2=
							this.namedFieldMatrix.getRowName(element_j.fromNamedFieldMatrixRow);
							if(competition.checkcompetition(name1, name2, this.writer)) {
								
								// two possible solutions:
								// 1. compare context of element_i and element_j and 
								// select element with more morphemes (? reallay morphemes?)
								// following
								// 2. or:  
								// consider relation of cardinality of OR and AND for partialroot1
								// and partialroot2;
								// select less as better solution
								//
								// what do do after: remind that deletion of worse
								// has consequences all remaining nodes in partialtree for ORs and
								// ANDs								
								//
								// one of proposed solution to be implemented here
								// very pragmatic solution in  pragmaticDeletionOfCompeting(treeNodeList);
								
								// no division by zero
								float div1=partialroot1.and.cardinality();
								if (div1==0)div1= 0.01F;
								float div2=partialroot2.and.cardinality();
								if (div2==0)div2= 0.01F;
								float val1=partialroot1.or.cardinality()/div1;
								float val2=partialroot2.or.cardinality()/div2;
								if (val2>val1){
									
									// mark element for name1 as deselected
									element_i.deselect=true;
									this.writer.println("MatrixDynamicMorphClustering name1 deselected: "+name1+
											" name2: "+name2);
									;
									
								} else {
									// mark element for name2 as deselected
									element_j.deselect=true;
									this.writer.println("MatrixDynamicMorphClustering name2 deselected: "+name1+
											" name2: "+name2);
									//int x=1/0;
								}
									
							
							}
						}
							catch(Exception e){
								System.out.println(" error checkConflictingElements i : "+ i+" j: "+j+
										" e: "+e);
								e.printStackTrace();
								System.exit(9);
										};
					}
				}
			}
			
		}
		
		
	}//checkConflictingElements
	
	
	private int evaluate(MatrixBitWiseOperationTreeNodeElement in1,MatrixBitWiseOperationTreeNodeElement in2){
		BitSet intersection=LogOp.AND(in1.contextBitSet, in2.contextBitSet);
		
		// 17-07-31 inclusion is better here
		BitSet dif1=LogOp.XOR(intersection,in1.contextBitSet);
		BitSet dif2=LogOp.XOR(intersection,in2.contextBitSet);
		//BitSet dif1=LogOp.AND(intersection,in1.contextBitSet);
		//BitSet dif2=LogOp.AND(intersection,in2.contextBitSet);
		int minor=Integer.min(dif1.cardinality(), dif2.cardinality());
		
		// should be reflected
		int val =intersection.cardinality()+intersection.cardinality()/(minor+1);
		
	
		//BitSet dif = LogOp.XOR(in1.contextBitSet, in2.contextBitSet);
		// result is difference of sum of intersection and sum of different bits
		
		// inclusion welcome
		// return intersection.cardinality()-dif.cardinality();
		//System.out.println(intersection.cardinality()+" result val: "+val);
		return val; //intersection.cardinality() - minor;
	}
	
	/* see above checkConflictingElements
	 private void pragmaticDeletionOfCompeting
	 (ArrayList<MatrixBitWiseOperationTreeNodeElement>treenodelist){
		 for (MatrixBitwiseOperationHelpCompetingElementGroup competitionElement:
			 MatrixBitWiseOperationCompetition.competitionList){
			 String bestStr="";int bestVal=-1;
			 for (String prefix:competitionElement.members){
				 
				 // get element prefix in treenodelist
				 
				 // search val in of element (number of morphememes following
				 // 
				 int val=0;//dummy
				 
				 if(val>bestVal){
					 bestVal=val;
					 bestStr=prefix;
				 }
				 
			 }
			 for (String prefix:competitionElement.members){
				// get element prefix in treenodelist
				 
				// mark all elements != bestStr as deleted
			 }
			 
		 }
	 };
	*/
	
	private boolean searchBestPairForNeighborhoodTree_1(ArrayList<MatrixBitWiseOperationTreeNodeElement> list, 
			int listSize,MatrixBitWiseOperationCompetition competition){
			int bestVal=Integer.MAX_VALUE * -1;
			int best_i=0,best_j=0;
			
			// two loops, all elements of list are compared
			for (int i=0;i<listSize-1;i++){
				MatrixBitWiseOperationTreeNodeElement element_i=list.get(i);
				
				// only not containing 
				if (element_i.containingList==null){
				// 
					for (int j=i+1;j<listSize;j++){
						MatrixBitWiseOperationTreeNodeElement element_j=list.get(j);
						//only not containing; different roots; no elements of identical tree 
						if ((element_j.containingList==null) &&(element_i.root != element_j.root)) {
							
							int val= evaluate(element_i,element_j);
							
							if (val> bestVal){							
								bestVal=val;best_i=i;best_j=j;

								
								
							}
						
						}
						
					}//for (int j=i+1;j<listSize;j++)
				}//if (element_i.containingList==null)
				
			}// for (int i=0;i<listSize-1;i++)
			// generate new mother element which is added to list, ORING , mother, children
			if (bestVal>Integer.MAX_VALUE * -1){
				MatrixBitWiseOperationTreeNodeElement bestChild1=list.get(best_i);
				MatrixBitWiseOperationTreeNodeElement bestChild2=list.get(best_j);
				// root may be bestchild in the case of not integrated nodes
				MatrixBitWiseOperationTreeNodeElement partialroot1=bestChild1.root;
				// this variable must be set here to identify nodes which are not integrated.
				// bestchild1 (and bestChild2) may be identical to partialroot1 (and 2);
				// partialroot1.mother however is set after following constructor, and by this,
				// bestchild..mother may be set too.
				boolean noBestChild1Mother=bestChild1.mother==null;
				boolean noBestChild2Mother=bestChild2.mother==null;
				
				MatrixBitWiseOperationTreeNodeElement partialroot2=bestChild2.root;
				
				//error check, to do
				int dummy_y=0;				
				if ((partialroot1==null)||(partialroot2==null))  dummy_y=10/0;
				// generate mother node and link bottom up with children
				MatrixBitWiseOperationTreeNodeElement mother= 
						new MatrixBitWiseOperationTreeNodeElement(
							LogOp.OR(bestChild1.contextBitSet, 
							bestChild2.contextBitSet),0, // row TODO
							partialroot1,partialroot2);
							// link with children
							partialroot1.mother=mother;
							partialroot2.mother=mother;
							
							// difference in mother, may be used for defining cut for class building TODO
							//ORing 
							if(partialroot1.or==null) {
								if (noBestChild1Mother)
									partialroot1.or=bestChild1.contextBitSet;
								else System.out.println("partialroot1 no or set; bestchild1: "+
										namedFieldMatrix.getRowName
										(bestChild1.fromNamedFieldMatrixRow));
							};
							if(partialroot2.or==null) {
								if (noBestChild2Mother)
									partialroot2.or=bestChild2.contextBitSet; 
								else System.out.println("partialroot2 no or set;  bestChild2: "
								+	namedFieldMatrix.getRowName
								(bestChild1.fromNamedFieldMatrixRow));
							};
							mother.or=LogOp.OR(partialroot1.or, partialroot2.or);
							// ANDing
							//System.out.println(" vor LogOp.containes");
							if(LogOp.containes(partialroot1.and,partialroot2.and)){
								mother.and=(BitSet)partialroot1.and.clone();
							} else if (LogOp.containes(partialroot2.and,partialroot1.and)){
								mother.and=(BitSet)partialroot2.and.clone();
							} else {
								mother.and=LogOp.AND(partialroot1.and, partialroot2.and);
							}
							
				checkConflictingElements(list,listSize,competition,partialroot1,partialroot2);			
				// reset root in partial tree with root mother	
				mother.walkForRootSetting(mother,mother,this.writer);		
				list.add(mother);//??
				
				
				// 
				this.writer.println("searchBestPairForNeighborhoodTree_1 bestChild1: "+
						this.namedFieldMatrix.getRowName(bestChild1.fromNamedFieldMatrixRow)+
						" bestChild2: "+
							this.namedFieldMatrix.getRowName(bestChild2.fromNamedFieldMatrixRow));
				this.writer.println("val: "+ bestVal+ " i: "+best_i+ " j: "+best_j+
				" mother nr: "+list.size());
				
				return true;
			}
			else return false;
		}// searchBestPairForNeighborhoodTree_1
	
	
	/*
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
	*/
	
	
	/*
	 
	private MatrixBitWiseOperationTreeNodeElement generateWeightedBinaryNeighborhoodTree
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
	
	*/

	/*
	 * The neighborhood tree is based on elements which should NOT be containing,
	 * i.e. which are morphologically unambiguous. The checkContaining
	 * method substitutes ambiguous containing elements by new elements which
	 * are not ambiguous (which correspond to contained unambiguous 
	 * morphological classes). The new elemnts are added to list
	 * 
	 */
	
	
	
	
	private MatrixBitWiseOperationTreeNodeElement generateMorphologicalNeighborhoodTree
	(ArrayList<MatrixBitWiseOperationTreeNodeElement> list,
		MatrixBitWiseOperationCompetition competition)
	/*
	 * neighborhood tree is built with elements of list; these elements are used (too) as nodes
	 * of neighborhood tree. Neighborhood tree is built bottom up, by joining most
	 * similar elements. There are (many) neighborhood trees in bottom up construction process.
	 * Two trees are joined if they contain two most similar elements (i.e. terminal
	 * nodes).
	 *  
	 */
	
	{
		this.writer.println(" generateMorphologicalNeighborhoodTree Entry");
		System.out.println(" generateMorphologicalNeighborhoodTree Entry");
		int listSize=list.size();
		// bottom up tree building by joining best pairs of list;
		// mother(s) of best pairs are added to list and then included in treebuilding, too
		//
		while(searchBestPairForNeighborhoodTree_1(list,listSize,competition)) {
				// nothing to do here	
		}
		 
		// root of tree is last element added in list
		
		MatrixBitWiseOperationTreeNodeElement root=list.get(list.size()-1);//root, i.e last of list	
		this.writer.println(" generateMorphologicalNeighborhoodTree vor Exit root_nr: "+
		(list.size()-1));
		
		return root;		
	}//generateMorphologicalNeighborhoodTree
	
	
	
	public NamedFieldMatrix restruct(NamedFieldMatrix nFieldMatrix/*inMatrix*/,
		MatrixBitWiseOperationCompetition competition,PrintWriter pwriter){
		this.writer=pwriter;
		this.writer.println("\n\n----------MatrixDynamicMorphClustering.restruct Entry");
		this.namedFieldMatrix=nFieldMatrix;		
		
		/* this.actual=new Stack<MatrixDynamicMorphClusteringEntryBitValue>();
		*  this.best=new Stack<MatrixDynamicMorphClusteringEntryBitValue>();
		*
		* 
		* // generates a list of context bits set in falling order
		*
		* //this.cardinorderedEntryContextList=this.generateLocalcardinorderedEntryContextList();
		*
		******************
		* ArrayList <MatrixDynamicMorphClusteringEntryBitValue> contextIntersectionList=
		* this.generateSortedContextIntersectionList();
		*
		* int entryIndex=0;// check only
		* MatrixDynamicMorphClusteringEntryBitValue entryElement=
		* .get(entryIndex);
		* this.complementary(contextIntersectionList,entryIndex,entryElement.bitSet,false);
		*	
		* //this.generateSpecificContextEntries();
		*
		* // extendNamedFieldMatrix
		********/
		
		
		this.writer.println("vor generateMatrixBitWiseOperationTreeNodeElementList");
		 ArrayList<MatrixBitWiseOperationTreeNodeElement> treeNodeList=
		generateMatrixBitWiseOperationTreeNodeElementList(); 
		 this.writer.println("nach generateMatrixBitWiseOperationTreeNodeElementList");
		 
		 //pragmaticDeletionOfCompeting(treeNodeList);
		
		 //-----restructMorphologically: analyze whether containing classes
		 RestructMorphologicalClasses restructMorph=new RestructMorphologicalClasses();
		 restructMorph.restructMorphologically(treeNodeList, this.namedFieldMatrix, writer);
		 
		 // enter morphologically disambiguated classes in treeNodeList
		 restructMorph.enterDisambiguatedClasses(treeNodeList,writer,nFieldMatrix);
		
		 MatrixBitWiseOperationTreeNodeElement root=
		 this.generateMorphologicalNeighborhoodTree(treeNodeList,competition);
		
		
		// 
		
		ArrayList<MatrixBitwiseOperationTreeNodeCodeGenerationElement> resultList=
				new ArrayList<MatrixBitwiseOperationTreeNodeCodeGenerationElement>();
		
		MatrixBitWiseOperationTreeNodeElement.walkForCodeGeneration
		(root,  '0',0,resultList,this.writer,this.namedFieldMatrix);
		
		MatrixBitwiseOperationTreeNodeCodeGenerationElement.printList
		(resultList, this.writer, this.namedFieldMatrix," unsorted");
		
		MatrixBitwiseOperationTreeNodeCodeGenerationElement.sort(resultList);
		MatrixBitwiseOperationTreeNodeCodeGenerationElement.printList
		(resultList, this.writer, this.namedFieldMatrix," sorted");
			
		writer.println("MatrixDynamicMorphClustering restruct Exit");
		return this.namedFieldMatrix;
	}
	
}
