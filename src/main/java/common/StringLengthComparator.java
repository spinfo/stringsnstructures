package common;

import java.util.Comparator;

/**
 * Compares two strings by string length.
 * @author marcel
 *
 */
public class StringLengthComparator implements Comparator<String> {

	private boolean ascendingOrder;
	
	public StringLengthComparator() {
		this(true);
	}
	
	/**
	 * Instantiate comparator
	 * @param ascendingOrder Set to true if the comparison should favor shorter strings, false otherwise.
	 */
	public StringLengthComparator(boolean ascendingOrder) {
		super();
		this.ascendingOrder = ascendingOrder;
	}

	@Override
	public int compare(String o1, String o2) {
		if (ascendingOrder)
			return o1.length()-o2.length();
		else
			return o2.length()-o1.length();
	}

	public boolean isAscendingOrder() {
		return ascendingOrder;
	}

	public void setAscendingOrder(boolean ascendingOrder) {
		this.ascendingOrder = ascendingOrder;
	}

}
