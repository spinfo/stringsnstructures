package modules.neo4j;

import org.neo4j.graphdb.RelationshipType;

public class Uebereinstimmungsquotientenverbindungstyp implements
		RelationshipType {

	@Override
	public String name() {
		return "aehnelt";
	}

}
