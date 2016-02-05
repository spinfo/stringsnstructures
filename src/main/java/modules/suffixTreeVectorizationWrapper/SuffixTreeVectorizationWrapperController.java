package modules.suffixTreeVectorizationWrapper;

//java standard imports:
import java.util.Properties;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;

//modularization imports:
import modules.Pipe;
import modules.BytePipe;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import common.parallelization.CallbackReceiver;

//vectorization specific imports:
import modules.suffixTreeClustering.data.Type;
import modules.suffixTreeClustering.features.FeatureType;
import modules.suffixTreeClustering.st_interface.SuffixTreeInfo;

//cluster wrapper specific imports:
import common.KwipXmlStreamReader;

// JSON gson imports
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This is a wrapper to modularize the vectorization process. 
 * @author Christopher Kraus
 * parts of this code are refactored from neumannm
 */

public class SuffixTreeVectorizationWrapperController extends ModuleImpl {
	
	// property keys:
	
	// this property saves the type as which a vector was created
	public static final String PROPERTYKEY_VECTYPE = "vector type";
	
	// this property saves the name of the used corpus
	public static final String PROPERTYKEY_CORPNAME = "corpus/text name";
	
	// variables:
	
	//input stream for kwipStreamReader
	private InputStream kwipInStream;
	
	// reference for a KWIP XML reader
	private KwipXmlStreamReader kwipStreamReader;
	
	//input stream of the suffix tree
	private InputStream suffixTreeInStream;
	
	// reference for suffix tree XML reader
	private XmlStreamReader treeXmlStreamReader;
	
	// variable for saving the corpus 
	private SuffixTreeInfo corpus;
	
	// variable to create a serializable corpus information
	private SuffixTreeInfoSer corpusSer;
	
	// the type of vector which should be created
	private String vecType;
	private FeatureType vectorType;
	
	// definitions of I/O variables
	private final String INPUTIDTREERES = "KWIP xml Result";
	private final String INPUTIDTREE = "suffixTree";
	private final String OUTPUTJSONID = "toJson";
	private final String OUTPUTID = "byteOutput";
	
	// end variables
	
	// constructors:
	
	public SuffixTreeVectorizationWrapperController (CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// call parent constructor
		super(callbackReceiver, properties);
		
		// module description
		this.setDescription("This is a wrapper to modularize the vectorization process.<br/>"
				+ "It takes two inputs:<br/>"
				+ "<ul type =\"disc\" ><li>KWIP suffix tree result in xml format</li><li>the suffix tree itself in xml format</li></ul>"
				+ "It creates an ouput representation of the vectors of the type \"SuffixTreeInfo\"<br/>"
				+ "which is serialized in JSON format.");
		
		// Add module category
		this.setCategory("Vectorization");
		
		// property descriptions 
		this.getPropertyDescriptions().put(PROPERTYKEY_VECTYPE, "The feature type of the vector. Possible inputs:"
				+ " \"TF-IDF\", \"TF-DF\", \"binary\"");
		this.getPropertyDescriptions().put(PROPERTYKEY_CORPNAME, "Insert corpus/text name");
		
		// property defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "SuffixTreeVectorizationWrapper"); 
		this.getPropertyDefaultValues().put(PROPERTYKEY_VECTYPE, "TF-IDF");
		
		// I/O definition
		InputPort inputPortTreeRes = new InputPort(INPUTIDTREERES, "[text/xml] Input of an XML representation of the KWIP result.", this);
		inputPortTreeRes.addSupportedPipe(BytePipe.class);
		
		InputPort inputPortTree = new InputPort(INPUTIDTREE, "[text/xml] Input of an XML representation of the suffix tree.", this);
		inputPortTree.addSupportedPipe(BytePipe.class);
		
		OutputPort outputJsonPort = new OutputPort(OUTPUTJSONID, "[JSON] Output: Vector after \"SuffixTreeInfo\" in JSON format.", this);
		outputJsonPort.addSupportedPipe(CharPipe.class);
		
		OutputPort outputPort = new OutputPort(OUTPUTID, "[byte] serilalized vector output after \"SuffixTreeInfoSer\".", this);
		outputPort.addSupportedPipe(BytePipe.class);
		
		
		
		// add I/O ports to instance
		super.addInputPort(inputPortTreeRes);
		super.addInputPort(inputPortTree);
		
		super.addOutputPort(outputJsonPort);
		super.addOutputPort(outputPort);
	}
	
