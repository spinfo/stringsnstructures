package modularization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import parallelization.Action;
import parallelization.CallbackReceiverImpl;

/**
 * Allows the construction of a chain of modules.
 * @author Marcel Boeing
 *
 */
public class ModuleChain extends CallbackReceiverImpl {

	// List of modules contained in this chain
	List<Module> moduleList = new ArrayList<Module>();
	
	// List of started threads
	List<Thread> startedThreads = new ArrayList<Thread>();
	
	/**
	 * Appends a module to the chain
	 * @param module module to append
	 * @return true if successful
	 * @throws NotSupportedException thrown if module cannot be inserted due to I/O incompatibilities
	 */
	public boolean appendModule(Module module) throws NotSupportedException {
		return this.appendModule(module, moduleList.size());
	}
	
	/**
	 * Appends a module to a specific index of the chain
	 * @param module module to append
	 * @param index index to append to
	 * @return true if successful
	 * @throws NotSupportedException thrown if module cannot be inserted due to I/O incompatibilities
	 */
	public boolean appendModule(Module module, int index) throws NotSupportedException {
		if (index>moduleList.size() || index<0)
			return false;
		
		// Link new module's input to predecessor's output (if present)
		if (index<=moduleList.size() && index > 0){
			try {
				module.getInputReader().connect(moduleList.get(index-1).getOutputWriter());
				Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Module "+moduleList.get(index-1).getName()+" and "+module.getName()+" linked via writer/reader.");
			} catch (NotSupportedException | IOException e1){
				try {
					module.getInputStream().connect(moduleList.get(index-1).getOutputStream());
					Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Module "+moduleList.get(index-1).getName()+" and "+module.getName()+" linked via stream.");
				} catch (NotSupportedException | IOException e2){
					throw new NotSupportedException("I'm terribly sorry, but that module cannot be linked to its predecessor.", e2);
				}
			}
		}
		
		// Link  new module's output to successor's input (if present)
		if (index<moduleList.size()){
			try {
				module.getOutputWriter().connect(moduleList.get(index).getInputReader());
				Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Module "+module.getName()+" and "+moduleList.get(index-1).getName()+" linked via writer/reader.");
			} catch (NotSupportedException | IOException e1){
				try {
					module.getOutputStream().connect(moduleList.get(index).getInputStream());
					Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Module "+module.getName()+" and "+moduleList.get(index-1).getName()+" linked via stream.");
				} catch (NotSupportedException | IOException e2){
					throw new NotSupportedException("I'm terribly sorry, but that module cannot be linked to its predecessor.", e2);
				}
			}
		}
		
		// Add module to list
		moduleList.add(index, module);
		
		// Set module's callback receiver
		module.setCallbackReceiver(this);
		
		return true;
	}
	
	/**
	 * Runs the module chain
	 */
	public void runChain(){
		Iterator<Module> modules = moduleList.iterator();
		
		while(modules.hasNext()){
			
			// Determine next module in list
			final Module m = modules.next();
			
			// Define action to perform on success
			Action successAction = new Action(){
				@Override
				public void perform(Object processResult){
					Boolean result = Boolean.parseBoolean(processResult.toString());
					if (result)
						Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Module "+m.getName()+" has successfully finished processing.");
					else
						Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Module "+m.getName()+" did not finish processing successfully.");
				}
			};
			
			// Define action to perform on success
			Action failureAction = new Action() {
				@Override
				public void perform(Object processResult) {
					Exception e = new Exception("(no error message received)");
					if (processResult.getClass().isAssignableFrom(e.getClass())){
						e = (Exception) processResult;
					}
					Logger.getLogger(this.getClass().getSimpleName()).log(Level.SEVERE, "Module " + m.getName() + " encountered an error.", e);
				}
			};
			
			// register callback actions
			this.registerSuccessCallback(m, successAction);
			this.registerFailureCallback(m, failureAction);
			
			Thread t1 = new Thread( m );
			t1.setName(m.getName());
			this.startedThreads.add(t1);
			t1.start();
		}
		
		// Wait for threads to finish
		while(!this.startedThreads.isEmpty()){
			try {
				// Sleep for one second
				Thread.sleep(1000l);
				
				// Test which threads are still active and remove the rest from the list
				for (int i=this.startedThreads.size(); i>0; i--){
					if (!this.startedThreads.get(i-1).isAlive()){
						Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Thread "+this.startedThreads.get(i-1).getName()+" is done.");
						Thread removedThread = this.startedThreads.remove(i-1);
						if (removedThread != null)
							Logger.getLogger(this.getClass().getSimpleName()).log(Level.FINEST, "Removed thread "+removedThread.getName()+".");
						else
							Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Could not remove thread.");
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
	 * Prints a pretty representation of the module chain
	 * @return String
	 */
	public String prettyPrint(){
		StringBuffer result = new StringBuffer();
		Iterator<Module> modules = moduleList.iterator();
		while(modules.hasNext()){
			Module m = modules.next();
			
			// Determine the used I/O
			String inputLink;
			try {
				m.getInputReader();
				inputLink = "R";
			} catch (NotSupportedException e1){
				try {
					m.getInputStream();
					inputLink = "S";
				} catch (NotSupportedException e2){
					inputLink = "X";
				}
			}
			String outputLink;
			try {
				m.getOutputWriter();
				outputLink = "W";
			} catch (NotSupportedException e1){
				try {
					m.getOutputStream();
					outputLink = "S";
				} catch (NotSupportedException e2){
					outputLink = "X";
				}
			}
			
			result.append(inputLink+"]-->"+m.getName()+"--["+outputLink);
		}
		
		return result.toString();
	}
	
}
