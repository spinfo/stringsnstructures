package modules.word_2_vec;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

import org.deeplearning4j.models.embeddings.learning.impl.elements.CBOW;
import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

/**
 * @author Peter
 *
 *         Wrapper to generate, serialize and read models
 */

public class Word2VecModelWrapper {

	String modelName;
	boolean useCBOW;
	int minWordFrequency;
	int dimensions;
	int windowSize;
	int numberOfIterations;
	String inputFilePath;
	Logger logger = Logger.getGlobal();

	static Word2Vec vec = null;

	public Word2VecModelWrapper(String modelName, boolean useCBOW, int dimensions, int minWordFrequency, int windowSize,
			int numberOfIterations, String inputFilePath) {
		this.modelName = modelName;
		if (modelName != null && new File(this.modelName).exists()) {
			logger.warning("nothing is calculated because model already exists. Loading this model instead.");
			vec = WordVectorSerializer.readWord2VecModel(modelName);
		} else {
			this.useCBOW = useCBOW;
			this.dimensions = dimensions;
			this.minWordFrequency = minWordFrequency;
			this.windowSize = windowSize;
			this.numberOfIterations = numberOfIterations;
			this.inputFilePath = inputFilePath;
			vec = generateModel();
		}
	}

	public Word2VecModelWrapper(String modelPath) {
		if (modelPath != null && new File(modelPath).exists()) {
			modelName = modelPath;
			logger.info("loading model");
			vec = WordVectorSerializer.readWord2VecModel(modelName);
		}
	}

	public Word2Vec getModel() {
		return vec;
	}

	public Word2Vec generateModel() {
		long startTime = System.currentTimeMillis();
		logger.info("starting generating model");
		try {
			// does this have to be adjustable? What other Iterators are there?
			SentenceIterator iter = new BasicLineIterator(inputFilePath);
			// What tokenizers exist?
			TokenizerFactory t = new DefaultTokenizerFactory();
			t.setTokenPreProcessor(new CommonPreprocessor());

			vec = new Word2Vec.Builder().minWordFrequency(minWordFrequency).iterations(numberOfIterations)
					.layerSize(dimensions).seed(42).windowSize(windowSize).iterate(iter).tokenizerFactory(t).build();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (useCBOW) {
			vec.setElementsLearningAlgorithm(new CBOW<VocabWord>());
		} else {
			vec.setElementsLearningAlgorithm(new SkipGram<VocabWord>());
		}
		vec.fit();
		logger.info("Training took " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
//		WordVectorSerializer.writeWord2VecModel(vec, this.modelName);
		return vec;
	}

	public void serializeWord2VecModel() {
		WordVectorSerializer.writeWord2VecModel(vec, modelName);
	}

}
