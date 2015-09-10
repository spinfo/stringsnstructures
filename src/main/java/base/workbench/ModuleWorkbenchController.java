package base.workbench;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import common.ListLoggingHandler;
import common.ModuleComparator;
import modules.Module;
import modules.ModuleImpl;
import modules.ModuleNetwork;
import modules.ModuleTreeGsonDeserializer;
import modules.ModuleNetworkGsonSerializer;
import modules.artificialSeqs.CreateArtificialSeqs;
import modules.basemodules.ConsoleWriterModule;
import modules.basemodules.ExampleModule;
import modules.basemodules.FileReaderModule;
import modules.basemodules.FileWriterModule;
import modules.basemodules.SmbFileReaderModule;
import modules.basemodules.SmbFileWriterModule;
import modules.neo4j.Neo4jOutputModule;
import modules.oanc.OANC;
import modules.oanc.OANCXMLParser;
import modules.paradigmSegmenter.ParadigmenErmittlerModul;
import modules.seqNewickExporter.SeqNewickExproterController;
import modules.seqSplitting.SeqMemory;
import modules.seqSuffixTrie2SuffixTree.SeqSuffixTrie2SuffixTreeController;
import modules.seqTreeProperties.SeqTreePropController;
import modules.suffixNetBuilder.SuffixNetBuilderModule;
import modules.treeBuilder.AtomicRangeSuffixTrieBuilder;
import modules.treeBuilder.TreeBuilder;
import modules.visualizationModules.ASCIIGraph;
import modules.visualizationModules.ColourGraph;

public class ModuleWorkbenchController implements TreeSelectionListener, ListSelectionListener {
	
	protected List<Module> availableModules = new ArrayList<Module>();
	private ModuleNetwork moduleNetwork;
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
		gsonBuilder.registerTypeAdapter(ModuleNetwork.class, new ModuleNetworkGsonSerializer());
		gsonBuilder.registerTypeAdapter(ModuleNetwork.class, new ModuleTreeGsonDeserializer());
		this.jsonConverter = gsonBuilder.setPrettyPrinting().create();
		
		// Add jlist handler to logger
		this.listLoggingHandler = new ListLoggingHandler();
		Logger.getLogger("").addHandler(this.listLoggingHandler);
		
		// Define available modules TODO Load at runtime
		
		// Prepare OANC module
		Properties oancProperties = new Properties();
		OANC oanc = new OANC(moduleNetwork, oancProperties);
		oancProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, oanc.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		oanc.applyProperties();

		// Prepare FileWriter module
		Properties fileWriterProperties = new Properties();
		FileWriterModule fileWriter = new FileWriterModule(moduleNetwork,
				fileWriterProperties);
		fileWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, fileWriter.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		fileWriter.applyProperties();

		// Prepare SmbFileWriter module
		Properties smbFileWriterProperties = new Properties();
		SmbFileWriterModule smbFileWriter = new SmbFileWriterModule(moduleNetwork,
				smbFileWriterProperties);
		smbFileWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, smbFileWriter.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		smbFileWriter.applyProperties();

		// Prepare OANC parser module
		Properties oancParserProperties = new Properties();
		OANCXMLParser oancParser = new OANCXMLParser(moduleNetwork,
				oancParserProperties);
		oancParserProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, oancParser.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		oancParser.applyProperties();

		// Prepare FileReader module
		Properties fileReaderProperties = new Properties();
		FileReaderModule fileReader = new FileReaderModule(moduleNetwork,
				fileReaderProperties);
		fileReaderProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, fileReader.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		fileReader.applyProperties();

		// Prepare SmbFileReader module
		Properties smbFileReaderProperties = new Properties();
		SmbFileReaderModule smbFileReader = new SmbFileReaderModule(moduleNetwork,
				smbFileReaderProperties);
		smbFileReaderProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, smbFileReader.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		smbFileReader.applyProperties();

		// Prepare ConsoleWriter module
		Properties consoleWriterProperties = new Properties();
		ConsoleWriterModule consoleWriter = new ConsoleWriterModule(moduleNetwork,
				consoleWriterProperties);
		consoleWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, consoleWriter.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		consoleWriter.applyProperties();

		// Prepare ExampleModule module
		Properties exampleModuleProperties = new Properties();
		ExampleModule exampleModule = new ExampleModule(moduleNetwork,
				exampleModuleProperties);
		exampleModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, exampleModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		exampleModule.applyProperties();

