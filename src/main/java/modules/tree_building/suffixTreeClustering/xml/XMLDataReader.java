package modules.tree_building.suffixTreeClustering.xml;

import java.io.File;
import java.util.Map;

import modules.tree_building.suffixTreeClustering.st_interface.SuffixTreeInfo;

public class XMLDataReader {

	private final File xmlFile;
	
	public XMLDataReader(String xmlFileName) {
		this.xmlFile = new File(xmlFileName);	
	}
	
	public SuffixTreeInfo read(Map<Integer, String> typeStrings){
		if(null == typeStrings)
			return new SuffixTreeInfo();
		return SAXHandler.read(xmlFile, typeStrings);		
	}
	
	public File getXmlFile() {
		return xmlFile;
	}
}