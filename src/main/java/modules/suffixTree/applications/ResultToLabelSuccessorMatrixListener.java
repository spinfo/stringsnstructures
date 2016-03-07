package modules.suffixTree.applications;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import common.StringUtil;
import models.NamedFieldMatrix;
import modules.suffixTree.SuffixTree;
import modules.suffixTree.node.Node;

public class ResultToLabelSuccessorMatrixListener implements ITreeWalkerListener {

	private final SuffixTree suffixTree;

	private final NamedFieldMatrix matrix;

	// A long label is one with multiple blanks in the middle
	private static final Pattern LONG_LABEL = Pattern.compile("^.*?[^ ]+[ ]+[^ ]+[ ]+[^ ]+.*$");

	public ResultToLabelSuccessorMatrixListener(SuffixTree suffixTree) {
		this.suffixTree = suffixTree;
		this.matrix = new NamedFieldMatrix();

	}

	// sets up variables for the recursive call to searchSuccessors, returns a
	// List of interned Strings which are the successors of the node
	private List<String> doSuccessorSearch(int nodeNr) {
		final Node node = this.suffixTree.nodes[nodeNr];
		final List<String> successorList = new ArrayList<String>();

		for (int childNodeNr : node.children.values()) {
			searchSuccessors(childNodeNr, successorList, "");
		}

		return successorList;
	}

	// Recursively walk the children of the node until a blank in the edge
	// string is reached. At that point add it to the successorsList and return
	private void searchSuccessors(int nodeNr, List<String> successorList, String readSoFar) {
		 final Node node = this.suffixTree.nodes[nodeNr];

		final String next = this.suffixTree.edgeString(nodeNr);

		// special case: if the end of a path has no edge string, and readSoFar
		// is not empty, readSoFar is a valid successor because a path through
		// the tree ends after it
		if (next.isEmpty() && !readSoFar.isEmpty() && (node.getStartPositionInformation() != null)) {
			successorList.add(readSoFar);
		}

		final int endIndex = next.indexOf(' ');
		switch (endIndex) {
		case -1: // next has no blanks, add it and process children
			readSoFar += next;
			break;
		case 0: // next starts with a blank, the successor is complete
			if (!readSoFar.isEmpty()) {
				successorList.add(readSoFar);
			}
			return;
		default: // next has a blank, add text till that to complete successor
			readSoFar += next.substring(0, endIndex);
			successorList.add(readSoFar);
			return;
		}

		// If we get this far, no blanks were encountered. So repeat.
		for (int childNodeNr : node.children.values()) {
			searchSuccessors(childNodeNr, successorList, readSoFar);
		}
	}

	@Override
	public void entryaction(int nodeNr, int level) throws Exception {
		final String label = this.suffixTree.edgeString(nodeNr);

		// Long and blank labels are ignored
		if (StringUtil.isBlank(label) || LONG_LABEL.matcher(label).matches()) {
			return;
		}
		final List<String> successors = doSuccessorSearch(nodeNr);
		for (String successor : successors) {
			matrix.addValue(label, successor, 1.0);
		}
	}

	@Override
	public void exitaction(int nodeNr, int level) throws Exception {
	}

	public NamedFieldMatrix getMatrix() {
		return matrix;
	}

}
