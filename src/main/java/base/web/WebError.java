package base.web;

class WebError {

	protected static class InvalidWorkflowDefiniton extends Exception {
		private static final long serialVersionUID = 1246852381687652883L;

		protected InvalidWorkflowDefiniton(Throwable cause) {
			super(cause);
		}
	}

	protected static class InvalidJobDefinition extends Exception {
		private static final long serialVersionUID = -6641311071071946210L;

		protected InvalidJobDefinition(String message) {
			super(message);
		}
	}

	protected static class ResourceNotFoundException extends Exception {
		private static final long serialVersionUID = -4910174999543201654L;

		protected ResourceNotFoundException(String message) {
			super(message);
		}
	}

	protected static class InvalidInputException extends Exception {
		private static final long serialVersionUID = -6582645374425561934L;

		protected InvalidInputException(String message) {
			super(message);
		}
	}

}
