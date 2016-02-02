package modules.treeSimilarityClustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import models.ExtensibleTreeNode;

public class NodeComparator {
	
	private int maximaleAuswertungsEbene = -1;
	private double ebenenexponent = 0d;
	private boolean ebenenFaktorNurAufTrefferAnwenden = false;
	
	public NodeComparator() {
		super();
	}

	public NodeComparator(int maximaleAuswertungsEbene,
			double ebenenexponentpositiv, boolean ebenenFaktorNurAufTrefferAnwenden) {
		super();
		this.maximaleAuswertungsEbene = maximaleAuswertungsEbene;
		this.ebenenexponent = ebenenexponentpositiv;
		this.ebenenFaktorNurAufTrefferAnwenden = ebenenFaktorNurAufTrefferAnwenden;
	}

	public int getMaximaleAuswertungsEbene() {
		return maximaleAuswertungsEbene;
	}

	public void setMaximaleAuswertungsEbene(int maximaleAuswertungsEbene) {
		this.maximaleAuswertungsEbene = maximaleAuswertungsEbene;
	}

	public double getEbenenexponent() {
		return ebenenexponent;
	}

	public void setEbenenexponent(double ebenenexponent) {
		this.ebenenexponent = ebenenexponent;
	}

	/**
	 * Vergleicht die Baeume miteinander, deren WurzelExtensibleTreeNode uebergeben wurden.
	 * @param k1 WurzelExtensibleTreeNode 1
	 * @param k2 WurzelExtensibleTreeNode 2
	 * @return Anteil des Trefferwerts am Gesamtwert
	 */
	public Double vergleiche(ExtensibleTreeNode k1, ExtensibleTreeNode k2) {

		ExtensibleTreeNode verschmolzenerBaum = verschmelzeBaeume(k1, k2);
		Double[] trefferWert = this.ermittleExtensibleTreeNodeTrefferwert(verschmolzenerBaum, this.maximaleAuswertungsEbene, this.ebenenexponent);
		return new Double(trefferWert[0] / trefferWert[1]);

	}
	
