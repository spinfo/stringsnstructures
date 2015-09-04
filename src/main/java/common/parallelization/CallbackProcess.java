package common.parallelization;

/**
 * A process that checks back with a receiver object after finishing its work.
 * @author Marcel Boeing
 *
 */
public interface CallbackProcess extends Runnable {

	public CallbackReceiver getCallbackReceiver();
	public void setCallbackReceiver(CallbackReceiver callbackReceiver);
	
}
