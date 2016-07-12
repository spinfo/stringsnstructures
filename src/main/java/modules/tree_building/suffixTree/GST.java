package modules.tree_building.suffixTree;

import java.io.*;
import java.util.LinkedList;
import java.util.List;


public class GST {

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
		NodePositionEnd oo = st.newEnd();// end value for leaves; is changed if final '$' is reached
									  // generate new st.OO for next text

		// set the beginning of the first text to first letter of the input
		st.setTextBegin(0, 0);
		
	    if(typeContextEndIndices != null) {
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
				oo.val=i+1;
				// generate new element for next text
				oo = st.newEnd();

				nrText++;
				// note the beginning of the whole next text in the tree
				if (i != inText.length() - 1) {
					st.setTextBegin(nrText, i + 1);
				}

				// Handle incrementing of type contexts if provided
				if (typeContextEndIndices != null) {
					incrementTypeContexts(st, typeContextEndIndices, nrText);
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
						st.addRemaining(nrText);
					}else break;

				} else break;// if end > i; inText end reached
			}// while

		}// for (int i = 0; i < inText.length(); i++)
		
		if(typeContextEndIndices != null && !typeContextEndIndices.isEmpty()) {
			throw new IllegalStateException(
					"Some type context end numbers were not handled. First remaining textNr: " + typeContextEndIndices.get(0));
		}
		
		return st;
	    
	}
	

	// Convenience method to build a GST for a String with the specified contexts. Multiple inputs should be separated by '$'. 
	public static SuffixTree buildGST(String input, List<Integer> typeContextEndIndices) throws Exception {
		return buildGST(new StringReader(input), typeContextEndIndices);
	}
	
	// Convenience method to build a GST for a String. Multiple inputs should be separated by '$'. 
	public static SuffixTree buildGST(String input) throws Exception {
		return buildGST(new StringReader(input), null);
	}
	
	private static void incrementTypeContexts(BaseSuffixTree st, List<Integer> typeContextEndIndices, int nrText) {
		// if type context end indices are provided, they may never be empty at this step
		if (typeContextEndIndices.isEmpty()) {
			throw new IllegalStateException(
				"No type context to set at text: " + nrText);
		}
		// If the type context end index matches the text number, one type context is completed.
		// Entering the next context is marked by incrementing the current type context.
		if (typeContextEndIndices.get(0) == nrText) {
			st.incrementTypeContext();
			typeContextEndIndices.remove(0);
		}
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
