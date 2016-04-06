package base.workbench;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import modules.Module;
import modules.ModuleImpl;
import modules.ModuleNetwork;
import modules.ModuleNetworkGsonSerializer;
import modules.ModuleTreeGsonDeserializer;
import modules.artificialSeqs.CreateArtificialSeqs;
import modules.artificialSeqs.CreateArtificialSeqsContent;
import modules.bagOfWords.BagsOfWordsDistancesModule;
import modules.bagOfWords.BagsOfWordsModule;
import modules.basemodules.BufferModule;
import modules.basemodules.ConsoleWriterModule;
import modules.basemodules.ExampleGsonDeserialization;
import modules.basemodules.ExampleGsonSerialization;
import modules.basemodules.ExampleModule;
//TODO: import your module
import modules.basemodules.ExampleRandString;
import modules.basemodules.ExternalCommandModule;
import modules.basemodules.FileFinderModule;
import modules.basemodules.FileReaderModule;
import modules.basemodules.FileWriterModule;
import modules.basemodules.FilterModule;
import modules.basemodules.RegExReplacementModule;
import modules.basemodules.ReverserModule;
import modules.basemodules.SmbFileReaderModule;
import modules.basemodules.SmbFileWriterModule;
import modules.hal.HalAdvancedModule;
import modules.keyWordInPhrase.KeyWordInPhraseModule;
import modules.neo4j.Neo4jOutputModule;
import modules.oanc.OANCXMLParser;
import modules.paradigmSegmenter.ParadigmenErmittlerModul;
import modules.plainText2TreeBuilder.PlainText2TreeBuilderConverter;
import modules.segmentationModules.SegmentJoinerModule;
import modules.segmentationModules.SegmentMatrixModule;
import modules.seqNewickExporter.SeqNewickExporterController;
import modules.seqNewickExporter.SeqNewickExporterControllerV2;
import modules.seqNewickExporter.SeqQueryController;
import modules.seqSplitting.SeqMemory;
import modules.seqSuffixTrie2SuffixTree.SeqSuffixTrie2SuffixTreeController;
import modules.seqTreeProperties.SeqTreePropController;
import modules.suffixNetBuilder.SuffixNetBuilderModule;
import modules.suffixTreeClusteringModuleWrapper.SuffixTreeClusteringModuleWrapper;
import modules.suffixTreeClusteringModuleWrapper.SuffixTreeClusteringWrapperV2;
import modules.suffixTreeModuleWrapper.GeneralisedSuffixTreeModule;
import modules.suffixTreeModuleWrapper.LabelDataMergeModule;
import modules.suffixTreeVectorizationWrapper.SuffixTreeVectorizationWrapperController;
import modules.treeBuilder.AtomicRangeSuffixTrieBuilder;
import modules.treeBuilder.ExtensibleTreeNode2GEXFModule;
import modules.treeBuilder.TreeBalanceIndexModule;
import modules.treeBuilder.TreeBuilder;
import modules.treeBuilder.TreeBuilderV2Module;
import modules.treeBuilder.TreeBuilderV3Module;
import modules.treeBuilder2Output.TreeBuilder2OutputController;
import modules.treeBuilder2Output.TreeBuilder2OutputControllerV2;
import modules.treeSimilarityClustering.GexfFilterModule;
import modules.treeSimilarityClustering.TreeSimilarityClusteringModule;
import modules.vectorAnalysis.MinkowskiDistanceMatrixModule;
import modules.vectorAnalysis.VectorAberrationCalculatorModule;
import modules.vectorAnalysis.VectorMedianCalculatorModule;
import modules.visualizationModules.ASCIIGraph;
import modules.visualizationModules.ColourGraph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import common.ListLoggingHandler;
import common.parallelization.CallbackReceiver;

public class ModuleWorkbenchController{ // TODO anderer Listener
	
	protected Map<String,Module> availableModules = new TreeMap<String,Module>(); // Key: module name
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
		gsonBuilder.registerTypeAdapter(ModuleNetwork.class, new ModuleTreeGsonDeserializer());
		this.jsonConverter = gsonBuilder.setPrettyPrinting().create();
		
