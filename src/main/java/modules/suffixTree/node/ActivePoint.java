package modules.suffixTree.node;

public class ActivePoint {
	public int active_node, active_edge,active_length;
	
	public ActivePoint(int active_node, int active_edge, int active_length) {
		this.active_node=active_node;
		if (active_edge<0)/*root*/ active_edge=0;
		this.active_edge=active_edge;
		this.active_length=active_length;
	}
}