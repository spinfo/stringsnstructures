package base.workbench;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import modules.Module;
import modules.ModuleNetwork;
import modules.NotSupportedException;
import modules.OccupiedException;

import common.PrettyLogRecord;
import common.parallelization.CallbackReceiverImpl;

/**
 * Provides a GUI to create/edit/run module trees.
 * @author Marcel Boeing
 *
 */
public class ModuleWorkbenchGui extends CallbackReceiverImpl implements InternalFrameListener, ActionListener, ListSelectionListener {
	
	protected static final String ACTION_CLEARMODULENETWORK = "ACTION_CLEARMODULENETWORK";
	protected static final String ACTION_ADDMODULETONETWORK = "ACTION_ADDMODULETONETWORK";
	protected static final String ACTION_DELETEMODULEFROMNETWORK = "ACTION_DELETEMODULEFROMNETWORK";
	protected static final String ACTION_RUNMODULES = "ACTION_RUNMODULES";
	protected static final String ACTION_STOPMODULES = "ACTION_STOPMODULES";
	protected static final String ACTION_EDITMODULE = "ACTION_EDITMODULE";
	protected static final String ACTION_LOADNETWORK = "ACTION_LOADNETWORK";
	protected static final String ACTION_SAVENETWORK = "ACTION_SAVENETWORK";
	protected static final String ACTION_ACTIVATEPORT = "ACTION_ACTIVATEPORT";

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
	private JDesktopPane moduleJDesktopPane;
	private Map<Module,ModuleInternalFrame> moduleFrameMap;
	private ToolTipJList<Module> moduleTemplateList; // Module template moduleTemplateList
	private Module selectedModuleTemplate = null;
	private ModuleInternalFrame selectedModuleFrame = null;
	private AbstractModulePortButton activeModulePortButton = null;
	private ModuleNetworkGlasspane moduleConnectionGlasspane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ModuleWorkbenchController controller = new ModuleWorkbenchController();
					controller.setModuleNetwork(new ModuleNetwork());
					ModuleWorkbenchGui window = new ModuleWorkbenchGui(controller);
					controller.getModuleNetwork().addCallbackReceiver(window);
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
		this.moduleFrameMap = new ConcurrentHashMap<Module, ModuleInternalFrame>();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 850, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(WINDOWTITLE+WINDOWTITLE_NEWTREESUFFIX);
		
		JSplitPane topSplitPane = new JSplitPane();
		topSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		
		JSplitPane splitPane = new JSplitPane();
		
		JPanel availableModulesPanel = new JPanel();
		splitPane.setLeftComponent(availableModulesPanel);
		
		availableModulesPanel.setLayout(new BorderLayout(0, 0));
		
		// Initialize available modules moduleTemplateList
		moduleTemplateList = new ToolTipJList<Module>(this.controller.getAvailableModules().values().toArray(new Module[this.controller.getAvailableModules().size()]));
		moduleTemplateList.addListSelectionListener(this);
		
		// Scrollpane for the module moduleTemplateList
		JScrollPane availableModulesScrollPane = new JScrollPane();
		availableModulesScrollPane.add(moduleTemplateList);
		availableModulesScrollPane.setViewportView(moduleTemplateList);
		
		availableModulesPanel.add(availableModulesScrollPane);
		
		JPanel moduleTreePanel = new JPanel();
		splitPane.setRightComponent(moduleTreePanel);
		moduleTreePanel.setLayout(new BorderLayout(0, 0));
		
		// Module desktop pane
		this.moduleJDesktopPane = new JDesktopPane();
		ModuleDesktopManager moduleDesktopManager = new ModuleDesktopManager();
		this.moduleJDesktopPane.setDesktopManager(moduleDesktopManager);
		moduleTreePanel.add(this.moduleJDesktopPane);
		
