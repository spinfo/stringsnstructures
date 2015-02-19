package preprocess;

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
import java.util.logging.Logger;

import util.LoggerConfigurator;

public class PreprocessMain {

	private static final Logger LOGGER = Logger.getGlobal();

	private static String TXTEXTENSION = ".txt";
	private static String FOLDER_NAME = "data/"; // TODO: use properties for
													// name of data folder and
													// name of text file to
													// process
	private static String fileSeparator = System.getProperty("file.separator");

	private static String pathName() {
		Path p = Paths.get("../");

		try {
			return p.toRealPath(LinkOption.NOFOLLOW_LINKS).toString()
					+ fileSeparator;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * reads name of input file from file TextInfo
	 */
	private static TextInfo getTextInfo(String PATH) {
		TextInfo textInfo = new TextInfo();

		try (BufferedReader reader = new BufferedReader(new FileReader(PATH
				+ "TextInfo" + TXTEXTENSION))) {
			textInfo.filename = reader.readLine();

			LOGGER.fine("First char: " + (int) textInfo.filename.charAt(0));
			if (!Character.isLetter(textInfo.filename.charAt(0)))
				textInfo.filename = textInfo.filename.substring(1);
			LOGGER.fine("textInfo.filename=" + textInfo.filename);

			textInfo.min = Integer.parseInt(reader.readLine());
			textInfo.max = Integer.parseInt(reader.readLine());
			LOGGER.fine("TextInfo " + textInfo.filename + " " + textInfo.min
					+ " " + textInfo.max);

			return textInfo;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		LoggerConfigurator.configGlobal();

		String PATH_NAME = pathName();
		TextInfo textInfo = getTextInfo(FOLDER_NAME);
		String NAME = textInfo.filename;

		final String ENCODING = "UTF-8";// StandardCharsets.UTF_8;
		final String TXTEXTENSION = ".txt";
		final String OUTPUT_FILE_NAME = NAME + "Preprocess" + TXTEXTENSION;

		String text = "";
		StringBuffer filterBuf = null;
		LOGGER.info("PreprocessMain " + PATH_NAME + FOLDER_NAME + NAME
				+ TXTEXTENSION);
		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(FOLDER_NAME + NAME
							+ TXTEXTENSION)), ENCODING));

			Preprocess preprocess = new Preprocess();
			text = preprocess.readText(reader);
			text = preprocess.process(text);
			filterBuf = preprocess.filter(text, textInfo.min, textInfo.max);

		} catch (Exception e) {
			e.printStackTrace();
			// int i=10/0;
		}

		// save name of Input text to file "Name.txt" in parent folder of project
		try {
			PrintWriter name = new PrintWriter(new FileWriter(PATH_NAME
					+ "Name" + TXTEXTENSION));
			name.print(NAME);
			name.close();

			System.out.println(filterBuf);

		} catch (Exception e) {
			e.printStackTrace();
			int i = 10 / 0;
		}

		// save result of preprocessing to a file in parent folder of project
		try {
			PrintWriter out = new PrintWriter(new FileWriter(PATH_NAME
					+ OUTPUT_FILE_NAME));
			out.print(filterBuf);
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
			int i = 10 / 0;
		}

		LOGGER.exiting("PreprocessMain", "main");
	}
}