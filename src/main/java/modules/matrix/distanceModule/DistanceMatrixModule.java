package modules.matrix.distanceModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Properties;


import common.parallelization.CallbackReceiver;
import models.NamedFieldMatrix;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import base.workbench.ModuleRunner;

public class DistanceMatrixModule extends ModuleImpl {

	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(DistanceMatrixModule.class, args);
	}

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_DELIMITER_OUTPUT = "output delimiter";
	public static final String PROPERTYKEY_ZEROVALUE = "empty value";
	public static final String PROPERTYKEY_DELIMITER_INPUT = "input delimiter";
	private static final String INPUT_MATRIX_ID = "input";

	// Define I/O IDs (must be unique for every input or output)
	// private static final String ID_INPUT = "CSV - Matrix";
	private static final String SV_OUTPUT = "output";
	public static final String PROPERTYKEY_DISTANCE = "distance type";

	// Local variables
	private String outputdelimiter = ";";
	private String inputMatrixCsvDelimiter = ";";
	private String distanceType;

	public DistanceMatrixModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("Creates a distance-matrix from any input matrix.");

		// Add property descriptions (obligatory for every property!)
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_INPUT, "Input matrix' delimiter");
		
		this.getPropertyDescriptions().put(PROPERTYKEY_DELIMITER_OUTPUT,
				"String to insert as CSV delimiter (only applicable to CSV output).");
		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Distance Matrix");

		// Property key for module name is defined in parent class
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_OUTPUT, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DELIMITER_INPUT, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DISTANCE, "ED");
		this.getPropertyDescriptions().put(PROPERTYKEY_DISTANCE,
				"Two possible distance types: \"ED\" " + "(Euclidean distance), \"CD\" (Cosine Distance) \"CS\" (Cosine similarity)");

		// Define I/O
		InputPort matrixInputPort = new InputPort(INPUT_MATRIX_ID,
				"[text/csv] A csv representation of a NamedFieldMatrix to cluster", this);
		matrixInputPort.addSupportedPipe(CharPipe.class);
		
		OutputPort outputPort = new OutputPort(SV_OUTPUT, "CSV Type Matrix output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		
		// Add I/O ports to instance (don't forget...)
		super.addInputPort(matrixInputPort);
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {

		InputPort matrixInput = this.getInputPorts().get(INPUT_MATRIX_ID);

		// matrix input
		if (matrixInput.isConnected()) {
			System.out.println("MatrixInput is connected!");
			BufferedReader matrixInReader = new BufferedReader(matrixInput.getInputReader());
			NamedFieldMatrix matrix = NamedFieldMatrix.parseCSV(matrixInReader, inputMatrixCsvDelimiter);
			NamedFieldMatrix distanceMatrix = null;
			switch (distanceType) {
			case "ED":
				distanceMatrix = generateDistanceMatrix(matrix, "ED");
				break;
			case "CS":
				distanceMatrix = generateDistanceMatrix(matrix, "CS");
				break;
			case "CD":
				distanceMatrix = generateDistanceMatrix(matrix, "CD");
				break;
			default:
				distanceMatrix = generateDistanceMatrix(matrix, "ED");
				break;
			}

			this.getOutputPorts().get(SV_OUTPUT).outputToAllCharPipes(distanceMatrix.csvHeader());
			for (int i = 0; i < distanceMatrix.getRowNames().size(); i++) {
				this.getOutputPorts().get(SV_OUTPUT).outputToAllCharPipes(distanceMatrix.csvLine(i).replaceAll(";;", ";" + new Double(0.0) + ";"));
			}
			
		}
		this.closeAllOutputs();
		return true;

	}

	private NamedFieldMatrix generateDistanceMatrix(NamedFieldMatrix nmfin, String distanceType) throws IOException {
		System.out.println("generating Distance");
		NamedFieldMatrix nmf = new NamedFieldMatrix();
		
		nmf.setDelimiter(outputdelimiter);

		for (String row : nmfin.getRowNames()) {
			for (String key : nmfin.getRowNames()) {
				double currentDouble = 0.0;
				if (distanceType.equals("CS")) {
					currentDouble = VectorMath.cosineSimilarity(nmfin.getRow(row), nmfin.getRow(key));
				}
				if (distanceType.equals("ED")) {
					currentDouble = VectorMath.euclidianDistance(nmfin.getRow(row), nmfin.getRow(key));
				}
				if (distanceType.equals("CD")) {
					currentDouble = VectorMath.cosineDistance(nmfin.getRow(row), nmfin.getRow(key));
				}
				nmf.setValue(row, key, currentDouble);
			}
		}
		return nmf;
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();
		// Apply own properties
		this.outputdelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_OUTPUT,
				this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_OUTPUT));
		this.inputMatrixCsvDelimiter = this.getProperties().getProperty(PROPERTYKEY_DELIMITER_INPUT,
				this.getPropertyDefaultValues().get(PROPERTYKEY_DELIMITER_INPUT));
		this.distanceType = this.getProperties().getProperty(PROPERTYKEY_DISTANCE,
				this.getPropertyDefaultValues().get(PROPERTYKEY_DISTANCE));
		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
