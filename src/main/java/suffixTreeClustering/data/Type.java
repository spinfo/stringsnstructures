package suffixTreeClustering.data;

import java.util.ArrayList;
import java.util.List;

import suffixTreeClustering.features.BinaryFeatures;
import suffixTreeClustering.features.FeatureType;
import suffixTreeClustering.features.FeatureVector;
import suffixTreeClustering.features.TfDfFeatures;
import suffixTreeClustering.features.TfIdfFeatures;
import suffixTreeClustering.st_interface.SuffixTreeInfo;

public final class Type implements Comparable<Type> {

	private int ID;
	private FeatureVector vector;
	private String string;

	private List<Token> tokens;

	public Type() {
		tokens = new ArrayList<>();
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

	public Integer getVectorLength() {
		return vector.getLength();
	}

	public void addToken(Token token) {
		tokens.add(token);
	}

	public int getID() {
		return ID;
	}

	public void setID(int id) {
		this.ID = id;
	}

	/**
	 * 2 Types are defined equal iff they have the same ID and the same string.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Type))
			return false;
		if (obj == this)
			return true;

		Type other = (Type) obj;
		return other.ID == this.ID && this.string.contentEquals(other.string);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + string.hashCode();
		result = 31 * result + ID;
		return result;
	}

	/**
	 * use Type string and ID to sort Types in sorted Maps or Sets.
	 */
	@Override
	public int compareTo(Type o) {
		int stringDiff = this.string.compareTo(o.string);
		if(stringDiff != 0)
			return stringDiff;
		//strings are equal, compare IDs
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

	public FeatureVector getVector() {
		return vector;
	}

	public List<Token> getTokens() {
		return tokens;
	}
}