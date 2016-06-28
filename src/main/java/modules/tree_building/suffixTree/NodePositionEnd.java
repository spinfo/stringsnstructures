package modules.tree_building.suffixTree;

/**
 * The end position of a node in the suffix tree is wrapped in an object such that
 * the end positions of multiple nodes may be represented by a single such object and
 * changed simultaneously.
 */
public class NodePositionEnd{

	protected int val;

	public NodePositionEnd(int val){
		this.val=val;
	}
}

