package seqTreeProperties;

import java.util.Properties;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Iterator;

import treeBuilder.Knoten; //necessary to read the objects form JSON
import modularization.CharPipe;
import modularization.ModuleImpl;
import parallelization.CallbackReceiver;

import com.google.gson.Gson;

/**
 * Reads trees from I/O pipe via JSON format.
 * Extracts tree properties such as 
 * 	- height of the tree (h = length of the longest path)
 * 	- average length of all paths
 * 	- ratio of subtree sites 
 * 	- average of subtree sites
 * 	- Sackin index and other tree balance measures for non-binary trees
 * 
 * @author Christopher Kraus
 * 
 */

public class SeqTreePropController extends ModuleImpl {
		
		//property keys:
		//public static final String PROPERTYKEY_PROP = "PROP";
		//end keys
	
		//variables:
		/*private int treeHeight;
		private double ratioSubtree;
		private double averageSubtree;
		private int sackinIndex;*/
		
		private String json = "";
		private Knoten mainNode;
		private Gson gson;
		//private HashMap<String, SeqPropertyNode> seqProperties;
		//end variables
		
		//constructors:
		public SeqTreePropController(CallbackReceiver callbackReceiver,
				Properties properties) throws Exception {
			super(callbackReceiver, properties);

			// Add property descriptions
			//this.getPropertyDescriptions().put(PROPERTYKEY_REGEX, "Regular expression to search for");
			
			// Add property defaults
			this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Sequence Tree Properties");
			//this.getPropertyDefaultValues().put(PROPERTYKEY_REGEX, "[aeiu]");
			
			// Define I/O
			this.getSupportedInputs().add(CharPipe.class);
			this.getSupportedOutputs().add(CharPipe.class);
			
		}
		//end constructors
		
		//setters:
		public void addJson(String str) {
			json += str;
		}
		
		public void setGson() {
			gson = new Gson();
			mainNode = gson.fromJson(json, Knoten.class);
		}
		//end setters
		
		//getters:
		public String getJson() {
			return json;
		}
		//end getters
		
		@Override
		public boolean process() throws Exception {
			
			// Variables used for input data
			int bufferSize = 1024;
			char[] buffer = new char[bufferSize];
			
			// Read first chunk of data
			int readChars = this.getInputCharPipe().read(buffer, 0, bufferSize);
			
			// Loop until no more data can be read
			while (readChars != -1){
				
				// Check for interrupt signal
				if (Thread.interrupted()) {
					this.closeAllOutputs();
					throw new InterruptedException("Thread has been interrupted.");
				}
				
				// Convert char array to string
				String inputChunk = new String(buffer).substring(0, readChars);
				this.addJson(inputChunk);
				
				// Read next chunk of data
				readChars = this.getInputCharPipe().read(buffer, 0, bufferSize);
			}
			
			//create mainNode by reading JSON input
			this.setGson();
			
			//iterate over the tree and get parameters
			this.iterateMainNode();
			
			
			// Write to outputs
			//this.outputToAllCharPipes(this.getJson());
			
			// Close outputs (important!)
			//this.closeAllOutputs();
			
			// Done
			return true;
		}
		
		@Override
		public void applyProperties() throws Exception {
			
			// Apply own properties
			/*if (this.getProperties().containsKey(PROPERTYKEY_REGEX))
				this.regex = this.getProperties().getProperty(PROPERTYKEY_REGEX, this.getPropertyDefaultValues().get(PROPERTYKEY_REGEX));
			if (this.getProperties().containsKey(PROPERTYKEY_REPLACEMENT))
				this.replacement = this.getProperties().getProperty(PROPERTYKEY_REPLACEMENT, this.getPropertyDefaultValues().get(PROPERTYKEY_REPLACEMENT));
			*/
			// Apply parent object's properties
			super.applyProperties();
		}
		
		public void iterateMainNode() {
			Iterator<Entry<String, Knoten>> it = mainNode.getKinder().entrySet().iterator();
			while (it.hasNext()) {
				HashMap.Entry<String, Knoten> pair = (HashMap.Entry<String, Knoten>)it.next();
			    System.out.println(pair.getKey() + " = " + pair.getValue());
			    it.remove(); // avoids a ConcurrentModificationException
			}
		}
}
