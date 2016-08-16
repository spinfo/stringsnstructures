package modules.transitionNetwork.elements;

public class SuffixElement extends AbstractElement{
	public int start;
	public int end;
	
	public SuffixElement(int start,int end){
		this.start=start;
		this.end=end;
	}
	public void writeSuffix(char[]text,boolean inverted){
		if (inverted) {
			int end=this.end-1;
			if(text[end]=='$')end--;
			for (int i=end; i>=this.start;i--){
				System.out.print(text[i]);
			}
			if((this.start==0) || (text[this.start-1]=='$')) System.out.print('$');
		}
		else
		for (int i=this.start; i<this.end;i++){
			System.out.print(text[i]);
		}
		System.out.println();
	}
}
