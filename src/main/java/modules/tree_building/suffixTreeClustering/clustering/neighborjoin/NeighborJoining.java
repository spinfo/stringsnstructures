package modules.tree_building.suffixTreeClustering.clustering.neighborjoin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import modules.tree_building.suffixTreeClustering.data.Type;

public class NeighborJoining {

	private static final String LOG_TAG = NeighborJoining.class.getSimpleName();
	private Logger logger;

	private List<Type> documents;
	private double[][] distanceMatrix;
	private int numCluster;
	private Map<Integer, String> aliasNames;
	private int[] alias; // merkt sich die urspruenglichen Indizes aus der
	// Distanzmatrix
	private double[] r; // Netto-Divergenzen
	private int newIndex;

	private NJNode root;
	private int best_i;
	private int best_j;
	private int aliasBesti;

	public NeighborJoining(List<Type> types) {
		this.logger = Logger.getLogger(LOG_TAG);

		this.documents = types;
		createDistanceMatrix();

		if (distanceMatrix.length < 3) {
			throw new IllegalArgumentException("ZU WENIGE TAXA IN DER MATRIX!");
		}
		if (!isSymmetric(distanceMatrix)) {
			throw new IllegalArgumentException("MATRIX IST UNSYMMETRISCH!");
		}
		if (!isAdditive(distanceMatrix)) {
			// throw new IllegalArgumentException("MATRIX IST NICHT ADDITIV!");
			logger.warning("Matrix ist nicht additiv");
		}

		this.numCluster = distanceMatrix.length;
		this.aliasNames = new HashMap<>();

		newIndex = 1;

		init();
	}

