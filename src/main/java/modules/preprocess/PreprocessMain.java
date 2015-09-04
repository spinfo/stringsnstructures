package modules.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.LoggerConfigurator;
import common.TextInfo;

public class PreprocessMain {

	private static final Logger LOGGER = Logger.getGlobal();
	
	public static void main (String[] args){
		run();
	}

	public static void run() {
		LoggerConfigurator.configGlobal();
		String workspacePath = TextInfo.getWorkspacePath();

		String textPath = TextInfo.getTextPath();
		String outputPath = TextInfo.getPreprocessPath();

		String text = "";
		StringBuffer filterBuf = null;
		LOGGER.info("PreprocessMain run " + workspacePath + textPath);
		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(textPath)), "UTF-8"));

			Preprocess preprocess = new Preprocess();
			text = preprocess.readText(reader);
			text = preprocess.process(text);
			filterBuf = preprocess.filter(text, TextInfo.getMinLength(),
					TextInfo.getMaxLength());

		} catch (Exception e) {
			e.printStackTrace();
		}

		// save result of preprocessing to a file in parent folder of project
		try {
			PrintWriter out = new PrintWriter(new FileWriter(outputPath));
			out.print(filterBuf);
			out.close();

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception in PreprocessMain.run", e);		}

		LOGGER.exiting("PreprocessMain", "run");
	}
}
