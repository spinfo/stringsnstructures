package modules.transitionNetwork.elements.comparator;

import java.util.Comparator;
import modules.transitionNetwork.elements.SuffixElement;

public class SuffixComparator implements Comparator<SuffixElement> {

	char[] text;

	public SuffixComparator(char[] text) {
		this.text = text;
	}

	@Override
	public int compare(SuffixElement e1, SuffixElement e2) {
		int i = e1.start;
		int j = e2.start;
		while ((i < e1.end) && (j < e2.end)) {
			if (this.text[i] > this.text[j])
				return 1;
			else if (this.text[i] < this.text[j])
				return -1;
			i++;
			j++;
		}
		if (i < e1.end)
			return -1;
		else if (j < e2.end)
			return 1;
		return 0;

	}
}
