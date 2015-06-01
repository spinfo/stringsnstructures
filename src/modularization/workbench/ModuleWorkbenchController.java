package modularization.workbench;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import modularization.ConsoleWriterModule;
import modularization.ExampleModule;
import modularization.FileReaderModule;
import modularization.FileWriterModule;
import modularization.Module;
import modularization.ModuleTree;
import parser.oanc.OANC;
import parser.oanc.OANCXMLParser;

public class ModuleWorkbenchController implements ActionListener, TreeSelectionListener, ListSelectionListener {
	
	protected static final String ACTION_STARTNEWMODULETREE = "ACTION_STARTNEWMODULETREE";
	protected static final String ACTION_ADDMODULETOTREE = "ACTION_ADDMODULETOTREE";
	protected List<Module> availableModules = new ArrayList<Module>();
	private ModuleTree moduleTree;
	private DefaultMutableTreeNode selectedTreeNode;
	private Module selectedModule;
	private Map<String, PropertyQuadrupel> selectedModulesProperties;

	/**
	 * Instantiates a new ModuleWorkbenchController
	 * @throws Exception Thrown if initialization fails
	 */
	public ModuleWorkbenchController() throws Exception {
		
		// Define available modules TODO Load at runtime
		availableModules.add(new ConsoleWriterModule(null, new Properties()));
		availableModules.add(new ExampleModule(null, new Properties()));
		availableModules.add(new FileReaderModule(null, new Properties()));
		availableModules.add(new FileWriterModule(null, new Properties()));
		availableModules.add(new OANC(null, new Properties()));
		availableModules.add(new OANCXMLParser(null, new Properties()));
		
	}
	
	/**
	 * Creates a new module tree with the given module as root.
	 * @param rootModule Root module
	 * @return The created module tree
	 */
	public ModuleTree startNewModuleTree(Module rootModule){
		
		// Create new module tree
		this.moduleTree = new ModuleTree(rootModule);
		rootModule.setCallbackReceiver(this.moduleTree);
		
		// Reset selected tree node
		this.selectedTreeNode = (DefaultMutableTreeNode) this.moduleTree.getModuleTree().getRoot();
		
		return this.moduleTree;
	}

	/**
	 * @return the moduleTree
	 */
	public ModuleTree getModuleTree() {
		return moduleTree;
	}

	/**
	 * @param moduleTree the moduleTree to set
	 */
	public void setModuleTree(ModuleTree moduleTree) {
		this.moduleTree = moduleTree;
	}

	/**
	 * @return the selectedModulesProperties
	 */
	public Map<String, PropertyQuadrupel> getSelectedModulesProperties() {
		return selectedModulesProperties;
	}
	
	public Module getNewInstanceOfSelectedModule(ModuleTree moduleTree) throws Exception{
		
		// If no module is selected, throw exception
		if (this.selectedModule == null)
			throw new Exception("Excuse me, but no module is selected, therefor I cannot instanciate it as requested.");
		
		// New module to create
		Module newModule;
		
		// Set properties
		Properties properties = new Properties();
		Iterator<String> propertyKeys = this.selectedModulesProperties.keySet().iterator();
		while(propertyKeys.hasNext()){
			String propertyKey = propertyKeys.next();
			properties.setProperty(propertyKey, this.selectedModulesProperties.get(propertyKey).getValue());
		}
		
		// Determine the type of the module and instanciate it accordingly
		// TODO Use JarClassLoader to load module classes at runtime
		if (this.selectedModule.getClass().equals(ConsoleWriterModule.class)){
			newModule = new ConsoleWriterModule(moduleTree, properties);
		} else if (this.selectedModule.getClass().equals(ExampleModule.class)){
			newModule = new ExampleModule(moduleTree, properties);
		} else if (this.selectedModule.getClass().equals(FileReaderModule.class)){
			newModule = new FileReaderModule(moduleTree, properties);
		} else if (this.selectedModule.getClass().equals(FileWriterModule.class)){
			newModule = new FileWriterModule(moduleTree, properties);
		} else if (this.selectedModule.getClass().equals(OANC.class)){
			newModule = new OANC(moduleTree, properties);
		} else if (this.selectedModule.getClass().equals(OANCXMLParser.class)){
			newModule = new OANCXMLParser(moduleTree, properties);
		}
		
		else {
			throw new Exception("Selected module is of unknown type.");
		}
		
		return newModule;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(ACTION_STARTNEWMODULETREE)){
			
			// New module to create
			Module rootModule;
			try {
				rootModule = this.getNewInstanceOfSelectedModule(null);
				this.startNewModuleTree(rootModule);
				
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "I'm sorry, but I could not create a new module tree.", e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_ADDMODULETOTREE)){
			
			try {
				// Determine module that is currently selected within the module tree
				Module parentModule = (Module) this.selectedTreeNode.getUserObject();
				Module newModule = this.getNewInstanceOfSelectedModule(this.moduleTree);
						
				// Add new module to selected tree node
				this.moduleTree.addModule(newModule, parentModule);
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Very sorry, but I wasn't able to add the selected module to the tree.", e1);
			}
			
		} else {
			Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but this command is unknown to me: "+e.getActionCommand());
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		try {
			// Determine which node is selected within the module tree
			this.selectedTreeNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
		} catch (ClassCastException ex){
			Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "I am afraid there was an error processing the selected element.", ex);
		}
		
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		try {
			
			// Set selected module
			@SuppressWarnings("unchecked")
			JList<Module> list = (JList<Module>) e.getSource();
			this.selectedModule = list.getSelectedValue();
			
			// Instantiate new map for module properties
			this.selectedModulesProperties = new TreeMap<String, PropertyQuadrupel>();
			
			// Determine properties
			Iterator<String> propertyDescriptionKeys = this.selectedModule.getPropertyDescriptions().keySet().iterator();
			while(propertyDescriptionKeys.hasNext()){
				
				// Instantiate new property quadrupel
				PropertyQuadrupel property = new PropertyQuadrupel();
				
				// Determine property key
				String propertyKey = propertyDescriptionKeys.next();
				
				// Set values for the new property
				property.setKey(propertyKey);
				property.setDescription(this.selectedModule.getPropertyDescriptions().get(propertyKey));
				property.setDefaultValue(this.selectedModule.getPropertyDefaultValues().get(propertyKey));
				property.setValue(this.selectedModule.getPropertyDefaultValues().get(propertyKey));
				
			}
			
			
		} catch (ClassCastException ex) {
			Logger.getLogger(this.getClass().getCanonicalName()).log(
					Level.WARNING, "Error processing selected element.", ex);
		}
	}

	/**
	 * @return the availableModules
	 */
	public List<Module> getAvailableModules() {
		return availableModules;
	}

}
