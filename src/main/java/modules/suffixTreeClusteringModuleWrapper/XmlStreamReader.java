package modules.suffixTreeClusteringModuleWrapper;

import java.io.InputStream;
import java.util.Map;

import modules.suffixTreeClustering.st_interface.SuffixTreeInfo;

public class XmlStreamReader {

	private InputStream inStream;
		
	public XmlStreamReader (InputStream inStreamName) {
			inStream = inStreamName;	
		}
		
	public SuffixTreeInfo read(Map<Integer, String> typeStrings){
		final SAXStreamHandler saxStreamHandler = new SAXStreamHandler();
		
		if(null == typeStrings)
			return new SuffixTreeInfo();
		return saxStreamHandler.read(inStream, typeStrings);	
	}
	
	public InputStream getInStream() {
		return inStream;
	}
}
