package modules.suffixTree.applications;

import java.util.ArrayList;
import java.util.Stack;

@Deprecated
public class ResultSuffixTreeNodeStack {

	private Stack<Integer> stack;

	private ArrayList<SuffixDescription> resultListOfSuffixesFromTerminalToRoot;

	private SuffixTreeAppl suffixTree = null;

	public ResultSuffixTreeNodeStack(SuffixTreeAppl suffixTree) {
		this.stack = new Stack<Integer>();
		this.suffixTree = suffixTree;
	}

	public void setSuffixTree(SuffixTreeAppl suffixTreeAppl) {
		suffixTree = suffixTreeAppl;
	}

	public ArrayList<SuffixDescription> SuffixDescriptionArrayListFactory() {
		return new ArrayList<SuffixDescription>();
	}

	public void SetSuffixDescriptionArrayList(ArrayList<SuffixDescription> list) {
		resultListOfSuffixesFromTerminalToRoot = list;
	}

	public ArrayList<SuffixDescription> GetSuffixDescriptionArrayList() {
		return resultListOfSuffixesFromTerminalToRoot;
	}

	public String writeStack() {
		StringBuffer strBuf = new StringBuffer();
		int node;

		for (int i = 0; i <= stack.size() - 1; i++) {
			node = stack.get(i);

			if (node != 1) {
				strBuf.append(suffixTree.edgeString(node));
			}
		}
		return strBuf.toString();
	}

	public void addStackForSuffixesToList(int nodePosition, ArrayList<SuffixDescription> listOfSuffixDescription) {

		int node, len;
		len = 0;
		System.out.println("ResultSuffixTreeNodeStack.writeStackForSuffixesToList entry node " + stack.peek());

		// for (int i = 0; i < stack.size(); i++)
		for (int i = stack.size() - 1; i >= 0; i--) {
			node = stack.get(i);
			// System.out.println
			// ("ResultSuffixTreeNodeStack.writeStackForSuffixesToList node
			// "+node+
			// " pathLength "+pathLength+ " len "+len);
			if (node != 1) {
				// length from end
				len = suffixTree.nodes[node].nodeInfo.getEnd() - suffixTree.nodes[node].nodeInfo.getStart();
				// determine branching positions for all endings in
				// generalized suffix trees
				// LOGGER.info("writeStackForSuffixes edgestring: "+
				// modules.suffixTree.edgeString(node)+ " len: "+len);
				System.out
						.println("ResultSuffixTreeNodeStack.writeStackForSuffixesToList  node " + node + " edgestring: "
								+ suffixTree.edgeString(node) + "\nnodePosition " + nodePosition + "  len: " + len);
				SuffixDescription suffixDescription = new SuffixDescription(nodePosition, node);

				listOfSuffixDescription.add(suffixDescription);
				nodePosition = nodePosition - len;
			}

		}

	}

	/**
	 * Pushes a nodeNr to the stack
	 * 
	 * @param nodeNr
	 *            the node number to push
	 * @return the nodeNr pushed
	 */
	public Integer push(int nodeNr) {
		return this.stack.push(nodeNr);
	}

	/**
	 * Pops a nodeNr to the stack.
	 * 
	 * @return the nodeNr.
	 */
	public Integer pop() {
		return this.stack.pop();
	}

	/**
	 * @return A boolean describing whether the stack has any nodeNrs.
	 */
	public boolean empty() {
		return this.stack.empty();
	}
	
	/**
	 * Look at the nodeNr at the top of the stack, but do not remove it.
	 * @return the nodeNr at the top of the stack
	 */
	public int peek() {
		return this.stack.peek();
	}
	
	/**
	 * Get the SuffixTreeAppl that this node stack ist bound to.
	 * @return a SuffixTreeAppl
	 */
	public SuffixTreeAppl getSuffixTreeAppl() {
		return this.suffixTree;
	}

	public void writeListOfSuffixDescription(ArrayList<SuffixDescription> list) {
		System.out.println("ResultSuffixTreeNodeStack.writeListOfSuffixDescription entry");
		for (SuffixDescription suffDesc : resultListOfSuffixesFromTerminalToRoot) {
			System.out.println("ResultSuffixTreeNodeStack.writeListOfSuffixDescription node " + suffDesc.node
					+ " position " + suffDesc.position);
		}
	};
}