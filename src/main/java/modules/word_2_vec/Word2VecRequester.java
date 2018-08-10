package modules.word_2_vec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.deeplearning4j.models.word2vec.Word2Vec;

import base.workbench.ModuleRunner;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

/**
 * @author Peter
 *
 */
public class Word2VecRequester extends ModuleImpl {

	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(Word2VecRequester.class, args);
	}

	// Define property keys (every setting has to have a unique key to associate
	// it with)
	public static final String PROPERTYKEY_RESULT_LENGTH = "number of results to be shwon.";
	public static final String PROPERTYKEY_WORD2VEC_MODEL_PATH = "Path to Word2Vec model.";
	public static final String PROPERTYKEY_QUERIES_FILE_PATH = "Path to the query list.";

	// Define I/O IDs (must be unique for every input or output)
	private final String outputId = "request result";
	private final String INPUTID = "inputID";
	private String inputModelPath = "";

	// Local variables
	private Word2Vec vec;
	private int resultListLength;
	private String queriesFilePath;

	public Word2VecRequester(CallbackReceiver callbackReceiver, Properties properties) throws Exception {

		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription(
				"Requests Vectors of Words and shows words close to the given query words in the w2v model.");
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Word2VecRequester");
		this.getPropertyDefaultValues().put(PROPERTYKEY_WORD2VEC_MODEL_PATH, "");

		this.getPropertyDescriptions().put(PROPERTYKEY_RESULT_LENGTH, "Length of result list.");
		this.getPropertyDescriptions().put(PROPERTYKEY_WORD2VEC_MODEL_PATH,
				"Path to word2vecModel (can also be the output of Word2vecGeneratorModule).");
		this.getPropertyDescriptions().put(PROPERTYKEY_QUERIES_FILE_PATH,
				"Path to the file that contains the words used as queries");

		// Define I/O
		/*
		 * I/O is structured into separate ports (input~/output~). Every port
		 * can support a range of pipe types (currently byte or character
		 * pipes). Output ports can provide data to multiple pipe instances at
		 * once, input ports can in contrast only obtain data from one pipe
		 * instance.
		 */
		InputPort inputPort = new InputPort(INPUTID, "Path to word2vec model", this);
		inputPort.addSupportedPipe(CharPipe.class);

		OutputPort outputPort = new OutputPort(outputId, "Request results", this);
		outputPort.addSupportedPipe(CharPipe.class);
		// Add I/O ports to instance (don't forget...)

		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

	}

	@Override
	public boolean process() throws Exception {
		boolean success = true;
		if (this.getInputPorts().get(INPUTID).isConnected() && !this.inputModelPath.isEmpty()) {
			throw new Exception("dont use the model path field and the pipe at the same time");
		} else {
			if ((this.getInputPorts() != null && this.getInputPorts().get(INPUTID) != null
					&& this.getInputPorts().get(INPUTID).isConnected()) && this.inputModelPath.isEmpty()) {
				BufferedReader reader = new BufferedReader(this.getInputPorts().get(INPUTID).getInputReader());
				this.inputModelPath = reader.readLine();
			}
		}

		Word2VecModelWrapper w2vmw = new Word2VecModelWrapper(this.inputModelPath);
		this.vec = w2vmw.getModel();

		try {
			this.getOutputPorts().get(outputId).outputToAllCharPipes(requestList(this.queriesFilePath));
		} catch (Exception exception) {
			success = false;
			throw exception;
		} finally {
			this.closeAllOutputs();
		}
		return success;
	}

	/**
	 * 
	 * @param queriesFilePath
	 * @return all results for all queries
	 * @throws FileNotFoundException
	 */
	public String requestList(String queriesFilePath) throws FileNotFoundException {
		StringBuilder sb = new StringBuilder();
		Scanner scan = new Scanner(new File(queriesFilePath));
		String currentString = "";
		while (scan.hasNextLine()) {
			currentString = scan.nextLine();
			sb.append(parseRequest(currentString));
		}
		scan.close();
		return sb.toString();
	}

	private String parseRequest(String query) {
		StringBuilder sb = new StringBuilder();
		if (query.contains(" ")) {
			List<String> positiveList = new ArrayList<>();
			List<String> negativeList = new ArrayList<>();
			for (String queryPart : query.split(" ")) {
				if (queryPart.startsWith("-")) {
					String negativeQuery = queryPart.replaceFirst("-", "");
					if (vec.getVocab().containsWord(negativeQuery))
						negativeList.add(negativeQuery);
				} else {
					String positiveQuery = queryPart.replaceFirst("\\+", "");
					if (vec.getVocab().containsWord(positiveQuery))
						positiveList.add(positiveQuery);
				}
			}
			if (negativeList.size() != 0 && positiveList.size() > 1) {
				sb.append("negatives: " + negativeList + " || ");
				sb.append("positives: " + positiveList + " = ");
			} else {
				sb.append(query + " : ");
			}
			sb.append(print(vec.wordsNearest(positiveList, negativeList, resultListLength)) + "\n");
		} else {
			if (vec.getVocab().containsWord(query)) {
				sb.append("query: " + query + " " + vec.getWordVectorMatrix(query) + "\n");
				sb.append(print(vec.wordsNearest(query, resultListLength)) + "\n\n");
			} else {
				sb.append(query + " is not contained\n");
			}
		}
		return sb.toString();
	}

	/**
	 * helper function to print results
	 * 
	 * @param list
	 * @return results for one query word
	 */
	private String print(Collection<String> list) {
		String out = "";
		for (String s : list) {
			out += "\n" + s + ": [";
			for (double d : vec.getWordVector(s)) {
				out += " " + d + " ";
			}
			out += "]";
		}
		return out;
	}

	@Override
	public void applyProperties() throws Exception {
		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// Apply own properties
		this.resultListLength = Integer
				.parseInt((String) this.getProperties().getOrDefault(PROPERTYKEY_RESULT_LENGTH, "10"));
		this.queriesFilePath = this.getProperties().getProperty(PROPERTYKEY_QUERIES_FILE_PATH);
		this.inputModelPath = this.getProperties().getProperty(PROPERTYKEY_WORD2VEC_MODEL_PATH);

		super.applyProperties();
	}

}
