package parallelization;

public class ExampleCallbackReceiver extends CallbackReceiverImpl {

	public static void main(String[] args) {
		ExampleCallbackReceiver receiver = new ExampleCallbackReceiver();
		receiver.start();
	}

	public void start(){
		
		// Instantiate two processes (change string to null if you want to see a process fail with en exception)
		ExampleCallbackProcess process1 = new ExampleCallbackProcess(this, "auf der mauer auf der lauer");
		ExampleCallbackProcess process2 = new ExampleCallbackProcess(this, "sitzt ne kleine wanze");
		
		// Define action to take upon callback from process 1
		Action process1SuccessAction = new Action(){
			@Override
			public void perform(Object processResult) {
				System.out.println("Process 1 success! "+processResult.toString());
			}
		};
		// Optional: Define action to take upon failure of process 1
		Action process1FailAction = new Action() {
			@Override
			public void perform(Object processResult) {
				// processResult should be of class Exception here
				System.out.println("Process 1 failure :-( " + processResult.toString());
			}
		};
		
		// Define action to take upon callback from process 2
		Action process2SuccessAction = new Action(){
			@Override
			public void perform(Object processResult) {
				System.out.println("Process 2 success! "+processResult.toString());
			}
		};
		
		// Register processes and actions withing the controlling CallbackReceiver (this object)
		this.registerSuccessCallback(process1, process1SuccessAction);
		this.registerFailureCallback(process1, process1FailAction);
		this.registerSuccessCallback(process2, process2SuccessAction);
		
		// Lastly, package processes into threads, start them and lean back
		Thread process1Thread = new Thread(process1);
		Thread process2Thread = new Thread(process2);
		process1Thread.start();
		process2Thread.start();
	}
	
}
