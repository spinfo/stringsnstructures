package base.workbench;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import common.ListLoggingHandler;
import common.parallelization.CallbackReceiver;
import modules.Module;
import modules.ModuleImpl;
import modules.ModuleNetwork;
import modules.ModuleNetworkGsonDeserializer;
import modules.ModuleNetworkGsonSerializer;
import modules.bag_of_words.BagsOfWordsDistancesModule;
import modules.bag_of_words.BagsOfWordsModule;
import modules.basic_text_processing.CaseChangerModule;
import modules.basic_text_processing.ComparisonModule;
import modules.basic_text_processing.FilterModule;
import modules.basic_text_processing.RegExLineFilterModule;
import modules.basic_text_processing.RegExReplacementModule;
import modules.basic_text_processing.ReverserModule;
import modules.basic_text_processing.TextSorterModule;
import modules.basic_text_processing.burrows_wheeler.BurrowsWheelerTransformationModule;
import modules.clustering.minkowskiDistance.MinkowskiDistanceMatrixModule;
import modules.clustering.suffixTreeClusteringModuleWrapper.SuffixTreeClusteringModuleWrapper;
import modules.clustering.suffixTreeClusteringModuleWrapper.SuffixTreeClusteringWrapperV2;
import modules.clustering.treeSimilarityClustering.TreeSimilarityClusteringModule;
import modules.examples.ExampleGsonDeserialization;
import modules.examples.ExampleGsonSerialization;
import modules.examples.ExampleModule;
import modules.examples.ExampleRandString;
import modules.experimental.CloudReaderModule;
import modules.experimental.CloudWriterModule;
import modules.experimental.suffixNetBuilder.SuffixNetBuilderModule;
import modules.format_conversion.CSV2GEXFModule;
import modules.format_conversion.ExtensibleTreeNode2CSVModule;
import modules.format_conversion.ExtensibleTreeNode2GEXFModule;
import modules.format_conversion.SuffixTreeVector2CsvModule;
import modules.format_conversion.TextReducerModule;
import modules.format_conversion.dot2tree.Dot2TreeController;
import modules.format_conversion.plainText2TreeBuilder.PlainText2TreeBuilderConverter;
import modules.format_conversion.seqNewickExporter.SeqNewickExporterController;
import modules.format_conversion.seqNewickExporter.SeqNewickExporterControllerV2;
import modules.format_conversion.treeBuilder2Output.TreeBuilder2OutputController;
import modules.format_conversion.treeBuilder2Output.TreeBuilder2OutputControllerV2;
import modules.generators.artificialSeqs.CreateArtificialSeqs;
import modules.generators.artificialSeqs.CreateArtificialSeqsContent;
import modules.graph_editing.GexfFilterModule;
import modules.hal.HalAdvancedModule;
import modules.input_output.BufferModule;
import modules.input_output.ConsoleReaderModule;
import modules.input_output.ConsoleWriterModule;
import modules.input_output.ExternalCommandModule;
import modules.input_output.FileFinderModule;
import modules.input_output.FileReaderModule;
import modules.input_output.FileWriterModule;
import modules.input_output.JoinModule;
import modules.input_output.SmbFileReaderModule;
import modules.input_output.SmbFileWriterModule;
import modules.kwip.KeyWordInPhraseModule;
import modules.kwip.KwipBowMatrixModule;
import modules.lfgroups.LFGroupBuildingModule;
import modules.matrix.BowTypeMatrixModule;
import modules.matrix.MatrixBitwiseOperationModule;
import modules.matrix.MatrixColumnSumModule;
import modules.matrix.MatrixEliminateOppositionalValuesModule;
import modules.matrix.MatrixFilterModule;
import modules.matrix.MatrixOperations;
import modules.matrix.MatrixRowColPairExtractorModule;
import modules.matrix.MatrixValuesExpressionApplyModule;
import modules.matrix.MatrixVectorSortModule;
import modules.matrix.MclModule;
import modules.matrix.SegmentMatrixAnalyzeModule;
import modules.matrix.distanceModule.DistanceMatrixModule;
import modules.morphology.MorphologyCheckModule;
import modules.parser.oanc.OANCXMLParser;
import modules.segmentation.SegmentCombinerModule;
import modules.segmentation.SegmentDistanceMatrixModule;
import modules.segmentation.SegmentJoinerModule;
import modules.segmentation.SegmentMatrixModule;
import modules.segmentation.SegmentationApplyModule;
import modules.segmentation.SegmentationCheckModule;
import modules.segmentation.SegmentsTransitionNetworkModule;
import modules.segmentation.paradigmSegmenter.ParadigmSegmenterModule;
import modules.segmentation.seqSplitting.SeqMemory;
import modules.tree_building.suffixTreeModuleWrapper.GeneralisedSuffixTreeModule;
import modules.tree_building.suffixTreeModuleWrapper.GeneralizedSuffixTreesMorphologyModule;
import modules.tree_building.treeBuilder.AtomicRangeSuffixTrieBuilder;
import modules.tree_building.treeBuilder.TreeBuilder;
import modules.tree_building.treeBuilder.TreeBuilderV2Module;
import modules.tree_building.treeBuilder.TreeBuilderV3Module;
import modules.tree_editing.LabelDataMergeModule;
import modules.tree_editing.seqNewick.SeqQueryController;
import modules.tree_editing.seqSuffixTrie2SuffixTree.SeqSuffixTrie2SuffixTreeController;
import modules.tree_properties.branchLengthGroups.BranchLengthGrouping;
import modules.tree_properties.motifDetection.MotifDetectionController;
import modules.tree_properties.seqTreeProperties.SeqTreePropController;
import modules.tree_properties.treeIndexes.TreeIndexController;
import modules.vectorization.VectorAberrationCalculatorModule;
import modules.vectorization.VectorMedianCalculatorModule;
import modules.vectorization.suffixTreeVectorizationWrapper.SuffixTreeVectorizationWrapperController;
import modules.visualization.ASCIIGraph;
import modules.visualization.ColourGraph;

