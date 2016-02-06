package modules.suffixTree.applications;

import modules.suffixTree.SuffixTree;
import modules.suffixTree.node.GeneralisedSuffixTreeNode;

import java.util.Map;
import java.util.TreeMap;

import models.GstLabelData;

public class ResutlToGstLabelDataListener extends AbstractNodeStackListener implements ITreeWalkerListener {

	private final Map<String, GstLabelData> labelsToData;
	
	public ResutlToGstLabelDataListener(SuffixTree suffixTree) {
		super(suffixTree);
		labelsToData = new TreeMap<String, GstLabelData>();
	}
	
	@Override
	public void entryaction(int nodeNr, int level) throws Exception {
		super.entryaction(nodeNr, level);
	}
	
	@Override
	public void exitaction(int nodeNr, int level) throws Exception {
		// get the label string and the data associated with it
		final String label = super.getCurrentNodeLabel();
		final GstLabelData data = labelsToData.getOrDefault(label, new GstLabelData());
		final GeneralisedSuffixTreeNode node = super.getCurrentNode();
		
		// add the data
		data.setLabel(label);
		data.getLevels().add(level);
		data.getSiblingCounts().add(super.getCurrentNodeSiblingCount());
		data.getChildCounts().add(node.children.size());
		data.getOccurenceCounts().add(node.getStartPositionInformation().size());
		data.getLeafCounts().add(node.sumOfLeaves);
		
		// push data back to the map
		this.labelsToData.put(label, data);

		// make sure that the node stack is handled correctly
		super.exitaction(nodeNr, level);
	}
	
	/**
	 * @return the labels encountered mapped to GstLabelData objects.
	 */
	public Map<String, GstLabelData> getLabelsToGstData() {
		return labelsToData;
	}
}