		// Add glasspane (for drawing the port connections onto)
		this.moduleConnectionGlasspane = new ModuleNetworkGlasspane(this.moduleJDesktopPane);
		moduleDesktopManager.setGlasspane(this.moduleConnectionGlasspane);
		//this.moduleConnectionGlasspane.setLayout(new BorderLayout());
		//this.moduleConnectionGlasspane.setOpaque(false);
		frame.setGlassPane(this.moduleConnectionGlasspane);
		this.moduleConnectionGlasspane.setVisible(true);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setOrientation(JToolBar.VERTICAL);
		moduleTreePanel.add(toolBar, BorderLayout.WEST);
		
		
		// Define toolbar buttons
		
		JButton startNewModuleTreeButton = new JButton();
		startNewModuleTreeButton.setActionCommand(ACTION_CLEARMODULENETWORK);
		startNewModuleTreeButton.setIcon(ICON_NEW_TREE);
		startNewModuleTreeButton.addActionListener(this);
		//startNewModuleTreeButton.setText("new tree");
		startNewModuleTreeButton.setToolTipText("Clears the current module tree and creates a new one based on the selected module type.");
		
		JButton addModuleButton = new JButton();
		addModuleButton.setActionCommand(ACTION_ADDMODULETONETWORK);
		addModuleButton.setIcon(ICON_ADD_MODULE);
		addModuleButton.addActionListener(this);
		//addModuleButton.setText("add module");
		addModuleButton.setToolTipText("Adds a module as a child to the one currently selected in the tree.");
		
		JButton deleteModuleButton = new JButton();
		deleteModuleButton.setActionCommand(ACTION_DELETEMODULEFROMNETWORK);
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
		saveTreeButton.setActionCommand(ACTION_SAVENETWORK);
		saveTreeButton.setIcon(ICON_SAVE);
		saveTreeButton.addActionListener(this);
		//saveTreeButton.setText("save tree");
		saveTreeButton.setToolTipText("Lets you choose a file to save the module tree to.");
		
		JButton loadTreeButton = new JButton();
		loadTreeButton.setActionCommand(ACTION_LOADNETWORK);
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
		
		JScrollPane messageListScrollPane = new JScrollPane();
		
