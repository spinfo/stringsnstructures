package modules.suffixTreeV2;

import java.io.*;
import java.util.LinkedList;
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
	public static SuffixTree buildGST(Reader inputReader, List<Integer> typeContextEndIndices) throws Exception {
		int nrText = 0;
		 
		String line,inText="",nextinText;		
	    BufferedReader in = new BufferedReader(inputReader);
	    inText=in.readLine();
	    while ((line=in.readLine())!=null) {
	    	if(line.charAt(line.length()-1)=='$')inText=inText+line; else inText=inText+" "+line;
	    }
	    in.close();
		
		SuffixTree st = new SuffixTree(inText.length());
		GST.OO=new PositionInfo(st.oo);// end value for leaves; is changed if final '$' is reached
									  // generate new st.OO for next text
		
	    if(typeContextEndIndices != null) {
	    	LOGGER.info(typeContextEndIndices.toString());
	    	// copy the list to not harm the input
	    	typeContextEndIndices = new LinkedList<Integer>(typeContextEndIndices);
	    	// initialise the type context numbers
	    	st.incrementTypeContext();
	    }
		
		for (int i = 0; i < inText.length(); i++) {
			st.addChar(inText.charAt(i), nrText);
			// while loop as completely repeated texts are possible
			while(inText.charAt(i) == '$') {
				// set value for end in leaves
				GST.OO.val=i+1;
				// generate new element for next text
				GST.OO = new PositionInfo(st.oo);

				nrText++;
				// DONT COMMIT, TODO: remove log messages, extract into private method
				System.out.println("Starting next Text: " + nrText);
				// Handle incrementation of type contexts if provided
				if (typeContextEndIndices != null) {
					// if type context end indices are provided, they may never be empty at this step
					if (typeContextEndIndices.isEmpty()) {
						throw new IllegalStateException(
							"The type context end numbers provided cannot be aligned with the texts provided. At char: " + i);
					}
					// If the type context end index matches the text number, one type context is completed.
					// Entering the next context is marked by incrementing the current type context.
					if (typeContextEndIndices.get(0) == nrText) {
						System.out.println("Finished type context " + st.getCurrentTypeContext() + " at: " + i);
						st.incrementTypeContext();
						typeContextEndIndices.remove(0);
					}
				}

				int end = findChar(inText, i + 1, '$');
				// inText end not reached
				if (end > i) {
					nextinText = inText.substring(i + 1, end + 1);
					int res=st.longestPath(nextinText,st.root);
					st.remainder=res; // see addChar, remainder corresponds 
					//					 to longest length of label to implicit node
					// chars from inText must be copied to st.text (=array of char) for identical longest path
					for (int j=i+1;j<=i+res;j++)st.text[++st.position]=inText.charAt(j);
					// print out for control
					i=i+res;
					
					// res must be greater 0; otherwise endless while loop
					if((res!=0)&& (inText.charAt(i)=='$')) {
					// next text is completely contained in suffix tree (i.e. it is a complete repeat of a
					// precedent text). In this case, addChar won't be called
						st.nodes[st.active_node].addPos(i-res, end-1, nrText, st.getCurrentTypeContext());

						System.out.println("adding Pos in GST: " + (i-res) + " " + (end-1) + " " + nrText + " " + st.getCurrentTypeContext());

						st.addRemainingSuffixesAtEndOfText(st.active_node,st.active_edge, nrText);
						
					}else break;

				} else break;// if end > i; inText end reached
				
			}// while
		}// for (int i = 0; i < inText.length(); i++)
		
		if(typeContextEndIndices != null && !typeContextEndIndices.isEmpty()) {
			throw new IllegalStateException(
					"Some type context end numbers were not handled. First remaining end: " + typeContextEndIndices.get(0));
		}
		
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
	    
	    final FileReader in = new FileReader(filename+".txt");
	    final PrintWriter out = new PrintWriter(new FileWriter("st.dot"));
	    
		SuffixTree st = GST.buildGST(in, null);
		st.printTree(out);
		
		in.close();
		out.close();
		System.out.println("All done.");
	}

}
