package parallelization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Abstract implementation of CallbackReceiver interface.
 * @author Marcel Boeing
 *
 */
public abstract class CallbackReceiverImpl implements
		CallbackReceiver {
	
	// Maps containing the actions to perform on process callback
	private Map<CallbackProcess, Action> successActions = new HashMap<CallbackProcess, Action>();
	private Map<CallbackProcess, Action> failActions = new HashMap<CallbackProcess, Action>();
	private List<CallbackReceiver> externalCallbackReceiverList = new ArrayList<CallbackReceiver>();

	/**
	 * Registers an Action to perform on a successful process' callback
	 * @param process Process
	 * @param action Action
	 */
	protected void registerSuccessCallback(CallbackProcess process, Action action){
		this.successActions.put(process, action);
	}
	
	/**
	 * Registers an Action to perform on a failed process' callback
	 * @param process Process
	 * @param action Action
	 */
	protected void registerFailureCallback(CallbackProcess process, Action action){
		this.failActions.put(process, action);
	}
	
	@Override
	public void receiveCallback(Object processingResult, CallbackProcess process) {
		this.receiveCallback(processingResult, process, false);
	}

	@Override
	public void receiveCallback(Object processingResult,
			CallbackProcess process, boolean repeat) {
		
		// Determine whether a success action is present
		if (successActions.containsKey(process)) {
			
			// Remove or keep the success action
			Action action;
			if (repeat)
				action = successActions.get(process);
			else
				action = successActions.remove(process);
			
			// Perform the action
			action.perform(processingResult);
		}
		
		// Relay callback to additional CallbackReceivers if present
		Iterator<CallbackReceiver> externalCallbackReceivers = this.externalCallbackReceiverList.iterator();
		while (externalCallbackReceivers.hasNext()){
			externalCallbackReceivers.next().receiveCallback(processingResult, process, repeat);
		}
	}

	@Override
	public void receiveException(CallbackProcess process, Exception exception) {
		
		// Log the error message
		Logger.getLogger(this.getClass().getSimpleName()).warning(exception.toString());
		exception.printStackTrace();
		
		// If a success action is present, remove it
		if (successActions.containsKey(process))
			successActions.remove(process);
		
		// If a fail action is present, remove & perform it
		if (failActions.containsKey(process)){
			Action action = failActions.remove(process);
			action.perform(exception);
		}
		
		// Relay exception to additional CallbackReceivers if present
		Iterator<CallbackReceiver> externalCallbackReceivers = this.externalCallbackReceiverList.iterator();
		while (externalCallbackReceivers.hasNext()){
			externalCallbackReceivers.next().receiveException(process, exception);
		}
	}

	/* (non-Javadoc)
	 * @see parallelization.CallbackReceiver#addCallbackReceiver(parallelization.CallbackReceiver)
	 */
	@Override
	public boolean addCallbackReceiver(CallbackReceiver receiver) {
		return this.externalCallbackReceiverList.add(receiver);
	}

	/* (non-Javadoc)
	 * @see parallelization.CallbackReceiver#removeCallbackReceiver(parallelization.CallbackReceiver)
	 */
	@Override
	public boolean removeCallbackReceiver(CallbackReceiver receiver) {
		return this.externalCallbackReceiverList.remove(receiver);
	}

	/* (non-Javadoc)
	 * @see parallelization.CallbackReceiver#removeAllCallbackReceivers()
	 */
	@Override
	public void removeAllCallbackReceivers() {
		this.externalCallbackReceiverList.clear();
	}

	/* (non-Javadoc)
	 * @see parallelization.CallbackReceiver#getCallbackReceivers()
	 */
	@Override
	public List<CallbackReceiver> getCallbackReceivers() {
		return this.externalCallbackReceiverList;
	}
	
	

}
