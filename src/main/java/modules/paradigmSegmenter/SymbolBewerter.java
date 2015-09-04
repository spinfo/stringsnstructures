package modules.paradigmSegmenter;

import modules.treeBuilder.Knoten;

public class SymbolBewerter {
	
	private double mindestKostenProSymbolschritt;
	private double bewertungsAbfallFaktor;

	public SymbolBewerter(double mindestKostenProSymbolschritt,
			double bewertungsAbfallFaktor) {
		super();
		this.mindestKostenProSymbolschritt = mindestKostenProSymbolschritt;
		this.bewertungsAbfallFaktor = bewertungsAbfallFaktor;
	}

	/**
	 * @return the bewertungsAbfallFaktor
	 */
	public double getBewertungsAbfallFaktor() {
		return bewertungsAbfallFaktor;
	}

	/**
	 * @param bewertungsAbfallFaktor the bewertungsAbfallFaktor to set
	 */
	public void setBewertungsAbfallFaktor(double bewertungsAbfallFaktor) {
		this.bewertungsAbfallFaktor = bewertungsAbfallFaktor;
	}

	/**
	 * @return the mindestKostenProSymbolschritt
	 */
	public double getMindestKostenProSymbolschritt() {
		return mindestKostenProSymbolschritt;
	}

	/**
	 * @param mindestKostenProSymbolschritt the mindestKostenProSymbolschritt to set
	 */
	public void setMindestKostenProSymbolschritt(
			double mindestKostenProSymbolschritt) {
		this.mindestKostenProSymbolschritt = mindestKostenProSymbolschritt;
	}

	/**
	 * Bewertet ein einzelnes Symbol (im Sinne, dass die Uebergangshuerde vom Elternknoten aus errechnet wird).
	 * @param symbol Symbol (Zeichenkette mit Laenge eins)
	 * @param elternKnoten Elternknoten im Suffixbaum
	 * @return bewertung (kleiner bedeutet geringere Uebergangshuerde)
	 */
	public double symbolBewerten(Character symbol, Knoten elternKnoten, double letzteBewertung){
		// Variable fuer das Gesamtergebnis, Standardwert ist die maximal moegliche Huerde
		double bewertung = Double.MAX_VALUE;
		
		// Pruefen, ob der aktuelle Knoten des Suffixbaumes unter dem aktuellen Symbol der Zeichenkette einen Kindknoten fuehrt.
		if (symbol != null && elternKnoten != null && elternKnoten.getKinder().containsKey(symbol.toString())){
			
			// Knoten ermitteln
			Knoten kindKnoten = elternKnoten.getKinder().get(symbol.toString());
			
			// Ermitteln, welchen Wert der aktuelle Knoten hat
			int gesamtwert = elternKnoten.getZaehler();
			
			// Ermitteln, welchen Wert der Kindknoten hat
			int teilwert = kindKnoten.getZaehler();
			
			// Anteil des Kindknotenzaehlers am Zaehler seines Elternknoten ermitteln
			double anteil = new Double(teilwert)/new Double(gesamtwert); // 0 < anteil <= 1
			
			// Bewertung fuer diesen Kindknoten errechnen
			bewertung = (1d/anteil)-1d+this.mindestKostenProSymbolschritt;
			
			// Abfall in der Bewertung miteinbeziehen (deutet auf paradigmatische Grenze hin)
			if (letzteBewertung<bewertung)
				bewertung = bewertung * (bewertung/letzteBewertung) * this.bewertungsAbfallFaktor;
			
		}
		
		// Ergebnis zurueckgeben
		return bewertung;
	}
}
