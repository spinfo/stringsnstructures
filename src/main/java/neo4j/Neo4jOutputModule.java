package neo4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Properties;

import modularization.CharPipe;
import modularization.ModuleImpl;
import parallelization.CallbackReceiver;
import treeBuilder.Knoten;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Neo4jOutputModule extends ModuleImpl {
	
	public static final String BERUEHRUNGSZAEHLER_SCHLUESSEL = "zaehler";
	// Property keys
	public static final String PROPERTYKEY_NEO4JURI = "URI of Neo4J DB";
	public static final String PROPERTYKEY_NEO4JUSR = "Username for Neo4J access";
	public static final String PROPERTYKEY_NEO4JPWD = "Password for Neo4J access";
	private String neo4jUri = "";
	private String neo4jUsr = "";
	private String neo4jPwd = "";

	public Neo4jOutputModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		// Define I/O
		this.getSupportedInputs().add(CharPipe.class);
		
		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_NEO4JURI, "URI of the Neo4j DB.");
		this.getPropertyDescriptions().put(PROPERTYKEY_NEO4JUSR, "Username for Neo4J access.");
		this.getPropertyDescriptions().put(PROPERTYKEY_NEO4JPWD, "Password for Neo4J access.");

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Remote Neo4J DB");
		this.getPropertyDefaultValues().put(PROPERTYKEY_NEO4JURI, "http://127.0.0.1:7474/db/data/");
		
		// Add module description
		this.setDescription("Stores a given suffix tree (JSON format) into a remote Neo4J-DB-backend.");
	}

	@Override
	public boolean process() throws Exception {
		
		// Graph-Datenbank-Klienten instanziieren
		final Neo4jRestKlient graph = new Neo4jRestKlient(this.neo4jUri, this.neo4jUsr, this.neo4jPwd);
		
		// Instantiate JSON parser
		Gson gson = new GsonBuilder().create();
		
		// Wurzelknoten einlesen
		Knoten rootNode = gson.fromJson(this.getInputCharPipe().getInput(), Knoten.class);
		
		// Wurzelknoten zu Graph hinzufuegen
		//Node graphRootNode = graph.fuegeKnotenErstellungZurWarteschlangeHinzu(rootNode.getName(), rootNode.getZaehler()).get(rootNode.getName());
		URI graphRootNode = graph.erstelleKnoten(rootNode.getName());
		graph.eigenschaftHinzufuegen(graphRootNode, BERUEHRUNGSZAEHLER_SCHLUESSEL, rootNode.getZaehler());
		
		// Baum durchlaufen; Knoten+Kanten erstellen
		Iterator<String> knotenKinderListenSchluessel = rootNode.getKinder().keySet().iterator();
		while (knotenKinderListenSchluessel.hasNext()){
			String schluessel = knotenKinderListenSchluessel.next();
			Knoten kindKnoten = rootNode.getKinder().get(schluessel);
			this.attachNodeToGraph(graphRootNode, kindKnoten, graph);
		}
		
		return true;
	}
	
	private void attachNodeToGraph(URI parent, Knoten child, Neo4jRestKlient graph) throws URISyntaxException{
		
		// Knoten erstellen
		URI neuerGraphenKnoten = graph.erstelleKnoten(child.getName());
		graph.eigenschaftHinzufuegen(neuerGraphenKnoten, BERUEHRUNGSZAEHLER_SCHLUESSEL, child.getZaehler());
		
		// Kante erstellen
		URI relationshipUri = graph.addRelationship(parent, neuerGraphenKnoten,
				"child", "{ }");
		
		// Baum durchlaufen; Knoten+Kanten erstellen
		Iterator<String> knotenKinderListenSchluessel = child.getKinder().keySet().iterator();
		while (knotenKinderListenSchluessel.hasNext()) {
			String schluessel = knotenKinderListenSchluessel.next();
			Knoten kindKnoten = child.getKinder().get(schluessel);
			this.attachNodeToGraph(neuerGraphenKnoten, kindKnoten, graph);
		}
	}

	/* (non-Javadoc)
	 * @see modularization.ModuleImpl#applyProperties()
	 */
	@Override
	public void applyProperties() throws Exception {
		if (this.getProperties().containsKey(PROPERTYKEY_NEO4JURI))
			this.neo4jUri = this.getProperties().getProperty(PROPERTYKEY_NEO4JURI);
		if (this.getProperties().containsKey(PROPERTYKEY_NEO4JUSR))
			this.neo4jUsr = this.getProperties().getProperty(PROPERTYKEY_NEO4JUSR);
		if (this.getProperties().containsKey(PROPERTYKEY_NEO4JPWD))
			this.neo4jPwd = this.getProperties().getProperty(PROPERTYKEY_NEO4JPWD);
		super.applyProperties();
	}

}
