package keyWordInPhrase;

//   
//
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import util.TextInfo;

public class KeyWordInPhrase {

	private static final String POSITION = "Position";
	private static final String SOURCE = "Source";
	private static final String TEXT = "Text";
	private static final String CONTEXT = "Context";

	private static final Logger LOGGER = Logger.getGlobal();

	static StandardAnalyzer analyzer = null;
	static StringBuffer resultBuf = new StringBuffer();
	static StringBuffer prettyBuf = new StringBuffer(
			"<HTML><HEAD><meta charset=\"utf-8\"><TITLE> </TITLE></HEAD><BODY>");

	static StringBuffer xmlBuf = new StringBuffer(
			"<?xml version=\"1.0\"?>\n<kwipInfo>\n");

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
			File preprocessedFile = new File(TextInfo.getPreprocessPath());
			int lineNr = 0;
			try (BufferedReader bufreader = new BufferedReader(new FileReader(
					preprocessedFile))) {

				String line;
				while ((line = bufreader.readLine()) != null) {
					LOGGER.fine(String.valueOf(lineNr) + " " + line);
					List<String> tokenList = TokenizeString.tokenizeString(
							analyzer, line);
					for (String token : tokenList) {
						LOGGER.fine(token + "\t " + line);

						// each token is one document
						addDoc(w, token, line, lineNr);
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

	static void types(Directory index, IndexReader reader) {
		ArrayList<Integer> unitList = new ArrayList<Integer>();
		ArrayList<String> typeList = new ArrayList<String>();
		int units = 0;
		try {
			Terms terms = SlowCompositeReaderWrapper.wrap(reader).terms(TEXT);
			TermsEnum termEnum = null;
			termEnum = terms.iterator(termEnum);

			String tabs = "\t\t";
			int len = 0;
			int currentPos = 0;
			while (termEnum.next() != null) {
				String type = termEnum.term().utf8ToString();

				xmlBuf.append("\t<type ").append("text=\"").append(type)
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

	static int tokens(IndexReader reader, String querystr, int currentPos) {
		int lastSource = -1;

		try {
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
				xmlBuf.append("\t\t<token>\n");
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				StringBuffer context = new StringBuffer(d.get(CONTEXT));
				xmlBuf.append("\t\t\t<context>").append(context)
						.append("</context>\n");

				int tokenPosition = d.getField(POSITION).numericValue()
						.intValue();
				LOGGER.fine("position of token in context: " + tokenPosition);

				LOGGER.finer("tokens querystr: " + querystr + " context: "
						+ context /* d.get("Context") */
						+ " source: " + d.get(SOURCE));
				xmlBuf.append("\t\t\t<contextStart>").append(currentPos)
						.append("</contextStart>\n");
				xmlBuf.append("\t\t\t<position>")
						.append(currentPos + tokenPosition)
						.append("</position>\n");
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
				xmlBuf.append("\t\t</token>\n");
			}

			return currentPos;

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			int x1 = 10 / 0;
			System.exit(3);
		}
		return -1;
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

	static void writeToFileUnitList(ArrayList<Integer> unitList) {
		/*
		 * a unit is the number of tokens for a type; a unit is defined as the
		 * last token of a type. Its value is a int, counting from the last
		 * token of the first type to the next last token of the next type (e.g.
		 * 3 (if there are three tokens of the first type), 5 (if there are two
		 * tokens of the second type), ...
		 */

		try {
			FileWriter fw = new FileWriter(TextInfo.getKwipUnitPath());
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
			FileWriter fw = new FileWriter(TextInfo.getKwipTypePath());
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

	public static void main(String[] args) {
		LoggerConfigurator.configGlobal();

		try {
			LOGGER.info("Path of file: " + TextInfo.getTextPath());

			// Text to search
			Directory index = generateIndex();
			IndexReader reader = DirectoryReader.open(index);
			types(index, reader);
			// reader can only be closed if there is no need to access the
			// documents any more
			reader.close();
			// System.out.println(resultBuf);
			FileWriter fwresult = new FileWriter(TextInfo.getKwipPath());
			fwresult.write(resultBuf.toString());
			fwresult.close();

			prettyBuf.append("</BODY></HTML>");
			LOGGER.fine("vor prettyWriter");
			FileWriter prettyWriter = new FileWriter(
					TextInfo.getPrettyKwipPath());

			prettyWriter.write(prettyBuf.toString());
			// System.out.println(prettyBuf);
			prettyWriter.close();
			LOGGER.fine("nach prettyWriter");

			xmlBuf.append("</kwipInfo>");
			FileWriter xmlWriter = new FileWriter(TextInfo.getKwipXMLPath());

			xmlWriter.write(xmlBuf.toString());
			xmlWriter.close();

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			int x1 = 10 / 0;
			// System.exit(7);
		}
	}
}