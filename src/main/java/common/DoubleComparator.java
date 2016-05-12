package common;

import java.util.Comparator;

public class DoubleComparator implements Comparator<Double> {

	@Override
	public int compare(Double o1, Double o2) {
		Double d = new Double(o1-o2);
		int result;
		if (d != 0d && d<1 && d>-1 ){
			if (d<0)
				result = -1;
			else
				result = 1;
		} else
			result = d.intValue();
			return result;
	}

}
