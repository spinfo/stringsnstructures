package modules.matrix;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class BowTypeMatrixModule extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_DELIMITER_OUTPUT = "output delimiter";
	public static final String PROPERTYKEY_ZEROVALUE = "empty value";
	
	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "BoW";
	private static final String ID_OUTPUT = "Type Matrix";
	
	// Local variables
	private String outputdelimiter;
	private String emptyFieldValue;

	public BowTypeMatrixModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Creates a type-type matrix from Bag of Words data.");
		
		// Add module category
		this.setCategory("Matrix");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT, "String to insert as segmentation delimiter into the output name-sum-pairs.");
		this.getPropertyDescriptions().put(PROPERTYKEY_ZEROVALUE, "String to insert as empty value into the output.");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "BoW Type Matrix"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ZEROVALUE, "0");
		
		// Define I/O
		InputPort inputPort = new InputPort(ID_INPUT, "JSON BoW data input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "CSV Type Matrix output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process() throws Exception {
		
		// Map for result matrix
		Map<String,Map<String,Double>> matrix = new TreeMap<String,Map<String,Double>>();
		
		// JSON parser
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		// Read and parse input
		Map<Double,Map<String,Double>> bowMap = new HashMap<Double,Map<String,Double>>();
		bowMap = gson.fromJson(this.getInputPorts().get(ID_INPUT).getInputReader(), bowMap.getClass());
		
		// Iterate over input BoW's
		Iterator<Map<String,Double>> bows = bowMap.values().iterator();
		while(bows.hasNext()){
			Map<String,Double> bow = bows.next();
			
			// Iterate over tokens within current BoW
			Iterator<String> tokens = bow.keySet().iterator();
			while(tokens.hasNext()){
				
				// Determine next token
				String token = tokens.next();
				
				// If the type has no entry yet, we can just add the current BoW
				if (!matrix.containsKey(token))
					matrix.put(token, bow);
				
				// Else we add the values of the current BoW to the existing entry
				else {
					Map<String,Double> existingEntry = matrix.get(token);
					Iterator<String> tokens2 = bow.keySet().iterator();
					while (tokens2.hasNext()){
						String token2 = tokens2.next();
						if (existingEntry.containsKey(token2)){
							existingEntry.put(token2, existingEntry.get(token2)+bow.get(token2));
						} else
							existingEntry.put(token2, bow.get(token2));
					}
				}
			}
		}
		
		// Output CSV header
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.outputdelimiter);
		Iterator<String> types = matrix.keySet().iterator();
		while(types.hasNext()){
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(types.next()+this.outputdelimiter);
		}
		this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes("\n");
		
		// Output matrix
		types = matrix.keySet().iterator();
		while(types.hasNext()){
			String type = types.next();
			this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(type+this.outputdelimiter);
			Iterator<String> types2 = matrix.keySet().iterator();
			while(types2.hasNext()){
				String type2 = types2.next();
				if (matrix.get(type).containsKey(type2))
					this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(matrix.get(type).get(type2).toString());
				else
					this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.emptyFieldValue);
				this.getOutputPorts().get(ID_OUTPUT).outputToAllCharPipes(this.outputdelimiter);
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
		this.outputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT, this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT));
		this.emptyFieldValue = this.getProperties().getProperty(PROPERTYKEY_ZEROVALUE,
				this.getPropertyDefaultValues().get(PROPERTYKEY_ZEROVALUE));
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
