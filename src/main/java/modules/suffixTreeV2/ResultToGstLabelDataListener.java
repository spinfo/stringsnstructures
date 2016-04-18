package modules.suffixTreeV2;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import models.GstLabelData;

public class ResultToGstLabelDataListener extends AbstractResultNodeStackListener {

	// the tree that will be iterated on with this listener
	private final BaseSuffixTree tree;

	// a map from labels to the accompanying data
	private final Map<String, GstLabelData> labelsToData;

	public ResultToGstLabelDataListener(BaseSuffixTree tree) {
		// call parent constructor to setup and handle the node stack
		super(tree);

		this.tree = tree;
		this.labelsToData = new TreeMap<String, GstLabelData>();
	}

	@Override
	public void process(int nodeNr, List<Node> path, int pathLength, int level) {
		// ignore the root node as it does not have any label
		if (nodeNr == tree.getRoot()) {
			return;
		}

		// the node and label in question
		final Node node = tree.getNode(nodeNr);
		final String label = tree.edgeString(node);

		// if data was added for the label we want to simply add to it
		final GstLabelData data = labelsToData.getOrDefault(label, new GstLabelData());

		// simply set the label and add the current level as well as the child
		// count and the leaf count
		data.setLabel(label);
		data.getLevels().add(level);
		data.getChildCounts().add(node.getEdgeBegins().size());
		data.getLeafCounts().add(node.getLeaves().size());

		// The parent node is the last node on the path to this node. use it to
		// get the count of siblings for this node (other edges from the
		// parent).
		int siblingsCount = path.get(path.size() - 1).getEdgeBegins().size() - 1;
		if (siblingsCount < 0) {
			throw new IllegalStateException("Negative numer of siblings reported for an existing node.");
		}
		data.getSiblingCounts().add(siblingsCount);

		// The amount of occurrences is equal to the amount of positions for
		// all leaves below the current node
		int occurrenceCount = 0;
		for (Node leaf : node.getLeaves()) {
			occurrenceCount += leaf.getPositions().size();
		}
		data.getOccurenceCounts().add(occurrenceCount);

		// push the data back to the collection
		labelsToData.put(label, data);
	}

	/**
	 * @return the labels encountered mapped to GstLabelData objects.
	 */
	public Map<String, GstLabelData> getLabelsToGstData() {
		return labelsToData;
	}
}
