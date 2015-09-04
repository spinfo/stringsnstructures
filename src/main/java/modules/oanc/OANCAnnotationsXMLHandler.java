package modules.oanc;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Verarbeitet OANC-Annotations-XML
 * @author marcel
 *
 */
public class OANCAnnotationsXMLHandler extends DefaultHandler {
	
	private List<OANCXMLAnnotation> ergebnisliste = new ArrayList<OANCXMLAnnotation>();
	private Stack<OANCXMLAnnotation> annotationen = new Stack<OANCXMLAnnotation>();

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		
		// Pruefen, ob Element eine Annotation beschreibt
		if (qName.equals("struct")){
			// Neue Annotation instanziieren
			OANCXMLAnnotation annotation = new OANCXMLAnnotation();
			try {
				// Attributwerte anfuegen
				annotation.setVon(Integer.parseInt(attributes.getValue("from")));
				annotation.setBis(Integer.parseInt(attributes.getValue("to")));
			} catch (Exception e){
				Logger.getLogger(this.getClass().getSimpleName()).warning("Fehler beim Parsen der Annotations-XML-Daten: "+e.getMessage());
			}
			// Annotation auf Stack legen
			annotationen.push(annotation);
		}
		// Pruefen, ob Element einen Annotationswert beschreibt
		else if (qName.equals("feat")){
			// Oberste Annotation des Stacks ermitteln
			OANCXMLAnnotation annotation = annotationen.peek();
			try {
				annotation.getAnnotationswerte().put(attributes.getValue("name"), attributes.getValue("value"));
			} catch (Exception e){
				Logger.getLogger(this.getClass().getSimpleName()).warning("Fehler beim Parsen der Annotations-XML-Daten: "+e.getMessage());
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		if (qName.equals("struct")){
			// Oberste Annotation zur Ergebnisliste hinzufuegen
			OANCXMLAnnotation annotation = annotationen.pop();
			ergebnisliste.add(annotation);
		}
	}

	/**
	 * Gibt eine Map mit den ermitterlten Annotationen zurueck.
	 * Schluesselwert ist eine Integer-Instanz mit dem Startindex
	 * des annotierten Wortes.
	 * @return Map mit Annotationen
	 */
	public List<OANCXMLAnnotation> getAnnotationen() {
		return ergebnisliste;
	}

}
