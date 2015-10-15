package modules.suffixNetBuilder;

import java.io.BufferedReader;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import modules.BytePipe;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.Pipe;
import modules.neo4j.Neo4jRestKlient;
import modules.oanc.WortAnnotationTupel;
import modules.treeBuilder.Knoten;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import common.parallelization.CallbackReceiver;

public class SuffixNetBuilderModule extends ModuleImpl {
	
	public static final String BERUEHRUNGSZAEHLER_SCHLUESSEL = "zaehler";
	// Property keys
	public static final String PROPERTYKEY_NEO4JURI = "URI of Neo4J DB";
	public static final String PROPERTYKEY_NEO4JUSR = "Username for Neo4J access";
	public static final String PROPERTYKEY_NEO4JPWD = "Password for Neo4J access";
	public static final String PROPERTYKEY_USENEO4J = "Output to Neo4J";
	public static final String PROPERTYKEY_INDIVIDUALBRANCHES = "Create individual branches";
	public static final String PROPERTYKEY_INDIVIDUALROOTNODES = "Create individual rootnodes";
	
	// Local variables
	private String neo4jUri = "";
	private String neo4jUsr = "";
	private String neo4jPwd = "";
	private boolean useNeo4j;
	private boolean individualBranches;
	private boolean individualRootNodes;
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
		this.getPropertyDescriptions().put(PROPERTYKEY_NEO4JURI, "URI of the Neo4j DB.");
		this.getPropertyDescriptions().put(PROPERTYKEY_NEO4JUSR, "Username for Neo4J access.");
		this.getPropertyDescriptions().put(PROPERTYKEY_NEO4JPWD, "Password for Neo4J access.");
		this.getPropertyDescriptions().put(PROPERTYKEY_USENEO4J, "Write nodes and edges to Neo4J upon creation.");
		this.getPropertyDescriptions().put(PROPERTYKEY_INDIVIDUALBRANCHES, "Creates individual branches for each sentence.");
		this.getPropertyDescriptions().put(PROPERTYKEY_INDIVIDUALROOTNODES, "Creates individual root nodes for each sentence.");

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "SuffixNetBuilder");
		this.getPropertyDefaultValues().put(PROPERTYKEY_NEO4JURI, "http://127.0.0.1:7474/db/data/");
		this.getPropertyDefaultValues().put(PROPERTYKEY_USENEO4J, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INDIVIDUALBRANCHES, "false");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INDIVIDUALROOTNODES, "false");

		// Add module description
		this.setDescription("Builds a suffix net from the annotated JSON output of OANCXMLParser (expects one JSON object per line).");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see modularization.ModuleImpl#applyProperties()
	 */
	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		if (this.getProperties().containsKey(PROPERTYKEY_NEO4JURI))
			this.neo4jUri = this.getProperties().getProperty(PROPERTYKEY_NEO4JURI);
		if (this.getProperties().containsKey(PROPERTYKEY_NEO4JUSR))
			this.neo4jUsr = this.getProperties().getProperty(PROPERTYKEY_NEO4JUSR);
		if (this.getProperties().containsKey(PROPERTYKEY_NEO4JPWD))
			this.neo4jPwd = this.getProperties().getProperty(PROPERTYKEY_NEO4JPWD);
		if (this.getProperties().containsKey(PROPERTYKEY_USENEO4J))
			this.useNeo4j = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_USENEO4J));
		if (this.getProperties().containsKey(PROPERTYKEY_INDIVIDUALBRANCHES))
			this.individualBranches = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_INDIVIDUALBRANCHES));
		if (this.getProperties().containsKey(PROPERTYKEY_INDIVIDUALROOTNODES))
			this.individualRootNodes = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_INDIVIDUALROOTNODES));
		super.applyProperties();
	}

	@Override
	public boolean process() throws Exception {

		// Wurzelknoten des zu erstellenden Baumes erstellen
		Knoten wurzelKnoten = new Knoten("^");
		
		// Graph-Datenbank-Klienten instanziieren
		Neo4jRestKlient graph = null;
		URI wurzelKnotenUri = null;
		if (this.useNeo4j){

			graph = new Neo4jRestKlient(this.neo4jUri, this.neo4jUsr, this.neo4jPwd);
			
			// Ersten Knoten in Neo4J-DB einspeisen
			if (!this.individualRootNodes){
				wurzelKnotenUri = graph.erstelleKnoten(wurzelKnoten.getName());
				graph.etikettHinzufuegen(wurzelKnotenUri, "wurzel");
			}
		}
		
		// Tupel aus URI und Knoten erstellen
		KnotenUriTupel wurzelKnotenUriTupel = new KnotenUriTupel(wurzelKnoten, wurzelKnotenUri);

		// JSON-Parser instanziieren
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		// Variable zur Nachhaltung erstellter Graphenknoten
		Map<String, KnotenUriTupel> createdNodes = null;
		if (!this.individualBranches)
			createdNodes = new HashMap<String, KnotenUriTupel>();

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
				this.buildSuffixNet(satzArray, wurzelKnotenUriTupel, graph, createdNodes);

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
	private void buildSuffixNet(String[] token, KnotenUriTupel rootnode, Neo4jRestKlient graph, Map<String, KnotenUriTupel> createdNodes) throws Exception {

		// Keep track of created nodes
		if (createdNodes == null)
			createdNodes = new HashMap<String, KnotenUriTupel>();

		// Set parent node
		Knoten parentNode = rootnode.getKnoten();
		URI parentURI = rootnode.getUri();
		
		// Check whether the root node exists within the graph or needs to be created
		if (parentURI == null && graph != null){
			parentURI = graph.erstelleKnoten(parentNode.getName());
			graph.etikettHinzufuegen(parentURI, "wurzel");
		}

		// Variable for child node
		KnotenUriTupel child;

		// Loop over the token array's elements
		for (int i = 0; i < token.length; i++) {

			// Check whether a node with the current token's name already exists
			if (createdNodes.containsKey(token[i])) {

				// If so, retrieve the node
				child = createdNodes.get(token[i]);

				// Check whether that node is already connected to the current parent node
				if (!parentNode.getKinder().containsKey(token[i])){
					
					// If not, make the connection
					parentNode.getKinder().put(token[i], child.getKnoten());
					
					// Write new pairing to Neo4J DB
					if (graph != null){
						graph.addRelationship(parentURI, child.getUri(), "child", "{ }");
						graph.eigenschaftHinzufuegen(child.getUri(), BERUEHRUNGSZAEHLER_SCHLUESSEL, child.getKnoten().getZaehler()+1);
					}
					
				}
			}

			else {

				// Instantiate new node
				child = new KnotenUriTupel();
				child.setKnoten(new Knoten(token[i]));

				// Connect it to parent
				parentNode.getKinder().put(token[i], child.getKnoten());

				// Put newly created token into DB
				if (graph != null){
					child.setUri(graph.erstelleKnoten(child.getKnoten().getName()));
					graph.addRelationship(parentURI, child.getUri(), "child", "{ }");
					graph.eigenschaftHinzufuegen(child.getUri(), BERUEHRUNGSZAEHLER_SCHLUESSEL, 1);
				}
				
				// Remember it as having been created
				createdNodes.put(token[i], child);
			}

			// Increment the counter of the child node
			child.getKnoten().inkZaehler();
			
			// Set new parent
			parentNode = child.getKnoten();
			parentURI = child.getUri();
		}
	}

}
