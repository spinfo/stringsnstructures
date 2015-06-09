package modularization.workbench;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import modularization.Module;

/**
 * A serializable representation of a module tree (node)
 * @author Marcel Boeing
 *
 */
public class SerializableModuleTreeNode {

	private Module module;
	private List<SerializableModuleTreeNode> children = new ArrayList<SerializableModuleTreeNode>();
	
	/**
	 * Returns the root node of a serializable representation of the given module tree model.
	 * @param treeModel DefaultTreeModel used with a module tree
	 * @return Root node
	 * @throws Exception 
	 */
	public static SerializableModuleTreeNode convertModuleTreeModel(DefaultTreeModel treeModel) throws Exception{
		
		// Determine the root node & module of the given tree model
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
		
		// Convert root node (& its children, recursively)
		SerializableModuleTreeNode serializableRootNode = convertModuleTreeModel(rootNode);
		
		// Return the serializable root node
		return serializableRootNode;
	}
	
	/**
	 * Returns the serializable representation of the given module tree node.
	 * @param node Module tree node
	 * @return Serializable module tree node
	 * @throws Exception 
	 */
	private static SerializableModuleTreeNode convertModuleTreeModel(DefaultMutableTreeNode node) throws Exception {
		
		// Determine the module of the given node
		Module module = (Module) node.getUserObject();
		
		// Instantiate serializable node and attach the root module to it
		SerializableModuleTreeNode serializableNode = new SerializableModuleTreeNode();
		serializableNode.setModule(module);
		
		// Loop over child nodes
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> children = node.children();
		while (children.hasMoreElements()){
			// Convert & add each child to the current serializable node
			serializableNode.getChildren().add(convertModuleTreeModel(children.nextElement()));
		}
		
		// Return the serializable node
		return serializableNode;
	}
	
	public SerializableModuleTreeNode() {
	}
	
	public SerializableModuleTreeNode(Module module) {
		this.module = module;
	}

	/**
	 * @return the module
	 */
	public Module getModule() {
		return module;
	}

	/**
	 * @param module the module to set
	 */
	public void setModule(Module module) {
		this.module = module;
	}

	/**
	 * @return the children
	 */
	public List<SerializableModuleTreeNode> getChildren() {
		return children;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(List<SerializableModuleTreeNode> children) {
		this.children = children;
	}
	
	

}
