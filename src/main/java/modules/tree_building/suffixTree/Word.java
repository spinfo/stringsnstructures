package modules.tree_building.suffixTree;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * @author JR
 * @version 1.0
 */

public class Word {
	ArrayList <Integer> morphInWordList;
	
	public BranchedStringBufferElement branchedString(ResultToMorphListListener r) {
		int node, start,end,branchPosition;
		// necessary for inverted positions
		ArrayList<Integer> branchPosList=new ArrayList<Integer>();
		branchPosition=0;
		StringBuffer sb= new StringBuffer();
		// 
		BitSet bitSetBranches= new BitSet();
		for ( int i=0;i<this.morphInWordList.size();i++)
		{
			node=this.morphInWordList.get(i);
			start=r.tree.getNode(node).getStart(0);
			end=r.tree.getNode(node).getEnd(0);
			for(int pos=start;pos<end;pos++){
				  //System.out.print(r.tree.text[pos]);
				  if(r.inverted) sb.insert(0, r.tree.text[pos]);
				  else
				  sb.append(r.tree.text[pos]);
				  branchPosition++;
			}
			//System.out.print(" ");
			//System.out.print(this.morphInWordList.get(i)+ " ");
			if (r.nodesWholePhrases[this.morphInWordList.get(i)]==2) 
				if (branchPosition>0){
					//System.out.print("branch: "+branchPosition+" ");
					branchPosList.add(branchPosition);
				}
				
			else if (r.nodesWholePhrases[this.morphInWordList.get(i)]<2) 
				//System.out.print(" nobranch ")
				;
			
		}
		if (r.inverted) {sb.deleteCharAt(0);sb.append('$');}
		//System.out.println();
		//System.out.println(sb);
		int pos=0;
		//System.out.println("BranchPositions");
		for (int j=0;j<branchPosList.size();j++){
			if (r.inverted) pos=sb.length()-1-branchPosList.get(j);
			else pos=branchPosList.get(j);
			//System.out.print(pos+" ");
			bitSetBranches.set(pos, true);
		}
		//System.out.println();
		// 
		BranchedStringBufferElement bs=new BranchedStringBufferElement(sb,bitSetBranches);
		return bs;
	}
}
