package modules.plainText2TreeBuilder;

/**
 * This TreeNode class holds information read and converted by the class
 * "PlainText2TreeBuilderConverter"
 * 
 * @author christopher
 *
 */

public class PlainText2TreeNodes {
	
	//variables:
	private String wort;
	private String annotation;
	private String begriff;
	//end variables
	
	//constructors:
	public PlainText2TreeNodes (String word, String annotation, String term) {
		this.wort = word;
		this.annotation = annotation;
		this.begriff = term;
	}
	//end constructors
	
	//setters:
	public void setWort (String word) {
		wort = word;
	}
	
	public void setAnnotation (String annot) {
		annotation = annot;
	}
	
	public void setBegriff (String term) {
		begriff = term;
	}
	
	//end setters
	
	//getters:
	public String getWort () {
		return wort;
	}
	
	
	public String getAnnotation () {
		return annotation;
	}
	
	public String getBegriff () {
		return begriff;
	}
	//end getters
}
