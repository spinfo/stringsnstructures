package modules.transitionNetwork.elements;


import modules.transitionNetwork.List.TNArrayList;
import modules.tree_building.suffixTree.Node;


public class StateElement extends AbstractElement{
	
	/* element for (vertical) list of States */
	public int state;
	public TNArrayList<StateTransitionElement> toStateTransitions;
	
	public StateElement(int state){
		this.state=state;
		this.toStateTransitions=new TNArrayList<StateTransitionElement>();
	}
	
	
	public StateTransitionElement getStateTransitionElementState(int state/*node pointing to*/) {
	return null;
	}
	
	public StateTransitionElement getStateTransitionElementSuffix(int suffix /*suffix pointing to*/) {
		return null;
		}
	
	/*public int addStateTransitionElement(int statesIndex,int suffixesIndex){
		StateTransitionElement e=new StateTransitionElement();
		//e.statesIndex=statesIndex; 
		//e.suffixesIndexe=suffixesIndex; 
		return -1;
	}
	*/
		
	


}
