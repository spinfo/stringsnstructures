package modules.treeSimilarityClustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import models.ExtensibleTreeNode;

import common.parallelization.CallbackReceiverImpl;

public class MetaTreeBuilder extends CallbackReceiverImpl {
	/*
	 * Variablen
	 */
	
	// Liste der laufenden Prozesse
	//private HashMap<NodePartnerProcessor, Action> rueckmeldeAktionen = new HashMap<NodePartnerProcessor, Action>();

	// NodeComparator
	private NodeComparator komparator = new NodeComparator();

	// Liste fuer MetaNode der naechsten Ebene; wird von den Vergleichsprozessen befuellt
	private List<MetaNode> MetaNodePoolNaechsterEbene;
	
	// Zeigt an, welches Element des MetaNodepools als naechstes verarbeitet werden soll.
	private int MetaNodePoolVerarbeitungsIndex;

	// MetaNodeliste
	private List<MetaNode> MetaNodePool = null;

	// Nur TrefferExtensibleTreeNode in Vergleichsbaeumen abbilden
	private boolean behalteNurTreffer;
	
	// Gleichzeitig auszufuehrende Prozesse
	private int gleichzeitigeProzesse = 1;
	
	// Variable fuer Ergebnis
	private List<MetaNode> ergebnisListe = null;

	/*
	 * Konstruktor
	 */
	public MetaTreeBuilder(NodeComparator komparator,
			List<MetaNode> MetaNodePool, boolean behalteNurTreffer, int gleichzeitigeProzesse) {
		super();
		this.komparator = komparator;
		this.MetaNodePool = MetaNodePool;
		this.behalteNurTreffer = behalteNurTreffer;
		this.gleichzeitigeProzesse = gleichzeitigeProzesse;
	}

	/*
	 * Getter und Setter
	 */
	public NodeComparator getKomparator() {
		return komparator;
	}

	public void setKomparator(NodeComparator komparator) {
		this.komparator = komparator;
	}

	public List<MetaNode> getMetaNodePool() {
		return MetaNodePool;
	}

	public void setMetaNodePool(List<MetaNode> MetaNodePool) {
		this.MetaNodePool = MetaNodePool;
	}

	public boolean isBehalteNurTreffer() {
		return behalteNurTreffer;
	}

	public void setBehalteNurTreffer(boolean behalteNurTreffer) {
		this.behalteNurTreffer = behalteNurTreffer;
	}
	
	public int getGleichzeitigeProzesse() {
		return gleichzeitigeProzesse;
	}

	public void setGleichzeitigeProzesse(int gleichzeitigeProzesse) {
		this.gleichzeitigeProzesse = gleichzeitigeProzesse;
	}

	/**
	 * Gibt den Ergebnisbaum des juengsten Aufrufs von baueBaum() zurueck.
	 * @return
	 */
	public List<MetaNode> getErgebnisListe() {
		return ergebnisListe;
	}

	/*
	 * Baumerstellung und -manipulation
	 */

