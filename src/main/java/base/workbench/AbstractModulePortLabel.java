package base.workbench;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import modules.Port;

public abstract class AbstractModulePortLabel extends JLabel {

	private static final long serialVersionUID = -1026763812527799258L;
	public static final ImageIcon ICON_INPUT = new ImageIcon(ModuleWorkbenchGui.class.getResource("/icons/input.png"));
	public static final ImageIcon ICON_OUTPUT = new ImageIcon(ModuleWorkbenchGui.class.getResource("/icons/output.png"));


	private Port port;

	/**
	 * Constructor.
	 * @param port Module I/O port
	 * @param icon e.g. AbstractModulePortLabel.ICON_INPUT
	 * @param alignment e.g. SwingConstants.LEFT
	 */
	public AbstractModulePortLabel(Port port, ImageIcon icon, int alignment) {
		super(port.getName());
		this.setHorizontalAlignment(alignment);
		this.setIcon(icon);
		this.port = port;
	}

	/**
	 * @return the port
	 */
	public Port getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(Port port) {
		this.port = port;
	}
	
	
}
