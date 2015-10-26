package modules;

import java.io.IOException;

public interface Pipe {

	/**
	 * Closes the pipe's input (that is read from)
	 * @throws IOException Thrown if something goes wrong
	 */
	public void readClose() throws IOException;
	
	/**
	 * Closes the pipe's output (that is written to)
	 * @throws IOException Thrown if something goes wrong
	 */
	public void writeClose() throws IOException;
	
	/**
	 * Resets the I/O (for re-use after close)
	 * @throws IOException Thrown if something goes wrong
	 */
	public void reset() throws IOException;
}
