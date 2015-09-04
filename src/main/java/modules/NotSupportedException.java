package modules;


public class NotSupportedException extends Exception {

	private static final long serialVersionUID = 8269766587407679587L;

	public NotSupportedException() {
	}

	public NotSupportedException(String message) {
		super(message);
	}

	public NotSupportedException(Throwable cause) {
		super(cause);
	}

	public NotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

}
