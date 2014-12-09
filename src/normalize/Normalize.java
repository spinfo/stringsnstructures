package normalize;

//import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Normalize {

	String eol = System.getProperty("line.separator");

	public String readText(InputStreamReader reader) {
		System.out.println("Normalize.readText entry");
		StringBuffer textBuffer = new StringBuffer();
		int ch;
		try {
			//ch = reader.read();// ??? hier wurde am anfang fragezeichen
								// gelesen???

			while ((ch = reader.read()) != -1) {
				System.out.print((char) ch);
				textBuffer.append((char) ch);
			}

		} catch (Exception e) {
			System.out.println("Exception Normalize.readText");
		}
		

		System.out.println("\n\nResult readText\n" + textBuffer.toString());
		return textBuffer.toString();
	}

	String normalize(String text) {

		try {
			System.out.println("\n\nNormalize.normalize entry");
			// replace initial line number
			text = text.replaceAll("^[0-9]+", "");
			// replace all white chars (blank, newline, tab)
			text = text.replaceAll("\\s", " ");
			// colon, quotation mark by blank
			text = text.replaceAll("[,\"\\«\\»]", " ");
			// parentheses
			text = text.replaceAll("[\\(][^\\)]*[\\)]", " ");
			// multiple blank by (one) blank
			text = text.replaceAll("[ ]+", " ");
			// date:10.-29., 30.-31,1.-9.month
			// blank vorher!!
			String daymonth=
			"([1-2][0-9]|[3][0-1]|[1-9])([\\.])([1-9]|[1][0-2])([\\.])";
			text = text.replaceAll(daymonth,"$1&$3&");
			String day="([1-2][0-9]|[3][0-1]|[1-9])([\\.])";
			text = text.replaceAll(day,"$1&");
			// abbreviation bzw., ca. Dr. usw. 
			String abbrev=
			"(a|B|bzw|ca|Chr|Dr|Hrg|Hrsg|I|i|Mio|Mrd|O|phil|Prof|s|S|St|u|usf|usw|v|V|z)([\\.])";
			text = text.replaceAll(abbrev,"$1&");
			// (blank) full stop (.,!,? ...) (blank) (line number) by $
			text = text.replaceAll("[ ]*[.;!?;:][ ]*\\d*[ ]*", "\\$" + eol);
			// undo & for ., s.above for date
			text = text.replaceAll("[&]","\\.");
			System.out.print(text);
		} catch (Exception e) {
			System.out.println("Exception normalize");
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
				System.out.println(phrases[i] + " len: " + len+" min: "+min+ " max: "+max);
				if ((len >= min) && (len <= max)) {
					System.out.println("Phrase within filter: "+phrases[i] + "  ");
					buf.append(phrases[i] + eol);
				}
			}
		}
		System.out.println();
		return buf;
	}

}
