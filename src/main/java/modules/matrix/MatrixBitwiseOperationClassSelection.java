package modules.matrix;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

//
// element of list of classes, contains members of class
// evaluates class
// resolves class competition and conflicts
//
public class MatrixBitwiseOperationClassSelection extends MatrixBitwiseOperationHelpBaseElement{
	
	
	ArrayList<String> adjacentMembers;
	private int classNr;
	public MatrixBitwiseOperationClassSelection(int classNr){
		this.classNr=classNr;
		this.adjacentMembers=new ArrayList<String>();
	}
	
	
	public void writeClass(PrintWriter writer,String message){
		if(!members.isEmpty() && !adjacentMembers.isEmpty()){
			writer.print(message+"-----classNr "+ this.classNr+"-----adjacent ");
			for(String adjacent:adjacentMembers)writer.print(adjacent+" ");
			writer.println();
			for(String member:members)writer.println(member+" ");			
		}
	}
	
	private void evaluate(MatrixBitwiseOperationClassSelection competingClass,PrintWriter writer){
		writer.println("MatrixBitwiseOperationClassSelection evaluate class competingClass");
		// 1. class and adjacent
		this.writeClass(writer,"evaluate class");		
		// 2. competingClass and competingClass.adjacent
		competingClass.writeClass(writer,"evaluate competingClass");	
		// criteria of evaluation: nr, frequency, summation of length, ...
		
	}
	
	public void competition(ArrayList<MatrixBitwiseOperationClassSelection>classList,
			 ArrayList<MatrixBitwiseOperationHelpCompetingElementGroup>competitionList,
			 HashMap<String, Integer>classesHashMap,
			 HashMap<String, Integer>competitionHashMap,
			 PrintWriter writer) {
		String outString="";boolean out;
		// get member of (morphological) class (this)
		Iterator<String> classIterator = this.members.iterator();
				while (classIterator.hasNext()) {
					outString="";out=false;
					String classElementName=classIterator.next();
					// check whether name is competing
					Integer competitionIndex=competitionHashMap.get(classElementName);
					if(competitionIndex!=null) {//System.out.print
						outString ="competition: classElementName: "
								+ classElementName+ " "+competitionIndex.toString();
						// the competitionIndex identifies competingElement in
						// competitionList; get competition element;
						// value of competitionIndex is position in list +1, 
						// therefore competitionIndex-1
						MatrixBitwiseOperationHelpCompetingElementGroup competingElementGroup=
							competitionList.get(competitionIndex-1);	
							for (int i=0;i<competingElementGroup.members.size();i++){
								String competingElementName=competingElementGroup.members.get(i);
								if (!classElementName.equals(competingElementName)){
									
									// get competition class in classesHashMap
									Integer classNr=classesHashMap.get(competingElementName);
									
									if (!classNr.equals(0)){
										outString=outString+" competingElementName: " + 
										competingElementName+" classNr: "+ classNr.toString();
										// get classelement (-1, as first classNr is 1, not 0)
										MatrixBitwiseOperationClassSelection otherCompetingClass=
											classList.get((int)classNr-1);
									
										// evaluate and remove less evaluated
										evaluate(/*this*/otherCompetingClass,writer);
										out=true;
									}
									
								
								}
							}//for (int i=0;i<competitionElement.members.size();i++)
							if (out) writer.println(outString);					
		
					}
					
				}
		
	}

}
