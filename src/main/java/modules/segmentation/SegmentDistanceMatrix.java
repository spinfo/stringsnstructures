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

	/**
	 * Add an array of segments to the matrix. The segments' distances will be
	 * computed and added to the distances present in the matrix already.
	 * 
	 * @param segments
	 *            The segments in order of succession.
	 */
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

	/**
	 * Produce an unmodifiable copy of the distances that are mapped to the
	 * segment combination.
	 * 
	 * @param one
	 *            One segment of the combination
	 * @param two
	 *            The other segment of the combination
	 * @return An unmmodifiable copy of the distances noted for the combination.
	 */
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

	/**
	 * Check if a combination is present in the matrix.
	 * 
	 * @param one
	 *            One segment of the combination
	 * @param two
	 *            The other segment of the combination
	 * @return Whether the combination identified by both strings is present in
	 *         the matrix.
	 */
	public boolean hasCombination(String one, String two) {
		boolean result = false;

		Integer idx1 = segmentsToIndices.get(one);
		Integer idx2 = segmentsToIndices.get(two);

		if (idx1 != null && idx2 != null) {
			result = rows.get(idx1).get(idx2);
		}

		return result;
	}

	/**
	 * Get a list of segments currently in the matrix.
	 * 
	 * @return A List of String representing the segments noted in the matrix.
	 */
	public List<String> getSegments() {
		return Collections.unmodifiableList(segments);
	}

	/**
	 * The amount of segments noted for the matrix. (Equal to it's
	 * x/y-dimenstions)
	 * 
	 * @return The size of segments noted for this matrix.
	 */
	public int getSegmentsAmount() {
		return segments.size();
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
			throw new IllegalStateException("Inconsistent state. Segments: " + segments.size() + ", rows: "
					+ rows.size() + ", segKeys: " + segmentsToIndices.keySet().size());
		}
	}

	// actually compute the hamming distance between two rows using the amount
	// of bits set in an XOR of both rows
	private int getRowsHammingDistance(int row1, int row2) {
		if (row1 == row2)
			return 0;
		// one BitSet needs to be a clone because it's values will be changed by
		// the XOR-operation
		BitSet bitsRow1 = (BitSet) rows.get(row1).clone();
		BitSet bitsRow2 = rows.get(row2);

		// the Hamming distance is the number of bits set after the XOR
		bitsRow1.xor(bitsRow2);
		return bitsRow1.cardinality();
	}

	/**
	 * Compute the Hamming distance between two rows (only regarding the fields
	 * set in the rows not the actual fields' values)
	 * 
	 * @param row1
	 *            idx of one row to check
	 * @param row2
	 *            idx of another row to check
	 * @return The amount of fields set in one row but not the other
	 */
	public int getRowsHammingDistance(String row1, String row2) {
		Integer idx1 = segmentsToIndices.get(row1);
		Integer idx2 = segmentsToIndices.get(row2);

		if (!(idx1 == null || idx2 == null)) {
			return getRowsHammingDistance(idx1, idx2);
		} else {
			throw new IllegalArgumentException("One of these rows does not exist: " + row1 + ", " + row2);
		}
	}

	/**
	 * Print a matrix (csv) representation using the provided field delimiter.
	 * This includes column and row headings
	 * 
	 * @param delim
	 *            The delimiter to use to separate field
	 * @return A String that is a csv representation of this matrix
	 */
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
					distances = getDistances(segments.get(idx1), segments.get(idx2));
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