	/**
	 * Vergleicht alle uebergebenen Baeume miteinander und gibt eine Matrix aus Uebereinstimmungsquotienten zurueck.
	 * @param ExtensibleTreeNodeListe Liste der WurzelExtensibleTreeNode der miteinander zu vergleichenden Baeume.
	 * @param reduziereVergleicheAufNotwendige Vergleiche werden auf Notwendige beschraenkt: Eigenvergleiche (A<->A) und redundante Vergleiche (A<->B B<->A) werden ignoriert.
	 * @param vergleichAufVergleichswortzweigBeschraenken Nur der Zweig, der mit dem Vergleichswort (das im WurzelExtensibleTreeNode eines jeden Graphen festgehalten ist) beginnt, wird verglichen.
	 * @param zeigeNurTrefferExtensibleTreeNode Im Graphenplot werden nur uebereinstimmende ExtensibleTreeNode angezeigt.
	 * @param layoutTyp Typ des anzuzeigenden Layouts, 1: RadialTreeLayout, 2:BalloonLayout
	 * @return Matrix aus Uebereinstimmungsquotienten (Double).
	 */
	public Double[][] vergleicheAlleExtensibleTreeNode(ArrayList<ExtensibleTreeNode> ExtensibleTreeNodeListe, boolean reduziereVergleicheAufNotwendige, boolean vergleichAufVergleichswortzweigBeschraenken, boolean zeigeNurTrefferExtensibleTreeNode, int layoutTyp){
		
		// Vergleichsmatrix erstellen
		Double[][] vergleichsmatrix = new Double[ExtensibleTreeNodeListe.size()][ExtensibleTreeNodeListe.size()];
		
		// Liste der Graphen durchlaufen
		for (int i = 0; i < ExtensibleTreeNodeListe.size(); i++) {

			// Zweite Dimension durchlaufen
			for (int j = 0; j < ExtensibleTreeNodeListe.size(); j++) {

				// Ggf. unnoetige Vergleiche ueberspringen
				if (reduziereVergleicheAufNotwendige && j <= i) {
					vergleichsmatrix[i][j] = null;
					continue;
				}

				// Ggf. nur jene Zweige der jeweiligen Suffixbaeume zum
				// Vergleich heranziehen, die mit dem Vergleichswort beginnen
				ExtensibleTreeNode vergleichsBaumWurzel1;
				ExtensibleTreeNode vergleichsBaumWurzel2;
				if (vergleichAufVergleichswortzweigBeschraenken) {
					vergleichsBaumWurzel1 = ExtensibleTreeNodeListe.get(i).getChildNodes().get(ExtensibleTreeNodeListe.get(i).getNodeValue());
					vergleichsBaumWurzel2 = ExtensibleTreeNodeListe.get(j).getChildNodes().get(ExtensibleTreeNodeListe.get(j).getNodeValue());
				} else {
					vergleichsBaumWurzel1 = ExtensibleTreeNodeListe.get(i);
					vergleichsBaumWurzel2 = ExtensibleTreeNodeListe.get(j);
				}

				// Baeume der zu vergleichenden Worte miteinander kombinieren
				ExtensibleTreeNode verschmolzenerBaum = this.verschmelzeBaeume(
						vergleichsBaumWurzel1, vergleichsBaumWurzel2);

				// Uebereinstimmungswerte ermitteln
				Double[] trefferWert = this.ermittleExtensibleTreeNodeTrefferwert(
						verschmolzenerBaum, this.maximaleAuswertungsEbene,this.ebenenexponent);

				// Meldung ueber Vergleichsergebnis
				Logger.getLogger(this.getClass().getCanonicalName())
						.info("Vergleich "
								+ ExtensibleTreeNodeListe.get(i).getNodeValue()
								+ " - "
								+ ExtensibleTreeNodeListe.get(j).getNodeValue()
								+ " : " + trefferWert[0] + "/" + trefferWert[1]);

				// Uebereinstimmungswerte auf Anteilswert reduzieren und in
				// Matrix speichern
				vergleichsmatrix[i][j] = trefferWert[0] / trefferWert[1];

			}
		}
		
		// Ergebnis zurueckgeben
		return vergleichsmatrix;
	}

