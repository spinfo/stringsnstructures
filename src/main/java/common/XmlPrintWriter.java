package common;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

public class XmlPrintWriter extends PrintWriter {

	private static final String openTag = "<";
	private static final String closeTag = "</";
	private static final String endTag = ">";

	public XmlPrintWriter(FileWriter fw) {
		super(new PrintWriter(fw));
	}
	
	public XmlPrintWriter(StringWriter sw) {
		super(new PrintWriter(sw));
	}

	public void printTag(String tag, boolean start, int nrTabs, boolean newline) {
		for (int i = 0; i < nrTabs; i++) {
			this.print("\t");
		}
		if (start)
			this.print(openTag);
		else
			this.print(closeTag);
		this.print(tag);
		this.print(endTag);
		if (newline)
			this.println();
	}

	public void printInt(int i) {
		this.print(String.valueOf(i));
	}
}