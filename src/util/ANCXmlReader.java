package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ANCXmlReader {

	private static Logger logger = Logger.getLogger(ANCXmlReader.class
			.getSimpleName());

	static List<String> parseXML(String xmlFileName, String text)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFileName);

		// optional, but recommended
		// read this -
		// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();

		logger.fine("Root element: " + doc.getDocumentElement().getNodeName());

		NodeList nList = doc.getElementsByTagName("struct");

		List<String> extracted = new ArrayList<>();
		String possibleVerb;

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);

			logger.fine("\nCurrent Element: " + nNode.getNodeName());

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				logger.fine("Type: " + eElement.getAttribute("type"));

				String from = eElement.getAttribute("from");
				logger.fine("From: " + from);
				int start = Integer.parseInt(from);

				String to = eElement.getAttribute("to");
				int end = Integer.parseInt(to);
				logger.fine("To: " + to);

				possibleVerb = text.substring(start, end);
				extracted.add(possibleVerb);

				NodeList subElements = eElement.getElementsByTagName("feat");
				for (int i = 0; i < subElements.getLength(); i++) {
					Element item = (Element) eElement.getElementsByTagName(
							"feat").item(i);

					logger.fine("Feature: " + item.getAttribute("name"));
					logger.fine("Value: " + item.getAttribute("value"));
				}
			}
		}

		return extracted;
	}
}