	/**
	 * Verschmilzt die Baeume ab der ersten Kindebene des jeweils uebergebenen
	 * ExtensibleTreeNode und gibt den WurzelExtensibleTreeNode des (neuen) Ergebnisbaumes zurueck.
	 * Uebereinstimmungen sind im Ergebnisbaum via isMatch() markiert; Zaehler
	 * der ExtensibleTreeNode wurden im Ergebnisbaum aufaddiert.
	 * 
	 * @param ExtensibleTreeNode1
	 * @param ExtensibleTreeNode2
	 * @return WurzelExtensibleTreeNode des neuen Baumes
	 */
	public ExtensibleTreeNode verschmelzeBaeume(ExtensibleTreeNode ExtensibleTreeNode1, ExtensibleTreeNode ExtensibleTreeNode2) {

		// Neuen ExtensibleTreeNode erzeugen
		ExtensibleTreeNode ergebnisExtensibleTreeNode = new ExtensibleTreeNode();

		// Ermitteln, ob die ExtensibleTreeNode gleichwertig sind
		if (ExtensibleTreeNode1 != null && ExtensibleTreeNode2 != null && ExtensibleTreeNode1.getNodeValue().equals(ExtensibleTreeNode2.getNodeValue())){
			ergebnisExtensibleTreeNode.getAttributes().put("match", new Boolean(true));
		}

		// Ggf. Werte der uebergebenen ExtensibleTreeNode aufaddieren und Kinder hinzufuegen
		if (ExtensibleTreeNode1 != null) {
			ergebnisExtensibleTreeNode.setNodeCounter(ergebnisExtensibleTreeNode.getNodeCounter()
					+ ExtensibleTreeNode1.getNodeCounter());
			ergebnisExtensibleTreeNode.setNodeValue(ExtensibleTreeNode1.getNodeValue());

			// Schleife ueber Kinder des ersten ExtensibleTreeNodes
			Iterator<String> k1Kinder = ExtensibleTreeNode1.getChildNodes().keySet().iterator();
			while (k1Kinder.hasNext()) {

				// Variable fuer neuen KindExtensibleTreeNode definieren
				ExtensibleTreeNode kindExtensibleTreeNode;

				// Name des Kindes von ExtensibleTreeNode1 ermitteln
				String k1KindName = k1Kinder.next();

				// Pruefen, ob ExtensibleTreeNode2 existiert und ebenfalls ein solches Kind
				// hat
				if (ExtensibleTreeNode2 != null
						&& ExtensibleTreeNode2.getChildNodes().containsKey(k1KindName)) {
					// Kind mit diesem Namen gefunden, steige hinab
					kindExtensibleTreeNode = this.verschmelzeBaeume(ExtensibleTreeNode1.getChildNodes()
							.get(k1KindName),
							ExtensibleTreeNode2.getChildNodes().get(k1KindName));

				} else {
					// Kein Kind mit diesem Namen gefunden oder ExtensibleTreeNode2 ist
					// Null, steige hinab
					kindExtensibleTreeNode = this.verschmelzeBaeume(ExtensibleTreeNode1.getChildNodes()
							.get(k1KindName), null);

				}

				// Neuen KindExtensibleTreeNode an Ergebnis anfuegen
				ergebnisExtensibleTreeNode.getChildNodes().put(k1KindName, kindExtensibleTreeNode);

			}
		}
		if (ExtensibleTreeNode2 != null) {
			ergebnisExtensibleTreeNode.setNodeCounter(ergebnisExtensibleTreeNode.getNodeCounter()
					+ ExtensibleTreeNode2.getNodeCounter());

			// Namen ggf. anhaengen
			if (ExtensibleTreeNode1 != null
					&& !(ExtensibleTreeNode1.getNodeValue().equals(ExtensibleTreeNode2.getNodeValue()))) {
				
				// Namen der ExtensibleTreeNode in lexikographischer Reihenfolge abbilden
				String neuerExtensibleTreeNodeName;
				if (ExtensibleTreeNode1.getNodeValue().compareTo(ExtensibleTreeNode2.getNodeValue())<0){
					neuerExtensibleTreeNodeName = ExtensibleTreeNode1.getNodeValue() + " / " + ExtensibleTreeNode2.getNodeValue();
				} else {
					neuerExtensibleTreeNodeName = ExtensibleTreeNode2.getNodeValue() + " / " + ExtensibleTreeNode1.getNodeValue();
				}
				ergebnisExtensibleTreeNode.setNodeValue(neuerExtensibleTreeNodeName);
			} else {
				ergebnisExtensibleTreeNode.setNodeValue(ExtensibleTreeNode2.getNodeValue());
			}

			// Schleife ueber Kinder des ersten ExtensibleTreeNodes
			Iterator<String> k2Kinder = ExtensibleTreeNode2.getChildNodes().keySet().iterator();
			while (k2Kinder.hasNext()) {

				// Variable fuer neuen KindExtensibleTreeNode definieren
				ExtensibleTreeNode kindExtensibleTreeNode;

				// Name des Kindes von ExtensibleTreeNode1 ermitteln
				String k2KindName = k2Kinder.next();

				// Falls dieser ExtensibleTreeNode schon im Ergebnis existiert, kann
				// abgebrochen werden
				if (ergebnisExtensibleTreeNode.getChildNodes().containsKey(k2KindName)) {
					continue;
				}

				// Pruefen, ob ExtensibleTreeNode2 existiert und ebenfalls ein solches Kind
				// hat
				if (ExtensibleTreeNode1 != null
						&& ExtensibleTreeNode1.getChildNodes().containsKey(k2KindName)) {
					// Kind mit diesem Namen gefunden, steige hinab
					kindExtensibleTreeNode = this.verschmelzeBaeume(ExtensibleTreeNode1.getChildNodes()
							.get(k2KindName),
							ExtensibleTreeNode2.getChildNodes().get(k2KindName));

				} else {
					// Kein Kind mit diesem Namen gefunden oder ExtensibleTreeNode2 ist
					// Null, steige hinab
					kindExtensibleTreeNode = this.verschmelzeBaeume(null, ExtensibleTreeNode2
							.getChildNodes().get(k2KindName));

				}

				// Neuen KindExtensibleTreeNode an Ergebnis anfuegen
				ergebnisExtensibleTreeNode.getChildNodes().put(k2KindName, kindExtensibleTreeNode);
			}
		}

		return ergebnisExtensibleTreeNode;
	}

