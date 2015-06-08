package modularization.workbench;

import helpers.ListLoggingHandler;

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
import modularization.ModuleImpl;
import modularization.ModuleTree;
import parser.oanc.OANC;
import parser.oanc.OANCXMLParser;

public class ModuleWorkbenchController implements TreeSelectionListener, ListSelectionListener {
	

	protected List<Module> availableModules = new ArrayList<Module>();
	private ModuleTree moduleTree;
	private DefaultMutableTreeNode selectedTreeNode;
	private Module selectedModule;
	private Map<String, PropertyQuadrupel> selectedModulesProperties;
	private ListLoggingHandler listLoggingHandler;

	/**
	 * Instantiates a new ModuleWorkbenchController
	 * @throws Exception Thrown if initialization fails
	 */
	public ModuleWorkbenchController() throws Exception {
		
		// Add jlist handler to logger
		this.listLoggingHandler = new ListLoggingHandler();
		Logger.getLogger("").addHandler(this.listLoggingHandler);
		
		// Define available modules TODO Load at runtime
		
		// Prepare OANC module
		Properties oancProperties = new Properties();
		oancProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, "OANC");
		OANC oanc = new OANC(moduleTree, oancProperties);

		// Prepare FileWriter module
		Properties fileWriterProperties = new Properties();
		fileWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME,
				"FileWriter");
		FileWriterModule fileWriter = new FileWriterModule(moduleTree,
				fileWriterProperties);

		// Prepare OANC parser module
		Properties oancParserProperties = new Properties();
		oancParserProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME,
				"OANC-Parser");
		OANCXMLParser oancParser = new OANCXMLParser(moduleTree,
				oancParserProperties);

		// Prepare FileReader module
		Properties fileReaderProperties = new Properties();
		fileReaderProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME,
				"FileReader");
		FileReaderModule fileReader = new FileReaderModule(moduleTree,
				fileReaderProperties);

		// Prepare ConsoleWriter module
		Properties consoleWriterProperties = new Properties();
		consoleWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME,
				"ConsoleWriter");
		ConsoleWriterModule consoleWriter = new ConsoleWriterModule(moduleTree,
				consoleWriterProperties);

		// Prepare ExampleModule module
		Properties exampleModuleProperties = new Properties();
		exampleModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME,
				"Example Module");
		ExampleModule exampleModule = new ExampleModule(moduleTree,
				exampleModuleProperties);
		
		availableModules.add(consoleWriter);
		availableModules.add(exampleModule);
		availableModules.add(fileReader);
		availableModules.add(fileWriter);
		availableModules.add(oanc);
		availableModules.add(oancParser);
		
		// Instantiate default module tree
		this.startNewModuleTree(oanc);
		
	}
	
	/**
	 * Creates a new module tree with the given module as root.
	 * @param rootModule Root module
	 * @return The created module tree
	 */
	public ModuleTree startNewModuleTree(Module rootModule){
		
		// Determine if a module tree already exists
		if (this.moduleTree != null && this.moduleTree.getModuleTree() != null)
			// If so, we just need to set a new root module
			this.moduleTree.getModuleTree().setRoot(new DefaultMutableTreeNode(rootModule));
		else {
			// Instantiate a new module tree
			this.moduleTree = new ModuleTree(rootModule);
		}
			
		
		// Reset selected tree node
		this.setSelectedTreeNode((DefaultMutableTreeNode) this.moduleTree.getModuleTree().getRoot());
		
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
	
	/**
	 * Returns a new instance of the module currently selected in the available modules list.
	 * @param moduleTree Module tree to set as callback receiver for the new module's instance
	 * @return new module instance
	 * @throws Exception
	 */
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
			String propertyValue = this.selectedModulesProperties.get(propertyKey).getValue();
			if (propertyValue != null)
				properties.setProperty(propertyKey, propertyValue);
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
	public void valueChanged(TreeSelectionEvent e) {
		try {
			// Determine which node is selected within the module tree
			this.setSelectedTreeNode((DefaultMutableTreeNode) e.getPath().getLastPathComponent());
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
				
				// Add property quadrupel to result list
				this.selectedModulesProperties.put(propertyKey, property);
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

	/**
	 * @return the listLoggingHandler
	 */
	public ListLoggingHandler getListLoggingHandler() {
		return listLoggingHandler;
	}

	public DefaultMutableTreeNode getSelectedTreeNode() {
		return selectedTreeNode;
	}

	public void setSelectedTreeNode(DefaultMutableTreeNode selectedTreeNode) {
		this.selectedTreeNode = selectedTreeNode;
	}

	/**
	 * @return the selectedModule
	 */
	public Module getSelectedModule() {
		return selectedModule;
	}

}
