package modules.suffixTreeClusteringModuleWrapper;

// java standard imports:
import java.util.Properties;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;

// modularization imports:
import modules.BytePipe;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import common.parallelization.CallbackReceiver;

// clustering specific imports:
import modules.suffixTreeClustering.clustering.flat.FlatCluster;
import modules.suffixTreeClustering.clustering.flat.FlatClusterer;
import modules.suffixTreeClustering.clustering.hierarchical.HierarchicalCluster;
import modules.suffixTreeClustering.clustering.hierarchical.HierarchicalClusterer;
import modules.suffixTreeClustering.clustering.neighborjoin.NeighborJoining;
import modules.suffixTreeClustering.data.Type;
import modules.suffixTreeClustering.features.FeatureType;
import modules.suffixTreeClustering.st_interface.SuffixTreeInfo;

// cluster wrapper specific imports:
import common.KwipXmlStreamReader;

/**
 * This is a wrapper to modularize the clustering process. 
 * @author Christopher Kraus
 * parts of the code were taken from modules.suffixTreeClustering.main which 
 * were originally written by neumannm
 */

public class SuffixTreeClusteringModuleWrapper extends ModuleImpl {
				
	// property keys:
	
		// this property saves the selected form of clustering
	public static final String PROPERTYKEY_CLUST = "clustering type";
	
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
	
		// variable which holds the types
	private List<Type> types;
	
		// the type of vector which should be created
	private String vecType;
	private FeatureType vectorType;
	
		// the type of clustering which should be applied
	private String clusterType;
	
		// the name of the corpus
	private String corpusName;
	
		//the result of the clustering
	private String clustResult;
	
		// definitions of I/O variables
	private final String INPUTIDTREERES = "KWIP xml Result";
	private final String INPUTIDTREE = "suffixTree";
	private final String OUTPUTID = "output";
	
	// end variables
	
	// constructors:
	
	public SuffixTreeClusteringModuleWrapper (CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// call parent constructor
		super(callbackReceiver, properties);
		
		// module description
		this.setDescription("This is a wrapper to modularize the clustering process.<br/>"
				+ "It takes two inputs:<br/>"
				+ "<ul type =\"disc\" ><li>KWIP suffix tree result in xml format</li><li>the suffix tree itself in xml format</li></ul>");
		
		// Add module category
		this.setCategory("Deprecated");
		
		// property descriptions 
		this.getPropertyDescriptions().put(PROPERTYKEY_CLUST, "Three possible clustering types: \"NJ\" "
				+ "(Neighbor Joining), \"KM\" (flat k-means), \"HAC\" (hierachial agglomerative clustering)");
		this.getPropertyDescriptions().put(PROPERTYKEY_VECTYPE, "The feature type of the vector. Possible inputs:"
				+ " \"TF-IDF\", \"TF-DF\", \"binary\"");
		this.getPropertyDescriptions().put(PROPERTYKEY_CORPNAME, "Insert corpus/text name");
		
		// property defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "SuffixTreeClusteringWrapper"); 
		this.getPropertyDefaultValues().put(PROPERTYKEY_CLUST, "KM");
		this.getPropertyDefaultValues().put(PROPERTYKEY_VECTYPE, "TF-IDF");
		this.getPropertyDefaultValues().put(PROPERTYKEY_CORPNAME, "myCorpus");
		
		// I/O definition
		InputPort inputPortTreeRes = new InputPort(INPUTIDTREERES, "[text/xml] Input of an XML representation of the KWIP result.", this);
		inputPortTreeRes.addSupportedPipe(BytePipe.class);
		
		InputPort inputPortTree = new InputPort(INPUTIDTREE, "[text/xml] Input of an XML representation of the suffix tree.", this);
		inputPortTree.addSupportedPipe(BytePipe.class);
		
		OutputPort outputPort = new OutputPort(OUTPUTID, "[text] Plain text character output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		// add I/O ports to instance
		super.addInputPort(inputPortTreeRes);
		super.addInputPort(inputPortTree);
		
		super.addOutputPort(outputPort);
	}
	
	// end constructors
	
	// setters:
	
	// end setters:
	
	// getters:
	
	// end getters
	
	// methods:
	
	// process()
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
		
		types = new ArrayList<Type>(corpus.getTypes());
		
		// selection of the clustering type
		switch (clusterType) {
		case "NJ":
			clusterNeighborJoin(types);
			break;
		case "KM":
			clusterFlat(types, corpusName);
			break;
		case "HAC":
			clusterHierarchical(types, corpusName);
			break;
		default:
			clusterFlat(types, corpusName);
			break;
		}
		
		// put the results into the output char pipe
		this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(this.clustResult);
		
		// close outputs
		this.closeAllOutputs();
				
		return true;
		
	}
	
	// applyProperties()
	@Override
	public void applyProperties() throws Exception {
		
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();
		
		// Apply own properties
		this.corpusName = this.getProperties().getProperty(PROPERTYKEY_CORPNAME, this.getPropertyDefaultValues().get(PROPERTYKEY_CORPNAME));
		this.vecType = this.getProperties().getProperty(PROPERTYKEY_VECTYPE, this.getPropertyDefaultValues().get(PROPERTYKEY_VECTYPE));
		this.clusterType = this.getProperties().getProperty(PROPERTYKEY_CLUST, this.getPropertyDefaultValues().get(PROPERTYKEY_CLUST));
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

	private TreeMap<Integer, String> fillTypeStrings(List<Type> kwipTypes) throws Exception {
		TreeMap<Integer, String> toReturn = new TreeMap<>();
		for (Type type : kwipTypes) {
			if(!toReturn.containsKey(type.getID()))
				toReturn.put(type.getID(), type.getString());
			else throw new Exception("There should not be 2 types with same ID!");
		}
		return toReturn;
	}
	
	private void clusterFlat(List<Type> types, String name) {
		FlatClusterer f_analysis = new FlatClusterer(types);
		//LOGGER.info("Flaches Clustern von " + types.size() + " Types");
		List<FlatCluster> fClusters = f_analysis.analyse(3, 10);
		for (FlatCluster cluster : fClusters) {
			System.out.println(cluster.getMedoid().getString());
		}
		this.clustResult = f_analysis.toDot();
		// System.out.println(dot);
	}

	private void clusterHierarchical(List<Type> types,
			String name) {
		// usage of hierachial clustering 
		HierarchicalClusterer h_analysis = new HierarchicalClusterer(types);
		//LOGGER.info("Hierarchisches Clustern von " + types.size() + " Types");
		h_analysis.analyze();
		List<HierarchicalCluster> hClusters = h_analysis.getClusters();

		if (hClusters.size() == 1) {
			this.clustResult = h_analysis.toDot();
			
		} else
			System.err.println("Hierarchical Clustering didn't come up with exactly 1 cluster!");
	}
	
	private void clusterNeighborJoin(List<Type> types) {
		// usage of Neighbor Joining 
		NeighborJoining nj = new NeighborJoining(types);
		//LOGGER.info("Neighbor Joining Clustern von " + types.size() + " Types");
		nj.start();
		this.clustResult = nj.getTree();
	}
	
	// end methods

}
