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
	private static final String HTML_EXTENSION = ".html";

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
	private static String kwipTypeName;
	private static String kwipTypePath;
	private static String kwipUnitName;
	private static String kwipUnitPath;
	private static String prettyKwipName;
	private static String prettyKwipPath;

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

			setPreprocessNameAndPath();
			setKwipNamesAndPaths();
			setSuffixTreeNameAndPath();

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

	private static void setPreprocessNameAndPath() {
		TextInfo.preprocessName = textName + "Preprocess";
		TextInfo.preprocessPath = createFilePath(preprocessName, TXT_EXTENSION);
	}

	private static void setKwipNamesAndPaths() {
		TextInfo.kwipName = textName + "Kwip";
		TextInfo.kwipPath = createFilePath(kwipName, TXT_EXTENSION);

		TextInfo.kwipTypeName = textName + "KwipType";
		TextInfo.kwipTypePath = createFilePath(kwipTypeName, TXT_EXTENSION);

		TextInfo.kwipUnitName = textName + "KwipUnit";
		TextInfo.kwipUnitPath = createFilePath(kwipUnitName, TXT_EXTENSION);

		TextInfo.prettyKwipName = textName + "PrettyKwip";
		TextInfo.prettyKwipPath = createFilePath(prettyKwipName, HTML_EXTENSION);
	}

	private static void setSuffixTreeNameAndPath() {
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

	public static String getKwipTypeName() {
		return kwipTypeName;
	}

	public static String getKwipTypePath() {
		return kwipTypePath;
	}

	public static String getKwipUnitName() {
		return kwipUnitName;
	}

	public static String getKwipUnitPath() {
		return kwipUnitPath;
	}

	public static String getSuffixTreePath() {
		return suffixTreePath;
	}

	public static String getPrettyKwipName() {
		return prettyKwipName;
	}

	public static String getPrettyKwipPath() {
		return prettyKwipPath;
	}
}