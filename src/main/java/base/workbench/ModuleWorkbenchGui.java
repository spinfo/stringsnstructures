package base.workbench;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import common.PrettyLogRecord;
import common.parallelization.CallbackReceiverImpl;
import modules.Module;
import modules.ModuleNetwork;

/**
 * Provides a GUI to create/edit/run module trees.
 * @author Marcel Boeing
 *
 */
public class ModuleWorkbenchGui extends CallbackReceiverImpl implements TreeModelListener, ActionListener {
	
	protected static final String ACTION_STARTNEWMODULETREE = "ACTION_STARTNEWMODULETREE";
	protected static final String ACTION_ADDMODULETOTREE = "ACTION_ADDMODULETOTREE";
	protected static final String ACTION_DELETEMODULEFROMTREE = "ACTION_DELETEMODULEFROMTREE";
	protected static final String ACTION_RUNMODULES = "ACTION_RUNMODULES";
	protected static final String ACTION_STOPMODULES = "ACTION_STOPMODULES";
	protected static final String ACTION_EDITMODULE = "ACTION_EDITMODULE";
	protected static final String ACTION_LOADTREE = "ACTION_LOADTREE";
	protected static final String ACTION_SAVETREE = "ACTION_SAVETREE";

	// Icons
	public static final ImageIcon ICON_APP = new ImageIcon(ModuleWorkbenchGui.class.getResource("/icons/app.png"));
	public static final ImageIcon ICON_NEW_TREE = new ImageIcon(ModuleWorkbenchGui.class.getResource("/icons/reload.png"));
	public static final ImageIcon ICON_ADD_MODULE = new ImageIcon(ModuleWorkbenchGui.class.getResource("/icons/add.png"));
	public static final ImageIcon ICON_DELETE_MODULE = new ImageIcon(ModuleWorkbenchGui.class.getResource("/icons/remove.png"));
	public static final ImageIcon ICON_RUN = new ImageIcon(ModuleWorkbenchGui.class.getResource("/icons/forward.png"));
	public static final ImageIcon ICON_STOP = new ImageIcon(ModuleWorkbenchGui.class.getResource("/icons/stop.png"));
	public static final ImageIcon ICON_EDIT_MODULE = new ImageIcon(ModuleWorkbenchGui.class.getResource("/icons/configure.png"));
	public static final ImageIcon ICON_SAVE = new ImageIcon(ModuleWorkbenchGui.class.getResource("/icons/save.png"));
	public static final ImageIcon ICON_LOAD = new ImageIcon(ModuleWorkbenchGui.class.getResource("/icons/open.png"));
	
	public static final String WINDOWTITLE = "Module Workbench - ";
	public static final String WINDOWTITLE_NEWTREESUFFIX = "(new module tree)";
	
	private JFrame frame;
	private ModuleWorkbenchController controller;
	private JTree moduleJTree;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ModuleWorkbenchController controller = new ModuleWorkbenchController();
					ModuleWorkbenchGui window = new ModuleWorkbenchGui(controller);
					controller.getModuleTree().addCallbackReceiver(window);
					window.frame.setIconImage(ICON_APP.getImage());
					window.frame.setVisible(true);
					
