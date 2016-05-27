package modules.treeIndexes;

// Model Dot2TreeNodes import.
import models.Dot2TreeNodes;

public class TreeIndexNodes extends Dot2TreeNodes {
	
	// Variables:
	
	// Variable holding the continuous edge label form the root to that specific node.
	private String contEdgeLabel;
	
	// End variables.
	
	// Constructors:
	
	public TreeIndexNodes (int number) {
		super (number);
		
	}
	// End Constructors.
	
	// Methods:
	
	// Getters:
	
	public String getContEdgeLabel () {
		return this.contEdgeLabel;
	}
	
	// End getters.
	
	// Setters:
	
	public void setContEdgeLabel (String label) {
		this.contEdgeLabel = label;
	}
	
	// End setters.
	
	// End Methods.

}
