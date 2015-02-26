package keyWordInPhrase;
//   
//
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import util.LoggerConfigurator;

public class KeyWordInPhrase {

	private static final Logger LOGGER = Logger.getGlobal();

	private static final String ENCODING = "UTF-8";// StandardCharsets.UTF_8;
	private static final String TXTEXTENSION = ".txt";
	private static String PATH = pathName();
	private static String name = ReadNameOfInputFile(PATH);
	private static String fileSeparator = System.getProperty("file.separator");
	private static String INPUT_FILE_NAME = PATH + name + "Preprocess"
			+ TXTEXTENSION;
	private static String OUTPUT_RESULT_FILE_NAME = PATH + name + "Kwip"
			+ TXTEXTENSION;
	private static String OUTPUT_TYPE_FILE_NAME = PATH + name + "Kwip" + "Type"
			+ TXTEXTENSION;
	private static String OUTPUT_UNIT_FILE_NAME = PATH + name + "Kwip" + "Unit"
			+ TXTEXTENSION;

	private static String pathName() {

		Path p = Paths.get("../");
		try {
			return p.toRealPath(LinkOption.NOFOLLOW_LINKS).toString() + "\\";
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	private static String ReadNameOfInputFile(String PATH) {
		// reads name of input file from file "Name"
		try (BufferedReader reader = new BufferedReader(new FileReader(PATH
				+ "Name" + TXTEXTENSION))) {
			String name = reader.readLine();
			LOGGER.info("Name of text file: " + name);
			return name;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static StandardAnalyzer analyzer = null;
	static StringBuffer resultBuf = new StringBuffer();
	static StringBuffer prettyBuf = new StringBuffer(
			"<HTML><HEAD><meta charset=\"utf-8\"><TITLE> </TITLE></HEAD><BODY>");

	private static void addDoc(IndexWriter w, String token, String context,
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
		Field field = new Field("Text", token, fieldType);
		doc.add(field);
		// ******************************************************************
		doc.add(new StringField("Context", context, Field.Store.YES));
		// ******************************************************************
		// a field for source because not to be tokenized
		doc.add(new IntField("Source", line, Field.Store.YES));
		w.addDocument(doc);
	}

	static Directory generateIndex() {
		Directory index = null;
		try {
			// Specify the analyzer for tokenizing text.
			// The same analyzer should be used for indexing and searching
			analyzer = new StandardAnalyzer(Version.LUCENE_46);

			// Code to create the index
			index = new RAMDirectory();

			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46,
					analyzer);
			IndexWriter w = new IndexWriter(index, config);
			// ----------------------------------------------------------------------
			File file = new File(INPUT_FILE_NAME);
			int lineNr = 0;
			try (BufferedReader bufreader = new BufferedReader(new FileReader(
					file))) {

				String text = null;

				while ((text = bufreader.readLine()) != null) {
					LOGGER.fine(String.valueOf(lineNr) + " " + text);
					// each token is one document
					List<String> tokenList = TokenizeString.tokenizeString(
							analyzer, text);
					for (String token : tokenList) {
						LOGGER.fine(token + "\t " + text);

						addDoc(w, token, text, lineNr);
					}

					lineNr++;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				int x1 = 10 / 0;
				System.exit(2);

			} catch (IOException e) {
				e.printStackTrace();
				int x1 = 10 / 0;
				System.exit(2);
			}
			w.close();
			System.out.println();

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			int x1 = 10 / 0;
			System.exit(2);
		}
		return index;
	}

	/* pretty print in html */
	static private StringBuffer pretty(StringBuffer buf, String type) {
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

	static void tokens(IndexReader reader, String querystr) {
		int lastSource = -1;

		try {
			int hitsPerPage = 100000;
			IndexSearcher searcher = new IndexSearcher(reader);

			// The \"title\" arg specifies the default field to use when no
			// field is explicitly specified in the query
			Query query = new QueryParser(Version.LUCENE_46, "Text", analyzer)
					.parse(querystr);

			TopScoreDocCollector collector = TopScoreDocCollector.create(
					hitsPerPage, true);

			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			// Code to display the results of search

			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				StringBuffer context = new StringBuffer(d.get("Context"));
				LOGGER.finer("tokens querystr: " + querystr + " context: "
						+ context /* d.get("Context") */
						+ " source: " + d.get("Source"));
				resultBuf.append(context);// d.get("Context"));
				prettyBuf.append(pretty(context, querystr));
				if (lastSource == Integer.parseInt(d.get("Source"))) {
					// int i1= 10/0;
				}

				lastSource = Integer.parseInt(d.get("Source"));
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			int x1 = 10 / 0;
			System.exit(3);
		}
	}

	static void writeToFileUnitList(ArrayList<Integer> unitList) {
		/*
		 * a unit is the number of tokens for a type; a unit is defined as the
		 * last token of a type. Its value is a int, counting from the last
		 * token of the first type to the next last token of the next type (e.g.
		 * 3 (if there are three tokens of the first type), 5 (if there are two
		 * tokens of the second type), ...
		 */

		try {
			FileWriter fw = new FileWriter(OUTPUT_UNIT_FILE_NAME);
			for (int i = 0; i < unitList.size(); i++) {
				fw.write(String.valueOf(unitList.get(i)) + "\n");
				LOGGER.finer("UnitList item: " + unitList.get(i));
			}
			fw.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			int x1 = 10 / 0;
			System.exit(4);
		}
	}

	static void writeToFileTypeList(ArrayList<String> typeList) {
		/* saves type strings */
		try {
			FileWriter fw = new FileWriter(OUTPUT_TYPE_FILE_NAME);
			for (int i = 0; i < typeList.size(); i++) {
				fw.write(String.valueOf(typeList.get(i)) + "\n");
				LOGGER.finer("TypeList item: " + typeList.get(i));
			}
			fw.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			int x1 = 10 / 0;
			System.exit(5);
		}
	}

	static void types(Directory index, IndexReader reader) {
		ArrayList<Integer> unitList = new ArrayList<Integer>();
		ArrayList<String> typeList = new ArrayList<String>();
		int units = 0;
		try {
			Terms terms = SlowCompositeReaderWrapper.wrap(reader).terms("Text");
			TermsEnum termEnum = null;
			termEnum = terms.iterator(termEnum);

			String tabs = "\t\t";
			int len = 0;
			while (termEnum.next() != null) {
				String type = termEnum.term().utf8ToString();
				if (type.length() < 8)
					len = 2;
				else
					len = 1;
				LOGGER.finest("types type: " + type + tabs.substring(0, len)
						+ "Freq.: " + termEnum.docFreq() + "\n");
				// exclude numbers and numberstrings
				if ((type.charAt(0) < '0') || (type.charAt(0) > '9')) {
					tokens(reader, type);
					units = units + termEnum.docFreq();
					unitList.add(units);
					typeList.add(type);
					LOGGER.finest("units: " + units);
				}
			}
			writeToFileUnitList(unitList);
			writeToFileTypeList(typeList);
			System.out.println();

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			int x1 = 10 / 0;
			System.exit(6);
		}
	}

	public static void main(String[] args) {
		LoggerConfigurator.configGlobal();

		try {
			LOGGER.info("Path of file: " + PATH + name);

			// Text to search
			Directory index = generateIndex();
			IndexReader reader = DirectoryReader.open(index);
			types(index, reader);
			// reader can only be closed if there is no need to access the
			// documents any more
			reader.close();
			// System.out.println(resultBuf);
			FileWriter fwresult = new FileWriter(OUTPUT_RESULT_FILE_NAME);
			fwresult.write(resultBuf.toString());
			fwresult.close();

			prettyBuf.append("</BODY></HTML>");
			LOGGER.fine("vor prettyWriter");
			FileWriter prettyWriter = new FileWriter(PATH + name + "PrettyKwip"
					+ ".html");

			prettyWriter.write(prettyBuf.toString());
			// System.out.println(prettyBuf);
			prettyWriter.close();
			LOGGER.fine("nach prettyWriter");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			int x1 = 10 / 0;
			// System.exit(7);
		}
	}
}