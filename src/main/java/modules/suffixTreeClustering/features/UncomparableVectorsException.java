package modules.suffixTreeClustering.features;

/**
 * Exception for unexpected vector comparison.
 * @author Fabian Steeg (fsteeg)
 */
class UncomparableVectorsException extends RuntimeException {
    transient private FeatureVector v1;
    transient private FeatureVector v2;

    /***/
    private static final long serialVersionUID = 8357404165430077570L;

    /**
     * {@inheritDoc}
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        return String.format("Can't compare %s and %s", v1, v2);
    }

    /**
     * @param v1 The first vector
     * @param v2 The second vector
     */
    public UncomparableVectorsException(FeatureVector v1, FeatureVector v2) {
        this.v1 = v1;
        this.v2 = v2;
    }
}
