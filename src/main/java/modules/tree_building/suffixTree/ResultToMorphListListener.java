package modules.tree_building.suffixTree;

/**
 * @author JR
 * @version 1.0
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import common.logicBits.ILogOp;
import modules.tree_building.suffixTree.Word;


public class ResultToMorphListListener  implements ITreeWalkerListener{
	
	// the suffix tree this will work on
	final BaseSuffixTree tree;
	boolean inverted = true;
	private Stack<Integer> nodeNrs = null;
	// the length of the path currently read
	private int lengthOfPath;
	
	
	
	
	public ArrayList <Word>words;
	
	int[] nodesWholePhrases;
	
	
	public ResultToMorphListListener(BaseSuffixTree suffixTree, boolean inverted) {
		this.tree = suffixTree;
		this.nodeNrs = new Stack<Integer>();
		
		this.inverted = inverted;
		this.nodesWholePhrases=new int[this.tree.nodes.length];
		
		this.words = new ArrayList<Word>();
		
	}
	
	@Override
	//	copy from ResultToFiniteStateMachineListener
	public void entryaction(int nodeNr, int level) throws IOException {
		this.nodeNrs.push(nodeNr);
		this.lengthOfPath = this.lengthOfPath + 
		this.tree.getNode(nodeNr).getEnd(0) - tree.getNode(nodeNr).getStart(0);
	
}

	@Override
	public void exitaction(int nodeNr, int level) throws IOException {
	// if the current node is a leaf of a whole input text, write out stack
		//System.out.println("exitAction entry nodeNr: "+nodeNr+" lengthOfPath: "+this.lengthOfPath);
		if (nodeIsLeafOfWholeInputText(nodeNr, this.lengthOfPath)) 
		
			{
			
			// mark node as in whole phrase;for paradigmatic relation;
			// except terminal nodes which don't branch but should be maintanined
			this.nodesWholePhrases[nodeNr]=3;//terminal node
			int anf, end, node;
			if (this.inverted) System.out.println("exitAction inverted LeafNode: "+nodeNr);
			else System.out.println("exitAction not inverted LeafNode: "+nodeNr);
			
			Word word=new Word();
			word.morphInWordList=new ArrayList<Integer>();
			this.words.add(word);
			
			if (this.inverted)
				
				{
				
				// 2-for loops, first loop for inverted order in word
				for (int i=0;i<this.nodeNrs.size();i++) 
					word.morphInWordList.add(this.nodeNrs.get(i));
				
				for (int i=this.nodeNrs.size()-1;i>=0;i-- )
				
					{ node=this.nodeNrs.get(i);
					
					  System.out.println("exitAction node: "+node);
					  anf=tree.getNode(node).getStart(0);
					  end=tree.getNode(node).getEnd(0)-1;
					 
					  for(int pos=end;pos>=anf;pos--)
						  if (this.tree.text[pos]!='$')
						  System.out.print(this.tree.text[pos]);
					  System.out.print(" ");
					}
					
				
				}
			else {
				for (int i=0;i<this.nodeNrs.size();i++)
				
				{
					node=this.nodeNrs.get(i);
					word.morphInWordList.add(node);
					
					anf=tree.getNode(node).getStart(0);
					end=tree.getNode(node).getEnd(0);
					 for(int pos=anf;pos<end;pos++)
						  System.out.print(this.tree.text[pos]);
					  System.out.print(" ");
					
				}
				
			};

				
		}
		this.lengthOfPath = this.lengthOfPath - 
				(tree.getNode(nodeNr).getEnd(0) - tree.getNode(nodeNr).getStart(0));
		this.nodeNrs.pop();
		
		// morphemes in contrast; mark mother if child is marked
		if (this.nodesWholePhrases[nodeNr]>0 )
			{
				System.out.println("exitaction nodeNrs nodeNr: "+nodeNr);
				if(!this.nodeNrs.empty())
					{System.out.println("peek: "+this.nodeNrs.peek() );
					if (this.nodesWholePhrases[this.nodeNrs.peek()]==0) {
					this.nodesWholePhrases[this.nodeNrs.peek()]=1;
					}
					else if (this.nodesWholePhrases[this.nodeNrs.peek()]==1) {
						this.nodesWholePhrases[this.nodeNrs.peek()]=2;
					}
				}
			
		}
	}
	
	// checks if the given node in this listeners tree corresponds to a whole
		// input text given the current path length.
		private boolean nodeIsLeafOfWholeInputText(int nodeNr, int pathLength) {
			Node node = this.tree.getNode(nodeNr);

			if (!node.isTerminal()) {
				return false;
			}
			System.out.println("nodeIsLeafOfWholeInputText terminal node:"+nodeNr+
					" pathLength: "+pathLength);
			for (NodePosition position : node.getPositions()) {
				//System.out.println("nodeIsLeafOfWholeInputText position.getEnd: "+
				//		position.getEnd() +" tree.getTextBegin "+tree.getTextBegin(position.getTextNr())+
				//				" TextBegin+pathLength: "+(tree.getTextBegin(position.getTextNr()) + 
				//					pathLength));
				if (position.getEnd() == tree.getTextBegin(position.getTextNr()) + pathLength) {
					
					return true;
				}
			}

			return false;
		}
		
		public ArrayList<BranchedStringBufferElement> results() {
			ArrayList <BranchedStringBufferElement>branchedStringElementList=new ArrayList <BranchedStringBufferElement>();
			BranchedStringBufferElement branchedStringElement;
			System.out.print("results ");
			if(this.inverted) System.out.println("inverted");
			else System.out.println("normal");
			System.out.println();
			for (int i=0;i<this.words.size();i++){
				Word word=this.words.get(i);
				branchedStringElement=word.branchedString(this);
				branchedStringElementList.add(branchedStringElement);
				System.out.println();
			}
			Collections.sort(branchedStringElementList,new StringBufferElementComparator());
			
			printBranchedStringElementList(branchedStringElementList);
			return branchedStringElementList;
		}
		
		public void printBranchedStringElementList(ArrayList<BranchedStringBufferElement>branchedStringElementList){
			System.out.println("\nSortedList\n");
			
			for(BranchedStringBufferElement b:branchedStringElementList){
				System.out.println(b.stringBuffer);
				StringBuffer outSb=new StringBuffer(b.stringBuffer);
				int nrInserts=0;
				for (int i=0;i<b.bitSet.length();i++){
					if(b.bitSet.get(i)) {
						System.out.print('|');
						outSb.insert(i+nrInserts,'|');
						nrInserts++;
						}
						else System.out.print(' ');
					
				} //for int i=0;i<b.bitSet.length();i++)
				System.out.println();System.out.println();
				System.out.println(outSb);
			}
				
						
		}
		
		public ArrayList<BranchedStringBufferElement>logOp(ArrayList<BranchedStringBufferElement>l1,
				ArrayList<BranchedStringBufferElement>l2, ILogOp il ) {
			// necessary precondition in1 and in2 contain identical strings
			// ??toDo throw eception if not??
			ArrayList<BranchedStringBufferElement>resList=new ArrayList<BranchedStringBufferElement>();
			for (int i=0;i<l1.size();i++){
				// warning, to check: no new strinbuffer, newElement has common reference with
				// element from l1 List!!!
				BranchedStringBufferElement newElement=new BranchedStringBufferElement(l1.get(i).stringBuffer,
				il.logOperation(l1.get(i).bitSet,l2.get(i).bitSet));
				resList.add(newElement);
				
			}
			
			
			
			return resList;
			
		}
		
	
		
}
	
	


