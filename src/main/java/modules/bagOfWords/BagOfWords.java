package modules.bagOfWords;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BagOfWords {
	
	
	public static void wordOccurencies() throws IOException{
		
		BufferedReader br = new BufferedReader(new FileReader("/Users/TodorTodorov/Documents/workspace/BagOf Words/main/test_text"));
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		String line;
		
		while ((line = br.readLine()) != null) {
			Integer i = 1;
			if(!map.containsKey(line)){
				map.put(line, i);
			}
			else if(map.containsKey(line)){
//				int val = map.get(line);
//				val = val + 1;
				map.put(line, map.get(line) + 1);
				
			}
			
			i++;
		}
		
		// TODO: I do not know whether the buffered reader should be closed here.
		br.close();
		
		System.out.println("Number of types: "+map.size() );
		//for (String key : map.keySet()) {
	        //System.out.println(key + " " + map.get(key));
	    //}
		
		List<Map.Entry<String,Integer>> entries = new ArrayList<Map.Entry<String,Integer>>(
				map.entrySet()
		    );
		    Collections.sort(
		    	entries
		    ,	new Comparator<Map.Entry<String,Integer>>() {
		    		public int compare(Map.Entry<String,Integer> a, Map.Entry<String,Integer> b) {
		    			return Integer.compare(b.getValue(), a.getValue());
		    		}
		    	}
		    );

		    
		    for (Map.Entry<String,Integer> e : entries) {
		    	System.out.println(e.getKey()+" , "+e.getValue());
		    	
		    	
		    }
	
	
	
	
	}
	
}
