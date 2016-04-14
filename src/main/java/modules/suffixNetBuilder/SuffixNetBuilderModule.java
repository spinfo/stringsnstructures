package modules.suffixNetBuilder;

import java.io.BufferedReader;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import models.ExtensibleTreeNode;
import modules.BytePipe;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.Pipe;
import modules.oanc.WortAnnotationTupel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.parallelization.CallbackReceiver;

public class SuffixNetBuilderModule extends ModuleImpl {
	
	public static final String BERUEHRUNGSZAEHLER_SCHLUESSEL = "zaehler";
	// Property keys
	public static final String PROPERTYKEY_INDIVIDUALBRANCHES = "Create individual branches";
	public static final String PROPERTYKEY_INDIVIDUALROOTNODES = "Create individual rootnodes";
	
	// Local variables
	private boolean individualBranches;
	//private boolean individualRootNodes;
	private final String INPUTID = "input";
	private final String OUTPUTID = "output";

	public SuffixNetBuilderModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// Define I/O
		InputPort inputPort = new InputPort(INPUTID, "JSON-encoded FileFinderModule-data, with the parsed contents of one source file per line.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUTID, "Suffix net in binary object form.", this);
		outputPort.addSupportedPipe(BytePipe.class);
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		
		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_INDIVIDUALBRANCHES, "Creates individual branches for each sentence.");
		this.getPropertyDescriptions().put(PROPERTYKEY_INDIVIDUALROOTNODES, "Creates individual root nodes for each sentence.");

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "SuffixNetBuilder");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INDIVIDUALBRANCHES, "false");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INDIVIDUALROOTNODES, "false");

		// Add module description
		this.setDescription("Builds a suffix net from the annotated JSON output of OANCXMLParser (expects one JSON object per line).");
		// Add module category
		this.setCategory("Experimental/WiP");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see modularization.ModuleImpl#applyProperties()
	 */
	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		if (this.getProperties().containsKey(PROPERTYKEY_INDIVIDUALBRANCHES))
			this.individualBranches = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_INDIVIDUALBRANCHES));
		//if (this.getProperties().containsKey(PROPERTYKEY_INDIVIDUALROOTNODES))
			//this.individualRootNodes = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_INDIVIDUALROOTNODES));
		super.applyProperties();
	}

	@Override
	public boolean process() throws Exception {

		// Wurzelknoten des zu erstellenden Baumes erstellen
		ExtensibleTreeNode wurzelKnoten = new ExtensibleTreeNode("^");

		// JSON-Parser instanziieren
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		// Variable zur Nachhaltung erstellter Graphenknoten
		Map<String, ExtensibleTreeNode> createdNodes = null;
		if (!this.individualBranches)
			createdNodes = new HashMap<String, ExtensibleTreeNode>();

		// Eingabe puffern
		BufferedReader eingabe = new BufferedReader(this.getInputPorts().get(INPUTID).getInputReader());

		// Eingabe einlesen
		String jsonObjekt = eingabe.readLine();
		while (jsonObjekt != null) {

			// JSON-Objekt parsen
			WortAnnotationTupel[][] saetze = gson.fromJson(jsonObjekt,
					new WortAnnotationTupel[0][0].getClass());

			// Saetze durchlaufen
			for (int i = 0; i < saetze.length; i++) {

				WortAnnotationTupel[] satz = saetze[i];

				// Satz in String-Array fassen
				String[] satzArray = new String[satz.length];
				for (int j = 0; j < satz.length; j++) {
					satzArray[j] = satz[j].getWort();
				}

				// Mit dem ermittelten Satz wird das Netz weiter konstruiert
				this.buildSuffixNet(satzArray, wurzelKnoten, createdNodes);

			}

			// Naechstes Objekt einlesen
			jsonObjekt = eingabe.readLine();

		}

		// Letztlich wird der Wurzelknoten (und damit der gesamte erstellte
		// Baum) als Bytestrom ausgegeben
		Iterator<Pipe> byteStroeme = this.getOutputPorts().get(OUTPUTID).getPipes(BytePipe.class).iterator();
		while (byteStroeme.hasNext()){
			ObjectOutputStream ausgabeStrom = new ObjectOutputStream(((BytePipe)byteStroeme.next()).getOutput());
			ausgabeStrom.writeObject(wurzelKnoten);
			ausgabeStrom.close();
		}

		// Ausgabekanaele schliessen
		this.closeAllOutputs();

		return true;
	}

	/**
	 * Builds a suffix net based on the given root node and writes it into a Neo4j-database (via given client).
	 * @param token Token of strings (will be nodes within the resulting net)
	 * @param rootnode Node/URI to start from
	 * @param graph Neo4J-REST-client
	 * @param createdNodes Map used to keep track of created nodes. If null, each call will create a different individual branch.
	 * @throws Exception
	 */
	private void buildSuffixNet(String[] token, ExtensibleTreeNode rootnode, Map<String, ExtensibleTreeNode> createdNodes) throws Exception {

		// Keep track of created nodes
		if (createdNodes == null)
			createdNodes = new HashMap<String, ExtensibleTreeNode>();

		// Set parent node
		ExtensibleTreeNode parentNode = rootnode;

		// Variable for child node
		ExtensibleTreeNode child;

		// Loop over the token array's elements
		for (int i = 0; i < token.length; i++) {

			// Check whether a node with the current token's name already exists
			if (createdNodes.containsKey(token[i])) {

				// If so, retrieve the node
				child = createdNodes.get(token[i]);

				// Check whether that node is already connected to the current parent node
				if (!parentNode.getChildNodes().containsKey(token[i])){
					
					// If not, make the connection
					parentNode.getChildNodes().put(token[i], child);
					
				}
			}

			else {

				// Instantiate new node
				child = new ExtensibleTreeNode(token[i]);

				// Connect it to parent
				parentNode.getChildNodes().put(token[i], child);
				
				// Remember it as having been created
				createdNodes.put(token[i], child);
			}

			// Increment the counter of the child node
			child.setNodeCounter(child.getNodeCounter()+1);
			
			// Set new parent
			parentNode = child;
		}
	}

}
