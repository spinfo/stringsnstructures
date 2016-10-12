package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import modules.Module;
import modules.ModuleImpl;
import modules.ModuleNetwork;
import modules.Port;
import modules.input_output.ConsoleReaderModule;
import modules.input_output.ConsoleWriterModule;
import modules.input_output.FileReaderModule;
import modules.input_output.FileWriterModule;

/**
 * Provides methods used to extract CLI parameters from module properties and
 * vice versa.
 * 
 * @author marcel
 *
 */
public class Properties2CLIOption {
	
	// Prefixes for i/o port options
	public static final String INPUT_PREFIX = "I";
	public static final String OUTPUT_PREFIX = "O";
	
	/**
	 * Creates module properties from the specified command line arguments + CLI options set.
	 * @param args Command line parameters
	 * @param options CLI option set
	 * @return Module properties
	 * @throws ParseException Thrown if parsing the command line failed
	 */
	public static Properties cmdLineArgs2Properties(String[] args, Options options) throws ParseException {
		// Instantiate new Properties object
		Properties properties = new Properties();

		// Instantiate parser for CLI options
		CommandLineParser parser = new org.apache.commons.cli.DefaultParser();
		CommandLine commandLine = null;
		commandLine = parser.parse(options, args);
		
		// Loop over available options
		Iterator<Option> optionsIterator = options.getOptions().iterator();
		while(optionsIterator.hasNext()){
			// Determine next option in list
			Option option = optionsIterator.next();
			// Check whether this option is present in the command line parameters and if so, get its value
			String value = null;
			if (commandLine.hasOption(option.getOpt())){
				// Determine the parameter value
				value = commandLine.getOptionValue(option.getOpt());
			}
			
			// If we have got a value, insert it into the Properties object
			if (value != null){
				properties.setProperty(option.getLongOpt(), value);
			}
		}

		return properties;
	}
	
	/**
	 * Creates a map of I/O IDs and respectively attributed file names from
	 * the specified command line arguments + CLI options set. Returns file
	 * names as string to permit use of system stdin/stdout via "-".
	 * 
	 * @param args
	 *            Command line parameters
	 * @param options
	 *            CLI option set
	 * @param prefix
	 *            Prefix identifying the I/O IDs in the command line input
	 * @return Map<String,String> of file IDs and file names
	 * @throws ParseException
	 *             Thrown if parsing the command line failed
	 */
	public static Map<String,String> cmdLineArgs2FileNames(String[] args, Options options, String prefix) throws ParseException {
		// Create new map for the result
		Map<String,String> resultMap = new HashMap<String,String>();
		
		// Instantiate parser for CLI options
		CommandLineParser parser = new org.apache.commons.cli.DefaultParser();
		CommandLine commandLine = null;
		commandLine = parser.parse(options, args);
		
		// Loop over available options
		Iterator<Option> optionsIterator = options.getOptions().iterator();
		while (optionsIterator.hasNext()) {
			// Determine next option in list
			Option option = optionsIterator.next();
			// Check whether this option denotes an input/output and is present in the command line
			if (option.getLongOpt().startsWith(prefix) && commandLine.hasOption(option.getOpt())) {
				// Determine the parameter value and add it to map
				String value = commandLine.getOptionValue(option.getOpt());
				resultMap.put(option.getLongOpt(), value);
			}
		}
		
		// Return result
		return resultMap;
	}
	
	/**
	 * Creates a map of input IDs and respectively attributed file names from
	 * the specified command line arguments + CLI options set. Returns file
	 * names as string to permit use of system stdin via "-".
	 * 
	 * @param args
	 *            Command line parameters
	 * @param options
	 *            CLI option set
	 * @return Map<String,String> of file IDs and file names
	 * @throws ParseException
	 *             Thrown if parsing the command line failed
	 */
	public static Map<String,String> cmdLineArgs2InputFileNames(String[] args, Options options) throws ParseException {
		return Properties2CLIOption.cmdLineArgs2FileNames(args, options, INPUT_PREFIX);
	}
	
	/**
	 * Creates a map of output IDs and respectively attributed file names from
	 * the specified command line arguments + CLI options set. Returns file
	 * names as string to permit use of system stdout via "-".
	 * 
	 * @param args
	 *            Command line parameters
	 * @param options
	 *            CLI option set
	 * @return Map<String,String> of file IDs and file names
	 * @throws ParseException
	 *             Thrown if parsing the command line failed
	 */
	public static Map<String,String> cmdLineArgs2OutputFileNames(String[] args, Options options) throws ParseException {
		return Properties2CLIOption.cmdLineArgs2FileNames(args, options, OUTPUT_PREFIX);
	}
	
	
	
