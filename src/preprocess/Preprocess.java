package preprocess;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Preprocess {

	private static final Logger LOGGER = Logger.getGlobal();

	private static final String EOL = System.lineSeparator();

	public String readText(BufferedReader reader) {
		LOGGER.entering(this.getClass().getName(), "readText");
		// must begin with line break for identification of line number in
		// normalize method
		StringBuilder textBuffer = new StringBuilder("\n");
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				textBuffer.append(line + "\n");
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception Preprocess.readText", e);
		}

		// For large Strings, StringBuffer.toString() is bad, use new
		// String(StringBuffer) instead
		String result = new String(textBuffer);
		LOGGER.finest("\n\nResult readText\n" + result);
		return result;
	}

	String process(String text) {
		LOGGER.info("Length of text: " + text.length());

		LOGGER.entering(this.getClass().getName(), "process");

		Map<String, String> replacements = getReplacements();

		try {
			// replace initial line number
			text = text.replaceAll("\\n[0-9]+", "");
			// colon, quotation mark by blank
			text = text.replaceAll("[,\"\\«\\»]", " ");

			// replace all white chars (blank, newline, tab)
			text = text.replaceAll("[\\s]+", " ");

			// 19,3
			text = text.replaceAll("([0-9])([,])([0-9])", "$1#$3");
			// colon, quotation mark by blank
			// text = text.replaceAll("[,\"\\«\\»]", " ");
			// parentheses
			text = text.replaceAll("[\\(][^\\)]*[\\)]", " ");
			// multiple blank by (one) blank
			text = text.replaceAll("[ ]+", " ");
			// date:10.-29., 30.-31,1.-9.month
			// blank vorher!!

			String daymonth = "([1-2][0-9]|[3][0-1]|[1-9])([\\.])([1-9]|[1][0-2])([\\.])";
			text = text.replaceAll(daymonth, "$1&$3&");

			String day = "([1-2][0-9]|[3][0-1]|[1-9])([\\.])";
			text = text.replaceAll(day, "$1&");

			// abbreviation bzw., ca. Dr. usw.
			String abbrev = "(a|al|B|bzw|ca|Chr|Dr|Fr|Hrg|Hrsg|I|i|Mill|Mio|Mr|Mrd|Nr|O|phil|Prof|s|S|St|u|usf|usw|v|V|z)([\\.])";
			text = text.replaceAll(abbrev, "$1&");

			// 100'000, 100 000
			text = text.replaceAll("([0-9]+)([\\'])([0-9]+)", "$1$3");

			text = text.replaceAll("([0-9]+)([ ])([0-9]+)", "$1$3");

			// z.B. 2:0
			text = text.replaceAll("([0-9])([:])([0-9])", "$1|$3");
			// replace e.g. 10.000 with 10000
			text = text.replaceAll("([0-9])([.])([0-9])", "$1$3");

			// (blank) full stop (.,!,? ...) (blank) by $ eol
			text = text.replaceAll("[ ]*[.;!?;:][\\s]*", "\\$" + EOL);
			// undo & for ., s.above for date
			text = text.replaceAll("[&]", "\\.");
			text = text.replaceAll("[|]", "\\:");
			text = text.replaceAll("[#]", "\\,");

			// blanks at beginning of text (may occur after deletion of
			// " for ex.
			// while (text.charAt(0)==' ') text=text.substring(1);

			LOGGER.exiting(this.getClass().getName(), "normalize");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception normalize", e);
			int i = 10 / 0;
		}

		return text;
	}

	private Map<String, String> getReplacements() {
		Map<String, String> toReturn = new HashMap<>();

		// replace initial line number
		toReturn.put("\\n[0-9]+", "");
		// replace colon, quotation mark by blank
		toReturn.put("[,\"\\«\\»]", " ");
		// normalize all white chars (blank, newline, tab) to single ws
		toReturn.put("[\\s]+", " ");

		toReturn.put("([0-9])([,])([0-9])", "$1#$3");
		// remove parentheses
		toReturn.put("[\\(][^\\)]*[\\)]", " ");
		// reduce multiple blanks to one blank
		toReturn.put("[ ]+", " ");
		// dates
		toReturn.put(
				"([1-2][0-9]|[3][0-1]|[1-9])([\\.])([1-9]|[1][0-2])([\\.])",
				"$1&$3&");
		toReturn.put("([1-2][0-9]|[3][0-1]|[1-9])([\\.])", "$1&");

		// abbreviations
		toReturn.put(
				"(a|al|B|bzw|ca|Chr|Dr|Fr|Hrg|Hrsg|I|i|Mill|Mio|Mr|Mrd|Nr|O|phil|Prof|s|S|St|u|usf|usw|v|V|z)([\\.])",
				"$1&");
		// 100'000
		toReturn.put("([0-9]+)([\\'])([0-9]+)", "$1$3");
		// 100 000
		toReturn.put("([0-9]+)([ ])([0-9]+)", "$1$3");
		// z.B. 2:0
		toReturn.put("([0-9])([:])([0-9])", "$1|$3");
		toReturn.put("([0-9])([.])([0-9])", "$1$3");
		// replace (blank) full stop (.,!,? ...) (blank) by $ eol
		toReturn.put("[ ]*[.;!?;:][\\s]*", "\\$" + EOL);

		// undo & for ., s.above for date
		toReturn.put("[&]", "\\.");
		toReturn.put("[|]", "\\:");
		toReturn.put("[#]", "\\,");
		return toReturn;
	}

	StringBuffer filter(String text, int min, int max) {
		int len;
		StringBuffer buf;
		if ((min == 0) && (max == 0)) {
			buf = new StringBuffer(text);
		} else {
			buf = new StringBuffer();

			String phrases[] = text.split(EOL);
			for (int i = 0; i < phrases.length; i++) {
				String words[] = phrases[i].split("[ ]");
				len = words.length;
				LOGGER.finest(phrases[i] + " len: " + len + " min: " + min
						+ " max: " + max);
				if ((len >= min) && (len <= max)) {
					LOGGER.finer("Phrase within filter: " + phrases[i] + "  ");
					buf.append(phrases[i] + EOL);
				}
			}
		}
		System.out.println();
		return buf;
	}
}