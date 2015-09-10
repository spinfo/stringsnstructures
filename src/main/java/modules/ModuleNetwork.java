package modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.parallelization.Action;
import common.parallelization.CallbackReceiverImpl;

/**
 * Allows the construction of a tree of modules.
 * 
 * @author Marcel Boeing
 *
 */
public class ModuleNetwork extends CallbackReceiverImpl {

	// The list used to organize the modules
	private List<Module> moduleList;

	// List of started threads
	private Map<Module,Thread> startedThreads = new HashMap<Module,Thread>();

	/**
	 * Determines which pipe to use between both given module ports (prefers
	 * byte pipe).
	 * 
	 * @param outputProviderPort
	 *            Module port that provides the output
	 * @param inputReceiverPort
	 *            Module port that receives the input
	 * @return Compatible pipe
	 * @throws IOException
	 *             Thrown if an I/O error occurs
	 * @throws NotSupportedException
	 *             Thrown if the module ports' I/O is not compatible
	 */
	public static Pipe getCompatiblePipe(OutputPort outputProviderPort,
			InputPort inputReceiverPort) throws NotSupportedException, IOException {
		Pipe pipe = new BytePipe();
		if (!(inputReceiverPort.supportsPipe(pipe) && outputProviderPort
				.supportsPipe(pipe))) {
			pipe = new CharPipe();
			if (!(inputReceiverPort.supportsPipe(pipe) && outputProviderPort
					.supportsPipe(pipe))) {
				throw new NotSupportedException (
						"The I/O of those two module ports does not seem to be compatible.");
			}
		}
		return pipe;
	}

	/**
	 * Constructor
	 */
	public ModuleNetwork() {
		super();
		this.moduleList = new ArrayList<Module>();
	}

	/**
	 * @return Returns a map of the running module threads
	 */
	public Map<Module,Thread> getStartedThreads() {
		return startedThreads;
	}
	
	/**
	 * Adds a thread to the list of the ones started in a thread-safe manner.
	 * If there already is a thread associated to the specified module, it will be interrupted.
	 * @param module Module that the thread is associated to
	 * @param thread Thread to add
	 */
	public synchronized void addStartedThread(Module module, Thread thread){
		
		// Put thread in map and get the currently associated value
		Thread replacedThread = this.startedThreads.put(module, thread);
		
		// Check whether there already was a thread associated to the specified module and interrupt it if it's still running
		if (replacedThread != null && replacedThread.isAlive() && !replacedThread.isInterrupted())
			replacedThread.interrupt();
	}
	
	/**
	 * Removes a thread from the list of the ones started in a thread-safe manner.
	 * @param thread Thread to remove
	 * @return True if successful
	 */
	public synchronized boolean removeStartedThread(Thread thread){
		thread.interrupt();
		return (this.startedThreads.remove(thread) != null);
	}
	
	/**
	 * Removes dead threads from the list of the ones started in a thread-safe manner.
	 */
	public synchronized void removeDeadThreads(){
		Iterator<Thread> threads = this.startedThreads.values().iterator();
		while (threads.hasNext()) {
			Thread thread = threads.next();
			if (!thread.isAlive()) {
				Logger.getLogger(this.getClass().getSimpleName())
					.log(Level.INFO, "Thread " + thread.getName() + " is done.");
				threads.remove();
			}
		}
	}
	
	public synchronized void interruptAllThreads(){
		
		Iterator<Thread> threads = this.startedThreads.values().iterator();
		while (threads.hasNext()) {
			Thread thread = threads.next();
			if (thread.isAlive()) {
				Logger.getLogger(this.getClass().getSimpleName())
					.log(Level.INFO, "Interrupting thread #" + thread.getId() + " (" + thread.getName() + ").");
				thread.interrupt();
			}
		}
	}

	/**
	 * Adds a connection between two I/O ports using a compatible pipe.
	 * 
	 * @param outputPort
	 *            Outputport to connect
	 * @param inputPort
	 *            Inputport to connect
	 * @return True if successful
	 * @throws NotSupportedException
	 *             Thrown if the pipe is not compatible with both ports
	 * @throws OccupiedException
	 *             Thrown if the input port is occupied
	 * @throws IOException
	 *             Thrown if an I/O error occurs
	 */
	public boolean addConnection(OutputPort outputPort, InputPort inputPort)
			throws NotSupportedException, OccupiedException, IOException {

		// Determine pipe that connects both modules
		Pipe pipe = ModuleNetwork.getCompatiblePipe(outputPort, inputPort);

		// Jump to more detailed method
		return this.addConnection(outputPort, inputPort, pipe);
	}

