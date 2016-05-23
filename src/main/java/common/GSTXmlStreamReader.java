package common;

// GST XML specific imports.
import models.GSTXmlNode;

// Stream specific imports.
import java.io.InputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
// SAX specific imports.
// Imports for SAX object generation.
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

// Imports for XML handling.
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// Java utility imports.
import java.util.TreeMap;

/**
 * This common class intended to read all fields of a generalized suffix tree (GST) xml output file.
 * 
 * @author christopher
 *
 */
public class GSTXmlStreamReader extends DefaultHandler {
	
	// Enumeration:
	
	private enum XmlTags {
		NUMBER, LABEL, TYPENR, PATTERNINFO, PATTERN, STARTPOS, FREQUENCY, UNDEFINED
	}
	
	// Variables:
	
	// GST XML input stream.
	private static InputStream gstXmlInStream;
	
	// HashMap holding all nodes of the GST.
	private TreeMap<Integer, GSTXmlNode> gstXMLNodes;
	
	// Save the current XML tag state for further parsing control and decisions.
	private XmlTags xmlState = XmlTags.UNDEFINED;
	
	// Save current XML fields.		
	private int number;
	private String label;
	private int typeNr;
	private int pattern;
	private int startPos;
	private int frequency;
			
	// End Variables.
	
	// Constructor:
	public GSTXmlStreamReader (InputStream xmlInputStream) {
		GSTXmlStreamReader.gstXmlInStream = xmlInputStream;
		this.gstXMLNodes = new TreeMap<Integer, GSTXmlNode>();
	}
	
	// End Constructor.
	
	// Methods:
	
	// Parse the content of the input stream (meaning the XML file format).
	public TreeMap<Integer, GSTXmlNode> read () {
		try {
			
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(GSTXmlStreamReader.gstXmlInStream, this);
			
		} catch (ParserConfigurationException pe) {
			pe.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		
		return this.gstXMLNodes;
	}
	
	
	// Decide here how to behave at a specific XML start tag.
	@Override
	public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (qName.equals("number")) {
			this.xmlState = XmlTags.NUMBER;
		} else if (qName.equals("label")) {
			this.xmlState = XmlTags.LABEL;
		} else if (qName.equals("typeNr")) {
			this.xmlState = XmlTags.TYPENR;
		} else if (qName.equals("pattern")) {
			this.xmlState = XmlTags.PATTERN;
		} else if (qName.equals("startpos")) {
			this.xmlState = XmlTags.STARTPOS;
		} else if (qName.equals("frequency")) {
			this.xmlState = XmlTags.FREQUENCY;
		}
		
	}
	
	// Decide how to behave at a specific XML end tag.
	@Override
	public void endElement(String uri, 
			String localName, String qName) throws SAXException {
		this.xmlState = XmlTags.UNDEFINED;
		
		if (qName.equals("patternInfo")) {
			this.gstXMLNodes.get(this.number).setNodeTypes(this.typeNr, this.pattern, this.startPos);
		}
      
	}
	
	
	// Parse the date depending on your decision.
	@Override
	public void characters(char ch[], 
			int start, int length) throws SAXException {
		String currString = new String (ch, start, length);
		switch (this.xmlState) {
			case NUMBER:
				
				this.number = Integer.parseInt(currString);
				this.gstXMLNodes.put(this.number, new GSTXmlNode(this.number));
				break;
				
			case LABEL:
				this.label = currString;
				this.gstXMLNodes.get(this.number).setNodeLabel(this.label);
				break;
				
			case TYPENR:
				this.typeNr = Integer.parseInt(currString);
				break;
				
			case PATTERN:
				this.pattern = Integer.parseInt(currString);
				break;
				
			case STARTPOS:
				this.startPos = Integer.parseInt(currString);
				break;
				
			case FREQUENCY:
				this.frequency = Integer.parseInt(currString);
				this.gstXMLNodes.get(this.number).setNodeFrequency(this.frequency);
				
			default:
				break;
		}
	}
	// End Methods.
	
	
}
