package modules.input_output;

import java.util.Properties;

import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

public class JoinModule extends ModuleImpl {

	// Identifiers for inputs and output
	private static final String INPUT1_ID = "Input 1";
	private static final String INPUT2_ID = "Input 2";
	private static final String OUTPUT_ID = "Output";

	public JoinModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// add neccessary module descriptions
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Join Module");

		this.setDescription("Module to join the character output of two modules.");

		// add input and output ports
		InputPort in1 = new InputPort(INPUT1_ID, "First character input.", this);
		in1.addSupportedPipe(CharPipe.class);
		this.addInputPort(in1);

		InputPort in2 = new InputPort(INPUT2_ID, "Second character input.", this);
		in2.addSupportedPipe(CharPipe.class);
		this.addInputPort(in2);

		OutputPort out = new OutputPort(OUTPUT_ID, "Combined character output.", this);
		out.addSupportedPipe(CharPipe.class);
		this.addOutputPort(out);
	}

	@Override
	public boolean process() throws Exception {

		boolean result = true;

		StringBuilder sb = new StringBuilder();
		InputPort in1 = this.getInputPorts().get(INPUT1_ID);
		InputPort in2 = this.getInputPorts().get(INPUT2_ID);

		try {
			if (in1.isConnected()) {
				sb.append(super.readStringFromInputPort(in1));
			}
			if (in2.isConnected()) {
				sb.append(super.readStringFromInputPort(in2));
			}

			this.getOutputPorts().get(OUTPUT_ID).outputToAllCharPipes(sb.toString());

		} catch (Exception e) {
			result = false;
		} finally {
			this.closeAllOutputs();
		}

		return result;
	}
}
