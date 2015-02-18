package suffixTree.suffixTree.applications;

import java.io.FileWriter;
import java.io.PrintWriter;

public class XmlPrintWriter extends PrintWriter {

	private String openTag = "<";
	private String closeTag = "</";
	private String endTag = ">";

	public XmlPrintWriter(FileWriter fw) {
		super(new PrintWriter(fw));
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