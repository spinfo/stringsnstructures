package modules.tree_building.suffixTreeClustering.features;

import java.util.Arrays;
import java.io.Serializable;

/**
 * Class representing a Document Vector. A vector is initialized with vector
 * values (e.g. tf-idf weights)
 * 
 * @author neumannm
 */
public final class FeatureVector implements Serializable {

	private static final long serialVersionUID = 4735812009246952588L;
	private Double[] features;

	@Override
	public String toString() {
		return String.format("%s with %s values", getClass().getSimpleName(),
				features.length);
	}

	/**
	 * Initialize Feature Vector.
	 * 
	 * @param values Values
	 */
	public FeatureVector(final Double[] values) {
		this.features = values;
	}

	public Double[] getValues() {
		return features;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof FeatureVector)) {
			return false;
		}
		FeatureVector that = (FeatureVector) obj;
		return Arrays.equals(this.features, that.features);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(features);
	}

	/**
	 * Calculates and returns Euclidian Distance from this vector to another.
	 * 
	 * @param other
	 *            - the other feature vector.
	 * @return float value of Euclidian Distance
	 */
	public Double distance(FeatureVector other) {
		/*
		 * Bevor wir mit den Berechnung beginnen, prüfen wir ob das überhaupt
		 * funktionieren kann (sowas erleichtert die Fehlersuche): die zu
		 * vergleichenden Vektoren müssen gleich lang sein, sonst stimmt
		 * irgendwas überhaupt nicht:
		 */
		if (this.features.length != other.features.length) {
			throw new UncomparableVectorsException(this, other);
		}

		/*
		 * Berechnung euklidischer Distanz (wobei 0 "identisch" bedeutet)
		 */
		double dist = (float) Math.sqrt(sumOfSquares(other));

		if (new Double(dist).isNaN()) {
			throw new IllegalStateException(
					"Distance computed by sum of squares of the two vectors is not a number");
		}
		return dist;
	}

	/*
	 * Calculates sum of squares (for Euclidian Distance).
	 */
	private double sumOfSquares(FeatureVector other) {
		double sum = 0;
		for (int i = 0; i < this.features.length; i++) {
			double difference = this.features[i] - other.features[i];
			double square = Math.pow(difference, 2);
			sum += square;
		}
		return sum;
	}

	/**
	 * Calculates and returns Cosine-similarity between this vector and another.
	 * 
	 * @param other
	 *            - the other feature vector.
	 * @return Double value of Cosine-similarity
	 */
	public Double similarity(FeatureVector other) {
		/*
		 * Bevor wir mit den Berechnung beginnen, prüfen wir ob das überhaupt
		 * funktionieren kann (sowas erleichtert die Fehlersuche): die zu
		 * vergleichenden Vektoren müssen gleich lang sein, sonst stimmt
		 * irgendwas überhaupt nicht:
		 */
		if (this.features.length != other.features.length) {
			throw new UncomparableVectorsException(this, other);
		}
		double dotProduct = dot(other);
		double euclidianLengthProduct = euc(other);
		/*
		 * Da die Winkel zwischen Vektoren in einem rein positiven
		 * Koordinatensystem maximal 90 Grad betragen, ist die
		 * Kosinusähnlichkeit immer ein Wert zwischen 0 und 1 und so ein
		 * brauchbares Maß zur Bestimmung der Ähnlichkeit (wobei 1 "identisch"
		 * und 0 "keine Ähnlichkeit" bedeutet)
		 */
		double dist = dotProduct == 0 || euclidianLengthProduct == 0 ? 0
				: dotProduct / euclidianLengthProduct;
		/*
		 * Obiges behaupten und vermuten wir, aber sowas hier und da zu
		 * überprüfen macht die Fehlersuche einfacher und erhöht das Vertrauen
		 * in die Korrektheit des Codes (wie auch oben für die
		 * Eingangsbedingung):
		 */
		if (dist < -0.0001f || dist > 1.0001f) {
			String message = "Cosine similarity must be between 0 and 1, but is: "
					+ dist;
			throw new IllegalStateException(message);
		}
		if (new Double(dist).isNaN()) {
			throw new IllegalStateException(
					String.format(
							"Distance computed by devision of dot product %s and euclidian distance %s is not a number",
							dotProduct, euclidianLengthProduct));
		}
		return dist;
	}

	private double euc(FeatureVector query) {

		double sum1 = 0;
		double sum2 = 0;
		/*
		 * Euklidische Länge: Wurzel aus der Summe der quadrierten Elemente
		 * eines der Vektoren:
		 */
		for (Double f : features) {
			sum1 += Math.pow(f, 2);
		}
		for (Double f : query.features) {
			sum2 += Math.pow(f, 2);
		}
		/*
		 * Wir wollen das Produkt der euklidischen Längen der zwei Vektoren
		 * (|V(d1)| |V(d2)|)
		 */
		return (double) (Math.sqrt(sum1) * Math.sqrt(sum2));
	}

	private double dot(FeatureVector query) {
		/*
		 * Das dot Produkt ist die Summe der Produkte der korrespondierenden
		 * Vektor-Werte:
		 */
		float sum = 0;
		for (int i = 0; i < features.length; i++) {
			sum += (features[i] * query.features[i]);
		}
		return sum;
	}

	public Integer getLength() {
		return this.features.length;
	}
}
