package treeBuilder;

import java.util.HashMap;

public class Knoten {
	
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
		return this.zaehler+""; //+":"+this.kinder.size();
	}
}