		// Add jlist handler to logger
		this.listLoggingHandler = new ListLoggingHandler();
		Logger.getLogger("").addHandler(this.listLoggingHandler);
		
		// Define available modules TODO Load at runtime
		
		/*
		 * INSTANTIATE MODULES BELOW
		 */
		
		// Prepare FileFinderModule module
		Properties oancProperties = new Properties();
		FileFinderModule fileFinderModule = new FileFinderModule(moduleNetwork, oancProperties);
		oancProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, fileFinderModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		fileFinderModule.applyProperties();

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

		// Prepare FileFinderModule parser module
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
		
		// Prepare CreateArtificialSeqsContent module
		Properties createArtificialSeqsContentProperties = new Properties();
		CreateArtificialSeqsContent createArtificialSeqsContent = new CreateArtificialSeqsContent(moduleNetwork,
				createArtificialSeqsContentProperties);
		createArtificialSeqsContentProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, createArtificialSeqsContent.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		createArtificialSeqsContent.applyProperties();
		
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
		Properties seqNewickExporterControllerProperties = new Properties();
		SeqNewickExporterController seqNewickExporterController = new SeqNewickExporterController(moduleNetwork,
				seqNewickExporterControllerProperties);
		seqNewickExporterControllerProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, seqNewickExporterController.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		seqNewickExporterController.applyProperties();
		
