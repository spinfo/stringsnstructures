package suffixTreeClustering.clustering.neighborjoin;

/**
 * Exception for unsymmetric distance/similarity matrices
 * 
 * @author Mandy Neumann
 */
class MatrixUnsymmetricException extends RuntimeException {
	transient private Float[][] matrix;

	/***/
	private static final long serialVersionUID = 8357404165430077570L;

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return String.format("Matrix %s is not symmetric!", this.matrix);
	}

	/**
	 * @param distanceMatrix
	 *            the matrix
	 */
	public MatrixUnsymmetricException(Float[][] distanceMatrix) {
		this.matrix = distanceMatrix;
	}
}
