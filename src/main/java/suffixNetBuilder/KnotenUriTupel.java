package suffixNetBuilder;

import java.net.URI;

import treeBuilder.Knoten;

public class KnotenUriTupel {

	private Knoten knoten;
	private URI uri;
	public KnotenUriTupel() {
		super();
	}
	public KnotenUriTupel(Knoten knoten, URI uri) {
		super();
		this.knoten = knoten;
		this.uri = uri;
	}
	/**
	 * @return the knoten
	 */
	public Knoten getKnoten() {
		return knoten;
	}
	/**
	 * @param knoten the knoten to set
	 */
	public void setKnoten(Knoten knoten) {
		this.knoten = knoten;
	}
	/**
	 * @return the uri
	 */
	public URI getUri() {
		return uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setUri(URI uri) {
		this.uri = uri;
	}
	
}
