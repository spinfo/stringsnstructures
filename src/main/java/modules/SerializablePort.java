package modules;

import java.util.HashMap;
import java.util.Map;

public class SerializablePort {

	private Integer instanceHashCode;
	private String name;
	private Map<Integer,String> connectedPipesDestinationHashCodes; // Key: Destination pipe instance hashcode, value: canonical class name of pipe
	
	public SerializablePort() {
		super();
		this.connectedPipesDestinationHashCodes = new HashMap<Integer,String>();
	}
	/**
	 * @return the instanceHashCode
	 */
	protected Integer getInstanceHashCode() {
		return instanceHashCode;
	}
	/**
	 * @param instanceHashCode the instanceHashCode to set
	 */
	protected void setInstanceHashCode(Integer instanceHashCode) {
		this.instanceHashCode = instanceHashCode;
	}
	/**
	 * @return the name
	 */
	protected String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	protected void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the connectedPipesDestinationHashCodes
	 */
	protected Map<Integer, String> getConnectedPipesDestinationHashCodes() {
		return connectedPipesDestinationHashCodes;
	}
	/**
	 * @param connectedPipesDestinationHashCodes the connectedPipesDestinationHashCodes to set
	 */
	protected void setConnectedPipesDestinationHashCodes(
			Map<Integer, String> connectedPipesDestinationHashCodes) {
		this.connectedPipesDestinationHashCodes = connectedPipesDestinationHashCodes;
	}
	
}
