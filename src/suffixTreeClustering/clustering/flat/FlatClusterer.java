package suffixTreeClustering.clustering.flat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import suffixTreeClustering.data.Type;

/**
 * Simple flat k-means clustering.
 * 
 * @author Fabian Steeg (fsteeg)
 */
public final class FlatClusterer {

	private List<FlatCluster> clusters;
	private List<Type> documents;

	/**
	 * K-Means clustering of the given documents, as part of the given corpus
	 * 
	 * @param corpus
	 *            The corpus the document selection is a part of
	 * @param documents
	 *            The documents to cluster
	 */
	public FlatClusterer(final List<Type> documents) {
		/*
		 * Da wir beim iterieren über die Cluster diese manipulieren (wir
		 * verschieben die Elemente in den passendsten Cluster), muessen wir
		 * eine List-Implementierung benutzen, die das unterstützt (die
		 * Alternative wäre das Anlegen neuer Listen und sowas), wie etwa
		 * CopyOnWriteArrayList, die (wie der umständliche Name sagt), das
		 * zugrunde liegende Array bei Schreibe-Operationen kopiert, was sich
		 * nachteilig auf die Performance auswirkt, wenn man nicht viel iteriert
		 * und wenig schreibt:
		 */
		clusters = new CopyOnWriteArrayList<FlatCluster>();
		this.documents = documents;
	}

	/**
	 * Single clustering into k clusters.
	 * 
	 * @param k
	 *            The number of clusters to partition the documents into
	 * @param iterations
	 *            The number of iterations
	 * @return The k clusters
	 */
	public List<FlatCluster> analyse(final int k, final int iterations) {
		Collections.shuffle(documents);
		// Initiale Mittelpunkte: k zufällige Dokumente
		for (int i = 0; i < k; i++) {
			FlatCluster cluster = new FlatCluster(documents.get(i));
			clusters.add(cluster);
		}
		/* Zu Beginn bilden alle Dokumente einen Cluster: */
		clusters.get(0).documents.addAll(documents);
		System.out.println(String.format(
				"%s-means clustering with %s iterations... ", k, iterations));
		for (int i = 0; i < iterations; i++) {
			/* In jeder iteration stellen wir die Cluster neu ein: */
			this.clusters = recompute(clusters);
			/* Eine simple Form von Fortschrittsanzeige: */
			System.out.print(String.format("%1.2f ", getPurity()));
		}
		System.out.println(String.format(
				"Purity for k=%s: %1.2f, clusters: %s", k, getPurity(),
				toString()));
		return this.clusters;
	}

	private List<FlatCluster> recompute(final List<FlatCluster> clusters) {
		List<FlatCluster> result = new CopyOnWriteArrayList<FlatCluster>(
				clusters);
		/* Wir betrachten jedes Dokument in jedem Cluster: */
		for (FlatCluster currentCluster : result) {
			for (Type document : currentCluster) {
				/*
				 * Und suchen zu diesem den passendsten Cluster, d.h. den
				 * Cluster, dessen Schwerpunkt-Dokument dem Dokument am
				 * ähnlichsten, d.h. im Vektorraum am nächsten ist:
				 */
				Double min = Double.POSITIVE_INFINITY;
				FlatCluster nearest = null;
				/* Ähnlichsten Mittelpunkt suchen: */
				for (FlatCluster clusterToCompare : clusters) {
					Type center = clusterToCompare.getMedoid();
					Double distance = document.getVector().distance(
							center.getVector());
					if (distance < min) {
						min = distance;
						nearest = clusterToCompare;
					}
				}
				if (nearest != null) {
					/*
					 * Wenn ein solcher ähnlichster Schwerpunkt gefunden wurde,
					 * entfernen wir das aktuelle Dokument aus seinem, d.h.
					 * diesem Cluster, und fügen es in den Cluster ein, dessen
					 * Mittelpunkt gewonnen hatte:
					 */
					// TODO: hier stimmt was nicht, Dokumente kommen doppelt
					// vor... Set verwenden?
					currentCluster.documents.remove(document);
					nearest.documents.add(document);
					/*
					 * (und damit wir hier diese Sache so machen können müssen
					 * die Listen CopyOnWriteArrayList sein)
					 */
				}
			}
			/*
			 * Nach der Neuberechnung des aktuellen Clusters setzen wir seinen
			 * Medoid neu fest:
			 */
			currentCluster.recomputeMedoid();
		}
		return result;
	}

