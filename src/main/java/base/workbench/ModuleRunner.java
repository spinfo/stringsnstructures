package base.workbench;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.Options;

import common.Properties2CLIOption;
import modules.Module;
import modules.ModuleImpl;
import modules.ModuleNetwork;
import modules.Port;

public class ModuleRunner {
	
	/**
	 * Instantiates a module class and executes the instance in a stand-alone fashion (without the workbench/batchrunner).
	 * @param moduleClass Module class to use
	 * @param args Command line arguments
	 * @throws Exception Thrown if something goes wrong
	 */
	public static void runStandAlone(Class<?extends Module> moduleClass, String[] args) throws Exception {
		
		// Create new controller
		ModuleWorkbenchController controller = null;
		try {
			controller = new ModuleWorkbenchController();
			controller.setModuleNetwork(new ModuleNetwork());
		} catch (Exception e) {
			Logger.getLogger("").log(Level.SEVERE, "Could not instantiate a new module workbench controller.", e);
			System.exit(1);
		}
		
		Properties moduleProperties = new Properties();
		
		// Modules should only have one constructor, so we can do this:
		@SuppressWarnings("unchecked")
		Constructor<Module> constructor = (Constructor<Module>) moduleClass.getConstructors()[0];
		Module module = constructor.newInstance(controller.getModuleNetwork(),moduleProperties);
		moduleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, module.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		module.applyProperties();
		
		ModuleRunner.runStandAlone(controller.getNewInstanceOfModule(module), args, controller);
	}

	/**
	 * Executes a module in a stand-alone fashion (without the workbench/batchrunner).
	 * @param module Module to run
	 * @param args Command line arguments
	 * @param controller ModuleWorkbenchController to use
	 * @throws Exception Thrown if something goes wrong
	 */
	public static void runStandAlone(Module module, String[] args, ModuleWorkbenchController controller) throws Exception {
		
		// Instantiate a new controller if needed
		if (controller == null)
			try {
				controller = new ModuleWorkbenchController();
				controller.setModuleNetwork(new ModuleNetwork());
			} catch (Exception e) {
				Logger.getLogger("").log(Level.SEVERE, "Could not instantiate a new module workbench controller.", e);
				System.exit(1);
			}
		
		// Create options instance from module properties
		Options options = Properties2CLIOption.properties2cliOptions(module.getProperties(), module.getPropertyDescriptions());
		
		// Add help option
		options.addOption("h", "help", false, "Lists valid command line arguments.");
		
		// Create port map for input ports (needed to get a map with the interface class "Port" as value)
		Map<String,Port> inputPortMap = new HashMap<String,Port>();
		inputPortMap.putAll(module.getInputPorts());
		// Dito for output ports
		Map<String,Port> outputPortMap = new HashMap<String,Port>();
		outputPortMap.putAll(module.getOutputPorts());
		
		// Add I/O parameters to options instance
		Properties2CLIOption.inputPorts2cliOptions(inputPortMap, options);
		Properties2CLIOption.outputPorts2cliOptions(outputPortMap, options);
		
		// Display help and exit if the help argument (or no argument at all) is detected
		if (Properties2CLIOption.listHelp(args, options, module.getName()))
			return;
		
		// Parse command line and convert the values to properties that our modules understand
		Properties properties = Properties2CLIOption.cmdLineArgs2Properties(args, options, module.getPropertyDescriptions().keySet());
		
		// Set and apply properties
		module.setProperties(properties);
		module.applyProperties();
		
		/*
		 * Attach module I/O
		 */
		// Create and attach I/O modules to module ports
		List<Module> ioModulesList = Properties2CLIOption.createIOModules(module, args, options, controller.getModuleNetwork());
		// Add created I/O modules to module network
		Iterator<Module> ioModules = ioModulesList.iterator();
		while(ioModules.hasNext()){
			controller.getModuleNetwork().addModule(ioModules.next());
		}
		
		// Add the main module to the network
		controller.getModuleNetwork().addModule(module);
		
		// Finally: Run the module tree
		try {
			controller.getModuleNetwork().runModules(true);
		} catch (Exception e) {
			Logger.getLogger("").log(Level.SEVERE, "Error running the module tree.", e);
			System.exit(1);
		}

		Logger.getLogger("").log(Level.INFO, "Finished successfully.");
	}

}
