package common;

import java.util.Arrays;
import java.util.Comparator;

public class CoOccList {
	
	private CoOcc[] coOccs;
	private int nextIndex;
	
	public CoOccList(){
		coOccs = new CoOcc[1];
		nextIndex = 0;
	}
	
	public void add(String coOcc){
		//check array size
		if (coOccs.length <= nextIndex)
			rescale();
		
		//check if already there
		int index;
		if ((index = getIndex(coOcc)) > -1){
			coOccs[index].increase();
		} else {
			coOccs[nextIndex] = new CoOcc(coOcc);
			nextIndex++;
		}
	}
	
	private int getIndex(String coOcc){
		for (int i = 0; i < coOccs.length; i++) {
			if (coOccs[i].getCoOcc().equals(coOcc)){
				return i;
			}
		}
		return -1;
	}
	
	private void rescale(){
		coOccs = Arrays.copyOf(coOccs, coOccs.length + 5);
	}
	
	public CoOcc[] finalizeArray(){
		coOccs = Arrays.copyOf(coOccs, nextIndex);
		Arrays.sort(coOccs, new CoOccComparator());
		coOccs = Arrays.copyOf(coOccs, 30);
		return coOccs;
	}
	
	public CoOccComparator getCoOccComparator(){
		return new CoOccComparator();
	}
	
	private class CoOcc {
		private String coOcc;
		private int count;
		
		public CoOcc (String coOcc){
			this.coOcc = coOcc;
			count = 0;
		}

		public String getCoOcc() {
			return coOcc;
		}

		public int getCount() {
			return count;
		}

		public void increase(){
			count++;
		}
	}
	
	private class CoOccComparator implements Comparator<CoOcc>{
		@Override
		public int compare(CoOcc o1, CoOcc o2) {
			return o2.getCount() - o1.getCount();
		}
	}

}
