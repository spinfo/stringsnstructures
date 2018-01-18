package base.web;

class WebError {

	protected static class InvalidWorkflowDefiniton extends Exception {
		private static final long serialVersionUID = 1246852381687652883L;

		public InvalidWorkflowDefiniton(Throwable cause) {
			super(cause);
		}
	}

	protected static class InvalidJobDefinition extends Exception {
		private static final long serialVersionUID = -6641311071071946210L;

		public InvalidJobDefinition(String message) {
			super(message);
		}
	}

}