	// end constructors
	
	@Override
	public boolean process() throws Exception {
		
		//Step 1: Read all necessary information from the KWIP (see step 1.1) and the GST module (see step 1.2).
		
		// Step 1.1: Read the "key-words in phrase" (KWIP) xml-output.
		try {
			kwipInStream = this.getInputPorts().get(INPUTIDTREERES).getInputStream();
			kwipStreamReader = new KwipXmlStreamReader (kwipInStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<Type> kwipTypes = this.kwipStreamReader.read();

		Map<Integer, String> typeStrings = null;
		try {
			typeStrings = fillTypeStrings(kwipTypes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Step 1.2: Read the generalized suffix tree (as xml-output).
		try {
			suffixTreeInStream = this.getInputPorts().get(INPUTIDTREE).getInputStream();
			treeXmlStreamReader = new XmlStreamReader (suffixTreeInStream);
			corpus = treeXmlStreamReader.read(typeStrings);
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		// Step 2: go through the list and search for each node its unit and
		// remember the absolute value for each unit after either tf/idf, tf/df or binary information.
		// Binary information in this case means that occurrences are noted as "1" and non-occurrences are noted as "0".
		// Afterwards save the results in the vector.
		
		switch (this.vecType) {
		case "TF-IDF":
			this.vectorType = FeatureType.TF_IDF;
			break;
		case "TF-DF":
			this.vectorType = FeatureType.TF_DF;
			break;
		case "binary":
			this.vectorType = FeatureType.BINARY;
			break;
		default:
			this.vectorType = FeatureType.TF_IDF;
			break;
		}
		
		// Assign "vector features" (values for each vector component) to each vector.
		for (Type doc : corpus.getTypes()) {
				doc.calculateVector(corpus, vectorType);

		}
		
		//Step 3: Write the output into a format and ship it to a particular clustering module
		//(actually step 3 would be the clustering).
		
		// Prepare serializable corpusSer object.
		this.corpusSer = new SuffixTreeInfoSer();
		this.corpusSer.setNumberOfNodes(this.corpus.getNumberOfNodes());
		this.corpusSer.setNumberOfTypes(this.corpus.getNumberOfTypes());
		this.corpusSer.setTypes(this.corpus.getTypes());
		this.corpusSer.convertNodes(this.corpus.getNodes());
		
		// Prepare byte output for several output pipes.
		Iterator <Pipe> it = this.getOutputPorts().get(OUTPUTID).getPipes(BytePipe.class).iterator();
		while(it.hasNext()) {
			BytePipe currPipe = (BytePipe) it.next();
			ObjectOutputStream ooStream = new ObjectOutputStream(currPipe.getOutput());
			ooStream.writeObject(this.corpusSer);
			ooStream.close();
		}
		
		// Prepare Json output.
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Iterator<Pipe> charPipes = this.getOutputPorts().get(OUTPUTJSONID).getPipes(CharPipe.class).iterator();
		while (charPipes.hasNext()){
			gson.toJson(this.corpus, ((CharPipe)charPipes.next()).getOutput());
		}
							
		// Close outputs (important!)
		this.closeAllOutputs();
		
		// success
		return true;
	}
	
	// custom methods:
	
	private TreeMap<Integer, String> fillTypeStrings(List<Type> kwipTypes) throws Exception {
		TreeMap<Integer, String> toReturn = new TreeMap<>();
		for (Type type : kwipTypes) {
			if(!toReturn.containsKey(type.getID()))
				toReturn.put(type.getID(), type.getString());
			else throw new Exception("There should not be 2 types with same ID!");
		}
		return toReturn;
	}
	// end custom methods;
	
	// applyProperties()
	@Override
	public void applyProperties() throws Exception {
		
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();
		
		// Apply own properties
		this.vecType = this.getProperties().getProperty(PROPERTYKEY_VECTYPE, this.getPropertyDefaultValues().get(PROPERTYKEY_VECTYPE));
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}
}
