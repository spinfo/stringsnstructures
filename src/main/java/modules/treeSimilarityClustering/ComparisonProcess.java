package modules.treeSimilarityClustering;

import java.util.concurrent.ConcurrentHashMap;

import common.parallelization.CallbackProcess;
import common.parallelization.CallbackReceiver;
import models.ExtensibleTreeNode;

public class ComparisonProcess implements CallbackProcess {
	private Double schwellwert;
	private ExtensibleTreeNode ExtensibleTreeNode;
	private ExtensibleTreeNode vergleichsExtensibleTreeNode;
	private ExtensibleTreeNode ExtensibleTreeNode2;
	private ExtensibleTreeNode vergleichsExtensibleTreeNode2;
	private ConcurrentHashMap<String,Double> verknuepfungen;
	private Progress progress;
	private int maxComparisonDepth;


	public ComparisonProcess(int maxComparisonDepth, Double schwellwert, ExtensibleTreeNode ExtensibleTreeNode,
			ExtensibleTreeNode vergleichsExtensibleTreeNode,
			ConcurrentHashMap<String, Double> verknuepfungen,
			Progress Progress) {
		this(maxComparisonDepth, schwellwert, ExtensibleTreeNode, vergleichsExtensibleTreeNode, null, null, verknuepfungen, Progress);
	}
	
	public ComparisonProcess(int maxComparisonDepth, Double schwellwert, ExtensibleTreeNode ExtensibleTreeNode,
			ExtensibleTreeNode vergleichsExtensibleTreeNode, ExtensibleTreeNode ExtensibleTreeNode2,
			ExtensibleTreeNode vergleichsExtensibleTreeNode2,
			ConcurrentHashMap<String, Double> verknuepfungen,
			Progress Progress) {
		super();
		this.maxComparisonDepth = maxComparisonDepth;
		this.schwellwert = schwellwert;
		this.ExtensibleTreeNode = ExtensibleTreeNode;
		this.vergleichsExtensibleTreeNode = vergleichsExtensibleTreeNode;
		this.ExtensibleTreeNode2 = ExtensibleTreeNode2;
		this.vergleichsExtensibleTreeNode2 = vergleichsExtensibleTreeNode2;
		this.verknuepfungen = verknuepfungen;
		this.progress = Progress;
	}

	@Override
	public void run() {
		if (this.vergleichsExtensibleTreeNode2 != null && this.ExtensibleTreeNode2 != null){
			vergleicheMulti(maxComparisonDepth,schwellwert, ExtensibleTreeNode, vergleichsExtensibleTreeNode, ExtensibleTreeNode2, vergleichsExtensibleTreeNode2, verknuepfungen);
		} else {
			vergleiche(maxComparisonDepth,schwellwert, ExtensibleTreeNode, vergleichsExtensibleTreeNode, verknuepfungen);
		}
	}
	
	private void vergleiche(int maxComparisonDepth, Double schwellwert, ExtensibleTreeNode ExtensibleTreeNode, ExtensibleTreeNode vergleichsExtensibleTreeNode, ConcurrentHashMap<String,Double> verknuepfungen){
		// Komparator instanziieren
		NodeComparator komparator = new NodeComparator();
		komparator.setMaximaleAuswertungsEbene(maxComparisonDepth);
		
		// Vergleich anstellen
		Double uebereinstimmungsQuotient = komparator.vergleiche(ExtensibleTreeNode, vergleichsExtensibleTreeNode);

		// Ggf. Kante zwischen beiden ExtensibleTreeNode erstellen
		if (uebereinstimmungsQuotient > schwellwert) {
			verknuepfungen.put(vergleichsExtensibleTreeNode.getNodeValue(), uebereinstimmungsQuotient);
		}
		
		// Progress nachhalten
		progress.countOne();
	}
	
	private void vergleicheMulti(int maxComparisonDepth, Double schwellwert, ExtensibleTreeNode ExtensibleTreeNode1, ExtensibleTreeNode vergleichsExtensibleTreeNode1, ExtensibleTreeNode ExtensibleTreeNode2, ExtensibleTreeNode vergleichsExtensibleTreeNode2, ConcurrentHashMap<String,Double> verknuepfungen){
		// Komparator instanziieren
		NodeComparator komparator = new NodeComparator();
		komparator.setMaximaleAuswertungsEbene(maxComparisonDepth);
		
		// Vergleiche anstellen
		Double uebereinstimmungsQuotient = (komparator.vergleiche(ExtensibleTreeNode1, vergleichsExtensibleTreeNode1) + komparator.vergleiche(ExtensibleTreeNode2, vergleichsExtensibleTreeNode2)) / 2d;

		// Ggf. Kante zwischen beiden ExtensibleTreeNode erstellen
		if (uebereinstimmungsQuotient > schwellwert) {
			verknuepfungen.put(vergleichsExtensibleTreeNode1.getNodeValue(), uebereinstimmungsQuotient);
		}
		
		// Progress nachhalten
		progress.countOne();
	}

	@Override
	public CallbackReceiver getCallbackReceiver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCallbackReceiver(CallbackReceiver callbackReceiver) {
		// TODO Auto-generated method stub
		
	}
}
