package modularization.workbench;

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import modularization.Module;

public class ModuleTreeCellRenderer  extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -1122661295170702986L;
	public static final ImageIcon ICON_MODULE_QUEUED = new ImageIcon("resources/icons/clock.png");
	public static final ImageIcon ICON_MODULE_RUNNING = new ImageIcon("resources/icons/run.png");
	public static final ImageIcon ICON_MODULE_SUCCESSFUL = new ImageIcon("resources/icons/clean.png");
	public static final ImageIcon ICON_MODULE_FAILED = new ImageIcon("resources/icons/error.png");

	private JPanel cellTextPanel;
	private JLabel cellTextLabel;
	
	public ModuleTreeCellRenderer() {
		this.cellTextPanel = new JPanel();
		this.cellTextLabel = new JLabel("empty");
		this.cellTextPanel.add(this.cellTextLabel);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		
		Component treeCellRenderer = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		
		try {
			// Determine the node to display
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			// Determine the attached module
			Module module = (Module) node.getUserObject();
			// Set text to display in tree node
			this.cellTextLabel.setText(module.toString());
			// Set icon depending on module status
			if (module.getStatus()==Module.STATUSCODE_NOTYETRUN)
				this.setIcon(ICON_MODULE_QUEUED);
			else if (module.getStatus()==Module.STATUSCODE_RUNNING)
				this.setIcon(ICON_MODULE_RUNNING);
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
