package normalize;

import java.io.BufferedReader;


public class Normalize {
	
	public String readText(BufferedReader reader){
		StringBuffer textBuffer=new StringBuffer();
		char ch;
		try 
			{while ((ch=(char)reader.read())!=-1)	textBuffer.append(ch);
			
			} catch (Exception e){};
			
		return textBuffer.toString();
	}
	
	String normalize(String text){
		// replace all white chars (blank, newline, tab)
		text=text.replaceAll("\\s", " ");
				
		// colon, quotation mark by blank
		text=text.replaceAll("','|'\"'", " ");
		// multiple blank by (one) blank
		text=text.replaceAll("[' '][' ']+", " ");
		// (blank) full stop (.,!,? ...) (blank) by $
		text=text.replaceAll("[' ']?'.'|';'|':'|'?'|'!'[' ']?", "$");
		
		for (int i=0;i<text.length();i++) 
		if (text.charAt(i)=='$') System.out.println('$');
		else System.out.println(text.charAt(i));
		
		return text;
	}
	
	
}
