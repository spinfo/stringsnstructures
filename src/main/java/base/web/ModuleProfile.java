package base.web;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.annotations.Expose;

import modules.Module;
import modules.Port;

class ModuleProfile {

	@Expose
	String name;

	@Expose
	String canonicalClassName;

	@Expose
	String description;

	@Expose
	String category;

	@Expose
	Map<String, String> propertyDescriptions;

	@Expose
	Map<String, String> propertyDefaultValues;

	@Expose
	List<PortProfile> inputPorts;

	@Expose
	List<PortProfile> outputPorts;

	class PortProfile {

		@Expose
		String name;

		@Expose
		String description;

		@Expose
		Set<String> supportedPipes;

		PortProfile(Port port) {
			this.name = port.getName();
			this.description = port.getDescription();

			this.supportedPipes = port.getSupportedPipeClasses().values().stream().map((Class<?> c) -> c.getName())
					.collect(Collectors.toSet());
		}

	}

	ModuleProfile(Module module) {
		this.name = module.getName();
		// TODO: The way to get a canonical class name should be standardised in Module
		this.canonicalClassName = module.getClass().getName();
		this.description = module.getDescription();
		this.category = module.getCategory();
		this.propertyDescriptions = module.getPropertyDescriptions();
		this.propertyDefaultValues = module.getPropertyDefaultValues();

		this.inputPorts = module.getInputPorts().values().stream().map((Port p) -> new PortProfile(p))
				.collect(Collectors.toList());
		this.outputPorts = module.getOutputPorts().values().stream().map((Port p) -> new PortProfile(p))
				.collect(Collectors.toList());
	}

}
