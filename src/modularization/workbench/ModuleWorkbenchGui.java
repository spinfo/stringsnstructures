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

import modularization.Module;

public class ModuleWorkbenchGui {

	private JFrame frame;
	private ModuleWorkbenchController controller;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ModuleWorkbenchController controller = new ModuleWorkbenchController();
					ModuleWorkbenchGui window = new ModuleWorkbenchGui(controller);
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
		
		JTree tree = new JTree(this.controller.getModuleTree().getModuleTree());
		tree.addTreeSelectionListener(this.controller);
		moduleTreePanel.add(tree);
		
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
}
