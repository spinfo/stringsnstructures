package common.parallelization;

public class ExampleCallbackProcess implements CallbackProcess {
	
	private CallbackReceiver callbackReceiver; // The object that our results go to
	private Object inputData; // Input data, optional but needed most of the time

	public ExampleCallbackProcess(CallbackReceiver callbackReceiver, Object inputData) {
		this.callbackReceiver = callbackReceiver;
		this.inputData = inputData;
	}

	@Override
	public void run() {
		
		/* It's very important to wrap everything in try/catch
		 * here -- otherwise our CallbackReceiver would not
		 * notice should we fail due to an exception.
		 */
		try {
			// Do whatever we need to
			String result = this.inputData.toString().replaceAll("[aeiu]", "o");
			// Return the result to our CallbackReceiver
			this.callbackReceiver.receiveCallback(Thread.currentThread(), result);
			
		} catch (Exception e){
			// If we catch an exception here, report it to the CallbackReceiver
			this.callbackReceiver.receiveException(Thread.currentThread(), e);
		}
	}

	@Override
	public CallbackReceiver getCallbackReceiver() {
		return this.callbackReceiver;
	}

	@Override
	public void setCallbackReceiver(CallbackReceiver callbackReceiver) {
		this.callbackReceiver = callbackReceiver;
	}

}
