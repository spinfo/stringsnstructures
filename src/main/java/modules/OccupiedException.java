package modules;

public class OccupiedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9143693709438837439L;

	public OccupiedException() {
	}

	public OccupiedException(String message) {
		super(message);
	}

	public OccupiedException(Throwable cause) {
		super(cause);
	}

	public OccupiedException(String message, Throwable cause) {
		super(message, cause);
	}

	public OccupiedException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
