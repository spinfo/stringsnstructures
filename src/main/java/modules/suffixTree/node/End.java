package modules.suffixTree.node;

/* for all terminal nodes end is (first) presumed to be maximal;
 * it should be set to the real position after arriving at the text end.
 * this can be done by a variable shared by all leaf nodes (which is to be
 * reset ex.g. after splitting edges
 */
public class End {

	int end;

	// cstr
	public End(int end) {
		this.end = end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getEnd() {
		return end;
	}
}