	/**
	 * Wertet die Zaehlvariable der ExtensibleTreeNode des uebergebenen Baumes aus.
	 * 
	 * @see #ermittleExtensibleTreeNodeTrefferwert(ExtensibleTreeNode ExtensibleTreeNode, int ebene, int maxebene, double ebenenexponent)
	 * @param ExtensibleTreeNode Der ExtensibleTreeNode des Baumes, ab dem ausgewertet werden soll.
	 * @return double-Array mit Trefferwert auf Index 0, Gesamtwert auf Index 1.
	 */
	public Double[] ermittleExtensibleTreeNodeTrefferwert(ExtensibleTreeNode ExtensibleTreeNode) {
		return this.ermittleExtensibleTreeNodeTrefferwert(ExtensibleTreeNode, 0, this.maximaleAuswertungsEbene,
				this.ebenenexponent);
	}

	/**
	 * Wertet die Zaehlvariable der ExtensibleTreeNode des uebergebenen Baumes aus.
	 * 
	 * @see #ermittleExtensibleTreeNodeTrefferwert(ExtensibleTreeNode ExtensibleTreeNode, int ebene, int maxebene, double ebenenexponent)
	 * @param ExtensibleTreeNode Der ExtensibleTreeNode des Baumes, ab dem ausgewertet werden soll.
	 * @param maxebene Tiefste Ebene der Baumhierarchie, die noch ausgewertet werden soll.
	 * @param ebenenexponent Exponent der Ebenennummer, welche Faktor fuer die Wertung von ExtensibleTreeNode ist.
	 * @return double-Array mit Trefferwert auf Index 0, Gesamtwert auf Index 1.
	 */
	public Double[] ermittleExtensibleTreeNodeTrefferwert(ExtensibleTreeNode ExtensibleTreeNode, int maxebene,
			double ebenenexponentpositiv) {
		return this.ermittleExtensibleTreeNodeTrefferwert(ExtensibleTreeNode, 0, maxebene,
				ebenenexponentpositiv);
	}

