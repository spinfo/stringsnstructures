package modules.format_conversion;

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
import java.util.Iterator;
import java.util.Properties;

import models.ExtensibleTreeNode;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.Pipe;

import com.google.gson.Gson;
import common.parallelization.CallbackReceiver;

public class ExtensibleTreeNode2GEXFModule extends ModuleImpl {

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	//public static final String PROPERTYKEY_DELIMITER_INPUT = "input delimiter";
	//public static final String PROPERTYKEY_DELIMITER_OUTPUT = "output delimiter";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "input";
	private static final String ID_OUTPUT = "output";
	
	private long edgeId;

	// Local variables
	//private String inputdelimiter;
	//private String outputdelimiter;

	public ExtensibleTreeNode2GEXFModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("Converts trees based on the ExtensibleTreeNode into a GEXF graph.");

		// Add property descriptions (obligatory for every property!)
		/*this.getPropertyDescriptions()
				.put(PROPERTYKEY_DELIMITER_INPUT,
						"Regular expression to use as segmentation delimiter for input");
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT,
				"String to insert as segmentation delimiter into the output");*/

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"ExtensibleTreeNode to GEXF converter"); // Property key for module name is defined in
									// parent class
		/*this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT,
				"[\\s]+");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT, "\n");*/

		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~). Every port
		 * can support a range of pipe types (currently byte or character
		 * pipes). Output ports can provide data to multiple pipe instances at
		 * once, input ports can in contrast only obtain data from one pipe
		 * instance.
		 */
		InputPort inputPort = new InputPort(ID_INPUT,
				"ExtensibleTreeNode tree.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT,
				"GEXF graph.", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {
		
		

		// Instantiate JSON parser
		Gson gson = new Gson();
		
		// Read tree from input & parse it
		ExtensibleTreeNode rootNode = gson.fromJson(this.getInputPorts().get(ID_INPUT).getInputReader(), ExtensibleTreeNode.class);

		// Instantiate GEXF writer
		Gexf gexf = new GexfImpl();

		Calendar date = Calendar.getInstance();
		gexf.getMetadata().setLastModified(date.getTime())
				.setCreator("Uni Koeln, Strings & Structures Project").setDescription("A Tree");
		gexf.setVisualization(true);

		Graph graph = gexf.getGraph();
		graph.setDefaultEdgeType(EdgeType.DIRECTED).setMode(Mode.STATIC);
		
		AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
		graph.getAttributeLists().add(attrList);
		
		AttributeImpl counterAttrib = new AttributeImpl("0", AttributeType.STRING, "nodeCounter");
		attrList.add(0, counterAttrib);
		
		Iterator<String> nodeAttributeKeys = rootNode.getAttributes().keySet().iterator();
		int counter = 1;
		while(nodeAttributeKeys.hasNext()){
			String nodeAttribute = nodeAttributeKeys.next();
			attrList.createAttribute(""+counter, AttributeType.STRING, nodeAttribute);
			counter++;
		}
		
		// Iterate through tree
		edgeId = 0;
		this.convertToGEXF(rootNode, graph, null, attrList, "^");
		
		StaxGraphWriter graphWriter = new StaxGraphWriter();
		
		Iterator<OutputPort> outputPorts = this.getOutputPorts().values().iterator();
		while(outputPorts.hasNext()){
			OutputPort outputPort = outputPorts.next();
			Iterator<Pipe> pipes = outputPort.getPipes(CharPipe.class).iterator();
			while(pipes.hasNext()){
				CharPipe pipe = (CharPipe) pipes.next();
				graphWriter.writeToStream(gexf, pipe.getOutput(), "UTF-8");
			}
		}

		// Close outputs (important!)
		this.closeAllOutputs();

		// Done
		return true;
	}
	
	/**
	 * Recursively converts an ExtensibleTreeNode and its children into GEXF, adding them to the specified GEXF graph.
	 * @param node ExtensibleTreeNode node
	 * @param graph GEXF graph
	 * @param gexfParentNode GEXF parent node
	 * @param attrList List of attributes to include
	 */
	private void convertToGEXF(ExtensibleTreeNode node, Graph graph, Node gexfParentNode, AttributeList attrList, String childLabel){
		
		Node gexfNode = graph.createNode();
		gexfNode.setLabel(childLabel);
		gexfNode.setSize(1);
		
		if (gexfParentNode != null){
			gexfParentNode.connectTo(""+edgeId, "child", EdgeType.DIRECTED, gexfNode);
			edgeId++;
		}
		
		gexfNode.getAttributeValues().addValue(attrList.get(0),""+node.getNodeCounter());
		for (int i=1; i<attrList.size(); i++){
			if (node.getAttributes().get(attrList.get(i)) != null)
				gexfNode.getAttributeValues().addValue(attrList.get(i),node.getAttributes().get(attrList.get(i)).toString());
		}
		
		Iterator<String> childLabels = node.getChildNodes().keySet().iterator();
		while (childLabels.hasNext()){
			String label = childLabels.next();
			this.convertToGEXF(node.getChildNodes().get(label), graph, gexfNode, attrList, label);
		}
		
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		/*this.inputdelimiter = this.getProperties().getProperty(
				PROPERTYKEY_DELIMITER_INPUT,
				this.getPropertyDefaultValues()
						.get(PROPERTYKEY_DELIMITER_INPUT));
		this.outputdelimiter = this.getProperties().getProperty(
				PROPERTYKEY_DELIMITER_OUTPUT,
				this.getPropertyDefaultValues().get(
						PROPERTYKEY_DELIMITER_OUTPUT));*/

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
