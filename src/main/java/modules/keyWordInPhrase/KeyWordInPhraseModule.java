package modules.keyWordInPhrase;

//   
//
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import common.parallelization.CallbackReceiver;

public class KeyWordInPhraseModule extends ModuleImpl {

	/*
	 * Various variables
	 */
	private static final String POSITION = "Position";
	private static final String SOURCE = "Source";
	private static final String TEXT = "Text";
	private static final String CONTEXT = "Context";
	private static final String LEFT_CONTEXT = "LeftContext";
	private static final String RIGHT_CONTEXT = "RightContext";

	private static final Logger LOGGER = Logger.getGlobal();

	private StandardAnalyzer analyzer;
	private StringBuffer resultBuf;
	private StringBuffer prettyBuf;
	private StringBuffer xmlBuf;
	
	/*
	 * Variables related to modularisation
	 */
	// Define I/O IDs (must be unique for every input or output)
	private final String INPUT1ID = "plain";
	private final String OUTPUTUNITID = "units";
	private final String OUTPUTTYPEID = "types";
	private final String OUTPUTPLAINID = "plain";
	private final String OUTPUTHTMLID = "html";
	private final String OUTPUTXMLID = "xml";

	/**
	 * Constructor
	 * @param callbackReceiver Target of thread callback
	 * @param properties Module properties
	 * @throws Exception Thrown upon error
	 */
	public KeyWordInPhraseModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		
		// Call parent constructor
		super(callbackReceiver, properties);

		// Add module description
		this.setDescription("KeyWord In Phrase Module.");
		