					Logger.getGlobal().log(Level.INFO, "Workbench GUI started.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ModuleWorkbenchGui(ModuleWorkbenchController controller) {
		this.controller = controller;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 550, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(WINDOWTITLE+WINDOWTITLE_NEWTREESUFFIX);
		
		JSplitPane topSplitPane = new JSplitPane();
		topSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		
		JSplitPane splitPane = new JSplitPane();
		
		JPanel availableModulesPanel = new JPanel();
		splitPane.setLeftComponent(availableModulesPanel);
		
		availableModulesPanel.setLayout(new BorderLayout(0, 0));
		
		// Initialize available modules list
		ToolTipJList<Module> list = new ToolTipJList<Module>(this.controller.getAvailableModules().toArray(new Module[this.controller.getAvailableModules().size()]));
		list.addListSelectionListener(this.controller);
		
		// Scrollpane for the module list
		JScrollPane availableModulesScrollPane = new JScrollPane();
		availableModulesScrollPane.add(list);
		availableModulesScrollPane.setViewportView(list);
		
		availableModulesPanel.add(availableModulesScrollPane);
		
		JPanel moduleTreePanel = new JPanel();
		splitPane.setRightComponent(moduleTreePanel);
		moduleTreePanel.setLayout(new BorderLayout(0, 0));
		
		// Instantiate new JTree with a custom TreeCellRenderer
		this.moduleJTree = new JTree(this.controller.getModuleTree().getModuleTreeModel());
		TreeCellRenderer moduleTreeCellRenderer = new ModuleTreeCellRenderer();
		this.moduleJTree.setCellRenderer(moduleTreeCellRenderer);
		this.moduleJTree.addTreeSelectionListener(this.controller);
		this.moduleJTree.getModel().addTreeModelListener(this);
		moduleTreePanel.add(this.moduleJTree);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setOrientation(JToolBar.VERTICAL);
		moduleTreePanel.add(toolBar, BorderLayout.WEST);
		
		
		// Define toolbar buttons
		
		JButton startNewModuleTreeButton = new JButton();
		startNewModuleTreeButton.setActionCommand(ACTION_STARTNEWMODULETREE);
		startNewModuleTreeButton.setIcon(ICON_NEW_TREE);
		startNewModuleTreeButton.addActionListener(this);
		//startNewModuleTreeButton.setText("new tree");
		startNewModuleTreeButton.setToolTipText("Clears the current module tree and creates a new one based on the selected module type.");
		
		JButton addModuleButton = new JButton();
		addModuleButton.setActionCommand(ACTION_ADDMODULETOTREE);
		addModuleButton.setIcon(ICON_ADD_MODULE);
		addModuleButton.addActionListener(this);
		//addModuleButton.setText("add module");
		addModuleButton.setToolTipText("Adds a module as a child to the one currently selected in the tree.");
		
		JButton deleteModuleButton = new JButton();
		deleteModuleButton.setActionCommand(ACTION_DELETEMODULEFROMTREE);
		deleteModuleButton.setIcon(ICON_DELETE_MODULE);
		deleteModuleButton.addActionListener(this);
		deleteModuleButton.setEnabled(true);
		//deleteModuleButton.setText("remove module");
		deleteModuleButton.setToolTipText("Removes the selected module (and all its children) from the tree.");
		
		JButton runModulesButton = new JButton();
		runModulesButton.setActionCommand(ACTION_RUNMODULES);
		runModulesButton.setIcon(ICON_RUN);
		runModulesButton.addActionListener(this);
		//runModulesButton.setText("run");
		runModulesButton.setToolTipText("Starts the processing of the module tree.");
		
		JButton stopModulesButton = new JButton();
		stopModulesButton.setActionCommand(ACTION_STOPMODULES);
		stopModulesButton.setIcon(ICON_STOP);
		stopModulesButton.addActionListener(this);
		stopModulesButton.setToolTipText("Stops the processing of the module tree.");
		
		JButton editModuleButton = new JButton();
		editModuleButton.setActionCommand(ACTION_EDITMODULE);
		editModuleButton.setIcon(ICON_EDIT_MODULE);
		editModuleButton.addActionListener(this);
		//editModuleButton.setText("edit");
		editModuleButton.setToolTipText("Lets you edit or review the properties of the module that is currently chosen in the tree.");
		
		JButton saveTreeButton = new JButton();
		saveTreeButton.setActionCommand(ACTION_SAVETREE);
		saveTreeButton.setIcon(ICON_SAVE);
		saveTreeButton.addActionListener(this);
		//saveTreeButton.setText("save tree");
		saveTreeButton.setToolTipText("Lets you choose a file to save the module tree to.");
		
		JButton loadTreeButton = new JButton();
		loadTreeButton.setActionCommand(ACTION_LOADTREE);
		loadTreeButton.setIcon(ICON_LOAD);
		loadTreeButton.addActionListener(this);
		//loadTreeButton.setText("load tree");
		loadTreeButton.setToolTipText("Lets you choose a file to load the module tree from.");
		
		toolBar.add(startNewModuleTreeButton);
		toolBar.add(addModuleButton);
		toolBar.add(deleteModuleButton);
		toolBar.add(runModulesButton);
		toolBar.add(stopModulesButton);
		toolBar.add(editModuleButton);
		toolBar.add(saveTreeButton);
		toolBar.add(loadTreeButton);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		
		DefaultListModel<PrettyLogRecord> messageListModel = new DefaultListModel<PrettyLogRecord>();
		JList<PrettyLogRecord> messageList = new JList<PrettyLogRecord>(messageListModel);
		this.controller.getListLoggingHandler().setListModel(messageListModel);
		this.controller.getListLoggingHandler().getAutoScrollLists().add(messageList);
		scrollPane.setViewportView(messageList);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		topSplitPane.setLeftComponent(splitPane);
		topSplitPane.setRightComponent(panel);
		frame.getContentPane().add(topSplitPane, BorderLayout.CENTER);
		
	}

