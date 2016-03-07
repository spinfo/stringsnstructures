package common;

public class StringUtil {

	/**
	 * Trims whitespace from the beginning of a string.
	 * 
	 * @param str
	 *            The string to strip
	 * @return A new String without any whitespace at the start or null if the
	 *         input was null.
	 */
	public static String ltrim(String str) {
		if (str == null)
			return null;

		int i = 0;
		while (i < str.length() && Character.isWhitespace(str.charAt(i))) {
			i++;
		}
		return str.substring(i);
	}

	/**
	 * Trims whitespace from the end of a string.
	 * 
	 * @param str
	 *            The string to strip
	 * @return A new String without any whitespace at the end or null if the
	 *         input was null.
	 */
	public static String rtrim(String str) {
		if (str == null)
			return null;

		int i = str.length() - 1;
		while (i >= 0 && Character.isWhitespace(str.charAt(i))) {
			i--;
		}
		return str.substring(0, i + 1);
	}

	/**
	 * Check whether a string has any non-whitespace content
	 * 
	 * @param str
	 *            The string to test
	 * @return true if str is null, empty or whitesapce-only, else false
	 */
	public static boolean isBlank(String str) {
		if (str == null)
			return true;
		if (str.isEmpty())
			return true;
		return str.trim().isEmpty();
	}

}
