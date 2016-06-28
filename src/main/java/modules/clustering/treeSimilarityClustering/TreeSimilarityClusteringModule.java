package modules.clustering.treeSimilarityClustering;

import it.uniroma1.dis.wsngroup.gexf4j.core.Edge;
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import models.ExtensibleTreeNode;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.Pipe;
import modules.ProgressWatcher;
import modules.ProgressWatcherThread;

import com.google.gson.Gson;
import common.parallelization.CallbackReceiver;

public class TreeSimilarityClusteringModule extends ModuleImpl {

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_MINSIMILARITY = "minimum similarity";
	public static final String PROPERTYKEY_MINDEGREE = "minimum degree";
	public static final String PROPERTYKEY_MAXPARALLELTHREADS = "maximum threads";
	public static final String PROPERTYKEY_MAXCOMPARISONDEPTH = "maximum comparison depth";
	public static final String PROPERTYKEY_PROGRESSWATCHERINTERVAL = "status update interval (ms)";
	public static final String PROPERTYKEY_MINTOKENAMOUNT = "minimum amount of tokens";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "suffix tree";
	private static final String ID_INPUT_REVERSED = "reversed tree";
	private static final String ID_OUTPUT = "GEXF graph";

	// Local variables
	private long edgeId;
	private int maxParallelThreads = 8;
	private float minSimilarity = 0.0f;
	private int minTokenAmount = 1;
	private int maxComparisonDepth = -1;
	private long progressWatcherInterval = 10000l;
	//private int minDegree = 0;

	public TreeSimilarityClusteringModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("Clusters elements of the first layer below the root node of specified trees by comparing them to one another, calculating a similarity quotient for each pairing in the process. The elements will then be inserted into a GEXF graph with edge weights set according to their respective similarity quotient. For details, see Magister thesis <i>Experimente zur Strukturbildung in natürlicher Sprache</i>, Marcel Boeing, Universität zu Köln, 2014.");
		this.getPropertyDescriptions().put(PROPERTYKEY_MINSIMILARITY, "Minimum similarity value that will result in an edge being created.");
		//this.getPropertyDescriptions().put(PROPERTYKEY_MINDEGREE, "Minimum node degree. Nodes with fewer connections will be removed from the graph prior to output.");
		this.getPropertyDescriptions().put(PROPERTYKEY_MAXPARALLELTHREADS, "Maximum number of parallel threads the module will spawn (in addition to its own thread and the ProgressWatcher's).");
		this.getPropertyDescriptions().put(PROPERTYKEY_MAXCOMPARISONDEPTH, "Maximum depth of the individual tree branches that will be used for comparison (-1 for no max.).");
		this.getPropertyDescriptions().put(PROPERTYKEY_PROGRESSWATCHERINTERVAL, "Interval (in milliseconds) that the module will give out details about the progress in. It will also calculate an estimated time remaining, so larger values may yield more precise information. Default is 10 seconds (10000 ms); minimum is 250 ms.");
		this.getPropertyDescriptions().put(PROPERTYKEY_MINTOKENAMOUNT, "Minimum amount of tokens a type must have to enter comparison.");
		
