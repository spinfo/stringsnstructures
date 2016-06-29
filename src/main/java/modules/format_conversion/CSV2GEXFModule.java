package modules.format_conversion;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;

import common.parallelization.CallbackReceiver;
import it.uniroma1.dis.wsngroup.gexf4j.core.Edge;
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.Pipe;

public class CSV2GEXFModule extends ModuleImpl {

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_DELIMITER_INPUT = "CSV input delimiter";
	public static final String PROPERTYKEY_EDGEDESIGNATOR = "Edge designator";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "input";
	private static final String ID_OUTPUT = "output";
	
	private long edgeId;
	private String edgeDesignator;

	// Local variables
	private String inputdelimiter;

	public CSV2GEXFModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("Converts a matrix with numerical values into a GEXF graph.");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_DELIMITER_INPUT,
						"Regular expression to use as field delimiter for CSV input");
		this.getPropertyDescriptions().put(PROPERTYKEY_EDGEDESIGNATOR,
				"Designator for edges in the GEXF graph (use to describe type of relationship between connected nodes)");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"CSV to GEXF converter"); // Property key for module name is defined in
									// parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT,
				";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_EDGEDESIGNATOR,
				"resembles");

		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~). Every port
		 * can support a range of pipe types (currently byte or character
		 * pipes). Output ports can provide data to multiple pipe instances at
		 * once, input ports can in contrast only obtain data from one pipe
		 * instance.
		 */
		InputPort inputPort = new InputPort(ID_INPUT,
				"CSV input.", this);
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
		
		/*
		 * Prepare input reading
		 */
		
		Scanner lineScanner = new Scanner(this.getInputPorts().get(ID_INPUT).getInputReader());
		lineScanner.useDelimiter("\\R+");
		
		if (! lineScanner.hasNext()){
			lineScanner.close();
			this.closeAllOutputs();
			throw new Exception("Empty input.");
		}
		
		// Read CSV header line
		String[] headerFields = lineScanner.next().split(this.inputdelimiter);
		
		/*
		 *  Instantiate GEXF writer
		 */
		Gexf gexf = new GexfImpl();

		Calendar date = Calendar.getInstance();
		gexf.getMetadata().setLastModified(date.getTime()).setCreator("Uni Koeln, Strings & Structures Project")
				.setDescription("A Tree");
		gexf.setVisualization(true);

		Graph graph = gexf.getGraph();
		graph.setDefaultEdgeType(EdgeType.UNDIRECTED).setMode(Mode.STATIC);

		//AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
		//graph.getAttributeLists().add(attrList);

		//AttributeImpl edgeWeight = new AttributeImpl("0", AttributeType.DOUBLE, "edgeWeight");
		//attrList.add(0, edgeWeight);
		
		/*
		 *  Create nodes from header fields
		 */
		Map<String,Node> nodeMap = new TreeMap<String,Node>();
		for (int i=1; i<headerFields.length; i++){
			Node gexfNode = graph.createNode();
			gexfNode.setLabel(headerFields[i]);
			gexfNode.setSize(1);
			nodeMap.put(headerFields[i], gexfNode);
		}

		/*
		 * Create edges from data fields
		 */
		this.edgeId = 0l;
		while (lineScanner.hasNext()){
			String[] dataLine = lineScanner.next().split(this.inputdelimiter);
			String lineTitle = dataLine[0];
			// If the line title does not yet have a node, create it
			if (lineTitle != null && ! nodeMap.containsKey(lineTitle)){
				Node gexfNode = graph.createNode();
				gexfNode.setLabel(lineTitle);
				gexfNode.setSize(1);
				nodeMap.put(lineTitle, gexfNode);
			}
			
			// Loop over actual data fields
			for (int i=1; i<dataLine.length; i++){
				
				// Determine line node
				Node rowNode = nodeMap.get(lineTitle);
				
				// Determine column node
				Node colNode = nodeMap.get(headerFields[i]);
				
				// Determine numerical data value
				Double value = 0d;
				if (dataLine[i] != null && ! dataLine[i].isEmpty()){
					value = Double.parseDouble(dataLine[i]);
				}
				
				// Create edge between both nodes
				Edge newEdge = rowNode.connectTo(""+edgeId, this.edgeDesignator, EdgeType.UNDIRECTED, colNode);
				newEdge.setWeight(value.floatValue());
				//newEdge.getAttributeValues().addValue(edgeWeight, value.toString());
				edgeId++;
			}
		}
		// Close input scanner
		lineScanner.close();
		
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

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		this.inputdelimiter = this.getProperties().getProperty(
				PROPERTYKEY_DELIMITER_INPUT,
				this.getPropertyDefaultValues()
						.get(PROPERTYKEY_DELIMITER_INPUT));
		this.edgeDesignator = this.getProperties().getProperty(
				PROPERTYKEY_EDGEDESIGNATOR,
				this.getPropertyDefaultValues()
						.get(PROPERTYKEY_EDGEDESIGNATOR));

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
