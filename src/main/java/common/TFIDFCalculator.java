package common;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TFIDFCalculator {

	private Map<String, Double> totalTermFrequencyPerRow;
	private Map<String, Set<String>> totalDocumentSetPerColumn;

	public TFIDFCalculator() {
		totalTermFrequencyPerRow = new ConcurrentHashMap<>();
		totalDocumentSetPerColumn = new ConcurrentHashMap<>();
	}

	public void addSentenceNeighboursToBase(Map<String, Double> neighbours, String token) {
		neighbours.forEach((word, freq) -> {
			addToDocumentFrequencyBase(freq, token);
			addToTermFrequencyBase(word, token);
		});
	}

	private void addToDocumentFrequencyBase(Double wordFrequency, String token) {
		Double termFrequency = totalTermFrequencyPerRow.getOrDefault(token, 0d);
		termFrequency += wordFrequency;
		totalTermFrequencyPerRow.put(token, termFrequency);
	}

	private void addToTermFrequencyBase(String neighbourWord, String token) {
		Set<String> documentSetPerColumn = totalDocumentSetPerColumn.getOrDefault(token, new HashSet<>());
		documentSetPerColumn.add(neighbourWord);
		totalDocumentSetPerColumn.put(token, documentSetPerColumn);
	}

	public void calculateTfidf(Map<String, ConcurrentHashMap<String, Double>> resultMatrix) {
		int totalDocs = resultMatrix.size();

		resultMatrix.forEach((rowTerm, rowValues) -> {
			rowValues.forEach((cellTerm, cellValue) -> {
				double tf = cellValue / totalTermFrequencyPerRow.get(rowTerm);
				double df = (double)totalDocs / (double)totalDocumentSetPerColumn.get(cellTerm).size();
				double idf = Math.log(df);
				rowValues.put(cellTerm, tf * idf);
			});
		});
	}
}
