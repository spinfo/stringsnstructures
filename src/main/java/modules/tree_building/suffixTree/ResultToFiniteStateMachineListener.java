package modules.tree_building.suffixTree;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.util.logging.Logger;

import com.google.gson.stream.JsonWriter;

import modules.OutputPort;
import modules.transitionNetwork.TransitionNetwork1;
import modules.transitionNetwork.elements.StateElement;
import modules.transitionNetwork.elements.StateTransitionElement;
import modules.transitionNetwork.elements.SuffixElement;

/*
// the separator is exposed read-only
public static final String SEPARATOR = "|";

private final BaseSuffixTree tree;

private final OutputPort out;

private final Stack<String> edges;

public ResultEdgeSegmentsListener(BaseSuffixTree tree, OutputPort out) {
	this.tree = tree;
	this.out = out;
	this.edges = new Stack<String>();
}
*/

/**
 * On the entry action, the current edge string is pushed on the edges'
 * stack. This ensures, that on the exit action the full path up to the
 * (then) current node will be present on the stack.
 * 
 * NOTE: This assumes depth-first traversal of the tree in the tree walker.
 * 
 * @param nodeNr
 *            the node whose edge is to be pushed on the stack.
 * @param level
 *            (irrelevant here, required by interface)
 * /
@Override
public void entryaction(int nodeNr, int level) throws IOException {
	if (!(this.tree.getRoot() == nodeNr)) {
		this.edges.push(this.tree.edgeString(nodeNr));
	}
}
*/
/**
 * On the exit action the edges' stack is assumed to contain all edges up to
 * the current node in the tree. If the current node is a terminal node and
 * the path up to this point is a full input text (not a suffix) the edges
 * of the path are written to output separate by
 * <code>this.class.SEPARATOR</code>.
 * 
 * NOTE: This assumes depth-first traversal of the tree in the tree walker.
 * 
 * @param nodeNr
 *            the node whose edges are to be printed.
 * @param level
 *            (irrelevant here, required by interface)
 */

/*
@Override
public void exitaction(int nodeNr, int level) throws IOException {
	// ignore root
	if (tree.getRoot() == nodeNr)
		return;

	final Node node = tree.getNode(nodeNr);

	if (node.isTerminal()) {
		final String path = String.join("", edges);
		// check that the node is not only terminal but represents at least
		// one full input text (i.e. is not a suffix)
		for (int i = 0; i < node.getPositionsAmount(); i++) {
			// string equality checking of the path at the terminal node
			// is necessary because for the starting (inner) nodes only
			// one position's textNr is noted. Thus there is no way to
			// simply check for integer equality of the text's begin and the
			// starting node's begin
			if (path.equals(tree.getInputText(node.getTextNr(i)))) {
				// actually write the output
				out.outputToAllCharPipes(String.join(SEPARATOR, edges) + System.lineSeparator());
				break;
			}
		}
	}

	edges.pop();
}

*/

/**
 * Allows to check if processing of the tree has finished. (Simply checks if
 * each edge that was pushed to the stack was popped as well.)
 */

// @return true or false




public class ResultToFiniteStateMachineListener extends AbstractResultNodeStackListener {

	private static final Logger LOGGER = Logger.getLogger(ResultToJsonListener.class.getName());

	private static final String ENCODING = java.nio.charset.StandardCharsets.UTF_8.name();

	// the OutputPort to write to
	private final OutputPort outputPort;

	// the suffix tree this will work on
	private final BaseSuffixTree tree;
	

	// variables needed internally to write the Json representation
	private final ByteArrayOutputStream outputStream;
	private JsonWriter writer;
	private boolean wroteBegin = false;
	
	private boolean inverted= true;
	
	private TransitionNetwork1 tn;

	private Stack<Integer> nodeNrs=null;
	private boolean afterBacktrack=true;
	private int nodeNrsStackPos;
	
	private int lengthOfPath;

	
	

