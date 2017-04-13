package modules.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

//
// element of list of classes, contains members of class
// evaluates class
// resolves class competition and conflicts
//
public class MatrixBitwiseOperationHelpClassElement extends MatrixBitwiseOperationHelpBaseElement{
	
	
	ArrayList<String> adjacentMembers;
	int classIndex;
	public MatrixBitwiseOperationHelpClassElement(int index){
		this.classIndex=index;
		this.adjacentMembers=new ArrayList<String>();
	}
	
	
	public void writeClass(){
		if(!members.isEmpty() && !adjacentMembers.isEmpty()){
			System.out.print("-----classIndex "+ classIndex+"-----adjacent ");
			for(String adjacent:adjacentMembers)System.out.print(adjacent+" ");
			System.out.println();
			for(String member:members)System.out.println(member+" ");			
		}
	}
	
	public void competition(ArrayList<MatrixBitwiseOperationHelpClassElement>classList,
			 ArrayList<MatrixBitwiseOperationHelpCompetionElement>competitionList,
			 HashMap<String, Integer>competitionHashMap) {
		
		// get members of element
		Iterator<String> memberIterator = this.members.iterator();
				while (memberIterator.hasNext()) {
					String name=memberIterator.next();
					// check whether name is competing
					Integer index=competitionHashMap.get(name);
					if(index!=null) {System.out.println
						("MatrixBitwiseOperationHelpClassElement competition: "
								+ name+ " competitionIdent: "+index);
						// the index identifies competitionElement in
						// competitionList; get competition element;
						// value of index is position in list +1, therefore index-1
						MatrixBitwiseOperationHelpCompetionElement competitionElement=
							competitionList.get(index-1);	
							for (int i=0;i<competitionElement.members.size();i++){
								String competitionName=competitionElement.members.get(i);
								if (!name.equals(competitionName)){
									System.out.println
								("MatrixBitwiseOperationHelpClassElement competition: "
								+ competitionName);
									// get competition class 
								}
							}
						
						
					
		
					
		// evaluate and remove less evaluated
					}
					
				}
		
	}

}
