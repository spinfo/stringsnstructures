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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		  final String INPUT_FILE_NAME = "Genesis.txt";
		  final String OUTPUT_FILE_NAME = "NormalizeOutput.txt";
		  String PATH_NAME="";
		  final String ENCODING = "UTF-8";//StandardCharsets.UTF_8;
		 
		  String text="";
		 
		  Path p = Paths.get( "../" );
		  System.out.println( p.toAbsolutePath() );
		  
		  try
		  {
			PATH_NAME=p.toRealPath( LinkOption.NOFOLLOW_LINKS ).toString()+"\\";
		    System.out.println(PATH_NAME);
		   
		  }
		  catch ( IOException e ) { e.printStackTrace(); }
		  
		  System.out.println("NormalizeMain");
		  try {InputStreamReader reader = 
				  new InputStreamReader
			       (new FileInputStream(PATH_NAME+INPUT_FILE_NAME), 
			    	ENCODING /*"ISO-8859-1" *//* "UTF-8"*/);
		  
		    	Normalize normalize = new Normalize();
		    	text=normalize.readText(reader);
		    	text=normalize.normalize(text);
		    	
		  } catch (Exception e) {e.printStackTrace();};
		    
		    try {PrintWriter out = new PrintWriter(new FileWriter(PATH_NAME+OUTPUT_FILE_NAME)); 
		    	out.print(text);
		    	out.close();
		    } catch (Exception e) {System.out.println("Exception Main normalize");};
		    System.out.println("NormalizeMain Ende");
	}

}
