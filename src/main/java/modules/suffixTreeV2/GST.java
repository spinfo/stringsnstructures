package modules.suffixTreeV2;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;


public class GST {
	private static final Logger LOGGER = Logger.getLogger(GST.class.getName());
	
	BufferedReader in;
	
	static PositionInfo OO;

	// class should not be instantiated
	private GST() {};
	
	//jr
	private static int findChar(String str, int start, char wanted) {
		for (int i = start; i < str.length(); i++) {
			if (str.charAt(i) == wanted)
				return i;
		}
		return -1;
	}

	// cstr
	public static SuffixTree buildGST(Reader inputReader, List<Integer> typeContextNrs) throws Exception {
		int nrText = 0;
		 
		String line,inText="",nextinText;		
	    BufferedReader in = new BufferedReader(inputReader);
	    inText=in.readLine();
	    while ((line=in.readLine())!=null) {
	    	if(line.charAt(line.length()-1)=='$')inText=inText+line; else inText=inText+" "+line;
	    }
	    in.close();
	    System.out.println(inText);
	    
	    if(typeContextNrs != null) {
	    	LOGGER.info(typeContextNrs.toString());
	    }
		
		
		SuffixTree st = new SuffixTree(inText.length());
		GST.OO=new PositionInfo(st.oo);// end value for leaves; is changed if final '$' is reached
									  // generate new st.OO for next text
		
		for (int i = 0; i < inText.length(); i++) {
			System.out.println("i: "+i+" ch: "+inText.charAt(i));

			st.addChar(inText.charAt(i), nrText);
			// while loop as completely repeated texts are possible
			while(inText.charAt(i) == '$') {
				System.out.println("while i: "+i);
				
				// set value for end in leaves
				GST.OO.val=i+1;
				// generate new element for next text
				GST.OO = new PositionInfo(st.oo);
				nrText++;
				int end = findChar(inText, i + 1, '$');
				System.out.println("findChar end: "+end);
				// inText end not reached
				if (end > i) {
					nextinText = inText.substring(i + 1, end + 1);
					System.out.println("nextText: "+nextinText);
					int res=st.longestPath(nextinText,st.root);
					st.remainder=res; // see addChar, remainder corresponds 
					//					 to longest length of label to implicit node
					System.out.println("GST i: "+i+" res: "+res);
					// chars from inText must be copied to st.text (=array of char) for identical longest path
					for (int j=i+1;j<=i+res;j++)st.text[++st.position]=inText.charAt(j);
					// print out for control
					System.out.println("st: copy from inText to text ");
					for (int j=i+1;j<=st.position;j++)System.out.print(st.text[j]);
					System.out.println();
					i=i+res;
					
					// res must be greater 0; otherwise endless while loop
					if((res!=0)&& (inText.charAt(i)=='$')) {
					// next text is completely contained in suffix tree (i.e. it is a complete repeat of a
					// precedent text). In this case, addChar won't be called
						System.out.println(" $ found");
						
						st.nodes[st.active_node].addPos(i-res, end-1, nrText);					
						st.addRemainingSuffixesAtEndOfText(st.active_node,st.active_edge, nrText);
						
					}else break;

				} else break;// if end > i; inText end reached
				
			}// while
		}// for (int i = 0; i < inText.length(); i++)
		
		return st;
	    
	}
	
	
	public static void main(String... args) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    System.out.print("Enter file name : ");
	    String filename = null;
	    try {
	        filename = reader.readLine();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    System.out.println("You entered : " + filename);
	    
		SuffixTree st = GST.buildGST(new FileReader(filename+".txt"), null);
		
		st.printTree();
	}

}