	/**
	 * Adds a connection between two I/O ports.
	 * 
	 * @param outputPort
	 *            Outputport to connect
	 * @param inputPort
	 *            Inputport to connect
	 * @param connectingPipe
	 *            Pipe to connect the two ports
	 * @return True if successful
	 * @throws NotSupportedException
	 *             Thrown if the pipe is not compatible with both ports
	 * @throws OccupiedException
	 *             Thrown if the input port is occupied
	 */
	public boolean addConnection(OutputPort outputPort, InputPort inputPort, Pipe pipe)
			throws NotSupportedException, OccupiedException {

		// Make sure the I/O pipe is compatible to both ports
		if (!outputPort.supportsPipe(pipe)
				|| !inputPort.supportsPipe(pipe))
			throw new NotSupportedException(
					"This pipe cannot be used for I/O between those ports.");

		// Connect modules
		outputPort.addPipe(pipe, inputPort);
		inputPort.addPipe(pipe, outputPort);

		return true;
	}

	/**
	 * Removes the specified connection.
	 * 
	 * @param outputPort Output port
	 * @param inputPort Input port
	 * @return True if successful
	 * @throws NotFoundException
	 *             Thrown if there is no connecting pipe between the ports
	 */
	public boolean removeConnection(OutputPort outputPort, InputPort inputPort) throws NotFoundException {

		// Remove pipe from both output and input port
		outputPort.removePipe(inputPort.getPipe());
		inputPort.removePipe(inputPort.getPipe());

		// Exit with success
		return true;
	}

	/**
	 * Stops all running modules.
	 * 
	 * @throws SecurityException
	 */
	public void stopModules() throws SecurityException {

		// Check whether there are running threads and if not, write a
		// message into the log
		if (this.startedThreads.isEmpty())
			Logger.getLogger("")
					.log(Level.INFO,
							"Excuse me, but there are no running threads to interrupt.");

		// Interrupt running threads
		this.interruptAllThreads();
		
		// Remove dead threads
		this.removeDeadThreads();
		
	}

	/**
	 * Runs all modules. Note that this method does not wait for the threads to
	 * finish, so you should only call it from within another continuous thread
	 * or loop.
	 * 
	 * @throws Exception
	 */
	public void runModules() throws Exception {
		this.runModules(false);
	}

	/**
	 * Runs all modules.
	 * 
	 * @param runUntilAllThreadsAreDone
	 *            If true, the method blocks until all spawned threads have
	 *            finished
	 * @throws Exception
	 */
	public void runModules(boolean runUntilAllThreadsAreDone) throws Exception {
		this.runModules(runUntilAllThreadsAreDone, 5000l);
	}

	/**
	 * Runs all modules.
	 * 
	 * @param runUntilAllThreadsAreDone
	 *            If true, the method runs until all spawned threads have
	 *            finished
	 * @param interval
	 *            Interval to check for thread completion in milliseconds
	 * @throws Exception
	 */
	public void runModules(boolean runUntilAllThreadsAreDone, long interval)
			throws Exception {

		// Loop over all modules
		Iterator<Module> modules = this.moduleList.iterator();
		while (modules.hasNext()){
			// Run module
			this.runModules(modules.next());
		}
		
		// Determine runtime environment
		Runtime rt = Runtime.getRuntime();
		long maxBelegterHauptspeicher = 0l;

		// Wait for threads to finish, if requested
		while (runUntilAllThreadsAreDone && !this.startedThreads.isEmpty()) {
			try {
				// Sleep for a quarter second
				Thread.sleep(interval);

				// Print pretty overview
				Logger.getLogger(this.getClass().getSimpleName()).log(
						Level.INFO, this.prettyPrint());
				
				// Speicherverbrauch ausgeben
				
			    long belegterHauptspeicher = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
			    if (belegterHauptspeicher>maxBelegterHauptspeicher)
			    	maxBelegterHauptspeicher = belegterHauptspeicher;
			    Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Hauptspeicher belegt (MB):" + belegterHauptspeicher + "; bisheriges Max.:"+maxBelegterHauptspeicher);

				// Test which threads are still active and remove the rest from
				// the list
				this.removeDeadThreads();

			} catch (InterruptedException e) {
				break;
			}
		}
	}

