package common;

import java.io.File;
import java.io.FileFilter;

public class DirectoryFilter implements FileFilter {

	@Override
	public boolean accept(File pathname) {
		if (pathname.isDirectory()) return true;
		return false;
	}

}
