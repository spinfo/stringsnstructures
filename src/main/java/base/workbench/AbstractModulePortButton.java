package base.workbench;

import java.awt.Font;
import java.awt.datatransfer.DataFlavor;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import modules.Port;

public abstract class AbstractModulePortButton extends JButton {

	private static final long serialVersionUID = -1026763812527799258L;
	public static final ImageIcon ICON_INPUT = new ImageIcon(ModuleWorkbenchGui.class.getResource("/icons/input.png"));
	public static final ImageIcon ICON_OUTPUT = new ImageIcon(ModuleWorkbenchGui.class.getResource("/icons/output.png"));
	public static final DataFlavor PORTDATAFLAVOR = new DataFlavor(Port.class, "x-application/moduleport; class=<modules.Port>");


	private Port port;
	
	/**
	 * Constructor.
	 * @param port Module I/O port
	 * @param icon e.g. AbstractModulePortButton.ICON_INPUT
	 * @param alignment e.g. SwingConstants.LEFT
	 * @param textposition e.g. SwingConstants.TRAILING
	 * @param maxlength Maximum length of button text
	 * @param font Font to use
	 */
	public AbstractModulePortButton(Port port, ImageIcon icon, int alignment, int textposition, int maxlength, Font font) {
		super();
		
		// Shorten button text if need be
		String buttonText = port.getName();
		if (buttonText.length()>maxlength)
			buttonText = buttonText.substring(0, maxlength)+"…";
		super.setText(buttonText);
		super.setFont(font);
		super.setToolTipText(port.getName()+": "+port.getDescription());
		this.setHorizontalTextPosition(textposition);
		this.setAlignmentX(alignment);
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
