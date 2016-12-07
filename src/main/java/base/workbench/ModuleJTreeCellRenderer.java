package base.workbench;

import java.awt.Color;
import java.awt.Component;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import modules.Module;

/**
 * Lets a JTree containing modules display the selected item's description as tooltip.
 * @author Marcel Boeing
 *
 */
public class ModuleJTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 8321180041458513120L;
	
	// Filter string
	private String filterString;
	
	public String getFilterString() {
		return filterString;
	}

	public void setFilterString(String filterString) {
		if (filterString != null)
			this.filterString = filterString.toLowerCase();
		else
			this.filterString = null;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		
		// Call super method
		Component c = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus);
		
		// Cast value to DefaultMutableTreeNode
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        
		// Determine whether a node carrying a module is selected
		if (Module.class.isAssignableFrom(node.getUserObject().getClass())){
			Module module = (Module)node.getUserObject();
			// Set tooltip to selected module's description
			setToolTipText("<html><div style='background-color:FFFFFF;' width=\"500\"><h1 style='font-size:12px;'>"+module.getName()+"</h1><p>"+module.getDescription()+"</p></div></html>");
			// Colorise node according to it matching the filter string
	        if (moduleNodeMatchesFilter(node)) {
	            c.setForeground(Color.BLACK);
	            return c;
	        }
	        else {
	            c.setForeground(Color.GRAY);
	            return c;
	        }
		} else{
			setToolTipText(null);
			// Colorise node according to it matching the filter string
	        if (categoryNodeMatchesFilter(node)) {
	            c.setForeground(Color.BLACK);
	            return c;
	        }
	        else {
	            c.setForeground(Color.GRAY);
	            return c;
	        }
		}
	}
	
	private boolean categoryNodeMatchesFilter(DefaultMutableTreeNode node) 
	{
		if (this.filterString == null || this.filterString.isEmpty())
			return true;
		
	    if ( node.getUserObject().toString().toLowerCase().contains(this.filterString)) {
            return true;
        }
		
		@SuppressWarnings("unchecked")
		Enumeration<TreeNode> childrenEnum = node.children();
	    while ( childrenEnum.hasMoreElements() ) {
	    	
	    	DefaultMutableTreeNode child = (DefaultMutableTreeNode)childrenEnum.nextElement();
	        
	    	// Determine whether a node carrying a module is selected (and if so, search in module name & description)
			if (child.getUserObject() != null
					&& Module.class.isAssignableFrom(child.getUserObject().getClass()) && (((Module)child.getUserObject()).getName().toLowerCase().contains(filterString) || ((Module)child.getUserObject()).getDescription().toLowerCase().contains(filterString))){
				return true;
			}
	    }
	    
	    return false;
	}
	
	private boolean moduleNodeMatchesFilter(DefaultMutableTreeNode node) {
		
		if (this.filterString == null || this.filterString.isEmpty())
			return true;
		if (node.getUserObject() != null && Module.class.isAssignableFrom(node.getUserObject().getClass())
				&& (((Module) node.getUserObject()).getName().toLowerCase().contains(filterString)
						|| ((Module) node.getUserObject()).getDescription().toLowerCase().contains(filterString))) {
			return true;
		}

		return false;
	}

}
