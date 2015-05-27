package modularization;

import java.io.IOException;

public interface Pipe {

	/**
	 * Closes the pipe's input (that is read from)
	 * @throws IOException
	 */
	public void readClose() throws IOException;
	
	/**
	 * Closes the pipe's output (that is written to)
	 * @throws IOException
	 */
	public void writeClose() throws IOException;
}
