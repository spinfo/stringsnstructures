package modules.basemodules;

import java.io.PrintStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import modules.BytePipe;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;

import common.parallelization.CallbackReceiver;

/**
 * Writes any input to console
 * @author Marcel Boeing
 *
 */
public class ConsoleWriterModule extends ModuleImpl {
	
	private final String INPUTID = "input";
	
	public ConsoleWriterModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		// Define I/O Ports
		InputPort inputPort = new InputPort("Input", "Accepts byte stream and character input.", this);
		inputPort.addSupportedPipe(BytePipe.class);
		inputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(INPUTID,inputPort);
		
		// Add default values
		super.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Console Writer");
	}

	@Override
	public boolean process() throws Exception {
		
		/*
		 * read from both input channels (reader/inputstream) -- if the first one
		 * is not connected to another module's output, it will throw an exception
		 * (that we will catch)
		 */
		try {
			// Output stream
			PrintStream out = System.out;
			
			// Define input buffer
			byte[] buffer = new byte[1024];
			
			// Read file data into buffer and write to outputstream
			int readBytes = this.getInputPorts().get(INPUTID).getInputStream().read(buffer);
			while (readBytes>0){
				out.write(buffer, 0, readBytes);
				readBytes = this.getInputPorts().get(INPUTID).getInputStream().read(buffer);
			}
			
			// Log message
			Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Streamed output onto console");
			
		} catch (Exception e){
			/* The inputstream does not seem to be connected -- try reader/writer instead */
			
			// Output writer
			PrintStream out = System.out;
			
			// Define input buffer
			char[] buffer = new char[1024];
			
			// Read file data into buffer and output to writer
			int readChars = this.getInputPorts().get(INPUTID).getInputReader().read(buffer);
			while(readChars != -1){
				for (int i=0; i<readChars; i++){
					if (buffer[i]<0)
						break;
					out.print(buffer[i]);
				}
				readChars = this.getInputPorts().get(INPUTID).getInputReader().read(buffer);
			}
			
			// Log message
			Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Wrote output to console");
		}
		
		// Success
		return true;
	}

}
