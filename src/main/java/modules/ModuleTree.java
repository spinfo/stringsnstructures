package modules;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import common.parallelization.Action;
import common.parallelization.CallbackReceiverImpl;

/**
 * Allows the construction of a tree of modules.
 * 
 * @author Marcel Boeing
 *
 */
public class ModuleTree extends CallbackReceiverImpl {

	// The treemodel used to organize the modules
	private DefaultTreeModel moduleTree;

	// List of started threads
	private List<Thread> startedThreads = new ArrayList<Thread>();

	/**
	 * Determines which pipe to use between both given module ports (prefers byte
	 * pipe).
	 * 
	 * @param outputProviderPort
	 *            Module port that provides the output
	 * @param inputReceiverPort
	 *            Module port that receives the input
	 * @return Compatible pipe
	 * @throws Exception
	 *             Thrown if the module ports' I/O is not compatible
	 */
	public static Pipe getCompatiblePipe(OutputPort outputProviderPort,
			InputPort inputReceiverPort) throws Exception {
		Pipe pipe = new BytePipe();
		if (!(inputReceiverPort.supportsPipe(pipe) && outputProviderPort
				.supportsPipe(pipe))) {
			pipe = new CharPipe();
			if (!(inputReceiverPort.supportsPipe(pipe) && outputProviderPort
					.supportsPipe(pipe))) {
				throw new Exception(
						"The I/O of those two module ports does not seem to be compatible.");
			}
		}
		return pipe;
	}

	/**
	 * When using the constructor without parameters you need to set the root
	 * module manually before adding any other modules!
	 */
	public ModuleTree() {
		super();
	}

	/**
	 * Preferred constructor
	 * 
	 * @param module
	 */
	public ModuleTree(Module module) {
		super();
		this.setRootModule(module);
	}

	/**
	 * @return Returns the module tree model
	 */
	public DefaultTreeModel getModuleTreeModel() {
		return moduleTree;
	}

	/**
	 * @param moduleTree
	 *            the moduleTree to set
	 */
	public void setModuleTree(DefaultTreeModel moduleTree) {

		// Update instance variable
		this.moduleTree = moduleTree;

		// Determine root module and set its callback receiver
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) this.moduleTree
				.getRoot();
		Module module = (Module) rootNode.getUserObject();
		module.setCallbackReceiver(this);

