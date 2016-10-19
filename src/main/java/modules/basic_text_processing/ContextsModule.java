package modules.basic_text_processing;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import base.workbench.ModuleWorkbenchController;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class ContextsModule extends ModuleImpl {

	// Identifiers for the term to find contexts of
	public static final String PROPERTYKEY_TERM = "term";
	public static final String PROPERTYKEY_CWIND = "context window";
	public static final String PROPERTYKEY_COMPOSITES = "composites";

	// Identifiers for inputs and outputs
	public static final String INPUT_ID = "input text corpus";
	public static final String OUTPUT_ID = "output contexts";
	
	private String term;
	private int cwind;
	private boolean composites;

	public ContextsModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// the module's name, description and category
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Contexts Extraction Module");
		this.setDescription("Extracts contexts of a term form a given text corpus");

		// description and default value for the regular expression
		this.getPropertyDescriptions().put(PROPERTYKEY_TERM, "term of which contexts will be extracted");
		this.getPropertyDefaultValues().put(PROPERTYKEY_TERM, "term");
		this.getPropertyDescriptions().put(PROPERTYKEY_CWIND, "Size of context window (in words)");
		this.getPropertyDefaultValues().put(PROPERTYKEY_CWIND, "10");
		this.getPropertyDescriptions().put(PROPERTYKEY_COMPOSITES, "also look for term in composites (true/false)");
		this.getPropertyDefaultValues().put(PROPERTYKEY_COMPOSITES, "false");

		// setup I/O
		InputPort input = new InputPort(INPUT_ID, "[text/plain] Input text corpus.", this);
		OutputPort output = new OutputPort(OUTPUT_ID, "[text/plain] Lines containing the contexts of the term.",
				this);
		
		input.addSupportedPipe(CharPipe.class);
		output.addSupportedPipe(CharPipe.class);

		this.addInputPort(input);
		this.addOutputPort(output);
	}

	@Override
	public boolean process() throws Exception {
		boolean result = true;

		// Input will be read line by line
		BufferedReader reader = null;
		String line = null;

		// Possible output ports
		OutputPort out = this.getOutputPorts().get(OUTPUT_ID);

		try {
			reader = new BufferedReader(super.getInputPorts().get(INPUT_ID).getInputReader());

			while ((line = reader.readLine()) != null) {
				for (String c : trimTextMulti(line, term, cwind/2, composites)){
					out.outputToAllCharPipes(c.trim() + ModuleWorkbenchController.LINEBREAK);
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
		this.term = this.getProperties().getProperty(PROPERTYKEY_TERM,
				this.getPropertyDefaultValues().get(PROPERTYKEY_TERM));
		
		String cwindProp = this.getProperties().getProperty(PROPERTYKEY_CWIND,
				this.getPropertyDefaultValues().get(PROPERTYKEY_CWIND));
		this.cwind = cwindProp == null ? 10 : Integer.parseInt(cwindProp);
		
		this.composites = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_COMPOSITES,
				this.getPropertyDefaultValues().get(PROPERTYKEY_COMPOSITES)));
		
		super.applyProperties();
	}
	
	
	public static List<String> trimTextMulti(String text,
											 String around,
											 int contextNrOfWords,
											 boolean useSubstrings) {
		
		List<String> out = new ArrayList<String>();
		String[] tokens = text.replaceAll("\\P{L}", " ")
							  .replaceAll("\\s+", " ")
							  .split(" ");
		
		around = around.toUpperCase();
		int min;
		int max;
		int ind = -1;

		for (int i = ind+1; i < tokens.length; i++) {
			if (useSubstrings && !tokens[i].toUpperCase().contains(around)) continue;
			if (!useSubstrings && !tokens[i].equalsIgnoreCase(around)) continue;
			ind = i;
			min = Math.max(ind - contextNrOfWords, 0);
			max = Math.min(ind + contextNrOfWords + 1, tokens.length);
			
			StringBuilder sb = new StringBuilder();
			for (int j = min; j < max; j++) {
				sb.append(tokens[j]);
				sb.append(" ");
			}
			out.add(sb.toString());
		}

		return out;
	}

}
