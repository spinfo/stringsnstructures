package base.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServerConfig {

	private static final Logger LOG = LoggerFactory.getLogger(ServerConfig.class);

	protected static final Options CLI_OPTIONS = new Options();
	static {
		CLI_OPTIONS.addOption("n", "name", true, "The name this webserver will be known by.");
		CLI_OPTIONS.addOption("p", "port", true, "The port this should liston on.");
		CLI_OPTIONS.addOption(null, "db-file", true, "The sqlite file path this should use.");
		CLI_OPTIONS.addOption(null, "enable-wb-log", false, "Whether the main application log should be enabled.");
		CLI_OPTIONS.addOption("h", "help", false, "Display this help.");
	}

	// the config used by the entire application
	private static ServerConfig config;

	// the name this server is known as (default name will be built from ip and
	// port)
	// TODO: Make sure, this is really used...
	private String name = null;

	// the port we should be reachable at
	private Integer port = null;

	// the database file to use
	private File databaseFile = null;

	// whether to turn off the logging from the main workbench loggers
	private Boolean shutdownWorkbenchLogger = null;

	// private, because there is only one config outside of this class
	private ServerConfig() {
	}

	protected static ServerConfig get() {
		return config;
	}

	protected static ServerConfig initialize(String propertiesFilePath, CommandLine cl) {
		config = defaultConfig();
		config.overwriteWith(fromPropertiesFile(propertiesFilePath));
		config.overwriteWith(fromCommandLine(cl));
		return config;
	}

	protected String getName() {
		return name;
	}

	protected Integer getPort() {
		return port;
	}

	protected File getDatabaseFile() {
		return databaseFile;
	}

	protected Boolean shouldShutdownWorkbenchLogger() {
		return shutdownWorkbenchLogger;
	}

	private static ServerConfig defaultConfig() {
		ServerConfig result = new ServerConfig();
		
		result.name = "";
		try {
			File temp = File.createTempFile("workbench-server-tmp-", ".db");
			temp.delete();
			result.setDatabaseFile(temp.getPath());
		} catch (IOException | SecurityException e) {
			LOG.warn("Unable to create then delete temporary file for default config's name.");
		}
		result.port = 4568;
		result.shutdownWorkbenchLogger = true;
		
		return result;
	}

	private static ServerConfig fromPropertiesFile(String filePath) {
		ServerConfig result = new ServerConfig();
		Properties properties = new Properties();

		InputStream propertiesFile = ServerConfig.class.getClassLoader().getResourceAsStream(filePath);
		if (propertiesFile != null) {
			try {
				properties.load(propertiesFile);
				LOG.debug(properties.toString());
				result.setName(properties.getProperty("name"));
				result.setPort(properties.getProperty("port"));
				result.setDatabaseFile(properties.getProperty("databaseFile"));
				result.setShutdownWorkbenchLogger(properties.getProperty("shutdownWorkbenchLogger"));
			} catch (IOException e) {
				LOG.error("Unable to read properties file at '" + propertiesFile + "', message: " + e.getMessage());
			}
		} else {
			LOG.warn("No server config properties file at: " + filePath);
		}

		return result;
	}

	private static ServerConfig fromCommandLine(CommandLine cl) {
		ServerConfig result = new ServerConfig();

		result.setName(cl.getOptionValue("name"));
		result.setPort(cl.getOptionValue("port"));
		result.setDatabaseFile(cl.getOptionValue("db-file"));
		if (cl.hasOption("enable-wb-log")) {
			result.setShutdownWorkbenchLogger("true");
		}

		return result;
	}

	private void overwriteWith(ServerConfig other) {
		if (other.name != null) {
			this.name = other.name;
		}
		if (other.port != null) {
			this.port = other.port;
		}
		if (other.databaseFile != null) {
			this.databaseFile = other.databaseFile;
		}
		if (other.shutdownWorkbenchLogger != null) {
			this.shutdownWorkbenchLogger = other.shutdownWorkbenchLogger;
		}
	}

	private void setName(String name) {
		if (name == null || name.isEmpty()) {
			this.name = null;
		} else {
			this.name = name;
		}
	}

	private void setPort(String portNr) {
		if (portNr == null || portNr.isEmpty()) {
			return;
		}
		try {
			this.port = Integer.parseInt(portNr);
		} catch (NumberFormatException e) {
			LOG.warn("Unable to parse port number: '" + portNr + "'");
			this.port = null;
		}
	}

	private void setDatabaseFile(String databaseFilePath) {
		if (databaseFilePath == null || databaseFilePath.isEmpty()) {
			return;
		}
		boolean createdTestFile = false;
		File file = new File(databaseFilePath);
		try {
			if (!file.exists()) {
				LOG.debug("Creating empty database file at: " + file.getAbsolutePath());
				createdTestFile = file.createNewFile();
			}
			if (file.canWrite()) {
				this.databaseFile = file;
			}
		} catch (IOException e) {
			LOG.error("Unable to create database file: at '" + databaseFilePath + "', msg: " + e.getMessage());
		} finally {
			if (createdTestFile) {
				file.delete();
			}
		}
	}

	private void setShutdownWorkbenchLogger(String boolString) {
		if (boolString == null || boolString.isEmpty()) {
			return;
		}
		this.shutdownWorkbenchLogger = Boolean.parseBoolean(boolString);
	}

}
