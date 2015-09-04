package modules.suffixTree.suffixTree.node.info;

// nodeInfo for modules.suffixTree
public class NodeInfo {

	int start;
	End end;

	// cstr
	public NodeInfo(int start, End end /* , int oo */) {
		this.start = start;
		this.end = end;
	}

	public int getEnd() {
		return this.end.getEnd();
	}

	public void setEnd(End end) {
		this.end = end;
	}

	public int getStart() {
		return this.start;
	}

	public void setStart(int start) {
		this.start = start;
	}
}