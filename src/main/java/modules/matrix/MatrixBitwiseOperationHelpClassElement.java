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
	
	private void evaluate(MatrixBitwiseOperationHelpClassElement comp){
		System.out.println("MatrixBitwiseOperationHelpClassElement evaluate");
	}
	
	public void competition(ArrayList<MatrixBitwiseOperationHelpClassElement>classList,
			 ArrayList<MatrixBitwiseOperationHelpCompetionElement>competitionList,
			 HashMap<String, Integer>classesHashMap,
			 HashMap<String, Integer>competitionHashMap) {
		
		// get members of element
		Iterator<String> memberIterator = this.members.iterator();
				while (memberIterator.hasNext()) {
					String name=memberIterator.next();
					// check whether name is competing
					Integer competitionIndex=competitionHashMap.get(name);
					if(competitionIndex!=null) {System.out.print
						("competition name: "
								+ name+ " "+competitionIndex);
						// the competitionIndex identifies competitionElement in
						// competitionList; get competition element;
						// value of competitionIndex is position in list +1, 
						// therefore competitionIndex-1
						MatrixBitwiseOperationHelpCompetionElement competitionElement=
							competitionList.get(competitionIndex-1);	
							for (int i=0;i<competitionElement.members.size();i++){
								String competitionName=competitionElement.members.get(i);
								if (!name.equals(competitionName)){
									System.out.println
									(" competitionname: "
											+ competitionName);
									// get competition class in classesHashMap
									Integer classIndex=classesHashMap.get(competitionName);
									System.out.println(" classIndex: "+ classIndex);
									if (classIndex!=null){
										// get classelement
										MatrixBitwiseOperationHelpClassElement competClassElement=
											classList.get((int)classIndex);
									
										// evaluate and remove less evaluated
										evaluate(/*this*/competClassElement);
									}
									
								
								}
							}//for (int i=0;i<competitionElement.members.size();i++)
							System.out.println();					
		
					}
					
				}
		
	}

}
