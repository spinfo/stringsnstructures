package modules.transitionNetwork.elements;

public class StateTransitionElement extends AbstractElement {
	/* Element in List of StateSuffix, pointing to SuffixElement which is read, and to
	 * following State (type of transition) 
	 */
	public int toStateElement;
	public int toSuffixElement;
}
