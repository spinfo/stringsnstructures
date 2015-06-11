package suffixTree.suffixTree.applications.event;

import java.util.EventObject;

public abstract class MyEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3512426506376547519L;

	public MyEvent(int node1) {
		// to do
		super(node1);

	}
}
