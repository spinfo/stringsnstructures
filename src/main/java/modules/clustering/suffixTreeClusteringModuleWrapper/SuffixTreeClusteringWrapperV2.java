package modules.clustering.suffixTreeClusteringModuleWrapper;

//java standard imports:
import java.util.Properties;
import java.util.List;
import java.io.BufferedReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;

//modularization imports:
import modules.Pipe;
import modules.tree_building.suffixTreeClustering.clustering.flat.FlatCluster;
import modules.tree_building.suffixTreeClustering.clustering.flat.FlatClusterer;
import modules.tree_building.suffixTreeClustering.clustering.hierarchical.HierarchicalCluster;
import modules.tree_building.suffixTreeClustering.clustering.hierarchical.HierarchicalClusterer;
import modules.tree_building.suffixTreeClustering.clustering.neighborjoin.NeighborJoining;
import modules.tree_building.suffixTreeClustering.data.Node;
import modules.tree_building.suffixTreeClustering.data.Type;
import modules.tree_building.suffixTreeClustering.features.FeatureVector;
import modules.tree_building.suffixTreeClustering.st_interface.SuffixTreeInfo;
import modules.CharPipe;
import modules.BytePipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import common.parallelization.CallbackReceiver;
import models.NamedFieldMatrix;
import modules.vectorization.suffixTreeVectorizationWrapper.SuffixTreeInfoSer;

//google gson imports
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * This is a wrapper to modularize the vectorization process. 
 * @author Christopher Kraus
 * parts of this code are refactored from neumannm
 */

import base.workbench.ModuleRunner;

