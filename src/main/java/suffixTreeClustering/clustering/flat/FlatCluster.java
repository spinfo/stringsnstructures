package suffixTreeClustering.clustering.flat;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import suffixTreeClustering.data.Type;

/**
 * A cluster of documents.
 * 
 * @author Fabian Steeg (fsteeg)
 */
public class FlatCluster implements Iterable<Type> {
	Set<Type> documents = new CopyOnWriteArraySet<Type>();
	private Type medoid = null;

	public FlatCluster(final Type document) {
		this.medoid = document;
		this.documents.add(document);
	}

	/**
	 * @return The medoid of this cluster, i.e. the document with the highest
	 *         similarity to the other documents, the most central member of the
	 *         cluster in the vector space
	 */
	public Type getMedoid() {
		return medoid;
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
	 * @return A label for the given cluster
	 */
	public String getLabel() {
		/*
		 * Eine einfache Heuristik: Das Label ist das topic des Medoids, auf
		 * ungefähr gleiche Länge getrimmt:
		 */
		String label = medoid.getString();
		label = label.substring(0, Math.min(8, label.length()));
		return label.toUpperCase();
	}

	/**
	 * @return The documents of this cluster
	 */
	public Set<Type> getDocuments() {
		return Collections.unmodifiableSet(documents);
	}

	/*
	 * Sog. package-private API (in Java die default-Sichtbarkeit), d.h. nur vom
	 * eigenen Package aus sichtbar. Das documents-Attribut ist hier package
	 * private. Die Manipulation eines clusters ist so nur innerhalb des Package
	 * möglich. Der Cluster der als Ergebnis zurückgegeben wird ist so nicht
	 * mehr manipulierbar. Die Veränderung des Clusters ist so hinter der public
	 * API weggekapselt.
	 */

	/**
	 * Recompute the medoid of this cluster based on its members
	 */
	void recomputeMedoid() {
		/*
		 * Der Schwerpunkt eines Clusters: Das Element mit der geringsten
		 * durchschnittlichen Entfernung, d.h. der größten Ähnlichkeit, zu allen
		 * anderen Elementen im Cluster.
		 */
		Double maxMean = Double.NEGATIVE_INFINITY;
		Type maxType = null;
		for (Type iDoc : documents) {
			Double simSum = 0d;
			for (Type jDoc : documents) {
				/*
				 * i ist der aktuelle Kandidat: wir messen die Ähnlichkeit von i
				 * zu allen j, der i mit der höchsten durchschnittlichen
				 * Ähnlichkeit gewinnt.
				 */
				if (!iDoc.equals(jDoc)) {
					/* jeder Paar i,j Ähnlichkeit berechnen: */
					Double sim = iDoc.getVector().similarity(jDoc.getVector());
					/* Ähnlichkeit aufsummieren: */
					simSum += sim;
				}
			}
			/* Die durchschnittliche Ähnlichkeit von i zu allen anderen: */
			Double meanSim = simSum / getDocuments().size() - 1;
			if (meanSim > maxMean) {
				maxMean = meanSim;
				/* i ist der beste Schwerpunkt bisher: */
				maxType = iDoc;
			}
		}
		if (maxType != null) {
			/*
			 * Hier fehlte im Eifer der schon überzogenen letzten Minuten im
			 * Seminar das kleine Wörtchen "return", so dass trotz Ergebnis
			 * immer null zurückgegeben wurde, ansonsten war es soweit komplett.
			 */
			this.medoid = maxType;
		} else {
			throw new IllegalStateException("No max found!");
		}
	}
}
