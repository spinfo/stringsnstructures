package modules.suffixTree.applications;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.logging.Logger;

import modules.suffixTree.SuffixTree;
import modules.suffixTree.node.ExtActivePoint;
import modules.suffixTree.node.nodeFactory.NodeFactory;

public class SuffixTreeAppl extends SuffixTree {

	private static final Logger LOGGER = Logger.getGlobal();
	// .getLogger(SuffixTreeAppl.class.getName());

	PrintWriter out;
	private int nr = 0;

	public SuffixTreeAppl(int len, NodeFactory nodeFactory) {
		super(len, nodeFactory);
	}

	// returns searchResult null if not found, 
	// 1 if found, 2 if found at end of edge, 3 if found at end of text before
	// '$'
	public SearchResult search(String pattern, int posInPattern, int node) {
		if (pattern.length() == 0) {			
			return new SearchResult(node,1);
		}
		else if (!nodes[node].children.containsKey(pattern.charAt(posInPattern)))
			return null;
		else {
			int child_node = this.nodes[node].children.get(pattern.charAt(posInPattern));
			
			write(1, "SuffixTreeAppl.search ", "search pattern: " + pattern
					+ " child_node: " + child_node + " start: "
					+ this.nodes[child_node].getStart() + " end: "
					+ this.nodes[child_node].getNodeInfo().getEnd() + " posInPattern: "
					+ posInPattern);
			// this.nodes[child_node].getEnd(this.getPosition()));
			for (int i = this.nodes[child_node].getStart(); i < this.nodes[child_node]
					.getNodeInfo().getEnd(); i++) {
				LOGGER.info(this.text[i] + " " + pattern.charAt(posInPattern));
				if (this.text[i] == pattern.charAt(posInPattern)) {
					posInPattern++;
					if (posInPattern >= pattern.length())
						if (i+1==this.nodes[child_node].getNodeInfo().getEnd())
							return new SearchResult(child_node,2);
						else if (this.text[i+1]=='$')
							return new SearchResult(child_node,3);
						else return new SearchResult(child_node,1);
				} else {
					LOGGER.info("" + pattern.charAt(posInPattern));
					return null;
				}
			}// for
			LOGGER.fine(pattern.substring(posInPattern));
			// recursion
			return search(pattern, posInPattern, child_node);

		}

	}

	// control active_edge!!

	public ExtActivePoint longestPath(String str, int phase, int node,
			int activeEdgeInConcatinatedText,
			// flag for longest path in generalized suffix tree, if set, do not
			// return
			// terminal node but preterminal node, see return 2
			boolean generalizedSuffixTree)

	{
		int active_length = 0;
		if (str.length() == 0)
			return null;
		else if (!nodes[node].children.containsKey(str.charAt(phase))) {

			LOGGER.warning("ExtActivePoint return 1 no fitting child");
			return new ExtActivePoint(node, activeEdgeInConcatinatedText/*
																		 * this.
																		 * nodes
																		 * [
																		 * node]
																		 * .
																		 * getEnd
																		 * (
																		 * this.
																		 * getPosition
																		 * ())
																		 * active_edge
																		 */,
					0/* active_length */, phase);
		} else {

			int child_node = this.nodes[node].children.get(str.charAt(phase));

			LOGGER.finer("SuffixTreeAppl.longestPath child_node: " + child_node
					+ " start " + this.nodes[child_node].getStart() + " end  "
					+ this.nodes[child_node].getEnd(this.getPosition())
					+ " phase " + phase + " activeEdgeInConcatinatedText: "
					+ activeEdgeInConcatinatedText);
			write(1,
					"SuffixTreeAppl.longestPath",
					"str: " + str + " child_node: " + child_node + " start: "
							+ this.nodes[child_node].getStart() + " end: "
							+ this.nodes[child_node].getEnd(this.getPosition())
							+ " uebereinstimmender kantenlabel: "
							+ str.substring(0, phase + 1));

			for (int i = this.nodes[child_node].getStart(); i < this.nodes[child_node]
					.getEnd(this.getPosition() + 1); i++) {
				/* char in edge equal to char in edge */
				if (this.text[i] == str.charAt(phase)) {
					LOGGER.finest("" + this.text[i]);
					phase++;
					if (phase >= str.length()) {
						write(1,
								"ExtActivePoint return 2 SuffixTreeAppl.longestPath 1:",
								" node: " + node + " active_length: "
										+ active_length + " i: " + i
										+ " phase: " + phase + "str: " + str);

						if (generalizedSuffixTree) {
							// return preterminal node
							int len = this.nodes[child_node].getEnd(this
									.getPosition() + 1)
									- this.nodes[child_node].getStart();
							return new ExtActivePoint(node,
									activeEdgeInConcatinatedText - len/*
																	 * this.nodes
																	 * [
																	 * child_node
																	 * ]
																	 * .getStart
																	 * ()
																	 */,
									0 /* active_length */, phase - len);

						}// if (generalizedSuffixTree)
						else
							return
							// new
							// ExtActivePoint(child_node,active_length,0,phase);
							// new
							// ExtActivePoint(child_node,i,active_length,phase);
							// new
							// ExtActivePoint(node,child_node,active_length,phase);
							new ExtActivePoint(child_node,
									activeEdgeInConcatinatedText/*
																 * this.nodes[
																 * child_node
																 * ].getStart()
																 */,
									active_length, phase);
					}
					active_length++;

				} /* char in edge not equal to char in edge */
				else {
					{
						write(1,
								"ExtActivePoint return 3 SuffixTreeAppl.longestPath 2: ",
								" node: " + node + " active_length: "
										+ active_length + " i: " + i
										+ " phase: " + phase);

						return new ExtActivePoint(/* child_ */node,
								activeEdgeInConcatinatedText/*
															 * this.nodes[child_node
															 * ].getStart()
															 */, active_length,
								phase);
					}
				}
			}// for
			LOGGER.fine("\nSuffixTreeAppl.longestPath vor Rekursion: "
					+ str.substring(0, phase + 1)
					+ " activeEdgeInConcatinatedText: "
					+ activeEdgeInConcatinatedText + " phase: " + phase);
			// neu 13-12-27
			// if (!nodes[node].children.containsKey(str.charAt(phase)))
			// return new
			// ExtActivePoint(node,this.nodes[node].getStart(),active_length,phase);
			// recursion
			return longestPath(str, phase, child_node,
					activeEdgeInConcatinatedText + active_length,
					generalizedSuffixTree);

		}
	}

