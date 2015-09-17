package base.workbench;

import javax.swing.SwingConstants;

import modules.Port;

public class ModuleOutputPortLabel extends AbstractModulePortLabel {

	private static final long serialVersionUID = 5980726404998443039L;

	public ModuleOutputPortLabel(Port port) {
		super(port, AbstractModulePortLabel.ICON_OUTPUT, SwingConstants.RIGHT, SwingConstants.LEADING);
	}

}
