package modules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HammingDistance {

	public static void main(String[] args) throws IOException {

		 Scanner scanner = new Scanner(new File("/Users/TodorTodorov/morpho-out.csv"));
	        scanner.useDelimiter(";");
			List<String> row = new ArrayList<String>();
			int i = 0;
			int distance = 0;
			while(scanner.hasNext()){
	        	
	            row.add(scanner.next());
	            
	            
	            System.out.print(row.get(i) + " ");

	            i++;
	            
	        }
			
			for(int i1= 0 ; i1 < row.size()-1; i1++){
                for(int k = i1+1 ; k < row.size() ; k++){
                    if(row.get(i1) == row.get(k)){
                        System.out.println(i1 + "and" + k + "are pairs");
                    }
                }
			
	        
	        scanner.close();
	        
	     
	         
	        
			}}

}
