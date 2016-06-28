package modules.tree_building.treeBuilder;

import java.io.Serializable;
import java.util.HashMap;

public class Knoten implements Serializable {
	
	private static final long serialVersionUID = 8432201362823586839L;
	private int zaehler;
	private String name;
	private HashMap<String,Knoten> kinder;
	private boolean match = false;

	public Knoten() {
		super();
		this.zaehler = 0;
		this.kinder = new HashMap<String,Knoten>();
	}
	
	public Knoten(String name) {
		super();
		this.zaehler = 0;
		this.kinder = new HashMap<String,Knoten>();
		this.name = name;
	}

	public int inkZaehler(){
		return this.zaehler++;
	}
	
	public int getZaehler() {
		return zaehler;
	}

	public void setZaehler(int zaehler) {
		this.zaehler = zaehler;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setName(char name) {
		this.name = String.valueOf(name);
	}

	public HashMap<String, Knoten> getKinder() {
		return kinder;
	}

	public void setKinder(HashMap<String, Knoten> kinder) {
		this.kinder = kinder;
	}

	public boolean isMatch() {
		return match;
	}

	public void setMatch(boolean match) {
		this.match = match;
	}

	@Override
	public String toString() {
		return this.name+"["+this.zaehler+"]"; //+":"+this.kinder.size();
	}
}
