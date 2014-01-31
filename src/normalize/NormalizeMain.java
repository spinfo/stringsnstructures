package normalize;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NormalizeMain {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		  final String INPUT_FILE_NAME = "Genesis.txt";
		  final String OUTPUT_FILE_NAME = "output.txt";
		  final String ENCODING = "UTF-8";//StandardCharsets.UTF_8;
		  //Path 
		  String path = INPUT_FILE_NAME;//Paths.get(INPUT_FILE_NAME);
		  String text="";
		 
		  System.out.println("NormalizeMain");
		  try {InputStreamReader reader = 
				  new InputStreamReader
			       (new FileInputStream(path), ENCODING /*"ISO-8859-1" *//* "UTF-8"*/);
		  
		    	Normalize normalize = new Normalize();
		    	text=normalize.readText(reader);
		    	text=normalize.normalize(text);
		    	
		  } catch (Exception e) {System.out.println("Exception 1");};
		    
		    try {PrintWriter out = new PrintWriter(new FileWriter(OUTPUT_FILE_NAME)); 
		    	//out.print(text);
		    	//out.close();
		    } catch (Exception e) {System.out.println("Exception Main normalize");};
		    System.out.println("NormalizeMain Ende");
	}

}