	public void printText() {
		// print text (array of char, s.a.)
		for (int i = 0; i <= this.getCurrentPosition(); i++) {
			LOGGER.fine("" + text[i]);
		}
	}

	// *************************************** Print Suffix Tree
	// **************************

	/*
	 * printing the Suffix Tree in a format understandable by graphviz. The
	 * output is written into st.dot file. In order to see the suffix tree as a
	 * PNG image, run the following command: dot -Tpng -O st.dot
	 */

	@Override
	public void printTree(String message, int hotNode1, int hotNode2,
			int active_length) {
		LOGGER.entering(this.getClass().getName(), "printTree", new Object[] {
				hotNode1, hotNode2, active_length });
		try {
			out = new PrintWriter(new FileWriter("st" + String.valueOf(nr)
					+ ".dot"));
			nr++;
			LOGGER.finer("printTree nr " + nr);
			out.println("digraph {");
			out.println("\trankdir = TB;");
			out.println("\tedge [arrowsize=0.4,fontsize=10]");

			out.println("\tmessage[label=\"" + message
					+ "\",shape=box,color=white]");
			if (hotNode1 == 1)
				out.println("\tnode1 [label=\"1\",style=filled,fillcolor=red,shape=circle,width=.6,height=.6];");
			else
				out.println("\tnode1 [label=\"1\",style=filled,fillcolor=lightgrey,shape=circle,width=.6,height=.6];");
			out.println("//------leaves------");
			printLeaves(this.getRoot(), hotNode1, hotNode2);
			out.println("//------internal nodes------");
			printInternalNodes(this.getRoot(), hotNode1, hotNode2);
			out.println("//------edges------");
			out.println("\tmessage -> node1 [color=white]");
			printEdges(this.getRoot(), hotNode1, hotNode2, active_length);
			out.println("//------suffix links------");
			printSuffixLinks(this.getRoot(), hotNode1, hotNode2);
			out.println("}");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.exiting(this.getClass().getName(), "printTree");
	}

	void printLeaves(int x, int hotNode1, int hotNode2) {
		if (this.nodes[x].children.size() == 0) {

			out.print("\tnode" + x + " [label=\"" + x + "\n");
			this.nodes[x].printStartPositionInformation(out);
			if ((x == hotNode1) || (x == hotNode2))
				out.println("\",style=filled,shape=circle,fillcolor=red,width=.6,height=.6]");
			else
				out.println("\",shape=circle,width=.6,height=.6]");

		} else {
			for (int child : this.nodes[x].children.values())
				printLeaves(child, hotNode1, hotNode2);
		}
	}

	void printInternalNodes(int x, int hotNode1, int hotNode2) {
		if (x != this.getRoot() && this.nodes[x].children.size() > 0)
			if ((x == hotNode1) || (x == hotNode2))
				out.println("\tnode"
						+ x
						+ " [label=\""
						+ x
						+ "\",style=filled,fillcolor=red,shape=circle,width=.6,height=.6]");
			else
				out.println("\tnode"
						+ x
						+ " [label=\""
						+ x
						+ "\",style=filled,fillcolor=lightgrey,shape=circle,width=.6,height=.6]");

		for (int child : this.nodes[x].children.values())
			printInternalNodes(child, hotNode1, hotNode2);
	}

	void printEdges(int x, int hotNode1, int hotNode2, int active_length) {
		for (int child : this.nodes[x].children.values()) {
			if ((x == hotNode1) && (child == hotNode2)) {
				String edgeString = "";
				if (active_length <= 0)
					edgeString = edgeString(child);
				else
					edgeString = edgeString(child).substring(0, active_length)
							+ "*"
							+ edgeString(child).substring(active_length,
									edgeString(child).length());
				out.println("\tnode" + x + " -> node" + child + " [label=\""
						+ edgeString/* (child) */+ "\",weight=3,color=red]");
			} else
				out.println("\tnode" + x + " -> node" + child + " [label=\""
						+ edgeString(child) + "\",weight=3]");
			printEdges(child, hotNode1, hotNode2, active_length);
		}
	}

	void printSuffixLinks(int x, int hotNode1, int hotNode2) {
		if (this.nodes[x].link > 0)
			if (((x == hotNode1) && (this.nodes[x].link == hotNode2))
					|| ((x == hotNode2) && (this.nodes[x].link == hotNode1)))
				out.println("\tnode" + x + " -> node" + this.nodes[x].link
						+ " [label=\"\",weight=1,style=dotted,color=red]");
			else
				out.println("\tnode" + x + " -> node" + this.nodes[x].link
						+ " [label=\"\",weight=1,style=dotted]");
		for (int child : this.nodes[x].children.values())
			printSuffixLinks(child, hotNode1, hotNode2);
	}

}