	/**
	 * Multiple clusterings for different values of k.
	 * 
	 * @param clusterStart
	 *            The minimum k cluster count
	 * @param clusterEnd
	 *            The maximum k cluster count
	 * @param iterations
	 *            The number of iterations to use when clustering with each k
	 * @return The clusters for each different k between clusterStart and
	 *         clusterEnd (inclusive)
	 */
	public List<List<FlatCluster>> analyse(final int clusterStart,
			final int clusterEnd, final int iterations) {
		final List<List<FlatCluster>> clustersForKs = new CopyOnWriteArrayList<List<FlatCluster>>();
		/*
		 * Wir lassen die verschiedenen, völlig unabhängigen Versuchsaufbauten,
		 * nämlich mit unterschiedlicher Clusterzahl (und leicht einbaubar
		 * unterschiedlich vielen Iterationen) parallel laufen. Hier können wir
		 * parallele Durchführung der verschiedenen Aufbauten einstellen. Ist
		 * auf 1 gestellt um einfacher die Ausgabe interpretieren zu können und
		 * fürs Debuggen. In Produktionscode würde man so einen Wert z.B. in
		 * eine Properties-Datei auslagern.
		 */
		ExecutorService exec = Executors.newFixedThreadPool(clusterEnd
				- clusterStart); // threads
		for (int i = clusterStart; i <= clusterEnd; i++) {
			final int k = i;
			exec.execute(new Runnable() {
				public void run() {
					FlatClusterer c = new FlatClusterer(documents);
					clusters = c.analyse(k, iterations);
					/* Wir sammeln die Ergebnisse für jedes k: */
					clustersForKs.add(clusters);
				}
			});
		}
		exec.shutdown();
		try {
			/** Basically no timeout */
			exec.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return clustersForKs;
	}

	/**
	 * @return The purity of the clusters
	 */
	public Float getPurity() {
		/*
		 * Die Purity ist die Anzahl der Elemente in jedem Cluster, die in
		 * diesem Cluster am häufigsten vorkommen, geteilt durch die Anzahl
		 * aller Elemente. So ist sie ein Maß für die Homogenität der Cluster.
		 * Ein Wert von 1 heißt dabei maximale Purity: Jeder Cluster enthält nur
		 * eine Art von Element.
		 */
		int maxSum = 0;
		int sum = 0;
		for (FlatCluster currentCluster : clusters) {
			/*
			 * Dazu müssen wir erstmal schauen, was denn die häufigste Art von
			 * Element in jedem Cluster ist, d.h. wir betrachten zunächst jedes
			 * Document im Cluster und zählen wie oft es vorkommt:
			 */
			Map<String, Integer> frequencies = new HashMap<String, Integer>();
			for (Type document : currentCluster) {
				Integer f = frequencies.get(document.toString());
				if (f == null) {
					f = 0;
				}
				frequencies.put(document.toString(), f + 1);
			}
			/*
			 * Dann schauen wir uns alle Häufigkeiten an und wählen die höchste:
			 */
			Collection<Integer> values = frequencies.values();
			int max = 0;
			for (Integer integer : values) {
				max = Math.max(integer, max);
				/* Die Gesamtsumme zählen wir immer: */
				sum += integer;
			}
			/*
			 * Zur Summe der maximalen Häufigkeiten addieren wir nur die
			 * Häufigkeit des Häufigsten
			 */
			maxSum += max;
		}
		Float result = maxSum / (float) sum;
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("|");
		for (int i = 0; i < clusters.size(); i++) {
			FlatCluster cluster = clusters.get(i);
			builder.append(String.format("%s:%s|", cluster.documents.size(),
					clusters.get(i).getMedoid().toString())); // TODO: anpassen
																// (war:
																// getTopic())
		}
		return builder.toString();
	}

	/**
	 * @return A Graphviz DOT represention of the clusters
	 */
	public String toDot() {
		/*
		 * Wir beginnen mit der Graph-Definition, die alle Cluster umschließen
		 * soll:
		 */
		StringBuilder builder = new StringBuilder(
				String.format(
						"graph clusters { label=\"Purity: %s\" node[shape=record] rankdir=TD\n",
						getPurity()));
		for (int i = 0; i < clusters.size(); i++) {
			FlatCluster cluster = clusters.get(i);
			String label = cluster.getLabel();
			/* Dann schreiben wir für jeden Cluster einen Knoten raus... */
			builder.append(String.format("\t%s[label = \"{%s|%s",
					cluster.hashCode(), label, cluster.documents.size()));
			for (Type document : cluster) {
				/*
				 * ...der in einem Kästchen jedes Dokument im Cluster
				 * beschreibt:
				 */
				builder.append(String.format("|%s", document.toString()));
			}
			builder.append(String.format("}\"]\n"));
		}
		return builder.append("}\n").toString();
	}
}
