package modules.lfgroups;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents a string and works similar to one in Sets and Maps, but
 * additionally allows for splits to be introduced that represent sections of
 * that string taken to be functional units.
 */
class Functional implements Comparable<Functional>, CharSequence {

	// TODO: Refactor class to work with an int array of split positions instead
	// of this String list
	final List<String> parts = new ArrayList<String>(1);

	public Functional(String complete) {
		this.parts.add(complete);
	}

	public Functional(List<String> parts) {
		this.parts.addAll(parts);
	}

	public String get() {
		return String.join("", parts);
	}

	/**
	 * Introduces a new internal split in this functional.
	 * 
	 * @param index
	 *            The index in the 0-indexed string representation of the
	 *            functional after which the split should be introduced.
	 * @return A boolean indicating whether the split could be introduced or
	 *         not.
	 */
	// TODO: Change this from splitting after to splitting before idx?
	public boolean splitAt(int index) {
		boolean result = false;
		int charCount = 0;
		ListIterator<String> iterator = parts.listIterator();

		while (iterator.hasNext()) {
			String part = iterator.next();

			charCount += part.length();
			if (charCount > index) {
				int splitPos = part.length() - (charCount - index) + 1;
				if (splitPos > 0 && splitPos < part.length()) {
					String one = part.substring(0, splitPos);
					String two = part.substring(splitPos);
					iterator.set(one);
					iterator.add(two);
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * If the Functional other is a substring of this functional all splits from
	 * the other functional will be copied over. If the begin of the other
	 * functional and this functional do not align, another split is introduced
	 * indicating that begin.
	 * 
	 * Example: If this functional is "Func-tional" and the other is "on-al",
	 * after this operation this Functional will be represented as
	 * "Func-ti-on-al".
	 * 
	 * @param other
	 *            The functional the splits of which are to be introduced into
	 *            this functional.
	 */
	// TODO: This needs to be tested thoroughly
	public void splitOn(Functional other) {

		int offset = this.get().indexOf(other.get());
		if (offset > -1) {
			int count = 0;
			for (String part : other.parts) {
				this.splitAt(offset + count - 1);
				count += part.length();
			}
			this.splitAt(offset + count - 1);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Functional other = (Functional) obj;
		return get().equals(other.get());
	}

	@Override
	public int compareTo(Functional other) {
		return get().compareTo(other.get());
	}

	public String represent() {
		return String.join("-", parts);
	}

	@Override
	public int length() {
		return get().length();
	}

	@Override
	public char charAt(int index) {
		return get().charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return get().subSequence(start, end);
	}

	@Override
	public String toString() {
		return get();
	}

}
