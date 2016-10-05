package base.workbench;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.Options;

import common.Properties2CLIOption;
import modules.Module;
import modules.Port;

public class ModuleRunner {

	public ModuleRunner(Module module, String[] args) throws Exception {
		
		// Create options instance from module properties
		Options options = Properties2CLIOption.properties2cliOptions(module.getProperties(), module.getPropertyDescriptions());
		
		// Create port map for input ports (needed to get a map with the interface class "Port" as value)
		Map<String,Port> inputPortMap = new HashMap<String,Port>();
		inputPortMap.putAll(module.getInputPorts());
		// Dito for output ports
		Map<String,Port> outputPortMap = new HashMap<String,Port>();
		outputPortMap.putAll(module.getOutputPorts());
		
		// Add I/O parameters to options instance
		Properties2CLIOption.inputPorts2cliOptions(inputPortMap, options);
		Properties2CLIOption.outputPorts2cliOptions(outputPortMap, options);
		
		// Parse command line and convert the values to properties that our modules understand
		Properties properties = Properties2CLIOption.cmdLineArgs2Properties(args, options);
		
		// Set and apply properties
		module.setProperties(properties);
		module.applyProperties();
		
		// TODO Run module
		
	}

}
