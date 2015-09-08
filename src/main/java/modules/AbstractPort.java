package modules;

import java.util.HashMap;
import java.util.Map;


public abstract class AbstractPort implements Port {
	
	private Map<String, Class<?>> supportedPipes = new HashMap<String, Class<?>>();
	private String name;
	private String description;

	public AbstractPort(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see modules.Port#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see modules.Port#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see modules.Port#getDescription()
	 */
	@Override
	public String getDescription() {
		return this.description;
	}

	/* (non-Javadoc)
	 * @see modules.Port#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see modules.Port#supportsPipe(modules.Pipe)
	 */
	@Override
	public boolean supportsPipe(Pipe pipe) {
		return this.supportsPipeClass(pipe.getClass());
	}
	
	/* (non-Javadoc)
	 * @see modules.Port#supportsPipeClass(Class<Pipe> pipeClass)
	 */
	@Override
	public boolean supportsPipeClass(Class<?> pipeClass) {
		return this.supportedPipes.containsKey(pipeClass.getCanonicalName());
	}

	/* (non-Javadoc)
	 * @see modules.Port#getSupportedPipeClasses()
	 */
	@Override
	public Map<String, Class<?>> getSupportedPipeClasses() {
		return this.supportedPipes;
	}

	/**
	 * Adds a class of pipe to the list of the ones supported.
	 * @param pipeClass
	 */
	protected void addSupportedPipe(Class<?> pipeClass) {
		this.supportedPipes.put(pipeClass.getCanonicalName(),pipeClass);
	}

}
