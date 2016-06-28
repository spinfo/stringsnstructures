package modules.tree_building.suffixTreeClustering.main;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Logger;

import common.KwipXmlReader;
import common.LoggerConfigurator;
import common.TextInfo;
import modules.tree_building.suffixTreeClustering.clustering.flat.FlatCluster;
import modules.tree_building.suffixTreeClustering.clustering.flat.FlatClusterer;
import modules.tree_building.suffixTreeClustering.clustering.hierarchical.HierarchicalCluster;
import modules.tree_building.suffixTreeClustering.clustering.hierarchical.HierarchicalClusterer;
import modules.tree_building.suffixTreeClustering.clustering.neighborjoin.NeighborJoining;
import modules.tree_building.suffixTreeClustering.data.Node;
import modules.tree_building.suffixTreeClustering.data.Type;
import modules.tree_building.suffixTreeClustering.features.FeatureType;
import modules.tree_building.suffixTreeClustering.st_interface.SuffixTreeInfo;
import modules.tree_building.suffixTreeClustering.xml.XMLDataReader;

/**
 * Main class
 * 
 * @author neumannm
 */
public class SuffixTreeClusteringMain {

	private static final Logger LOGGER = Logger.getGlobal();

	
	/**
	 * 
	 */
	public static void run() {
		LoggerConfigurator.configGlobal();

		// Schritt 1: lies suffixTreeResult als Liste ein
		String workspacePath = TextInfo.getWorkspacePath();
		String textName = TextInfo.getTextName();

		// ********************** //
		KwipXmlReader kwipReader = new KwipXmlReader(TextInfo.getKwipXMLPath());
		List<Type> kwipTypes = kwipReader.read();
		for (Type type : kwipTypes)
			System.out.println(type);

		Map<Integer, String> typeStrings = null;
		try {
			typeStrings = fillTypeStrings(kwipTypes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		XMLDataReader reader = new XMLDataReader(TextInfo.getSuffixTreePath());
		SuffixTreeInfo corpus = reader.read(typeStrings);

		LOGGER.info("Number of nodes: " + corpus.getNumberOfNodes());
		LOGGER.info("Number of Types (=Documents): "
				+ corpus.getNumberOfTypes());

		for (Node node : corpus.getNodes()) {
			System.out.println(node);
		}

		for (Type type : corpus.getTypes()) {
			System.out.println(type);
		}

		System.out.println("---------------------------");

		// ************* User Input ***************************//
		FeatureType features = null;

		System.out
				.println("Which features should be used for vector creation?");
		System.out.println("1 - TF-IDF");
		System.out.println("2 - TF-DF");
		System.out.println("3 - Binary");
		System.out.println("0 - Exit");

		Scanner scanner = new Scanner(System.in);
		if (!scanner.hasNextLine()) {
			LOGGER.severe("Console Input has no next line");
		}
		String line = scanner.nextLine();

		int answer = processLine(line);

		while (answer < 0 || answer > 3) {
			System.err.println("Undefined Answer! Please enter again:");
			line = scanner.nextLine();
			answer = processLine(line);
		}

		switch (answer) {
		case 1:
			features = FeatureType.TF_IDF;
			break;
		case 2:
			features = FeatureType.TF_DF;
			break;
		case 3:
			features = FeatureType.BINARY;
			break;
		default:
			scanner.close();
			System.exit(0);
			break;
		}

		// Schritt 2: durchlaufe Liste, suche für jeden Knoten die unit und
		// notiere Betrag der unit nach tf/idf + speichere in Vektor
		for (Type doc : corpus.getTypes()) {
			LOGGER.info(String.format("Node weights for Type %s (%s)\n",
					doc.getID(), doc.getString()));
			doc.calculateVector(corpus, features);

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

	private static Map<Integer, String> fillTypeStrings(List<Type> kwipTypes) throws Exception {
		Map<Integer, String> toReturn = new TreeMap<>();
		for (Type type : kwipTypes) {
			if(!toReturn.containsKey(type.getID()))
				toReturn.put(type.getID(), type.getString());
			else throw new Exception("There should not be 2 types with same ID!");
		}
		return toReturn;
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
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(TextInfo.getClusterPath()), "UTF-8"))) {
			writer.write(clusterToDot);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}