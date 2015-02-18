package suffixTreeClustering.features;

import java.util.ArrayList;
import java.util.List;

import suffixTreeClustering.data.Node;
import suffixTreeClustering.data.Type;
import suffixTreeClustering.st_interface.SuffixTreeInfo;

/**
 * Node Weight Calculation based on TF (Term Frequency, i.e. how often a
 * specific term occurs in a document - in case of STC: how often a Document
 * visited the specific Node) and IDF (inverse Document Frequency - how many
 * Documents contain the term / visited the specific node)
 * 
 * @author neumannm
 */
public final class TfDfFeatures {

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
	public TfDfFeatures(final Type document, final SuffixTreeInfo corpus) {
		if (corpus.getNodes().size() == 0) {
			throw new IllegalArgumentException("Empty Corpus!");
		}
		this.document = document;
		this.corpus = corpus;
	}

		
	/**
	 * Create vector with tf-df for this document.
	 * 
	 * @return FeatureVector
	 */
	public FeatureVector vector() {
		// Ein Vektor für dieses Dokument ist...
		List<Double> values = new ArrayList<Double>();
		// ...für jeden Term im Vokabular... (=jeder Knoten im SuffixTree)
		List<Node> terms = corpus.getNodes();

		boolean ok = false;
		for (Node node : terms) {
			// der tf-idf-Wert des Terms:
			Double tfdf = tfDf(node);
			if (tfdf > 0) {
				ok = true;
			}
			values.add(tfdf);
		}
		if (!ok) {
			// FIXME is this OK?
			String warning = "Warning: Created a TF-DF vector without any activation for terms size: "
					+ terms.size();
			System.out.println(warning);
			throw new IllegalStateException(warning);
		}
		return new FeatureVector(values.toArray(new Double[values.size()]));
	}

	/**
	 * Calculate tf-df for given node wrt this document.
	 * 
	 * @param node
	 *            - roughly corresponds to term
	 * @return tf-df value
	 */
	public Double tfDf(Node node) {
		/* TF und DF */
		// tf(n,d) = Anzahl, wie oft Dokument d Knoten n durchlaufen hat
		Integer tf = node.getTermfrequencyFor(document);
		tf = tf == null ? 0 : tf;

		// df(n) = Anzahl verschiedener Dokumente, die den Knoten durchlaufen
		// haben
		Integer df = node.getDF();
		df = df == null ? 0 : df;

		double wf = 1 + Math.log(tf);
		wf = Math.max(0, wf);

		/* TF-DF */
		double f = (double) (wf * df);

		return f;
	}
}