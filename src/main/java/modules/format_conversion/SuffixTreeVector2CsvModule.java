package modules.format_conversion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class SuffixTreeVector2CsvModule extends ModuleImpl {

	public static final String PROPERTYKEY_CSVDELIMITER = "CSV delimiter";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "input";
	private static final String ID_OUTPUT = "output";

	// Local variables
	private String csvdelimiter;

	public SuffixTreeVector2CsvModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("Converts a JSON-formatted list of vectors (output from SuffixTreeVectorizationWrapper) into a CSV matrix.");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_CSVDELIMITER,
				"String to use as CSV field delimiter.");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Suffix Tree Vectors to CSV converter");
		this.getPropertyDefaultValues().put(PROPERTYKEY_CSVDELIMITER, ";");

		// Define I/O
		InputPort inputPort = new InputPort(ID_INPUT,
				"JSON-formatted list of vectors (output from SuffixTreeVectorizationWrapper).", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT,
				"CSV matrix.", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process() throws Exception {
		
		

		// Instantiate JSON parser
		Gson gson = new Gson();
		
		// Read JSON from input & parse it
		LinkedTreeMap<String,Object> ltm = new LinkedTreeMap<String,Object>();
		ltm = gson.fromJson(this.getInputPorts().get(ID_INPUT).getInputReader(), ltm.getClass());
		
		ArrayList<Object> types = (ArrayList<Object>)ltm.get("types");
		
		Iterator<Object> ltmki = types.iterator();
		while (ltmki.hasNext()){
			LinkedTreeMap<String,Object> type = (LinkedTreeMap<String, Object>) ltmki.next();
			
			String name = type.get("string").toString();
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(name+this.csvdelimiter);
			LinkedTreeMap<String, Object> featureMap = (LinkedTreeMap<String, Object>) type.get("vector");
			
			ArrayList<?> featureList = (ArrayList<?>) featureMap.get("features");
			for (int i=0; i<featureList.size(); i++){
				this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(featureList.get(i)+this.csvdelimiter);
			}
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");
			
		}
		
		

		// Close outputs (important!)
		this.closeAllOutputs();

		// Done
		return true;
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		this.csvdelimiter = this.getProperties().getProperty(
				PROPERTYKEY_CSVDELIMITER,
				this.getPropertyDefaultValues()
						.get(PROPERTYKEY_CSVDELIMITER));

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
