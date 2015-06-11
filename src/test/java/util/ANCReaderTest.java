package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class ANCReaderTest {

	private String text;

	@Before
	public void setUp() {
		text = readText(TextInfo.getTextPath());
	}

	private String readText(String path) {
		StringBuilder textBuffer = new StringBuilder();
		String line;
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			while ((line = reader.readLine()) != null) {
				textBuffer.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// For large Strings, StringBuffer.toString() is bad, use new
		// String(StringBuffer) instead
		String result = new String(textBuffer);
		return result;
	}

	@Test
	public void test() {
		String ancPath = TextInfo.getAncPath();

		try {
			List<String> verbs = ANCXmlReader.parseXML(ancPath, text);
			Assert.assertNotNull(verbs);
			for (String string : verbs) {
				System.out.println(string);
			}

		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

	}

}
