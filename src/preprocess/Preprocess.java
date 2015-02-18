package preprocess;

import java.io.BufferedReader;

public class Preprocess {

	String eol = System.getProperty("line.separator");

	public String readText(BufferedReader reader) {
		System.out.println("Preprocess.readText entry");
		// must begin with line break for identification of line number in
		// normalize method
		StringBuilder textBuffer = new StringBuilder("\n");
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				textBuffer.append(line);
			}
		} catch (Exception e) {
			System.out.println("Exception Preprocess.readText");
		}

		// System.out.println("\n\nResult readText\n"
		// + /* textBuffer.toString() */result);
		return textBuffer.toString();
	}

	String process(String text) {

		try {
			System.out.println("\n\nPreprocess.normalize entry");
			// replace initial line number
			text = text.replaceAll("\\n[0-9]+", "");
			// colon, quotation mark by blank
			text = text.replaceAll("[,\"\\«\\»]", " ");

			// System.out.println("Test1"+"\n"+text);

			// replace all white chars (blank, newline, tab)
			text = text.replaceAll("[\\s]+", " ");

			// System.out.println("Test2 "+"\n"+text);
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
			// 10.000
			text = text.replaceAll("([0-9])([.])([0-9])", "$1$3");

			// System.out.println("Test2 "+"\n"+text);

			// (blank) full stop (.,!,? ...) (blank) by $ eol
			text = text.replaceAll("[ ]*[.;!?;:][\\s]*", "\\$" + eol);
			// undo & for ., s.above for date
			text = text.replaceAll("[&]", "\\.");
			text = text.replaceAll("[|]", "\\:");
			text = text.replaceAll("[#]", "\\,");

			// blanks at beginning of text (may occur after deletion of
			// " for ex.
			// while (text.charAt(0)==' ') text=text.substring(1);
			System.out.println("Test3" + "\n" + text);

			System.out.println("Exit normalize");
		} catch (Exception e) {
			System.out.println("Exception normalize");
			int i = 10 / 0;
		}

		;

		return text;
	}

	StringBuffer filter(String text, int min, int max) {
		int len;
		StringBuffer buf;
		if ((min == 0) && (max == 0)) {
			buf = new StringBuffer(text);
		} else {
			buf = new StringBuffer();

			String phrases[] = text.split(eol);
			for (int i = 0; i < phrases.length; i++) {
				String words[] = phrases[i].split("[ ]");
				len = words.length;
				System.out.println(phrases[i] + " len: " + len + " min: " + min
						+ " max: " + max);
				if ((len >= min) && (len <= max)) {
					System.out.println("Phrase within filter: " + phrases[i]
							+ "  ");
					buf.append(phrases[i] + eol);
				}
			}
		}
		System.out.println();
		return buf;
	}

}
