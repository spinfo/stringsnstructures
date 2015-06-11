package preprocess;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class PreprocessTest {

	private static final String LINE_SEPARATOR = System.lineSeparator();

	@Test
	public void testFilter() {
		Preprocess proc = new Preprocess();

		String text = readText("data/genesis.txt");
		System.out.println(text);

		System.out.println("***************");

		int min = 3;
		int max = 5;

		String processed = proc.process(text);
		StringBuffer filtered = proc.filter(processed, min, max);
		System.out.println(filtered);

		Assert.assertTrue(filtered.length() < text.length());
	}

	@Test
	public void testProcess() {
		Preprocess proc = new Preprocess();

		String text = "Dies ist ein Testtext. Er enthält 19,3 Zahlen, "
				+ "1.000€ und viele Abkürzungen wie z.B. diese hier. "
				+ "1973 gab es diesen Text noch nicht, dafür aber 10'000 "
				+ "andere dieser Art. Außerdem (klar) kommen hier "
				+ "mehrere    Leerzeichen vor. Am 31.12.2022 werden "
				+ "wir sowieso alle sterben. Ist das klar?! Oder was? Nicht..?";
		System.out.println(text);

		System.out.println("***************");

		String processed = proc.process(text);

		System.out.println(processed);

	}

	private String readText(String path) {
		StringBuffer sb = new StringBuffer();

		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append(LINE_SEPARATOR);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new String(sb);
	}

}
