package base.workbench;

import javax.swing.SwingConstants;

import modules.Port;

public class ModuleInputPortLabel extends AbstractModulePortLabel {

	private static final long serialVersionUID = 1724164621285798807L;

	public ModuleInputPortLabel(Port port) {
		super(port, AbstractModulePortLabel.ICON_INPUT, SwingConstants.RIGHT);
	}

}
