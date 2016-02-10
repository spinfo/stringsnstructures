package modules.treeSimilarityClustering;

import java.util.ArrayList;
import java.util.List;

import models.ExtensibleTreeNode;
import common.parallelization.CallbackProcess;
import common.parallelization.CallbackReceiver;

public class NodePartnerProcessor implements CallbackProcess {
	
	private CallbackReceiver callbackReceiver;
	private MetaNode einsamerExtensibleTreeNode;
	private List<MetaNode> partnerPool;
	private NodeComparator komparator;
	
	// Zeigt an, ob im Kombinationsbaum nur die TrefferExtensibleTreeNode enthalten sein sollen
	private boolean behalteNurTreffer;

	public NodePartnerProcessor(CallbackReceiver callbackReceiver,
			MetaNode einsamerExtensibleTreeNode, List<MetaNode> partnerPool,
			NodeComparator komparator, boolean behalteNurTreffer) {
		super();
		this.callbackReceiver = callbackReceiver;
		this.einsamerExtensibleTreeNode = einsamerExtensibleTreeNode;
		this.partnerPool = partnerPool;
		this.komparator = komparator;
		this.behalteNurTreffer = behalteNurTreffer;
	}

	public MetaNode getEinsamerExtensibleTreeNode() {
		return einsamerExtensibleTreeNode;
	}

	public void setEinsamerExtensibleTreeNode(MetaNode einsamerExtensibleTreeNode) {
		this.einsamerExtensibleTreeNode = einsamerExtensibleTreeNode;
	}

	public List<MetaNode> getPartnerPool() {
		return partnerPool;
	}

	public void setPartnerPool(ArrayList<MetaNode> partnerPool) {
		this.partnerPool = partnerPool;
	}

	public NodeComparator getKomparator() {
		return komparator;
	}

	public void setKomparator(NodeComparator komparator) {
		this.komparator = komparator;
	}

	public boolean isBehalteNurTreffer() {
		return behalteNurTreffer;
	}

	public void setBehalteNurTreffer(boolean behalteNurTreffer) {
		this.behalteNurTreffer = behalteNurTreffer;
	}

	@Override
	public void run() {
		
		try {

			// Variable fuer bisherig besten Vergleichswert
			Double besterVergleichswert = 0d;
			
			// Variable fuer Bestpassendsten ExtensibleTreeNode
			MetaNode besterPartner = null;
			
			// Variable fuer Kombinationsbaum aus beiden ExtensibleTreeNode
			ExtensibleTreeNode besterKombinationsBaumWurzel = null;
			
			// Groesse des Partnerpools ermitteln
			int partnerPoolGroesse;
			synchronized(this){
				partnerPoolGroesse = this.partnerPool.size();
			}
			
			// ExtensibleTreeNode des Partnerpools durchlaufen
			for (int i=0; i<partnerPoolGroesse; i++){
				
				// Aktuellen ExtensibleTreeNode ermitteln
				MetaNode ExtensibleTreeNode = null;
				synchronized(this){
					try{
						ExtensibleTreeNode = this.partnerPool.get(i);
					} catch (Exception e){
						e.printStackTrace();
					}
				}
				
				// Vergleich mit leerem ExtensibleTreeNode oder mit sich selbst ausschliessen
				if (ExtensibleTreeNode == null || ExtensibleTreeNode.equals(this.einsamerExtensibleTreeNode)){
					continue;
				}
				
				// Baeume miteinander kombinieren
				ExtensibleTreeNode kombinationsBaumWurzel = this.komparator.verschmelzeBaeume(einsamerExtensibleTreeNode.getKnoten(), ExtensibleTreeNode.getKnoten());
				Double[] trefferWert = this.komparator.ermittleExtensibleTreeNodeTrefferwert(kombinationsBaumWurzel);
				Double vergleichswert =  new Double(trefferWert[0] / trefferWert[1]);
				
				// Ergebnis auswerten
				if (vergleichswert > besterVergleichswert){
					besterVergleichswert = vergleichswert;
					besterPartner = ExtensibleTreeNode;
					besterPartner.setUebereinstimmungsQuotient(vergleichswert);
					besterKombinationsBaumWurzel = kombinationsBaumWurzel;
				}
				
			}
			
			// Den einsamen ExtensibleTreeNode zurueckgeben, wenn GAR KEINE Uebereinstimmung gefunden wurde
			if (besterPartner == null){
				// Ergebnis an CallbackReceiver zurueckgeben
				this.callbackReceiver.receiveCallback(Thread.currentThread(), this.einsamerExtensibleTreeNode);
			}
			
			// Ansonsten werden entsprechende MetaNode geschaffen und als Kombination zurueckgegeben
			else {
				
				// Ggf. Vergleichsbaum auf TrefferExtensibleTreeNode beschraenken
				if (this.behalteNurTreffer){
					besterKombinationsBaumWurzel = this.komparator.trefferBaum(besterKombinationsBaumWurzel);
				}
				
				// MetaNode mit dem kombinierten Vergleichsbaum erstellen
				MetaNode vergleichsbaumMetaNode = new MetaNode(besterKombinationsBaumWurzel);
				vergleichsbaumMetaNode.getKindMetaNode().add(this.einsamerExtensibleTreeNode);
				vergleichsbaumMetaNode.getKindMetaNode().add(besterPartner);
				vergleichsbaumMetaNode.setUebereinstimmungsQuotient(besterVergleichswert);
				
				// Ergebnis an CallbackReceiver zurueckgeben
				this.callbackReceiver.receiveCallback(Thread.currentThread(), vergleichsbaumMetaNode);
			}
			
		} catch (Exception e){
			this.callbackReceiver.receiveException(Thread.currentThread(), e);
		}
		
		
	}

	@Override
	public CallbackReceiver getCallbackReceiver() {
		return this.callbackReceiver;
	}

	@Override
	public void setCallbackReceiver(CallbackReceiver CallbackReceiver) {
		this.callbackReceiver = CallbackReceiver;
	}
}
