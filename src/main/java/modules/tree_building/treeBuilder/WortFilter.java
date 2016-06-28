package modules.tree_building.treeBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WortFilter {
	
	private ArrayList<String> filterWoerter;

	public WortFilter() {
		super();
		this.filterWoerter = new ArrayList<String>();
	}

	public List<String> getFilterWoerter() {
		return filterWoerter;
	}

	public boolean addWort(String wort) {
		return this.filterWoerter.add(wort);
	}
	
	/**
	 * Gibt true zurueck, wenn der uebergebene Satz auf den Filter passt.
	 * @param satz Sentence
	 * @return True if sentence contains the specified word
	 */
	public boolean hatWort(String[] satz){
		for (int i=0; i<satz.length; i++){
			if (this.filterWoerter.contains(satz[i])) return true;
		}
		return false;
	}
	
	/**
	 * Gibt true zurueck, wenn der uebergebene Satz auf den Filter passt.
	 * @param satz Sentence
	 * @return True if sentence contains the specified word
	 */
	public boolean hatWort(List<String> satz){
		Iterator<String> worte = satz.iterator();
		while(worte.hasNext()){
			if (this.filterWoerter.contains(worte.next())) return true;
		}
		return false;
	}
	
	/**
	 * Gibt einen Array mit Indices der Worte zurueck, auf die der Filter passt.
	 * @param satz Sentence
	 * @return Array with indices of words that match this filter
	 */
	public Integer[] getWortIndices(List<String> satz){
		ArrayList<Integer> indices = new ArrayList<Integer>();
		Iterator<String> worte = satz.iterator();
		int index = 0;
		while(worte.hasNext()){
			if (this.filterWoerter.contains(worte.next())) indices.add(new Integer(index));
			index ++;
		}
		return indices.toArray(new Integer[indices.size()]);
	}
	
	/**
	 * Gibt einen Array mit Indices der Worte zurueck, auf die der Filter passt.
	 * @param satz Sentence
	 * @return Array with indices of words that match this filter
	 */
	public Integer[] getWortIndices(String[] satz){
		ArrayList<Integer> indices = new ArrayList<Integer>();
		for (int i=0; i<satz.length; i++){
			if (this.filterWoerter.contains(satz[i])) indices.add(new Integer(i));
		}
		return indices.toArray(new Integer[indices.size()]);
	}

}