	public ResultToFiniteStateMachineListener(BaseSuffixTree suffixTree, OutputPort outputPort) {
		super(suffixTree);

		this.tree = suffixTree;
		this.outputPort = outputPort;
		this.nodeNrs=new Stack<Integer>();
		this.tn=new TransitionNetwork1(suffixTree.text,this.inverted);

		this.outputStream = new ByteArrayOutputStream();
		try {
			this.writer = new JsonWriter(new OutputStreamWriter(this.outputStream, ENCODING));
		} catch (UnsupportedEncodingException e) {
			LOGGER.severe("Encoding " + ENCODING + "not supported.");
			e.printStackTrace();
		}
		this.writer.setIndent("  ");
		
	}
	
	public void setTN(TransitionNetwork1 tn){
		this.tn=tn;
	}
	
	public TransitionNetwork1 getTN(){
		return this.tn;
	}
	
	public void setInverted(boolean inverted){
		this.inverted=inverted;
	}
	
	@Override
	public void entryaction(int nodeNr, int level) throws IOException {
		
		//if (!(this.tree.getRoot() == nodeNr)) {
			if (this.afterBacktrack){
				this.afterBacktrack=false;
				nodeNrsStackPos=this.nodeNrs.size()-1;
			}
			this.nodeNrs.push(nodeNr);
			this.lengthOfPath=this.lengthOfPath+tree.getNode(nodeNr).getEnd(0)-
					tree.getNode(nodeNr).getStart(0);	
		//}
	}

	/**
	 * On the exit action the edges' stack is assumed to contain all edges up to
	 * the current node in the tree. If the current node is a terminal node and
	 * the path up to this point is a full input text (not a suffix) the edges
	 * of the path are written to output separate by
	 * <code>this.class.SEPARATOR</code>.
	 * 
	 * NOTE: This assumes depth-first traversal of the tree in the tree walker.
	 * 
	 * @param nodeNr
	 *            the node whose edges are to be printed.
	 * @param level
	 *            (irrelevant here, required by interface)
	 */
	 
	@Override
	public void exitaction(int nodeNr, int level) throws IOException {
		this.afterBacktrack=true;
		// ignore root
		//if (tree.getRoot() == nodeNr)
		//	return;
		
		final Node node = tree.getNode(nodeNr);

		if (node.isTerminal()) {
			
			//final String path = String.join("", edges);
			// check that the node is not only terminal but represents at least
			// one full input text (i.e. is not a suffix)
			for (int i = 0; i < node.getPositionsAmount(); i++) {
				// string equality checking of the path at the terminal node
				// is necessary because for the starting (inner) nodes only
				// one position's textNr is noted. Thus there is no way to
				// simply check for integer equality of the text's begin and the
				// starting node's begin
				
				//process(nodeNr,null,0,level);
				/*
				if (path.equals(tree.getInputText(node.getTextNr(i)))) {
					// actually write the output
					out.outputToAllCharPipes(String.join(SEPARATOR, edges) + System.lineSeparator());
					break;
			
				}
				*/
				
			}
			process(nodeNr,null,0,level);
			
		}
		
		this.lengthOfPath=this.lengthOfPath- (tree.getNode(nodeNr).getEnd(0)-
				tree.getNode(nodeNr).getStart(0));	
		this.nodeNrs.pop();
	}

	
	