		// Add property defaults
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "KWIP Module");

		// Define I/O
		InputPort inputPort1 = new InputPort(INPUT1ID,
				"[text/plain] Input for preprocessed plaintext.", this);
		inputPort1.addSupportedPipe(CharPipe.class);
		OutputPort unitOutputPort = new OutputPort(OUTPUTUNITID,
				"[text/plain] Outputs a line-by-line list of units.", this);
		unitOutputPort.addSupportedPipe(CharPipe.class);
		OutputPort typeOutputPort = new OutputPort(OUTPUTTYPEID,
				"[text/plain] Outputs a line-by-line list of types.", this);
		typeOutputPort.addSupportedPipe(CharPipe.class);
		OutputPort plainOutputPort = new OutputPort(OUTPUTPLAINID,
				"[text/plain] Outputs a plaintext representation of the KWIP result.", this);
		plainOutputPort.addSupportedPipe(CharPipe.class);
		OutputPort htmlOutputPort = new OutputPort(OUTPUTHTMLID,
				"[text/html] Outputs an HTML representation of the KWIP result.", this);
		htmlOutputPort.addSupportedPipe(CharPipe.class);
		OutputPort xmlOutputPort = new OutputPort(OUTPUTXMLID,
				"[text/xml] Outputs an XML representation of the KWIP result.", this);
		xmlOutputPort.addSupportedPipe(CharPipe.class);

		// Add I/O ports to instance (don't forget...)
		super.addInputPort(inputPort1);
		super.addOutputPort(unitOutputPort);
		super.addOutputPort(typeOutputPort);
		super.addOutputPort(plainOutputPort);
		super.addOutputPort(htmlOutputPort);
		super.addOutputPort(xmlOutputPort);
	}

	/**
	 * Adds a document to lucene index
	 * @param w index writer
	 * @param token token
	 * @param context context
	 * @param line line number
	 * @throws IOException Thrown if an I/O error occurs
	 */
	private void addDoc(IndexWriter w, String token, String context,
			int line) throws IOException {
		Document doc = new Document();
		// A text field will be tokenized
		// ******************************************************************
		FieldType fieldType = new FieldType();
		fieldType.setIndexed(true);
		fieldType
				.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		fieldType.setStored(true);
		fieldType.setStoreTermVectors(true);

		fieldType.setStoreTermVectorPositions(true);
		fieldType.setStoreTermVectorOffsets(true);
		Field field = new Field(TEXT, token, fieldType);
		doc.add(field);
		// ******************************************************************
		doc.add(new StringField(CONTEXT, context, Field.Store.YES));
		// ******************************************************************
		// a field for source because not to be tokenized
		doc.add(new IntField(SOURCE, line, Field.Store.YES));

		// a field for the token's position in the text
		int pos = context.toLowerCase().indexOf(token.toLowerCase());
		doc.add(new IntField(POSITION, pos, Field.Store.YES));

		String leftContext = getLeftContext(context, token);
		doc.add(new StringField(LEFT_CONTEXT, leftContext, Field.Store.YES));

		String rightContext = getRightContext(context, token);
		doc.add(new StringField(RIGHT_CONTEXT, rightContext, Field.Store.YES));

		w.addDocument(doc);
	}

	/**
	 * Determine righthand context
	 * @param text text
	 * @param token token
	 * @return righthand context
	 */
	private String getRightContext(String text, String token) {
		int pos = text.toLowerCase().indexOf(token.toLowerCase());
		return text.substring(pos + token.length(), text.length());
	}

	/**
	 * Determine lefthand context
	 * @param text text
	 * @param token token
	 * @return lefthand context
	 */
	private String getLeftContext(String text, String token) {
		int pos = text.toLowerCase().indexOf(token.toLowerCase());
		return text.substring(0, pos);
	}

	/**
	 * Generates a lucene index from the given input.
	 * @param bufreader Reader to obtain input from
	 * @return lucene directory
	 * @throws Exception Thrown if an error occurs
	 */
	private Directory generateIndex(BufferedReader bufreader) throws Exception {
		Directory index = null;
		// Specify the analyzer for tokenizing text.
		// The same analyzer should be used for indexing and searching
		analyzer = new StandardAnalyzer(Version.LUCENE_46);

		// Code to create the index
		index = new RAMDirectory();

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46,
				analyzer);
		IndexWriter w = new IndexWriter(index, config);
		// ----------------------------------------------------------------------
		int lineNr = 0;

		String line; // line = sentence
		while ((line = bufreader.readLine()) != null) {
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			LOGGER.fine(String.valueOf(lineNr) + " " + line);
			List<String> tokenList = TokenizeString.tokenizeString(analyzer,
					line);
			for (String token : tokenList) {
				LOGGER.fine(token + "\t " + line);

				// each token is one document
				addDoc(w, token, line, lineNr);
			}

			lineNr++;
		}
		w.close();
		return index;
	}

	/**
	 * Determine types
	 * @param index lucene index
	 * @param reader index reader
	 * @throws Exception Thrown if an error occurs
	 */
	private void types(Directory index, IndexReader reader) throws Exception {
		ArrayList<Integer> unitList = new ArrayList<Integer>();
		ArrayList<String> typeList = new ArrayList<String>();
		int units = 0;
		Terms terms = SlowCompositeReaderWrapper.wrap(reader).terms(TEXT);
		TermsEnum termEnum = null;
		termEnum = terms.iterator(termEnum);

		String tabs = "\t\t";
		int len = 0;
		int currentPos = 0;
		int typeNr = 0;
		while (termEnum.next() != null) {
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			String type = termEnum.term().utf8ToString();

			xmlBuf.append("\t<type ").append("text=\"").append(type)
					.append("\" ").append("id=\"").append(typeNr)
					.append("\">\n");

			if (type.length() < 8)
				len = 2;
			else
				len = 1;
			LOGGER.finest("types type: " + type + tabs.substring(0, len)
					+ "Freq.: " + termEnum.docFreq() + "\n");
			// exclude numbers and numberstrings
			if ((type.charAt(0) < '0') || (type.charAt(0) > '9')) {
				currentPos = tokens(reader, type, currentPos);
				units = units + termEnum.docFreq();
				unitList.add(units);
				typeList.add(type);
				LOGGER.finest("units: " + units);
			}
			xmlBuf.append("\t</type>\n");
			typeNr++;
		}

		outputUnitList(unitList);
		outputTypeList(typeList);
	}

	/**
	 * Determine tokens
	 * @param reader index reader
	 * @param querystr query string
	 * @param currentPos current position
	 * @return
	 * @throws Exception Exception Thrown if an error occurs
	 */
	private int tokens(IndexReader reader, String querystr, int currentPos)
			throws Exception {
		int lastSource = -1;

		int hitsPerPage = 100000;
		IndexSearcher searcher = new IndexSearcher(reader);

		// The \"title\" arg specifies the default field to use when no
		// field is explicitly specified in the query
		Query query = new QueryParser(Version.LUCENE_46, TEXT, analyzer)
				.parse(querystr);

		TopScoreDocCollector collector = TopScoreDocCollector.create(
				hitsPerPage, true);

		searcher.search(query, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		// Code to display the results of search

		for (int i = 0; i < hits.length; ++i) {
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			xmlBuf.append("\t\t<token>\n");
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			StringBuffer context = new StringBuffer(d.get(CONTEXT));
			xmlBuf.append("\t\t\t<context>").append(context)
					.append("</context>\n");

			int tokenPosition = d.getField(POSITION).numericValue().intValue();
			LOGGER.fine("position of token in context: " + tokenPosition);

			LOGGER.finer("tokens querystr: " + querystr + " context: "
					+ context /* d.get("Context") */
					+ " source: " + d.get(SOURCE));
			xmlBuf.append("\t\t\t<contextStart>").append(currentPos)
					.append("</contextStart>\n");
			xmlBuf.append("\t\t\t<position>")
					.append(currentPos + tokenPosition).append("</position>\n");
			resultBuf.append(context);// d.get("Context"));
			currentPos += context.length();
			xmlBuf.append("\t\t\t<contextEnd>").append(currentPos)
					.append("</contextEnd>\n");
			prettyBuf.append(pretty(context, querystr));

			if (lastSource == Integer.parseInt(d.get(SOURCE))) {
				// int i1= 10/0;
			}

			lastSource = Integer.parseInt(d.get(SOURCE));
			xmlBuf.append("\t\t\t<source>").append(lastSource)
					.append("</source>\n");

			String leftContext = d.get(LEFT_CONTEXT);
			xmlBuf.append("\t\t\t<left>").append(leftContext)
					.append("</left>\n");
			String rightContext = d.get(RIGHT_CONTEXT);
			xmlBuf.append("\t\t\t<right>").append(rightContext)
					.append("</right>\n");

			xmlBuf.append("\t\t</token>\n");
		}

		return currentPos;
	}

	/**
	 * Pretty print in HTML
	 * @param buf buffer
	 * @param type type
	 * @return pretty HTML
	 */
	private StringBuffer pretty(StringBuffer buf, String type) {
		int resPosition;
		int start = 0;
		while ((resPosition = buf.indexOf(type, start)) != -1) {
			buf.insert(resPosition, "<b>");
			buf.insert(resPosition + type.length() + 3, "</b>");
			start = resPosition + type.length() + 7;
		}
		// capital letter
		StringBuilder stType = new StringBuilder(type);
		stType.setCharAt(0, Character.toUpperCase(type.charAt(0)));
		type = stType.toString();
		start = 0;
		while ((resPosition = buf.indexOf(type, start)) != -1) {
			buf.insert(resPosition, "<b>");
			buf.insert(resPosition + type.length() + 3, "</b>");
			start = resPosition + type.length() + 7;
		}
		buf.append("<br />");
		return buf;
	}

	/**
	 * Outputs the specified unit list.
	 * @param unitList unit list
	 * @throws IOException Thrown if I/O error happens
	 */
	private void outputUnitList(ArrayList<Integer> unitList) throws IOException {
		/*
		 * a unit is the number of tokens for a type; a unit is defined as the
		 * last token of a type. Its value is a int, counting from the last
		 * token of the first type to the next last token of the next type (e.g.
		 * 3 (if there are three tokens of the first type), 5 (if there are two
		 * tokens of the second type), ...
		 */
		for (int i = 0; i < unitList.size(); i++) {
			this.getOutputPorts()
					.get(OUTPUTUNITID)
					.outputToAllCharPipes(
							String.valueOf(unitList.get(i)) + "\n");
		}
	}
	
	/**
	 * Outputs the specified unit list.
	 * @param unitList unit list
	 * @throws IOException Thrown if I/O error happens
	 */
	private void outputTypeList(ArrayList<String> typeList) throws IOException {
		/* saves type strings */
		for (int i = 0; i < typeList.size(); i++) {
			this.getOutputPorts()
					.get(OUTPUTTYPEID)
					.outputToAllCharPipes(
							String.valueOf(typeList.get(i)) + "\n");
		}
	}

	@Override
	public boolean process() throws Exception {
		
		// (re-)initialise variables
		this.analyzer = null;
		this.resultBuf = new StringBuffer();
		this.prettyBuf = new StringBuffer("<HTML><HEAD><meta charset=\"utf-8\"><TITLE> </TITLE></HEAD><BODY>");
		this.xmlBuf = new StringBuffer("<?xml version=\"1.0\"?>\n<kwipInfo>\n");

		// Check for interrupt signal
		if (Thread.interrupted()) {
			this.closeAllOutputs();
			throw new InterruptedException("Thread has been interrupted.");
		}
		
		// Text to search
		Directory index = generateIndex(new BufferedReader(this.getInputPorts().get(INPUT1ID).getInputReader()));
		IndexReader reader = DirectoryReader.open(index);
		types(index, reader);
		// reader can only be closed if there is no need to access the
		// documents any more
		reader.close();
		
		// Check for interrupt signal
		if (Thread.interrupted()) {
			this.closeAllOutputs();
			throw new InterruptedException("Thread has been interrupted.");
		}
		
		// output plain text result
		this.getOutputPorts().get(OUTPUTPLAINID).outputToAllCharPipes(resultBuf.toString());
		this.getOutputPorts().get(OUTPUTPLAINID).close();
		
		// Check for interrupt signal
		if (Thread.interrupted()) {
			this.closeAllOutputs();
			throw new InterruptedException("Thread has been interrupted.");
		}

		// output pretty html result
		prettyBuf.append("</BODY></HTML>");
		this.getOutputPorts().get(OUTPUTHTMLID).outputToAllCharPipes(prettyBuf.toString());
		this.getOutputPorts().get(OUTPUTHTMLID).close();
		
		// Check for interrupt signal
		if (Thread.interrupted()) {
			this.closeAllOutputs();
			throw new InterruptedException("Thread has been interrupted.");
		}

		// output xml result
		xmlBuf.append("</kwipInfo>");
		this.getOutputPorts().get(OUTPUTXMLID).outputToAllCharPipes(xmlBuf.toString());
		this.getOutputPorts().get(OUTPUTXMLID).close();
		
		// Close remaining outputs (if necessary)
		this.closeAllOutputs();
		
		return true;
	}
}