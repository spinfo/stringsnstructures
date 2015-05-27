package modularization;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

public class CharPipe implements Pipe {
	
	private PipedReader input;
	private PipedWriter output;

	public CharPipe() throws IOException {
		this.input = new PipedReader();
		this.output = new PipedWriter();
		this.input.connect(this.output);
	}
	
	/**
	 * @return the input
	 */
	public PipedReader getInput() {
		return input;
	}

	/**
	 * @return the output
	 */
	public PipedWriter getOutput() {
		return output;
	}

	public void write(String data) throws IOException {
		this.output.write(data);
	}
	
	public void write(char[] data, int offset, int length) throws IOException {
		this.output.write(data, offset, length);
	}

	@Override
	public void writeClose() throws IOException {
		this.output.close();
	}
	
	public int read(char[] buffer, int offset, int length) throws IOException {
		return this.input.read(buffer, offset, length);
	}
	
	@Override
	public void readClose() throws IOException {
		this.input.close();
	}

}
