package modules.basic_text_processing;

import java.io.BufferedReader;
import java.util.Properties;

import base.workbench.ModuleWorkbenchController;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import base.workbench.ModuleRunner;

public class RegExLineFilterModule extends ModuleImpl {

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(RegExLineFilterModule.class, args);
	}


	// Identifiers for the Regex to filter on and whether it should be escaped
	public static final String PROPERTYKEY_REGEX = "regex";
	public static final String PROPERTYKEY_UNESCAPE = "unescape";

	// Identifiers for inputs and outputs
	public static final String INPUT_ID = "Input lines";
	public static final String OUTPUT_MATCHES_ID = "Matches";
	public static final String OUTPUT_NON_MATCHES_ID = "Non-Matches";

	// Variables for the workflow
	private String regex = null;

	public RegExLineFilterModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// the module's name, description and category
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "RegEx Line Filter Module");
		this.setDescription("Filters input lines by the provided regex into matching and non-matching lines.");

		// description and default value for the regular expression
		this.getPropertyDescriptions().put(PROPERTYKEY_REGEX, "Regular expression to search for");
		this.getPropertyDefaultValues().put(PROPERTYKEY_REGEX, "[aeiu]");

		// setup I/O
		InputPort input = new InputPort(INPUT_ID, "[text/plain] Input lines to filter.", this);
		OutputPort matchingOut = new OutputPort(OUTPUT_MATCHES_ID, "[text/plain] Lines matching the provided regex.",
				this);
		OutputPort nonMatchingOut = new OutputPort(OUTPUT_NON_MATCHES_ID,
				"[text/plain] Lines not matching the provided regex.", this);

		input.addSupportedPipe(CharPipe.class);
		matchingOut.addSupportedPipe(CharPipe.class);
		nonMatchingOut.addSupportedPipe(CharPipe.class);

		this.addInputPort(input);
		this.addOutputPort(matchingOut);
		this.addOutputPort(nonMatchingOut);
	}

	@Override
	public boolean process() throws Exception {

		boolean result = true;

		// Input will be read line by line
		BufferedReader reader = null;
		String line = null;

		// Whether a line matches
		boolean lineMatches;

		// Possible output ports
		OutputPort matchesOut = this.getOutputPorts().get(OUTPUT_MATCHES_ID);
		OutputPort nonMatchesOut = this.getOutputPorts().get(OUTPUT_NON_MATCHES_ID);

		try {
			reader = new BufferedReader(super.getInputPorts().get(INPUT_ID).getInputReader());

			while ((line = reader.readLine()) != null) {
				lineMatches = line.matches(regex);

				if (lineMatches && matchesOut.isConnected()) {
					matchesOut.outputToAllCharPipes(line + ModuleWorkbenchController.LINEBREAK);
				} else if (!lineMatches && nonMatchesOut.isConnected()) {
					nonMatchesOut.outputToAllCharPipes(line + ModuleWorkbenchController.LINEBREAK);
				}
			}

		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
			this.closeAllOutputs();
		}

		return result;
	}

	@Override
	public void applyProperties() throws Exception {
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		this.regex = this.getProperties().getProperty(PROPERTYKEY_REGEX,
				this.getPropertyDefaultValues().get(PROPERTYKEY_REGEX));

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
