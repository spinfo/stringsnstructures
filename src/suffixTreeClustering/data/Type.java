package suffixTreeClustering.data;

import suffixTreeClustering.features.BinaryFeatures;
import suffixTreeClustering.features.FeatureType;
import suffixTreeClustering.features.FeatureVector;
import suffixTreeClustering.features.TfDfFeatures;
import suffixTreeClustering.features.TfIdfFeatures;
import suffixTreeClustering.st_interface.SuffixTreeInfo;

public final class Type implements Comparable<Type> {

	private int ID;
	private FeatureVector vector;
	// private Double[] nodeWeights;
	private String string;

	public Type(int vectorLength) {
		// this.nodeWeights = new Double[vectorLength];
	}

	public Type() {
	}

	/**
	 * Creates a new vector representation for this type. Return value indicates
	 * if new vector was created or not (because it already existed).
	 * 
	 * @param corpus
	 *            data base for creating vector
	 * @return true iff new vector was created, false if vector already existed
	 */
	public void calculateVector(final SuffixTreeInfo corpus, FeatureType type) {
		if (vector == null) {
			switch (type) {
			case TF_IDF:
				vector = new TfIdfFeatures(this, corpus).vector();
				break;
			case TF_DF:
				vector = new TfDfFeatures(this, corpus).vector();
				break;
			case BINARY:
				vector = new BinaryFeatures(this, corpus).vector();
				break;
			default:
				System.err.println("Feature Type unknown");
			}
		}
	}

	// private void setVectorValue(int index, Double tfIdf) {
	// this.nodeWeights[index] = tfIdf;
	// }

	public Integer getVectorLength() {
		return vector.getLength();
	}

	public int getID() {
		return ID;
	}

	public void setID(int id) {
		this.ID = id;
	}

	// private Double[] getNodeWeights() {
	// return nodeWeights;
	// }
	//
	// private void setNodeWeights(Double[] nodeWeigthts) {
	// this.nodeWeights = nodeWeigthts;
	// }

	/**
	 * 2 Types are defined equal iff they have the same ID.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Type)
			return ((Type) obj).ID == this.ID ? true : false;
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * use Type ID to sort Types in sorted Maps or Sets
	 */
	@Override
	public int compareTo(Type o) {
		return this.ID - o.ID;
	}

	@Override
	public String toString() {
		return String.format("%s with ID %s and String content %s", this
				.getClass().getSimpleName(), this.ID, this.string);
	}

	public void setTypeString(String string) {
		this.string = string;
	}

	public String getString() {
		return string;
	}

	public String getLabel() {
		return string;
	}

	public FeatureVector getVector() {
		return vector;
	}
}