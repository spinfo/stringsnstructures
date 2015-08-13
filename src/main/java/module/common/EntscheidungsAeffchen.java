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
	public SplitDecisionNode konstruiereEntscheidungsbaum(StringBuffer zeichenkette, SplitDecisionNode entscheidungsbaumWurzelknoten) throws Exception {
		
		// Rueckkehr zur Wurzel des Entscheidungsbaumes
		if (aktuellerEntscheidungsKnoten != null)
			aktuellerEntscheidungsKnoten.setNotiz(null);
		aktuellerEntscheidungsKnoten = entscheidungsbaumWurzelknoten;
		aktuellerEntscheidungsKnoten.setNotiz(" {A}");// TODO Zur Veranschaulichung; Zeile kann entfernt werden
		aktuellerKnoten = suffixbaumWurzelknoten;
		double letzteBewertung = Double.MAX_VALUE;
		
		// Schleife ueber alle Zeichen
		for (int index=0; index<zeichenkette.length();){
			
			// Pruefen, ob der aktuelle Entscheidungsknoten bereits Kindelemente hat
			if (aktuellerEntscheidungsKnoten.getSplit() != null && aktuellerEntscheidungsKnoten.getJoin() != null){
				
				// Suffixbaum-Kindknoten ermitteln, der die Zeichenkette fortfuehren wuerde
				Knoten kindKnoten = aktuellerKnoten.getKinder().get(new Character(zeichenkette.charAt(index)).toString());
				// Falls der Kindknoten nicht existiert, ist eine Verbindung unmoeglich
				/*if (kindKnoten==null)
					aktuellerEntscheidungsKnoten.getJoin().setAktivierungsPotential(Double.MAX_VALUE);*/
				
				// Kindelement mit dem geringsten Widerstand auswaehlen (auf der ersten Ebene des Suffixbaumes kann keine Trennung gewaehlt werden)
				if (kindKnoten==null || aktuellerEntscheidungsKnoten.getSplit().getAktivierungsPotential()<aktuellerEntscheidungsKnoten.getJoin().getAktivierungsPotential() && !aktuellerEntscheidungsKnoten.equals(entscheidungsbaumWurzelknoten)){
					letzteBewertung = aktuellerEntscheidungsKnoten.getSplit().getBewertung();
					aktuellerEntscheidungsKnoten.setNotiz(null);// TODO Zur Veranschaulichung; Zeile kann entfernt werden
					aktuellerEntscheidungsKnoten = aktuellerEntscheidungsKnoten.getSplit();
					aktuellerEntscheidungsKnoten.setNotiz(" {A}");// TODO Zur Veranschaulichung; Zeile kann entfernt werden
					aktuellerKnoten = suffixbaumWurzelknoten;
				} else {
					letzteBewertung = aktuellerEntscheidungsKnoten.getJoin().getBewertung();
					aktuellerEntscheidungsKnoten.setNotiz(null);// TODO Zur Veranschaulichung; Zeile kann entfernt werden
					aktuellerEntscheidungsKnoten = aktuellerEntscheidungsKnoten.getJoin();
					aktuellerEntscheidungsKnoten.setNotiz(" {A}");// TODO Zur Veranschaulichung; Zeile kann entfernt werden
					aktuellerKnoten = kindKnoten;
				}
				
				index++;
				
			} else {
				
				if (index ==9)
					System.out.println(9);
				
				// Der aktuelle Entscheidungsbaumknoten hat noch KEINE Kindelemente, daher muessen zunaechst die Bewertungen ermittelt werden
				double bewertungVerbinde = symbolBewerter.symbolBewerten(zeichenkette.charAt(index), aktuellerKnoten, letzteBewertung);
				double bewertungTrenne;
				if (aktuellerEntscheidungsKnoten.equals(entscheidungsbaumWurzelknoten))
					bewertungTrenne = Double.MAX_VALUE;
				else
					bewertungTrenne = symbolBewerter.symbolBewerten(zeichenkette.charAt(index), suffixbaumWurzelknoten, Double.MAX_VALUE);
				
				SplitDecisionNode entscheidungsknotenVerbinde = new SplitDecisionNode(bewertungVerbinde, aktuellerKnoten, aktuellerEntscheidungsKnoten, zeichenkette.charAt(index));
				SplitDecisionNode entscheidungsknotenTrenne = new SplitDecisionNode(bewertungTrenne, suffixbaumWurzelknoten, aktuellerEntscheidungsKnoten, zeichenkette.charAt(index));
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
					aktuellerEntscheidungsKnoten.setNotiz(null);// TODO Zur Veranschaulichung; Zeile kann entfernt werden
					aktuellerEntscheidungsKnoten = aktuellerEntscheidungsKnoten.getElternKnoten();
					aktuellerEntscheidungsKnoten.setNotiz(" {A}");// TODO Zur Veranschaulichung; Zeile kann entfernt werden
					if (aktuellerEntscheidungsKnoten.getSuffixTrieKnoten() == null)
						throw new Exception("Aktueller Knoten ist null");
					aktuellerKnoten = aktuellerEntscheidungsKnoten.getSuffixTrieKnoten(); // FIXME Hier landet man irgendwo beim falschen Knoten ...
					
					// Index der Position im Zeichenpuffer reduzieren
					index--;
				}
				
				
				
				
			}
			
			
		}
		// Wenn die Schleife bis zum Ende der Zeichenkette durchlaeuft, ist der beste Weg gefunden
		return aktuellerEntscheidungsKnoten;
	}

}
