package suffixTreeClustering.xml;

import java.io.File;
import java.util.Map;

import suffixTreeClustering.st_interface.SuffixTreeInfo;

public class XMLDataReader {

	private final File xmlFile;
	
	public XMLDataReader(String xmlFileName) {
		this.xmlFile = new File(xmlFileName);	
	}
	
	public SuffixTreeInfo read(Map<Integer, String> typeStrings){
		return SAXHandler.read(xmlFile, typeStrings);		
	}
	
	public File getXmlFile() {
		return xmlFile;
	}
}