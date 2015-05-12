package helpers;

import java.io.File;
import java.io.FileFilter;

public class RegAusdruckDateiFilter implements FileFilter {
	
	private String regAusdruck;


	public RegAusdruckDateiFilter(String regAusdruck) {
		super();
		this.regAusdruck = regAusdruck;
	}
	

	public String getRegAusdruck() {
		return regAusdruck;
	}


	public void setRegAusdruck(String regAusdruck) {
		this.regAusdruck = regAusdruck;
	}


	@Override
	public boolean accept(File pathname) {
		if (pathname.isFile() && pathname.getName().matches(this.regAusdruck)) return true;
		return false;
	}

}
