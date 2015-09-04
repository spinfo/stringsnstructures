package modules.neo4j;

import java.util.HashMap;
import java.util.Map;

public class Neo4JRestRequest {
	
	public final String STATEMENT_KEY = "statement";
	
	private Map<String,Object> statements;
	
	public Neo4JRestRequest(){
		statements = new HashMap<String,Object>();
	}

	/**
	 * @return the statements
	 */
	public Map<String, Object> getStatements() {
		return statements;
	}

	/**
	 * @param statements the statements to set
	 */
	public void setStatements(Map<String, Object> statements) {
		this.statements = statements;
	}
	
	public void addStatement(String statement){
		this.statements.put(STATEMENT_KEY, statement);
	}

}
