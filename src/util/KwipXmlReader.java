package util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import suffixTreeClustering.data.Token;
import suffixTreeClustering.data.Type;

public class KwipXmlReader extends DefaultHandler {

	private static final int TYPE = 1;
	private static final int TOKEN = 2;
	private static final int CONTEXTSTART = 3;
	private static final int POSITION = 4;
	private static final int CONTEXTEND = 5;
	private static final int SOURCE = 6;
	private static final int CONTEXT = 7;

	private static Logger logger = Logger.getLogger(KwipXmlReader.class
			.getSimpleName());
	private static File xmlFile;
	private int iState = 0;
	private Type currentType;
	private Token currentToken;
	private List<Type> types;

	public KwipXmlReader(String xmlFileName) {
		KwipXmlReader.xmlFile = new File(xmlFileName);
	}

	public List<Type> read() {
		SAXParser saxParser;

		try {
			saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(xmlFile, this);

		} catch (ParserConfigurationException pe) {
			pe.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}

		return types;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		String eName = ("".equals(localName)) ? qName : localName;

		if (eName.equals("kwipInfo")) {
			logger.info("Create new KwipInfo Object");
			this.types = new ArrayList<Type>();
		}
		if (eName.equals("type")) {
			iState = TYPE;
			if (attributes != null && attributes.getLength() > 0) {
				String text = attributes.getValue(0);
				currentType = new Type();
				currentType.setTypeString(text);
			}
		}
		if (eName.equals("token")) {
			iState = TOKEN;
			currentToken = new Token();
		}
		if (eName.equals("context")) {
			iState = CONTEXT;
		}
		if (eName.equals("contextStart")) {
			iState = CONTEXTSTART;
		}
		if (eName.equals("position")) {
			iState = POSITION;
		}
		if (eName.equals("contextEnd")) {
			iState = CONTEXTEND;
		}
		if (eName.equals("source")) {
			iState = SOURCE;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String s = new String(ch, start, length);

		switch (iState) {
		case CONTEXT:
			currentToken.setText(s);
			break;
		case CONTEXTEND:
			currentToken.setContextEnd(Integer.parseInt(s));
			break;
		case CONTEXTSTART:
			currentToken.setContextStart(Integer.parseInt(s));
			break;
		case POSITION:
			currentToken.setTypePosition(Integer.parseInt(s));
			break;
		case SOURCE:
			currentToken.setSource(Integer.parseInt(s));
			break;
		default:
			break;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		iState = 0;

		String eName = ("".equals(localName)) ? qName : localName;

		if (eName.equals("type")) {
			// end of currentType --> save it
			types.add(currentType);
		}

		if (eName.equals("token")) {
			// end of current token --> save it
			currentType.addToken(currentToken);
		}
	}
}