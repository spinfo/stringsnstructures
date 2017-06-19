package modules.matrix.distanceModule;

public class VectorMath {
	
	public static double euclidianDistance(double[] ds, double[] ds2) {
        double diff_square_sum = 0.0;
        for (int i = 0; i < ds.length; i++) {
            diff_square_sum += (ds[i] - ds2[i]) * (ds[i] - ds2[i]);
        }
        return Math.sqrt(diff_square_sum);
    }
	
	public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
	    double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    if(vectorA.equals(vectorB)){
	    	return 0.0;
	    }
	    for (int i = 0; i < vectorA.length; i++) {
	        dotProduct += vectorA[i] * vectorB[i];
	        normA += Math.pow(vectorA[i], 2);
	        normB += Math.pow(vectorB[i], 2);
	    }   
	    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}
	
	public static double cosineDistance(double[] vectorA, double[] vectorB){
		return 1 - cosineSimilarity(vectorA, vectorB);
	}
}
