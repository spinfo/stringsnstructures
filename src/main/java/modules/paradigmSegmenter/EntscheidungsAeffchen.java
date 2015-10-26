package modules.paradigmSegmenter;

import modules.treeBuilder.Knoten;

public class EntscheidungsAeffchen {

	public static boolean debug = false;
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
	 * @param zeichenkette String
	 * @param entscheidungsbaumWurzelknoten Decision tree root node
	 * @return Blatt am Ende des Weges des geringsten Widerstands
	 * @throws Exception Thrown if something goes wrong
	 */
	public SplitDecisionNode konstruiereEntscheidungsbaum(StringBuffer zeichenkette, SplitDecisionNode entscheidungsbaumWurzelknoten) throws Exception {
		
		// Rueckkehr zur Wurzel des Entscheidungsbaumes
		if (debug && aktuellerEntscheidungsKnoten != null)
			aktuellerEntscheidungsKnoten.setNotiz(null);
		aktuellerEntscheidungsKnoten = entscheidungsbaumWurzelknoten;
		if (debug) aktuellerEntscheidungsKnoten.setNotiz(" {A}");
		aktuellerKnoten = aktuellerEntscheidungsKnoten.getSuffixTrieKindKnoten();
		double letzteBewertung = Double.MAX_VALUE;
		
		// Schleife ueber alle Zeichen (das erste ist bereits im Entsche4idungsbaumwurzelknoten hinterlegt)
		for (int index=1; index<zeichenkette.length();){
			
			// Pruefen, ob der aktuelle Entscheidungsknoten bereits Kindelemente hat
			if (aktuellerEntscheidungsKnoten.getSplit() != null && aktuellerEntscheidungsKnoten.getJoin() != null){
				
				// Kindelement mit dem geringsten Widerstand auswaehlen
				if (aktuellerEntscheidungsKnoten.getSplit().getAktivierungsPotential()<aktuellerEntscheidungsKnoten.getJoin().getAktivierungsPotential()){
					letzteBewertung = aktuellerEntscheidungsKnoten.getSplit().getBewertung();
					if (debug) aktuellerEntscheidungsKnoten.setNotiz(null);
					aktuellerEntscheidungsKnoten = aktuellerEntscheidungsKnoten.getSplit();
					if (debug) aktuellerEntscheidungsKnoten.setNotiz(" {A}");
					aktuellerKnoten = aktuellerEntscheidungsKnoten.getSuffixTrieKindKnoten();
				} else {
					letzteBewertung = aktuellerEntscheidungsKnoten.getJoin().getBewertung();
					if (debug) aktuellerEntscheidungsKnoten.setNotiz(null);
					aktuellerEntscheidungsKnoten = aktuellerEntscheidungsKnoten.getJoin();
					if (debug) aktuellerEntscheidungsKnoten.setNotiz(" {A}");
					aktuellerKnoten = aktuellerEntscheidungsKnoten.getSuffixTrieKindKnoten();
				}
				
				index++;
				
			} else {
				
				// Der aktuelle Entscheidungsbaumknoten hat noch KEINE Kindelemente, daher muessen zunaechst die Bewertungen ermittelt werden
				double bewertungVerbinde = symbolBewerter.symbolBewerten(zeichenkette.charAt(index), aktuellerKnoten, letzteBewertung);
				double bewertungTrenne = symbolBewerter.symbolBewerten(zeichenkette.charAt(index), suffixbaumWurzelknoten, Double.MAX_VALUE);
				
				SplitDecisionNode entscheidungsknotenVerbinde = new SplitDecisionNode(bewertungVerbinde, aktuellerKnoten, aktuellerKnoten.getKinder().get(new Character(zeichenkette.charAt(index)).toString()), aktuellerEntscheidungsKnoten, zeichenkette.charAt(index));
				SplitDecisionNode entscheidungsknotenTrenne = new SplitDecisionNode(bewertungTrenne, suffixbaumWurzelknoten, suffixbaumWurzelknoten.getKinder().get(new Character(zeichenkette.charAt(index)).toString()), aktuellerEntscheidungsKnoten, zeichenkette.charAt(index));
				aktuellerEntscheidungsKnoten.setJoin(entscheidungsknotenVerbinde);
				aktuellerEntscheidungsKnoten.setSplit(entscheidungsknotenTrenne);
				

				/*
				 *  Rueckkehr zum naechsten Ahnenelement des Entscheidungsbaumes, dessen Aktivierungspotential sich
				 *  durch den hinzugekommenen Wert nicht aendert, um von dort aus erneut den Weg des geringsten
				 *  Widerstands zu ermitteln.
				 */
				boolean aktivierungsPotentialVeraendert = true;
				while(aktivierungsPotentialVeraendert){
					
					// Knotenpotential auf das minimal notwendige Niveau anheben und ermitteln, ob es sich dadurch aendert
					aktivierungsPotentialVeraendert = aktuellerEntscheidungsKnoten.hebeAktivierungsPotentialAufMinimumAn();
					
					// Abbrechen, falls wir bereits den Wurzelknoten des Entscheidungsbaumes erreicht haben
					if (aktuellerEntscheidungsKnoten.equals(entscheidungsbaumWurzelknoten) || !aktivierungsPotentialVeraendert)
						break;
					
					// Elternknoten im Entscheidungs- und Suffixbaum ermitteln
					if (debug) aktuellerEntscheidungsKnoten.setNotiz(null);
					aktuellerEntscheidungsKnoten = aktuellerEntscheidungsKnoten.getElternKnoten();
					if (debug) aktuellerEntscheidungsKnoten.setNotiz(" {A}");
					aktuellerKnoten = aktuellerEntscheidungsKnoten.getSuffixTrieElternKnoten();
					
					// Index der Position im Zeichenpuffer reduzieren
					index--;
				}
				
				
				
				
			}
			
			
		}
		// Wenn die Schleife bis zum Ende der Zeichenkette durchlaeuft, ist der beste Weg gefunden
		return aktuellerEntscheidungsKnoten;
	}

}