		// Prepare TreeBuilder module
		Properties treeBuilderModuleProperties = new Properties();
		TreeBuilder treeBuilder = new TreeBuilder(moduleNetwork,
				treeBuilderModuleProperties);
		treeBuilderModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, treeBuilder.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		treeBuilder.applyProperties();

		// Prepare AtomicRangeSuffixTrieBuilder module
		Properties atomicRangeSuffixTrieBuilderProperties = new Properties();
		AtomicRangeSuffixTrieBuilder atomicRangeSuffixTrieBuilder = new AtomicRangeSuffixTrieBuilder(moduleNetwork,
				atomicRangeSuffixTrieBuilderProperties);
		atomicRangeSuffixTrieBuilderProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, atomicRangeSuffixTrieBuilder.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		atomicRangeSuffixTrieBuilder.applyProperties();

		// Prepare Neo4jOutputModule module
		Properties neo4jOutputModuleProperties = new Properties();
		Neo4jOutputModule neo4jOutputModule = new Neo4jOutputModule(moduleNetwork,
				neo4jOutputModuleProperties);
		neo4jOutputModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, neo4jOutputModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		neo4jOutputModule.applyProperties();

		// Prepare SuffixNetBuilderModule module
		Properties suffixNetBuilderModuleProperties = new Properties();
		SuffixNetBuilderModule suffixNetBuilderModule = new SuffixNetBuilderModule(moduleNetwork,
				suffixNetBuilderModuleProperties);
		suffixNetBuilderModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, suffixNetBuilderModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		suffixNetBuilderModule.applyProperties();

		// Prepare ColourGraph module
		Properties colourGraphModuleProperties = new Properties();
		ColourGraph colourGraphModule = new ColourGraph(moduleNetwork,
				colourGraphModuleProperties);
		colourGraphModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, colourGraphModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		colourGraphModule.applyProperties();

		// Prepare ASCIIGraph module
		Properties asciiGraphModuleProperties = new Properties();
		ASCIIGraph asciiGraphModule = new ASCIIGraph(moduleNetwork,
				asciiGraphModuleProperties);
		asciiGraphModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, asciiGraphModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		asciiGraphModule.applyProperties();

		// Prepare ParadigmenErmittlerModul module
		Properties paradigmenErmittlerModulProperties = new Properties();
		ParadigmenErmittlerModul paradigmenErmittlerModul = new ParadigmenErmittlerModul(moduleNetwork,
				paradigmenErmittlerModulProperties);
		paradigmenErmittlerModulProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, paradigmenErmittlerModul.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		paradigmenErmittlerModul.applyProperties();
		
		// Prepare CreateArtificialSeqs module
		Properties createArtificialSeqsProperties = new Properties();
		CreateArtificialSeqs createArtificialSeqs = new CreateArtificialSeqs(moduleNetwork,
				createArtificialSeqsProperties);
		createArtificialSeqsProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, createArtificialSeqs.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		createArtificialSeqs.applyProperties();
		
		// Prepare SeqMemory module
		Properties SeqMemoryProperties = new Properties();
		SeqMemory seqMemory = new SeqMemory(moduleNetwork,
				SeqMemoryProperties);
		SeqMemoryProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, seqMemory.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		seqMemory.applyProperties();

		// Prepare SeqTreePropController module
		Properties SeqTreePropControllerProperties = new Properties();
		SeqTreePropController seqTreePropController = new SeqTreePropController(moduleNetwork,
				SeqTreePropControllerProperties);
		SeqTreePropControllerProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, seqTreePropController.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		seqTreePropController.applyProperties();
		
		// Prepare modules.seqSuffixTrie2SuffixTree module
		Properties seqSuffixTrie2SuffixTreeControllerProperties = new Properties();
		SeqSuffixTrie2SuffixTreeController seqSuffixTrie2SuffixTreeController = new SeqSuffixTrie2SuffixTreeController(moduleNetwork,
				seqSuffixTrie2SuffixTreeControllerProperties);
		seqSuffixTrie2SuffixTreeControllerProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, seqSuffixTrie2SuffixTreeController.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		seqSuffixTrie2SuffixTreeController.applyProperties();
			
		// Prepare seqNewickExporter module
		Properties SeqNewickExproterControllerProperties = new Properties();
		SeqNewickExproterController seqNewickExproterController = new SeqNewickExproterController(moduleNetwork,
				SeqNewickExproterControllerProperties);
		SeqNewickExproterControllerProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, seqNewickExproterController.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		seqNewickExproterController.applyProperties();
				
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
		availableModules.add(createArtificialSeqs);
		availableModules.add(seqMemory);
		availableModules.add(seqTreePropController);
		availableModules.add(seqSuffixTrie2SuffixTreeController);
		availableModules.add(seqNewickExproterController);
		
