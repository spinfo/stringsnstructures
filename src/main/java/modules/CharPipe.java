package modules;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

public class CharPipe implements Pipe {
	
	private PipedReader input;
	private PipedWriter output;

	public CharPipe() throws IOException {
		this.reset();
	}
	
	/**
	 * Get input reader
	 * @return the input
	 */
	public PipedReader getInput() {
		return input;
	}

	/**
	 * Get output writer
	 * @return the output
	 */
	public PipedWriter getOutput() {
		return output;
	}
	
	/**
	 * Writes to the output pipe.
	 * @see java.io.Writer#write(String) Writer.write
	 * @param data String to write
	 * @throws IOException thrown on I/O error
	 */
	public void write(String data) throws IOException {
		this.output.write(data);
	}
	
	/**
	 * Writes to the output pipe.
	 * @see PipedWriter#write(char[], int, int) PipedReader.write
	 * @param data char-array with data to write
	 * @param offset write offset
	 * @param length length of data to write
	 * @throws IOException thrown on I/O error
	 */
	public void write(char[] data, int offset, int length) throws IOException {
		this.output.write(data, offset, length);
	}

	@Override
	public void writeClose() throws IOException {
		this.output.close();
	}
	
	/**
	 * Reads from the input pipe.
	 * @see PipedReader#read(char[], int, int) PipedReader.read
	 * @param buffer buffer to store read input in
	 * @param offset read offset
	 * @param length amount of chars to read
	 * @return amount of chars read
	 * @throws IOException thrown on I/O error
	 */
	public int read(char[] buffer, int offset, int length) throws IOException {
		return this.input.read(buffer, offset, length);
	}
	
	@Override
	public void readClose() throws IOException {
		this.input.close();
	}

	@Override
	public void reset() throws IOException {
		this.input = new PipedReader();
		this.output = new PipedWriter();
		this.input.connect(this.output);
	}

}
