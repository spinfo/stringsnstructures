package modules.suffixTreeV2;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


// SuffixTree extends BaseSuffixTree which contains the proper Ukkonen Generalized SuffixTree algorithm.
// Here print methods are added
public class SuffixTree extends BaseSuffixTree {
	
	public SuffixTree(int length) {
		super(length);
	}


//-------------------------------------printTree-------------------------------------------------------------
	public void printTree() {
		try {
				out = new PrintWriter(new FileWriter("st.dot"));
		    } catch (IOException e) {
		        e.printStackTrace();
		}
	
		out.println("digraph {");
		out.println("\trankdir = LR;");
		out.println("\tedge [arrowsize=0.4,fontsize=10]");
		out.println("\tnode1 [label=\"\",style=filled,fillcolor=lightgrey,shape=circle,width=.1,height=.1];");
		out.println("//------leaves------");
		printLeaves(root);
		out.println("//------internal nodes------");
		printInternalNodes(root);
		out.println("//------edges------");
		printEdges(root);
		out.println("//------suffix links------");
		printSLinks(root);
		out.println("}");
		out.close();
	}

	void printLeaves(int x) {
		if (nodes[x].next.size() == 0){
			String positionlabel="";
			for (int i=0;i<nodes[x].positionList.size();i=i+3) {
				// nrText
				positionlabel=positionlabel+"\n "+nodes[x].positionList.get(i+2).val+" "+
				// anf
				nodes[x].positionList.get(i).val+" "+	
				// end
				nodes[x].positionList.get(i+1).val;
				
			}
			out.println("\tnode" + x + " [label=\""+x /* +"\" */ + positionlabel +"\",shape=circle]");
			
		}
			
		else 
			for (int child : nodes[x].next.values())
				printLeaves(child);
		
	}

	void printInternalNodes(int x) {
		if (x != root && nodes[x].next.size() > 0)
			out.println("\tnode" + x
					+ " [label=\""+x+"\",style=filled,fillcolor=lightgrey,shape=circle,width=.07,height=.07]");

		for (int child : nodes[x].next.values())
			printInternalNodes(child);
	}

	void printEdges(int x) {
		for (int child : nodes[x].next.values()) {
			out.println("\tnode" + x + " -> node" + child + "[label=\"" + edgeString(child) + "\",weight=3]");
			System.out.println("\tnode" + x + " -> node" + child + " " + nodes[child].getTextNr(0) + " [label=\""
					+ edgeString(child) + "\",weight=3]");
			printEdges(child);
		}
	}

	void printSLinks(int x) {
		if (nodes[x].link > 0)
			out.println("\tnode" + x + " -> node" + nodes[x].link + " [label=\"\",weight=1,style=dotted]");
		for (int child : nodes[x].next.values())
			printSLinks(child);
	}

	
	
} // class st	

