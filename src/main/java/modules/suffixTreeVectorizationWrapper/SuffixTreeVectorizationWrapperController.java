package modules.suffixTreeVectorizationWrapper;

//java standard imports:
import java.util.Properties;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.io.InputStream;
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

//TODO: ATTENTION THIS MODULE IS STILL EXPERIMENTAL!

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
	
	// the type of vector which should be created
	private String vecType;
	private FeatureType vectorType;
	
	// definitions of I/O variables
	private final String INPUTIDTREERES = "KWIP xml Result";
	private final String INPUTIDTREE = "suffixTree";
	private final String OUTPUTID = "output";
	
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
		
		OutputPort outputPort = new OutputPort(OUTPUTID, "[JSON] Output: Vector after \"SuffixTreeInfo\" in JSON format.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		// add I/O ports to instance
		super.addInputPort(inputPortTreeRes);
		super.addInputPort(inputPortTree);
		
		super.addOutputPort(outputPort);
	}
	
	// end constructors
	
	@Override
	public boolean process() throws Exception {
				
		try {
			kwipInStream = this.getInputPorts().get(INPUTIDTREERES).getInputStream();
			kwipStreamReader = new KwipXmlStreamReader (kwipInStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<Type> kwipTypes = this.kwipStreamReader.read();
//		for (Type type : kwipTypes)
//			System.out.println(type);

		Map<Integer, String> typeStrings = null;
		try {
			typeStrings = fillTypeStrings(kwipTypes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// read the suffix tree
		try {
			suffixTreeInStream = this.getInputPorts().get(INPUTIDTREE).getInputStream();
			treeXmlStreamReader = new XmlStreamReader (suffixTreeInStream);
			corpus = treeXmlStreamReader.read(typeStrings);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		// print nodes for debugging
		for (Node node : corpus.getNodes()) {
			System.out.println(node);
		}

		for (Type type : corpus.getTypes()) {
			System.out.println(type);
		}

		System.out.println("---------------------------");
		*/
		
		// step 2: go through the list and search for each node its unit and
		// remember the absolute value for each unit after tf/idf.
		// afterwards save the results in the vector.
		
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
		
		for (Type doc : corpus.getTypes()) {
				doc.calculateVector(corpus, vectorType);

			/*
			// for debugging
			System.out.print("[");
			for (Double val : doc.getVector().getValues()) {
				System.out.print(val.doubleValue() + ", ");
			}
			System.out.println("]");
			*/
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Iterator<Pipe> charPipes = this.getOutputPorts().get(OUTPUTID).getPipes(CharPipe.class).iterator();
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