	/**
	 * Add I/O parameters to specified options instance.
	 * @param inputPorts input ports
	 * @param options Options
	 * @return Options
	 * @throws Exception thrown if something goes wrong
	 */
	public static Options inputPorts2cliOptions(Map<String, Port> inputPorts, Options options) throws Exception {

		if (options == null)
			options = new Options();
		
		return Properties2CLIOption.ports2cliOptions(inputPorts, options, 'A', 'N', INPUT_PREFIX);
	}
	
	/**
	 * Add I/O parameters to specified options instance.
	 * @param outputPorts input ports
	 * @param options Options
	 * @return Options
	 * @throws Exception thrown if something goes wrong
	 */
	public static Options outputPorts2cliOptions(Map<String, Port> outputPorts, Options options) throws Exception {

		if (options == null)
			options = new Options();
		
		return Properties2CLIOption.ports2cliOptions(outputPorts, options, 'O', 'Z', OUTPUT_PREFIX);
	}
	
	public static Options ports2cliOptions(Map<String,Port> portMap, Options options, int min, int max, String prefix) throws Exception{

		// Stash of mnemonics we can use (like "-c" for "--config")
		TreeSet<String> unUsedMnemonics = new TreeSet<String>();
		for (int i = min; i <= max; i++)
			unUsedMnemonics.add("" + ((char) i));
		
		// Loop over input ports
		Iterator<String> portNames = portMap.keySet().iterator();
		while(portNames.hasNext()){
			// Retrieve input port's name
			String portName = portNames.next();
			// Grab an unused char for the short opt
			String opt = unUsedMnemonics.pollFirst();
			if (opt==null)
				throw new Exception("Too many ports -- there are not enough characters to label all of them.");
			/*
			 *  Long opt consists of uppercase prefix + port name.
			 *  This is needed to
			 *    a) prevent overlapping with other (lowercase) options
			 *    b) make it possible to identify the port from the option alone.
			 */
			String longOpt = prefix+portName.replace(' ', '_');
			// Add option to set
			options.addOption(opt, longOpt, true, portMap.get(portName).getDescription());
		}
		
		// Return options set
		return options;
	}
	


	/**
	 * Provides an Options element containing the specified
	 * properties+descriptions.
	 * 
	 * @param properties
	 *            Properties
	 * @param propertyDescriptions
	 *            Descriptions for properties. Must use the same keys.
	 * @return Options
	 * @throws Exception
	 *             Thrown if there are too many options and not enough
	 *             characters (a-z) to assign to them.
	 */
	public static Options properties2cliOptions(Properties properties, Map<String, String> propertyDescriptions)
			throws Exception {
		// Instantiate new command line options
		Options options = new Options();
		
		return Properties2CLIOption.properties2cliOptions(properties, propertyDescriptions, options);
	}

	/**
	 * Provides an Options element containing the specified
	 * properties+descriptions.
	 * 
	 * @param properties
	 *            Properties
	 * @param propertyDescriptions
	 *            Descriptions for properties. Must use the same keys.
	 * @param options
	 *            Options set to add the newly created options to
	 * @return Options
	 * @throws Exception
	 *             Thrown if there are too many options and not enough
	 *             characters (a-z, excluding 'h') to assign to them.
	 */
	public static Options properties2cliOptions(Properties properties, Map<String, String> propertyDescriptions, Options options)
			throws Exception {

		// Stash of mnemonics we can use (like "-c" for "--config")
		TreeSet<String> unUsedMnemonics = new TreeSet<String>();
		for (int i = 'a'; i < 'z'; i++){
			if (i=='h')
				continue;
			unUsedMnemonics.add("" + ((char) i));
		}

		// Loop over properties
		Iterator<Object> keys = properties.keySet().iterator();
		while (keys.hasNext()) {

			// Retrieve property key, this will be our long option string
			String longOpt = keys.next().toString();
			// We also need a short option string (one character)
			String shortOpt = null;

			// If possible, use a character contained in the long option for the
			// short option
			for (int i = 0; i < longOpt.length(); i++) {
				String opt = longOpt.substring(i, i + 1);
				if (unUsedMnemonics.remove(opt)) {
					shortOpt = opt;
					break;
				}
			}

			// If there was no hitherto unused character that we can use …
			if (shortOpt == null) {
				// … we just grab a free one from the end of the set …
				shortOpt = unUsedMnemonics.pollLast();

				// … or throw an exception if none were left.
				if (shortOpt == null)
					throw new Exception("Too many options -- there are not enough characters to label all of them.");

			}
			// At this stage we have all we need to construct the option element
			options.addOption(shortOpt, longOpt, true, propertyDescriptions.get(longOpt));
		}

		// Return the complete option set
		return options;
	}
	
