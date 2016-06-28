package modules.tree_building.suffixTreeClustering.clustering.hierarchical;

import modules.tree_building.suffixTreeClustering.data.Type;

/**
 * Class representing a pair of types. Useful for storing Document Vector
 * Distances.
 * 
 * @author neumannm
 */
public class TypePair {

	private Type type1;
	private Type type2;

	/**
	 * Constructor setting the 2 members of the pair.
	 * 
	 * @param t1
	 *            - first member
	 * @param t2
	 *            - second member
	 */
	public TypePair(Type t1, Type t2) {
		this.type1 = t1;
		this.type2 = t2;
	}

	Type getFirstType() {
		return type1;
	}

	Type getSecondType() {
		return type2;
	}

	@Override
	/**
	 * Two type pairs are defined to be equal if both of their members are equal, respectively.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof TypePair) {
			TypePair other = (TypePair) obj;
			return this.type1.equals(other.type1)
					&& this.type2.equals(other.type2);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return type1.hashCode() + type2.hashCode();
	}

	@Override
	public String toString() {
		return type1.getID() + "|" + type2.getID();
	}
}