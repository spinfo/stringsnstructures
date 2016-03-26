package modules.suffixTreeV2;

import java.util.Arrays;

/*
 Description, source base and comments see 
 http://stackoverflow.com/questions/9452701/ukkonens-suffix-tree-algorithm-in-plain-english
 */

public class BaseSuffixTree {
	
	static final int oo = Integer.MAX_VALUE / 2;
	static int position=-1;
	Node[] nodes;
	char[] text;
	int root, currentNode, needSuffixLink, remainder;

	// The Suffix tree can be used to note type context numbers in addition
	// to positions for every node.
	public static final int NO_TYPE_CONTEXT = -1;
	private int currentTypeContext = NO_TYPE_CONTEXT;
	
	int active_node, active_length, active_edge;
	
	public BaseSuffixTree(int length) {
		nodes = new Node[2 * length + 2];
		text = new char[length];
		root = active_node = newNode(-1, -1, 0, NO_TYPE_CONTEXT);

		// reset position in case there was a tree created earlier
		// TODO: change position to a non-static variable to allow 
		// 		 for multiple instances of this class
		position = -1;
	}
	
	int newNode(int start, int end, int nrText, int typeContextNr) {
		nodes[++currentNode] = new Node(start, end, nrText, typeContextNr);
		return currentNode;
	}

	private void addSuffixLink(int node) {
		if (needSuffixLink > 0)
			nodes[needSuffixLink].link = node;
		needSuffixLink = node;
	}

	char active_edge() {
		return this.text[active_edge];
	}

	boolean walkDown(int next) {
		if (active_length >= nodes[next].edgeLength()) {
			active_edge += nodes[next].edgeLength();
			active_length -= nodes[next].edgeLength();
			active_node = next;
			return true;
		}
		return false;
	}

	// if end of text is reached ('$') and last suffix is implicitly contained in previously built suffix
	// tree (e.g. given two texts aaabxy$aaazxy$, last suffix is xy$, here the (existing) suffixes
	// y$ and & must be counted in suffix tree
	public void addRemaining(int textNr){
		int pos=0;
		int next=this.root;

		for (int i=position-remainder+1;i<=position;i++){
			pos=i;
			while (pos<=position) {
				if (!nodes[next].next.containsKey(this.text[pos]))  {
					throw new IllegalStateException("addRemaining Error");
				}
				else {
					next = nodes[next].next.get(this.text[pos]);
					pos+=nodes[next].edgeLength();
				}
			
			}// while
			if (nodes[next].isTerminal()){
				nodes[next].addPos(pos-nodes[next].edgeLength(), this.oo, textNr, currentTypeContext);
			}
			next=this.root;
		}// for
	}

	public void addChar(char ch, int nrText) throws Exception {
		this.text[++position] = ch;
		needSuffixLink = -1;
		remainder++;
		while (remainder > 0) {
			if (active_length == 0)
				active_edge = position;
			if (!nodes[active_node].next.containsKey(active_edge())) {
				int leaf = newNode(position, oo, nrText, currentTypeContext);
				nodes[active_node].next.put(active_edge(), leaf);
				addSuffixLink(active_node);  
				/* rule 2:
				If we create a new internal node OR make an inserter from an internal node, 
				and this is not the first SUCH internal node at current step, 
				then we link the previous SUCH node with THIS one through a suffix link.
				*/
			} else {
				int next = nodes[active_node].next.get(active_edge());
				if (walkDown(next)) {
					continue; /* observation 2:
						If at some point active_length is greater or equal to the length of 
						current edge (edge_length), we move our active point down 
						until edge_length is not strictly greater than active_length.
					*/
				}
				if (this.text[nodes[next].getStart(0) + active_length] == ch) { 
					// end of text, for further texts in GST
					if (ch=='$') {
						if (nodes[next].isTerminal()){
							addRemaining(nrText);
						}// if  ..isTerminal
						else {
							throw new IllegalStateException("error in addChar terminal");
						}
					}// if (ch=='$')
					/* observation 1:
					 	When the final suffix we need to insert is found to exist in the tree already, 
					 	the tree itself is not changed at all (we only update the active point and remainder).
					 */
					active_length++;
					addSuffixLink(active_node);
					/*observation 3:
					 When the symbol we want to add to the tree is already on the edge, 
					 we, according to Observation 1, update only active point and remainder, 
					 leaving the tree unchanged. BUT if there is an internal node marked as needing suffix link, 
					 we must connect that node with our current active node through a suffix link.
					 */

					break;
				}
				int split = newNode(nodes[next].getStart(0), nodes[next].getStart(0) + active_length, nrText, currentTypeContext);
				nodes[active_node].next.put(active_edge(), split);

				int leaf = newNode(position, oo, nrText, currentTypeContext);
				nodes[split].next.put(ch, leaf);
				nodes[next].updateStartPositions(active_length);

				nodes[split].next.put(this.text[nodes[next].getStart(0)], next);
				addSuffixLink(split); 
				/* rule 2:
				 If we create a new internal node OR make an inserter from an internal node, 
				and this is not the first SUCH internal node at current step, 
				then we link the previous SUCH node with THIS one through a suffix link.
				*/
				 
			}
			remainder--;

			if (active_node == root && active_length > 0) { 
				/* rule 1:
				After an insertion from root, the active length is greater than 0:
			    active_node remains root
			    active_edge is set to the first character of the new suffix we need to insert, i.e. b
			    active_length is reduced by 1
				 */
				active_length--;
				active_edge = position - remainder + 1;
			} else
				active_node = nodes[active_node].link > 0 ? nodes[active_node].link : root; 
				/*rule 3:
				After an insert from the active node which is not the root node, 
				we must follow the suffix link and set the active node to the node it points to. 
				If there is no a suffix link, set the active node to the root node.
				 Either way, active edge and active length stay unchanged.
				 */
		}// while remainder
	}// addChar