	/**
	 * Runs the modules from a given tree node on.
	 * 
	 * @param parentNode
	 *            Root of tree branch from which to run the modules
	 * @throws Exception
	 *             Thrown if the method argument values are not right
	 */
	private void runModules(Module module) throws Exception {

		// Initialize thread
		final Thread moduleThread = new Thread(module);
		moduleThread.setName(module.getName());

		// Final list of started threads
		final ModuleNetwork moduleTreeInstance = this;

		// Define action to perform on success (note that this merely means the
		// module finished without throwing an exception -- not necessarily that
		// the module's own computation was successful)
		Action successAction = new Action() {
			@Override
			public void perform(Object processResult) {
				Boolean result = Boolean.parseBoolean(processResult.toString());
				if (result)
					Logger.getLogger("").log(
							Level.INFO,
							"Module " + module.getName()
									+ " has successfully finished processing.");
				else
					Logger.getLogger("")
							.log(Level.WARNING,
									"Module "
											+ module.getName()
											+ " did not finish processing successfully.");
				moduleTreeInstance.removeStartedThread(moduleThread);
			}
		};

		// Define action to perform on failure
		Action failureAction = new Action() {
			@Override
			public void perform(Object processResult) {
				// Since any exception already gets reported from within the
				// super class' receiveException() method, we only need to
				// remove the thread from our list.
				moduleTreeInstance.removeStartedThread(moduleThread);
			}
		};

		// Add module thread to list of the ones started
		this.addStartedThread(module, moduleThread);

		// Register callback actions
		this.registerSuccessCallback(moduleThread, successAction);
		this.registerFailureCallback(moduleThread, failureAction);

		// Log thread start message & fire it up 
		Logger.getLogger("").log(
				Level.INFO,
				"Starting to process module " + module.getName()
						+ " on thread #" + moduleThread.getId());
		moduleThread.start();

	}

	/**
	 * Resets the modules' I/O. Must be called prior re-running the module tree.
	 * 
	 * @throws Exception
	 */
	public void resetModuleIO() throws Exception {
		
		// Loop over all modules
		Iterator<Module> modules = this.moduleList.iterator();
		while (modules.hasNext()) {
			// Reset module
			modules.next().resetOutputs();
		}
	}
	
	/**
	 * Adds a new module.
	 * @param module
	 * @return True if successful
	 */
	public boolean addModule(Module module){
		return this.moduleList.add(module);
	}
	
	/**
	 * Removes a module.
	 * @param module
	 * @return True if successful
	 */
	public boolean removeModule(Module module){
		
		// Iterate over input ports
		Iterator<InputPort> inputPorts = module.getInputPorts().values().iterator();
		while (inputPorts.hasNext()){
			InputPort inputPort = inputPorts.next();
			Port connectedPort = inputPort.getConnectedPort();
			// If the input port is connected, remove the pipe from the other port
			if (connectedPort != null)
				try {
					connectedPort.removePipe(inputPort.getPipe());
				} catch (NotFoundException e) {
					e.printStackTrace();
				}
		}
		
		// Iterate over output ports
		Iterator<OutputPort> outputPorts = module.getOutputPorts().values().iterator();
		while (outputPorts.hasNext()){
			OutputPort outputPort = outputPorts.next();
			
			// Iterate over that output port's pipe lists
			Iterator<List<Pipe>> connectedPipeLists = outputPort.getPipes().values().iterator();
			while (connectedPipeLists.hasNext()){
				List<Pipe> connectedPipeList = connectedPipeLists.next();
				
				// Iterate over the pipe list's items
				Iterator<Pipe> connectedPipes = connectedPipeList.iterator();
				while (connectedPipes.hasNext()){
					Pipe connectedPipe = connectedPipes.next();
					Port connectedPort = outputPort.getConnectedPort(connectedPipe);
					
					// If that port is connected to another port, remove the pipe from it
					if (connectedPort != null)
						try {
							connectedPort.removePipe(connectedPipe);
						} catch (NotFoundException e) {
							e.printStackTrace();
						}
				}
			}
		}
		
		// Remove module
		return this.moduleList.remove(module);
	}
	
	/**
	 * Removes all modules.
	 * @return True if successful
	 */
	public boolean removeAllModules(){
		// Loop over all modules
		Iterator<Module> modules = this.moduleList.iterator();
		boolean allRemoved = true;
		while (modules.hasNext()) {
			if (!this.removeModule(modules.next()))
				allRemoved = false;
		}
		return allRemoved;
	}

	/**
	 * Returns a pretty representation of the modules' status.
	 * @return String
	 */
	private String prettyPrint(){

		StringBuffer result = new StringBuffer("Module Status\n-------------\n");
		
		// Loop over all modules
		Iterator<Module> modules = this.moduleList.iterator();
		while (modules.hasNext()) {
			// Print module status
			Module module = modules.next();
			result.append(module.getName()+":\t"+module.getStatus()+"\n");
		}

		return result.toString();
	}

	/**
	 * @return the moduleList
	 */
	protected List<Module> getModuleList() {
		return moduleList;
	}

}