		// Prepare seqNewickExporter version 2 module
		Properties seqNewickExporterControllerV2Properties = new Properties();
		SeqNewickExporterControllerV2 seqNewickExporterControllerV2 = new SeqNewickExporterControllerV2(moduleNetwork,
				seqNewickExporterControllerV2Properties);
		seqNewickExporterControllerV2Properties.setProperty(ModuleImpl.PROPERTYKEY_NAME, seqNewickExporterControllerV2.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		seqNewickExporterControllerV2.applyProperties();
		
		// Prepare HAL advanced module
		Properties halAdvancedModuleProperties = new Properties();
		HalAdvancedModule halAdvancedModule = new HalAdvancedModule(moduleNetwork,
				halAdvancedModuleProperties);
		halAdvancedModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, halAdvancedModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		halAdvancedModule.applyProperties();
		
		// Prepare regex replacement module
		Properties regExReplacementModuleProperties = new Properties();
		RegExReplacementModule regExReplacementModule = new RegExReplacementModule(moduleNetwork,
				regExReplacementModuleProperties);
		regExReplacementModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, regExReplacementModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		regExReplacementModule.applyProperties();
		
		// Prepare Bag Of Words module
		Properties bagOfWordsProperties = new Properties();
		BagsOfWordsModule bagOfWordsModule = new BagsOfWordsModule(moduleNetwork,
				bagOfWordsProperties);
		bagOfWordsProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, bagOfWordsModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		bagOfWordsModule.applyProperties();
		
		// Prepare filter module
		Properties filterProperties = new Properties();
		FilterModule filterModule = new FilterModule(moduleNetwork,
				filterProperties);
		filterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, filterModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		filterModule.applyProperties();
		
		// Prepare KWIP module
		Properties kwipProperties = new Properties();
		KeyWordInPhraseModule kwipModule = new KeyWordInPhraseModule(moduleNetwork,
				kwipProperties);
		kwipProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, kwipModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		kwipModule.applyProperties();
		
		// Prepare PlainText2TreeBuilderConverter module
		Properties PlainText2TreeBuilderConverterProperties = new Properties();
		PlainText2TreeBuilderConverter plainText2TreeBuilderConverter = new PlainText2TreeBuilderConverter (moduleNetwork,
				PlainText2TreeBuilderConverterProperties);
		PlainText2TreeBuilderConverterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, plainText2TreeBuilderConverter.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		plainText2TreeBuilderConverter.applyProperties();
		
		// Prepare treeBuilder2Output module
		Properties treeBuilder2OutputControllerProperties = new Properties();
		TreeBuilder2OutputController treeBuilder2OutputController = new TreeBuilder2OutputController (moduleNetwork,
				treeBuilder2OutputControllerProperties);
		treeBuilder2OutputControllerProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, treeBuilder2OutputController.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		treeBuilder2OutputController.applyProperties();
		
		// Prepare treeBuilder2OutputV2 module
		Properties treeBuilder2OutputV2ControllerProperties = new Properties();
		TreeBuilder2OutputControllerV2 treeBuilder2OutputControllerV2 = new TreeBuilder2OutputControllerV2 (moduleNetwork,
				treeBuilder2OutputV2ControllerProperties);
		treeBuilder2OutputV2ControllerProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, treeBuilder2OutputControllerV2.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		treeBuilder2OutputControllerV2.applyProperties();
		
		// Prepare GeneralisedSuffixTree module
		Properties generalisedSuffixTreeProperties = new Properties();
		GeneralisedSuffixTreeModule generalisedSuffixTreeModule = new GeneralisedSuffixTreeModule(moduleNetwork,
				generalisedSuffixTreeProperties);
		generalisedSuffixTreeProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, generalisedSuffixTreeModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		generalisedSuffixTreeModule.applyProperties();
		
		// Prepare BufferModule
		Properties bufferModuleProperties = new Properties();
		BufferModule bufferModule = new BufferModule(moduleNetwork,
				bufferModuleProperties);
		bufferModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, bufferModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		bufferModule.applyProperties();
		
		// Prepare SuffixTreeClusteringModuleWrapper module
		Properties suffixTreeClusteringModuleWrapperProperties = new Properties();
		SuffixTreeClusteringModuleWrapper suffixTreeClusteringModuleWrapper = new SuffixTreeClusteringModuleWrapper (moduleNetwork,
				suffixTreeClusteringModuleWrapperProperties);
		suffixTreeClusteringModuleWrapperProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, suffixTreeClusteringModuleWrapper.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		suffixTreeClusteringModuleWrapper.applyProperties();
		
		// Bag of Words module
		Properties bagsOfWordsDistancesModuleProperties = new Properties();
		BagsOfWordsDistancesModule bagsOfWordsDistancesModule  = new BagsOfWordsDistancesModule(moduleNetwork, 
				bagsOfWordsDistancesModuleProperties);
		bagsOfWordsDistancesModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, bagsOfWordsDistancesModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		bagsOfWordsDistancesModule.applyProperties();
		
		// Reverser module
		Properties reverserModuleProperties = new Properties();
		ReverserModule reverserModule  = new ReverserModule(moduleNetwork, 
				reverserModuleProperties);
		reverserModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, reverserModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		reverserModule.applyProperties();
		
		// TreeBalanceIndexModule
		Properties treeBalanceIndexModuleProperties = new Properties();
		TreeBalanceIndexModule treeBalanceIndexModule  = new TreeBalanceIndexModule(moduleNetwork, 
				treeBalanceIndexModuleProperties);
		treeBalanceIndexModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, treeBalanceIndexModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		treeBalanceIndexModule.applyProperties();
		
		// ExternalCommandModule
		Properties externalCommandModuleProperties = new Properties();
		ExternalCommandModule externalCommandModule  = new ExternalCommandModule(moduleNetwork, 
				externalCommandModuleProperties);
		externalCommandModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, externalCommandModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		externalCommandModule.applyProperties();
		
		// TreeBuilderV2Module
		Properties treeBuilderV2ModuleProperties = new Properties();
		TreeBuilderV2Module treeBuilderV2Module  = new TreeBuilderV2Module(moduleNetwork, 
				treeBuilderV2ModuleProperties);
		treeBuilderV2ModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, treeBuilderV2Module.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		treeBuilderV2Module.applyProperties();
		
		// TreeBuilderV2GSTModule
		Properties treeBuilderV2GSTModuleProperties = new Properties();
		TreeBuilderV3Module treeBuilderV2GSTModule  = new TreeBuilderV3Module(moduleNetwork, 
				treeBuilderV2GSTModuleProperties);
		treeBuilderV2GSTModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, treeBuilderV2GSTModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		treeBuilderV2GSTModule.applyProperties();
		
		// ExampleRandString
		Properties exampleRandStringProperties = new Properties();
		ExampleRandString exampleRandString  = new ExampleRandString(moduleNetwork, 
				exampleRandStringProperties);
		exampleRandStringProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, exampleRandString.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		exampleRandString.applyProperties();
		
		// ExampleGsonSerialization
		Properties exampleGsonSerializationProperties = new Properties();
		ExampleGsonSerialization exampleGsonSerialization  = new ExampleGsonSerialization(moduleNetwork, 
				exampleGsonSerializationProperties);
		exampleGsonSerializationProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, exampleGsonSerialization.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		exampleGsonSerialization.applyProperties();
		
		// ExampleGsonDeserialization
		Properties exampleGsonDeserializationProperties = new Properties();
		ExampleGsonDeserialization exampleGsonDeserialization  = new ExampleGsonDeserialization(moduleNetwork, 
				exampleGsonDeserializationProperties);
		exampleGsonDeserializationProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, exampleGsonDeserialization.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		exampleGsonDeserialization.applyProperties();
		
		// ExtensibleTreeNode2GEXFModule
		Properties extensibleTreeNode2GEXFModuleProperties = new Properties();
		ExtensibleTreeNode2GEXFModule extensibleTreeNode2GEXFModule  = new ExtensibleTreeNode2GEXFModule(moduleNetwork, 
				extensibleTreeNode2GEXFModuleProperties);
		extensibleTreeNode2GEXFModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, extensibleTreeNode2GEXFModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		extensibleTreeNode2GEXFModule.applyProperties();
		
		// SuffixTreeVectorizationWrapperController
		Properties suffixTreeVectorizationWrapperControllerProperties = new Properties();
		SuffixTreeVectorizationWrapperController suffixTreeVectorizationWrapperController  = new SuffixTreeVectorizationWrapperController(moduleNetwork, 
				suffixTreeVectorizationWrapperControllerProperties);
		suffixTreeVectorizationWrapperControllerProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, suffixTreeVectorizationWrapperController.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		suffixTreeVectorizationWrapperController.applyProperties();
		
		// SuffixTreeClusteringWrapperV2
		Properties suffixTreeClusteringWrapperV2Properties = new Properties();
		SuffixTreeClusteringWrapperV2 suffixTreeClusteringWrapperV2  = new SuffixTreeClusteringWrapperV2(moduleNetwork, 
				suffixTreeClusteringWrapperV2Properties);
		suffixTreeClusteringWrapperV2Properties.setProperty(ModuleImpl.PROPERTYKEY_NAME, suffixTreeClusteringWrapperV2.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		suffixTreeClusteringWrapperV2.applyProperties();

		// LabelDataMergeModule
		Properties labelDataMergeModuleProperties = new Properties();
		LabelDataMergeModule labelDataMergeModule = new LabelDataMergeModule(moduleNetwork, labelDataMergeModuleProperties);
		labelDataMergeModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, labelDataMergeModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		labelDataMergeModule.applyProperties();	

		// TreeSimilarityClusteringModule
		Properties treeSimilarityClusteringModuleProperties = new Properties();
		TreeSimilarityClusteringModule treeSimilarityClusteringModule = new TreeSimilarityClusteringModule(moduleNetwork, treeSimilarityClusteringModuleProperties);
		treeSimilarityClusteringModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, treeSimilarityClusteringModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		treeSimilarityClusteringModule.applyProperties();	
		
		// SeqQueryController
		Properties seqQueryControllerProperties = new Properties();
		SeqQueryController seqQueryController = new SeqQueryController(moduleNetwork, seqQueryControllerProperties);
		seqQueryControllerProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, seqQueryController.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		seqQueryController.applyProperties();	
		
		// VectorAberrationCalculatorModule
		Properties vectorAnalysisModuleProperties = new Properties();
		VectorAberrationCalculatorModule vectorAnalysisModule = new VectorAberrationCalculatorModule(moduleNetwork, vectorAnalysisModuleProperties);
		vectorAnalysisModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, vectorAnalysisModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		vectorAnalysisModule.applyProperties();
		
		// MinkowskiDistanceMatrixModule
		Properties minkowskiDistanceMatrixModuleProperties = new Properties();
		MinkowskiDistanceMatrixModule minkowskiDistanceMatrixModule = new MinkowskiDistanceMatrixModule(moduleNetwork, minkowskiDistanceMatrixModuleProperties);
		minkowskiDistanceMatrixModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, minkowskiDistanceMatrixModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		minkowskiDistanceMatrixModule.applyProperties();
		
		// VectorMedianCalculatorModule
		Properties vectorMedianCalculatorModuleProperties = new Properties();
		VectorMedianCalculatorModule vectorMedianCalculatorModule = new VectorMedianCalculatorModule(moduleNetwork, vectorMedianCalculatorModuleProperties);
		vectorMedianCalculatorModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, vectorMedianCalculatorModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		vectorMedianCalculatorModule.applyProperties();
		
		// GexfFilterModule
		Properties gexfFilterModuleProperties = new Properties();
		GexfFilterModule gexfFilterModule = new GexfFilterModule(moduleNetwork, gexfFilterModuleProperties);
		gexfFilterModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, gexfFilterModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		gexfFilterModule.applyProperties();
		
		// Segment neighbor joiner
		Properties segmentJoinerModuleProperties = new Properties();
		SegmentJoinerModule segmentJoinerModule = new SegmentJoinerModule(moduleNetwork, segmentJoinerModuleProperties);
		segmentJoinerModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, segmentJoinerModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		segmentJoinerModule.applyProperties();
		
		// Segment matrix
		Properties segmentMatrixModuleProperties = new Properties();
		SegmentMatrixModule segmentMatrixModule = new SegmentMatrixModule(moduleNetwork, segmentMatrixModuleProperties);
		segmentMatrixModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, segmentMatrixModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		segmentMatrixModule.applyProperties();
		
		/*
		 * ADD MODULE INSTANCES TO LIST BELOW
		 */
		
		availableModules.put(consoleWriter.getName(),consoleWriter);
		availableModules.put(exampleModule.getName(),exampleModule);
		availableModules.put(fileReader.getName(),fileReader);
		availableModules.put(smbFileReader.getName(),smbFileReader);
		availableModules.put(fileWriter.getName(),fileWriter);
		availableModules.put(smbFileWriter.getName(),smbFileWriter);
		availableModules.put(fileFinderModule.getName(),fileFinderModule);
		availableModules.put(oancParser.getName(),oancParser);
		availableModules.put(treeBuilder.getName(),treeBuilder);
		availableModules.put(atomicRangeSuffixTrieBuilder.getName(),atomicRangeSuffixTrieBuilder);
		availableModules.put(neo4jOutputModule.getName(),neo4jOutputModule);
		availableModules.put(suffixNetBuilderModule.getName(),suffixNetBuilderModule);
		availableModules.put(colourGraphModule.getName(),colourGraphModule);
		availableModules.put(asciiGraphModule.getName(),asciiGraphModule);
		availableModules.put(paradigmenErmittlerModul.getName(),paradigmenErmittlerModul);
		availableModules.put(createArtificialSeqs.getName(),createArtificialSeqs);
		availableModules.put(createArtificialSeqsContent.getName(),createArtificialSeqsContent);
		availableModules.put(seqMemory.getName(),seqMemory);
		availableModules.put(seqTreePropController.getName(),seqTreePropController);
		availableModules.put(seqSuffixTrie2SuffixTreeController.getName(),seqSuffixTrie2SuffixTreeController);
		availableModules.put(seqNewickExporterController.getName(),seqNewickExporterController);
		availableModules.put(seqNewickExporterControllerV2.getName(),seqNewickExporterControllerV2);
		availableModules.put(halAdvancedModule.getName(),halAdvancedModule);
		availableModules.put(regExReplacementModule.getName(),regExReplacementModule);
		availableModules.put(bagOfWordsModule.getName(), bagOfWordsModule);
		availableModules.put(filterModule.getName(), filterModule);
		availableModules.put(kwipModule.getName(), kwipModule);
		availableModules.put(plainText2TreeBuilderConverter.getName(), plainText2TreeBuilderConverter);
		availableModules.put(treeBuilder2OutputController.getName(), treeBuilder2OutputController);
		availableModules.put(treeBuilder2OutputControllerV2.getName(), treeBuilder2OutputControllerV2);
		availableModules.put(generalisedSuffixTreeModule.getName(), generalisedSuffixTreeModule);
		availableModules.put(bufferModule.getName(), bufferModule);
		availableModules.put(suffixTreeClusteringModuleWrapper.getName(), suffixTreeClusteringModuleWrapper);
		availableModules.put(bagsOfWordsDistancesModule.getName(), bagsOfWordsDistancesModule);
		availableModules.put(reverserModule.getName(), reverserModule);
		availableModules.put(treeBalanceIndexModule.getName(), treeBalanceIndexModule);
		availableModules.put(externalCommandModule.getName(), externalCommandModule);
		availableModules.put(treeBuilderV2Module.getName(), treeBuilderV2Module);
		availableModules.put(treeBuilderV2GSTModule.getName(), treeBuilderV2GSTModule);
		availableModules.put(exampleRandString.getName(), exampleRandString);
		availableModules.put(exampleGsonSerialization.getName(), exampleGsonSerialization);
		availableModules.put(exampleGsonDeserialization.getName(), exampleGsonDeserialization);
		availableModules.put(extensibleTreeNode2GEXFModule.getName(), extensibleTreeNode2GEXFModule);
		availableModules.put(suffixTreeVectorizationWrapperController.getName(), suffixTreeVectorizationWrapperController);
		availableModules.put(suffixTreeClusteringWrapperV2.getName(), suffixTreeClusteringWrapperV2);
		availableModules.put(labelDataMergeModule.getName(), labelDataMergeModule);
		availableModules.put(treeSimilarityClusteringModule.getName(), treeSimilarityClusteringModule);
		availableModules.put(seqQueryController.getName(), seqQueryController);
		availableModules.put(vectorAnalysisModule.getName(), vectorAnalysisModule);
		availableModules.put(minkowskiDistanceMatrixModule.getName(), minkowskiDistanceMatrixModule);
		availableModules.put(vectorMedianCalculatorModule.getName(), vectorMedianCalculatorModule);
		availableModules.put(gexfFilterModule.getName(), gexfFilterModule);
		availableModules.put(segmentJoinerModule.getName(), segmentJoinerModule);
		availableModules.put(segmentMatrixModule.getName(), segmentMatrixModule);
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
		FileReader fileReader = new FileReader(file);
		ModuleNetwork loadedModuleNetwork = this.jsonConverter.fromJson(fileReader, ModuleNetwork.class);
				
		// Close file writer
		fileReader.close();
		
		// Apply properties to modules
		Iterator<Module> modules = loadedModuleNetwork.getModuleList().iterator();
		while (modules.hasNext()){
			Module module = modules.next();
			module.applyProperties();
			if (!replaceCurrent && this.getModuleNetwork() != null)
				this.getModuleNetwork().addModule(module);
			Logger.getLogger("").log(Level.INFO, "Loaded module "+module.getName()+" ["+module.getClass().getSimpleName()+"]");
		}
		
		// Replace the current module network if specified to do so
		if (replaceCurrent || this.getModuleNetwork() == null)
			this.setModuleNetwork(loadedModuleNetwork);
		
        // Write log message
        Logger.getLogger("").log(Level.INFO, "Successfully loaded the module network from the file "+file.getPath());
        
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

}