	/**
	 * Creates and connects an I/O module (stdin/out or file) to any I/O port of the specified module that is set via the command line arguments. 
	 * @param module Module with I/O ports
	 * @param args Command line arguments
	 * @param options Command line options
	 * @param moduleNetwork ModuleNetwork to add the I/O modules to
	 * @return List of I/O modules
	 * @throws Exception Thrown if something goes wrong
	 */
	public static List<Module> createIOModules(Module module, String[] args, Options options, ModuleNetwork moduleNetwork) throws Exception{
		List<Module> ioModules = new ArrayList<Module>();
		
		// Determine values for input and output
		Map<String, String> inputFileNamesMap = cmdLineArgs2InputFileNames(args, options);
		Map<String, String> outputFileNamesMap = cmdLineArgs2OutputFileNames(args, options);

		/*
		 *  Loop over module input
		 */
		Iterator<String> inputPortNames = module.getInputPorts().keySet().iterator();
		while (inputPortNames.hasNext()) {
			// Determine next input port name
			String inputPortName = inputPortNames.next();
			// Determine port name used for command line
			String inputPortNameCli = Properties2CLIOption.INPUT_PREFIX + inputPortName.replace(' ', '_');
			// Check whether the input is among the CLI arguments
			if (inputFileNamesMap.containsKey(inputPortNameCli)){
				// Determine file name
				String fileName = inputFileNamesMap.get(inputPortNameCli);
				// Input module instance
				Module inputModule;
				// Check what kind of input we need (stdin or file)
				if (fileName.equals("-")){
					// Create module for reading from stdin
					Properties consoleReaderModuleProperties = new Properties();
					inputModule = new ConsoleReaderModule(moduleNetwork, consoleReaderModuleProperties);
					consoleReaderModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, inputModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
					consoleReaderModuleProperties.setProperty(ConsoleReaderModule.PROPERTYKEY_ENCODING, "UTF-8");
					inputModule.applyProperties();
					
				} else {
					// Create module for reading from file
					Properties fileReaderProperties = new Properties();
					inputModule = new FileReaderModule(moduleNetwork,
							fileReaderProperties);
					fileReaderProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, inputModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
					fileReaderProperties.setProperty(FileReaderModule.PROPERTYKEY_INPUTFILE, fileName);
					fileReaderProperties.setProperty(FileReaderModule.PROPERTYKEY_USEGZIP, "false");
					inputModule.applyProperties();
				}
				
				// Connect module ports to each other
				moduleNetwork.addConnection(inputModule.getOutputPorts().values().iterator().next(), module.getInputPorts().get(inputPortName));
				
				// Add module to result map
				ioModules.add(inputModule);
			}
		}
		
		/*
		 *  Loop over module output
		 */
		Iterator<String> outputPortNames = module.getOutputPorts().keySet().iterator();
		while (outputPortNames.hasNext()) {
			// Determine next output port name
			String outputPortName = outputPortNames.next();
			// Determine port name used for command line
			String outputPortNameCli = Properties2CLIOption.OUTPUT_PREFIX + outputPortName.replace(' ', '_');
			// Check whether the output is among the CLI arguments
			if (outputFileNamesMap.containsKey(outputPortNameCli)){
				// Determine file name
				String fileName = outputFileNamesMap.get(outputPortNameCli);
				// Input module instance
				Module outputModule;
				// Check what kind of output we need (stdout or file)
				if (fileName.equals("-")){
					// Create module for writing to stdout
					Properties consoleWriterProperties = new Properties();
					outputModule = new ConsoleWriterModule(moduleNetwork,
							consoleWriterProperties);
					consoleWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, outputModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
					outputModule.applyProperties();
					
				} else {
					// Create module for writing to file
					Properties fileWriterProperties = new Properties();
					outputModule = new FileWriterModule(moduleNetwork,
							fileWriterProperties);
					fileWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, outputModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
					fileWriterProperties.setProperty(FileReaderModule.PROPERTYKEY_INPUTFILE, fileName);
					fileWriterProperties.setProperty(FileReaderModule.PROPERTYKEY_USEGZIP, "false");
					outputModule.applyProperties();
				}
				
				// Connect module ports to each other
				moduleNetwork.addConnection(outputModule.getInputPorts().values().iterator().next(), module.getOutputPorts().get(outputPortName));
				
				// Add module to result map
				ioModules.add(outputModule);
			}
		}
		
		return ioModules;
	}
	
	/**
	 * Lists command line options if the help argument is present or if no
	 * argument is given at all.
	 * 
	 * @param args
	 *            Command line arguments
	 * @param options
	 *            Options
	 * @param programName
	 *            Name of the program to display in help text
	 * @return True if help was listed
	 * @throws ParseException
	 *             Thrown if parsing the command line failed
	 */
	public static boolean listHelp(String[] args, Options options, String programName) throws ParseException {
		// Instantiate parser for CLI options
		CommandLineParser parser = new org.apache.commons.cli.DefaultParser();
		CommandLine commandLine = null;
		commandLine = parser.parse(options, args);

		// Display help text if the help argument is present or if no argument is given at all
		if (args.length==0 || commandLine.hasOption('h')) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( programName, options );
			System.out.println("Inputs and outputs can be either files or stdin/stdout ('-')");
			return true;
		}

		return false;
	}

}
