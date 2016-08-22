package modules.transitionNetwork.elements;

public class SuffixElement extends AbstractElement {
	public int start;
	public int end;

	public SuffixElement(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public String writeSuffix(char[] text, boolean inverted) {
		StringBuilder sb = new StringBuilder();
		if (inverted) {
			int end = this.end - 1;
			if (text[end] == '$')
				end--;
			for (int i = end; i >= this.start; i--) {
				sb.append(text[i]);
			}
			if ((this.start == 0) || (text[this.start - 1] == '$'))
				sb.append('$');
		} else
			for (int i = this.start; i < this.end; i++) {
				sb.append(text[i]);
			}
		return sb.toString();
	}
}
