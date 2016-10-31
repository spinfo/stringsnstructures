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
import modules.basic_text_processing.ContextsModule;
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
import modules.input_output.MultiFileReaderModule;
import modules.input_output.SmbFileReaderModule;
import modules.input_output.SmbFileWriterModule;
import modules.kwip.KeyWordInPhraseModule;
import modules.kwip.KwipBowMatrixModule;
import modules.lfgroups.LFGroupBuildingModule;
import modules.matrix.BowTypeMatrixModule;
import modules.matrix.CosineDistanceModule;
import modules.matrix.MatrixBitwiseOperationModule;
import modules.matrix.MatrixColumnSumModule;
import modules.matrix.MatrixEliminateOppositionalValuesModule;
import modules.matrix.MatrixFilterModule;
import modules.matrix.MatrixRowColPairExtractorModule;
import modules.matrix.MclModule;
import modules.parser.oanc.OANCXMLParser;
import modules.segmentation.SegmentJoinerModule;
import modules.segmentation.SegmentMatrixModule;
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
		
		// Define available modules TODO Load at runtime
		
		/*
		 * INSTANTIATE MODULES BELOW
		 */
		
		// Prepare FileFinderModule module
		Properties oancProperties = new Properties();
		FileFinderModule fileFinderModule = new FileFinderModule(moduleNetwork, oancProperties);
		oancProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, fileFinderModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		fileFinderModule.applyProperties();
		
		// Prepare CosineDistanceModule module
		Properties cosineDistProperties = new Properties();
		CosineDistanceModule cosineDistanceModule = new CosineDistanceModule(moduleNetwork, cosineDistProperties);
		oancProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, cosineDistanceModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		cosineDistanceModule.applyProperties();

		// Prepare FileWriter module
		Properties fileWriterProperties = new Properties();
		FileWriterModule fileWriter = new FileWriterModule(moduleNetwork,
				fileWriterProperties);
		fileWriterProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, fileWriter.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		fileWriter.applyProperties();
		
		// Prepare MultiFileReader module
		Properties multiFileReaderProperties = new Properties();
		MultiFileReaderModule multiFileReader = new MultiFileReaderModule(moduleNetwork,
				multiFileReaderProperties);
		multiFileReaderProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, multiFileReader.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		multiFileReader.applyProperties();
		
		// Prepare ContextsModule module
		Properties contextsModuleProperties = new Properties();
		ContextsModule contextsModule = new ContextsModule(moduleNetwork, contextsModuleProperties);
		contextsModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, contextsModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		contextsModule.applyProperties();

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

		// Prepare ParadigmSegmenterModule module
		Properties paradigmenErmittlerModulProperties = new Properties();
		ParadigmSegmenterModule paradigmenErmittlerModul = new ParadigmSegmenterModule(moduleNetwork,
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

		// Prepare regex line filter module
		Properties regExLineFilterModuleProperties = new Properties();
		RegExLineFilterModule regExLineFilterModule = new RegExLineFilterModule(moduleNetwork,
				regExLineFilterModuleProperties);
		regExLineFilterModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, regExLineFilterModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		regExLineFilterModule.applyProperties();
		
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
		
		// Comparison module
		Properties comparisonModuleProperties = new Properties();
		ComparisonModule comparisonModule = new ComparisonModule(moduleNetwork, comparisonModuleProperties);
		comparisonModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, comparisonModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		comparisonModule.applyProperties();
		
		// Comparison module
		Properties caseChangerModuleProperties = new Properties();
		CaseChangerModule caseChangerModule = new CaseChangerModule(moduleNetwork, caseChangerModuleProperties);
		caseChangerModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, caseChangerModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		caseChangerModule.applyProperties();
		
		// KwipBowMatrixModule
		Properties kwipBowMatrixModuleProperties = new Properties();
		KwipBowMatrixModule kwipBowMatrixModule = new KwipBowMatrixModule(moduleNetwork, kwipBowMatrixModuleProperties);
		kwipBowMatrixModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, kwipBowMatrixModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		kwipBowMatrixModule.applyProperties();
		
		// Segmentation Check Module
		Properties segmentationCheckModuleProperties = new Properties();
		SegmentationCheckModule segmentationCheckModule = new SegmentationCheckModule(moduleNetwork, segmentationCheckModuleProperties);
		segmentationCheckModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, segmentationCheckModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		segmentationCheckModule.applyProperties();
		
		Properties matrixColumnSumModuleProperties = new Properties();
		MatrixColumnSumModule matrixColumnSumModule = new MatrixColumnSumModule(moduleNetwork, matrixColumnSumModuleProperties);
		matrixColumnSumModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, matrixColumnSumModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		matrixColumnSumModule.applyProperties();

		// Matrix Bitwise Transformation Module
		Properties matrixBitwiseModuleProperties = new Properties();
		MatrixBitwiseOperationModule matrixBitwiseModule = new MatrixBitwiseOperationModule(moduleNetwork, matrixBitwiseModuleProperties);
		matrixBitwiseModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, matrixBitwiseModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		matrixBitwiseModule.applyProperties();
		
		// Dot to Tree conversion module.
		Properties dot2TreeControllerProperties = new Properties();
		Dot2TreeController dot2TreeController = new Dot2TreeController(moduleNetwork, dot2TreeControllerProperties);
		dot2TreeControllerProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, dot2TreeController.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		dot2TreeController.applyProperties();
		
		
		// GST analysis tool "Tree Index Properties". TreeIndexController
		Properties treeIndexControllerProperties = new Properties();
		TreeIndexController treeIndexController = new TreeIndexController(moduleNetwork, treeIndexControllerProperties);
		treeIndexControllerProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, treeIndexController.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		treeIndexController.applyProperties();
		
		// MatrixEliminateOppositionalValuesModule
		Properties matrixEliminateOppositionalValuesModuleProperties = new Properties();
		MatrixEliminateOppositionalValuesModule matrixEliminateOppositionalValuesModule = new MatrixEliminateOppositionalValuesModule(moduleNetwork, matrixEliminateOppositionalValuesModuleProperties);
		matrixEliminateOppositionalValuesModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, matrixEliminateOppositionalValuesModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		matrixEliminateOppositionalValuesModule.applyProperties();
		
		// MatrixRowColPairExtractorModule
		Properties matrixRowColPairExtractorModuleProperties = new Properties();
		MatrixRowColPairExtractorModule matrixRowColPairExtractorModule = new MatrixRowColPairExtractorModule(moduleNetwork, matrixRowColPairExtractorModuleProperties);
		matrixRowColPairExtractorModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, matrixRowColPairExtractorModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		matrixRowColPairExtractorModule.applyProperties();

		// Join Module
		Properties joinModuleProperties = new Properties();
		JoinModule joinModule = new JoinModule(moduleNetwork, joinModuleProperties);
		joinModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, joinModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		joinModule.applyProperties();

		// BowTypeMatrixModule
		Properties bowTypeMatrixModuleProperties = new Properties();
		BowTypeMatrixModule bowTypeMatrixModule = new BowTypeMatrixModule(moduleNetwork, bowTypeMatrixModuleProperties);
		bowTypeMatrixModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, bowTypeMatrixModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		bowTypeMatrixModule.applyProperties();

		// BurrowsWheelerTransformationModule
		Properties burrowsWheelerTransformationModuleProperties = new Properties();
		BurrowsWheelerTransformationModule burrowsWheelerTransformationModule = new BurrowsWheelerTransformationModule(moduleNetwork, burrowsWheelerTransformationModuleProperties);
		burrowsWheelerTransformationModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, burrowsWheelerTransformationModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		burrowsWheelerTransformationModule.applyProperties();

		// Markov Clustering Module
		Properties mclProperties = new Properties();
		MclModule mclModule = new MclModule(moduleNetwork, mclProperties);
		mclProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, mclModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		mclModule.applyProperties();

		// CSV2GEXF Module
		Properties csv2GEXFModuleProperties = new Properties();
		CSV2GEXFModule csv2GEXFModule = new CSV2GEXFModule(moduleNetwork, csv2GEXFModuleProperties);
		csv2GEXFModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, csv2GEXFModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		csv2GEXFModule.applyProperties();

		// textSorterModule
		Properties textSorterModuleProperties = new Properties();
		TextSorterModule textSorterModule = new TextSorterModule(moduleNetwork, textSorterModuleProperties);
		textSorterModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, textSorterModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		textSorterModule.applyProperties();
		
		// LFGroupBuilding Module
		Properties lfgroupBuildingModuleProperties = new Properties();
		LFGroupBuildingModule lfgroupBuildingModule = new LFGroupBuildingModule(moduleNetwork, lfgroupBuildingModuleProperties);
		lfgroupBuildingModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, lfgroupBuildingModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		lfgroupBuildingModule.applyProperties();
		
		// BranchLengthGrouping
		Properties branchLengthGroupingProperties = new Properties();
		BranchLengthGrouping branchLengthGrouping = new BranchLengthGrouping(moduleNetwork, branchLengthGroupingProperties);
		branchLengthGroupingProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, branchLengthGrouping.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		branchLengthGrouping.applyProperties();
		
		// MatrixFilterModule
		Properties matrixFilterModuleProperties = new Properties();
		MatrixFilterModule matrixFilterModule = new MatrixFilterModule(moduleNetwork, matrixFilterModuleProperties);
		matrixFilterModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, matrixFilterModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		matrixFilterModule.applyProperties();
		
		// ExtensibleTreeNode2CSVModule
		Properties extensibleTreeNode2CSVModuleProperties = new Properties();
		ExtensibleTreeNode2CSVModule extensibleTreeNode2CSVModule = new ExtensibleTreeNode2CSVModule(moduleNetwork, extensibleTreeNode2CSVModuleProperties);
		extensibleTreeNode2CSVModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, extensibleTreeNode2CSVModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		extensibleTreeNode2CSVModule.applyProperties();
		
		// Motif Detection Module
		Properties motifDetectionControllerProperties = new Properties();
		MotifDetectionController motifDetectionController = new MotifDetectionController(moduleNetwork, motifDetectionControllerProperties);
		motifDetectionControllerProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, motifDetectionController.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		motifDetectionController.applyProperties();
		
		// SuffixTreeVector2CsvModule
		Properties suffixTreeVector2CsvModuleProperties = new Properties();
		SuffixTreeVector2CsvModule suffixTreeVector2CsvModule = new SuffixTreeVector2CsvModule(moduleNetwork, suffixTreeVector2CsvModuleProperties);
		suffixTreeVector2CsvModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, suffixTreeVector2CsvModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		suffixTreeVector2CsvModule.applyProperties();
		
		// TextReducerModule
		Properties textReducerModuleProperties = new Properties();
		TextReducerModule textReducerModule = new TextReducerModule(moduleNetwork, textReducerModuleProperties);
		textReducerModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, textReducerModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		textReducerModule.applyProperties();
		

		// MorphologyModule
		Properties resultToGeneralizedSuffixTreesMorphologyModuleProperties = new Properties();
		GeneralizedSuffixTreesMorphologyModule resultToGeneralizedSuffixTreesMorphologyModule = 
		new GeneralizedSuffixTreesMorphologyModule(moduleNetwork, resultToGeneralizedSuffixTreesMorphologyModuleProperties);
		resultToGeneralizedSuffixTreesMorphologyModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, 
		resultToGeneralizedSuffixTreesMorphologyModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		resultToGeneralizedSuffixTreesMorphologyModule.applyProperties();
				
		

		// ConsoleReaderModule
		Properties consoleReaderModuleProperties = new Properties();
		ConsoleReaderModule consoleReaderModule = new ConsoleReaderModule(moduleNetwork, consoleReaderModuleProperties);
		consoleReaderModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, consoleReaderModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		consoleReaderModule.applyProperties();

		// SegmentsTransitionNetworkModule
		Properties segmentsTransitionNetworkModuleProperties = new Properties();
		SegmentsTransitionNetworkModule segmentsTransitionNetworkModule = new SegmentsTransitionNetworkModule(moduleNetwork, segmentsTransitionNetworkModuleProperties);
		segmentsTransitionNetworkModuleProperties.setProperty(ModuleImpl.PROPERTYKEY_NAME, segmentsTransitionNetworkModule.getPropertyDefaultValues().get(ModuleImpl.PROPERTYKEY_NAME));
		segmentsTransitionNetworkModule.applyProperties();

				
		/*
		 * ADD MODULE INSTANCES TO LIST BELOW
		 */
		
		availableModules.put(contextsModule.getName(),contextsModule);
		availableModules.put(cosineDistanceModule.getName(),cosineDistanceModule);
		availableModules.put(multiFileReader.getName(),multiFileReader);
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
		availableModules.put(regExLineFilterModule.getName(), regExLineFilterModule);
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
		availableModules.put(comparisonModule.getName(), comparisonModule);
		availableModules.put(caseChangerModule.getName(), caseChangerModule);
		availableModules.put(kwipBowMatrixModule.getName(), kwipBowMatrixModule);
		availableModules.put(segmentationCheckModule.getName(), segmentationCheckModule);
		availableModules.put(matrixColumnSumModule.getName(), matrixColumnSumModule);
		availableModules.put(matrixBitwiseModule.getName(), matrixBitwiseModule);
		availableModules.put(dot2TreeController.getName(), dot2TreeController);
		availableModules.put(matrixEliminateOppositionalValuesModule.getName(), matrixEliminateOppositionalValuesModule);
		availableModules.put(matrixRowColPairExtractorModule.getName(), matrixRowColPairExtractorModule);
		availableModules.put(treeIndexController.getName(), treeIndexController);
		availableModules.put(joinModule.getName(), joinModule);
		availableModules.put(bowTypeMatrixModule.getName(), bowTypeMatrixModule);
		availableModules.put(burrowsWheelerTransformationModule.getName(), burrowsWheelerTransformationModule);
		availableModules.put(mclModule.getName(), mclModule);
		availableModules.put(csv2GEXFModule.getName(), csv2GEXFModule);
		availableModules.put(textSorterModule.getName(), textSorterModule);
		availableModules.put(lfgroupBuildingModule.getName(), lfgroupBuildingModule);
		availableModules.put(branchLengthGrouping.getName(), branchLengthGrouping);
		availableModules.put(matrixFilterModule.getName(), matrixFilterModule);
		availableModules.put(extensibleTreeNode2CSVModule.getName(), extensibleTreeNode2CSVModule);
		availableModules.put(motifDetectionController.getName(), motifDetectionController);
		availableModules.put(suffixTreeVector2CsvModule.getName(), suffixTreeVector2CsvModule);
		availableModules.put(textReducerModule.getName(), textReducerModule);
		availableModules.put(resultToGeneralizedSuffixTreesMorphologyModule.getName(), resultToGeneralizedSuffixTreesMorphologyModule);
		availableModules.put(consoleReaderModule.getName(), consoleReaderModule);
		availableModules.put(segmentsTransitionNetworkModule.getName(), segmentsTransitionNetworkModule);
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
		ModuleNetwork loadedModuleNetwork = null;
		try {
			loadedModuleNetwork = this.jsonConverter.fromJson(jsonString, ModuleNetwork.class);
		} catch (Exception e){
			Logger.getLogger("").log(Level.WARNING, "The specified module network seems to be invalid/out-of-date -- trying autoupdate.", e);
			try {
				loadedModuleNetwork = this.jsonConverter.fromJson(this.updateExpDefinition(jsonString), ModuleNetwork.class);
				Logger.getLogger("").log(Level.INFO, "Autoupdate successful -- please save the module network to make this change permanent.");
			} catch (Exception e1){
				Logger.getLogger("").log(Level.WARNING, "Autoupdate failed -- cannot load the specified module network.", e1);
				throw e1;
			}
		}
		
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
