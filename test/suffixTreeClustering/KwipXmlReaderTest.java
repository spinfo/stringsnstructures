package suffixTreeClustering;

import java.util.List;

import org.junit.Test;

import suffixTreeClustering.data.Token;
import suffixTreeClustering.data.Type;
import util.KwipXmlReader;
import util.TextInfo;

public class KwipXmlReaderTest {

	@Test
	public void test() {
		List<Type> types = new KwipXmlReader(TextInfo.getKwipXMLPath()).read();
		for (Type type : types) {
			String string = type.getString();
			System.out.println(string);
			System.out.println(type);
			List<Token> tokens = type.getTokens();
			for (Token token : tokens) {
				System.out.println(token);
			}
		}
	}
}