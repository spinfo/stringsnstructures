package modules.keyWordInPhrase;

import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import common.parallelization.CallbackReceiver;

public class KwipBowMatrixModule extends ModuleImpl {
	
	// Define property keys (every setting has to have a unique key to associate it with)
	public static final String PROPERTYKEY_OMITZEROVALUES = "omit zero values";
	
	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT_KWIPUNITS = "kwip-units";
	private static final String ID_INPUT_KWIPTYPES = "kwip-types";
	private static final String ID_INPUT_BOW = "bow";
	private static final String ID_OUTPUT_MATRIX = "matrix";
	private boolean omitZeroValues;

	public KwipBowMatrixModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Constructs a matrix from Kwip and BoW results.");
		
		// Add module category
		//this.setCategory("");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_OMITZEROVALUES, "Omit zero values in output [true/false]");
		
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Kwip BoW Matrix"); // Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_OMITZEROVALUES, "false");
		
		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~).
		 * Every port can support a range of pipe types (currently
		 * byte or character pipes). Output ports can provide data
		 * to multiple pipe instances at once, input ports can
		 * in contrast only obtain data from one pipe instance.
		 */
		InputPort inputPortKwipUnits = new InputPort(ID_INPUT_KWIPUNITS, "Kwip units [plain text].", this);
		inputPortKwipUnits.addSupportedPipe(CharPipe.class);
		InputPort inputPortKwipTypes = new InputPort(ID_INPUT_KWIPTYPES, "Kwip types [plain text].", this);
		inputPortKwipTypes.addSupportedPipe(CharPipe.class);
		InputPort inputPortBow = new InputPort(ID_INPUT_BOW, "Bag of Words [json].", this);
		inputPortBow.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT_MATRIX, "Matrix output [csv].", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPortKwipUnits);
		super.addInputPort(inputPortKwipTypes);
		super.addInputPort(inputPortBow);
		super.addOutputPort(outputPort);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process() throws Exception {
		
		// Construct scanner instances for input segmentation
		Scanner kwipUnitScanner = new Scanner(this.getInputPorts().get(ID_INPUT_KWIPUNITS).getInputReader());
		kwipUnitScanner.useDelimiter("\n");
		Scanner kwipTypeScanner = new Scanner(this.getInputPorts().get(ID_INPUT_KWIPTYPES).getInputReader());
		kwipTypeScanner.useDelimiter("\n");
		
		// Parse BoW input
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		TreeMap<Integer,LinkedTreeMap<String,Double>> bowMap = null;
		bowMap = gson.fromJson(this.getInputPorts().get(ID_INPUT_BOW).getInputReader(), TreeMap.class);
		
		// Prepare output matrix
		TreeMap<String,LinkedTreeMap<String,Double>> matrix = new TreeMap<String,LinkedTreeMap<String,Double>>();
		
		// Map index
		Integer mapIndex = 0;
		
		// Input read loop
		while (true){
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				kwipUnitScanner.close();
				kwipTypeScanner.close();
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// Check whether inputs have more segments
			if (!(kwipUnitScanner.hasNext() && kwipTypeScanner.hasNext())){
				break;
			}
			
			// Read next segments
			Integer unit = Integer.parseInt(kwipUnitScanner.next());
			String type = kwipTypeScanner.next();
			
			// Read map at index
			LinkedTreeMap<String,Double> typeMap = bowMap.get(mapIndex.toString());
			
			// Add following maps that belong to the current unit
			while((++mapIndex)<unit){
				// Read map at index
				LinkedTreeMap<String,Double> nextTypeMap = bowMap.get(mapIndex.toString());
				// Add maps
				Iterator<String> keys = nextTypeMap.keySet().iterator();
				while(keys.hasNext()){
					String key = keys.next();
					Double value = nextTypeMap.get(key);
					if (typeMap.containsKey(key))
						value += typeMap.get(key);
					typeMap.put(key, value);
				}
			}
			
			// Add to matrix
			matrix.put(type, typeMap);
			
		}

		//Close input scanners.
		kwipUnitScanner.close();
		kwipTypeScanner.close();
		
		/*
		 *  Output matrix
		 */
		
		// Get matrix keys (map of all types read)
		Set<String> typeSet = matrix.keySet();
		
		// Output header line
		this.getOutputPorts().get(ID_OUTPUT_MATRIX).outputToAllCharPipes(";");
		Iterator<String> types = typeSet.iterator();
		while(types.hasNext()){
			String type = types.next();
			this.getOutputPorts().get(ID_OUTPUT_MATRIX).outputToAllCharPipes(type+";");
		}
		this.getOutputPorts().get(ID_OUTPUT_MATRIX).outputToAllCharPipes("\n");
		
		// Output data lines
		types = typeSet.iterator();
		while(types.hasNext()){
			String type = types.next();
			this.getOutputPorts().get(ID_OUTPUT_MATRIX).outputToAllCharPipes(type+";");
			Iterator<String> types2 = typeSet.iterator();
			while(types2.hasNext()){
				String type2 = types2.next();
				if (!type.equals(type2) || !this.omitZeroValues){
					if (matrix.get(type).containsKey(type2)){
						Integer value = matrix.get(type).get(type2).intValue();
						this.getOutputPorts().get(ID_OUTPUT_MATRIX).outputToAllCharPipes(value.toString());
					} else if (!this.omitZeroValues)
						this.getOutputPorts().get(ID_OUTPUT_MATRIX).outputToAllCharPipes("0");
				}
				this.getOutputPorts().get(ID_OUTPUT_MATRIX).outputToAllCharPipes(";");
			}
			if (types.hasNext())
				this.getOutputPorts().get(ID_OUTPUT_MATRIX).outputToAllCharPipes("\n");
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
		String omitZeroValuesString = this.getProperties().getProperty(PROPERTYKEY_OMITZEROVALUES, this.getPropertyDefaultValues().get(PROPERTYKEY_OMITZEROVALUES));
		if (omitZeroValuesString != null && !omitZeroValuesString.isEmpty())
			this.omitZeroValues = Boolean.parseBoolean(omitZeroValuesString);
		
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
