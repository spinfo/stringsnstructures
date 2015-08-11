package module.common;

import treeBuilder.Knoten;

public class SplitDecisionNode {
	
	private double value;
	private SplitDecisionNode split;
	private SplitDecisionNode join;
	private Knoten suffixTrieKnoten;
	private SplitDecisionNode elternKnoten;
	private String notiz;
	public Character symbol;
	
	public SplitDecisionNode() {
		super();
	}
	public SplitDecisionNode(double value) {
		super();
		this.value = value;
	}
	public SplitDecisionNode(double value, String notiz) {
		super();
		this.value = value;
		this.notiz = notiz;
	}
	public SplitDecisionNode(double value, Knoten suffixTrieKnoten,
			SplitDecisionNode elternKnoten, Character symbol) {
		super();
		this.value = value;
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
	 * @return the value
	 */
	public double getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
		this.value = value;
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
		
		sb.append(this.getValue());
		
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
		this.value += huerdenWert;
		if (this.elternKnoten != null)
			this.elternKnoten.addiereHuerdenWert(huerdenWert);
	}

}
