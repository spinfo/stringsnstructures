package modules;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedReader;


public class InputPort extends AbstractPort {
	private Pipe pipe;
	
	
	public InputPort(String name, String description, Module parent) {
		super(name, description, parent);
		this.pipe = null;
	}
	
	/**
	 * @return the pipes
	 */
	public Pipe getPipe() {
		return this.pipe;
	}
	
	/**
	 * Returns the "naked" input stream.
	 * @return
	 * @throws NotSupportedException
	 */
	public PipedInputStream getInputStream() throws NotSupportedException {
		if (!this.pipe.getClass().equals(BytePipe.class)){
			throw new NotSupportedException("This port does not provide byte stream input.");
		} else {
			BytePipe bytePipe = (BytePipe) this.pipe;
			return bytePipe.getInput();
		}
	}
	
	/**
	 * Returns the "naked" input reader.
	 * @return
	 * @throws NotSupportedException
	 */
	public PipedReader getInputReader() throws NotSupportedException {
		if (!this.pipe.getClass().equals(CharPipe.class)){
			throw new NotSupportedException("This port does not provide character stream input.");
		} else {
			CharPipe charPipe = (CharPipe) this.pipe;
			return charPipe.getInput();
		}
	}
	
	/**
	 * Reads bytes from the input.
	 * @see modules.BytePipe#read(byte[] buffer, int offset, int length)
	 * @throws NotSupportedException Thrown if this port does not provide byte stream input after all
	 */
	public int read(byte[] buffer, int offset, int length) throws NotSupportedException, IOException {
		if (!this.pipe.getClass().equals(BytePipe.class)){
			throw new NotSupportedException("This port does not provide byte stream input.");
		} else {
			BytePipe bytePipe = (BytePipe) this.pipe;
			return bytePipe.read(buffer, offset, length);
		}
	}
	
	/**
	 * Reads characters from the input.
	 * @see modules.CharPipe#read(char[] buffer, int offset, int length)
	 * @throws NotSupportedException Thrown if this port does not provide character stream input after all
	 */
	public int read(char[] buffer, int offset, int length) throws NotSupportedException, IOException {
		if (!this.pipe.getClass().equals(CharPipe.class)){
			throw new NotSupportedException("This port does not provide character stream input.");
		} else {
			CharPipe charPipe = (CharPipe) this.pipe;
			return charPipe.read(buffer, offset, length);
		}
	}

	@Override
	public void addPipe(Pipe pipe) throws NotSupportedException, OccupiedException {
		if (super.supportsPipe(pipe)){
				if (this.pipe == null)
					this.pipe = pipe;
				else
					throw new OccupiedException("This input port is already occupied.");
		} else
			throw new NotSupportedException("That pipe is not supported by this port.");
	}

	@Override
	public void removePipe(Pipe pipe) throws NotFoundException {
		if (pipe.equals(this.pipe))
			pipe = null;
		else throw new NotFoundException("The specified pipe could not be found.");
	}

	@Override
	public void reset() throws IOException {
		this.pipe.reset();
	}
	

}