		// Do the same with all other modules within the tree
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> childNodes = rootNode
				.breadthFirstEnumeration();
		while (childNodes.hasMoreElements()) {
			module = (Module) childNodes.nextElement().getUserObject();
			module.setCallbackReceiver(this);
		}
	}

	/**
	 * @return Returns a list of running threads
	 */
	public List<Thread> getStartedThreads() {
		return startedThreads;
	}
	
	/**
	 * Adds a thread to the list of the ones started in a thread-safe manner.
	 * @param thread Thread to add
	 * @return true if successful
	 */
	public synchronized boolean addStartedThread(Thread thread){
		return this.startedThreads.add(thread);
	}
	
	/**
	 * Removes a thread from the list of the ones started in a thread-safe manner.
	 * @param thread Thread to remove
	 * @return true if successful
	 */
	public synchronized boolean removeStartedThread(Thread thread){
		return this.startedThreads.remove(thread);
	}
	
	/**
	 * Removes dead threads from the list of the ones started in a thread-safe manner.
	 */
	public synchronized void removeDeadThreads(){
		Iterator<Thread> threads = this.startedThreads.iterator();
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
		
		Iterator<Thread> threads = this.startedThreads.iterator();
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
	 * Sets the tree's root module
	 * 
	 * @param module
	 *            Module to set as root
	 */
	public void setRootModule(Module module) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(module);
		if (this.moduleTree != null)
			this.moduleTree.setRoot(rootNode);
		else {
			this.moduleTree = new DefaultTreeModel(rootNode);
			module.setCallbackReceiver(this);
		}
	}

	/**
	 * Returns the module tree's root node.
	 * 
	 * @return root node
	 */
	public DefaultMutableTreeNode getRootNode() throws ClassCastException {
		return (DefaultMutableTreeNode) this.moduleTree.getRoot();
	}

	/**
	 * Returns the module tree's root module.
	 * 
	 * @return root module
	 */
	public Module getRootModule() {
		try {
			return (Module) this.getRootNode().getUserObject();

		} catch (Exception e) {
			Logger.getLogger("").log(Level.WARNING,
					"Failed to determine the module tree's root module.", e);
			return null;
		}
	}

	/**
	 * Returns the tree node that holds the given module (or null if it is not
	 * found)
	 * 
	 * @param module
	 *            Module to search for
	 * @return Tree node that holds the module or null
	 */
	public DefaultMutableTreeNode locateModuleInTree(Module module) {
		// Determine the tree's root node
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) this.moduleTree
				.getRoot();

		return this.locateModuleInTree(module, rootNode);
	}

	/**
	 * Returns the tree node that holds the given module (or null if it is not
	 * found)
	 * 
	 * @param module
	 *            Module to search for
	 * @param parentNode
	 *            Tree node to start the search from
	 * @return Tree node that holds the module or null
	 */
	private DefaultMutableTreeNode locateModuleInTree(Module module,
			DefaultMutableTreeNode parentNode) {
		if (parentNode.getUserObject() != null
				&& parentNode.getUserObject().equals(module))
			return parentNode;

		// Recursively run this method for the tree node's children
		Enumeration<?> childNodes = parentNode.children();
		while (childNodes.hasMoreElements()) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes
					.nextElement();
			DefaultMutableTreeNode foundNode = this.locateModuleInTree(module,
					childNode);
			if (foundNode != null)
				return foundNode;
		}

		return null;
	}

	/**
	 * Appends a module to the given tree node. Tries to guess the best pipe and
	 * connects the modules.
	 * 
	 * @param newModule
	 *            Module to add
	 * @param parentModule
	 *            Module the new module should be a child of
	 * @return True if successful
	 * @throws NotSupportedException
	 *             Thrown if the pipe is not compatible with both the new and
	 *             the parent module
	 * @throws Exception
	 *             Thrown if the method argument values are not right
	 */
	public boolean addModule(Module newModule, Module parentModule)
			throws NotSupportedException, Exception {

		// Determine pipe that connects both modules
		Pipe pipe = ModuleTree.getCompatiblePipe(parentModule, newModule);

		// Jump to more detailed method
		return this.addModule(newModule, parentModule, pipe);
	}

	/**
	 * Appends a module to the given tree node
	 * 
	 * @param newModule
	 *            Module to add
	 * @param parentModule
	 *            Module the new module should be a child of
	 * @param connectingPipe
	 *            Pipe to connect the new module to its parent
	 * @return True if successful
	 * @throws NotSupportedException
	 *             Thrown if the pipe is not compatible with both the new and
	 *             the parent module
	 * @throws Exception
	 *             Thrown if the method argument values are not right
	 */
	public boolean addModule(Module newModule, Module parentModule, Pipe pipe)
			throws NotSupportedException, Exception {
		// Determine the location of the parent module within the tree
		DefaultMutableTreeNode parentModuleNode = this
				.locateModuleInTree(parentModule);

		// If the parent module isn't found, we're done
		if (parentModuleNode == null)
			return false;

		// Jump to more detailed method
		return this.addModule(newModule, parentModuleNode, pipe);
	}

	/**
	 * Appends a module to the given tree node
	 * 
	 * @param newModule
	 *            Module to add
	 * @param parentNode
	 *            Node the new module should be a child of
	 * @param connectingPipe
	 *            Pipe to connect the new module to its parent
	 * @return True if successful
	 * @throws NotSupportedException
	 *             Thrown if the pipe is not compatible with both the new and
	 *             the parent module
	 * @throws Exception
	 *             Thrown if the method argument values are not right
	 */
	public boolean addModule(Module newModule,
			DefaultMutableTreeNode parentNode, Pipe connectingPipe)
			throws NotSupportedException, Exception {

		// Check whether the parent node holds a module as expected (throw an
		// exception otherwise)
		if (parentNode.getUserObject() == null
				|| !Module.class.isAssignableFrom(parentNode.getUserObject()
						.getClass()))
			throw new Exception(
					"Excuse me, but this tree node does not hold a module -- I am afraid I cannot continue the operation.");

		// Determine parent module
		Module parentModule = (Module) parentNode.getUserObject();

		// Make sure the I/O pipe is compatible to both modules
		if (!newModule.supportsInputPipe(connectingPipe)
				|| !parentModule.supportsOutputPipe(connectingPipe))
			throw new NotSupportedException(
					"Terribly sorry, but this pipe cannot be used for I/O between those modules.");

		// Connect modules
		newModule.setInputPipe(connectingPipe);
		parentModule.addOutputPipe(connectingPipe);

		// Create new tree node
		DefaultMutableTreeNode newModuleNode = new DefaultMutableTreeNode(
				newModule);

		// Insert new tree node
		this.moduleTree.insertNodeInto(newModuleNode, parentNode,
				parentNode.getChildCount());

		// Set module's callback receiver
		newModule.setCallbackReceiver(this);

		return true;
	}

	/**
	 * Removes the specified node and all its children from the tree. The root
	 * node cannot be removed.
	 * 
	 * @param node
	 * @return True if successful
	 * @throws Exception
	 *             Thrown if you try to remove the root node
	 */
	public boolean removeModule(DefaultMutableTreeNode node) throws Exception {

		// Throw exception if the root node is selected
		if (node.equals(this.moduleTree.getRoot()))
			throw new Exception(
					"The root node cannot be removed -- please do start a new tree instead.");

		// Determine the node's module
		Module module = (Module) node.getUserObject();

		// Determine parent node and ~module
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
				.getParent();
		Module parentModule = (Module) parent.getUserObject();

		// Determine correct input pipe
		Pipe inputPipe;
		if (module.getInputBytePipe() != null)
			inputPipe = module.getInputBytePipe();
		else
			inputPipe = module.getInputCharPipe();

		// Exit with failure if no input pipe is present
		if (inputPipe == null)
			return false;

		// Delete pipe from parent
		parentModule.removeOutputPipe(inputPipe);

		// Delete node
		this.moduleTree.removeNodeFromParent(node);

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
	 * Runs all modules the module tree contains.
	 * 
	 * @throws Exception
	 */
	public void runModules() throws Exception {
		this.runModules(false);
	}

	/**
	 * Runs all modules the module tree contains.
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
	 * Runs all modules the module tree contains.
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

		// Determine the tree's root node
		DefaultMutableTreeNode rootNode = this.getRootNode();

		// If that node's module has already been run, we will have to reset all
		// modules' I/O
		if (((Module) rootNode.getUserObject()).getStatus() != Module.STATUSCODE_NOTYETRUN)
			this.resetModuleIO();

		// Run modules
		this.runModules(rootNode);
		
		// Laufzeitumgebung ermitteln
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
	private void runModules(DefaultMutableTreeNode parentNode) throws Exception {

		// Check whether the parent node holds a module as expected (throw an
		// exception otherwise)
		if (parentNode.getUserObject() == null
				|| !Module.class.isAssignableFrom(parentNode.getUserObject()
						.getClass()))
			throw new Exception(
					"Excuse me, but this tree node does not hold a module -- I am afraid I cannot continue the operation.");

		// Determine the module
		final Module module = (Module) parentNode.getUserObject();

		// Initialize thread
		final Thread moduleThread = new Thread(module);
		moduleThread.setName(module.getName());

		// Final list of started threads
		final ModuleTree moduleTreeInstance = this;

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
		this.addStartedThread(moduleThread);

		// Register callback actions
		this.registerSuccessCallback(moduleThread, successAction);
		this.registerFailureCallback(moduleThread, failureAction);

		// Log thread start message & fire it up 
		Logger.getLogger("").log(
				Level.INFO,
				"Starting to process module " + module.getName()
						+ " on thread #" + moduleThread.getId());
		moduleThread.start();

		// Recursively run this method for the tree node's children
		Enumeration<?> childNodes = parentNode.children();
		while (childNodes.hasMoreElements()) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes
					.nextElement();
			this.runModules(childNode);
		}

	}

	/**
	 * Resets the modules' I/O. Must be called prior re-running the module tree.
	 * 
	 * @throws Exception
	 */
	public void resetModuleIO() throws Exception {

		// Determine root node + module and reset the latter
		DefaultMutableTreeNode rootNode = this.getRootNode();
		Module rootModule = (Module) rootNode.getUserObject();
		rootModule.resetOutputs();

		// Do the same with all child nodes/modules
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> children = rootNode
				.breadthFirstEnumeration();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode childNode = children.nextElement();
			Module childModule = (Module) childNode.getUserObject();
			childModule.resetOutputs();
		}
	}

	/**
	 * Prints a pretty representation of the module chain
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String prettyPrint() throws Exception {
		// Determine the tree's root node
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) this.moduleTree
				.getRoot();

		return this.prettyPrint(rootNode, 0);
	}

	/**
	 * Prints a pretty representation of the module chain (from the given node
	 * on)
	 * 
	 * @param parentNode
	 *            Node to start from
	 * @param level
	 *            Level of indention
	 * @return String
	 * @throws Exception
	 */
	private String prettyPrint(DefaultMutableTreeNode parentNode, int level)
			throws Exception {

		if (parentNode.getUserObject() == null
				|| !Module.class.isAssignableFrom(parentNode.getUserObject()
						.getClass()))
			throw new Exception(
					"This tree node does not hold a module -- I am sorry, but I cannot print it.");

		// Instantiate string buffer to concatenate the result
		StringBuffer result = new StringBuffer();

		// Insert indention
		for (int i = 0; i < level; i++) {
			result.append("\t");
		}

		Module module = (Module) parentNode.getUserObject();
		Pipe pipe = module.getInputBytePipe();
		if (pipe == null)
			pipe = module.getInputCharPipe();

		// Print pipe details
		if (pipe != null) {
			result.append("--" + pipe.getClass().getSimpleName() + "--> ");
		}

		// Print module details
		result.append(module.getName() + "[" + module.getStatus() + "] ");

		// Recursively run this method for the tree node's children
		Enumeration<?> childNodes = parentNode.children();
		while (childNodes.hasMoreElements()) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes
					.nextElement();
			result.append("\n" + this.prettyPrint(childNode, level + 1));
		}

		return result.toString();
	}

}
