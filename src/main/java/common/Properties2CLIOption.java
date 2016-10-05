package common;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import modules.Port;

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

	// TODO cmdLineArgs2InputPorts & -OutputPorts (may need filereader and -writer modules)
	
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
	 * Add I/O parameters to specified options instance.
	 * @param inputPorts input ports
	 * @param options Options
	 * @return Options
	 * @throws Exception thrown if something goes wrong
	 */
	public static Options inputPorts2cliOptions(Map<String, Port> inputPorts, Options options) throws Exception {

		if (options == null)
			options = new Options();
		
		// Define ASCII range for option characters
		int min = 65; // A
		int max = 78; // N
		
		return Properties2CLIOption.ports2cliOptions(inputPorts, options, min, max, INPUT_PREFIX);
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
		
		// Define ASCII range for option characters
		int min = 79; // O
		int max = 90; // Z
		
		return Properties2CLIOption.ports2cliOptions(outputPorts, options, min, max, OUTPUT_PREFIX);
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
	 *             characters (a-z) to assign to them.
	 */
	public static Options properties2cliOptions(Properties properties, Map<String, String> propertyDescriptions, Options options)
			throws Exception {

		// Stash of mnemonics we can use (like "-c" for "--config")
		TreeSet<String> unUsedMnemonics = new TreeSet<String>();
		for (int i = 97; i < 123; i++)
			unUsedMnemonics.add("" + ((char) i));

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

}
