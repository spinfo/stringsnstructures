package modules.neo4j;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class Neo4jRestKlient {

	private String server_wurzel_uri = "http://127.0.0.1:7474/db/data/";
	private String server_knoten_uri = server_wurzel_uri + "node";
	private String username;
	private String password;
	private HTTPBasicAuthFilter authFilter = null;

	public Neo4jRestKlient(String server_wurzel_uri) {
		this(server_wurzel_uri, null, null);
	}

	public Neo4jRestKlient(String server_wurzel_uri, String username,
			String password) {
		super();
		this.server_wurzel_uri = server_wurzel_uri;
		this.server_knoten_uri = server_wurzel_uri + "node";
		this.username = username;
		this.password = password;
		if (this.username != null && this.password != null && !this.username.isEmpty() && !this.password.isEmpty()){
			this.authFilter = new HTTPBasicAuthFilter(this.username, this.password);
		}
	}

	public static void main(String[] args) {
		Neo4jRestKlient instanz = new Neo4jRestKlient("http://127.0.0.1:7474/db/data/","neo4j", "NJXee5Taeh");
		instanz.test();
	}

	public void test() {

		WebResource resource = Client.create().resource(server_wurzel_uri);
		if (this.authFilter != null){
			resource.addFilter(this.authFilter);
		}
		ClientResponse response = resource.get(ClientResponse.class);

		//System.out.println(String.format("GET on [%s], status code [%d]", server_wurzel_uri, response.getStatus()));
		int status = response.getStatus();
		response.close();

		if (status != 200) {
			System.out.println("Keine Verbindung zum Server (Statuscode "+status+") -- beende.");
			System.exit(1);
		} else {
			System.out.println("Verbunden (Statuscode "+status+").");
		}

		// Knoten erstellen
		URI firstNode = erstelleKnoten();
		eigenschaftHinzufuegen(firstNode, "name", "Joe Strummer");
		URI secondNode = erstelleKnoten();
		eigenschaftHinzufuegen(secondNode, "band", "The Clash");

		try {
			URI relationshipUri = addRelationship(firstNode, secondNode,
					"singer", "{ \"from\" : \"1976\", \"until\" : \"1986\" }");
			addMetadataToProperty(relationshipUri, "stars", "5");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}
	
	public URI erstelleKnoten(String name){
		URI uri = this.erstelleKnoten();
		try {
			this.addMetadataToProperty(uri, "title", name);
			//this.addLabelToNode(uri, "name", name);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return uri;
	}

	public URI erstelleKnoten() {

		WebResource noderesource = Client.create().resource(server_knoten_uri);
		if (this.authFilter != null){
			noderesource.addFilter(this.authFilter);
		}
		// POST {} to the node entry point URI
		ClientResponse noderesponse = null;
		try {
			noderesponse = noderesource
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).entity("{}")
				.post(ClientResponse.class);
		} catch (Exception e){
			e.printStackTrace(System.out);
		}

		final URI location = noderesponse.getLocation();
		// System.out.println(String.format("POST to [%s], status code [%d], location header [%s]",server_knoten_uri, noderesponse.getStatus(),location.toString()));
		noderesponse.close();

		return location;
	}
	
	public void eigenschaftHinzufuegen(URI knotenUri,
			String eigenschaftsBezeichner, String eigenschaftsWert) {
		this.metaInformationHinzufuegen(knotenUri, eigenschaftsBezeichner, eigenschaftsWert, "/properties/", false);
	}
	
	public void eigenschaftHinzufuegen(URI knotenUri,
			String eigenschaftsBezeichner, int eigenschaftsWert) {
		this.metaInformationHinzufuegen(knotenUri, eigenschaftsBezeichner, String.valueOf(eigenschaftsWert), "/properties/", false);
	}
	
	public void etikettHinzufuegen(URI knotenUri, String etikettWert) {
		this.metaInformationHinzufuegen(knotenUri, "", etikettWert, "/labels", true);
	}

	private void metaInformationHinzufuegen(URI knotenUri,
			String eigenschaftsBezeichner, String eigenschaftsWert, String pfad, boolean post) {
		String propertyUri = knotenUri.toString() + pfad
				+ eigenschaftsBezeichner;
		// http://localhost:7474/db/data/node/{node_id}/properties/{property_name}

		WebResource resource = Client.create().resource(propertyUri);
		if (this.authFilter != null){
			resource.addFilter(this.authFilter);
		}
		ClientResponse response;
		if (post)
			response = resource.accept(MediaType.APPLICATION_JSON)
					.type(MediaType.APPLICATION_JSON)
					.entity("\"" + eigenschaftsWert + "\"")
					.post(ClientResponse.class);
		else
			response = resource.accept(MediaType.APPLICATION_JSON)
					.type(MediaType.APPLICATION_JSON)
					.entity("\"" + eigenschaftsWert + "\"")
					.put(ClientResponse.class);

		// System.out.println(String.format("PUT to [%s], status code [%d]",propertyUri, response.getStatus()));
		response.close();
	}

	public URI addRelationship(URI startNode, URI endNode,
			String relationshipType, String jsonAttributes)
			throws URISyntaxException {
		URI fromUri = new URI(startNode.toString() + "/relationships");
		String relationshipJson = generateJsonRelationship(endNode,
				relationshipType, jsonAttributes);

		WebResource resource = Client.create().resource(fromUri);
		if (this.authFilter != null){
			resource.addFilter(this.authFilter);
		}
		// POST JSON to the relationships URI
		ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).entity(relationshipJson)
				.post(ClientResponse.class);

		final URI location = response.getLocation();
		// System.out.println(String.format("POST to [%s], status code [%d], location header [%s]",fromUri, response.getStatus(), location.toString()));

		response.close();
		return location;
	}

	private String generateJsonRelationship(URI endNode,
			String relationshipType, String... jsonAttributes) {
		StringBuilder sb = new StringBuilder();
		sb.append("{ \"to\" : \"");
		sb.append(endNode.toString());
		sb.append("\", ");

		sb.append("\"type\" : \"");
		sb.append(relationshipType);
		if (jsonAttributes == null || jsonAttributes.length < 1) {
			sb.append("\"");
		} else {
			sb.append("\", \"data\" : ");
			for (int i = 0; i < jsonAttributes.length; i++) {
				sb.append(jsonAttributes[i]);
				if (i < jsonAttributes.length - 1) { // Miss off the final comma
					sb.append(", ");
				}
			}
		}

		sb.append(" }");
		return sb.toString();
	}

	public void addLabelToNode(URI nodeUri, String name,
			String value) throws URISyntaxException {
		URI labelUri = new URI(nodeUri.toString() + "/labels");
		String entity = toJsonNameValuePairCollection(name, value);
		WebResource resource = Client.create().resource(labelUri);
		if (this.authFilter != null){
			resource.addFilter(this.authFilter);
		}
		ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).entity(entity)
				.put(ClientResponse.class);

		// System.out.println(String.format("PUT [%s] to [%s], status code [%d]",entity, propertyUri, response.getStatus()));
		response.close();
	}

	public void addMetadataToProperty(URI relationshipUri, String name,
			String value) throws URISyntaxException {
		URI propertyUri = new URI(relationshipUri.toString() + "/properties");
		String entity = toJsonNameValuePairCollection(name, value);
		WebResource resource = Client.create().resource(propertyUri);
		if (this.authFilter != null){
			resource.addFilter(this.authFilter);
		}
		ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).entity(entity)
				.put(ClientResponse.class);

		// System.out.println(String.format("PUT [%s] to [%s], status code [%d]",entity, propertyUri, response.getStatus()));
		response.close();
	}

	private String toJsonNameValuePairCollection(String name, String value) {
		return String.format("{ \"%s\" : \"%s\" }", name, value);
	}
	
	/**
	 * Erstellt mehrere Kanten in einer Transaktion (hat deutlich hoehere Performanz).
	 * Die uebergebenen Arrays muessen gleich lang sein!
	 * @param quellen IDs der Quell-Knoten
	 * @param ziele IDs der Ziel-Knoten
	 * @param gewichte Gewicht der Kanten
	 * @param beziehungsTyp Association type
	 * @param graphDb Graph DB
	 * @throws Exception Bei unterschiedlich langen Arrays als Eingabe
	 */
	public void erstelleKanten(int[] quellen, int[] ziele, Double[] gewichte, String beziehungsTyp, GraphDatabaseService graphDb) throws Exception {
		
		// Eingabe pruefen
		if (quellen.length != ziele.length || ziele.length != gewichte.length){
			throw new Exception("Es muessen gleich viele Quellen, Ziele und Gewichte angegeben werden!");
		}
		
		//String jsonAttributes = "{}";
		Neo4JRestRequest request = new Neo4JRestRequest();
		
		for (int i=0; i<quellen.length; i++){
    		String statement = "MATCH (a),(b) "
    				+ "WHERE WHERE id(a) = "+quellen[i]+" AND WHERE id(b) = "+ziele[i]+" "
    				+ "CREATE (a)-[r:RELTYPE { name : a.name + '<->' + b.name }]->(b)";
    		request.addStatement(statement);
    	}
		
		Transaction tx = graphDb.beginTx();
	    try
	    {
	    
	    	//graphDb.
	        tx.success();
	    }
	    finally
	    {
	        tx.close();
	    }
		
	}

}