public class ModuleWorkbenchController{ // TODO anderer Listener
	
	public static final String LINEBREAKREGEX = "\\R+";
	public static final String LINEBREAK = "\n";
	
	protected Map<String,Module> availableModules = new TreeMap<String,Module>(); // Key: module name

	public static String moduleNetWorkName="";
	private ModuleNetwork moduleNetwork;

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
		gsonBuilder.registerTypeAdapter(ModuleNetwork.class, new ModuleNetworkGsonDeserializer());
		this.jsonConverter = gsonBuilder.setPrettyPrinting().create();
		
		// Add jlist handler to logger
		this.listLoggingHandler = new ListLoggingHandler();
		Logger.getLogger("").addHandler(this.listLoggingHandler);

		/*
		 * INSTANTIATE MODULES BELOW
		 */
		createAndRegisterModule(FileFinderModule.class);
		createAndRegisterModule(FileWriterModule.class);
		createAndRegisterModule(SmbFileWriterModule.class);
		createAndRegisterModule(OANCXMLParser.class);
		createAndRegisterModule(FileReaderModule.class);
		createAndRegisterModule(SmbFileReaderModule.class);
		createAndRegisterModule(ConsoleWriterModule.class);
		createAndRegisterModule(ConsoleReaderModule.class);
		createAndRegisterModule(ExampleModule.class);
		createAndRegisterModule(TreeBuilder.class);
		createAndRegisterModule(AtomicRangeSuffixTrieBuilder.class);
		createAndRegisterModule(SuffixNetBuilderModule.class);
		createAndRegisterModule(ColourGraph.class);
		createAndRegisterModule(ASCIIGraph.class);
		createAndRegisterModule(ParadigmSegmenterModule.class);
		createAndRegisterModule(CreateArtificialSeqs.class);
		createAndRegisterModule(CreateArtificialSeqsContent.class);
		createAndRegisterModule(SeqMemory.class);
		createAndRegisterModule(SeqTreePropController.class);
		createAndRegisterModule(SeqSuffixTrie2SuffixTreeController.class);
		createAndRegisterModule(SeqNewickExporterController.class);
		createAndRegisterModule(SeqNewickExporterControllerV2.class);
		createAndRegisterModule(HalAdvancedModule.class);
		createAndRegisterModule(RegExReplacementModule.class);
		createAndRegisterModule(RegExLineFilterModule.class);
		createAndRegisterModule(BagsOfWordsModule.class);
		createAndRegisterModule(FilterModule.class);
		createAndRegisterModule(KeyWordInPhraseModule.class);
		createAndRegisterModule(PlainText2TreeBuilderConverter.class);
		createAndRegisterModule(TreeBuilder2OutputController.class);
		createAndRegisterModule(TreeBuilder2OutputControllerV2.class);
		createAndRegisterModule(GeneralisedSuffixTreeModule.class);
		createAndRegisterModule(BufferModule.class);
		createAndRegisterModule(SuffixTreeClusteringModuleWrapper.class);
		createAndRegisterModule(BagsOfWordsDistancesModule.class);
		createAndRegisterModule(ReverserModule.class);
		createAndRegisterModule(ExternalCommandModule.class);
		createAndRegisterModule(TreeBuilderV2Module.class);
		createAndRegisterModule(TreeBuilderV3Module.class);
		createAndRegisterModule(ExampleRandString.class);
		createAndRegisterModule(ExampleGsonSerialization.class);
		createAndRegisterModule(ExampleGsonDeserialization.class);
		createAndRegisterModule(ExtensibleTreeNode2GEXFModule.class);
		createAndRegisterModule(SuffixTreeVectorizationWrapperController.class);
		createAndRegisterModule(SuffixTreeClusteringWrapperV2.class);
		createAndRegisterModule(LabelDataMergeModule.class);
		createAndRegisterModule(TreeSimilarityClusteringModule.class);
		createAndRegisterModule(SeqQueryController.class);
		createAndRegisterModule(VectorAberrationCalculatorModule.class);
		createAndRegisterModule(MinkowskiDistanceMatrixModule.class);
		createAndRegisterModule(VectorMedianCalculatorModule.class);
		createAndRegisterModule(GexfFilterModule.class);
		createAndRegisterModule(SegmentJoinerModule.class);
		createAndRegisterModule(SegmentMatrixModule.class);
		createAndRegisterModule(ComparisonModule.class);
		createAndRegisterModule(CaseChangerModule.class);
		createAndRegisterModule(KwipBowMatrixModule.class);
		createAndRegisterModule(SegmentationCheckModule.class);
		createAndRegisterModule(MatrixColumnSumModule.class);
		createAndRegisterModule(MatrixBitwiseOperationModule.class);
		createAndRegisterModule(Dot2TreeController.class);
		createAndRegisterModule(TreeIndexController.class);
		createAndRegisterModule(MatrixEliminateOppositionalValuesModule .class);
		createAndRegisterModule(MatrixRowColPairExtractorModule.class);
		createAndRegisterModule(JoinModule.class);
		createAndRegisterModule(BowTypeMatrixModule.class);
		createAndRegisterModule(BurrowsWheelerTransformationModule.class);
		createAndRegisterModule(MclModule.class);
		createAndRegisterModule(CSV2GEXFModule.class);
		createAndRegisterModule(TextSorterModule.class);
		createAndRegisterModule(LFGroupBuildingModule.class);
		createAndRegisterModule(BranchLengthGrouping.class);
		createAndRegisterModule(MatrixFilterModule.class);
		createAndRegisterModule(ExtensibleTreeNode2CSVModule.class);
		createAndRegisterModule(MotifDetectionController.class);
		createAndRegisterModule(SuffixTreeVector2CsvModule.class);
		createAndRegisterModule(TextReducerModule.class);
		createAndRegisterModule(GeneralizedSuffixTreesMorphologyModule.class);
		createAndRegisterModule(SegmentsTransitionNetworkModule.class);
		createAndRegisterModule(SegmentDistanceMatrixModule.class);
		createAndRegisterModule(SegmentCombinerModule.class);
		createAndRegisterModule(SegmentationApplyModule.class);
		createAndRegisterModule(SegmentMatrixAnalyzeModule.class);
		createAndRegisterModule(MorphologyCheckModule.class);
		createAndRegisterModule(MatrixValuesExpressionApplyModule.class);
		createAndRegisterModule(MatrixOperations.class);
		createAndRegisterModule(DistanceMatrixModule.class);
		createAndRegisterModule(MatrixVectorSortModule.class);
		createAndRegisterModule(CloudReaderModule.class);
		createAndRegisterModule(CloudWriterModule.class);
	}
	
	/**
	 * Instantiates a new module of the desired class along with a Properties object and makes
	 * it available to the workbench.
	 * @param clazz
	 * 		A class of the desired type extending ModuleImpl and having a Constructor with
	 * 		signature (CallbackReceiver, Properties)
	 */
	private void createAndRegisterModule(Class<? extends ModuleImpl> clazz) throws Exception {
		// initiate a new Properties object for the module
		Properties properties = new Properties();
		
		// get the correct constructor of the module class
		Constructor<? extends ModuleImpl> constructor = clazz.getDeclaredConstructor(CallbackReceiver.class, Properties.class);
		
		// instantiate the new module of the given class clazz
		ModuleImpl module = constructor.newInstance(moduleNetwork, properties);
		
		// set the name value of the new module in it's properties
		properties.setProperty(ModuleImpl.PROPERTYKEY_NAME, module.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		
		// apply Properties of the specific module
		module.applyProperties();
		
		// make the module available in the workbench
		availableModules.put(module.getName(), module);
	}
	
	/**
	 * Clears the current module network.
	 */
	public void clearModuleNetwork(){
		
		// Remove all module nodes
		this.moduleNetwork.removeAllModules();
	}

	/**
	 * @return the moduleNetwork
	 */
	public ModuleNetwork getModuleNetwork() {
		return moduleNetwork;
	}

	/**
	 * @param moduleNetwork the moduleNetwork to set
	 */
	public void setModuleNetwork(ModuleNetwork moduleNetwork) {
		this.moduleNetwork = moduleNetwork;
	}
	
	/**
	 * Returns a new instance of the module with the specified name.
	 * @param moduleName Name of module to instantiate
	 * @return new module instance
	 * @throws Exception Thrown if something goes wrong
	 */
	public Module getNewInstanceOfModule(String moduleName) throws Exception{
		return this.getNewInstanceOfModule(this.availableModules.get(moduleName));
	}
	
	/**
	 * Returns a new instance of the specified module.
	 * @param module Name of module to instantiate
	 * @return new module instance
	 * @throws Exception Thrown if something goes wrong
	 */
	public Module getNewInstanceOfModule(Module module) throws Exception{
		
		// If there is no module network, throw an exception
		if (this.moduleNetwork == null)
			throw new Exception("There does not seem to be a module network I can bind a new module to.");
		
		// If specified module is null, throw exception
		if (module == null || !this.availableModules.containsKey(module.getName()))
			throw new Exception("I do not know the specified module template.");
		
		// Template module
		Module templateModule = this.availableModules.get(module.getName());
		
		// Transfer module properties from template to new instance (via new properties instance)
		Properties properties = new Properties();
		Iterator<Object> propertyKeys = templateModule.getProperties().keySet().iterator();
		while(propertyKeys.hasNext()){
			String propertyKey = propertyKeys.next().toString();
			Object propertyValue = templateModule.getProperties().get(propertyKey);
			if (propertyValue != null)
				properties.setProperty(propertyKey, propertyValue.toString());
		}
		
		// Determine the constructor of the module and return a new instance
		Constructor <? extends Module> moduleConstructor = templateModule.getClass().getConstructor(CallbackReceiver.class, Properties.class);
		return moduleConstructor.newInstance(this.moduleNetwork, properties);
	}

	/**
	 * @return the availableModules
	 */
	public Map<String,Module> getAvailableModules() {
		return availableModules;
	}

	/**
	 * @return the listLoggingHandler
	 */
	public ListLoggingHandler getListLoggingHandler() {
		return listLoggingHandler;
	}
	
	/**
	 * Loads the module network from a file and adds it to the current one.
	 * @param file file
	 * @return Loaded module tree
	 * @throws Exception Thrown if something goes wrong
	 */
	public ModuleNetwork loadModuleNetworkFromFile(File file) throws Exception {
		return this.loadModuleNetworkFromFile(file, false);
	}
	
	/**
	 * Loads the module network from a file.
	 * @param file file
	 * @param replaceCurrent If true, replaces the current module network
	 * @return Loaded module tree
	 * @throws Exception Thrown if something goes wrong
	 */
	public ModuleNetwork loadModuleNetworkFromFile(File file, boolean replaceCurrent) throws Exception {

		// Read JSON representation of the current module tree from file
		byte[] encoded = Files.readAllBytes(file.toPath());
		String jsonString = new String(encoded);

		ModuleNetwork result = loadModuleNetworkFromString(jsonString, replaceCurrent);

		// If we get here, loading went ok, so log it and return
		Logger.getLogger("").log(Level.INFO, "Successfully loaded the module network from the file " + file.getPath());
		moduleNetWorkName = file.getPath();
		return result;
	}
	
	/**
	 * Loads the module network from a json string.
	 * @param jsonString A json representation of the ModuleNetwork, e.g. by <class>ModuleNetworkGsonSerializer</class>
	 * @param replaceCurrent If true, replaces the current module network
	 * @return Loaded module tree
	 * @throws Exception Thrown if something goes wrong
	 */
	public ModuleNetwork loadModuleNetworkFromString(String jsonString, boolean replaceCurrent) throws Exception {
		ModuleNetwork loadedModuleNetwork = null;
		try {
			loadedModuleNetwork = this.jsonConverter.fromJson(jsonString, ModuleNetwork.class);
		} catch (Exception e) {
			Logger.getLogger("").log(Level.WARNING,
					"The specified module network seems to be invalid/out-of-date -- trying autoupdate.", e);
			try {
				loadedModuleNetwork = this.jsonConverter.fromJson(this.updateExpDefinition(jsonString),
						ModuleNetwork.class);
				Logger.getLogger("").log(Level.INFO,
						"Autoupdate successful -- please save the module network to make this change permanent.");
			} catch (Exception e1) {
				Logger.getLogger("").log(Level.WARNING,
						"Autoupdate failed -- cannot load the specified module network.", e1);
				throw e1;
			}
		}

		// Apply properties to modules
		Iterator<Module> modules = loadedModuleNetwork.getModuleList().iterator();
		while (modules.hasNext()) {
			Module module = modules.next();
			module.applyProperties();
			if (!replaceCurrent && this.getModuleNetwork() != null)
				this.getModuleNetwork().addModule(module);
			Logger.getLogger("").log(Level.INFO,
					"Loaded module " + module.getName() + " [" + module.getClass().getSimpleName() + "]");
		}

		// Replace the current module network if specified to do so
		if (replaceCurrent || this.getModuleNetwork() == null)
			this.setModuleNetwork(loadedModuleNetwork);

		// Return the loaded network
		return loadedModuleNetwork;
	}
	
	/**
	 * Saves the current module tree to a file.
	 * @param file File to save to
	 * @throws Exception Thrown if something goes wrong
	 * @throws JsonIOException Thrown if serialising fails
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
	
	/**
	 * Updates the specified EXP file, searching invalid module classes and replacing them with valid ones (if possible). 
	 * @param input Input file
	 * @param output Output file
	 * @throws IOException Thrown on I/O error
	 */
	public void updateExpFile(File input, File output) throws IOException{
		byte[] encoded = Files.readAllBytes(input.toPath());
		String outString = this.updateExpDefinition(new String(encoded));
		Files.write(output.toPath(), outString.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
	}
	
	/**
	 * Updates the specified JSON string, searching invalid module classes and replacing them with valid ones (if possible). 
	 * @param jsonString JSON string
	 * @return updated JSON string
	 */
	public String updateExpDefinition(String jsonString){
		StringBuffer returnBuffer = new StringBuffer();
		
		Scanner lineScanner = new Scanner(new StringReader(jsonString));
		lineScanner.useDelimiter("\\R+");
		while(lineScanner.hasNext()){
			String line = lineScanner.next();
			if (line.matches("^[\\s]+\\\"moduleCanonicalClassName\\\"\\:.*$")){
				// Determine whether module class is valid
				String className = line.substring(line.indexOf("\"", line.indexOf(":"))+1, line.lastIndexOf('"'));
				try {
					Class.forName(className);
				} catch (ClassNotFoundException e){
					// Search for replacement class
					Class<?> moduleClass = null;
					Iterator<Module> modules = this.availableModules.values().iterator();
					while (modules.hasNext()){
						Module module = modules.next();
						if (module.getClass().getSimpleName().equals(className.substring(className.lastIndexOf('.')+1))){
							moduleClass = module.getClass();
							break;
						}
					}
					
					if (moduleClass != null){
						Logger.getLogger("").log(Level.INFO, "The module class '"+className+"' will be replaced by '"+moduleClass.getCanonicalName()+"'.");
						line = line.replace(className, moduleClass.getCanonicalName());
					} else {
						Logger.getLogger("").log(Level.WARNING, "The module class '"+className+"' is missing and a substitute could not be found.");
					}
				}
			}
			returnBuffer.append(line+"\n");
		}
		lineScanner.close();
		
		return returnBuffer.toString();
	}

}
