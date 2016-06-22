package modules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HammingDistance {

	public static void main(String[] args) throws IOException {

		 Scanner scanner = new Scanner(new File("/Users/TodorTodorov/morpho-out.csv"));
		 
	        scanner.useDelimiter("\\n");
	        
			List<String> row = new ArrayList<String>();
			int distance = 0;
			
			while(scanner.hasNext()){
				
	            row.add(scanner.next());	            
	            	            //System.out.print(row.get(i) + " " + "\n");
	        }
			//row.remove(0);
			
			
			String[] first;
			String[] second;
			System.out.println(row.get(0));

			for (int j = 1; j < row.size() -1; j++) {
				distance = 0;
				
				first = row.get(j).split(";", -1);
				second = row.get(j + 1).split(";", -1);
				
				for(int k = 1; k < first.length; k++) {
					if (!first[k].equals(second[k])) {
						distance++;
					}
				}
				
				System.out.println(distance + "  "  + row.get(j));
			}
			
			System.out.println("   " + row.get(row.size()-1));
			System.out.println("   ");

			
			float globalDistance;
//			for (int j = 1; j < row.size() -1; j++) {
//				globalDistance = 0;
//
//				first = row.get(j).split(";", -1);
//				second = row.get(j + 1).split(";", -1);
//				
//				for(int k = 1; k < first.length; k++) {
//					
//					if (!first[j].equals(second[k])) {
						
//						globalDistance++;
						
//					}
//				}

	//			System.out.println(row.get(j) + "   " + globalDistance / row.size());
		//	}
			System.out.println("   ");

			System.out.println("   ");

			
			for(int i=1; i<row.size(); i++){
				globalDistance =0;
				
				  for(int j=i + 1; j<row.size()-1; j++){
					  first = row.get(j).split(";", -1);
						second = row.get(j + 1).split(";", -1);
						
						for(int k = 1; k < first.length; k++) {
							
							if (!first[k].equals(second[k])) {
								
								globalDistance++;
								
							}
						}
				  	}
				  
					System.out.println(row.get(i) + "   " + globalDistance / row.size());

			}
			
	        scanner.close();

	
	}

}
