package base.workbench;

import java.awt.Font;

import javax.swing.SwingConstants;

import modules.Port;

public class ModuleOutputPortButton extends AbstractModulePortButton {

	private static final long serialVersionUID = 5980726404998443039L;

	public ModuleOutputPortButton(Port port, int maxlength, Font font) {
		super(port, AbstractModulePortButton.ICON_OUTPUT, SwingConstants.RIGHT, SwingConstants.LEADING, maxlength, font);
	}

}
