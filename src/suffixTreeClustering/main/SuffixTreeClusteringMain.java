package suffixTreeClustering.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;

import suffixTreeClustering.clustering.flat.FlatCluster;
import suffixTreeClustering.clustering.flat.FlatClusterer;
import suffixTreeClustering.clustering.hierarchical.HierarchicalCluster;
import suffixTreeClustering.clustering.hierarchical.HierarchicalClusterer;
import suffixTreeClustering.clustering.neighborjoin.NeighborJoining;
import suffixTreeClustering.data.Node;
import suffixTreeClustering.data.Type;
import suffixTreeClustering.features.FeatureType;
import suffixTreeClustering.st_interface.SuffixTreeInfo;
import suffixTreeClustering.xml.XMLDataReader;
import util.LoggerConfigurator;
import util.TextInfo;

/**
 * Main class
 * 
 * @author neumannm
 */
public class SuffixTreeClusteringMain {

	private static final Logger LOGGER = Logger.getGlobal();

	private static Map<Integer, String> typeStrings;

	/*
	 * reads in the types list from the specified file and saves it to a Map to
	 * get a mapping from type IDs to type Strings
	 */
	private static void readTypesFromFile() {
		typeStrings = new HashMap<Integer, String>();
		try {
			String typeName;

			BufferedReader typeReader = new BufferedReader(new FileReader(
					TextInfo.getKwipTypePath()));

			LineNumberReader lnReader = new LineNumberReader(typeReader);

			int id;
			while ((typeName = lnReader.readLine()) != null) {
				id = lnReader.getLineNumber() - 1;
				typeStrings.put(id, typeName);
			}

			typeReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main method
	 * 
	 * @param args
	 *            (not used)
	 */
	public static void main(String[] args) {
		LoggerConfigurator.configGlobal();

		// Schritt 1: lies suffixTreeResult als Liste ein
		String workspacePath = TextInfo.getWorkspacePath();
		String textName = TextInfo.getTextName();
		String inputXml = TextInfo.getSuffixTreePath();

		readTypesFromFile(); // fill the typeID <-> typeString mapping

		XMLDataReader reader = new XMLDataReader(inputXml);
		// XMLDataReader reader = new
		// XMLDataReader("data/suffixTreeResult2.xml");

		SuffixTreeInfo corpus = reader.read(typeStrings);

		LOGGER.info("Number of nodes: " + corpus.getNumberOfNodes());
		LOGGER.info("Number of Types (=Documents): "
				+ corpus.getNumberOfTypes());

		for (Node node : corpus.getNodes()) {
			System.out.println(node);
		}

		Set<Type> types2 = corpus.getTypes();
		for (Type type : types2) {
			System.out.println(type);
		}

		System.out.println("---------------------------");

		// ************* User Input ***************************//
		FeatureType features = null;

		System.out
				.println("Which features should be used for vector creation?");
		System.out.println("1 - TF-IDF");
		System.out.println("2 - TF-DF");
		System.out.println("0 - Exit");

		Scanner scanner = new Scanner(System.in);
		if (!scanner.hasNextLine()) {
			LOGGER.severe("Console Input has no next line");
		}
		String line = scanner.nextLine();

		int answer = processLine(line);

		while (answer == -1 || answer > 2) {
			System.err.println("Undefined Answer! Please enter again:");
			line = scanner.nextLine();
			answer = processLine(line);
		}

		if (answer == 0) {
			scanner.close();
			System.exit(0);
		} else {
			if (answer == 1)
				features = FeatureType.TF_IDF;
			else if (answer == 2)
				features = FeatureType.TF_DF;
		}

		// Schritt 2: durchlaufe Liste, suche für jeden Knoten die unit und
		// notiere Betrag der unit nach tf/idf + speichere in Vektor
		for (Type doc : corpus.getTypes()) {
			LOGGER.info(String.format("Node weights for Type %s (%s)\n",
					doc.getID(), doc.getString()));
			doc.calculateVector(corpus, features);

			// for (Node node : corpus.getNodes()) {
			// Double tfIdf = new TfIdfFeatures(doc, corpus).tfIdf(node);
			//
			// // doc.setVectorValue(node.getNodeNumber() - 2, tfIdf);
			// }
			System.out.print("[");
			for (Double val : doc.getVector().getValues()) {
				System.out.print(val.doubleValue() + ", ");
			}
			System.out.println("]");
		}

		List<Type> types = new ArrayList<Type>(corpus.getTypes());
		System.out.println("****************\n");

		// ************* User Input ***************************//
		System.out.println("Which clustering algorithm should be used?");
		System.out.println("1 - Hierarchical Agglomerative Clustering");
		System.out.println("2 - Neighbor Joining");
		System.out.println("3 - Flat k-means Clustering");
		System.out.println("0 - Exit");

		line = scanner.nextLine();

		answer = processLine(line);

		while (answer == -1 || answer > 3) {
			System.err.println("Undefined Answer! Please enter again:");
			line = scanner.nextLine();
			answer = processLine(line);
		}

		switch (answer) {
		case 0:
			scanner.close();
			System.exit(0);
		case 1:
			clusterHierarchical(types, workspacePath, textName);
			break;
		case 2:
			clusterNeighborJoin(types);
			break;
		case 3:
			clusterFlat(types, workspacePath, textName);
			break;
		default:
			System.err.println("Undefined Answer! Exiting...");
			System.exit(0);
		}
	}

	private static void clusterFlat(List<Type> types, String path, String name) {
		FlatClusterer f_analysis = new FlatClusterer(types);
		LOGGER.info("Flaches Clustern von " + types.size() + " Types");
		List<FlatCluster> fClusters = f_analysis.analyse(3, 10);
		for (FlatCluster cluster : fClusters) {
			System.out.println(cluster);
		}
		String dot = f_analysis.toDot();
		// System.out.println(dot);
		saveToFile(dot, path, name);
	}

	private static void clusterHierarchical(List<Type> types, String PATH,
			String name) {
		// wende hierarchisches Clusterverfahren auf Vektoren an
		HierarchicalClusterer h_analysis = new HierarchicalClusterer(types);
		LOGGER.info("Hierarchisches Clustern von " + types.size() + " Types");
		h_analysis.analyze();
		List<HierarchicalCluster> hClusters = h_analysis.getClusters();

		if (hClusters.size() == 1) {
			String clusterToDot = h_analysis.toDot();
			// System.out.println(clusterToDot);
			saveToFile(clusterToDot, PATH, name);
		} else
			System.err
					.println("Hierarchical Clustering didn't come up with exactly 1 cluster!");

		System.out.println();
	}

	private static void clusterNeighborJoin(List<Type> types) {
		// wende Neighbor Joining an (ebenfalls hierarchisches
		// Clusterverfahren)
		NeighborJoining nj = new NeighborJoining(types);
		LOGGER.info("Neighbor Joining Clustern von " + types.size() + " Types");
		nj.start();
		nj.printTree();
	}

	private static int processLine(String line) {
		int answer = -1;
		line = line.trim();
		try {
			answer = Integer.parseInt(line);
		} catch (NumberFormatException e) {
			// System.err.println("Undefined");
			return -1;
		}
		return answer;
	}

	private static void saveToFile(String clusterToDot, String path, String name) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(TextInfo.getClusterPath()), "UTF-8"));
			writer.write(clusterToDot);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}