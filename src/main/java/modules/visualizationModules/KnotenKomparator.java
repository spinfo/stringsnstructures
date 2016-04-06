package modules.visualizationModules;

import java.util.Comparator;

import models.ExtensibleTreeNode;

public class KnotenKomparator implements Comparator<ExtensibleTreeNode> {

	public KnotenKomparator() {
	}

	@Override
	public int compare(ExtensibleTreeNode o1, ExtensibleTreeNode o2) {
		int zaehlerUnterschied = o1.getNodeCounter()*2-o2.getNodeCounter()*2;
		if (zaehlerUnterschied == 0){
			if (o1.getNodeValue().compareTo(o2.getNodeValue())>0)
				return 1;
			else if (o1.getNodeValue().compareTo(o2.getNodeValue())<0)
				return -1;
		}
		return zaehlerUnterschied;
	}

}
