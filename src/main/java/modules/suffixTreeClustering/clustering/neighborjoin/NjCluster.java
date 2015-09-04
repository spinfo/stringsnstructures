package modules.suffixTreeClustering.clustering.neighborjoin;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import modules.suffixTreeClustering.data.Type;

public class NjCluster implements Iterable<Type> {

	List<Type> documents = new CopyOnWriteArrayList<Type>();

	public NjCluster(final Type... types) {
		this.documents.addAll(Arrays.asList(types));
	}

	/*
	 * Dadurch dass wir Iterable implementieren können wir unsere Cluster in
	 * einer foreach-Schleife verwenden: for(Document doc : cluster){ ... }
	 */
	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Type> iterator() {
		return documents.iterator();
	}

	/**
	 * @return The documents of this cluster
	 */
	public List<Type> getDocuments() {
		return Collections.unmodifiableList(documents);
	}

	/**
	 * @return A label for the given cluster
	 */
	public String getLabel() {
		// TODO: what is the label of a cluster?
		String label = "";
		label = label.substring(0, Math.min(8, label.length()));
		return label.toUpperCase();
	}

	@Override
	public String toString() {
		return String.format("NJ_Cluster containing document(s): %s",
				this.documents);
	}

	/* 2 Cluster sind dann gleich, wenn sie dieselben Dokumente enthalten */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NjCluster))
			return false;
		if (obj == this)
			return true;

		NjCluster other = (NjCluster) obj;
		return other.documents.containsAll(this.documents);
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + documents.hashCode();
		return result;
	}
}
