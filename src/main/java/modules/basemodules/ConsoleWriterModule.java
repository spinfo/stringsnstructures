package modules.basemodules;

import java.io.PrintStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import modules.BytePipe;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.NotSupportedException;
import common.parallelization.CallbackReceiver;

/**
 * Writes any input to console
 * 
 * @author Marcel Boeing
 *
 */
public class ConsoleWriterModule extends ModuleImpl {

	private final String INPUTID = "input";

	public ConsoleWriterModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		// Add module description
		this.setDescription("Writes char output to console.");

		// Define I/O Ports
		InputPort inputPort = new InputPort(INPUTID,
				"Accepts byte stream and character input.", this);
		inputPort.addSupportedPipe(BytePipe.class);
		inputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputPort);

		// Add default values
		super.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"Console Writer");
	}

	@Override
	public boolean process() throws Exception {

		// Determine input port
		InputPort inputPort = this.getInputPorts().get(INPUTID);

		// Keep track of whether an input has been successfully read
		boolean successfullyReadInput = false;

		/*
		 * try to read from the stream input channel
		 */
		try {
			if (inputPort.getInputStream() != null) {
				// Output stream
				PrintStream out = System.out;

				// Define input buffer
				byte[] buffer = new byte[1024];

				// Read file data into buffer and write to outputstream
				int readBytes = inputPort.getInputStream().read(buffer);
				while (readBytes > 0) {
					out.write(buffer, 0, readBytes);
					readBytes = inputPort.getInputStream().read(buffer);
				}

				// Log message
				Logger.getLogger(this.getClass().getSimpleName()).log(
						Level.INFO, "Streamed output onto console");

				// Keep track of whether an input has been successfully read
				successfullyReadInput = true;

			}
		} catch (NotSupportedException e) {

		}

		try {
			if (!successfullyReadInput && inputPort.getInputReader() != null) {
				/*
				 * The inputstream does not seem to be connected -- try reader
				 * instead
				 */

				// Output writer
				PrintStream out = System.out;

				// Define input buffer
				char[] buffer = new char[1024];

				// Read file data into buffer and output to writer
				int readChars = inputPort.getInputReader().read(buffer);
				while (readChars != -1) {
					for (int i = 0; i < readChars; i++) {
						if (buffer[i] < 0)
							break;
						out.print(buffer[i]);
					}
					readChars = inputPort.getInputReader().read(buffer);
				}

				// Log message
				Logger.getLogger(this.getClass().getSimpleName()).log(
						Level.INFO, "Wrote output to console");

				// Keep track of whether an input has been successfully read
				successfullyReadInput = true;
			}
		} catch (NotSupportedException e) {

		}

		// If no input has been successfully read throw an exception
		if (!successfullyReadInput)
			throw new Exception(
					"The input of this module does not seem to be connected to anything.");

		// Success
		return true;
	}

}
