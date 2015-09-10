package modules;

import java.util.Map;

public class SerializablePort {

	private Integer instanceHashCode;
	private String name;
	private Map<String,Integer> connectedPipesDestinationHashCodes;
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
	protected Map<String, Integer> getConnectedPipesDestinationHashCodes() {
		return connectedPipesDestinationHashCodes;
	}
	/**
	 * @param connectedPipesDestinationHashCodes the connectedPipesDestinationHashCodes to set
	 */
	protected void setConnectedPipesDestinationHashCodes(
			Map<String, Integer> connectedPipesDestinationHashCodes) {
		this.connectedPipesDestinationHashCodes = connectedPipesDestinationHashCodes;
	}
	
}
