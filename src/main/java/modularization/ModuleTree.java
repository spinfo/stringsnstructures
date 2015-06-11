package modularization;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import parallelization.Action;
import parallelization.CallbackReceiverImpl;

/**
 * Allows the construction of a tree of modules.
 * @author Marcel Boeing
 *
 */
public class ModuleTree extends CallbackReceiverImpl {
	
	// The treemodel used to organize the modules
	private DefaultTreeModel moduleTree;
	
	// List of started threads
	private List<Thread> startedThreads = new ArrayList<Thread>();
	
	/**
	 * Determines which pipe to use between both given modules (prefers byte pipe).
	 * @param outputProvider Module that provides the output
	 * @param inputReceiver Module that receives the input
	 * @return Compatible pipe
	 * @throws Exception Thrown if the modules' I/O is not compatible
	 */
	public static Pipe getCompatiblePipe(Module outputProvider, Module inputReceiver) throws Exception{
		Pipe pipe = new BytePipe();
		if (!(inputReceiver.supportsInputPipe(pipe) && outputProvider.supportsOutputPipe(pipe))){
			pipe = new CharPipe();
			if (!(inputReceiver.supportsInputPipe(pipe) && outputProvider.supportsOutputPipe(pipe))){
				throw new Exception("I'm very sorry, but the I/O of those two modules does not seem to be compatible.");
			}
		}
		return pipe;
	}
	
	/**
	 * When using the constructor without parameters you need to
	 * set the root module manually before adding any other modules!
	 */
	public ModuleTree(){
		super();
	}
	
