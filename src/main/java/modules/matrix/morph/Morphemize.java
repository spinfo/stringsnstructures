package modules.matrix.morph;

import java.util.ArrayList;
import java.util.BitSet;
import common.logicBits.LogOp;

import modules.matrix.MatrixBitWiseOperationTreeNodeElement;

public class Morphemize {
	
	
	private ContainingElement contains(MatrixBitWiseOperationTreeNodeElement el1, 
			MatrixBitWiseOperationTreeNodeElement el2){
			BitSet res=LogOp.AND(el1.contextBitSet, el2.contextBitSet);
			// el2 contained in el1
			if (res.cardinality()==el2.contextBitSet.cardinality()){
				
				return new ContainingElement(el1,el2,res);
			}
		return null;
	}
	
	private ArrayList<ContainingElement> analyzeContainingList(ArrayList<ContainingElement> containingList,
			BitSet bestSet, MatrixBitWiseOperationTreeNodeElement containingElement) {
		
		while (containingElement.contextBitSet.cardinality()> bestSet.cardinality()) {
			int best =bestSet.cardinality();int best_i=-1;
			BitSet localBestSet=null;
			// search best
			for (int i=0;i<containingList.size();i++){
				ContainingElement contained=containingList.get(i);
				if (contained.unDone){
					BitSet res=LogOp.OR(bestSet,contained.containedBitSet );
					if(res.cardinality()>best){
						best=res.cardinality();
						best_i=i;
						localBestSet=res;
					}
				}
			} //for (int =0...
			// no further success
			if (best_i==-1)return null; else {
				bestSet=LogOp.OR(bestSet, localBestSet);
				containingList.get(best_i).unDone=false;
			}// if(best_i
			
			
		}// while
		// reduce containingList to elements which are done; remove undone		
		int i=0;
		while(i<containingList.size()){
			ContainingElement contained=containingList.get(i);
			if (contained.unDone){
				containingList.remove(i);
			} else i++;
			
		}
		return containingList;
	}
	
	public void morphemize(ArrayList<MatrixBitWiseOperationTreeNodeElement> list){
		
			
			for (int i=0;i<list.size();i++){
				
				int max=0;int indexOfBest=-1;
				ArrayList<ContainingElement> containingList=
				new ArrayList<ContainingElement>();	
				MatrixBitWiseOperationTreeNodeElement element_i=list.get(i);
				// 
				for (int j=0;j<list.size();j++){
					if (i!=j){
						MatrixBitWiseOperationTreeNodeElement element_j=list.get(j);
						// create list of containing
						ContainingElement contEl=contains(element_i,element_j);
						if (contEl !=null ){
							containingList.add(contEl);
							if (contEl.containedBitSet.cardinality()>max) {
								max=contEl.containedBitSet.cardinality();
								indexOfBest=containingList.size()-1;;
							}
						}// if (contEl..
					}// if (i!=j)
					
				}// for(int j..
				if (indexOfBest>=0)
					{ 
						// save best set
						BitSet bestSet=containingList.get(indexOfBest).containedBitSet;
						// delete best element in list which is already known
						containingList.get(indexOfBest).unDone=false;
						// analyze rest
						analyzeContainingList(containingList,bestSet,element_i);
					
				}//if (maxIndex>=0)
				
				
			}// for (int i...
	 }
}
