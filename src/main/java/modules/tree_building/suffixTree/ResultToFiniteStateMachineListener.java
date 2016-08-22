package modules.tree_building.suffixTree;



import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import modules.OutputPort;
import modules.transitionNetwork.TransitionNetwork;
import modules.transitionNetwork.elements.StateElement;
import modules.transitionNetwork.elements.StateTransitionElement;
import modules.transitionNetwork.elements.SuffixElement;


public class ResultToFiniteStateMachineListener extends AbstractResultNodeStackListener {

	// the OutputPort to write to
	private final OutputPort outputPort;

	// the suffix tree this will work on
	private final BaseSuffixTree tree;
	
	private boolean inverted= true;
	
	private TransitionNetwork tn;

	private Stack<Integer> nodeNrs=null;
	private boolean afterBacktrack=true;
	private int nodeNrsStackPos;
	
	private int lengthOfPath;

	public ResultToFiniteStateMachineListener(BaseSuffixTree suffixTree, OutputPort outputPort) {
		super(suffixTree);

		this.tree = suffixTree;
		this.outputPort = outputPort;
		this.nodeNrs=new Stack<Integer>();
		this.tn=new TransitionNetwork(suffixTree.text,this.inverted);		
	}
	
	public void setTN(TransitionNetwork tn){
		this.tn=tn;
	}
	
	public TransitionNetwork getTN(){
		return this.tn;
	}
	
	public void setInverted(boolean inverted){
		this.inverted=inverted;
	}
	
	@Override
	public void entryaction(int nodeNr, int level) throws IOException {
		if (this.afterBacktrack){
			this.afterBacktrack=false;
			nodeNrsStackPos=this.nodeNrs.size()-1;
		}
		this.nodeNrs.push(nodeNr);
		this.lengthOfPath=this.lengthOfPath+tree.getNode(nodeNr).getEnd(0)-
				tree.getNode(nodeNr).getStart(0);
	}

	/**
	 * NOTE: This assumes depth-first traversal of the tree in the tree walker.
	 * 
	 * @param nodeNr
	 *            the node whose edges are to be printed.
	 * @param level
	 *            (irrelevant here, required by interface)
	 */
	@Override
	public void exitaction(int nodeNr, int level) throws IOException {
		this.afterBacktrack=true;
		
		final Node node = tree.getNode(nodeNr);

		if (node.isTerminal()) {
			process(nodeNr,null,0,level);
		}
		
		this.lengthOfPath=this.lengthOfPath- (tree.getNode(nodeNr).getEnd(0)-
				tree.getNode(nodeNr).getStart(0));	
		this.nodeNrs.pop();
	}
	

	@Override
	public void process(int nodeNr, List<Node> path, int pathLength, int level) throws IOException {

		// get the node and label in question
		final Node node = tree.getNode(nodeNr);
		
		final String label = tree.edgeString(node);
		
		if (node.isTerminal()) {
			
			// path starts with first sign of text, i.e. whole word
			// to Do PositionAmounts, e.g. aufhören$ hören$
			if (node.getEnd(0)==this.tree.getTextBegin(node.getTextNr(0))+
					this.lengthOfPath)
				
			{
				System.out.println("whole word");
				System.out.print("leave: "+nodeNr+"  ");
				System.out.println("label: "+label);

				// stack 
				int nodeIndex=0;
				ListIterator<Integer> it = nodeNrs.listIterator(this.nodeNrsStackPos);
				if (it.hasNext()) nodeIndex=it.next();
				// do not repeat already written nodes
				System.out.println("process nodeIndex: "+nodeIndex+"  "+"nodeNrsStackPos: "+
				this.nodeNrsStackPos);
				//while((nodeIndex !=this.entryActionNewNodeNr)&& (it.hasNext())) nodeIndex=it.next();
				//for (ListIterator<Integer> it = nodeNrs.listIterator(0); it.hasNext(); )
				while (it.hasNext())
				{
					
					//int nodeIndex=it.next();
					System.out.println("mother: "+nodeIndex+"  "+tree.edgeString(tree.getNode(nodeIndex))
					);
					
					// node(Index) in stateList? insert, if not
					
					
					int posInStates=
					this.tn.addStateElement(new StateElement(nodeIndex));
					System.out.println("posInStates: "+posInStates);
					StateElement stateElement= (StateElement)this.tn.states.get(posInStates);
					System.out.println("PosInStateElementList:" +posInStates);
					// get children (follow states) and suffix strings which lead to them
					int childNr=it.next();
					/* for (int childNr : this.tree.nodes[nodeIndex].next.values()) { */
						// generate StateTransitionElement
						StateTransitionElement stateTransitionElement= new StateTransitionElement();
						int childPosInStateElementList=
						this.tn.addStateElement(new StateElement(childNr));
						// follow state, transition in next state
						stateTransitionElement.toStateElement=childPosInStateElementList;
						// suffixes (?? for all PositionAmounts (???, TODo)
						int suffixStart=this.tree.nodes[childNr].getStart(0);
						int suffixEnd =this.tree.nodes[childNr].getEnd(0);
						
						
						SuffixElement suffixElement=new SuffixElement(suffixStart, suffixEnd);
						int posInSuffixes=
						this.tn.addSuffixElement(suffixElement);
						stateTransitionElement.toSuffixElement=posInSuffixes;
						// append in toStateTransitions in (mother)State element
						/*int posInStateTransitions=*/
						stateElement.toStateTransitions.add/*StateTransitionElement*/(stateTransitionElement);
						
					/* }*/

						nodeIndex=childNr;
				}// for (ListIterator<Integer> it ...
					
				tn.writeTN();
			}
			
		}
	}

}



