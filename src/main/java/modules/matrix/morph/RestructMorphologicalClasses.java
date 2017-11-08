package modules.matrix.morph;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import common.logicBits.LogOp;
import models.NamedFieldMatrix;
import modules.matrix.MatrixBitWiseOperationTreeNodeElement;

public class RestructMorphologicalClasses {

	PrintWriter printWriter;
	
/*
 *  for all elements of list (TreeNodeElements) do in RestructMorphologicalClasses:
 *  generate for each element list of contained (other) elements of treeNodeElementlist.
 *  This means: it is looked for elements of partial distribution, e.g. regier+e, regier+st,
 *  regier+ung. Partial distribution is found in telefonier+e, telefonier+st, but there is no
 *  *telefonier+ung. A further, but disjunct distribution is found in zeit+ung.
 *  Thus, regier is quite similar to two disjunct classes.
 *  First, in morphemize, all contained (but not necessarily disjunct) morphological classes are found.
 *  As a side effect, the greatest contained class is identified (e.g. telefonier).
 *  
 *  In a second step (in selectDisjunctUnAmbigious) the best but disjunct 
 *  remaining morphological classes are found
 *  and gathered (in the resulting containing list, by marking its elements by the undone flag).
 *  It should be remarked that a contained class itself may contain further contained classes.
 *  These contained classes may be identified prior or later. The resulting lists are 
 *  transfered to the elements for neighborhood tree building. Thus, regier 
 *  is quite similar to telefonier on the one hand and to zeit(ung) on the other.
 *
 * 
 * 
 * 
 */
	
	private ContainingElement contains(MatrixBitWiseOperationTreeNodeElement el1, 
			MatrixBitWiseOperationTreeNodeElement el2){
			BitSet res=LogOp.AND(el1.contextBitSet, el2.contextBitSet);
			// el2 (completely) contained in el1
			if (res.cardinality()==el2.contextBitSet.cardinality()){
				// not equal el1, el2
				if (res.cardinality()<el1.contextBitSet.cardinality())
				return new ContainingElement(el1,el2,res);
			}
		return null;
	}//contains
	
