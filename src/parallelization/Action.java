/**
 * 
 */
package parallelization;

/**
 * An action to perform.
 * @author Marcel Boeing
 * 
 */
public abstract class Action {
	
	public Action(){
		super();
	}

	/**
	 * Performs the action
	 * @param processResult The result of a process
	 */
	public void perform(Object processResult) {
	}

}
