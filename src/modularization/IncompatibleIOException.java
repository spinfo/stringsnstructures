package modularization;

public class IncompatibleIOException extends Exception {

	/**
	 * Exception for handling errors due to incompatible module input/output classes.
	 * @author Marcel Boeing
	 */
	private static final long serialVersionUID = 6135187441193882189L;

	public IncompatibleIOException() {
		super();
	}

	public IncompatibleIOException(String message) {
		super(message);
	}

}
