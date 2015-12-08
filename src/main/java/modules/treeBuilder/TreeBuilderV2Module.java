package modules.treeBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.Pipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.ParentRelationTreeNode;
import common.ParentRelationTreeNodeImpl;
import common.parallelization.CallbackReceiver;

public class TreeBuilderV2Module extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_INPUTDELIMITER = "Input delimiter";
	public static final String PROPERTYKEY_MAXDEPTH = "Tree depth";
	//public static final String PROPERTYKEY_MAXTHREADS = "Max. threads";
	
	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "input";
	private static final String ID_OUTPUT = "output";
	
	// Local variables
	private String inputDelimiter;
	private int maxDepth;
	//private int maxThreads;

	public TreeBuilderV2Module(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("TreeBuilder v2 module. Can process larger datasets more quickly. Replaces AtomicRangeSuffixTrieBuilder and TreeBuilder.");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_INPUTDELIMITER, "Regular expression to use as segmentation delimiter for the input; leave empty for char-by-char segmentation.");
		this.getPropertyDescriptions().put(PROPERTYKEY_MAXDEPTH, "Maximum depth for the resulting tree; set to -1 for no constraint.");
		//this.getPropertyDescriptions().put(PROPERTYKEY_MAXTHREADS, "Maximum number of threads to run concurrently.");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "TreeBuilder v2 Module"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_INPUTDELIMITER, "[\\s]+");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MAXDEPTH, "-1");
		//this.getPropertyDefaultValues().put(PROPERTYKEY_MAXTHREADS, "4");
		
		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~).
		 * Every port can support a range of pipe types (currently
		 * byte or character pipes). Output ports can provide data
		 * to multiple pipe instances at once, input ports can
		 * in contrast only obtain data from one pipe instance.
		 */
		InputPort inputPort = new InputPort(ID_INPUT, "Plain text character input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "Plain text character output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		
	}

	@Override
	public boolean process() throws Exception {
		
		// Construct scanner instances for input segmentation
		Scanner inputScanner = new Scanner(this.getInputPorts().get(ID_INPUT).getInputReader());
		inputScanner.useDelimiter(this.inputDelimiter);
		
		// Initialise trie root node
		ParentRelationTreeNode rootNode = new ParentRelationTreeNodeImpl("^", null);
		if (inputScanner.hasNext()) // Root node counter has to be set to one from start if there is any input 
			rootNode.setNodeCounter(1);
		
		// Initialise leaf list
		List<ParentRelationTreeNode> leafList = new ArrayList<ParentRelationTreeNode>();
		leafList.add(rootNode);
		
		// Input read loop
		while (inputScanner.hasNext()){
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				inputScanner.close();
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// Determine next segment
			String inputSegment = inputScanner.next();
			
			/*
			 * Each segment is to be attached as a child to each tree node
			 * of last loop's leaf list (or increase the counter of the
			 * respective child node should it already exist) and then add
			 * the created/found child node to next loop's list.
			 */
			
			List<ParentRelationTreeNode> nextLeafList = new ArrayList<ParentRelationTreeNode>(leafList.size()+1);
			nextLeafList.add(rootNode);
			
			Iterator<ParentRelationTreeNode> leaves = leafList.iterator();
			while (leaves.hasNext()){
				// Determine next node
				ParentRelationTreeNode node = leaves.next();
				
				// Get child node for the read segment
				ParentRelationTreeNode childNode = (ParentRelationTreeNode) node.getChildNodes().get(inputSegment);
				
				// Check if child node does not yet exist (and if so, create it)
				if (childNode == null){
					childNode = new ParentRelationTreeNodeImpl(inputSegment, node);
					node.getChildNodes().put(inputSegment, childNode);
					
					// Increment child node counter
					childNode.incNodeCounter();
					
					// If there is a new split, increment ancestor node counters
					ParentRelationTreeNode ancestor = node;
					if (ancestor.getChildNodes().size() > 1)
						while (ancestor != null) {
							ancestor.incNodeCounter();
							ancestor = ancestor.getParentNode();
						}
				}
				
				// Apply max depth constraint if specified
				if (this.maxDepth < 0 || nextLeafList.size() <= this.maxDepth)
					
					// Append child node to next loop's leaf list
					nextLeafList.add(childNode);
			}
			
			// Update list reference
			leafList = nextLeafList;
		}
		
		// Loop over leaves to eliminate parent relations (since they make serialisation impossible)
		Iterator<ParentRelationTreeNode> leaves = leafList.iterator();
		while (leaves.hasNext()){
			ParentRelationTreeNode leaf = leaves.next();
			ParentRelationTreeNode parent = leaf.getParentNode();
			while (parent != null){
				leaf.setParentNode(null);
				leaf = parent;
				parent = parent.getParentNode();
			}
		}
		
		// Initialise JSON serialiser
		GsonBuilder gsonBuilder = new GsonBuilder();
		//gsonBuilder.registerTypeAdapter(ModuleNetwork.class, new ParentRelationTreeNodeSerialiser());
		Gson gson = gsonBuilder.setPrettyPrinting().create();
		
		// Write to outputs // TODO Not sure if this is really the best performing method. Needs testing.
		Iterator<Pipe> outputPipes = this.getOutputPorts().get(ID_OUTPUT).getPipes(CharPipe.class).iterator();
		while(outputPipes.hasNext()){
			CharPipe outputPipe = (CharPipe) outputPipes.next();
			gson.toJson(rootNode, outputPipe.getOutput());
		}
		
		// Close input scanner
		inputScanner.close();
		
		// Close outputs (important!)
		this.closeAllOutputs();
		
		// Done
		return true;
	}
	
	@Override
	public void applyProperties() throws Exception {
		
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();
		
		// Apply own properties
		this.inputDelimiter = this.getProperties().getProperty(PROPERTYKEY_INPUTDELIMITER, this.getPropertyDefaultValues().get(PROPERTYKEY_INPUTDELIMITER));
		String maxDepthString = this.getProperties().getProperty(PROPERTYKEY_MAXDEPTH, this.getPropertyDefaultValues().get(PROPERTYKEY_MAXDEPTH));
		if (maxDepthString != null)
			this.maxDepth = Integer.parseInt(maxDepthString);
		//this.maxThreads = Integer.parseInt(this.getProperties().getProperty(PROPERTYKEY_MAXTHREADS, this.getPropertyDefaultValues().get(PROPERTYKEY_MAXTHREADS)));
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