	private void init() {
		logger.info("Init NJ...");

		this.root = new NJNode(null, "root");

		for (int i = 0; i < numCluster; i++) {
			NJNode newTaxon = new NJNode(root, documents.get(i));
			aliasNames.put(i, documents.get(i).getString());
			root.addChild(newTaxon);
		}

		alias = new int[numCluster];
		for (int i = 0; i < numCluster; i++) {
			alias[i] = i;
		}

		r = new double[numCluster];
	}

	
	private boolean isAdditive(double[][] matrix) {
		boolean holds = false;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				for (int k = 0; k < matrix.length; k++) {
					for (int l = 0; l < matrix.length; l++) {
						if (i != j && i != k && i != l && j != k && j != l
								&& k != l) {
							holds = isSame(matrix[i][k] + matrix[j][l], 
									matrix[i][l] + matrix[j][k]) 
									&& isGreaterOrEqual(matrix[i][k] + matrix[j][l], 
											matrix[i][j] + matrix[k][l]);
							if (!holds)
								return false;
						}
					}
				}
			}
		}
		return true;
	}

	private boolean isGreaterOrEqual(double d1, double d2) {
		return Double.compare(d1, d2) == 0;
	}

	private boolean isSame(double d1, double d2) {
		return Double.compare(d1, d2) >= 0;
	}

	private boolean isSymmetric(double[][] matrix) {
		if (matrix.length != matrix[0].length) {
			System.err.println("Matrix is not symmetric!");
			return false;
		} else {
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					if (!(matrix[i][j] == (matrix[j][i]))) {
						System.out
								.printf("Values of matrix[%s][%s]=%s and matrix[%s][%s]=%s are not the same",
										i, j, matrix[i][j], j, i, matrix[j][i]);
						return false;
					}
					if (i == j && !(matrix[i][j] == (new Double(0.0)))) {
						System.out.printf(
								"Value of matrix[%s][%s]=%s is not 0", i, j,
								matrix[i][j]);
						return false;
					}
				}
			}
		}
		return true;
	}

	public void start() {
		logger.info("Start NJ...");

		while (true) {
			findNextPair();
			newBranchLengths();
			if (numCluster == 3) {
				break;
			}
			join();
		}

		finish();
	}

	private void findNextPair() {
		// r_i = sum(d_ij) / (N-2) for all j=1 to N
		for (int i = 0; i < numCluster; i++) {
			r[i] = 0.0f;
			for (int j = 0; j < numCluster; j++) {
				r[i] += getDistance(i, j);
			}
			r[i] /= (numCluster - 2);
		}

		best_i = 0;
		best_j = 0;

		double min = Double.POSITIVE_INFINITY;

		// calculate M and simultaniously, store the cell with smallest value
		for (int i = 0; i < numCluster - 1; i++) {
			for (int j = i + 1; j < numCluster; j++) {
				// M_ij = D_ij - r_i - r_j
				double M_ij = getDistance(i, j) - r[i] - r[j];

				if (M_ij < min) {
					// if there are multiple smallest values, the first pair
					// will be stored...
					min = M_ij;
					best_i = i;
					best_j = j;
				}
			}
		}

		aliasBesti = alias[best_i];
	}

	private double getDistance(int i, int j) {
		return distanceMatrix[alias[i]][alias[j]];
	}

	private void newBranchLengths() {
		// nodes to be joined
		NJNode child1 = root.getChildByName(aliasNames.get(alias[best_i]));
		NJNode child2 = root.getChildByName(aliasNames.get(alias[best_j]));

		// new branch lengths
		double dij = getDistance(best_i, best_j);
		// L_iu = (D_ij/2) + ((r_i - r_j) / 2)
		double liu = ((dij) / 2) + ((r[best_i] - r[best_j]) / 2);
		double lju = ((dij) - liu);

		child1.setBranchLength(liu);
		child2.setBranchLength(lju);
	}

	private void join() {
		// Update distances
		for (int k = 0; k < numCluster; k++) {
			if (k != best_i && k != best_j) {
				int ak = alias[k];
				distanceMatrix[ak][aliasBesti] = distanceMatrix[aliasBesti][ak] = updatedDistance(
						best_i, best_j, k);
			}
		}
		distanceMatrix[aliasBesti][aliasBesti] = 0.0;

		joinChilds(this.root, best_i, best_j);

		// Update alias
		for (int i = best_j; i < numCluster - 1; i++) {
			alias[i] = alias[i + 1];
		}

		numCluster--;
	}

	private void joinChilds(NJNode root, int n1, int n2) {
		if (n1 == n2) {
			throw new IllegalArgumentException("CHILDREN MUST BE DIFFERENT");
		}

		// create new Node "U_i" with the root node as parent
		// joined nodes will be its children
		String newName = "U" + (newIndex++);
		NJNode newNode = new NJNode(root, newName);

		// nodes to be joined
		NJNode child1 = root.getChildByName(aliasNames.get(alias[best_i]));
		NJNode child2 = root.getChildByName(aliasNames.get(alias[best_j]));

		// reset structure
		newNode.addChild(child1);
		newNode.addChild(child2);
		newNode.addDocuments(child1.getClusteredDocuments());
		newNode.addDocuments(child2.getClusteredDocuments());
		child1.setParent(newNode);
		child2.setParent(newNode);

		root.removeChild(child1);
		root.removeChild(child2);
		root.addChild(newNode);

		aliasNames.put(aliasBesti, newName);
	}

	private double updatedDistance(int i, int j, int k) {
		// new distances of taxa:
		// D_xU = D_ix + D_jx - D_ij
		return (getDistance(k, i) + getDistance(k, j) - getDistance(i, j)) / 2.0;
	}

	private void finish() {
		getRoot().getChild(best_j).setBranchLength(
				updatedDistance(best_i, best_j, 2));

		distanceMatrix = null;
	}

	private void createDistanceMatrix() {
		double[][] temp = new double[documents.size()][documents.size()];

		for (int i = 0; i < temp.length; i++) {
			for (int j = 0; j < temp.length; j++) {
				temp[i][j] = computeDistance(documents.get(i), documents.get(j));
			}
		}
		this.distanceMatrix = temp;
	}

	private Double computeDistance(Type type1, Type type2) {
		Double distance = type1.getVector().distance(type2.getVector());
		return distance;
	}

	public NJNode getRoot() {
		return root;
	}

	public void printTree() {
		System.out.println("graph NJTree {");
		if (root.getChildren().isEmpty()) {
			System.out.println(root.getLabel() + ";");
		} else {
			for (NJNode child : root.getChildren()) {
				System.out.print(root.getLabel() + " -- ");
				printSubTree(child);
			}
		}
		System.out.println("}");
	}
	
	public String getTree() {
		String njResult = "";
		njResult += "graph NJTree {";
		if (root.getChildren().isEmpty()) {
			njResult += root.getLabel() + ";";
		} else {
			for (NJNode child : root.getChildren()) {
				njResult += root.getLabel() + " -- ";
				printSubTree(child);
			}
		}
		njResult +="}";
		return njResult;
	}

	private void printSubTree(NJNode node) {
		System.out.println(node.getLabel() + "[label = "
				+ node.getBranchLength() + "];");
		for (NJNode child : node.getChildren()) {
			System.out.print(node.getLabel() + " -- ");
			printSubTree(child);
			System.out.println();
		}
	}
}