	/**
	 * Preferred constructor
	 * @param module
	 */
	public ModuleTree(Module module){
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
	 * @param moduleTree the moduleTree to set
	 */
	public void setModuleTree(DefaultTreeModel moduleTree) {
		
		// Update instance variable
		this.moduleTree = moduleTree;
		
		// Determine root module and set its callback receiver
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) this.moduleTree.getRoot();
		Module module = (Module) rootNode.getUserObject();
		module.setCallbackReceiver(this);
		
		// Do the same with all other modules within the tree
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> childNodes = rootNode.breadthFirstEnumeration();
		while (childNodes.hasMoreElements()){
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
	 * Sets the tree's root module
	 * @param module Module to set as root
	 */
	public void setRootModule(Module module){
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
	 * @return root node
	 */
	public DefaultMutableTreeNode getRootNode() throws ClassCastException {
		return (DefaultMutableTreeNode) this.moduleTree.getRoot();
	}
	
	/**
	 * Returns the module tree's root module.
	 * @return root module
	 */
	public Module getRootModule(){
		try {
			return (Module) this.getRootNode().getUserObject();
			
		} catch (Exception e){
			Logger.getLogger("").log(Level.WARNING, "Failed to determine the module tree's root module.", e);
			return null;
		}
	}
	
	/**
	 * Returns the tree node that holds the given module
	 * (or null if it is not found)
	 * @param module Module to search for
	 * @return Tree node that holds the module or null
	 */
	public DefaultMutableTreeNode locateModuleInTree(Module module){
		// Determine the tree's root node
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) this.moduleTree.getRoot();
		
		return this.locateModuleInTree(module, rootNode);
	}
	
	/**
	 * Returns the tree node that holds the given module
	 * (or null if it is not found)
	 * @param module Module to search for
	 * @param parentNode Tree node to start the search from
	 * @return Tree node that holds the module or null
	 */
	private DefaultMutableTreeNode locateModuleInTree(Module module, DefaultMutableTreeNode parentNode){
		if (parentNode.getUserObject() != null && parentNode.getUserObject().equals(module))
			return parentNode;
		
		// Recursively run this method for the tree node's children
		Enumeration<?> childNodes = parentNode.children();
		while (childNodes.hasMoreElements()) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes.nextElement();
			DefaultMutableTreeNode foundNode = this.locateModuleInTree(module, childNode);
			if (foundNode != null)
				return foundNode;
		}
		
		return null;
	}
	
	/**
	 * Appends a module to the given tree node. Tries to guess the best pipe and connects the modules.
	 * @param newModule Module to add
	 * @param parentModule Module the new module should be a child of
	 * @return True if successful
	 * @throws NotSupportedException Thrown if the pipe is not compatible with both the new and the parent module
	 * @throws Exception Thrown if the method argument values are not right
	 */
	public boolean addModule(Module newModule, Module parentModule) throws NotSupportedException, Exception{
		
		// Determine pipe that connects both modules
		Pipe pipe = ModuleTree.getCompatiblePipe(parentModule, newModule);
		
		// Jump to more detailed method
		return this.addModule(newModule, parentModule, pipe);
	}
	
	/**
	 * Appends a module to the given tree node
	 * @param newModule Module to add
	 * @param parentModule Module the new module should be a child of
	 * @param connectingPipe Pipe to connect the new module to its parent
	 * @return True if successful
	 * @throws NotSupportedException Thrown if the pipe is not compatible with both the new and the parent module
	 * @throws Exception Thrown if the method argument values are not right
	 */
	public boolean addModule(Module newModule, Module parentModule, Pipe pipe) throws NotSupportedException, Exception{
		// Determine the location of the parent module within the tree
		DefaultMutableTreeNode parentModuleNode = this.locateModuleInTree(parentModule);
		
		// If the parent module isn't found, we're done
		if (parentModuleNode==null) return false;
		
		// Jump to more detailed method
		return this.addModule(newModule, parentModuleNode, pipe);
	}
	
	/**
	 * Appends a module to the given tree node
	 * @param newModule Module to add
	 * @param parentNode Node the new module should be a child of
	 * @param connectingPipe Pipe to connect the new module to its parent
	 * @return True if successful
	 * @throws NotSupportedException Thrown if the pipe is not compatible with both the new and the parent module
	 * @throws Exception Thrown if the method argument values are not right
	 */
	public boolean addModule(Module newModule, DefaultMutableTreeNode parentNode, Pipe connectingPipe) throws NotSupportedException, Exception {
		
		// Check whether the parent node holds a module as expected (throw an exception otherwise)
		if (parentNode.getUserObject()==null || !Module.class.isAssignableFrom(parentNode.getUserObject().getClass()))
			throw new Exception("Excuse me, but this tree node does not hold a module -- I am afraid I cannot continue the operation.");
		
		// Determine parent module
		Module parentModule = (Module) parentNode.getUserObject();
		
		// Make sure the I/O pipe is compatible to both modules
		if (!newModule.supportsInputPipe(connectingPipe) || !parentModule.supportsOutputPipe(connectingPipe))
			throw new NotSupportedException("Terribly sorry, but this pipe cannot be used for I/O between those modules.");
		
		// Connect modules
		newModule.setInputPipe(connectingPipe);
		parentModule.addOutputPipe(connectingPipe);
		
		// Create new tree node
		DefaultMutableTreeNode newModuleNode = new DefaultMutableTreeNode(newModule);
		
		// Insert new tree node
		this.moduleTree.insertNodeInto(newModuleNode, parentNode, parentNode.getChildCount());
		
		// Set module's callback receiver
		newModule.setCallbackReceiver(this);
		
		return true;
	}
	
	/**
	 * Runs all modules the module tree contains.
	 * @throws Exception
	 */
	public void runModules() throws Exception {
		this.runModules(false);
	}
	
	/**
	 * Runs all modules the module tree contains.
	 * @param runUntilAllThreadsAreDone If true, the method runs until all spawned threads have finished
	 * @throws Exception
	 */
	public void runModules(boolean runUntilAllThreadsAreDone) throws Exception {
		this.runModules(runUntilAllThreadsAreDone, 5000l);
	}
	
	/**
	 * Runs all modules the module tree contains.
	 * @param runUntilAllThreadsAreDone If true, the method runs until all spawned threads have finished
	 * @param interval Interval to check for thread completion in milliseconds
	 * @throws Exception
	 */
	public void runModules(boolean runUntilAllThreadsAreDone, long interval) throws Exception {
		
		// Determine the tree's root node
		DefaultMutableTreeNode rootNode = this.getRootNode();
		
		// If that node's module has already been run, we will have to reset all modules' I/O
		if (((Module)rootNode.getUserObject()).getStatus()!=Module.STATUSCODE_NOTYETRUN)
			this.resetModuleIO();
		
		// Run modules
		this.runModules(rootNode);
		
		// Wait for threads to finish, if requested
		while (runUntilAllThreadsAreDone && !this.startedThreads.isEmpty()) {
			try {
				// Sleep for a quarter second
				Thread.sleep(interval);

				// Print pretty overview
				Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, this.prettyPrint());

				// Test which threads are still active and remove the rest from the list
				for (int i = this.startedThreads.size(); i > 0; i--) {
					if (!this.startedThreads.get(i - 1).isAlive()) {
						Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Thread "+this.startedThreads.get(i-1).getName()+" is done.");
						Thread removedThread = this.startedThreads
								.remove(i - 1);
						if (removedThread != null)
							Logger.getLogger(this.getClass().getSimpleName())
									.log(Level.FINEST,
											"Removed thread "
													+ removedThread.getName()
													+ ".");
						else
							Logger.getLogger(this.getClass().getSimpleName())
									.log(Level.WARNING,
											"Could not remove thread.");
					} else {
						Logger.getLogger(this.getClass().getSimpleName()).log(Level.FINEST, "Thread "+this.startedThreads.get(i-1).getName()+" is still active.");
					}
				}

			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
	/**
	 * Runs the modules from a given tree node on.
	 * @param parentNode Root of tree branch from which to run the modules
	 * @throws Exception Thrown if the method argument values are not right
	 */
	private void runModules(DefaultMutableTreeNode parentNode) throws Exception {
		
		// Check whether the parent node holds a module as expected (throw an exception otherwise)
		if (parentNode.getUserObject()==null || !Module.class.isAssignableFrom(parentNode.getUserObject().getClass()))
			throw new Exception("Excuse me, but this tree node does not hold a module -- I am afraid I cannot continue the operation.");
			
		// Determine the module
		final Module m = (Module) parentNode.getUserObject();

		// Define action to perform on success
		Action successAction = new Action() {
			@Override
			public void perform(Object processResult) {
				Boolean result = Boolean.parseBoolean(processResult.toString());
				if (result)
					Logger.getLogger("").log(
							Level.INFO,
							"Module " + m.getName()
									+ " has successfully finished processing.");
				else
					Logger.getLogger("")
							.log(Level.WARNING,
									"Module "
											+ m.getName()
											+ " did not finish processing successfully.");
			}
		};

		// Define action to perform on failure
		Action failureAction = new Action() {
			@Override
			public void perform(Object processResult) {
				Exception e = new Exception("(no error message received)");
				if (processResult.getClass().isAssignableFrom(e.getClass())) {
					e = (Exception) processResult;
				}
				Logger.getLogger(this.getClass().getSimpleName()).log(
						Level.SEVERE,
						"Module " + m.getName() + " encountered an error.", e);
			}
		};

		// register callback actions
		this.registerSuccessCallback(m, successAction);
		this.registerFailureCallback(m, failureAction);

		Thread t1 = new Thread(m);
		t1.setName(m.getName());
		this.startedThreads.add(t1);
		
		Logger.getLogger("").log(Level.INFO, "Starting to process module "+m.getName()+" on thread #"+t1.getId());
		t1.start();
		
		// Recursively run this method for the tree node's children
		Enumeration<?> childNodes = parentNode.children();
		while (childNodes.hasMoreElements()) {
		  DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes.nextElement();
		  this.runModules(childNode);
		}
		
	}
	
	/**
	 * Resets the modules' I/O. Must be called prior re-running the module tree.
	 * @throws Exception
	 */
	public void resetModuleIO() throws Exception{
		
		// Determine root node + module and reset the latter
		DefaultMutableTreeNode rootNode = this.getRootNode();
		Module rootModule = (Module) rootNode.getUserObject();
		rootModule.resetOutputs();
		
		// Do the same with all child nodes/modules
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> children = rootNode.breadthFirstEnumeration();
		while (children.hasMoreElements()){
			DefaultMutableTreeNode childNode = children.nextElement();
			Module childModule = (Module) childNode.getUserObject();
			childModule.resetOutputs();
		}
	}
	
	/**
	 * Prints a pretty representation of the module chain
	 * @return String
	 * @throws Exception
	 */
	public String prettyPrint() throws Exception {
		// Determine the tree's root node
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) this.moduleTree.getRoot();
		
		return this.prettyPrint(rootNode, 0);
	}
	
	/**
	 * Prints a pretty representation of the module chain
	 * (from the given node on)
	 * @param parentNode Node to start from
	 * @param level Level of indention
	 * @return String
	 * @throws Exception
	 */
	private String prettyPrint(DefaultMutableTreeNode parentNode, int level) throws Exception {
		
		if (parentNode.getUserObject()==null || !Module.class.isAssignableFrom(parentNode.getUserObject().getClass()))
			throw new Exception("This tree node does not hold a module -- I am sorry, but I cannot print it.");
		
		// Instantiate string buffer to concatenate the result
		StringBuffer result = new StringBuffer();
		
		// Insert indention
		for (int i=0; i<level; i++){
			result.append("\t");
		}
		
		Module module = (Module) parentNode.getUserObject();
		Pipe pipe = module.getInputBytePipe();
		if (pipe == null)
			pipe = module.getInputCharPipe();
		
		// Print pipe details
		if (pipe != null){
			result.append("--"+pipe.getClass().getSimpleName()+"--> ");
		}
		
		// Print module details
		result.append(module.getName()+"["+module.getStatus()+"] ");
		
		// Recursively run this method for the tree node's children
		Enumeration<?> childNodes = parentNode.children();
		while (childNodes.hasMoreElements()) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes.nextElement();
			result.append("\n"+this.prettyPrint(childNode, level+1));
		}
		
		return result.toString();
	}
	
}
