package common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Provides vector calculation methods.
 * Feel free to add more ...
 *
 */
public class VectorCalculation {
	
	/**
	 * Calculates the Minkowski-Distance of two n-dimensional vectors.
	 * Also refer to MERKL, Rainer 2015, Bioinformatik, p.159
	 * and the <a href="https://en.wikipedia.org/wiki/Minkowski_distance">Wikipedia page</a> about the Minkowski-Distance.
	 * @param vectorA First vector
	 * @param vectorB Second vector
	 * @return Minkowski-Distance
	 * @throws Exception Thrown if vectors are null or of different length
	 */
	public static double calculateMinkowskiDistance(List<Double> vectorA, List<Double> vectorB) throws Exception{
		
		// Check input
		if (vectorA==null || vectorB==null || vectorA.size()!=vectorB.size()){
			throw new Exception("Sets must both be non-null and equal in length.");
		}
		
		// Prepare result variable
		double result = 0d;
		
		// Compute distance
		Iterator<Double> aIterator = vectorA.iterator();
		Iterator<Double> bIterator = vectorB.iterator();
		while(aIterator.hasNext() && bIterator.hasNext()){
			result += Math.pow(Math.abs(aIterator.next()-bIterator.next()),2d);
		}
		result = Math.sqrt(result);
		
		return result;
	}
}
