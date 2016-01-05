package modules;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import common.parallelization.CallbackReceiver;

public class ModuleLabel implements Module {
	
	private String label;
	
	public ModuleLabel(String label){
		this.label = label;
	}
	
	@Override
	public String toString(){
		return this.label;
	}

	@Override
	public CallbackReceiver getCallbackReceiver() {
		return null;
	}

	@Override
	public void setCallbackReceiver(CallbackReceiver callbackReceiver) {
	}

	@Override
	public void run() {
	}

	@Override
	public Map<String, InputPort> getInputPorts() {
		return null;
	}

	@Override
	public Map<String, OutputPort> getOutputPorts() {
		return null;
	}

	@Override
	public boolean process() throws Exception {
		return false;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void setName(String name) {
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void setDescription(String desc) {
	}

	@Override
	public Properties getProperties() {
		return null;
	}

	@Override
	public void setProperties(Properties properties) throws Exception {
	}

	@Override
	public Map<String, String> getPropertyDescriptions() {
		return null;
	}

	@Override
	public Map<String, String> getPropertyDefaultValues() {
		return null;
	}

	@Override
	public int getStatus() {
		return 0;
	}

	@Override
	public void applyProperties() throws Exception {
	}

	@Override
	public void resetOutputs() throws IOException {
	}

	@Override
	public String getCategory() {
		return null;
	}

	@Override
	public void setCategory(String category) {
	}

}