	@Override
	public void process(int nodeNr, List<Node> path, int pathLength, int level) throws IOException {
		
		if (!this.wroteBegin) {
			writeBegin();
		}


		// get the node and label in question
		final Node node = tree.getNode(nodeNr);
		
		final String label = tree.edgeString(node);
		/*
		System.out.println("node: "+nodeNr);
		System.out.println("label: "+label);
		
		*/
		
		if (node.isTerminal()) {
			
			
			// path starts with first sign of text, i.e. whole word
			// to Do PositionAmounts, e.g. aufhören$ hören$
			if (node.getEnd(0)==this.tree.getTextBegin(node.getTextNr(0))+
					this.lengthOfPath)
				
			{
				System.out.println("whole word");
				System.out.print("leave: "+nodeNr+"  ");
				System.out.println("label: "+label);
				
				// write Node Stack
				// write edge label stack
				// inversion for right left suffix trees
					
			
				// stack 
				
				int nodeIndex=0;
				ListIterator<Integer> it = nodeNrs.listIterator(this.nodeNrsStackPos);
				if (it.hasNext()) nodeIndex=it.next();
				// do not repeat already written nodes
				System.out.println("process nodeIndex: "+nodeIndex+"  "+"nodeNrsStackPos: "+
				this.nodeNrsStackPos);
				//while((nodeIndex !=this.entryActionNewNodeNr)&& (it.hasNext())) nodeIndex=it.next();
				//for (ListIterator<Integer> it = nodeNrs.listIterator(0); it.hasNext(); )
				while (it.hasNext())
				{
					
					//int nodeIndex=it.next();
					System.out.println("mother: "+nodeIndex+"  "+tree.edgeString(tree.getNode(nodeIndex))
					);
					
					// node(Index) in stateList? insert, if not
					
					
					int posInStates=
					this.tn.addStateElement(new StateElement(nodeIndex));
					System.out.println("posInStates: "+posInStates);
					StateElement stateElement= (StateElement)this.tn.states.get(posInStates);
					System.out.println("PosInStateElementList:" +posInStates);
					// get children (follow states) and suffix strings which lead to them
					int childNr=it.next();
					/* for (int childNr : this.tree.nodes[nodeIndex].next.values()) { */
						// generate StateTransitionElement
						StateTransitionElement stateTransitionElement= new StateTransitionElement();
						int childPosInStateElementList=
						this.tn.addStateElement(new StateElement(childNr));
						// follow state, transition in next state
						stateTransitionElement.toStateElement=childPosInStateElementList;
						// suffixes (?? for all PositionAmounts (???, TODo)
						int suffixStart=this.tree.nodes[childNr].getStart(0);
						int suffixEnd =this.tree.nodes[childNr].getEnd(0);
						
						
						SuffixElement suffixElement=new SuffixElement(suffixStart, suffixEnd);
						int posInSuffixes=
						this.tn.addSuffixElement(suffixElement);
						stateTransitionElement.toSuffixElement=posInSuffixes;
						// append in toStateTransitions in (mother)State element
						/*int posInStateTransitions=*/
						stateElement.toStateTransitions.add/*StateTransitionElement*/(stateTransitionElement);
						
					/* }*/

						nodeIndex=childNr;
				}// for (ListIterator<Integer> it ...
					
				tn.writeTN();
				
			}
			
		}
		//writer.endArray();

		/*writer.name("frequency").value(frequency);*/

		// end writing of the NodeRepresentation and flush everything to the
		// output port
		//writer.endObject();
		//this.flushOutput();
	}

	

	
	
	/**
	 * Finalises writing of the SuffixTreeRepresentation, closes all ports
	 * connected and/or used internally.
	 * 
	 * @throws IOException
	 *             on error
	 */
	public void finishWriting() throws IOException {
		// finalise the node array and object begun in writeBegin()
		this.writer.endArray();
		this.writer.endObject();

		// write everything out
		this.flushOutput();

		// close all opened streams and ports
		this.outputStream.close();
		this.writer.close();
		this.outputPort.close();
	}

	/**
	 * Writes the beginning of the tree and notes that it has already been
	 * written, ensuring that this operation is only called once in the life
	 * cycle of this object
	 * 
	 * @throws IOException
	 *             on error
	 */
	private void writeBegin() throws IOException {
		if (wroteBegin) {
			return;
		}

		writer.beginObject();
		writer.name("unitCount").value(tree.getTypeContextsAmount());
		writer.name("nodeCount").value(tree.getNodeAmount());
		writer.name("nodes");
		writer.beginArray();
		flushOutput();

		wroteBegin = true;
	}

	/**
	 * used internally to output changes in the writer's stream to the
	 * outputPort
	 * 
	 * @throws IOException
	 */
	private void flushOutput() throws IOException {
		writer.flush();
		final String jsonOut = outputStream.toString(ENCODING);
		outputPort.outputToAllCharPipes(jsonOut);
		outputStream.reset();
	}
	
	public boolean hasCompleted() {
		//return edges.empty();
		return true;
	}


}



