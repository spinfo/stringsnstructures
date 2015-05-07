package parallelization;

import java.util.HashMap;
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
	}

}
