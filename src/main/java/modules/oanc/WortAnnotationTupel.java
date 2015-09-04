package modules.oanc;

public class WortAnnotationTupel {
	
	private String wort;
	private String annotation;
	private String begriff;
	
	public WortAnnotationTupel(){
	}
	
	public WortAnnotationTupel(String wort, String annotation, String begriff) {
		super();
		this.wort = wort;
		this.annotation = annotation;
		this.begriff = begriff;
	}
	public String getWort() {
		return wort;
	}
	public void setWort(String wort) {
		this.wort = wort;
	}
	public String getAnnotation() {
		return annotation;
	}
	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}
	public String getBegriff() {
		return begriff;
	}
	public void setBegriff(String begriff) {
		this.begriff = begriff;
	}
	
	

}
