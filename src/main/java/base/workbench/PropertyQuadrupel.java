package base.workbench;

/**
 * Holds a property (key:value) as well as its default value and its description.
 * @author Marcel Boeing
 *
 */
public class PropertyQuadrupel {

	private String key;
	private String value;
	private String description;
	private String defaultValue;
	
	public PropertyQuadrupel() {
	}

	public PropertyQuadrupel(String key, String value, String description,
			String defaultValue) {
		super();
		this.key = key;
		this.value = value;
		this.description = description;
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	

}
