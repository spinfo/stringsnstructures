package suffixTree.suffixTree;

import java.util.Arrays;
import java.util.logging.Logger;

import suffixTree.suffixTree.node.Node;
import suffixTree.suffixTree.node.activePoint.ExtActivePoint;
import suffixTree.suffixTree.node.info.End;
import suffixTree.suffixTree.node.info.NodeInfo;
import suffixTree.suffixTree.node.nodeFactory.NodeFactory;
import suffixTree.suffixTree.node.textStartPosInfo.TextStartPosInfo;

/* 
 * refactored by jr from 
 * http://stackoverflow.com/questions/9452701/ukkonens-suffix-tree-algorithm-in-plain-english
 * 
 */
public class SuffixTree {

	private static final Logger LOGGER = Logger.getGlobal();
	// .getLogger(SuffixTree.class.getName());

	// to be initialized in calling class e.g. SuffixTreeAppl
	public static End oo;
	public static int unit = 0;
	public static int textNr = 0;
	public static int leafCount = -1;
	// first position in Node (seems to be empty (?why))
	public Node[] nodes;
	public char[] text;
	private boolean printTree = false;

	int currentPosition = -1,// position in text
			needSuffixLink, // number of suffixes to be entered in tree
			remainder,
			// all vars ending in "_node" are node references (index) to Node []
			// nodes,
			// i.e. root_node, active_node, current_node,leaf_node,
			// child_node,split_node
			root_node, current_node,
			/*
			 * active point information, consisting of active_node, active_edge
			 * and active_length
			 */
			active_node,// node in consideration
			active_edge,// edge departing from active node (position of char in
						// text(???)
						// (addressed by int val of active_edge))
			active_length;// position in active edge (where to add next char)

	private NodeFactory nodeFactory;

	public SuffixTree(int length, NodeFactory nodeFactory) {
		nodes = new Node[2 * length + 2];
		text = new char[length];

		this.nodeFactory = nodeFactory;
		root_node = active_node = newNode(-1, new End(-1), "cstr");

	}

