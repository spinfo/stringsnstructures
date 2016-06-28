package modules.parser.oanc;

import java.util.HashMap;
import java.util.Map;

/**
 * Beinhaltet Satzgrenzeninformation (FileFinderModule)
 * @author marcel
 *
 */
public class OANCXMLAnnotation {

	private int von;
	private int bis;
	private Map<String,String> annotationswerte;
	
	public OANCXMLAnnotation(){
		this.annotationswerte = new HashMap<String,String>();
	}
	public int getVon() {
		return von;
	}
	public void setVon(int von) {
		this.von = von;
	}
	public int getBis() {
		return bis;
	}
	public void setBis(int bis) {
		this.bis = bis;
	}
	public Map<String, String> getAnnotationswerte() {
		return annotationswerte;
	}
	public void setAnnotationswerte(Map<String, String> annotationswerte) {
		this.annotationswerte = annotationswerte;
	}
	
	
}