	/* (non-Javadoc)
	 * @see parallelization.CallbackReceiverImpl#receiveCallback(java.lang.Object, parallelization.CallbackProcess, boolean)
	 */
	@Override
	public void receiveCallback(Thread process, Object processingResult, boolean repeat) {
		// Inserting a hook here -- update the GUI tree display
		this.moduleJTree.invalidate();
		this.moduleJTree.validate();
		this.moduleJTree.repaint();
		this.expandAllNodes(this.moduleJTree);
	}

	/* (non-Javadoc)
	 * @see parallelization.CallbackReceiverImpl#receiveException(parallelization.CallbackProcess, java.lang.Exception)
	 */
	@Override
	public void receiveException(Thread process, Throwable exception) {
		// Inserting a hook here -- update the GUI tree display
		this.moduleJTree.invalidate();
		this.moduleJTree.validate();
		this.moduleJTree.repaint();
		this.expandAllNodes(this.moduleJTree);
	}

	@Override
	public void treeNodesChanged(TreeModelEvent e) {
	}

	@Override
	public void treeNodesInserted(TreeModelEvent e) {
		Object[] children = e.getChildren();
		if (children.length>0 && DefaultMutableTreeNode.class.isAssignableFrom(children[0].getClass())){
			TreePath newNodePath = new TreePath(((DefaultMutableTreeNode)children[0]).getPath());
			this.moduleJTree.setSelectionPath(newNodePath);
		}
	}

	@Override
	public void treeNodesRemoved(TreeModelEvent e) {
	}

