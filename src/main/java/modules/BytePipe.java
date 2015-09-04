package modules;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class BytePipe implements Pipe {
	
	private PipedInputStream input;
	private PipedOutputStream output;

	public BytePipe() throws IOException {
		this.reset();
	}
	
	/**
	 * @return the input
	 */
	public PipedInputStream getInput() {
		return input;
	}

	/**
	 * @return the output
	 */
	public PipedOutputStream getOutput() {
		return output;
	}

	public void write(byte[] data, int offset, int length) throws IOException {
		this.output.write(data, offset, length);
	}
	
	@Override
	public void writeClose() throws IOException {
		this.output.close();
	}
	
	public int read(byte[] buffer, int offset, int length) throws IOException {
		return this.input.read(buffer, offset, length);
	}
	
	@Override
	public void readClose() throws IOException {
		this.input.close();
	}

	@Override
	public void reset() throws IOException {
		this.input = new PipedInputStream();
		this.output = new PipedOutputStream();
		this.input.connect(this.output);
	}

}
