package modules.tree_building.suffixTreeClustering.clustering.hierarchical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modules.tree_building.suffixTreeClustering.data.Type;

public class HierarchicalClusterer {

	private List<Type> documents;
	private List<HierarchicalCluster> clusters;
	int N; // number of Types/Documents
	private int currentNumberOfClusters;
	private SimilarityMeasure s;
	private Map<TypePair, Double> vectorDistances;

	/**
	 * Constructor. Initializes documents to cluster and similarity measure.
	 * 
	 * @param types
	 *            - List of documents to cluster.
	 */
	public HierarchicalClusterer(List<Type> types) {
		this.documents = types;
		N = this.documents.size();
		this.clusters = new ArrayList<HierarchicalCluster>();

		s = SimilarityMeasure.AVERAGE_LINK;
	}

	/**
	 * Hierarchical Clustering.
	 */
	public void analyze() {

		this.vectorDistances = new HashMap<TypePair, Double>();

		/*
		 * in the beginning, each document constitutes 1 cluster
		 */
		for (int i = 0; i < N; i++) {
			Type type = documents.get(i);
			HierarchicalCluster cluster = new HierarchicalCluster(type);
			// set cluster topic
			cluster.addTopic(type.getString());
			clusters.add(cluster);
		}
		currentNumberOfClusters = clusters.size();

		cluster();
	}

	/*
	 * Clustering Method. As long as more than 1 Cluster is left (halting
	 * criterion), combine nearest (according to similarity measure) clusters.
	 */
	private void cluster() {
		while (currentNumberOfClusters > 1) {
			if (currentNumberOfClusters == documents.size()) {
				getVectorDistances();
			}

			combineNearestCluster();

			currentNumberOfClusters--;
		}
	}

	/*
	 * Set up vector distances to be used for clustering.
	 */
	private void getVectorDistances() {
		for (int i = 0; i < documents.size() - 1; i++) {
			for (int j = i + 1; j < documents.size(); j++) {
				Type type1 = documents.get(i);
				Type type2 = documents.get(j);
				Double dist = type1.getVector().distance(type2.getVector());
				TypePair pair = new TypePair(type1, type2);
				Double put = vectorDistances.put(pair, dist);
				if (put != null) {
					System.err.println("Map already contained key " + pair);
				}
			}
		}
	}

	/*
	 * Combine the 2 nearest clusters. Add combined cluster to set of clusters
	 * and delete the 2 clusters.
	 */
	private void combineNearestCluster() {
		Double smallestClusterDist = Double.POSITIVE_INFINITY;
		int c1 = 0;
		int c2 = 0;
		for (int i = 0; i < clusters.size() - 1; i++) {
			HierarchicalCluster cluster1 = clusters.get(i);
			for (int j = i + 1; j < clusters.size(); j++) {
				HierarchicalCluster cluster2 = clusters.get(j);
				Double clusterDist = getClusterDistance(cluster1, cluster2);
				if (clusterDist < smallestClusterDist) {
					smallestClusterDist = clusterDist;
					c1 = i;
					c2 = j;
				}
			}
		}
		System.out.println("combine: " + clusters.get(c1) + " and "
				+ clusters.get(c2));
		HierarchicalCluster newCluster = new HierarchicalCluster(
				clusters.get(c1), clusters.get(c2));
		clusters.remove(c2);
		clusters.set(c1, newCluster);
	}

	/*
	 * Calculate the cluster distance between 2 clusters according to given
	 * similarity measure.
	 */
	private Double getClusterDistance(HierarchicalCluster c1,
			HierarchicalCluster c2) {
		List<Type> vecsInC1 = c1.getAllTypes();
		List<Type> vecsInc2 = c2.getAllTypes();
		Double minDist = Double.POSITIVE_INFINITY;
		Double maxDist = 0d;
		Double averageDist = 0d;

		for (int i = 0; i < vecsInC1.size(); i++) {
			Type vecID1 = vecsInC1.get(i);
			for (int j = 0; j < vecsInc2.size(); j++) {
				Type vecID2 = vecsInc2.get(j);
				Double dist = 0d;
				TypePair pair;

				if (vecID2.getID() < vecID1.getID()) {
					pair = new TypePair(vecID2, vecID1);
				} else {
					pair = new TypePair(vecID1, vecID2);
				}

				if (!vectorDistances.containsKey(pair))
					System.out.println("VectorDistances does not contain key "
							+ pair);

				dist = vectorDistances.get(pair);
				averageDist += dist;
				if (dist < minDist) {
					minDist = dist;
				}
				if (dist > maxDist) {
					maxDist = dist;
				}
			}
		}
		switch (s) {
		case SINGLE_LINK:
			return minDist;
		case COMPLETE_LINK:
			return maxDist;
		case AVERAGE_LINK:
			averageDist /= (vecsInC1.size() + vecsInc2.size());
			return averageDist;
		default:
			throw new RuntimeException("invalid similarity measure");
		}
	}

	/**
	 * @return A Graphviz DOT represention of the clusters
	 */
	public String toDot() {
		StringBuilder builder = new StringBuilder();
		HierarchicalCluster root = this.clusters.get(0);

		return String.format("digraph{\n%s}", clusterToDot(root, builder));
	}

	private static String clusterToDot(HierarchicalCluster cluster,
			StringBuilder builder) {
		if (cluster != null) { // Pre-order: 1. root
			int clusterID = cluster.getAge();

			builder.append(String.format(clusterID + "[label = \"%s\"];",
					cluster.getTopics()/* .iterator().next() */));

			if (cluster.getLeftChild() != null) {
				builder.append(String.format("%s -> %s;\n", clusterID, cluster
						.getLeftChild().getAge()));
			}
			clusterToDot(cluster.getLeftChild(), builder); // 2. left
			if (cluster.getRightChild() != null) {
				builder.append(String.format("%s -> %s;\n", clusterID, cluster
						.getRightChild().getAge()));
			}
			clusterToDot(cluster.getRightChild(), builder); // 3. right
		}
		return builder.toString();
	}

	/**
	 * Get the Clustering result.
	 * 
	 * @return List of clusters.
	 */
	public List<HierarchicalCluster> getClusters() {
		return clusters;
	}
}