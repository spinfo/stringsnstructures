package suffixTreeClustering.features;

import java.util.ArrayList;
import java.util.List;

import suffixTreeClustering.data.Node;
import suffixTreeClustering.data.Type;
import suffixTreeClustering.st_interface.SuffixTreeInfo;

/**
 * Vector representation for documents indicating left/right contexts.
 * 
 * @author neumannm
 */
public final class ContextFeatures {

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
	public ContextFeatures(final Type document, final SuffixTreeInfo corpus) {
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
		List<Node> nodes = corpus.getNodes();

		for (Node node : nodes) {
			
			List<Integer> startPositionsOfType = node
					.getStartPositionsOfType(document);
			System.out.println("Start positions at node " + node + " of "
					+ document);
			if (null != startPositionsOfType) {
				for (Integer pos : startPositionsOfType) {
					System.out.println(pos);
				}
			} else
				System.out.println("none");
			System.out.println("-----------------");
			
			
			
			double value = 0.0; // TODO: anpassen
			values.add(value);
		}

		return new FeatureVector(values.toArray(new Double[values.size()]));
	}

}