package base.workbench;

import java.awt.Dimension;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * 
 * @see http://www.java2s.com/Code/Java/Swing-JFC/InterestingthingsusingJInternalFramesJDesktopPaneandDesktopManager2.htm
 *
 */
public class ModuleDesktopManager extends DefaultDesktopManager {

	private static final long serialVersionUID = 3915844154537065196L;
	private ModuleNetworkGlasspane glasspane;

	/* (non-Javadoc)
	 * @see javax.swing.DefaultDesktopManager#dragFrame(javax.swing.JComponent, int, int)
	 */
	@Override
	public void dragFrame(JComponent f, int x, int y) {
		JDesktopPane desk = null;
		if (f instanceof JInternalFrame) { // Deal only w/internal frames
			JInternalFrame frame = (JInternalFrame) f;
			desk = frame.getDesktopPane();
			Dimension d = desk.getSize();

			// Nothing all that fancy below, just figuring out how to adjust
			// to keep the frame on the desktop.
			if (x < 0) { // too far left?
				x = 0; // flush against the left side
			} else {
				if (x + frame.getWidth() > d.width) { // too far right?
					x = d.width - frame.getWidth(); // flush against right side
				}
			}
			if (y < 0) { // too high?
				y = 0; // flush against the top
			} else {
				if (y + frame.getHeight() > d.height) { // too low?
					y = d.height - frame.getHeight(); // flush against the
					// bottom
				}
			}
		}

		// Pass along the (possibly cropped) values to the normal drag handler.
		super.dragFrame(f, x, y);
		if (desk!=null)
			desk.repaint();
	}

	/* (non-Javadoc)
	 * @see javax.swing.DefaultDesktopManager#endResizingFrame(javax.swing.JComponent)
	 */
	@Override
	public void endResizingFrame(JComponent f) {
		super.endResizingFrame(f);
		this.glasspane.setVisible(true);
		
	}

	/**
	 * @return the glasspane
	 */
	protected ModuleNetworkGlasspane getGlasspane() {
		return glasspane;
	}

	/**
	 * @param glasspane the glasspane to set
	 */
	protected void setGlasspane(ModuleNetworkGlasspane glasspane) {
		this.glasspane = glasspane;
	}

}
