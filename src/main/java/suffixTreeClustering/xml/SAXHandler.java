package suffixTreeClustering.xml;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import suffixTreeClustering.data.Node;
import suffixTreeClustering.data.Type;
import suffixTreeClustering.st_interface.SuffixTreeInfo;

/**
 * Utility Class to read XML encoded data structure into own data structure to
 * work with in following steps.
 * 
 * @author neumannm
 */
public class SAXHandler extends DefaultHandler {
	private static Logger logger = Logger.getLogger(SAXHandler.class
			.getSimpleName());

	private static SuffixTreeInfo outputInfo;
	private static Map<Integer, String> types;
	private Node currentNode;

	private Tags iState = Tags.UNDEFINED;
	private Type currentType;

	private enum Tags {UNITS, NODES, NODE, NUMBER, LABEL, FREQUENCY, TYPE, PATTERNINFO, TYPENR, PATTERN, STARTPOS, UNDEFINED};
	
	/**
	 * Read content from XML File into own data structure {@link SuffixTreeInfo}
	 * .
	 * 
	 * @param xmlFile
	 *            - XML file to read, should contain information about
	 *            SuffixTree Nodes.
	 * @param typeStrings
	 *            - mapping from type IDs to type Strings
	 * @return {@link SuffixTreeInfo} object
	 */
	public static SuffixTreeInfo read(File xmlFile,
			Map<Integer, String> typeStrings) {

		SAXHandler.types = typeStrings;
		DefaultHandler handler = new SAXHandler();

		SAXParser saxParser;

		try {
			saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(xmlFile, handler);

		} catch (ParserConfigurationException pe) {
			pe.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}

		return outputInfo;
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes attrs) throws SAXException {
		String eName = ("".equals(localName)) ? qName : localName;

		if (attrs != null && attrs.getLength() > 0) {
			logger.warning("Node should not have attributes!");
		}

		if (eName.equals("output")) {
			outputInfo = new SuffixTreeInfo();
			logger.info("Create new SuffixTreeInfo Object");
		}
		if (eName.equals("units")) {
			iState = Tags.UNITS;
		}
		if (eName.equals("nodes")) {
			iState = Tags.NODES;
		}
		if (eName.equals("node")) {
			currentNode = new Node();
		}
		if (eName.equals("number")) {
			iState = Tags.NUMBER;
		}
		if (eName.equals("label")) {
			iState = Tags.LABEL;
		}
		if (eName.equals("frequency")) {
			iState = Tags.FREQUENCY;
		}
		if (eName.equals("type")) {
			iState = Tags.TYPE;
		}
		if (eName.equals("patternInfo")) {
			// TODO: eigentlich muss nicht jedes Mal ein neuer Type erstellt
			// werden...
			currentType = new Type();
			logger.info("Create new type");
			iState = Tags.PATTERNINFO;
		}
		if (eName.equals("typeNr")) {
			iState = Tags.TYPENR;
		}
		if (eName.equals("pattern")) {
			iState = Tags.PATTERN;
		}
		if (eName.equals("startpos")) {
			iState = Tags.STARTPOS;
		}
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {

		iState = Tags.UNDEFINED;

		String eName = ("".equals(localName)) ? qName : localName;

		if (eName.equals("node")) {
			// end of currentNode --> save it
			// if node number == 1 --> ignore (root node)
			if (!(currentNode.getNodeNumber() == 1))
				outputInfo.addNode(currentNode);
		}

		if (eName.equals("patternInfo")) {
			// end of current type --> save it
			outputInfo.addType(currentType);
		}
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] buf, int offset, int len) throws SAXException {
		String s = new String(buf, offset, len);

		switch (iState) {
		case UNITS:
			outputInfo.setNumberOfTypes(Integer.parseInt(s));
			break;
		case NODES:
			// manually subtract 1 because total number contains
			// root node
			outputInfo.setNumberOfNodes(Integer.parseInt(s) - 1);
			break;
		case NUMBER:
			currentNode.setNodeNumber(Integer.parseInt(s));
			break;
		case LABEL:
			currentNode.setPathLabel(s);
			break;
		case FREQUENCY:
			currentNode.setFrequency(Integer.parseInt(s));
			break;
		case TYPENR:
			int typeID = Integer.parseInt(s);
			currentType.setID(typeID);
			currentType.setTypeString(types.get(typeID));

			if (!currentNode.getTypes().containsKey(currentType))
				currentNode.addType(currentType);
			else logger.info("Type already contained in node");

			break;
		case PATTERN:
			break;
		case STARTPOS:
			int startPosition = Integer.parseInt(s);
			currentNode.getTypes().get(currentType).add(startPosition);
			break;
		default:
			break;
		}
	}
}