		// Add module category
		this.setCategory("Clustering");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Tree Similarity Clustering");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MINSIMILARITY, "0.0");
		//this.getPropertyDefaultValues().put(PROPERTYKEY_MINDEGREE, "0");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MAXPARALLELTHREADS, "8");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MAXCOMPARISONDEPTH, "-1");
		this.getPropertyDefaultValues().put(PROPERTYKEY_PROGRESSWATCHERINTERVAL, "10000");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MINTOKENAMOUNT, "1");

		// Define I/O
		InputPort inputPort = new InputPort(ID_INPUT,
				"ExtensibleTreeNode atomic suffix tree.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		InputPort inputPort2 = new InputPort(ID_INPUT_REVERSED,
				"(optional) ExtensibleTreeNode reversed atomic suffix tree.", this);
		inputPort2.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT,
				"GEXF graph.", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addInputPort(inputPort2);
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {
		
		

		// Instantiate JSON parser
		Gson gson = new Gson();
		
		// Updating status detail
		this.setStatusDetail("Receiving/parsing input");
		
		// Read tree from input & parse it
		ExtensibleTreeNode rootNode = gson.fromJson(this.getInputPorts().get(ID_INPUT).getInputReader(), ExtensibleTreeNode.class);
		
		// Read reversed tree from input & parse it
		ExtensibleTreeNode reversedRootNode = null;
		if (this.getInputPorts().get(ID_INPUT_REVERSED).isConnected())
			reversedRootNode = gson.fromJson(this.getInputPorts().get(ID_INPUT_REVERSED).getInputReader(), ExtensibleTreeNode.class);
		
		// If a second (reversed) suffix tree is present, make sure it contains the same keys
		if (reversedRootNode != null && !rootNode.getChildNodes().keySet().equals(reversedRootNode.getChildNodes().keySet())){
			throw new Exception("The second tree does not seem to contain the same key set. However, this is necessary to perform the comparison.");
		}

		// Map of elements to compare to each other (first degree tree children)
		Map<String,ExtensibleTreeNode> typeMap = new HashMap<String,ExtensibleTreeNode>();
		
		// Insert child nodes of the root node into a map (and apply node counter filter if set)
		if (this.minTokenAmount>1){
			Iterator<Entry<String, ExtensibleTreeNode>> rootChildren = rootNode.getChildNodes().entrySet().iterator();
			while (rootChildren.hasNext()){
				Entry<String, ExtensibleTreeNode> node = rootChildren.next();
				if (node.getValue().getNodeCounter()>=this.minTokenAmount)
					typeMap.put(node.getKey(), node.getValue());
			}
		} else {
			typeMap.putAll(rootNode.getChildNodes());
		}
		
		// Instantiate GEXF writer ...
		Gexf gexf = new GexfImpl();

		// ... metadata
		Calendar date = Calendar.getInstance();
		gexf.getMetadata().setLastModified(date.getTime())
				.setCreator("Uni Koeln, Strings & Structures Project").setDescription("Tree Similarity Quotient Cluster");
		gexf.setVisualization(true);

		// ... graph
		Graph graph = gexf.getGraph();
		graph.setDefaultEdgeType(EdgeType.UNDIRECTED).setMode(Mode.STATIC);
		
		// ... attributes
		AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
		graph.getAttributeLists().add(attrList);
		AttributeImpl counterAttrib = new AttributeImpl("0", AttributeType.LONG, "nodeCounter");
		attrList.add(0, counterAttrib);
		
		Iterator<String> nodeAttributeKeys = rootNode.getAttributes().keySet().iterator();
		int counter = 1;
		while(nodeAttributeKeys.hasNext()){
			String nodeAttribute = nodeAttributeKeys.next();
			attrList.createAttribute(""+counter, AttributeType.STRING, nodeAttribute);
			counter++;
		}
		
		/*
		 * Add nodes to graph and store a link to the created graph nodes in a map.
		 * We have to do this in advance to be able to insert the edges without hassle.
		 */
		// Map to store the node label <-> graph node relationship
		Map<String,Node> graphNodes = new HashMap<String,Node>();
		
		// Node comparator
		//NodeComparator comparator = new NodeComparator();
		
		// Reset edge id
		this.edgeId = 0;
		
		// Updating status detail
		this.setStatusDetail("Inserting "+typeMap.size()+" nodes into the graph");
		
		// Loop over types
		Iterator<Entry<String, ExtensibleTreeNode>> types = typeMap.entrySet().iterator();
		while (types.hasNext()) {

			// Determine next type to compare
			Entry<String, ExtensibleTreeNode> type = types.next();

			// Add to graph
			Node newNode = graph.createNode();
			
			// Apply attributes
			newNode.setLabel(type.getKey());
			newNode.getAttributeValues().addValue(counterAttrib, ""+type.getValue().getNodeCounter());
			
			// Other attributes
			/*Iterator<Attribute> attributes = attrList.iterator();
			while(attributes.hasNext()){
				Attribute attribute = attributes.next();
				
			}*/
			
			// Store in map
			graphNodes.put(type.getKey(), newNode);
		}
		

		// Calculate amount of work that lies ahead
		int elementsToCompare = typeMap.size();
		long comparisonsToConduct = (long) ((Math.pow(elementsToCompare, 2)/2)-elementsToCompare);
		
		// Track progress
		ProgressWatcher progress = new ProgressWatcher(comparisonsToConduct);
		Thread progressIndicator = new ProgressWatcherThread(progress,this, this.progressWatcherInterval);
		progressIndicator.start();
		
		/*
		 *  Compare every type to every other. We will do this by separating one by one
		 *  from the list and comparing it to the remainder. 
		 */
		
		// Loop over types again
		types = typeMap.entrySet().iterator();
		while(types.hasNext()){
			
			// Determine next type to compare
			Entry<String, ExtensibleTreeNode> type = types.next();
			// Remove it from list
			types.remove();
			
			// Create executor service
			ExecutorService executor = Executors.newFixedThreadPool(this.maxParallelThreads);
			
			// Map for comparison results
			final ConcurrentHashMap<String,Double> comparisonResultMap = new ConcurrentHashMap<String,Double>();
			
			// Loop over remainder
			Iterator<Entry<String, ExtensibleTreeNode>> typesRemainder = typeMap.entrySet().iterator();
			while(typesRemainder.hasNext()){
				// Determine next type to compare to the previously determined
				Entry<String, ExtensibleTreeNode> typeToCompareTo = typesRemainder.next();
				// Run comparison
				Runnable comparisonProcess;
				if (reversedRootNode != null){
					comparisonProcess = new ComparisonProcess(this.maxComparisonDepth, new Double(this.minSimilarity), type.getValue(), typeToCompareTo.getValue(), reversedRootNode.getChildNodes().get(type.getKey()), reversedRootNode.getChildNodes().get(typeToCompareTo.getKey()), comparisonResultMap, progress);
				} else {
					comparisonProcess = new ComparisonProcess(this.maxComparisonDepth, new Double(this.minSimilarity), type.getValue(), typeToCompareTo.getValue(), comparisonResultMap, progress);
				}
				executor.execute(comparisonProcess);
			}
			
			// Shutdown executor service
			executor.shutdown();
			executor.awaitTermination(5000l, TimeUnit.MILLISECONDS);
			
			// Put the results into the graph
			Iterator<Entry<String, Double>> comparisonResults = comparisonResultMap.entrySet().iterator();
			while(comparisonResults.hasNext()){
				Entry<String, Double> comparisonResult = comparisonResults.next();
				Edge edge = graphNodes.get(type.getKey()).connectTo(""+edgeId, "similar", EdgeType.UNDIRECTED, graphNodes.get(comparisonResult.getKey()));
				edge.setWeight(comparisonResult.getValue().floatValue());
				this.edgeId++;
			}
		}
		
		// Remove nodes not reaching the minimum degree range, if one is specified // DOES NOT WORK; APPARENTLY NODES CANNOT BE REMOVED
		/*if (this.minDegree > 0){
			Iterator<Node> nodes = graphNodes.values().iterator();
			while(nodes.hasNext()){
				Node node = nodes.next();
				if (node.getEdges().size()<this.minDegree)
					nodes.remove();
			}
		}*/
		
		// Write graph to output(s)
		StaxGraphWriter graphWriter = new StaxGraphWriter();

		// Updating status detail
		this.setStatusDetail("Writing graph to output");
		
		Iterator<OutputPort> outputPorts = this.getOutputPorts().values().iterator();
		while(outputPorts.hasNext()){
			OutputPort outputPort = outputPorts.next();
			Iterator<Pipe> pipes = outputPort.getPipes(CharPipe.class).iterator();
			while(pipes.hasNext()){
				CharPipe pipe = (CharPipe) pipes.next();
				graphWriter.writeToStream(gexf, pipe.getOutput(), "UTF-8");
			}
		}
		
		// Updating status detail
		this.setStatusDetail(null);

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
		/*String minDegreeString = this.getProperties().getProperty(
				PROPERTYKEY_MINDEGREE,
				this.getPropertyDefaultValues()
						.get(PROPERTYKEY_MINDEGREE));
		if (minDegreeString != null)
		this.minDegree = Integer.parseInt(minDegreeString);*/
		
		String maxParallelThreadsString = this.getProperties().getProperty(
				PROPERTYKEY_MINDEGREE,
				this.getPropertyDefaultValues()
						.get(PROPERTYKEY_MINDEGREE));
		if (maxParallelThreadsString != null)
		this.maxParallelThreads = Integer.parseInt(maxParallelThreadsString);
		
		String minSimilarityString = this.getProperties().getProperty(
				PROPERTYKEY_MINSIMILARITY,
				this.getPropertyDefaultValues().get(
						PROPERTYKEY_MINSIMILARITY));
		if (minSimilarityString != null)
		this.minSimilarity = Float.parseFloat(minSimilarityString);
		
		String maxComparisonDepthString = this.getProperties().getProperty(
				PROPERTYKEY_MAXCOMPARISONDEPTH,
				this.getPropertyDefaultValues().get(
						PROPERTYKEY_MAXCOMPARISONDEPTH));
		if (maxComparisonDepthString != null)
		this.maxComparisonDepth = Integer.parseInt(maxComparisonDepthString);
		
		String progressWatcherIntervalString = this.getProperties().getProperty(
				PROPERTYKEY_PROGRESSWATCHERINTERVAL,
				this.getPropertyDefaultValues().get(
						PROPERTYKEY_PROGRESSWATCHERINTERVAL));
		if (progressWatcherIntervalString != null){
			long value = Long.parseLong(progressWatcherIntervalString);
			if (value>250l)
				this.progressWatcherInterval = value;
		}
		
		String minTokenAmountString = this.getProperties().getProperty(
				PROPERTYKEY_MINTOKENAMOUNT,
				this.getPropertyDefaultValues().get(
						PROPERTYKEY_MINTOKENAMOUNT));
		if (minTokenAmountString != null){
			int value = Integer.parseInt(minTokenAmountString);
			if (value>0)
				this.minTokenAmount = value;
		}
			
		
		

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
