package modularization.workbench;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Provides a CLI to run module trees.
 * @author Marcel Boeing
 *
 */
public class ModuleBatchRunner {

	public static void main(String[] args) {
		
		// Define command line options
		Options options = new Options();
		options.addOption("c", "config", true, "Module tree configuration file");
		options.addOption("h", "help", false, "Show help and exit");
		
		// Instantiate parser for CLI options
		CommandLineParser parser = new org.apache.commons.cli.DefaultParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse( options, args);
		} catch (ParseException e) {
			Logger.getLogger("").log(Level.SEVERE, "Parsing of the command line options failed.", e);
			System.exit(1);
		}
		
		// Show help if requested
		if (commandLine.hasOption("h")) {
			HelpFormatter lvFormater = new HelpFormatter();
			lvFormater.printHelp("java [-server -d64 -Xms500m -Xmx7500m] -jar ModuleBatchRunner.jar <options>", options);
			System.exit(0);
		}
		
		// Read config file path from CLI option
		String configFilePath = null;
		if(commandLine.hasOption("c")) {
			configFilePath = commandLine.getOptionValue("c");
		} else {
			// No config file path option present; cannot continue.
			Logger.getLogger("").log(Level.SEVERE, "Please use the option -c <file> to set a module tree config file to use.");
			System.exit(1);
		}
		
		/*
		 *  All options are read, now to create a controller and reconstruct the module tree from the config file
		 */
		
		// Create new controller
		ModuleWorkbenchController controller = null;
		try {
			controller = new ModuleWorkbenchController();
		} catch (Exception e) {
			Logger.getLogger("").log(Level.SEVERE, "Could not instantiate a new module workbench controller.", e);
			System.exit(1);
		}
		
		// Load module tree config file
		try {
			controller.loadModuleTreeFromFile(new File(configFilePath));
		} catch (Exception e) {
			Logger.getLogger("").log(Level.SEVERE, "Could not load module tree from given config file.", e);
			System.exit(1);
		}
		
		// Finally: Run the module tree
		try {
			controller.getModuleTree().runModules(true);
		} catch (Exception e) {
			Logger.getLogger("").log(Level.SEVERE, "Error running the module tree.", e);
			System.exit(1);
		}
		
		Logger.getLogger("").log(Level.INFO, "Finished successfully.");
		
	}

}