		// Sort list
		availableModules.sort(new ModuleComparator());
		
		// Instantiate default module tree
		this.startNewModuleTree(oanc);
		
	}
	
	/**
	 * Clears the current module network.
	 */
	public void clearModuleNetwork(){
		
		// Determine if a module network already exists
		if (this.moduleNetwork != null){
			// If so, we just need to set a new root module
			this.moduleNetwork.getModuleTreeModel().setRoot(new DefaultMutableTreeNode(rootModule));
			// ... and make sure the root node knows its callback receiver
			rootModule.setCallbackReceiver(this.moduleNetwork);
		}
			
		else {
			// Instantiate a new module tree
			this.moduleNetwork = new ModuleNetwork(rootModule);
		}
			
		
		// Reset selected tree node
		this.setSelectedTreeNode((DefaultMutableTreeNode) this.moduleNetwork.getModuleTreeModel().getRoot());
		
		return this.moduleNetwork;
	}

	/**
	 * @return the moduleNetwork
	 */
	public ModuleNetwork getModuleTree() {
		return moduleNetwork;
	}

	/**
	 * @param moduleNetwork the moduleNetwork to set
	 */
	public void setModuleTree(ModuleNetwork moduleNetwork) {
		this.moduleNetwork = moduleNetwork;
	}

	/**
	 * @return the selectedModulesProperties
	 */
	public Map<String, PropertyQuadrupel> getSelectedModulesProperties() {
		return selectedModulesProperties;
	}
	
	/**
	 * Returns a new instance of the module currently selected in the available modules list.
	 * @param moduleNetwork Module tree to set as callback receiver for the new module's instance
	 * @return new module instance
	 * @throws Exception
	 */
	public Module getNewInstanceOfSelectedModule(ModuleNetwork moduleNetwork) throws Exception{
		
		// If no module is selected, throw exception
		if (this.selectedModule == null)
			throw new Exception("Excuse me, but no module is selected, therefor I cannot instanciate it as requested.");
		
		// New module to create
		Module newModule;
		
		// Set propertiesErmittlerModul
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
			newModule = new ConsoleWriterModule(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(ExampleModule.class)){
			newModule = new ExampleModule(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(FileReaderModule.class)){
			newModule = new FileReaderModule(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(FileWriterModule.class)){
			newModule = new FileWriterModule(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(OANC.class)){
			newModule = new OANC(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(OANCXMLParser.class)){
			newModule = new OANCXMLParser(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(TreeBuilder.class)){
			newModule = new TreeBuilder(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(AtomicRangeSuffixTrieBuilder.class)){
			newModule = new AtomicRangeSuffixTrieBuilder(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(Neo4jOutputModule.class)){
			newModule = new Neo4jOutputModule(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(SuffixNetBuilderModule.class)){
			newModule = new SuffixNetBuilderModule(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(ColourGraph.class)){
			newModule = new ColourGraph(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(ASCIIGraph.class)){
			newModule = new ASCIIGraph(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(SmbFileReaderModule.class)){
			newModule = new SmbFileReaderModule(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(SmbFileWriterModule.class)){
			newModule = new SmbFileWriterModule(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(ParadigmenErmittlerModul.class)){
			newModule = new ParadigmenErmittlerModul(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(CreateArtificialSeqs.class)){
			newModule = new CreateArtificialSeqs(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(SeqMemory.class)){
			newModule = new SeqMemory(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(SeqTreePropController.class)){
			newModule = new SeqTreePropController(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(SeqSuffixTrie2SuffixTreeController.class)){
			newModule = new SeqSuffixTrie2SuffixTreeController(moduleNetwork, properties);
		} else if (this.selectedModule.getClass().equals(SeqNewickExproterController.class)){
			newModule = new SeqNewickExproterController(moduleNetwork, properties);
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
	public ModuleNetwork loadModuleTreeFromFile(File file) throws Exception {
				
		// Read JSON representation of the current module tree from file
		FileReader fileReader = new FileReader(file);
		ModuleNetwork loadedModuleTree = this.jsonConverter.fromJson(fileReader, ModuleNetwork.class);
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
		this.jsonConverter.toJson(this.moduleNetwork, fileWriter);
		
		// Close file writer
		fileWriter.close();
		
        // Write log message
        Logger.getLogger("").log(Level.INFO, "Successfully saved the module tree into the file "+file.getPath());
	}

}