	// overridden in SuffixTreeAppl
	public void printTree(String message, int hotNode1, int hotNode2,
			int active_length) {
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	public int getRoot() {
		return root_node;
	}

	public int getCurrentNode() {
		return current_node;
	}

	private int newNode(int start, End end, String m) {
		// see leaf node condition in nodeFactory.generateNode, i
		// increment nr
		NodeInfo nodeInfo = new NodeInfo(start, end /* ,this.oo.getEnd() */);

		nodes[++current_node] = this.nodeFactory.generateNode(nodeInfo);
		LOGGER.fine("newNode " + m + " active_node: " + current_node
				+ " nodeInfo: " + nodeInfo.getStart() + " " + nodeInfo.getEnd());
		return current_node;
	}

	public void write(int val, String messageFrom, String str) {
		if (val > 0)
			LOGGER.finest(messageFrom);
		LOGGER.finest(" " + str);
	}

	public int getPosition() {
		return this.currentPosition;
	}

	private void addSuffixLink(int node) {
		write(1, "addSuffixLink", " needSuffixLink: " + needSuffixLink
				+ " node: " + node);
		if (needSuffixLink > 0) {
			try {
				nodes[needSuffixLink].link = node;
				if (printTree)
					this.printTree("Set Suffix Link \nLinker " + needSuffixLink
							+ " Linked " + node, needSuffixLink, node, -1);
			} catch (Exception e) {
			}
		}
		needSuffixLink = node;
	}

	char active_edge() {
		return text[active_edge];
	}

	// reset active point, skip/Count trick.
	boolean walkDown(int child_node) {
		if (active_length >= nodes[child_node].edgeLength(currentPosition)) {
			active_edge += nodes[child_node].edgeLength(currentPosition);
			active_length -= nodes[child_node].edgeLength(currentPosition);
			active_node = child_node;
			LOGGER.fine("walkDown true node: " + child_node
					+ " active_length: " + active_length);
			return true;
		}
		LOGGER.fine("walkDown false node: " + child_node + " active_length: "
				+ active_length);
		return false;
	}

	private void updateActivePoint() {
		remainder--;
		/* rule 1 if active_node is root, update activePoint */
		if (active_node == root_node && active_length > 0) {
			active_length--;
			active_edge = currentPosition - remainder + 1;
			if (printTree)
				try {
					this.printTree("updateActivePoint rule 1 \nact.edge "
							+ active_edge() + " act.length " + active_length
							+ "\nactive_node(=root) " + active_node,
							active_node, -1, -1);
				} catch (Exception e) {
				}
			;

		} else /* rule 3 */
		{
			write(1, "updateActivePoint", "rule 3 reset active node: "
					+ active_node);
			active_node = nodes[active_node].link > 0 ? nodes[active_node].link
					: root_node;
			write(1, "updateActivePoint", "rule 3 new active node: "
					+ active_node);
			if (printTree)
				try {
					this.printTree("updateActivePoint rule 2 \nact.edge "
							+ active_edge() + " act.length " + active_length
							+ "\n active_node reset " + active_node,
							active_node, -1, -1);
				} catch (Exception e) {
				}
		}
	}

	public void addChar(char ch) {
		String messageFrom = "SuffixTree.addChar char " + ch;

		if (ch == '$')
			write(1, messageFrom + " : Dollar ", "$");
		text[++currentPosition] = ch;
		// reference to new created internal node;will be set to node (index)
		// after creation of internal node within a phase
		needSuffixLink = -1;
		remainder++;// one more suffix to enter in tree

		while (remainder > 0) {
			write(1, messageFrom + " :while", "active_node: " + active_node
					+ " active_edge: " + active_edge + " active_length: "
					+ active_length + " char: " + ch + " "
					+ " char active_edge: " + active_edge() + " remainder: "
					+ remainder + " currentPosition: " + currentPosition);
			LOGGER.fine("active_node start+active_length: "
					+ (nodes[active_node].getStart() + active_length));
			if (active_node != root_node)
				LOGGER.fine("edge label "
						+ text[nodes[active_node].getStart() + active_length]);
			if (active_length == 0)
				active_edge = currentPosition;
			/*
			 * rule 2: suffix link if a new internal node is created OR if an
			 * inserter is made from an internal node AND if this is NOT the
			 * first internal node in the current phase then add a suffix link
			 * from internal node to new internal node
			 */
			// no child beginning with active_edge() (char at text[active_edge])
			if (!nodes[active_node].children.containsKey(active_edge())) {
				int endposition = nodes[active_node].getNodeInfo().getEnd() - 1;
				if (endposition == 1073741822) {
					for (int k = 0; k < currentPosition; k++) {
						System.out.print(text[k]);
					}
					LOGGER.fine("Text length: " + text.length);
				}
				if ((endposition < 0) || (!(text[endposition] == '$'))) {
					// make new leaf node
					int leaf_node = newNode(currentPosition, oo,
							"new leaf put to active_node");
					// enter new node (leaf node, of course) at currentPosition
					// active_node
					write(1, messageFrom, "rule 2 char unequal; char: "
							+ active_edge() + " active_node: " + active_node
							+ " leaf_node: " + leaf_node + " endposition: "
							+ endposition);
					nodes[active_node].children.put(active_edge(), leaf_node);
					/*
					 * set suffix link if there is an internal node created in
					 * this phase.This node is referenced by the global variable
					 * needSuffixLink. needSuffixLink is set to active_node in
					 * any case
					 */
					addSuffixLink(active_node);

					if (printTree)
						this.printTree("rule 2 ch " + ch + "\n act.edge "
								+ active_edge() + " act.length "
								+ active_length + "\nnew leaf " + leaf_node
								+ " at active node " + active_node,
								active_node, leaf_node, -1);
				}
			}

			else {
				// there is an edge starting with equal char
				int child_node = nodes[active_node].children.get(active_edge());
				write(1, messageFrom, "char equal  active_node: " + active_node
						+ " active_edge: " + active_edge
						+ " char active_edge: " + active_edge()
						+ " child_node: " + child_node);
				if (walkDown(child_node)) {
					if (printTree)
						this.printTree(
								"observation 2 implicit suffix\nwalkdown(internalNode!) ch "
										+ ch + "\n act.edge " + active_edge()
										+ " act.length " + active_length
										+ "\nchild " + child_node
										+ " at active node " + active_node,
								active_node, child_node, active_length);
					continue; // observation 2
				}

				if (text[nodes[child_node].getStart() + active_length] == ch) {
					// observation 1
					write(1, messageFrom, "after...==ch: " + ch
							+ " pos in text: "
							+ (nodes[child_node].getStart() + active_length)
							+ " active_node: " + active_node + " child_node: "
							+ child_node);
					active_length++;

					if (ch == '$') {
						nodes[child_node]
								.addStartPositionOfSuffix(new TextStartPosInfo(
										unit, textNr, ++leafCount));
						active_length--;
						write(1, messageFrom, "new startposition ");
						// int x = 0;
						// if (unit > 1) x=1/0;
						// ****************************************************************
						//
						updateActivePoint();
						// active_node = nodes[active_node].link;
						// remainder--;
						// ****************************************************************
						continue;
					} else {
						addSuffixLink(active_node); // observation 3
						if (printTree)
							this.printTree("observation 3 ch " + ch
									+ " implicit \n act.edge " + active_edge()
									+ " act.length " + active_length
									+ "\nto child " + child_node
									+ " at active node " + active_node,
									active_node, child_node, active_length);
						break;
					}

				}// if (text[(text[nodes[child_node].getStart() + active_length]
					// == ch)

				/*
				 * rule 2
				 */
				// split_node is first part of splitted edge
				int split_node = newNode(nodes[child_node].getStart(), new End(
						nodes[child_node].getStart() + active_length),
						"split node");

				nodes[active_node].children.put(active_edge(), split_node);
				// leaf is child of splitted node
				int leaf_node = newNode(currentPosition, oo,
						"new leaf after split");
				nodes[split_node].children.put(ch, leaf_node);
				// reset start of child and put child as child of splitted node
				nodes[child_node].setStart(nodes[child_node].getStart()
						+ active_length);

				write(1, messageFrom,
						"terminal node after split: " + child_node + " start: "
								+ nodes[child_node].getStart() + " end: "
								+ nodes[child_node].nodeInfo.getEnd()
								+ " active_length: " + active_length);
				nodes[split_node].children.put(
						text[nodes[child_node].getStart()], child_node);
				addSuffixLink(split_node);
				if (printTree)
					this.printTree("rule 2 split edge ch " + ch
							+ "\n act.edge " + active_edge() + " act.length "
							+ active_length + "\nnew leaf " + leaf_node
							+ " at split node " + split_node, split_node,
							leaf_node, -1);

			}// end of if/else (i.e. edge starting with equal char)

			updateActivePoint();

		} // while remainder
	}// addChar

	// traverse textStr (input) from left to right and add char at pos i
	public void phases(String textStr, int start, int end,
			ExtActivePoint activePoint)  {
		write(1, "phases", " text: " + textStr.substring(start, end)
				+ " start(+activePoint.phase): " + start + " end: " + end
				+ " currentPosition: " + currentPosition);

		remainder = 0;
		if (activePoint != null) {// generalized suffix tree
			// next text as substring,start-phase is first sign of
			// substring(i.e.next text),end is last sign

			remainder = activePoint.phase;

			// **********************************

			active_edge = activePoint.active_edge;
			active_length = activePoint.active_length;// -1;
			active_node = activePoint.active_node;
			write(1, "phases activePoint", " active_node: " + active_node
					+ " active_edge: " + active_edge + "  " + active_edge()
					+ " active_length: " + active_length + " remainder: "
					+ remainder + " active_pointPhase: " + activePoint.phase);
			currentPosition = start - 1;// 13.1.-1;
			//
			LOGGER.finer(" start, end : " + nodes[active_node].getStart() + "  "
					+ nodes[active_node].getEnd(this.getPosition() + 1));
			if (nodes[active_node].getStart() >= 0)
				LOGGER.finer(" active_node Kante " + edgeString(active_node));
			for (int i = start - activePoint.phase; i < start; i++) {
				text[i] = textStr.charAt(i);
				LOGGER.fine("" + text[i]);
			}
			System.out.println();
			// System.out.print("text (phase): ");
			for (int i = 0; i < text.length; i++) {
				LOGGER.finer("Char: " + text[i]);
				if (text[i] == '$')
					System.out.println();
			}
		}

		for (int i = start; i < end; i++) {
			write(1, "phases", " textStr[i]: " + textStr.charAt(i) + " i: " + i);
			addChar(textStr.charAt(i));
		}

		LOGGER.finest("currentPosition : " + currentPosition);
		oo.setEnd(currentPosition + 1);
		LOGGER.finest("oo.End end : " + oo.getEnd());
	}

	public String edgeString(int node) {
		return new String(Arrays.copyOfRange(
				this.text,
				this.nodes[node].getStart(),
				Math.min(this.getCurrentPosition() + 1,
						this.nodes[node].getEnd(this.getPosition() + 1))));
	}
}