	/**
	 * Konstruiert neue Ebene von MetaNode.
	 * @return Liste der neuen MetaNode
	 */
	public List<MetaNode> baueBaum() {
		
		// Ergebnisvariable loeschen
		this.ergebnisListe = null;
		
		// Neue Instanz fuer MetaNodepool der naechsten Ebene
		MetaNodePoolNaechsterEbene = new ArrayList<MetaNode>();
		
		// Verarbeitungsindex auf erstes Element setzen, das nach den initialen Prozessen verarbeitet werden soll.
		MetaNodePoolVerarbeitungsIndex = gleichzeitigeProzesse;
		
		// Schleife ueber Anzahl der maximalen Parallelprozesse
		for (int i=0; (i<gleichzeitigeProzesse && i<MetaNodePool.size()); i++){
			
			// Rueckmelde-Aktion definieren
			NodePartnerProcessorCallbackAction aktion = new NodePartnerProcessorCallbackAction(MetaNodePoolNaechsterEbene);
			
			// Naechsten MetaNode ermitteln, falls vorhanden (Es ist bei sehr kleinen Korpora moeglich, dass die bereits gestarteten Prozesse zu diesem Zeitpunkt alles schon abgearbeitet haben).
			MetaNode naechsterExtensibleTreeNode = MetaNodePool.get(i);
			
			if (naechsterExtensibleTreeNode != null){
				// Prozessor instanziieren
				NodePartnerProcessor prozessor = new NodePartnerProcessor(this, naechsterExtensibleTreeNode, MetaNodePool, komparator, behalteNurTreffer);
				
				// Prozessor in Prozess kapseln, mit Aktion in Liste aufnehmen und nebenlaeufig starten
				Thread prozess = new Thread(prozessor);
				this.registerSuccessCallback(prozess, aktion);
				prozess.start();
			}
		}
		
		// Auf Ergebnisse warten und derweil Meldungen ausgeben
		while(this.ergebnisListe == null){
			
			// Prozess schlafen legen
			try {
				Thread.sleep(1500l);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Meldung ausgeben
			//Logger.getLogger(this.getClass().getSimpleName()).info("Konstruiere MetaNodebaum. Laufende Prozesse: "+this.rueckmeldeAktionen.size());
		}
		
		return this.ergebnisListe;
	}

	@Override
	public void receiveCallback(Thread prozess, Object ergebnis) {
		
		// Falls Ergebnis Null ist, abbrechen
		if (ergebnis == null){
			Logger.getLogger(this.getClass().getCanonicalName()).warning("Null-Ergebnis empfangen (wird ignoriert).");
			return;
		}
		
		// Aktion ermitteln und aus Liste loeschen
		//Aktion ergebnisAktion = this.rueckmeldeAktionen.get(prozess);
		
		super.receiveCallback(prozess, ergebnis);
		
		synchronized (this) {
			
			// Verarbeitungsindex erhoehen
			this.MetaNodePoolVerarbeitungsIndex++;

			// Pruefen, ob noch MetaNode im Pool verbleiben
			if (this.MetaNodePoolVerarbeitungsIndex<this.MetaNodePool.size()) {

				// Pruefen, ob weitere MetaNode existieren, die
				// verarbeitet werden sollen
				MetaNode naechsterExtensibleTreeNode = null;
				try {
					naechsterExtensibleTreeNode = this.MetaNodePool.get(MetaNodePoolVerarbeitungsIndex);

				} catch (NoSuchElementException e) {
					e.printStackTrace();
				}

				// Falls noch ein ExtensibleTreeNode existiert, wird der naechste
				// Prozessor initiiert und gestartet
				if (naechsterExtensibleTreeNode != null) {
					
					Logger.getLogger(this.getClass().getCanonicalName()).info("Erstelle naechsten Prozess ("+this.MetaNodePoolVerarbeitungsIndex+" / "+this.MetaNodePool.size());
					
					// Rueckmelde-Aktion definieren
					NodePartnerProcessorCallbackAction aktion = new NodePartnerProcessorCallbackAction(MetaNodePoolNaechsterEbene);

					// Prozessor instanziieren
					NodePartnerProcessor prozessor = new NodePartnerProcessor(
							this, naechsterExtensibleTreeNode, MetaNodePool,
							komparator, behalteNurTreffer);

					// Prozessor in Prozess kapseln, mit Aktion in Liste aufnehmen und nebenlaeufig starten
					Thread naechsterProzess = new Thread(prozessor);
					this.registerSuccessCallback(naechsterProzess, aktion);
					naechsterProzess.start();
				}
			} else {
				// Keine ExtensibleTreeNode mehr verbleibend - pruefen, ob noch Prozesse
				// laufen, oder die Bearbeitung der aktuellen Beumebene
				// (bzw. des aktuellen MetaNodepools) abgeschlossen ist.

				if (this.getRegisteredThreads().size() <= 1) {
					// Meldung ausgeben
					Logger.getLogger(this.getClass().getCanonicalName()).info(
							"Bearbeitung des aktuellen Pools abgeschlossen. Der neue Pool hat "
									+ this.MetaNodePoolNaechsterEbene
											.size() + " Elemente.");

					// Ergebnis speichern
					this.ergebnisListe = MetaNodePoolNaechsterEbene;
				}
			}

			// Prozess aus Liste entfernen
			//this.rueckmeldeAktionen.remove(prozess);

		}
		
	}
	
	/**
	 * Bildet eine MetaNodestruktur mit ExtensibleTreeNode ab (verwirft also die Tokendimension).
	 * Der Uebereinstimmungsquotient des MetaNodes wird dabei in Promille im Zaehler
	 * des ExtensibleTreeNodes angegeben.
	 * @param mk
	 * @return Wurzel des neuen Baumes
	 */
	public ExtensibleTreeNode konvertiereMetaNodeZuExtensibleTreeNode(MetaNode mk){
		if (mk == null || mk.getKnoten() == null) return null;
		ExtensibleTreeNode k = new ExtensibleTreeNode();
		k.setNodeValue(mk.getKnoten().getNodeValue());
		if (mk.getUebereinstimmungsQuotient() != null){
			k.setNodeCounter((int) (mk.getUebereinstimmungsQuotient()*1000d));
			k.getAttributes().put(MetaNode.KEY_MATCHING, new Boolean(true));
		}
		
		Iterator<MetaNode> kinder = mk.getKindMetaNode().iterator();
		while(kinder.hasNext()){
			ExtensibleTreeNode kind = konvertiereMetaNodeZuExtensibleTreeNode(kinder.next());
			if (kind != null)
			k.getChildNodes().put(kind.getNodeValue(), kind);
		}
		
		return k;
	}
}
