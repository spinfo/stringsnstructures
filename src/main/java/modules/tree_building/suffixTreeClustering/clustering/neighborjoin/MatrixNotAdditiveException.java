package modules.tree_building.suffixTreeClustering.clustering.neighborjoin;

/**
 * Exception for non-additive distance/similarity matrices
 * 
 * @author Mandy Neumann
 */
class MatrixNotAdditiveException extends RuntimeException {

	/***/
	private static final long serialVersionUID = 8357404165430077570L;

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return "Matrix is not additive!";
	}

}
