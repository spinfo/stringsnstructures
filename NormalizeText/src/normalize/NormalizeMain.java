package normalize;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;



public class NormalizeMain {

	/**
	 * @param args
	 */
	
	private static String pathName(){

		  Path p = Paths.get( "../" );		 		  
		  try
		  {
			return p.toRealPath( LinkOption.NOFOLLOW_LINKS ).toString()+"\\";		   		   
		  }
		  catch (IOException e) { e.printStackTrace(); }
		  return null;
	}
		
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//save name to file
		  String NAME = "";
		  final String ENCODING = "UTF-8";//StandardCharsets.UTF_8;
		  final String TXTEXTENSION=".txt";		 
		  final String OUTPUT_FILE_NAME = 
				  NAME+"Normalize"+TXTEXTENSION;
		  String PATH_NAME=pathName();
		 		 
		 
		  
		  String text="";
		  System.out.println("NormalizeMain");
		  try {InputStreamReader reader = 
				  new InputStreamReader
			       (new FileInputStream(PATH_NAME+NAME+TXTEXTENSION), 
			    	ENCODING /*"ISO-8859-1" *//* "UTF-8"*/);
		  
		    	Normalize normalize = new Normalize();
		    	text=normalize.readText(reader);
		    	text=normalize.normalize(text);
		    	
		  } catch (Exception e) {e.printStackTrace();};
		    
		  // save name of Input text
		    try {PrintWriter name = 
		    	new PrintWriter(new FileWriter(PATH_NAME+"Name"+TXTEXTENSION)); 
		    	name.print(NAME);
		    	name.close();
		    	
		    } catch (Exception e) {e.printStackTrace();};
		    System.out.println("NormalizeMain Ende");
	
		    try {PrintWriter out = new PrintWriter(new FileWriter(PATH_NAME+OUTPUT_FILE_NAME)); 
		    	out.print(text);
		    	out.close();
		    	
		    } catch (Exception e) {e.printStackTrace();};
		    System.out.println("NormalizeMain Ende");
	}

}
