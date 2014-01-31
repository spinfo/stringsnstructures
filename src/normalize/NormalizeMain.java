package normalize;

import java.io.BufferedReader;
import java.io.FileWriter;
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
		  final String INPUT_FILE_NAME = "input.txt";
		  final String OUTPUT_FILE_NAME = "output.txt";
		  final Charset ENCODING = StandardCharsets.UTF_8;
		  Path path = Paths.get(INPUT_FILE_NAME);
		  String text="";
		  try {BufferedReader reader = Files.newBufferedReader(path, ENCODING);
		    	Normalize normalize = new Normalize();
		    	text=normalize.readText(reader);
		    	text=normalize.normalize(text);
		  } catch (Exception e) {};
		    
		    try {PrintWriter out = new PrintWriter(new FileWriter(OUTPUT_FILE_NAME)); 
		    	out.print(text);
		    	out.close();
		    } catch (Exception e) {};
		    
	}

}
