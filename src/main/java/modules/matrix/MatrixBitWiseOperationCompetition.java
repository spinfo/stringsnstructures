package modules.matrix;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class MatrixBitWiseOperationCompetition {
	
	HashMap<String, Integer> competitionHashMap;
	ArrayList<MatrixBitwiseOperationHelpCompetingElementGroup> competitionList=
				new ArrayList<MatrixBitwiseOperationHelpCompetingElementGroup>();
	
	void readEvalHashMap(BufferedReader r,PrintWriter writer) throws Exception{
		// produces a map which consists of a prefix string for competing strings 
		// competing strings are strings which might be separated differently: Belg|ae vs Belga|e
		competitionHashMap=new HashMap<String,Integer>();
		String line;
		// competitions are identified by competitionIdent; competing elements
		// elements have equal competitionIdent, e.g. ama+re, amar+e
		int competitionIdent=0;
		MatrixBitwiseOperationHelpCompetingElementGroup competitionElement;
		while((line=r.readLine())!=null){
			// skip empty lines (i.e. lines which don't contain pipes("|" or dollars ("$")
			if (line.length()>0){
				// '$' marks end of String. ';' separates competing strings
				String[] competionStr= line.split("\\$;?");
				int nrCompetitions=competionStr.length;
				if(nrCompetitions>1){
					writer.println
					("readEvalHashMap: "+line+" nrCompetitions: "+nrCompetitions+" ");
					competitionIdent++;
					
					competitionElement=new MatrixBitwiseOperationHelpCompetingElementGroup();
					for (int i=0;i<nrCompetitions;i++){
						// binary separation of competing string(s)
						// save (different) prefixes
						String prefix[]=competionStr[i].split("\\|");
						//System.out.println
						//("readEvalHashMap prefix: "+prefix[0]);
						competitionHashMap.put(prefix[0], new Integer(competitionIdent));
						competitionElement.add(prefix[0]);
					}
					competitionList.add(competitionElement);
				}					
				
			}
			
		}			
		
	}// readEvalHashMap
	
	
	 boolean checkcompetition(String name1,String name2,PrintWriter writer) throws Exception
	{
		// for key name1 and key name2 get value and compare value with 'equals'
		if ((competitionHashMap.get(name1)!=null) &&
				(competitionHashMap.get(name1).equals(competitionHashMap.get(name2))))
			{
				writer.println("checkcompetition competition name1: "+name1+ " name2: "+name2);
				return true;
			}
			else return false;	
			
	}//checkcompetition
	
}// class MatrixBitWiseOperationCompetition


