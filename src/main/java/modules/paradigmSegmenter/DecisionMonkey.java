package modules.paradigmSegmenter;

import java.util.List;

import models.ExtensibleTreeNode;

public class DecisionMonkey {

	public static boolean debug = false;
	private SymbolRater symbolBewerter;
	private SplitDecisionNode aktuellerEntscheidungsKnoten;
	private ExtensibleTreeNode suffixbaumWurzelknoten;
	private ExtensibleTreeNode aktuellerKnoten;

	public DecisionMonkey(SymbolRater symbolBewerter, ExtensibleTreeNode suffixbaumWurzelknoten) {
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
	public SplitDecisionNode konstruiereEntscheidungsbaum(List<String> zeichenkette, SplitDecisionNode entscheidungsbaumWurzelknoten) throws Exception {
		// Rueckkehr zur Wurzel des Entscheidungsbaumes
		if (debug && aktuellerEntscheidungsKnoten != null)
			aktuellerEntscheidungsKnoten.setNotiz(null);
		aktuellerEntscheidungsKnoten = entscheidungsbaumWurzelknoten;
		if (debug) aktuellerEntscheidungsKnoten.setNotiz(" {A}");
		aktuellerKnoten = aktuellerEntscheidungsKnoten.getSuffixTrieKindKnoten();
		double letzteBewertung = Double.MAX_VALUE;
		
		// Schleife ueber alle Zeichen (das erste ist bereits im Entscheidungsbaumwurzelknoten hinterlegt)
		for (int index=1; index<zeichenkette.size();){
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
				double bewertungVerbinde = symbolBewerter.symbolBewerten(zeichenkette.get(index), aktuellerKnoten, letzteBewertung);
				double bewertungTrenne = symbolBewerter.symbolBewerten(zeichenkette.get(index), suffixbaumWurzelknoten, Double.MAX_VALUE);
				if (aktuellerKnoten == null)
					throw new Exception("The segmenter seems to have encountered an unknown symbol and cannot continue -- please make sure the suffix trie contains all symbols used within the segmentation input.");
				SplitDecisionNode entscheidungsknotenVerbinde = new SplitDecisionNode(bewertungVerbinde, aktuellerKnoten, aktuellerKnoten.getChildNodes().get(zeichenkette.get(index)), aktuellerEntscheidungsKnoten, zeichenkette.get(index));
				SplitDecisionNode entscheidungsknotenTrenne = new SplitDecisionNode(bewertungTrenne, suffixbaumWurzelknoten, suffixbaumWurzelknoten.getChildNodes().get(zeichenkette.get(index)), aktuellerEntscheidungsKnoten, zeichenkette.get(index));
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
