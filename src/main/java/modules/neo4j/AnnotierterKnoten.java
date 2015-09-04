package modules.neo4j;

import java.util.HashMap;
import java.util.Map;

public class AnnotierterKnoten {
	
	private String name;
	private Map<String,Long> zahlenwerte;
	private Map<String,String> textwerte;

	public AnnotierterKnoten(String name) {
		super();
		this.name = name;
		this.zahlenwerte = new HashMap<String,Long>();
		this.textwerte = new HashMap<String,String>();
	}

	public AnnotierterKnoten(String name,
			Map<String, Long> zahlenwerte, Map<String, String> textwerte) {
		super();
		this.name = name;
		this.zahlenwerte = zahlenwerte;
		this.textwerte = textwerte;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Long> getZahlenwerte() {
		return zahlenwerte;
	}

	public void setZahlenwerte(Map<String, Long> zahlenwerte) {
		this.zahlenwerte = zahlenwerte;
	}

	public Map<String, String> getTextwerte() {
		return textwerte;
	}

	public void setTextwerte(Map<String, String> textwerte) {
		this.textwerte = textwerte;
	}

}
