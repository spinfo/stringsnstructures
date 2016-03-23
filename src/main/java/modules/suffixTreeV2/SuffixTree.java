package modules.suffixTreeV2;

import java.io.PrintWriter;

// SuffixTree extends BaseSuffixTree which contains the proper Ukkonen Generalized SuffixTree algorithm.
// Here print methods are added
public class SuffixTree extends BaseSuffixTree {
	
	public SuffixTree(int length) {
		super(length);
	}


//-------------------------------------printTree-------------------------------------------------------------
	public void printTree(final PrintWriter out) {
		out.println("digraph {");
		out.println("\trankdir = LR;");
		out.println("\tedge [arrowsize=0.4,fontsize=10]");
		out.println("\tnode1 [label=\"\",style=filled,fillcolor=lightgrey,shape=circle,width=.1,height=.1];");
		out.println("//------leaves------");
		printLeaves(root, out);
		out.println("//------internal nodes------");
		printInternalNodes(root, out);
		out.println("//------edges------");
		printEdges(root, out);
		out.println("//------suffix links------");
		printSLinks(root, out);
		out.println("}");
		out.close();
	}

	private void printLeaves(int x, final PrintWriter out) {
		if (nodes[x].next.size() == 0){
			String positionlabel="";
			for (int i = 0 ; i< nodes[x].getPositionsAmount() ; i++) {
				// textNr
				positionlabel += "\n " + nodes[x].getTextNr(i) + " " +
				// anf
				nodes[x].getStart(i) + " " +	
				// end
				nodes[x].getEnd(i);
			}
			out.println("\tnode" + x + " [label=\""+x /* +"\" */ + positionlabel +"\",shape=circle]");
		}
			
		else 
			for (int child : nodes[x].next.values())
				printLeaves(child, out);
		
	}

	private void printInternalNodes(int x, final PrintWriter out) {
		if (x != root && nodes[x].next.size() > 0)
			out.println("\tnode" + x
					+ " [label=\""+x+"\",style=filled,fillcolor=lightgrey,shape=circle,width=.07,height=.07]");

		for (int child : nodes[x].next.values())
			printInternalNodes(child, out);
	}

	private void printEdges(int x, final PrintWriter out) {
		for (int child : nodes[x].next.values()) {
			out.println("\tnode" + x + " -> node" + child + "[label=\"" + edgeString(child) + "\",weight=3]");
			printEdges(child, out);
		}
	}

	private void printSLinks(int x, final PrintWriter out) {
		if (nodes[x].link > 0)
			out.println("\tnode" + x + " -> node" + nodes[x].link + " [label=\"\",weight=1,style=dotted]");
		for (int child : nodes[x].next.values())
			printSLinks(child, out);
	}

	
	
} // class st	

