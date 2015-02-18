package suffixTreeClustering.clustering.neighborjoin;

/**
 * Exception for non-additive distance/similarity matrices
 * 
 * @author Mandy Neumann
 */
class MatrixNotAdditiveException extends RuntimeException {
	transient private Double[][] matrix;

	/***/
	private static final long serialVersionUID = 8357404165430077570L;

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return String.format("Matrix %s is not additive!", this.matrix);
	}

	/**
	 * @param matrix
	 *            the matrix
	 */
	public MatrixNotAdditiveException(Double[][] matrix) {
		this.matrix = matrix;
	}
}
