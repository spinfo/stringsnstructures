package common;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.cli.Options;

/**
 * Provides methods used to construct a CLI interface from module properties.
 * @author marcel
 *
 */
public class Properties2CLIOption {

	/**
	 * Provides an Options element containing the specified properties+descriptions.
	 * @param properties Properties
	 * @param propertyDescriptions Descriptions for properties. Must use the same keys.
	 * @return Options
	 * @throws Exception Thrown if there are too many options and not enough characters (a-z) to assign to them.
	 */
	public static Options getCLIOptions(Properties properties, Map<String, String> propertyDescriptions) throws Exception {
		// Define command line options
		Options options = new Options();
		
		// Stash of mnemonics we can use (like "-c" for "--config")
		TreeSet<String> unUsedMnemonics = new TreeSet<String>();
		for (int i=97; i<123; i++)
			unUsedMnemonics.add(""+((char)i));
		
		// Loop over properties
		Iterator<Object> keys = properties.keySet().iterator();
		while(keys.hasNext()){
			
			// Retrieve property key, this will be our long option string
			String longOpt = keys.next().toString();
			// We also need a short option string (one character)
			String shortOpt = null;
			
			// If possible, use a character contained in the long option for the short option
			for (int i=0; i<longOpt.length(); i++){
				String opt = longOpt.substring(i, i+1);
				if (unUsedMnemonics.remove(opt)){
					shortOpt = opt;
					break;
				}
			}
			
			// If there was no hitherto unused character that we can use …
			if (shortOpt == null){
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