	/**
	 * Wertet die Zaehlvariable der ExtensibleTreeNode des uebergebenen Baumes aus -
	 * TrefferExtensibleTreeNode werden aufaddiert, andere abgezogen. Als zweites Ergebnis
	 * werden alle Zaehlerwerte aufaddiert, unabhaengig von deren Trefferstatus.
	 * Der erste ExtensibleTreeNode wird in der Wertung ignoriert. Der Wert der
	 * Zaehlervariable kann jeweils abhaengig von der Ebene modifiziert werden,
	 * indem man Exponenten uebergibt. Multipliziert wird der Wert dann mit
	 * Ebenennummer^Exponent, d.h. "0" bedeutet keine Aenderung.
	 * 
	 * @param ExtensibleTreeNode Der ExtensibleTreeNode des Baumes, ab dem ausgewertet werden soll.
	 * @param ebene Die Nummer der Ebene, auf der sich der Auswertungsprozess in der Baumhierarchie befindet.
	 * @param maxebene Tiefste Ebene der Baumhierarchie, die noch ausgewertet werden soll. <0 zum Ignorieren.
	 * @param ebenenexponent Exponent der Ebenennummer, welche Faktor fuer die Wertung von ExtensibleTreeNode ist.
	 * @return double-Array mit Trefferwert auf Index 0, Gesamtwert auf Index 1.
	 */
	private Double[] ermittleExtensibleTreeNodeTrefferwert(ExtensibleTreeNode ExtensibleTreeNode, int ebene,
			int maxebene, double ebenenexponent) {
		Double[] ExtensibleTreeNodeMatches = new Double[] { 0d, 0d };

		// Zaehlerwerte ermitteln (der WurzelExtensibleTreeNode wird ignoriert)
		if (ebene > 0){
			if (Boolean.parseBoolean(ExtensibleTreeNode.getAttributes().get("match").toString())) {
				// Treffer - zum Ergebnis addieren
				ExtensibleTreeNodeMatches[0] += ExtensibleTreeNode.getNodeCounter()
						* Math.pow(ebene, ebenenexponent);
			}
			// Zaehlerwert zur Gesamtzahl addieren
			if (ebenenFaktorNurAufTrefferAnwenden){
				ExtensibleTreeNodeMatches[1] += ExtensibleTreeNode.getNodeCounter();
			} else {
				ExtensibleTreeNodeMatches[1] += ExtensibleTreeNode.getNodeCounter()* Math.pow(ebene, ebenenexponent);
			}
		}

		if (ebene < maxebene || maxebene <0) {
			// Kinder durchlaufen
			Iterator<String> kinder = ExtensibleTreeNode.getChildNodes().keySet().iterator();
			while (kinder.hasNext()) {
				String kindName = kinder.next();
				Double[] kindExtensibleTreeNodeMatches = ermittleExtensibleTreeNodeTrefferwert(ExtensibleTreeNode
						.getChildNodes().get(kindName), ebene + 1, maxebene,
						ebenenexponent);
				ExtensibleTreeNodeMatches[0] += kindExtensibleTreeNodeMatches[0];
				ExtensibleTreeNodeMatches[1] += kindExtensibleTreeNodeMatches[1];
			}
		}

		return ExtensibleTreeNodeMatches;
	}
	
	/**
	 * Gibt einen Baum zurueck, der nur aus den TrefferExtensibleTreeNode des Eingabebaumes besteht (ab der ersten Kindebene).
	 * @param k
	 * @return WurzelExtensibleTreeNode des Trefferbaumes
	 */
	public ExtensibleTreeNode trefferBaum(ExtensibleTreeNode k) {
		
		// ExtensibleTreeNode kopieren
		ExtensibleTreeNode neuerExtensibleTreeNode = new ExtensibleTreeNode();
		neuerExtensibleTreeNode.setNodeValue(k.getNodeValue());
		neuerExtensibleTreeNode.setNodeCounter(k.getNodeCounter());
		neuerExtensibleTreeNode.getAttributes().put("match",Boolean.parseBoolean(k.getAttributes().get("match").toString()));
		
		// KindExtensibleTreeNode durchlaufen
		Iterator<ExtensibleTreeNode> kinder = k.getChildNodes().values().iterator();
		while(kinder.hasNext()){
			ExtensibleTreeNode kind = kinder.next();
			if (Boolean.parseBoolean(kind.getAttributes().get("match").toString())){
				ExtensibleTreeNode neuesKind = this.trefferBaum(kind);
				neuerExtensibleTreeNode.getChildNodes().put(neuesKind.getNodeValue(), neuesKind);
			}
		}
		
		// Neuen ExtensibleTreeNode zurueckgeben
		return neuerExtensibleTreeNode;
		
	}
}
