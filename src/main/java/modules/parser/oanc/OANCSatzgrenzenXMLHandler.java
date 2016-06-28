package modules.parser.oanc;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Verarbeitet FileFinderModule-Satzgenzen-XML
 * @author marcel
 *
 */
public class OANCSatzgrenzenXMLHandler extends DefaultHandler {
	
	List<OANCXMLSatzgrenze> satzgrenzen = new ArrayList<OANCXMLSatzgrenze>();

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals("struct") && (attributes.getValue("type").equals("s") || attributes.getValue("type").equals("u"))){
			this.satzgrenzen.add(new OANCXMLSatzgrenze(Integer.parseInt(attributes.getValue("from")), Integer.parseInt(attributes.getValue("to"))));
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// Wuerde nur benoetigt, wenn wir das innere XML-Element "id" noch braeuchten
		super.endElement(uri, localName, qName);
	}

	public List<OANCXMLSatzgrenze> getSatzgrenzen() {
		return satzgrenzen;
	}

	public void setSatzgrenzen(List<OANCXMLSatzgrenze> satzgrenzen) {
		this.satzgrenzen = satzgrenzen;
	}

}
