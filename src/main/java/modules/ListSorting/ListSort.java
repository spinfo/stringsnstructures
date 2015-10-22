package modules.ListSorting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Pattern;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.NotSupportedException;
import modules.OutputPort;

/**
 * Module to sort a list of Strings separated by line breaks. Duplicate strings
 * are not preserved. Ordering is "ascii-betical". Reads from and writes to
 * CharPipe. Can be configured to preserve information appearing on an
 * additional line (like sentence number and anchors), which is then preserved
 * and appended to the output after the String in question. Additional lines are
 * prepended with a tabstop in the output.
 * 
 * Example Input:
 * 
 * word 
 *   3, 24, 28
 * word
 *   2, 13, 17 
 * vord
 *   3, 7, 11
 * 
 * Output then is:
 * 
 * vord
 * 	3, 7, 11
 * word
 * 	3, 24, 28
 * 	2, 13, 17
 * 
 * @author David Neugebauer
 */
public class ListSort extends modules.ModuleImpl {

	// Strings identifying/describing in- and output pipes
	private final static String INPUT_ID = "input";
	private final static String OUTPUT_ID = "output";
	private final static String INPUT_DESC = "Plain text character input";
	private final static String OUTPUT_DESC = "Plain text character ouput";

	// The Pattern to divide input by and the String to divide output by
	private static final Pattern INPUT_SEPARATOR_PATTERN = Pattern.compile("\r\n|\n|\r");
	private static final char OUTPUT_SEPARATOR = '\n';
	private static final char OUTPUT_INFO_PREFIX = '\t';

	// Name and description for the User
	private final static String MODULE_NAME = "List Sorting";
	private final static String MODULE_DESCRIPTION = "Module to sort a list of Strings separated by line breaks."
			+ " Duplicate strings are not preserved. Ordering is \"ascii-betical\"."
			+ " Reads from and writes to CharPipe";

	// boolean property describing whether text is only String instances or
	// contains
	// additional information in the following lines
	private final static String PROPERTY_KEY_PRESERVE_NEXT_LINE = "Preserve next line";
	private final static String DESCRIPTION_PRESERVE_NEXT_LINE = "Whether each item has a second line with additional information";
	private final static String DEFAULT_PRESERVE_NEXT_LINE = "false";
	private boolean preserveNextLine;

	public ListSort(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Set the modules name and description
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, MODULE_NAME);
		this.setDescription(MODULE_DESCRIPTION);

		// Setup I/O, reads from and writes to CharPipe
		InputPort inputPort = new InputPort(INPUT_ID, INPUT_DESC, this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUT_ID, OUTPUT_DESC, this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

		// Setup property determining whether second lines should not be
		// treated as string instances or as additional information
		this.getPropertyDescriptions().put(PROPERTY_KEY_PRESERVE_NEXT_LINE, DESCRIPTION_PRESERVE_NEXT_LINE);
		this.getPropertyDefaultValues().put(PROPERTY_KEY_PRESERVE_NEXT_LINE, DEFAULT_PRESERVE_NEXT_LINE);
	}

	@Override
	public boolean process() throws Exception {
		try {
			// The whole input must be present in itemised form (i.e. split by
			// the separator) before any output can be generated.
			final TreeMap<String, ArrayList<String>> items = getItemsFromInput(this.getInputPorts().get(INPUT_ID));

			// Output the items in the order given by the item maps set of keys
			final OutputPort outputPort = this.getOutputPorts().get(OUTPUT_ID);
			final Iterator<String> itemIterator = items.keySet().iterator();
			while (itemIterator.hasNext()) {
				// Output item
				final String item = itemIterator.next();
				outputPort.outputToAllCharPipes(item + OUTPUT_SEPARATOR);

				// Output additional information if enabled
				if (this.preserveNextLine) {
					for (String additionalInfo : items.get(item)) {
						additionalInfo = additionalInfo.trim();
						outputPort.outputToAllCharPipes(OUTPUT_INFO_PREFIX + additionalInfo + OUTPUT_SEPARATOR);
					}
				}
			}
			// no catch block, this should just crash on error
		} finally {
			this.closeAllOutputs();
		}

		return true;
	}

	/**
	 * Reads from inputPort to generate a Set of item Strings that were
	 * separated by the input separator
	 * 
	 * @return A Map of items (Strings) as keys with either an array of the
	 *         additional information strings as values, or an empty array if
	 *         preserving next lines was not enabled
	 */
	private TreeMap<String, ArrayList<String>> getItemsFromInput(InputPort inputPort)
			throws IOException, InterruptedException, NotSupportedException {
		TreeMap<String, ArrayList<String>> result = new TreeMap<String, ArrayList<String>>();

		// read the whole text once
		StringBuilder totalText = new StringBuilder();
		int charCode = inputPort.getInputReader().read();
		while (charCode != -1) {
			if (Thread.interrupted()) {
				throw new InterruptedException("Thread has been interrupted.");
			}
			totalText.append((char) charCode);
			charCode = inputPort.getInputReader().read();
		}

		// split the whole text by the separator, producing items
		final String[] items = INPUT_SEPARATOR_PATTERN.split(totalText);

		// Walk through the items and add them to the result
		// i might be incremented in the loops body
		for (int i = 0; i < items.length; i++) {
			final String item = items[i];
			final ArrayList<String> additionalInfos = result.getOrDefault(item, new ArrayList<String>());
			// Add additional information to the array if enabled, incrementing
			// i
			if (this.preserveNextLine && (i + 1 < items.length)) {
				additionalInfos.add(items[++i]);
			}
			result.put(item, additionalInfos);
		}

		return result;
	}

	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();

		if (this.getProperties().containsKey(PROPERTY_KEY_PRESERVE_NEXT_LINE)) {
			this.preserveNextLine = Boolean
					.parseBoolean(this.getProperties().getProperty(PROPERTY_KEY_PRESERVE_NEXT_LINE));
		}

		super.applyProperties();
	}

}
