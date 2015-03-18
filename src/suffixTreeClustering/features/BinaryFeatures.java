package suffixTreeClustering.features;

import java.util.ArrayList;
import java.util.List;

import suffixTreeClustering.data.Node;
import suffixTreeClustering.data.Type;
import suffixTreeClustering.st_interface.SuffixTreeInfo;

/**
 * Node Weight Calculation based on binary values - Node weight is 1 if document
 * visited node, 0 if not.
 * 
 * @author neumannm
 */
public final class BinaryFeatures {

	private SuffixTreeInfo corpus;
	private Type document;

	/**
	 * Constructor
	 * 
	 * @param document
	 *            - Document for which term weights are calculated.
	 * @param corpus
	 *            - data structure containing all documents and nodes
	 */
	public BinaryFeatures(final Type document, final SuffixTreeInfo corpus) {
		if (corpus.getNodes().size() == 0) {
			throw new IllegalArgumentException("Empty Corpus!");
		}
		this.document = document;
		this.corpus = corpus;
	}

	/**
	 * Create vector for this document.
	 * 
	 * @return FeatureVector
	 */
	public FeatureVector vector() {
		// Ein Vektor für dieses Dokument ist...
		List<Double> values = new ArrayList<Double>();
		// ...für jeden Term im Vokabular... (=jeder Knoten im SuffixTree)
		List<Node> terms = corpus.getNodes();

		for (Node node : terms) {
			// der boolesche Wert des Terms: hat Knoten besucht oder nicht
			boolean value = value(node);
			if (value) {
				values.add(1.0);
			} else
				values.add(0.0);
		}

		return new FeatureVector(values.toArray(new Double[values.size()]));
	}

	/**
	 * Determine if document visited node.
	 * 
	 * @param node
	 *            node to be examined
	 * @return true iff document visited node
	 */
	public boolean value(Node node) {
		// tf(n,d) = Anzahl, wie oft Dokument d Knoten n durchlaufen hat
		Integer tf = node.getTermfrequencyFor(document);
		// falls Dokument d Knoten n durchlaufen hat, ist Rückgabe true, sonst
		// false
		return (tf == null || tf == 0) ? false : true;
	}
}