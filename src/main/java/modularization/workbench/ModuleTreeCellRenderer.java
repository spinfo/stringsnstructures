package modularization.workbench;

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import modularization.Module;

public class ModuleTreeCellRenderer  extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -1122661295170702986L;
	public static final ImageIcon ICON_MODULE_QUEUED = new ImageIcon(ModuleTreeCellRenderer.class.getResource("/icons/idle.gif"));
	public static final ImageIcon ICON_MODULE_RUNNING = new ImageIcon(ModuleTreeCellRenderer.class.getResource("/icons/running.gif"));
	public static final ImageIcon ICON_MODULE_SUCCESSFUL = new ImageIcon(ModuleTreeCellRenderer.class.getResource("/icons/clean.png"));
	public static final ImageIcon ICON_MODULE_FAILED = new ImageIcon(ModuleTreeCellRenderer.class.getResource("/icons/error.png"));
	
	public ModuleTreeCellRenderer() {
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		
		try {
			// Determine the node to display
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			// Determine the attached module
			Module module = (Module) node.getUserObject();
			// Set icon depending on module status
			if (module.getStatus()==Module.STATUSCODE_NOTYETRUN)
				this.setIcon(ICON_MODULE_QUEUED);
			else if (module.getStatus()==Module.STATUSCODE_RUNNING){
				this.setIcon(ICON_MODULE_RUNNING);
				ICON_MODULE_RUNNING.setImageObserver(tree);
			}
			else if (module.getStatus()==Module.STATUSCODE_SUCCESS)
				this.setIcon(ICON_MODULE_SUCCESSFUL);
			else if (module.getStatus()==Module.STATUSCODE_FAILURE)
				this.setIcon(ICON_MODULE_FAILED);
			
			
		} catch (Exception e){
			// If there is an error, log it and continue with unmodified icons
			Logger.getLogger("").log(Level.WARNING, "Could not modify the icon of tree node.", e);
		}
		
		return this;
	}

}
