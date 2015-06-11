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

	private static final String TXT_EXTENSION = ".txt";
	private static final String XML_EXTENSION = ".xml";
	private static final String HTML_EXTENSION = ".html";
	private static final String DOT_EXTENSION = ".dot";

	private static int minLength;
	private static int maxLength;
	private static String textName;

	private static String workspacePath;
	private static String textPath;
	private static String preprocessPath;
	private static String kwipPath;
	private static String suffixTreePath;
	private static String kwipTypePath;
	private static String kwipUnitPath;
	private static String prettyKwipPath;
	private static String clusterPath;
	private static String kwipXmlPath;
	private static String ancPath;

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
			setMinLength(properties.getProperty("minLength"));
			setMaxLength(properties.getProperty("maxLength"));

			setPreprocessPath();
			setKwipPaths();
			setSuffixTreePath();
			setClusterPath();

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

	private static void setClusterPath() {
		TextInfo.clusterPath = createFilePath(textName + "Cluster",
				DOT_EXTENSION);
	}

	private static String createFilePath(String name, String extension) {
		return workspacePath + name + extension;
	}

	private static void setTextNameAndPath(String name) {
		TextInfo.textName = name;
		TextInfo.textPath = FOLDER_NAME + FILE_SEPARATOR + textName
				+ TXT_EXTENSION;
		TextInfo.ancPath = FOLDER_NAME + FILE_SEPARATOR + textName + "-vp"
				+ XML_EXTENSION;
	}

	private static void setPreprocessPath() {
		TextInfo.preprocessPath = createFilePath(textName + "Preprocess",
				TXT_EXTENSION);
	}

	private static void setKwipPaths() {
		TextInfo.kwipPath = createFilePath(textName + "Kwip", TXT_EXTENSION);
		TextInfo.kwipTypePath = createFilePath(textName + "KwipType",
				TXT_EXTENSION);
		TextInfo.kwipUnitPath = createFilePath(textName + "KwipUnit",
				TXT_EXTENSION);
		TextInfo.prettyKwipPath = createFilePath(textName + "PrettyKwip",
				HTML_EXTENSION);
		TextInfo.kwipXmlPath = createFilePath(textName + "KwipInfo",
				XML_EXTENSION);
	}

	private static void setSuffixTreePath() {
		TextInfo.suffixTreePath = createFilePath(textName + "SuffixTree",
				XML_EXTENSION);
	}

	private static void setMaxLength(String length) {
		TextInfo.maxLength = Integer.parseInt(length);
	}

	private static void setMinLength(String length) {
		TextInfo.minLength = Integer.parseInt(length);
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

	public static String getTextPath() {
		return textPath;
	}

	public static String getPreprocessPath() {
		return preprocessPath;
	}

	public static String getKwipPath() {
		return kwipPath;
	}

	public static String getKwipTypePath() {
		return kwipTypePath;
	}

	public static String getKwipUnitPath() {
		return kwipUnitPath;
	}

	public static String getSuffixTreePath() {
		return suffixTreePath;
	}

	public static String getPrettyKwipPath() {
		return prettyKwipPath;
	}

	public static String getClusterPath() {
		return clusterPath;
	}

	public static String getKwipXMLPath() {
		return kwipXmlPath;
	}

	public static String getAncPath() {
		return ancPath;
	}
}