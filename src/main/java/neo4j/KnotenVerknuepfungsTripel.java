package neo4j;

import org.neo4j.graphdb.Node;

public class KnotenVerknuepfungsTripel {
	
	private Node knotenQuelle;
	private Node knotenZiel;
	private Double gewicht;
	
	public KnotenVerknuepfungsTripel(Node knotenQuelle, Node knotenZiel,
			Double gewicht) {
		super();
		this.knotenQuelle = knotenQuelle;
		this.knotenZiel = knotenZiel;
		this.gewicht = gewicht;
	}

	public Node getKnotenQuelle() {
		return knotenQuelle;
	}

	public void setKnotenQuelle(Node knotenQuelle) {
		this.knotenQuelle = knotenQuelle;
	}

	public Node getKnotenZiel() {
		return knotenZiel;
	}

	public void setKnotenZiel(Node knotenZiel) {
		this.knotenZiel = knotenZiel;
	}

	public Double getGewicht() {
		return gewicht;
	}

	public void setGewicht(Double gewicht) {
		this.gewicht = gewicht;
	}

}
