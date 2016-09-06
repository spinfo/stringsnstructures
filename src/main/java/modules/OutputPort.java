package modules;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class OutputPort extends AbstractPort {
	
	// Maps a list of pipes to each of the supported pipe classes 
	private Map<Class<? extends Pipe>, List<Pipe>> pipes;
	private Map<Pipe, Port> connectedPorts;
	
	
	public OutputPort(String name, String description, Module parent) {
		super(name, description, parent);
		this.pipes = new ConcurrentHashMap<Class<? extends Pipe>, List<Pipe>>();
		this.connectedPorts = new ConcurrentHashMap<Pipe, Port>();
	}


	/* (non-Javadoc)
	 * @see modules.AbstractPort#addSupportedPipe(java.lang.Class)
	 */
	@Override
	public void addSupportedPipe(Class<? extends Pipe> pipeClass) {
		if (!super.supportsPipeClass(pipeClass)){
			super.addSupportedPipe(pipeClass);
			this.pipes.put(pipeClass, new CopyOnWriteArrayList<Pipe>());
		}
	}


	@Override
	public void addPipe(Pipe pipe, Port connectedPort) throws NotSupportedException, OccupiedException {
		if (super.supportsPipe(pipe))
			if (InputPort.class.isAssignableFrom(connectedPort.getClass())){
				this.pipes.get(pipe.getClass()).add(pipe);
				this.connectedPorts.put(pipe, connectedPort);
			} else
				throw new NotSupportedException("This port ("+this.toString()+") can only be connected to an input port.");
		else throw new NotSupportedException("That pipe is not supported by this port ("+this.toString()+").");
	}

	/**
	 * Returns a map containing lists of pipe instances for each supported class of pipe.
	 * @return the pipes
	 */
	public Map<Class<? extends Pipe>, List<Pipe>> getPipes() {
		return pipes;
	}
	
	/**
	 * Returns the pipes of a specified class.
	 * @param pipeClass Pipe class
	 * @return List of pipes
	 */
	public List<Pipe> getPipes(Class<? extends Pipe> pipeClass) {
		return pipes.get(pipeClass);
	}
	
	/**
	 * Returns the port the specified pipe is connected to.
	 * @param pipe Pipe
	 * @return Port
	 */
	public Port getConnectedPort(Pipe pipe){
		return this.connectedPorts.get(pipe);
	}


	@Override
	public void removePipe(Pipe pipe) throws NotFoundException {
		if (!this.pipes.containsKey(pipe.getClass()) || !this.pipes.get(pipe.getClass()).remove(pipe))
			throw new NotFoundException("The specified pipe could not be found.");
		if (this.connectedPorts.get(pipe) != null)
			this.connectedPorts.remove(pipe);
	}
	
	/**
	 * Writes the given byte array to all stream outputs.
	 * @param data Data to write
	 * @param offset The start offset in the data
	 * @param bytesToWrite The number of bytes to write
	 * @throws IOException Thrown if an I/O problem occurs
	 */
	public void outputToAllBytePipes(byte[] data, int offset, int bytesToWrite) throws IOException {
		
		// Check whether this port does support byte stream output
		if (!this.supportsPipeClass(BytePipe.class))
			throw new IOException("This port ("+this.toString()+") does not support byte stream output.");
		
		// Loop over the defined outputs
		Iterator<Pipe> outputStreams = this.pipes.get(BytePipe.class).iterator();
		while (outputStreams.hasNext()) {

			// Determine the next output on the list
			BytePipe outputStream = (BytePipe) outputStreams.next();

			// Write file list JSON to output
			outputStream.write(data, offset, bytesToWrite);
		}
	}
	
	/**
	 * Writes the given data to all char output pipes.
	 * @param data Data to write
	 * @param offset The start offset in the data
	 * @param charsToWrite The number of chars to write
	 * @throws IOException Thrown if an I/O problem occurs
	 */
	public void outputToAllCharPipes(char[] data, int offset, int charsToWrite) throws IOException {
		
		// Check whether this port does support byte stream output
		if (!this.supportsPipeClass(CharPipe.class))
			throw new IOException("This port ("+this.toString()+") does not support character stream output.");
		
		// Loop over the defined outputs
		Iterator<Pipe> outputPipes = this.pipes.get(CharPipe.class).iterator();
		while (outputPipes.hasNext()) {

			// Determine the next output on the list
			CharPipe outputPipe = (CharPipe) outputPipes.next();

			// Write file list JSON to output
			outputPipe.write(data, offset, charsToWrite);
		}
	}
	
	/**
	 * Writes the given byte array to all byte stream output pipes.
	 * @param data Data to write
	 * @throws IOException Thrown if an I/O problem occurs
	 */
	public void outputToAllBytePipes(byte[] data) throws IOException {
		this.outputToAllBytePipes(data, 0, data.length);
	}

	/**
	 * Writes the given String to all char output pipes.
	 * @param data Data to write
	 * @throws IOException Thrown if an I/O problem occurs
	 */
	public void outputToAllCharPipes(String data) throws IOException {
		if (data != null && !data.isEmpty())
			this.outputToAllCharPipes(data.toCharArray(), 0, data.length());
	}
	
	public void close() throws IOException{
		Iterator<List<Pipe>> pipeLists = this.getPipes().values().iterator();
		while (pipeLists.hasNext()){
			Iterator<Pipe> pipes = pipeLists.next().iterator();
			while (pipes.hasNext()){
				Pipe pipe = pipes.next();
				pipe.writeClose();
			}
		}
	}


	@Override
	public void reset() throws IOException {
		Iterator<List<Pipe>> pipeLists = this.getPipes().values().iterator();
		while (pipeLists.hasNext()){
			Iterator<Pipe> pipes = pipeLists.next().iterator();
			while (pipes.hasNext()){
				Pipe pipe = pipes.next();
				pipe.reset();
			}
		}
	}


	@Override
	public boolean isConnected() {
		return !this.connectedPorts.isEmpty();
	}

}
