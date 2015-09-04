package modules.suffixTree.suffixTree.node.activePoint;

public class ExtActivePoint extends ActivePoint{
	
	public int phase;
	
	public ExtActivePoint(int active_node, int active_edge,int active_length,
			int phase) {
			super(active_node, active_edge,active_length);
			this.phase=phase;
	}
}