package module.common;

import treeBuilder.Knoten;

public class SymbolBewerter {
	
	/**
	 * Bewertet ein einzelnes Symbol (im Sinne, dass die Uebergangshuerde vom Elternknoten aus errechnet wird).
	 * @param symbol Symbol (Zeichenkette mit Laenge eins)
	 * @param elternKnoten Elternknoten im Suffixbaum
	 * @return bewertung 0 <= B < ∞-1 (kleiner bedeutet geringere Uebergangshuerde)
	 */
	public double symbolBewerten(Character symbol, Knoten elternKnoten){
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
			bewertung = (1d/anteil)-1d;
			
		}
		
		// Ergebnis zurueckgeben
		return bewertung;
	}
}
