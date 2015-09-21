package base.workbench;

import javax.swing.SwingConstants;

import modules.Port;

public class ModuleInputPortButton extends AbstractModulePortButton {

	private static final long serialVersionUID = 1724164621285798807L;

	public ModuleInputPortButton(Port port) {
		super(port, AbstractModulePortButton.ICON_INPUT, SwingConstants.LEFT, SwingConstants.TRAILING);
	}

}
