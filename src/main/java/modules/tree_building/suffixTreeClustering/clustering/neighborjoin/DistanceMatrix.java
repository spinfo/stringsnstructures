package modules.tree_building.suffixTreeClustering.clustering.neighborjoin;

public class DistanceMatrix {

	private String[] names; // wird später ausgetauscht gegen Type[]
	private double[][] distances;
	private int[] alias;
	private int numElements;
	private double maxDistance;
	private double minDistance;

	public DistanceMatrix(double[][] distances, String[] names) {
		if (distances.length != names.length)
			throw new IllegalArgumentException(
					"Matrix and Header lengths do not match!");

		this.numElements = names.length;
		this.distances = distances;
		this.names = names;

		this.alias = new int[numElements];
		for (int i = 0; i < numElements; i++) {
			alias[i] = i;
		}

		this.maxDistance = Double.NEGATIVE_INFINITY;
		this.minDistance = Double.POSITIVE_INFINITY;
	}

	public boolean isSymmetric() {
		return checkForSymmetry(distances);
	}

	private boolean checkForSymmetry(double[][] matrix) {
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

	public double getDistance(int indexI, int indexJ) {
		assert (indexI >= 0 && indexI < numElements && indexJ >= 0 && indexJ < numElements) : "ERROR: index out of bounds!";

		return distances[indexI][indexJ];
	}

	public void setDistance(int indexI, int indexJ, double value) {
		assert (indexI >= 0 && indexI < numElements && indexJ >= 0 && indexJ < numElements) : "ERROR: index out of bounds!";

		if (indexI != indexJ) {
			this.distances[indexI][indexJ] = value;

			if (minDistance > value && value >= 0.0f) {
				minDistance = value;
			}

			if (maxDistance < value && value >= 0.0f) {
				maxDistance = value;
			}
		}
	}

	public int getSize() {
		return numElements;
	}

	public double getMaxDistance() {
		return maxDistance;
	}

	public double getMinDistance() {
		return minDistance;
	}

	public int[] getAlias() {
		return alias;
	}

	public double[][] getDistances() {
		return distances;
	}

	public String[] getNames() {
		return names;
	}

	public int getNumElements() {
		return numElements;
	}

}
