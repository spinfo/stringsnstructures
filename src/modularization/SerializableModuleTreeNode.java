package modularization;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * A serializable representation of a module tree (node)
 * @author Marcel Boeing
 *
 */
public class SerializableModuleTreeNode {

	private Properties properties;
	private String inputCanonicalClassName = null;
	private String moduleCanonicalClassName;
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
		SerializableModuleTreeNode serializableNode = new SerializableModuleTreeNode(module);
		
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
		this.properties = module.getProperties();
		this.moduleCanonicalClassName = module.getClass().getCanonicalName();
		if (module.getInputCharPipe() != null)
			this.inputCanonicalClassName = module.getInputCharPipe().getClass().getCanonicalName();
		else if (module.getInputBytePipe() != null)
			this.inputCanonicalClassName = module.getInputBytePipe().getClass().getCanonicalName();
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

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * @return the inputCanonicalClassName
	 */
	public String getInputCanonicalClassName() {
		return inputCanonicalClassName;
	}

	/**
	 * @param inputCanonicalClassName the inputCanonicalClassName to set
	 */
	public void setInputCanonicalClassName(String inputCanonicalClassName) {
		this.inputCanonicalClassName = inputCanonicalClassName;
	}

	/**
	 * @return the moduleCanonicalClassName
	 */
	public String getModuleCanonicalClassName() {
		return moduleCanonicalClassName;
	}

	/**
	 * @param moduleCanonicalClassName the moduleCanonicalClassName to set
	 */
	public void setModuleCanonicalClassName(String moduleCanonicalClassName) {
		this.moduleCanonicalClassName = moduleCanonicalClassName;
	}

}
