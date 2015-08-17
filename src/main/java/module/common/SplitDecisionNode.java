package module.common;

import java.text.DecimalFormat;

import treeBuilder.Knoten;

public class SplitDecisionNode {
	
	private static final DecimalFormat NUMMERNFORMAT = new DecimalFormat("###,###.###");
	
	private double bewertung;
	private double aktivierungsPotential;
	private SplitDecisionNode split;
	private SplitDecisionNode join;
	private Knoten suffixTrieElternKnoten;
	private Knoten suffixTrieKindKnoten;
	private SplitDecisionNode elternKnoten;
	private String notiz;
	private Character symbol;
	
	public SplitDecisionNode() {
		super();
	}
	public SplitDecisionNode(double bewertung) {
		super();
		this.bewertung = bewertung;
		this.aktivierungsPotential = bewertung;
	}
	
	public SplitDecisionNode(double bewertung, Knoten suffixTrieElternKnoten, Knoten suffixTrieKindKnoten,
			SplitDecisionNode elternKnoten, Character symbol) {
		super();
		this.bewertung = bewertung;
		this.aktivierungsPotential = bewertung;
		this.suffixTrieElternKnoten = suffixTrieElternKnoten;
		this.suffixTrieKindKnoten = suffixTrieKindKnoten;
		this.elternKnoten = elternKnoten;
		this.symbol = symbol;
	}
	public SplitDecisionNode(double bewertung, String notiz) {
		super();
		this.bewertung = bewertung;
		this.aktivierungsPotential = bewertung;
		this.notiz = notiz;
	}
	/**
	 * @return the aktivierungsPotential
	 */
	public double getAktivierungsPotential() {
		return aktivierungsPotential;
	}
	/**
	 * @return the bewertung
	 */
	public double getBewertung() {
		return bewertung;
	}
	/**
	 * @return the elternKnoten
	 */
	public SplitDecisionNode getElternKnoten() {
		return elternKnoten;
	}
	/**
	 * @return the join
	 */
	public SplitDecisionNode getJoin() {
		return join;
	}
	/**
	 * @return the notiz
	 */
	public String getNotiz() {
		return notiz;
	}
	/**
	 * @return the split
	 */
	public SplitDecisionNode getSplit() {
		return split;
	}
	/**
	 * @return the suffixTrieElternKnoten
	 */
	public Knoten getSuffixTrieElternKnoten() {
		return suffixTrieElternKnoten;
	}
	/**
	 * @return the suffixTrieKindKnoten
	 */
	public Knoten getSuffixTrieKindKnoten() {
		return suffixTrieKindKnoten;
	}
	/**
	 * @return the symbol
	 */
	public Character getSymbol() {
		return symbol;
	}
	/**
	 * Hebt das Aktivierungspotential dieses Entscheidungsknotens auf den minimal
	 * notwenigen Wert, um das Niveau eines der Kindelemente zu erreichen.
	 * Gibt true zurueck, falls sich das Aktivierungspotential nicht aendert.
	 * @param potentialWert
	 * @return
	 */
	public boolean hebeAktivierungsPotentialAufMinimumAn() {
		// Aktivierungspotential auf das minimal notwenige erhoehen
		if (this.getJoin()!=null && this.getSplit()!=null){
			
			double minimalwert;
			
			// Falls dieser Knoten der oberste ist, ist keine Trennung moeglich!
			if (this.elternKnoten == null)
				minimalwert = this.getJoin().getAktivierungsPotential();
			else
				minimalwert = Math.max(Math.min(this.getJoin().getAktivierungsPotential(), this.getSplit().getAktivierungsPotential()),this.getBewertung());
			
			
			// Falls der Wert sich nicht erhoeht, wird die Rekursion abgebrochen
			if (minimalwert<=this.aktivierungsPotential)
				return false;
			this.aktivierungsPotential = minimalwert;
		}
		return true;
	}
	/**
	 * @param aktivierungsPotential the aktivierungsPotential to set
	 */
	public void setAktivierungsPotential(double aktivierungsPotential) {
		this.aktivierungsPotential = aktivierungsPotential;
	}
	/**
	 * @param bewertung the bewertung to set
	 */
	public void setBewertung(double bewertung) {
		this.bewertung = bewertung;
	}
	
	/**
	 * @param elternKnoten the elternKnoten to set
	 */
	public void setElternKnoten(SplitDecisionNode elternKnoten) {
		this.elternKnoten = elternKnoten;
	}
	
	/**
	 * @param join the join to set
	 */
	public void setJoin(SplitDecisionNode join) {
		this.join = join;
	}
	/**
	 * @param notiz the notiz to set
	 */
	public void setNotiz(String notiz) {
		this.notiz = notiz;
	}
	/**
	 * @param split the split to set
	 */
	public void setSplit(SplitDecisionNode split) {
		this.split = split;
	}
	/**
	 * @param suffixTrieElternKnoten the suffixTrieElternKnoten to set
	 */
	public void setSuffixTrieElternKnoten(Knoten suffixTrieKnoten) {
		this.suffixTrieElternKnoten = suffixTrieKnoten;
	}
	/**
	 * @param suffixTrieKindKnoten the suffixTrieKindKnoten to set
	 */
	public void setSuffixTrieKindKnoten(Knoten suffixTrieKindKnoten) {
		this.suffixTrieKindKnoten = suffixTrieKindKnoten;
	}
	

	/**
	 * @param symbol the symbol to set
	 */
	public void setSymbol(Character symbol) {
		this.symbol = symbol;
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
		
		if (this.symbol != null)
			sb.append(this.symbol+":");
		if (this.getAktivierungsPotential()==Double.MAX_VALUE)
			sb.append("X");
		else
			sb.append(NUMMERNFORMAT.format(this.getAktivierungsPotential()));
		if (this.getBewertung()==Double.MAX_VALUE)
			sb.append(" [X]");
		else
			sb.append(" ["+NUMMERNFORMAT.format(this.getBewertung())+"]");
		if (this.notiz != null && !this.notiz.isEmpty())
			sb.append(this.notiz);
		
		// Recurse for child nodes
		if (this.getJoin()!=null)
			sb.append("\n"+this.getJoin().toString(indentLevel+1));
		if (this.getSplit()!=null)
			sb.append("\n"+this.getSplit().toString(indentLevel+1));
		
		return sb.toString();
	}

}