	@Override
	public void treeStructureChanged(TreeModelEvent e) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(ACTION_STARTNEWMODULETREE)){
			
			// New module to create
			Module rootModule;
			try {
				if (this.controller.getSelectedModule() == null)
					throw new Exception("Please do select a module from the lefthand list first.");
				rootModule = this.controller.getNewInstanceOfSelectedModule(null);
				// Start new module tree and add this class as callback receiver to it
				ModuleNetwork moduleNetwork = this.controller.startNewModuleTree(rootModule);
				moduleNetwork.addCallbackReceiver(this);
				frame.setTitle(WINDOWTITLE+WINDOWTITLE_NEWTREESUFFIX);
				
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Could not create a new module tree.", e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_ADDMODULETOTREE)){
			
			try {
				// Determine module that is currently selected within the module tree
				Module parentModule = (Module) this.controller.getSelectedTreeNode().getUserObject();
				Module newModule = this.controller.getNewInstanceOfSelectedModule(this.controller.getModuleTree());
						
				// Add new module to selected tree node
				this.controller.getModuleTree().addConnection(newModule, parentModule);
				
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "The selected module could not be added to the tree.", e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_DELETEMODULEFROMTREE)){
			
			try {
				// Determine node that is currently selected within the module tree
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.controller.getSelectedTreeNode();
						
				// Remove module from tree
				boolean removed = this.controller.getModuleTree().removeModule(node);
				
				// Log message
				if (removed)
					Logger.getLogger(this.getClass().getCanonicalName()).log(Level.INFO, "The selected module has been removed from the tree.");
				else
					Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "The selected module could not be removed from the tree.");
				
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "The selected module could not be removed from the tree.", e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_EDITMODULE)){
			
			try {
				// Determine module that is currently selected within the module tree
				final Module selectedModule = (Module) this.controller.getSelectedTreeNode().getUserObject();
						
				// Create new editor dialogue in separate thread
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							ModulePropertyEditor modulePropertyEditor = new ModulePropertyEditor(selectedModule);
							modulePropertyEditor.setVisible(true);
							
							//Logger.getGlobal().log(Level.INFO, "Opened editor for "+selectedModule.getName()+".");
						} catch (Exception e) {
							Logger.getLogger("").log(Level.WARNING, "Could not open editor for module "+selectedModule.getName()+".", e);
						}
					}
				});
				
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Could not display editor dialogue.", e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_RUNMODULES)){
			
			try {
				this.controller.getModuleTree().runModules();
				this.moduleJTree.revalidate();
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but I wasn't able to run the modules.", e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_STOPMODULES)){
			
			try {
				this.controller.getModuleTree().stopModules();
				this.moduleJTree.revalidate();
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but I wasn't able to stop the modules.", e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_LOADTREE)){
			
			try {
				
				// Instantiate a new file chooser
				final JFileChooser fileChooser = new JFileChooser();
				
				// Determine return value
				int returnVal = fileChooser.showOpenDialog(this.frame);
				
				// If the return value indicates approval, load the selected file
				if (returnVal==JFileChooser.APPROVE_OPTION){
					ModuleNetwork loadedModuleTree = this.controller.loadModuleTreeFromFile(fileChooser.getSelectedFile());
					loadedModuleTree.getModuleTreeModel().addTreeModelListener(this);
					loadedModuleTree.addCallbackReceiver(this);
					this.moduleJTree.setModel(loadedModuleTree.getModuleTreeModel());
					this.moduleJTree.revalidate();
					this.expandAllNodes(this.moduleJTree);
					frame.setTitle(WINDOWTITLE+fileChooser.getSelectedFile().getName());
				}
				
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but I wasn't able to load the module tree.", e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_SAVETREE)){
			
			try {
				// Instantiate a new file chooser
				final JFileChooser fileChooser = new JFileChooser();
				
				// Determine return value
				int returnVal = fileChooser.showSaveDialog(this.frame);
				
				// If the return value indicates approval, save module tree to the selected file
				if (returnVal==JFileChooser.APPROVE_OPTION){
					this.controller.saveModuleTreeToFile(fileChooser.getSelectedFile());
					frame.setTitle(WINDOWTITLE+fileChooser.getSelectedFile().getName());
				}
				
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but I wasn't able to save the module tree.", e1);
			}
			
		} else {
			Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but this command is unknown to me: "+e.getActionCommand());
		}
	}
	
	/**
	 * Expands all nodes on the given JTree
	 * @param tree
	 */
	private void expandAllNodes(JTree tree) {
		int row = 0;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}
	}
	
	/**
	 * Collapses all nodes on the given JTree
	 * @param tree
	 */
	/*private void collapseAllNodes(JTree tree) {
		int row = tree.getRowCount() - 1;
	    while (row >= 0) {
	      tree.collapseRow(row);
	      row--;
	    }
	}*/
}
