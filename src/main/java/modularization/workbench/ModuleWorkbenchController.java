package modularization.workbench;

import helpers.ListLoggingHandler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Enumeration;
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
import modularization.ModuleTreeGsonDeserializer;
import modularization.ModuleTreeGsonSerializer;
import modularization.SmbFileReaderModule;
import modularization.SmbFileWriterModule;
import module.ParadigmenErmittlerModul;
import neo4j.Neo4jOutputModule;
import parser.oanc.OANC;
import parser.oanc.OANCXMLParser;
import suffixNetBuilder.SuffixNetBuilderModule;
import treeBuilder.AtomicRangeSuffixTrieBuilder;
import treeBuilder.TreeBuilder;
import visualizationModules.ASCIIGraph;
import visualizationModules.ColourGraph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

public class ModuleWorkbenchController implements TreeSelectionListener, ListSelectionListener {
	
	protected List<Module> availableModules = new ArrayList<Module>();
	private ModuleTree moduleTree;
	private DefaultMutableTreeNode selectedTreeNode;
	private Module selectedModule;
	private Map<String, PropertyQuadrupel> selectedModulesProperties;
	private ListLoggingHandler listLoggingHandler;
	private Gson jsonConverter;

	/**
	 * Instantiates a new ModuleWorkbenchController
	 * @throws Exception Thrown if initialization fails
	 */
	public ModuleWorkbenchController() throws Exception {
		
		// Initialize JSON converter
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(ModuleTree.class, new ModuleTreeGsonSerializer());
		gsonBuilder.registerTypeAdapter(ModuleTree.class, new ModuleTreeGsonDeserializer());
		this.jsonConverter = gsonBuilder.setPrettyPrinting().create();
		
		// Add jlist handler to logger
		this.listLoggingHandler = new ListLoggingHandler();
		Logger.getLogger("").addHandler(this.listLoggingHandler);
		
		// Define available modules TODO Load at runtime
		
		// Prepare OANC module
		Properties oancProperties = new Properties();
		OANC oanc = new OANC(moduleTree, oancProperties);
		oancProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, oanc.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		oanc.applyProperties();

		// Prepare FileWriter module
		Properties fileWriterProperties = new Properties();
		FileWriterModule fileWriter = new FileWriterModule(moduleTree,
				fileWriterProperties);
		fileWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, fileWriter.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		fileWriter.applyProperties();

		// Prepare SmbFileWriter module
		Properties smbFileWriterProperties = new Properties();
		SmbFileWriterModule smbFileWriter = new SmbFileWriterModule(moduleTree,
				smbFileWriterProperties);
		smbFileWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, smbFileWriter.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		smbFileWriter.applyProperties();

		// Prepare OANC parser module
		Properties oancParserProperties = new Properties();
		OANCXMLParser oancParser = new OANCXMLParser(moduleTree,
				oancParserProperties);
		oancParserProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, oancParser.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		oancParser.applyProperties();

		// Prepare FileReader module
		Properties fileReaderProperties = new Properties();
		FileReaderModule fileReader = new FileReaderModule(moduleTree,
				fileReaderProperties);
		fileReaderProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, fileReader.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		fileReader.applyProperties();

		// Prepare SmbFileReader module
		Properties smbFileReaderProperties = new Properties();
		SmbFileReaderModule smbFileReader = new SmbFileReaderModule(moduleTree,
				smbFileReaderProperties);
		smbFileReaderProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, smbFileReader.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		smbFileReader.applyProperties();

		// Prepare ConsoleWriter module
		Properties consoleWriterProperties = new Properties();
		ConsoleWriterModule consoleWriter = new ConsoleWriterModule(moduleTree,
				consoleWriterProperties);
		consoleWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, consoleWriter.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		consoleWriter.applyProperties();

		// Prepare ExampleModule module
		Properties exampleModuleProperties = new Properties();
		ExampleModule exampleModule = new ExampleModule(moduleTree,
				exampleModuleProperties);
		exampleModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, exampleModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		exampleModule.applyProperties();

		// Prepare TreeBuilder module
		Properties treeBuilderModuleProperties = new Properties();
		TreeBuilder treeBuilder = new TreeBuilder(moduleTree,
				treeBuilderModuleProperties);
		treeBuilderModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, treeBuilder.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		treeBuilder.applyProperties();

		// Prepare AtomicRangeSuffixTrieBuilder module
		Properties atomicRangeSuffixTrieBuilderProperties = new Properties();
		AtomicRangeSuffixTrieBuilder atomicRangeSuffixTrieBuilder = new AtomicRangeSuffixTrieBuilder(moduleTree,
				atomicRangeSuffixTrieBuilderProperties);
		atomicRangeSuffixTrieBuilderProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, atomicRangeSuffixTrieBuilder.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		atomicRangeSuffixTrieBuilder.applyProperties();

		// Prepare Neo4jOutputModule module
		Properties neo4jOutputModuleProperties = new Properties();
		Neo4jOutputModule neo4jOutputModule = new Neo4jOutputModule(moduleTree,
				neo4jOutputModuleProperties);
		neo4jOutputModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, neo4jOutputModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		neo4jOutputModule.applyProperties();

		// Prepare SuffixNetBuilderModule module
		Properties suffixNetBuilderModuleProperties = new Properties();
		SuffixNetBuilderModule suffixNetBuilderModule = new SuffixNetBuilderModule(moduleTree,
				suffixNetBuilderModuleProperties);
		suffixNetBuilderModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, suffixNetBuilderModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		suffixNetBuilderModule.applyProperties();

		// Prepare ColourGraph module
		Properties colourGraphModuleProperties = new Properties();
		ColourGraph colourGraphModule = new ColourGraph(moduleTree,
				colourGraphModuleProperties);
		colourGraphModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, colourGraphModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		colourGraphModule.applyProperties();

		// Prepare ASCIIGraph module
		Properties asciiGraphModuleProperties = new Properties();
		ASCIIGraph asciiGraphModule = new ASCIIGraph(moduleTree,
				asciiGraphModuleProperties);
		asciiGraphModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, asciiGraphModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		asciiGraphModule.applyProperties();

		// Prepare ParadigmenErmittlerModul module
		Properties paradigmenErmittlerModulProperties = new Properties();
		ParadigmenErmittlerModul paradigmenErmittlerModul = new ParadigmenErmittlerModul(moduleTree,
				paradigmenErmittlerModulProperties);
		paradigmenErmittlerModulProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, paradigmenErmittlerModul.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		paradigmenErmittlerModul.applyProperties();
		
		availableModules.add(consoleWriter);
		availableModules.add(exampleModule);
		availableModules.add(fileReader);
		availableModules.add(smbFileReader);
		availableModules.add(fileWriter);
		availableModules.add(smbFileWriter);
		availableModules.add(oanc);
		availableModules.add(oancParser);
		availableModules.add(treeBuilder);
		availableModules.add(atomicRangeSuffixTrieBuilder);
		availableModules.add(neo4jOutputModule);
		availableModules.add(suffixNetBuilderModule);
		availableModules.add(colourGraphModule);
		availableModules.add(asciiGraphModule);
		availableModules.add(paradigmenErmittlerModul);
		
		// Sort list
		availableModules.sort(new ModuleComparator());
		
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
		if (this.moduleTree != null && this.moduleTree.getModuleTreeModel() != null){
			// If so, we just need to set a new root module
			this.moduleTree.getModuleTreeModel().setRoot(new DefaultMutableTreeNode(rootModule));
			// ... and make sure the root node knows its callback receiver
			rootModule.setCallbackReceiver(this.moduleTree);
		}
			
		else {
			// Instantiate a new module tree
			this.moduleTree = new ModuleTree(rootModule);
		}
			
		
		// Reset selected tree node
		this.setSelectedTreeNode((DefaultMutableTreeNode) this.moduleTree.getModuleTreeModel().getRoot());
		
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
		} else if (this.selectedModule.getClass().equals(TreeBuilder.class)){
			newModule = new TreeBuilder(moduleTree, properties);
		} else if (this.selectedModule.getClass().equals(AtomicRangeSuffixTrieBuilder.class)){
			newModule = new AtomicRangeSuffixTrieBuilder(moduleTree, properties);
		} else if (this.selectedModule.getClass().equals(Neo4jOutputModule.class)){
			newModule = new Neo4jOutputModule(moduleTree, properties);
		} else if (this.selectedModule.getClass().equals(SuffixNetBuilderModule.class)){
			newModule = new SuffixNetBuilderModule(moduleTree, properties);
		} else if (this.selectedModule.getClass().equals(ColourGraph.class)){
			newModule = new ColourGraph(moduleTree, properties);
		} else if (this.selectedModule.getClass().equals(ASCIIGraph.class)){
			newModule = new ASCIIGraph(moduleTree, properties);
		} else if (this.selectedModule.getClass().equals(SmbFileReaderModule.class)){
			newModule = new SmbFileReaderModule(moduleTree, properties);
		} else if (this.selectedModule.getClass().equals(SmbFileWriterModule.class)){
			newModule = new SmbFileWriterModule(moduleTree, properties);
		} else if (this.selectedModule.getClass().equals(ParadigmenErmittlerModul.class)){
			newModule = new ParadigmenErmittlerModul(moduleTree, properties);
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
	
	/**
	 * Loads the module tree from a file.
	 * @param file file
	 * @return Loaded module tree
	 * @throws Exception 
	 */
	public ModuleTree loadModuleTreeFromFile(File file) throws Exception {
				
		// Read JSON representation of the current module tree from file
		FileReader fileReader = new FileReader(file);
		ModuleTree loadedModuleTree = this.jsonConverter.fromJson(fileReader, ModuleTree.class);
		this.setModuleTree(loadedModuleTree);
				
		// Close file writer
		fileReader.close();
		
		// Apply properties to modules
		DefaultMutableTreeNode rootNode = loadedModuleTree.getRootNode();
		((Module)rootNode.getUserObject()).applyProperties();
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> children = rootNode.breadthFirstEnumeration();
		while (children.hasMoreElements()){
			((Module)children.nextElement().getUserObject()).applyProperties();
		}
		
        // Write log message
        Logger.getLogger("").log(Level.INFO, "Successfully loaded the module tree from the file "+file.getPath());
        
        // Return the loaded tree
		return loadedModuleTree;
	}
	
	/**
	 * Saves the current module tree to a file.
	 * @param file File to save to
	 * @throws Exception 
	 * @throws JsonIOException 
	 */
	public void saveModuleTreeToFile(File file) throws JsonIOException, Exception {
		
		// Write JSON representation of the current module tree to file
		FileWriter fileWriter = new FileWriter(file);
		this.jsonConverter.toJson(this.moduleTree, fileWriter);
		
		// Close file writer
		fileWriter.close();
		
        // Write log message
        Logger.getLogger("").log(Level.INFO, "Successfully saved the module tree into the file "+file.getPath());
	}

}
