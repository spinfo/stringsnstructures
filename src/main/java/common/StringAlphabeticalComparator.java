package common;

import java.util.Comparator;

/**
 * Compares two strings by alphabetical order.
 * @author marcel
 *
 */
public class StringAlphabeticalComparator implements Comparator<String> {

	private boolean ascendingOrder;
	
	public StringAlphabeticalComparator() {
		this(true);
	}
	
	/**
	 * Instantiate comparator
	 * @param ascendingOrder True yields A-Z ordering, false Z-A
	 */
	public StringAlphabeticalComparator(boolean ascendingOrder) {
		super();
		this.ascendingOrder = ascendingOrder;
	}

	@Override
	public int compare(String o1, String o2) {
		if (ascendingOrder)
			return o1.compareTo(o2);
		else
			return o2.compareTo(o1);
	}

	public boolean isAscendingOrder() {
		return ascendingOrder;
	}

	public void setAscendingOrder(boolean ascendingOrder) {
		this.ascendingOrder = ascendingOrder;
	}

}
