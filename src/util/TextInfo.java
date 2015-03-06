package util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class TextInfo {

	private static final String FILENAME = "config/textinfo.properties";
	private static String FOLDER_NAME;

	private static final String FILE_SEPARATOR = System
			.getProperty("file.separator");
	private static final String PATH_SEPARATOR = "/";

	private static String workspacePath;

	private static final String TXT_EXTENSION = ".txt";
	private static final String XML_EXTENSION = ".xml";

	private static String textName;
	private static String preprocessName;
	private static String kwipName;
	private static String suffixTreeName;

	private static String textPath;
	private static String preprocessPath;
	private static String kwipPath;
	private static String suffixTreePath;

	private static int minLength;
	private static int maxLength;

	static {
		determineWorkspacePath();

		Properties properties = new Properties();
		BufferedInputStream stream;
		try {
			stream = new BufferedInputStream(new FileInputStream(FILENAME));
			properties.load(stream);
			stream.close();

			FOLDER_NAME = properties.getProperty("folder");

			setTextNameAndPath(properties.getProperty("name"));
			setMinLengthAndPath(properties.getProperty("minLength"));
			setMaxLengthAndPath(properties.getProperty("maxLength"));

			setPreprocessName();
			setKwipName();
			setSuffixTreeName();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void determineWorkspacePath() {
		Path p = Paths.get("../");
		try {
			workspacePath = p.toRealPath(LinkOption.NOFOLLOW_LINKS).toString()
					+ FILE_SEPARATOR;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String createFilePath(String name, String extension) {
		return workspacePath + name + extension;
	}

	private static void setTextNameAndPath(String name) {
		TextInfo.textName = name;
		TextInfo.textPath = FOLDER_NAME + PATH_SEPARATOR + textName
				+ TXT_EXTENSION;
	}

	private static void setPreprocessName() {
		TextInfo.preprocessName = textName + "Preprocess";
		TextInfo.preprocessPath = createFilePath(preprocessName, TXT_EXTENSION);
	}

	private static void setKwipName() {
		TextInfo.kwipName = textName + "Kwip";
		TextInfo.kwipPath = createFilePath(kwipName, TXT_EXTENSION);
	}

	private static void setSuffixTreeName() {
		TextInfo.suffixTreeName = textName + "SuffixTree";
		TextInfo.suffixTreePath = createFilePath(suffixTreeName, XML_EXTENSION);
	}

	private static void setMaxLengthAndPath(String length) {
		TextInfo.maxLength = Integer.parseInt(length);
	}

	private static void setMinLengthAndPath(String length) {
		TextInfo.minLength = Integer.parseInt(length);
	}

	public static String getSuffixTreeName() {
		return suffixTreeName;
	}

	public static String getKwipName() {
		return kwipName;
	}

	public static int getMaxLength() {
		return maxLength;
	}

	public static int getMinLength() {
		return minLength;
	}

	public static String getTextName() {
		return textName;
	}

	public static String getWorkspacePath() {
		return workspacePath;
	}

	public static String getPreprocessName() {
		return preprocessName;
	}

	public static String getTextPath() {
		return textPath;
	}

	public static String getPreprocessPath() {
		return preprocessPath;
	}

	public static String getKwipPath() {
		return kwipPath;
	}

	public static String getSuffixTreePath() {
		return suffixTreePath;
	}
}