package modularization.workbench;

import helpers.PrettyLogRecord;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import modularization.Module;
import modularization.ModuleImpl;
import parallelization.CallbackProcess;
import parallelization.CallbackReceiverImpl;

public class ModuleWorkbenchGui extends CallbackReceiverImpl {

	private JFrame frame;
	private ModuleWorkbenchController controller;
	private JTree moduleTree;

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
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Module Workbench");
		
		JSplitPane topSplitPane = new JSplitPane();
		topSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		
		JSplitPane splitPane = new JSplitPane();
		
		JPanel availableModulesPanel = new JPanel();
		splitPane.setLeftComponent(availableModulesPanel);
		availableModulesPanel.setLayout(new BorderLayout(0, 0));
		
		// Initialize available modules list
		JList<Module> list = new JList<Module>(this.controller.getAvailableModules().toArray(new Module[this.controller.getAvailableModules().size()]));
		list.addListSelectionListener(this.controller);
		availableModulesPanel.add(list);
		
		JPanel moduleTreePanel = new JPanel();
		splitPane.setRightComponent(moduleTreePanel);
		moduleTreePanel.setLayout(new BorderLayout(0, 0));
		
		// Instantiate new JTree with a custom TreeCellRenderer
		this.moduleTree = new JTree(this.controller.getModuleTree().getModuleTree());
		TreeCellRenderer moduleTreeCellRenderer = new ModuleTreeCellRenderer();
		this.moduleTree.setCellRenderer(moduleTreeCellRenderer);
		this.moduleTree.addTreeSelectionListener(this.controller);
		moduleTreePanel.add(this.moduleTree);
		
		JToolBar toolBar = new JToolBar();
		moduleTreePanel.add(toolBar, BorderLayout.SOUTH);
		
		
		// Define toolbar buttons
		
		JButton startNewModuleTreeButton = new JButton();
		startNewModuleTreeButton.setActionCommand(ModuleWorkbenchController.ACTION_STARTNEWMODULETREE);
		startNewModuleTreeButton.addActionListener(this.controller);
		startNewModuleTreeButton.setText("new tree");
		startNewModuleTreeButton.setToolTipText("Clears the current module tree and creates a new one based on the selected module type.");
		
		JButton addModuleButton = new JButton();
		addModuleButton.setActionCommand(ModuleWorkbenchController.ACTION_ADDMODULETOTREE);
		addModuleButton.addActionListener(this.controller);
		addModuleButton.setText("add module");
		addModuleButton.setToolTipText("Adds a module as a child to the one currently selected in the tree.");
		
		JButton runModulesButton = new JButton();
		runModulesButton.setActionCommand(ModuleWorkbenchController.ACTION_RUNMODULES);
		runModulesButton.addActionListener(this.controller);
		runModulesButton.setText("run");
		runModulesButton.setToolTipText("Starts the processing of the module tree.");
		
		JButton editModuleButton = new JButton();
		editModuleButton.setActionCommand(ModuleWorkbenchController.ACTION_EDITMODULE);
		editModuleButton.addActionListener(this.controller);
		editModuleButton.setText("edit");
		editModuleButton.setToolTipText("Lets you edit or review the properties of the module that is currently chosen in the tree.");
		
		toolBar.add(startNewModuleTreeButton);
		toolBar.add(addModuleButton);
		toolBar.add(runModulesButton);
		toolBar.add(editModuleButton);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		
		DefaultListModel<PrettyLogRecord> messageListModel = new DefaultListModel<PrettyLogRecord>();
		JList<PrettyLogRecord> messageList = new JList<PrettyLogRecord>(messageListModel);
		this.controller.getListLoggingHandler().setListModel(messageListModel);
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
	public void receiveCallback(Object processingResult,
			CallbackProcess process, boolean repeat) {
		// Inserting a hook here -- if the process sending the callback is a module, we update the GUI tree display
		if (ModuleImpl.class.isAssignableFrom(process.getClass())){
			this.moduleTree.revalidate();
		}
		super.receiveCallback(processingResult, process, repeat);
	}

	/* (non-Javadoc)
	 * @see parallelization.CallbackReceiverImpl#receiveException(parallelization.CallbackProcess, java.lang.Exception)
	 */
	@Override
	public void receiveException(CallbackProcess process, Exception exception) {
		// Inserting a hook here -- if the process sending the callback is a module, we update the GUI tree display
		if (ModuleImpl.class.isAssignableFrom(process.getClass())){
			this.moduleTree.revalidate();
		}
		super.receiveException(process, exception);
	}
}
