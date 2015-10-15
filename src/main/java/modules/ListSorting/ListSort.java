package modules.ListSorting;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;
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
 * CharPipe.
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
	private static final String OUTPUT_SEPARATOR = "\n";

	// Name and description for the User
	private final static String MODULE_NAME = "List Sorting";
	private final static String MODULE_DESCRIPTION = "Module to sort a list of Strings separated by line breaks."
			+ " Duplicate strings are not preserved. Ordering is \"ascii-betical\"."
			+ " Reads from and writes to CharPipe";

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
	}

	@Override
	public boolean process() throws Exception {
		try {
			// The whole input must be present in itemised form (i.e. split by
			// the separator)
			// before any output can be generated.
			final TreeSet<String> items = getItemsFromInput(this.getInputPorts().get(INPUT_ID));

			// Output the items in the order given by the TreeSet
			final OutputPort outputPort = this.getOutputPorts().get(OUTPUT_ID);
			final Iterator<String> itemIterator = items.iterator();
			while (itemIterator.hasNext()) {
				final String item = itemIterator.next();
				outputPort.outputToAllCharPipes(item + OUTPUT_SEPARATOR);
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
	 * @return The Set of items (Strings)
	 */
	private TreeSet<String> getItemsFromInput(InputPort inputPort)
			throws IOException, InterruptedException, NotSupportedException {
		TreeSet<String> result = new TreeSet<String>();

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

		// split the whole text by the separator and process each item;
		final String[] items = INPUT_SEPARATOR_PATTERN.split(totalText);
		for (final String item : items) {
			result.add(item);
		}

		return result;
	}

}
