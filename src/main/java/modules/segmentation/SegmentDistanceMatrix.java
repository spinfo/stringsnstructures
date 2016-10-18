package modules.segmentation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SegmentDistanceMatrix {

	// all the segments that this matrix keeps distances of
	private List<String> segments;

	// An object to map a single segment to a row/col index, e.g. for "Buch" =>
	// 1, the index 1 will be the position of "Buch" in the segments array as
	// well as the position of the "Buch" row in the rows list and the index of
	// the bit representing the "Buch" column in any BitSet of a row
	private Map<String, Integer> segmentsToIndices;

	// rows of the matrix are saved in a bitset view to be able to quickly check
	// for rows with matching entries
	private List<BitSet> rows;

	// An object to map a segment combinattion to a list of distances, e.g.
	// "Buch|kauft" => [-2,-3]
	private Map<String, List<Short>> fields;

	// the initial number of segments this matrix will map
	private int initialDimenstions;
	
	// a single char used to construct the keys for the field map
	private static final char KEY_CONNECTOR = '|';

	public SegmentDistanceMatrix() {
		this(1000);
	}
	
	public SegmentDistanceMatrix(int initialDimensions) {
		this.initialDimenstions = initialDimensions;

		this.segments = new ArrayList<>(initialDimensions);
		this.rows = new ArrayList<>(initialDimensions);
		this.segmentsToIndices = new HashMap<>();
		this.fields = new HashMap<>();
	}

	public void addSegments(String[] segments) {
		// keep an array of indices to reduce lookups
		int[] indices = new int[segments.length];

		String segmentOne;
		String segmentTwo;

		for (short i = 0; i < segments.length; i++) {
			segmentOne = segments[i];
			indices[i] = addSegment(segmentOne);

			for (short j = 0; j < i; j++) {
				segmentTwo = segments[j];
				markCombinationPresent(indices[i], indices[j]);
				// casts necessary due to javas implicit conversion in
				// operations
				addDistance(segmentTwo, segmentOne, (short) (i - j));
				addDistance(segmentOne, segmentTwo, (short) ((i - j) * -1));
			}
		}
	}
	
	public List<Short> getDistances(String one, String two) {
		List<Short> distances = fields.get(makeDistancesKey(one, two));
		return Collections.unmodifiableList(distances);
	}
	
	// add a segment and return it's position (row/col) in the indices. If the
	// segment is already present this is a simple position lookup.
	private int addSegment(String segment) {
		Integer index = this.segmentsToIndices.get(segment);

		// Test if the segment is already present
		if (index == null) {
			segments.add(segment);
			rows.add(new BitSet(initialDimenstions));
			
			index = segments.size() - 1;
		
			segmentsToIndices.put(segment, index);

			consistencyCheck();
		}
		return index;
	}

	// mark a combination as present by setting the appropriate bit in the
	// appropriate rows
	private void markCombinationPresent(int indexOne, int indexTwo) {
		BitSet row = rows.get(indexOne);
		row.set(indexTwo);

		row = rows.get(indexTwo);
		row.set(indexOne);
	}

	// add a distance to the distances recorded for these strings
	private void addDistance(String one, String two, short distance) {
		final String key = makeDistancesKey(one, two);

		final List<Short> distances = fields.getOrDefault(key.toString(), new ArrayList<Short>());
		distances.add(distance);
		fields.put(key.toString(), distances);
	}

	private String makeDistancesKey(String one, String two) {
		StringBuilder key = new StringBuilder();
		key.append(one);
		key.append(KEY_CONNECTOR);
		key.append(two);
		return key.toString();
	}

	private void consistencyCheck() {
		// Check if all objects are consistent. This is meant for testing and
		// may later be removed.
		if ((segments.size() != rows.size()) || (segments.size() != segmentsToIndices.keySet().size())) {
			throw new IllegalStateException("Inconsistent state. Segments: " + segments.size() + ", rows: " + rows.size() + ", segKeys: " + segmentsToIndices.keySet().size());
		}
	}

	public String print(String delim) {
		List<String> sorted = new ArrayList<String>(segments);
		sorted.sort(String::compareToIgnoreCase);

		StringBuilder sb = new StringBuilder();
		
		// print header
		sb.append(delim);
		for (int i = 0; i < sorted.size(); i++) {
			sb.append(sorted.get(i));
			sb.append(delim);
		}
		sb.append("\n");

		// print all rows
		BitSet row;
		String key;
		List<Short> distances;
		int idx1, idx2;
		for (int i = 0; i < sorted.size(); i++) {
			idx1 = segmentsToIndices.get(sorted.get(i));

			sb.append(segments.get(idx1));
			sb.append(delim);

			row = rows.get(idx1);
			for (int j = 0; j < sorted.size(); j++) {
				idx2 = segmentsToIndices.get(sorted.get(j));

				if (row.get(idx2)) {
					key = makeDistancesKey(segments.get(idx1), segments.get(idx2));
					distances = fields.get(key);
					for (short distance : distances) {
						sb.append(distance);
						sb.append(',');
					}
					sb.setLength(sb.length() - 1);
				}
				sb.append(delim);
			}
			sb.append("\n");
		}

		return sb.toString();
	}

}
