package modules;

import java.io.IOException;
import java.util.Map;

public interface Port {

	public String getName();
	public void setName(String name);
	public String getDescription();
	public void setDescription(String description);
	public boolean supportsPipe(Pipe pipe);
	public boolean supportsPipeClass(Class<?> pipeClass);
	public Map<String, Class<?>> getSupportedPipeClasses();
	public void addPipe(Pipe pipe, Port connectingPort) throws NotSupportedException, OccupiedException;
	public void removePipe(Pipe pipe) throws NotFoundException;
	public void reset() throws IOException;
	public Module getParent();
}