		DefaultListModel<PrettyLogRecord> messageListModel = new DefaultListModel<PrettyLogRecord>();
		JList<PrettyLogRecord> messageList = new JList<PrettyLogRecord>(messageListModel);
		this.controller.getListLoggingHandler().setListModel(messageListModel);
		this.controller.getListLoggingHandler().getAutoScrollLists().add(messageList);
		messageListScrollPane.setViewportView(messageList);
		panel.add(messageListScrollPane, BorderLayout.CENTER);
		
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
		/*this.moduleJTree.invalidate();
		this.moduleJTree.validate();
		this.moduleJTree.repaint();
		this.expandAllNodes(this.moduleJTree);*/ // TODO check whether this needs updating
		super.receiveCallback(process, processingResult, repeat);
	}

	/* (non-Javadoc)
	 * @see parallelization.CallbackReceiverImpl#receiveException(parallelization.CallbackProcess, java.lang.Exception)
	 */
	@Override
	public void receiveException(Thread process, Throwable exception) {
		// Inserting a hook here -- update the GUI tree display
		/*this.moduleJTree.invalidate();
		this.moduleJTree.validate();
		this.moduleJTree.repaint();
		this.expandAllNodes(this.moduleJTree);*/ // TODO check whether this needs updating
		super.receiveException(process, exception);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(ACTION_ACTIVATEPORT)){
			this.actionActivatePortButton(e);
		}
		
		else if (e.getActionCommand().equals(ACTION_CLEARMODULENETWORK)){
			// Loop over module frames
			Iterator<ModuleInternalFrame> moduleFrames = this.moduleFrameMap.values().iterator();
			while (moduleFrames.hasNext()) {
				// Close module frames
				try {
					moduleFrames.next().setClosed(true);
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
					Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but I could not close this module frame.", e1);
				}
			}
			
		} else if (e.getActionCommand().equals(ACTION_ADDMODULETONETWORK)){
			this.actionAddModule();
			
		} else if (e.getActionCommand().equals(ACTION_DELETEMODULEFROMNETWORK)){
			
			if (this.selectedModuleFrame == null){
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "I'm afraid I don't know which module to delete -- there is none selected.");
			} else {
				try {
					this.selectedModuleFrame.setClosed(true);
				} catch (PropertyVetoException e1) {
					Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Bugger! Could not close the selected module frame.", e1);
					e1.printStackTrace();
				}
			}
			
		} else if (e.getActionCommand().equals(ACTION_EDITMODULE)){
			
			if (this.selectedModuleFrame == null){
				Logger.getLogger("").log(Level.WARNING, "Please select a module frame first.");
			}
			
			try {
				// Determine module that is currently selected within the module network
				final ModuleInternalFrame selectedModuleFrame = this.selectedModuleFrame;
						
				// Create new editor dialogue in separate thread
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							ModulePropertyEditor modulePropertyEditor = new ModulePropertyEditor(selectedModuleFrame.getModule());
							modulePropertyEditor.setLocation(selectedModuleFrame.getLocationOnScreen().x, selectedModuleFrame.getLocationOnScreen().y);
							modulePropertyEditor.setVisible(true);
							
						} catch (Exception e) {
							Logger.getLogger("").log(Level.WARNING, "Could not open editor for module "+selectedModuleFrame.getModule().getName()+".", e);
						}
					}
				});
				
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Could not display editor dialogue.", e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_RUNMODULES)){
			
			try {
				this.controller.getModuleNetwork().resetModuleIO();
				this.controller.getModuleNetwork().runModules();
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but I wasn't able to run the modules.", e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_STOPMODULES)){
			
			try {
				this.controller.getModuleNetwork().stopModules();
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but I wasn't able to stop the modules.", e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_LOADNETWORK)){
			
			try {
				
				// Instantiate a new file chooser
				final JFileChooser fileChooser = new JFileChooser();
				
				// Determine return value
				int returnVal = fileChooser.showOpenDialog(this.frame);
				
				// If the return value indicates approval, load the selected file
				if (returnVal==JFileChooser.APPROVE_OPTION){
					ModuleNetwork loadedModuleNetwork = this.controller.loadModuleNetworkFromFile(fileChooser.getSelectedFile());
					loadedModuleNetwork.addCallbackReceiver(this);
					frame.setTitle(WINDOWTITLE+fileChooser.getSelectedFile().getName());
				}
				
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but I wasn't able to load the module tree.", e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_SAVENETWORK)){
			
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
	 * Links ports to each other based on which port buttons have been activated.
	 * @param e
	 */
	private void actionActivatePortButton(ActionEvent e) {
		// User clicked on a port button -- either to start a linking or to
		// finish it.

		// Ensure glasspane is visible
		this.moduleConnectionGlasspane.setVisible(true);

		// Determine whether there is port label active (respectively a linking
		// started)
		if (this.activeModulePortButton != null) {

			// Another label is already active -- make connection (if possible)
			AbstractModulePortButton sourceButton = (AbstractModulePortButton) e
					.getSource();

			// Determine which is the input and which the output port button
			try {
				ModuleInputPortButton inputButton = null;
				ModuleOutputPortButton outputButton = null;
				if (ModuleInputPortButton.class.isAssignableFrom(sourceButton
						.getClass())) {
					inputButton = (ModuleInputPortButton) sourceButton;
					outputButton = (ModuleOutputPortButton) this.activeModulePortButton;
				} else {
					outputButton = (ModuleOutputPortButton) sourceButton;
					inputButton = (ModuleInputPortButton) this.activeModulePortButton;
				}

				// Connect the ports to each other
				this.controller.getModuleNetwork().addConnection(
						this.activeModulePortButton.getPort(),
						sourceButton.getPort());
				// Draw connection line on glasspane
				this.moduleConnectionGlasspane.link(inputButton, outputButton);
				// Reset the reference to the active port button
				this.activeModulePortButton = null;
			} catch (NotSupportedException | OccupiedException
					| ClassCastException | IOException e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(
						Level.WARNING,
						"Sorry, but I cannot connect those ports: "
								+ e1.getMessage());
				e1.printStackTrace();
			}

		} else {
			// No active port label present -- start new linking activity
			this.activeModulePortButton = (AbstractModulePortButton) e
					.getSource();
		}
	}

	/**
	 * Adds a module to the network (and a corresponding frame to the GUI) based
	 * on what template is currently selected.
	 */
	private void actionAddModule() {
		try {
			// Check whether there is no template selected
			if (this.selectedModuleTemplate == null)
				throw new Exception("There is no template selected -- please do so beforehand.");
			
			// Instantiate new module from selected template
			Module newModule = this.controller.getNewInstanceOfModule(this.selectedModuleTemplate);
			
			// Add module to network
			if (this.controller.getModuleNetwork().addModule(newModule)) {
				
				// Add corresponding module frame
				this.addModuleFrame(newModule);
				
				// Apply module (default) properties
				newModule.applyProperties();
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "The module could not be added to the network.", e);
		}
	}
	
	/**
	 * Adds an internal frame for the specified module to the GUI.
	 * @param module Module
	 */
	private void addModuleFrame(Module module) {
		// Instantiate module frame
		ModuleInternalFrame moduleFrame = new ModuleInternalFrame(module, this);

		// Add frame listener
		moduleFrame.addInternalFrameListener(this);

		// Add to map
		this.moduleFrameMap.put(module, moduleFrame);

		// Add module frame to workbench gui
		this.moduleJDesktopPane.add(moduleFrame);
		moduleFrame.setVisible(true);

		// Select module frame
		try {
			moduleFrame.setSelected(true);
		} catch (java.beans.PropertyVetoException e1) {
		}
	}
	
	/**
	 * Removes all connections drawn on the glass pane for the specified module frame.
	 * @param moduleFrame Module frame
	 */
	private void removeConnectionsFromGlasspane(ModuleInternalFrame moduleFrame) {
		// Remove connections from glasspane
		Iterator<ModuleInputPortButton> inputButtons = moduleFrame
				.getInputButtons().iterator();
		while (inputButtons.hasNext()) {
			this.moduleConnectionGlasspane.unlink(inputButtons.next());
		}

		Iterator<ModuleOutputPortButton> outputButtons = moduleFrame
				.getOutputButtons().iterator();
		while (outputButtons.hasNext()) {
			this.moduleConnectionGlasspane.unlink(outputButtons.next());
		}
	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		ModuleInternalFrame moduleFrame = (ModuleInternalFrame) e.getInternalFrame();
		try {
			// Check whether there is no module frame selected
			if (moduleFrame == null)
				throw new Exception("There is no module frame selected -- please do so beforehand.");
			
			// Remove connections from glass pane
			this.removeConnectionsFromGlasspane(moduleFrame);
			
			// Determine the module to delete
			Module module = moduleFrame.getModule();
			
			// Remove from frame map
			this.moduleFrameMap.remove(moduleFrame.getModule());
					
			// Remove module from tree
			boolean removed = this.controller.getModuleNetwork().removeModule(module);
			
			// Log message
			if (removed)
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.INFO, "The module '"+module.getName()+"' has been removed from the network.");
			else
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but the selected module could not be removed from the network.");
			
		} catch (Exception e1) {
			Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but due to an error the module could not be removed from the network.", e1);
		}
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		this.selectedModuleFrame = (ModuleInternalFrame) e.getInternalFrame();
		
		this.moduleConnectionGlasspane.setVisible(true);
	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
		this.selectedModuleFrame = null;
		this.moduleConnectionGlasspane.setVisible(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void valueChanged(ListSelectionEvent e) {
		this.selectedModuleTemplate = ((ToolTipJList<Module>)e.getSource()).getSelectedValue();
	}
}
