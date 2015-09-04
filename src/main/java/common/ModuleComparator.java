package common;

import java.util.Comparator;

import modules.Module;

public class ModuleComparator implements Comparator<Module> {

	@Override
	public int compare(Module o1, Module o2) {
		if (o1 != null && o2 != null && o1.getName() != null && o2.getName() != null)
			return o1.getName().compareTo(o2.getName());
		return 0;
	}

}
