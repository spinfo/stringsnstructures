package parallelization;

/**
 * A process that keeps track of running processes and stores actions to perform on callback.
 * @author Marcel Boeing
 *
 */
public interface CallbackReceiver {

	/**
	 * Accepts a processing result and performs the associated action.
	 * @param processingResult Result of the process
	 * @param process The process calling back
	 */
	public void receiveCallback(Object processingResult, CallbackProcess process);
	
	/**
	 * Accepts a processing result and performs the associated action.
	 * @param processingResult Result of the process
	 * @param process The process calling back
	 * @param repeat If true, the associated action will not be removed (and can be used for another callback).
	 */
	public void receiveCallback(Object processingResult, CallbackProcess process, boolean repeat);
	
	/**
	 * Performs the action associated with a process failure.
	 * @param process The process calling back
	 * @param exception The exception that occurred
	 */
	public void receiveException(CallbackProcess process, Exception exception);
	
}
