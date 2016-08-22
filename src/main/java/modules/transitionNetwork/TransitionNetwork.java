package modules.transitionNetwork;


import modules.transitionNetwork.List.TNArrayList;
import modules.transitionNetwork.elements.comparator.StateComparator;
import modules.transitionNetwork.elements.StateElement;
import modules.transitionNetwork.elements.StateTransitionElement;
import modules.transitionNetwork.elements.comparator.SuffixComparator;
import modules.transitionNetwork.elements.SuffixElement;

public class TransitionNetwork {
	
	public TNArrayList<StateElement> states=null;
	public TNArrayList<SuffixElement> suffixes=null;
	private StateComparator stateComparator;
	private SuffixComparator suffixComparator;
	private char[]text;
	private boolean inverted;
	
	public TransitionNetwork(char[]text,boolean inverted) {
		this.states=new TNArrayList<StateElement>();
		this.suffixes=new TNArrayList<SuffixElement>();
		this.stateComparator=new StateComparator();
		this.suffixComparator=new SuffixComparator(text);
		this.text=text;
		this.inverted=inverted;
	}
	
	public int addStateElement(StateElement stateElement) {
		int index= this.states.find(stateElement,this.stateComparator);
		
		if (index<0)
		{	this.states.add(stateElement);
			index= this.states.size()-1;
		}
		return index;
	}
	
	public int addSuffixElement(SuffixElement suffixElement) {
		int index= this.suffixes.find(suffixElement,this.suffixComparator);
		System.out.println("addSuffixElement: "+index);
		if (index<0)
		{	this.suffixes.add(suffixElement);
			index= this.suffixes.size()-1;
		}
		return index;
	}
	
	public void writeSuffixes(){
		System.out.println("writeSuffixes");
		for (int i=0;i<this.suffixes.size();i++){
			SuffixElement e=(SuffixElement)this.suffixes.get(i);
			e.writeSuffix(text,this.inverted);
			
		}
		
	}
	
	public void writeTN(){
		System.out.println("writeTN");
		this.writeSuffixes();
		System.out.println("writeStates");
		for (int i=0;i<this.states.size();i++){
			StateElement stateElement = (StateElement)this.states.get(i);
			System.out.println("Line: "+i+"  "+"State(Element)(Node): "+stateElement.state);
			for (int j=0;j<stateElement.toStateTransitions.size();j++)
			{	
				StateTransitionElement stateTransitionElement = 
						(StateTransitionElement)stateElement.toStateTransitions.get(j);
				System.out.print("		StateTransitionElement: "+
						stateTransitionElement.toStateElement+ "  ");
						SuffixElement suffixElement=(SuffixElement)this.suffixes.get(stateTransitionElement.toSuffixElement);
						suffixElement.writeSuffix(text,this.inverted);
			}
		}
	}
	

}
