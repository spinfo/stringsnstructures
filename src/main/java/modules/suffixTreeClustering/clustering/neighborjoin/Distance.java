package modules.suffixTreeClustering.clustering.neighborjoin;

import modules.suffixTreeClustering.data.Type;

public class Distance {

	private Type t1;
	private Type t2;
	private Float distance;

	public Distance(Type type1, Type type2, Float temp) {
		this.distance = temp;
		this.t1 = type1;
		this.t2 = type2;
	}

	public Type getT1() {
		return t1;
	}

	public Type getT2() {
		return t2;
	}

	public Float getDistance() {
		return distance;
	}
}