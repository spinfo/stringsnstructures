package normalize;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;


public class NormalizeMain {

	/**
	 * @param args
	 */

	static String INPUT_FILE_NAME = "";
	static String TXTEXTENSION = ".txt";
	Path workspacePath;
	
	

	private static String pathName() {

		Path p = Paths.get("../");
		
		try {
			return p.toRealPath(LinkOption.NOFOLLOW_LINKS).toString()
					+ "\\";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	

	private static TextInfo getTextInfo(String PATH) {
		// reads name of input file from file TextInfo
	
		TextInfo textInfo = new TextInfo();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(PATH
					+ "TextInfo" + TXTEXTENSION));
			
			
			textInfo.filename = reader.readLine();		
			//???dirty first char on laptop version
			System.out.println((int)textInfo.filename.charAt(0));
			if (!Character.isLetter(textInfo.filename.charAt(0)))
				textInfo.filename=textInfo.filename.substring(1);
			System.out.println("textInfo.filename=" + textInfo.filename);
			// System.out.println(textInfo.filename);
			textInfo.min = Integer.parseInt(reader.readLine());
			textInfo.max = Integer.parseInt(reader.readLine());
			System.out.println("TextInfo " + textInfo.filename + " "
					+ textInfo.min + " " + textInfo.max);

			return textInfo;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// save name to file

		String PATH_NAME = pathName();
		TextInfo textInfo = getTextInfo("Data/"/*PATH_NAME*/);
		
		String NAME = textInfo.filename;
		
		/*StringBuilder sb = new StringBuilder();
		for (Character c : filename.toCharArray()) {
			if(Character.isLetter(c))
				sb.append(c);
		}
		
		String NAME = sb.toString();
		*/
		
		final String ENCODING = "UTF-8";// StandardCharsets.UTF_8;
		final String TXTEXTENSION = ".txt";
		final String OUTPUT_FILE_NAME = NAME + "Normalize" + TXTEXTENSION;

		String text = "";
		StringBuffer filterBuf = null;
		System.out.println("NormalizeMain " + PATH_NAME + NAME + TXTEXTENSION);
		try {
			
			InputStreamReader reader = new InputStreamReader(
					new FileInputStream(new File("Data/"/*PATH_NAME*/ + NAME
							+ TXTEXTENSION)));

			Normalize normalize = new Normalize();
			text = normalize.readText(reader);
			text = normalize.normalize(text);
			filterBuf = normalize.filter(text, textInfo.min, textInfo.max);

		} catch (Exception e) {
			e.printStackTrace();
			//int i=10/0;
		}
		;

		// save name of Input text
		try {
			PrintWriter name = new PrintWriter(new FileWriter(PATH_NAME
					+ "Name" + TXTEXTENSION));
			name.print(NAME);
			name.close();

			System.out.println(filterBuf);

		} catch (Exception e) {
			e.printStackTrace();
			int i=10/0;
		}
		;

		try {
			PrintWriter out = new PrintWriter(new FileWriter(PATH_NAME
					+ OUTPUT_FILE_NAME));
			out.print(filterBuf);
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
			int i=10/0;
		}
		
		System.out.println("NormalizeMain Ende");
	}

}
