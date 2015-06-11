package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggerConfigurator {

	private static final Logger LOGGER = Logger
			.getLogger(LoggerConfigurator.class.getName());
	private static final LogManager LOGMANAGER = LogManager.getLogManager();

	static {
		LOGGER.setLevel(Level.ALL);
	}

	public static void configLogger(String name) {
		Logger toConfig = Logger.getLogger(name);
		determineLoggingLevel(toConfig);
	}

	public static void configGlobal() {
		Logger toConfig = Logger.getGlobal();
		determineLoggingLevel(toConfig);
	}

	private static void determineLoggingLevel(Logger toConfig) {
		LOGMANAGER.addLogger(toConfig);
		try {
			LOGMANAGER.readConfiguration(new FileInputStream(
					"./config/logger.properties"));
		} catch (IOException exception) {
			LOGGER.log(Level.SEVERE, "Error in loading configuration",
					exception);
		}
	}
}
