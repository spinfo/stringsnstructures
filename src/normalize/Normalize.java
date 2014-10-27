package normalize;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class Normalize {
	
	String eol=System.getProperty("line.separator");
	
	public String readText(InputStreamReader reader){
		System.out.println("Normalize.readText entry");
		StringBuffer textBuffer=new StringBuffer();
		int ch;
		try 
			{ch=reader.read();//??? hier wurde am anfang fragezeichen gelesen???
			
			while ((ch=reader.read())!=-1)	
				{System.out.print((char)ch);
				 textBuffer.append((char)ch);
				}
			
			} catch (Exception e)
			{System.out.println
				("Exception Normalize.readText");};
		
		System.out.println("\n\nResult readText\n"+textBuffer.toString());
		return textBuffer.toString();
	}
	
	String normalize(String text){
		
		try {
		System.out.println("\n\nNormalize.normalize entry");
		// replace all white chars (blank, newline, tab)
		text=text.replaceAll("\\s", " ");
		// colon, quotation mark by blank
		text=text.replaceAll("[,\"]", " ");
		// multiple blank by (one) blank
		text=text.replaceAll("[ ]+", " ");
		// (blank) full stop (.,!,? ...) (blank) by $
		text=text.replaceAll("[ ]*[.;!?;:][ ]*", "\\$"+eol);
		System.out.print(text);
		}
		catch (Exception e){System.out.println("Exception normalize");};
		return text;
	}
	
	StringBuffer filter(String text,int min,int max){
		int len;
		StringBuffer buf=new StringBuffer();
		String phrases []=text.split(eol);
		for(int i=0;i<phrases.length;i++)
		{ 	String words[]=phrases[i].split("[ ]");
			len=words.length;
			System.out.println(phrases[i]+ "  " +len);
			if((len>=min) && (len<=max)){
				buf.append(phrases[i]+eol);
			}
		}
		return buf;
	}
	
	
}
