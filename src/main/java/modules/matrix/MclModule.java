package modules.matrix;

import java.io.PipedReader;
import java.util.Properties;

import Jama.Matrix;
import common.parallelization.CallbackReceiver;
import models.NamedFieldMatrix;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class MclModule extends ModuleImpl {

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_L = "l";
	public static final String PROPERTYKEY_R = "r";
	public static final String PROPERTYKEY_ITERATIONS = "iterations";
	public static final String PROPERTYKEY_CSV_DELIMITER = "csv delimiter";

	// Define I/O IDs (must be unique for every input or output)
	private static final String ID_INPUT = "input matrix";
	private static final String ID_OUTPUT = "output matrix";

	private int iterations;
	private int l;
	private double r;
	private String csvDelimiter;

	public MclModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("Takes a matrix and runs a fixed number of iterations of markov clustering on it.");

		// Add module category


		// Add property descriptions (obligatory for every property!)
		// TODO: Write proper descriptions
		this.getPropertyDescriptions().put(PROPERTYKEY_ITERATIONS,
				"How often to iterate the inflation and deflation steps.");
		this.getPropertyDescriptions().put(PROPERTYKEY_L, "Amount of matrix multiplications per iteration, int >= 1.");
		this.getPropertyDescriptions().put(PROPERTYKEY_R, "Exponent in the inflation step, double >= 1.");
		this.getPropertyDescriptions().put(PROPERTYKEY_CSV_DELIMITER, "Delimiter of the input csv cells.");

		// Add property defaults (_should_ be provided for every property)
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Markov Clustering Module");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ITERATIONS, "1000");
		this.getPropertyDefaultValues().put(PROPERTYKEY_L, "2");
		this.getPropertyDefaultValues().put(PROPERTYKEY_R, "2.0");
		this.getPropertyDefaultValues().put(PROPERTYKEY_CSV_DELIMITER, ";");

		// Define I/O
		InputPort inputPort = new InputPort(ID_INPUT, "[text/csv] (Named Field) Matrix to cluster. NOTE: x and y dimensions of the matrix must agree.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(ID_OUTPUT, "[text/csv] Matrix clustered.", this);
		outputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {

		boolean result = true;
		PipedReader inputReader = null;

		try {
			inputReader = this.getInputPorts().get(ID_INPUT).getInputReader();

			// read the input into a NamedFieldMatrix
			final NamedFieldMatrix nfMatrix = NamedFieldMatrix.parseCSV(inputReader, this.csvDelimiter);

			// The JAMA matrix is initialised from the named field matrix'
			// values and will operate on them directly, avoiding some
			// duplication of memory or copying of values.
			// This is ok because the mcl algorithm changes field's values
			// but not their location, such that the mapping of array fields to
			// column and row names in the named field matrix stays intact.
			Matrix matrix = new Matrix(nfMatrix.getValues());

			// run the algorithm as many times as specified by the user
			for (int i = 0; i < this.iterations; i++) {
				matrix = mcl(matrix, this.l, this.r);
			}

			// Set the named fields matrix' values to the ones given by mcl
			// and write them to the output port
			nfMatrix.setValues(matrix.getArray());
			OutputPort out = this.getOutputPorts().get(ID_OUTPUT);
			nfMatrix.setDelimiter(this.csvDelimiter);
			out.outputToAllCharPipes(nfMatrix.csvHeader());
			for (int i = 0; i < nfMatrix.getRowAmount(); i++) {
				out.outputToAllCharPipes(nfMatrix.csvLine(i));
			}
		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			if (inputReader != null) {
				inputReader.close();
			}
			this.closeAllOutputs();
		}

		// Done
		return result;
	}

	// The main routine altering as well as returning the transformed matrix.
	private Matrix mcl(Matrix A, int l, double r) {
		A = gamma(A, r);
		A = power(A, l);

		return A;
	}

	// The inflation step of MCL boosts big matrix entries in contrast to small
	// ones, such that clusters can form
	public Matrix gamma(Matrix A, double r) {

		for (int i = 0; i < A.getRowDimension(); i++) {
			double denom = 0;

			for (int k = 0; k < A.getColumnDimension(); k++) {
				denom = denom + Math.pow(A.get(i, k), r);
			}

			if (denom != 0) {
				for (int j = 0; j < A.getColumnDimension(); j++) {
					A.set(i, j, Math.pow(A.get(i, j), r) / denom);
				}
			}
		}

		return A;
	}

	// alters and returns the input matrix by multiplying it l-1 times with
	// itself.
	private Matrix power(Matrix A, int l) {
		Matrix B = A.copy();

		for (int i = 1; i < l; i++) {
			A = A.times(B);
		}

		return A;
	}

	@Override
	public void applyProperties() throws Exception {
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties making sure something has been set.
		if (this.getProperties().getProperty(PROPERTYKEY_ITERATIONS) != null) {
			this.iterations = Integer.parseInt(this.getProperties().getProperty(PROPERTYKEY_ITERATIONS));
		}
		if (this.getProperties().getProperty(PROPERTYKEY_L) != null) {
			this.l = Integer.parseInt(this.getProperties().getProperty(PROPERTYKEY_L));
		}
		if (this.getProperties().getProperty(PROPERTYKEY_R) != null) {
			this.r = Double.parseDouble(this.getProperties().getProperty(PROPERTYKEY_R));
		}
		if (this.getProperties().getProperty(PROPERTYKEY_CSV_DELIMITER) != null) {
			this.csvDelimiter = this.getProperties().getProperty(PROPERTYKEY_CSV_DELIMITER);
		}

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
