package modules;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedReader;


public class InputPort extends AbstractPort {
	private Pipe pipe;
	private Port connectedPort;

	public InputPort(String name, String description, Module parent) {
		super(name, description, parent);
		this.pipe = null;
		this.connectedPort = null;
	}
	
	/**
	 * @return the pipes
	 */
	public Pipe getPipe() {
		return this.pipe;
	}
	
	/**
	 * @return the connectedPort
	 */
	public Port getConnectedPort() {
		return connectedPort;
	}
	
	/**
	 * Returns the "naked" input stream.
	 * @return Input stream
	 * @throws NotSupportedException Thrown if something goes wrong
	 */
	public PipedInputStream getInputStream() throws NotSupportedException {
		if (this.pipe == null) return null;
		if (!this.pipe.getClass().equals(BytePipe.class)){
			throw new NotSupportedException("This port ("+this.toString()+") does not provide byte stream input.");
		} else {
			BytePipe bytePipe = (BytePipe) this.pipe;
			return bytePipe.getInput();
		}
	}
	
	/**
	 * Returns the "naked" input reader.
	 * @return Input reader
	 * @throws NotSupportedException Thrown if something goes wrong
	 */
	public PipedReader getInputReader() throws NotSupportedException {
		if (this.pipe == null) return null;
		if (!this.pipe.getClass().equals(CharPipe.class)){
			throw new NotSupportedException("This port ("+this.toString()+") does not provide character stream input.");
		} else {
			CharPipe charPipe = (CharPipe) this.pipe;
			return charPipe.getInput();
		}
	}
	
	/**
	 * Reads bytes from the input.
	 * @see modules.BytePipe#read(byte[] buffer, int offset, int length)
	 * @param buffer Buffer
	 * @param offset Offset
	 * @param length Length
	 * @return Bytes read (-1 if input is closed)
	 * @throws NotSupportedException Thrown if this port does not provide byte stream input after all
	 * @throws IOException Thrown if something goes wrong
	 */
	public int read(byte[] buffer, int offset, int length) throws NotSupportedException, IOException {
		if (this.pipe == null) throw new IOException("There is no pipe to read from.");
		if (!this.pipe.getClass().equals(BytePipe.class)){
			throw new NotSupportedException("This port ("+this.toString()+") does not provide byte stream input.");
		} else {
			BytePipe bytePipe = (BytePipe) this.pipe;
			return bytePipe.read(buffer, offset, length);
		}
	}
	
	/**
	 * Reads characters from the input.
	 * @see modules.CharPipe#read(char[] buffer, int offset, int length)
	 * @param buffer Buffer
	 * @param offset Offset
	 * @param length Length
	 * @return Chars read (-1 if input is closed)
	 * @throws NotSupportedException Thrown if this port does not provide character stream input after all
	 * @throws IOException Thrown if something goes wrong
	 */
	public int read(char[] buffer, int offset, int length) throws NotSupportedException, IOException {
		if (this.pipe == null) throw new IOException("There is no pipe to read from.");
		if (!this.pipe.getClass().equals(CharPipe.class)){
			throw new NotSupportedException("This port ("+this.toString()+") does not provide character stream input.");
		} else {
			CharPipe charPipe = (CharPipe) this.pipe;
			return charPipe.read(buffer, offset, length);
		}
	}

	@Override
	public void addPipe(Pipe pipe, Port connectedPort) throws NotSupportedException, OccupiedException {
		if (super.supportsPipe(pipe)){
				if (this.pipe == null)
					if (OutputPort.class.isAssignableFrom(connectedPort.getClass())){
						this.pipe = pipe;
						this.connectedPort = connectedPort;
					} else
						throw new NotSupportedException("This port ("+this.toString()+") can only be connected to an output port.");
				else
					throw new OccupiedException("This input port ("+this.toString()+") is already occupied.");
		} else
			throw new NotSupportedException("That pipe is not supported by this port ("+this.toString()+").");
	}

	@Override
	public void removePipe(Pipe pipe) throws NotFoundException {
		if (pipe.equals(this.pipe)){
			this.connectedPort.removePipe(pipe);
			this.pipe = null;
			this.connectedPort = null;
		}
		else throw new NotFoundException("The specified pipe could not be found.");
	}

	@Override
	public void reset() throws IOException {
		this.pipe.reset();
	}

	@Override
	public boolean isConnected() {
		return this.connectedPort != null;
	}
	

}
