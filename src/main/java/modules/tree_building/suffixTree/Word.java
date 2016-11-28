package modules.tree_building.suffixTree;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * @author JR
 * @version 1.0
 */

public class Word {
	ArrayList <Integer> morphInWordList;
	
	public ArrayList <BranchedStringBufferElement> branchedString(ResultToMorphListListener resultToMorphListener) {
		// generates a (whole graphical) word and its branching positions from the suffix tree,
		// both for forward and backward texts/trees
		// returns an ArrayList with one element in the case of a forwarded tree,
		// with two elements (the first (this element is revered) left right and bitset, the 
		// second element right left with its bitset for branching positions
		//
		
		int node, start,end,branchPosition;
		// each branching position is notified and stored in the branchPosList; thus, branching
		// positions can be marked in a bitSet.
		// 
		ArrayList<Integer> branchPosList=new ArrayList<Integer>();
		branchPosition=0;
		StringBuffer sb= new StringBuffer();
		StringBuffer reversedSb=null;
		// 
		BitSet bitSetBranches= new BitSet();
		BitSet reversedBitSetBranches=null;
		if (resultToMorphListener.inverted)reversedBitSetBranches=new BitSet();
		for ( int i=0;i<this.morphInWordList.size();i++)
		{
			node=this.morphInWordList.get(i);
			start=resultToMorphListener.tree.getNode(node).getStart(0);
			end=resultToMorphListener.tree.getNode(node).getEnd(0);
			for(int pos=start;pos<end;pos++){
				  //System.out.print(r.tree.text[pos]);
				  if(resultToMorphListener.inverted) sb.insert(0, resultToMorphListener.tree.text[pos]);
				  else
				  sb.append(resultToMorphListener.tree.text[pos]);
				  branchPosition++;
			}
			//System.out.print(" ");
			//System.out.print(this.morphInWordList.get(i)+ " ");
			
			// only branches whose leaves are leaf nodes of WHOLE words
			if (resultToMorphListener.nodesWholePhrases[this.morphInWordList.get(i)]==2) 
				if (branchPosition>0){
					//System.out.print("branch: "+branchPosition+" ");
					branchPosList.add(branchPosition);
				}
				
			else if (resultToMorphListener.nodesWholePhrases[this.morphInWordList.get(i)]<2) 
				//System.out.print(" nobranch ")
				;
			
		}
		if (resultToMorphListener.inverted) {
			System.out.println("Word inverted: "+sb);
			reversedSb=new StringBuffer(sb);
			reversedSb.reverse();
			System.out.println("Word inverted: "+sb+"   "+reversedSb);
			sb.deleteCharAt(0);sb.append('$');}
		//System.out.println();
		//System.out.println(sb);
		int pos=0;
		//System.out.println("BranchPositions");
		for (int j=0;j<branchPosList.size();j++){
			if (resultToMorphListener.inverted) {
				pos=sb.length()-1-branchPosList.get(j);
				reversedBitSetBranches.set(branchPosList.get(j), true);
			}
			else pos=branchPosList.get(j);
			//System.out.print(pos+" ");
			bitSetBranches.set(pos, true);
		}// for int j=0;j<branchPosList.size();j++)
		
		System.out.println("Word: "+sb);
		if (reversedSb!=null){
			System.out.println(reversedSb);
			for (int i=0;i<reversedBitSetBranches.length();i++)
				if(reversedBitSetBranches.get(i)) System.out.print('<');
				else System.out.print(' ');
			System.out.println();
		}
		
		BranchedStringBufferElement bs=new BranchedStringBufferElement(sb,bitSetBranches);
		
		ArrayList <BranchedStringBufferElement>branchedStringBufferElementList = 
		new ArrayList<BranchedStringBufferElement>();
		branchedStringBufferElementList.add(bs);
		if (reversedSb!=null)
		branchedStringBufferElementList.add(
				new BranchedStringBufferElement(reversedSb,reversedBitSetBranches));
		return branchedStringBufferElementList;
	}
}
