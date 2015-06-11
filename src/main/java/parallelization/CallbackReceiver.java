package parallelization;

import java.util.List;

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
	
	/**
	 * Adds another CallbackReceiver to relay received callbacks to.
	 * @param receiver Another CallbackReceiver
	 * @return True if CallbackReceiver was added successfully
	 */
	public boolean addCallbackReceiver(CallbackReceiver receiver);
	
	/**
	 * Removes a previously added additional CallbackReceiver.
	 * @param receiver CallbackReceiver to remove
	 * @return True if CallbackReceiver had been present and was removed
	 */
	public boolean removeCallbackReceiver(CallbackReceiver receiver);
	
	/**
	 * Removes all previously added additional CallbackReceivers.
	 */
	public void removeAllCallbackReceivers();
	
	/**
	 * Returns a list of previously added additional CallbackReceivers.
	 * @return List of CallbackReceivers
	 */
	public List<CallbackReceiver> getCallbackReceivers();
	
}