	private ArrayList<ContainingElement> selectDisjunctUnAmbigious(ArrayList<ContainingElement> containingList,
			BitSet bestSet, // bestSet is the greatest common set found in morphemize
			MatrixBitWiseOperationTreeNodeElement containingElement,
			NamedFieldMatrix namedFieldMatrix) {
		
		System.out.println("selectDisjunctUnAmbigious Entry ");
		// not all subclasses found? 
		// bestSet is augmented by ORing with localbestSet (see below)
		while (containingElement.contextBitSet.cardinality()> bestSet.cardinality()) {
			int best =bestSet.cardinality();
			int best_i=-1;
			BitSet localBestSet=null;
			// search best
			for (int i=0;i<containingList.size();i++){
				ContainingElement contained=containingList.get(i);
				// only undone elements
				if (contained.unDone){
					// ORing of elements, in order to find all contained classes which
					// cover the containing
					BitSet res=LogOp.OR(bestSet,contained.containedBitSet );
					// does result grow? (i.e. does it cover new elements)
					if(res.cardinality()>best){
						// save best(better) result
						best=res.cardinality();
						best_i=i;
						localBestSet=res;
					}
				}
			} //for (int =0...
			// no further success
			if (best_i==-1)return null; 
			else 
			
			{	// ORing, in order to get all covering contained
				bestSet=LogOp.OR(bestSet, localBestSet);
				// mark elements in list as worked
				containingList.get(best_i).unDone=false;
			}// if(best_i
			
			
		}// while
		// reduce containingList to elements which are done; 
		// remove undone elements; undone elemments do not contribute to contained 
		// classes		
		int i=0;
		while(i<containingList.size()){
			ContainingElement contained=containingList.get(i);
			if (contained.unDone){
				containingList.remove(i);
			} else i++;
			
		}
		// result is given back as (shortened)containingList. This list is used for 
		// neighborhood tree building, by using (indirect)references 
		// to entries in NamedFieldMatrix
		//
		this.printWriter.println("selectDisjunctUnAmbigious");
		for (int j=0;j<containingList.size();j++){
			int row=containingList.get(j).contained.fromNamedFieldMatrixRow;
			this.printWriter.print(namedFieldMatrix.getRowName(row)+" ");
			
		}
		this.printWriter.println();
		System.out.println("selectDisjunctUnAmbigious Exit ");
		return containingList;
		
	}//selectDisjunctUnAmbigious
	
	
	public void restructMorphologically(ArrayList<MatrixBitWiseOperationTreeNodeElement> list,
			NamedFieldMatrix namedFieldMatrix,
			PrintWriter p){
			System.out.println("restructMorphologically Entry ");
			this.printWriter =p;
		
			// check all elements for subclasses (outer for loop for containing)
			for (int i=0;i<list.size();i++){
				
				int max=0;int indexOfBest=-1;
				// for each element create list of contained
				// elements of containedList contain both, containing and contained
				ArrayList<ContainingElement> containedList=
				new ArrayList<ContainingElement>();	
				MatrixBitWiseOperationTreeNodeElement containingElement_i=list.get(i);
				// 
				// inner for loop for contained
				for (int j=0;j<list.size();j++){
					if (i!=j){
						MatrixBitWiseOperationTreeNodeElement containedElement_j=list.get(j);
						// create list of containing and contained
						ContainingElement containedElement=contains(containingElement_i,containedElement_j);
						if (containedElement !=null ){
							// gather in list
							containedList.add(containedElement);
							// look for best element contained (side effect of gathering,
							// is used as base for analyzeContaining
							if (containedElement.containedBitSet.cardinality()>max) {
								max=containedElement.containedBitSet.cardinality();
								indexOfBest=containedList.size()-1;
							}
						}// if (contEl..
					}// if (i!=j)
					
				}// for(int j..
				
				if (indexOfBest>=0)
					{ 
						ContainingElement bestContainedElement=
								containedList.get(indexOfBest);	
						// save best set
						BitSet bestSet=bestContainedElement.containedBitSet;
						// delete best element in list which is already known
						bestContainedElement.unDone=false;
						int bestRowContaining=
						bestContainedElement.containing.fromNamedFieldMatrixRow;
						int bestRowContained=
						bestContainedElement.contained.fromNamedFieldMatrixRow;
						this.printWriter.println("morphemize bestContaining: "+
						namedFieldMatrix.getRowName(bestRowContaining)						
						+ " bestContained: "+
						namedFieldMatrix.getRowName(bestRowContained)+ " cardinality: "
						+bestSet.cardinality());
												
						// analyze remaining contained classes; 
						// save containedlist in containingElement containingElement_i
						// is needed when generating neighborhood tree
						containingElement_i.containingList=
						selectDisjunctUnAmbigious(containedList,bestSet,containingElement_i,
						namedFieldMatrix);
						
				}//if (maxIndex>=0)
				 
				
			}// for (int i...
			System.out.println("restructMorphologically Exit ");
	 }//restructMorphologically
	
	
	// enters parts (which are not ambiguous and which correspond to contained) of containing in list
	//
	
	
	private void checkContaining(MatrixBitWiseOperationTreeNodeElement originContaining,
			MatrixBitWiseOperationTreeNodeElement element,
			ArrayList<MatrixBitWiseOperationTreeNodeElement> list,int count,
			PrintWriter writer,NamedFieldMatrix namedFieldMatrix)	{
		writer.println(" checkContaining entry count: "+count);
		if(count>50)count=count/0;
		// loop for bredth second traversal
		for (int i=0;i<element.containingList.size();i++){
			// locallyContainedEl is contained element of containingList (here list of contained)
			ContainingElement elementOfContainingList=element.containingList.get(i);
			// ?? check
			MatrixBitWiseOperationTreeNodeElement contained=elementOfContainingList.contained;
			
			if (contained==originContaining)count=count/0;
			// depth first recursion: does contained contain further elements?
			if (contained.containingList != null){
				writer.println(" checkContaining: origin "+
				" "+namedFieldMatrix.getRowName(originContaining.fromNamedFieldMatrixRow)+
				" contained "+
				namedFieldMatrix.getRowName(contained.fromNamedFieldMatrixRow));
				checkContaining(originContaining,contained,list,count+1,writer,namedFieldMatrix);	
			} else {
				// contained which is NOT containing;
				// generate new element for given origin
				MatrixBitWiseOperationTreeNodeElement newElement=
				new	MatrixBitWiseOperationTreeNodeElement(contained.contextBitSet,
				originContaining.fromNamedFieldMatrixRow,null,null);
				list.add(newElement);
				writer.println("checkContaining newElement for: "+
				namedFieldMatrix.getRowName(originContaining.fromNamedFieldMatrixRow)+
				" "+namedFieldMatrix.getRowName(contained.fromNamedFieldMatrixRow));
			}
		}//for (int i=0;i<element.containingList.size();i++)
	}// checkContaining
	
	
	public void enterDisambiguatedClasses(ArrayList<MatrixBitWiseOperationTreeNodeElement> list,			
			PrintWriter writer,NamedFieldMatrix nFieldMatrix){
		 
		int listsize=list.size();
		for (int i=0;i<listsize;i++){
			MatrixBitWiseOperationTreeNodeElement el=list.get(i);
			if (el.containingList!=null){
				checkContaining(el, // origin, to be replaced
						el,//here identical with origin, will be changed
						//in recursive traverse 
						list,// to which new elements are added
						0,writer,nFieldMatrix);
			}
			
		}
		
	};
	
	
	
}
