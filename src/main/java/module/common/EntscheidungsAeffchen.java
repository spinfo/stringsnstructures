package module.common;

import treeBuilder.Knoten;

public class EntscheidungsAeffchen {

	private SymbolBewerter symbolBewerter;
	private SplitDecisionNode aktuellerEntscheidungsKnoten;
	private Knoten suffixbaumWurzelknoten;
	private Knoten aktuellerKnoten;

	public EntscheidungsAeffchen(SymbolBewerter symbolBewerter, Knoten suffixbaumWurzelknoten) {
		super();
		this.symbolBewerter = symbolBewerter;
		this.suffixbaumWurzelknoten = suffixbaumWurzelknoten;
	}

	/**
	 * Erstellt einen Entscheidungsbaum anhand der uebergebenen Zeichenkette und gibt den Weg des geringsten Widerstands zurueck.
	 * @param zeichenkette
	 * @return Blatt am Ende des Weges des geringsten Widerstands
	 */
	public SplitDecisionNode konstruiereEntscheidungsbaum(StringBuffer zeichenkette, SplitDecisionNode entscheidungsbaumWurzelknoten) {
		
		// Rueckkehr zur Wurzel des Entscheidungsbaumes
		aktuellerEntscheidungsKnoten = entscheidungsbaumWurzelknoten;
		aktuellerKnoten = suffixbaumWurzelknoten;
		
		// Schleife ueber alle Zeichen
		for (int index=0; index<zeichenkette.length();){
			
			// Pruefen, ob der aktuelle Entscheidungsknoten bereits Kindelemente hat
			if (aktuellerEntscheidungsKnoten.getSplit() != null && aktuellerEntscheidungsKnoten.getJoin() != null){
				
				// Kindelement mit dem geringsten Widerstand auswaehlen
				if (aktuellerEntscheidungsKnoten.getSplit().getValue()<aktuellerEntscheidungsKnoten.getJoin().getValue()){
					aktuellerEntscheidungsKnoten = aktuellerEntscheidungsKnoten.getSplit();
					aktuellerKnoten = suffixbaumWurzelknoten;
				} else {
					aktuellerEntscheidungsKnoten = aktuellerEntscheidungsKnoten.getJoin();
					aktuellerKnoten = aktuellerKnoten.getKinder().get(new Character(zeichenkette.charAt(index)).toString());
				}
				
				index++;
				
			} else {
				// Der aktuelle Entscheidungsbaumknoten hat noch KEINE Kindelemente, daher muessen zunaechst die Bewertungen ermittelt werden
				double bewertungVerbinde = symbolBewerter.symbolBewerten(zeichenkette.charAt(index), aktuellerKnoten);
				double bewertungTrenne;
				if (aktuellerKnoten.equals(suffixbaumWurzelknoten))
					bewertungTrenne = Double.MAX_VALUE;
				else
					bewertungTrenne = symbolBewerter.symbolBewerten(zeichenkette.charAt(index), suffixbaumWurzelknoten);
				
				SplitDecisionNode entscheidungsknotenVerbinde = new SplitDecisionNode(bewertungVerbinde, aktuellerKnoten, aktuellerEntscheidungsKnoten, zeichenkette.charAt(index));
				SplitDecisionNode entscheidungsknotenTrenne = new SplitDecisionNode(bewertungTrenne, suffixbaumWurzelknoten, aktuellerEntscheidungsKnoten, zeichenkette.charAt(index));
				aktuellerEntscheidungsKnoten.setJoin(entscheidungsknotenVerbinde);
				aktuellerEntscheidungsKnoten.setSplit(entscheidungsknotenTrenne);
				
				// Geringsten Huerdenwert addieren
				aktuellerEntscheidungsKnoten.addiereHuerdenWert(Math.min(bewertungVerbinde, bewertungTrenne));
				
				// Rueckkehr zur Wurzel des Entscheidungsbaumes, um erneut den Weg des geringsten Widerstands zu ermitteln
				aktuellerEntscheidungsKnoten = entscheidungsbaumWurzelknoten;
				aktuellerKnoten = suffixbaumWurzelknoten;
				index = 0;
			}
			
			
		}
		// Wenn die Schleife bis zum Ende der Zeichenkette durchlaeuft, ist der beste Weg gefunden
		return aktuellerEntscheidungsKnoten;
	}

}
