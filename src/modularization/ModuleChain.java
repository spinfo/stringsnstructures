package modularization;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import parallelization.CallbackReceiverImpl;

/**
 * Allows the construction of a chain of modules.
 * @author Marcel Boeing
 *
 */
public class ModuleChain extends CallbackReceiverImpl {

	// List of modules contained in this chain
	List<Module> moduleList = new ArrayList<Module>();
	
	/**
	 * Matches two lists of IO-Classes against each other and returns the first match.
	 * @param classesA First list of IO-Classes
	 * @param classesB Second list of IO-Classes
	 * @return Matching IO-Class
	 * @throws IncompatibleIOException Thrown if no match was found
	 */
	private Class<?> matchIOClasses(List<Class<?>> classesA, List<Class<?>> classesB) throws IncompatibleIOException {
		
		Iterator<Class<?>> ioClassesA = classesA.iterator();
		while(ioClassesA.hasNext()){
			
			Class<?> classA = ioClassesA.next();
			
			Iterator<Class<?>> ioClassesB = classesB.iterator();
			while(ioClassesB.hasNext()){
				
				if (ioClassesB.next().equals(classA))
					return classA;
				
			}
			
		}
		
		throw new IncompatibleIOException("No matching I/O-classes found.");
	}
	
	public boolean appendModule(Module module) throws IncompatibleIOException {
		return this.appendModule(module, moduleList.size());
	}
	
	public boolean appendModule(Module module, int index) throws IncompatibleIOException {
		if (index>moduleList.size() || index<0)
			return false;
		
		// Link new module's input to predecessor's output (if present)
		if (index<=moduleList.size() && index > 0){
			module.setInput(moduleList.get(index-1).getOutput());
		}
		
		// Link  new module's output to successor's input (if present)
		if (index<moduleList.size()){
			moduleList.get(index).setInput(module.getOutput());
		}
		
		// Add module to list
		moduleList.add(index, module);
		
		return true;
	}
	
	public String prettyPrint(){
		StringBuffer result = new StringBuffer("o --> ");
		Iterator<Module> modules = moduleList.iterator();
		while(modules.hasNext()){
			result.append(modules.next().getName()+" --> ");
		}
		
		return result.toString();
	}
	
}