	// get the edge string of a node by node number
	public String edgeString(int node) {
		return edgeString(nodes[node]);
	}
	
	// get the edge string of a node
	public String edgeString(Node node) {
		int end=node.getEnd(0);
		if (end==oo) {
			for (end=node.getStart(0);end<=oo;end++) {
				if (this.text[end]=='$') {end++; break;}
			}
		}
		return new String(Arrays.copyOfRange(this.text, node.getStart(0), Math.min(position + 1,end)));
	}
	

	void setActivePoint(int node, int active_edge,int active_length){
		this.active_node=node;
		this.active_edge=active_edge;
		this.active_length=active_length;
		//------------------------
		//active_edge++;
	}
	
	
	//jr
	int longestPath(String nextText,int node/*root*/){
		int localActiveEdge=0;int i=0;
		for (i=0;i<nextText.length();i++){
			// find edge
			if (nodes[node].next.containsKey(nextText.charAt(i))){
				int child_node = this.nodes[node].next.get(nextText.charAt(i));
				localActiveEdge=0;
				// compare edge
				int pos=i+1;// pos is index for position in nextText
				for (int j=this.nodes[child_node].getStart(0)+1;j<this.nodes[child_node].getEnd(0);j++){
					if (this.text[j]==nextText.charAt(pos)) {
						pos++;
						localActiveEdge++;
					}
					else {
						setActivePoint(node,this.nodes[child_node].getStart(0),
								j-this.nodes[child_node].getStart(0));
						return pos;
					};
					
				} // for
				i=pos-1;
				node=child_node;// next node (child)
			}
			else {
				setActivePoint(node,0,0);
				return i;
			}
		} // for
		setActivePoint(node,nodes[node].getStart(0),localActiveEdge);
		return i;
	} // longestPath
	
	// TODO: implement, mostly like longestpath
	boolean findPattern(String pattern, int node /*root*/){
		return false;
	}
	
	// return the root nodes node nr
	public int getRoot() {
		return root;
	}
	
	// return the node corresponding to nodeNr
	public Node getNode(int nodeNr) {
		try {
			return this.nodes[nodeNr];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	
	// return the number of nodes in the tree
	public int getNodeAmount() {
		return currentNode;
	}
	
	// return the number of the current type context
	public int getCurrentTypeContext() {
		return currentTypeContext;
	}
	
	// return the whole input as a string, do not expose the underlying char[]
	public String getText() {
		return new String(text);
	}
	
	// Increment or initialise the current type context number.
	// This value will be used when node's positions are assigned to
	// mark the type context.
	public int incrementTypeContext() {
		if(currentTypeContext == NO_TYPE_CONTEXT) {
			currentTypeContext = 0;
		} else {
			currentTypeContext += 1;
		}
		return currentTypeContext;
	}

} // class st	


