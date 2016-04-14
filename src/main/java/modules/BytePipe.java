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
	 * Get the input stream.
	 * @return input stream
	 */
	public PipedInputStream getInput() {
		return input;
	}

	/**
	 * Get the output stream.
	 * @return output stream
	 */
	public PipedOutputStream getOutput() {
		return output;
	}

	/**
	 * Writes to the output pipe.
	 * @see PipedOutputStream#write(byte[], int, int) PipedOutputStream.write
	 * @param data byte-array with data to write
	 * @param offset write offset
	 * @param length length of data to write
	 * @throws IOException thrown on I/O error
	 */
	public void write(byte[] data, int offset, int length) throws IOException {
		this.output.write(data, offset, length);
	}
	
	@Override
	public void writeClose() throws IOException {
		this.output.close();
	}
	
	/**
	 * Reads from the input pipe.
	 * @see PipedInputStream#read(byte[], int, int) PipedInputStream.read
	 * @param buffer buffer to store read input in
	 * @param offset read offset
	 * @param length amount of bytes to read
	 * @return amount of bytes read
	 * @throws IOException thrown on I/O error
	 */
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
