package modules.visualizationModules;

import java.util.Comparator;

import modules.treeBuilder.Knoten;

public class KnotenKomparator implements Comparator<Knoten> {

	public KnotenKomparator() {
	}

	@Override
	public int compare(Knoten o1, Knoten o2) {
		int zaehlerUnterschied = o1.getZaehler()*2-o2.getZaehler()*2;
		if (zaehlerUnterschied == 0){
			if (o1.getName().compareTo(o2.getName())>0)
				return 1;
			else if (o1.getName().compareTo(o2.getName())<0)
				return -1;
		}
		return zaehlerUnterschied;
	}

}
