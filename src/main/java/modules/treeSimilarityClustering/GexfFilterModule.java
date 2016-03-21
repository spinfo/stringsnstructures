package modules.treeSimilarityClustering;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import common.parallelization.CallbackReceiver;
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
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.Pipe;

public class GexfFilterModule extends ModuleImpl {

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_MINSIMILARITY = "minimum similarity";
	public static final String PROPERTYKEY_MINTOKENAMOUNT = "minimum amount of tokens";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "GEXF graph";
	private static final String ID_OUTPUT = "GEXF graph";

	// Local variables
	private float minSimilarity = 0.0f;
	private int minTokenAmount = 1;

	public GexfFilterModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("Filters GEXF data according to the specified parameters. Expects nodes to have the attribute 'nodeCounter' indicating the amount of tokens associated.");
		this.getPropertyDescriptions().put(PROPERTYKEY_MINSIMILARITY,
				"Minimum similarity value an edge must have to be kept in the graph.");
		this.getPropertyDescriptions().put(PROPERTYKEY_MINTOKENAMOUNT,
				"Minimum amount of tokens a type must have to be kept in the graph.");

		// Add module category
		this.setCategory("Clustering");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "GEXF Filter");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MINSIMILARITY, "0.1");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MINTOKENAMOUNT, "2");

		// Define I/O
		InputPort inputPort = new InputPort(ID_INPUT, "GEXF graph. Nodes must have the attribute 'nodeCounter'.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "GEXF graph (filtered).", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {

		// Instantiate GEXF writer ...
		Gexf gexf = new GexfImpl();

		// ... metadata
		Calendar date = Calendar.getInstance();
		gexf.getMetadata().setLastModified(date.getTime()).setCreator("Uni Koeln, Strings & Structures Project")
				.setDescription("GEXF graph");
		gexf.setVisualization(true);

		// ... graph
		Graph graph = gexf.getGraph();
		graph.setDefaultEdgeType(EdgeType.UNDIRECTED).setMode(Mode.STATIC);

		// ... attributes
		AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
		graph.getAttributeLists().add(attrList);
		AttributeImpl counterAttrib = new AttributeImpl("0", AttributeType.LONG, "nodeCounter");
		attrList.add(0, counterAttrib);

		/*
		 * Add nodes to graph and store a link to the created graph nodes in a
		 * map. We have to do this in advance to be able to insert the edges
		 * without hassle.
		 */
		// Map to store the node label <-> graph node relationship
		Map<String, Node> graphNodes = new HashMap<String, Node>();

		// Parse input
		Scanner inputScanner = new Scanner(this.getInputPorts().get(ID_INPUT).getInputReader());
		inputScanner.useDelimiter("<node |<edge ");

		while (inputScanner.hasNext()) {
			String inputChunk = inputScanner.next();

			String key;
			int keyIndex;
			int valueStartIndex;
			int valueEndIndex;

			key = "id";
			keyIndex = inputChunk.indexOf(key+"=");
			if (keyIndex == -1)
				continue;
			valueStartIndex = keyIndex + key.length()+2;
			valueEndIndex = inputChunk.indexOf('"', valueStartIndex);
			String id = inputChunk.substring(valueStartIndex, valueEndIndex);

			key = "label";
			keyIndex = inputChunk.indexOf(key+"=");
			if (keyIndex == -1)
				continue;
			valueStartIndex = keyIndex + key.length()+2;
			valueEndIndex = inputChunk.indexOf('"', valueStartIndex);
			String label = inputChunk.substring(valueStartIndex, valueEndIndex);

			key = "source";
			keyIndex = inputChunk.indexOf(key+"=");
			if (keyIndex == -1) {
				// might be a node
				key = "value";
				keyIndex = inputChunk.indexOf(key+"=");
				if (keyIndex == -1)
					continue;
				valueStartIndex = keyIndex + key.length()+2;
				valueEndIndex = inputChunk.indexOf('"', valueStartIndex);
				String valueString = inputChunk.substring(valueStartIndex, valueEndIndex);
				Long value = 0l;
				try {
					value = Long.parseLong(valueString);
				} catch (Exception e) {
					continue;
				}
				// Determine if the node just read will be kept or filtered

				if (value >= this.minTokenAmount) {
					// Add to graph
					Node newNode = graph.createNode(id);

					// Apply attributes
					newNode.setLabel(label);
					newNode.getAttributeValues().addValue(counterAttrib, valueString);

					// Add to map
					graphNodes.put(id, newNode);
				}

			} else {
				// seems to be an edge
				valueStartIndex = keyIndex + key.length()+2;
				valueEndIndex = inputChunk.indexOf('"', valueStartIndex);
				String source = inputChunk.substring(valueStartIndex, valueEndIndex);

				key = "target";
				keyIndex = inputChunk.indexOf(key+"=");
				if (keyIndex == -1)
					continue;
				valueStartIndex = keyIndex + key.length()+2;
				valueEndIndex = inputChunk.indexOf('"', valueStartIndex);
				String target = inputChunk.substring(valueStartIndex, valueEndIndex);

				key = "weight";
				keyIndex = inputChunk.indexOf(key+"=");
				if (keyIndex == -1)
					continue;
				valueStartIndex = keyIndex + key.length()+2;
				valueEndIndex = inputChunk.indexOf('"', valueStartIndex);
				String weightString = inputChunk.substring(valueStartIndex, valueEndIndex);
				Float weight = 0f;
				try {
					weight = Float.parseFloat(weightString);
				} catch (Exception e) {
					continue;
				}

				// Determine if the edge will be kept or filtered out
				if (weight >= this.minSimilarity) {
					Node sourceNode = graphNodes.get(source);
					Node targetNode = graphNodes.get(target);
					if (sourceNode != null && targetNode != null){
						Edge edge = sourceNode.connectTo(id, "similar", EdgeType.UNDIRECTED,
								targetNode);
						edge.setWeight(weight);
					} else {
						// TODO report error
					}
					
				}

			}
		}
		inputScanner.close();

		// Write graph to output(s)
		StaxGraphWriter graphWriter = new StaxGraphWriter();

		Iterator<OutputPort> outputPorts = this.getOutputPorts().values().iterator();
		while (outputPorts.hasNext()) {
			OutputPort outputPort = outputPorts.next();
			Iterator<Pipe> pipes = outputPort.getPipes(CharPipe.class).iterator();
			while (pipes.hasNext()) {
				CharPipe pipe = (CharPipe) pipes.next();
				graphWriter.writeToStream(gexf, pipe.getOutput(), "UTF-8");
			}
		}

		// Close outputs
		this.closeAllOutputs();

		// Done
		return true;
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties

		String minSimilarityString = this.getProperties().getProperty(PROPERTYKEY_MINSIMILARITY,
				this.getPropertyDefaultValues().get(PROPERTYKEY_MINSIMILARITY));
		if (minSimilarityString != null)
			this.minSimilarity = Float.parseFloat(minSimilarityString);

		String minTokenAmountString = this.getProperties().getProperty(PROPERTYKEY_MINTOKENAMOUNT,
				this.getPropertyDefaultValues().get(PROPERTYKEY_MINTOKENAMOUNT));
		if (minTokenAmountString != null) {
			int value = Integer.parseInt(minTokenAmountString);
			if (value > 0)
				this.minTokenAmount = value;
		}

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
