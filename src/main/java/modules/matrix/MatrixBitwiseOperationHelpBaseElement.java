package modules.matrix;

import java.util.ArrayList;

public class MatrixBitwiseOperationHelpBaseElement {
	ArrayList<String> members;
	
	public MatrixBitwiseOperationHelpBaseElement() {
		this.members=new ArrayList<String>();
	}
	
	public void add(String str){
		this.members.add(str);
	}
	
	public String get(int i){
		return this.members.get(i);
	}
	
	public int indexOf(String str){
		return this.members.indexOf(str);
	}

}
