package module.common;

import treeBuilder.Knoten;

public class SplitDecisionNode {
	
	private double bewertung;
	private double aktivierungsPotential;
	private SplitDecisionNode split;
	private SplitDecisionNode join;
	private Knoten suffixTrieKnoten;
	private SplitDecisionNode elternKnoten;
	private String notiz;
	public Character symbol;
	
	public SplitDecisionNode() {
		super();
	}
	public SplitDecisionNode(double bewertung) {
		super();
		this.bewertung = bewertung;
		this.aktivierungsPotential = bewertung;
	}
	public SplitDecisionNode(double bewertung, String notiz) {
		super();
		this.bewertung = bewertung;
		this.aktivierungsPotential = bewertung;
		this.notiz = notiz;
	}
	public SplitDecisionNode(double bewertung, Knoten suffixTrieKnoten,
			SplitDecisionNode elternKnoten, Character symbol) {
		super();
		this.bewertung = bewertung;
		this.aktivierungsPotential = bewertung;
		this.suffixTrieKnoten = suffixTrieKnoten;
		this.elternKnoten = elternKnoten;
		this.symbol = symbol;
	}
	/**
	 * @return the notiz
	 */
	public String getNotiz() {
		return notiz;
	}
	/**
	 * @param notiz the notiz to set
	 */
	public void setNotiz(String notiz) {
		this.notiz = notiz;
	}
	/**
	 * @return the bewertung
	 */
	public double getBewertung() {
		return bewertung;
	}
	/**
	 * @param bewertung the bewertung to set
	 */
	public void setBewertung(double bewertung) {
		this.bewertung = bewertung;
	}
	/**
	 * @return the split
	 */
	public SplitDecisionNode getSplit() {
		return split;
	}
	/**
	 * @param split the split to set
	 */
	public void setSplit(SplitDecisionNode split) {
		this.split = split;
	}
	/**
	 * @return the join
	 */
	public SplitDecisionNode getJoin() {
		return join;
	}
	/**
	 * @param join the join to set
	 */
	public void setJoin(SplitDecisionNode join) {
		this.join = join;
	}
	
	@Override
	public String toString(){
		return this.toString(0);
	}
	
	public String toString(int indentLevel){
		StringBuffer sb = new StringBuffer();
		
		// Value of this node
		for (int i=0; i<indentLevel; i++){
			sb.append("\t");
		}
		
		if (this.notiz != null && !this.notiz.isEmpty())
			sb.append(this.notiz+":");
		if (this.symbol != null)
			sb.append(this.symbol+":");
		
		sb.append(this.getAktivierungsPotential());
		sb.append(" ["+this.getBewertung()+"]");
		
		// Recurse for child nodes
		if (this.getJoin()!=null)
			sb.append("\n"+this.getJoin().toString(indentLevel+1));
		if (this.getSplit()!=null)
			sb.append("\n"+this.getSplit().toString(indentLevel+1));
		
		return sb.toString();
	}
	/**
	 * @return the elternKnoten
	 */
	public SplitDecisionNode getElternKnoten() {
		return elternKnoten;
	}
	/**
	 * @param elternKnoten the elternKnoten to set
	 */
	public void setElternKnoten(SplitDecisionNode elternKnoten) {
		this.elternKnoten = elternKnoten;
	}
	/**
	 * @return the suffixTrieKnoten
	 */
	public Knoten getSuffixTrieKnoten() {
		return suffixTrieKnoten;
	}
	/**
	 * @param suffixTrieKnoten the suffixTrieKnoten to set
	 */
	public void setSuffixTrieKnoten(Knoten suffixTrieKnoten) {
		this.suffixTrieKnoten = suffixTrieKnoten;
	}
	/**
	 * Fuegt diesem Entscheidungsknoten und seinen Ahnen rekursiv den uebergebenen Huerdenwert hinzu.
	 * @param huerdenWert
	 */
	public void addiereHuerdenWert(double huerdenWert) {
		// Aktivierungspotential auf das minimal notwenige erhoehen
		if (this.getJoin()!=null && this.getSplit()!=null){
			
			double minimalwert;
			
			// Falls dieser Knoten der oberste ist, ist keine Trennung moeglich!
			if (this.elternKnoten == null)
				minimalwert = this.getJoin().getAktivierungsPotential();
			else
				minimalwert = Math.max(Math.min(this.getJoin().getAktivierungsPotential(), this.getSplit().getAktivierungsPotential()),this.getBewertung());
			
			
			// Falls der Wert sich nicht aendert, wird die Rekursion abgebrochen
			if (minimalwert==this.aktivierungsPotential)
				return;
			this.aktivierungsPotential = minimalwert;
		} else
			this.aktivierungsPotential += huerdenWert;
		if (this.elternKnoten != null)
			this.elternKnoten.addiereHuerdenWert(huerdenWert);
	}
	/**
	 * @return the aktivierungsPotential
	 */
	public double getAktivierungsPotential() {
		return aktivierungsPotential;
	}
	/**
	 * @param aktivierungsPotential the aktivierungsPotential to set
	 */
	public void setAktivierungsPotential(double aktivierungsPotential) {
		this.aktivierungsPotential = aktivierungsPotential;
	}

}
