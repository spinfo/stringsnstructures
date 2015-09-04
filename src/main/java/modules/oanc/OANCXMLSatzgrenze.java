package modules.oanc;

/**
 * Beinhaltet Satzgrenzeninformation (OANC)
 * @author marcel
 *
 */
public class OANCXMLSatzgrenze {

	private int von;
	private int bis;
	
	public OANCXMLSatzgrenze(int von, int bis) {
		super();
		this.von = von;
		this.bis = bis;
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
	
	
}
