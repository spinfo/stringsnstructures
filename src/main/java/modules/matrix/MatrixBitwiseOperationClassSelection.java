package modules.matrix;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

//
// element of list of classes, contains members of class
// evaluates class
// resolves class competition and conflicts
//
public class MatrixBitwiseOperationClassSelection extends MatrixBitwiseOperationHelpBaseElement{
	
	
	Set<String> adjacentMembers;
	private int classNr;
	private int number;// nr of elements in class
	private int nrAdjacentMembers; // nr of adjacent elements for elements of class
	private int lengthMembers;
	private int lengthAdjacentMembers;
	private double ratio; // correctness of class, for match with lex
	public MatrixBitwiseOperationClassSelection(int classNr){
		this.classNr=classNr;
		this.adjacentMembers=new TreeSet<String>();
	}
	
	
	
	public void countWriteClass(PrintWriter writer,String message,Set<String> morphemes,
			boolean count){
		this.number=0;// nr of elements in class
		this.nrAdjacentMembers=0; // nr of adjacent elements for elements of class
		int occurrence=0; // counts how many items are correct
		this.ratio=0;//correctness after match with lex
		if(!members.isEmpty() && !adjacentMembers.isEmpty()){
			writer.print(message+"-----classNr "+ this.classNr+"-----adjacent ");
			for(String adjacent:adjacentMembers) {
				writer.print(adjacent+" ");
				if (count) this.nrAdjacentMembers++;
			}
			writer.println();
			if (count) writer.println(" nrAdjacentMembers: "+this.nrAdjacentMembers);
			for(String member:members) {
				writer.println(member+" ");	
				this.number++;
				// without <>|
				String comparison=member.replaceAll("\\<|\\>|\\|", "");
				writer.println("in Lex: "+comparison);
				if (morphemes.contains(comparison)) {
					writer.println("in Lex compare: "+comparison);
					if (count) occurrence++;
				}
				
			}// for (string member
			if (count) { 
				writer.println(" nr elements of class: "+this.number);
				ratio=(occurrence*100)/number;
				writer.println(" Correctness: "+ratio);
			}
		}
	}//countWriteClass
	
	private void evaluate(MatrixBitwiseOperationClassSelection competingClass,PrintWriter writer,
			Set<String>morphemes){
		writer.println("MatrixBitwiseOperationClassSelection evaluate class competingClass");
		// 1. class and adjacent
		this.countWriteClass(writer,"evaluate class",morphemes,false);		
		// 2. competingClass and competingClass.adjacent
		competingClass.countWriteClass(writer,"evaluate competingClass",morphemes,false);	
		// criteria of evaluation: nr, frequency, summation of length, ...
		
	}
	
	public void competition(ArrayList<MatrixBitwiseOperationClassSelection>classList,
			 ArrayList<MatrixBitwiseOperationHelpCompetingElementGroup>competitionList,
			 HashMap<String, Integer>classesHashMap,
			 HashMap<String, Integer>competitionHashMap,
			 PrintWriter writer,Set<String>morphemes) {
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
										evaluate(/*this*/otherCompetingClass,writer,morphemes);
										out=true;
									}
									
								
								}
							}//for (int i=0;i<competitionElement.members.size();i++)
							if (out) writer.println(outString);					
		
					}
					
				}
		
	}

}
