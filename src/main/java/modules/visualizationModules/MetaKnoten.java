package modules.visualizationModules;

import models.ExtensibleTreeNode;

/**
 * Class that wraps a Knoten object with some metadata.
 * @author Marcel Boeing
 *
 */
public class MetaKnoten {

	private ExtensibleTreeNode knoten;
	private int position;
	private int r;
	private int g;
	private int b;
	
	public MetaKnoten(ExtensibleTreeNode knoten, int position, int r, int g, int b) {
		super();
		this.knoten = knoten;
		this.position = position;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	/**
	 * @return the knoten
	 */
	public ExtensibleTreeNode getKnoten() {
		return knoten;
	}

	/**
	 * @param knoten the knoten to set
	 */
	public void setKnoten(ExtensibleTreeNode knoten) {
		this.knoten = knoten;
	}

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * @return the r
	 */
	public int getR() {
		return r;
	}

	/**
	 * @param r the r to set
	 */
	public void setR(int r) {
		this.r = r;
	}

	/**
	 * @return the g
	 */
	public int getG() {
		return g;
	}

	/**
	 * @param g the g to set
	 */
	public void setG(int g) {
		this.g = g;
	}

	/**
	 * @return the b
	 */
	public int getB() {
		return b;
	}

	/**
	 * @param b the b to set
	 */
	public void setB(int b) {
		this.b = b;
	}
	
}
