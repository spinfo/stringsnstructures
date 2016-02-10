package modules.treeSimilarityClustering;

import java.util.List;
import java.util.logging.Logger;

import common.parallelization.Action;

public class NodePartnerProcessorCallbackAction extends Action {
private List<MetaNode> MetaNodePoolNaechsterEbene;
	
	public NodePartnerProcessorCallbackAction(List<MetaNode> MetaNodePoolNaechsterEbene) {
		super();
		this.MetaNodePoolNaechsterEbene = MetaNodePoolNaechsterEbene;
	}

	public void ausfuehren(Object prozessErgebnis) {
		
		// Pruefen, ob Ergebnisobjekt vom richtigen Typ ist; andernfalls Warnung ausgeben
		if (!prozessErgebnis.getClass().equals(MetaNode.class)){
			Logger.getLogger(this.getClass().getSimpleName()).warning("Ergebnis des Vergleichprozesses ist kein MetaNode.");
		}
		
		// Ergebnis auf MetaNode casten und zum Pool der naechsten Ebene hinzufuegen
		else {
			MetaNode mk = (MetaNode) prozessErgebnis;
			synchronized(this){
				MetaNodePoolNaechsterEbene.add(mk);
			}
			// Meldung ausgeben
			Logger.getLogger(this.getClass().getSimpleName()).info("MetaNode \""+mk.getKnoten().getNodeValue()+"\" hinzugefuegt.");
		}
		
	}
}