public class SuffixTreeClusteringWrapperV2 extends ModuleImpl {

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(SuffixTreeClusteringWrapperV2.class, args);
	}

	// property keys:

	// this property saves the selected form of clustering
	public static final String PROPERTYKEY_CLUST = "clustering type";

	// this property saves the name of the used corpus
	public static final String PROPERTYKEY_CORPNAME = "corpus/text name";

	// this property determines the delimiter to use when parsing csv input
	public static final String PROPERTYKEY_MATRIX_CSV_DELIM = "matrix input csv delimiter";

	// variables:

	// variable for saving the corpus
	private SuffixTreeInfo corpus;

	// serializable SuffixTreeInfo variable
	private SuffixTreeInfoSer corpusSer;

	// variable which holds the types
	private List<Type> types;

	// the type of clustering which should be applied
	private String clusterType;

	// the name of the corpus
	private String corpusName;

	// the delimiter used in parsing matrix csv input
	private String matrixCsvDelimiter;

	// the result of the clustering
	private String clustResult;

	// flat cluster kmeans result
	Object clusterJsonRes;

	// definitions of I/O variables
	private final String INPUT_ST_ID = "byteInput";
	private final String INPUT_MATRIX_ID = "NamedFieldMatrix csv";
	private final String OUTPUTID = "output";
	private final String OUTPUTJSONID = "json";

	// end variables

	// constructors:

	public SuffixTreeClusteringWrapperV2(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// call parent constructor
		super(callbackReceiver, properties);

		// module description
		this.setDescription("This is a wrapper to modularize the clustering process.<br/>" + "It takes one input:<br/>"
				+ "<ul type =\"disc\" ><li>vectors of the type \"SuffixTreeInfo\" in JSON format</li></ul>");

		// property descriptions
		this.getPropertyDescriptions().put(PROPERTYKEY_CLUST, "Three possible clustering types: \"NJ\" "
				+ "(Neighbor Joining), \"KM\" (flat k-means), \"HAC\" (hierachial agglomerative clustering)");

		this.getPropertyDescriptions().put(PROPERTYKEY_CORPNAME, "Insert corpus/text name");
		this.getPropertyDescriptions().put(PROPERTYKEY_MATRIX_CSV_DELIM,
				"The delimiter to use when reading matrix csv input.");

		// property defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "SuffixTreeClusteringWrapperV2");
		this.getPropertyDefaultValues().put(PROPERTYKEY_CLUST, "KM");
		this.getPropertyDefaultValues().put(PROPERTYKEY_CORPNAME, "myCorpus");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MATRIX_CSV_DELIM, ";");

		// I/O definition
		InputPort inputPortVec = new InputPort(INPUT_ST_ID, "[byte] deserialized vector after \"SuffixTreeInfoSer\".",
				this);
		inputPortVec.addSupportedPipe(BytePipe.class);

		InputPort matrixInputPort = new InputPort(INPUT_MATRIX_ID,
				"[text/csv] A csv representation of a NamedFieldMatrix to cluster", this);
		matrixInputPort.addSupportedPipe(CharPipe.class);

		OutputPort outputPort = new OutputPort(OUTPUTID, "[text] Plain text character output.", this);
		outputPort.addSupportedPipe(CharPipe.class);

		OutputPort outputJsonPort = new OutputPort(OUTPUTJSONID, "[JSON] text character output.", this);
		outputJsonPort.addSupportedPipe(CharPipe.class);

		// add I/O ports to instance
		super.addInputPort(inputPortVec);
		super.addInputPort(matrixInputPort);

		super.addOutputPort(outputPort);
		super.addOutputPort(outputJsonPort);
	}

	// process()
	@Override
	public boolean process() throws Exception {

		InputPort suffixTreeInput = this.getInputPorts().get(INPUT_ST_ID);
		InputPort matrixInput = this.getInputPorts().get(INPUT_MATRIX_ID);

		// byte input
		if (suffixTreeInput.isConnected()) {
			BytePipe pipe = (BytePipe) suffixTreeInput.getPipe();
			ObjectInputStream oiStream = new ObjectInputStream(pipe.getInput());
			this.corpusSer = (SuffixTreeInfoSer) oiStream.readObject();
			oiStream.close();

			// Prepare proper SuffixTreeInfo object "corpus" with all vectors.
			this.corpus = new SuffixTreeInfo();
			this.corpus.setNumberOfNodes(this.corpusSer.getNumberOfNodes());
			this.corpus.setNumberOfTypes(this.corpusSer.getNumberOfTypes());
			this.corpus.setTypes(this.corpusSer.getTypes());
			for (Node i : this.corpusSer.getNodes()) {
				this.corpus.addNode(i);
			}
			types = new ArrayList<Type>(this.corpus.getTypes());
		}
		// matrix input
		else if (matrixInput.isConnected()) {
			BufferedReader matrixInReader = new BufferedReader(matrixInput.getInputReader());
			NamedFieldMatrix matrix = NamedFieldMatrix.parseCSV(matrixInReader, matrixCsvDelimiter);

			types = new ArrayList<>(matrix.getRowAmount());
			int idCounter = 0;
			for (int i = 0; i < matrix.getRowAmount(); i++) {
				FeatureVector vector = new FeatureVector(matrix.getRow(i));
				Type type = new Type(vector);

				type.setTypeString(matrix.getRowName(i));
				type.setID(idCounter);
				types.add(type);
				
				idCounter += 1;
			}
		}
		// complain if no input or both connected
		else {
			throw new IllegalStateException("One and only one of the following inputs must be connected: " + INPUT_ST_ID
					+ " or " + INPUT_MATRIX_ID + ".");
		}

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

		Iterator<Pipe> jsonPipes = this.getOutputPorts().get(OUTPUTJSONID).getPipes(CharPipe.class).iterator();

		while (jsonPipes.hasNext()) {
			Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
			gson.toJson(this.clusterJsonRes, ((CharPipe) jsonPipes.next()).getOutput());
		}

		// put the results of each clustering into an appropriate JSON format.
		// But this is not that easy to do, as there are 3 different
		// output formats: FlatCluster, HierachialCluster, NeighborJoining. How
		// to combine the output formats to one?

		// close outputs
		this.closeAllOutputs();

		// success
		return true;
	}

	// applyProperties()
	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		this.corpusName = this.getProperties().getProperty(PROPERTYKEY_CORPNAME,
				this.getPropertyDefaultValues().get(PROPERTYKEY_CORPNAME));
		this.clusterType = this.getProperties().getProperty(PROPERTYKEY_CLUST,
				this.getPropertyDefaultValues().get(PROPERTYKEY_CLUST));
		this.matrixCsvDelimiter = this.getProperties().getProperty(PROPERTYKEY_MATRIX_CSV_DELIM,
				this.getPropertyDefaultValues().get(PROPERTYKEY_MATRIX_CSV_DELIM));

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

	// methods:

	private void clusterFlat(List<Type> types, String name) {
		FlatClusterer f_analysis = new FlatClusterer(types);

		List<FlatCluster> fClusters = f_analysis.analyse(3, 10);
		this.clusterJsonRes = fClusters;
		for (FlatCluster cluster : fClusters) {
			System.out.println(cluster.getMedoid().getString());
		}
		this.clustResult = f_analysis.toDot();

	}

	private void clusterHierarchical(List<Type> types, String name) {
		// usage of hierachial clustering
		HierarchicalClusterer h_analysis = new HierarchicalClusterer(types);
		// LOGGER.info("Hierarchisches Clustern von " + types.size() + "
		// Types");
		h_analysis.analyze();
		List<HierarchicalCluster> hClusters = h_analysis.getClusters();
		this.clusterJsonRes = hClusters;
		if (hClusters.size() == 1) {
			this.clustResult = h_analysis.toDot();

		} else
			System.err.println("Hierarchical Clustering didn't come up with exactly 1 cluster!");
	}

	private void clusterNeighborJoin(List<Type> types) {
		// usage of Neighbor Joining
		NeighborJoining nj = new NeighborJoining(types);
		// LOGGER.info("Neighbor Joining Clustern von " + types.size() + "
		// Types");
		nj.start();
		this.clustResult = nj.getTree();
		this.clusterJsonRes = nj.getJSONTree();
	}

	// end methods
}