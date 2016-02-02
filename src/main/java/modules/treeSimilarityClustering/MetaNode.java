package modules.treeSimilarityClustering;

import java.util.ArrayList;
import java.util.List;

import models.ExtensibleTreeNode;

public class MetaNode {
	
	public static final String KEY_MATCHING = "match";
	
	private ExtensibleTreeNode knoten;
	private List<MetaNode> kindMetaNode = new ArrayList<MetaNode>();
	private Double uebereinstimmungsQuotient;
	
	public MetaNode(ExtensibleTreeNode knoten) {
		super();
		this.knoten = knoten;
	}
	public ExtensibleTreeNode getKnoten() {
		return knoten;
	}
	public void setKnoten(ExtensibleTreeNode knoten) {
		this.knoten = knoten;
	}
	public List<MetaNode> getKindMetaNode() {
		return kindMetaNode;
	}
	public void setKindMetaNode(List<MetaNode> kindMetaNode) {
		this.kindMetaNode = kindMetaNode;
	}
	public Double getUebereinstimmungsQuotient() {
		return uebereinstimmungsQuotient;
	}
	public void setUebereinstimmungsQuotient(Double uebereinstimmungsQuotient) {
		this.uebereinstimmungsQuotient = uebereinstimmungsQuotient;
	}
}
