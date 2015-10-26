package base.workbench;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import modules.InputPort;
import modules.Module;
import modules.ModuleNetwork;
import modules.NotFoundException;
import modules.NotSupportedException;
import modules.OccupiedException;
import modules.OutputPort;

import common.PrettyLogRecord;
import common.parallelization.CallbackReceiverImpl;

/**
 * Provides a GUI to create/edit/run module trees.
 * @author Marcel Boeing
 *
 */
public class ModuleWorkbenchGui extends CallbackReceiverImpl implements InternalFrameListener, ActionListener, ListSelectionListener, MouseListener {
	
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
	private Map<Module,ModulePropertyEditor> modulePropertyEditors;

	/**
	 * Launch the application.
	 * @param args Program arguments
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
	 * @param controller Controller instance
	 */
	public ModuleWorkbenchGui(ModuleWorkbenchController controller) {
		this.controller = controller;
		this.moduleFrameMap = new ConcurrentHashMap<Module, ModuleInternalFrame>();
		this.modulePropertyEditors = new ConcurrentHashMap<Module, ModulePropertyEditor>();
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
		// Extend tooltip display time
		ToolTipManager.sharedInstance().setDismissDelay(10000); 
		
		// Scrollpane for the module moduleTemplateList
		JScrollPane availableModulesScrollPane = new JScrollPane();
		availableModulesScrollPane.add(moduleTemplateList);
		availableModulesScrollPane.setViewportView(moduleTemplateList);
		
		availableModulesPanel.add(availableModulesScrollPane);
		
		JPanel moduleNetworkPanel = new JPanel();
		splitPane.setRightComponent(moduleNetworkPanel);
		moduleNetworkPanel.setLayout(new BorderLayout(0, 0));
		
		// Module desktop pane
		this.moduleJDesktopPane = new JDesktopPane();
		
		// Add a desktop manager to handle internal frame movement
		final ModuleDesktopManager moduleDesktopManager = new ModuleDesktopManager();
		this.moduleJDesktopPane.setDesktopManager(moduleDesktopManager);
		
		// Add component adapter to re-adjust frame positioning upon window resize
		this.moduleJDesktopPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent evt) {
				// Determine desktop pane
				JDesktopPane desktopPane = (JDesktopPane) evt.getSource();

				// Loop over all internal frames present
				for (int i = 0; i < desktopPane.getAllFrames().length; i++) {
					try {
						JInternalFrame frame = desktopPane.getAllFrames()[i];
						Rectangle rect = frame.getBounds();

						Dimension d = desktopPane.getSize();
						int x = rect.x;
						int y = rect.y;

						// Determine valid bounds
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
								y = d.height - frame.getHeight(); // flush against the bottom
							}
						}

						// Move frame
						moduleDesktopManager.setBoundsForFrame(frame, x, y, rect.width, rect.height);
					} catch (ArrayIndexOutOfBoundsException e) {
					}
				}
			}
		});
		
		// Add desktop pane to parent panel
		moduleNetworkPanel.add(this.moduleJDesktopPane);
		
		// Add glasspane (for drawing the port connections onto)
		this.moduleConnectionGlasspane = new ModuleNetworkGlasspane(this.moduleJDesktopPane);
		moduleDesktopManager.setGlasspane(this.moduleConnectionGlasspane);
		frame.setGlassPane(this.moduleConnectionGlasspane);
		this.moduleConnectionGlasspane.setVisible(true);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setOrientation(JToolBar.VERTICAL);
		moduleNetworkPanel.add(toolBar, BorderLayout.WEST);
		
		
		// Define toolbar buttons
		
		JButton startNewModuleTreeButton = new JButton();
		startNewModuleTreeButton.setActionCommand(ACTION_CLEARMODULENETWORK);
		startNewModuleTreeButton.setIcon(ICON_NEW_TREE);
		startNewModuleTreeButton.addActionListener(this);
		startNewModuleTreeButton.setToolTipText("Clears the current module tree and creates a new one based on the selected module type.");
		
		JButton addModuleButton = new JButton();
		addModuleButton.setActionCommand(ACTION_ADDMODULETONETWORK);
		addModuleButton.setIcon(ICON_ADD_MODULE);
		addModuleButton.addActionListener(this);
		addModuleButton.setToolTipText("Adds a module as a child to the one currently selected in the tree.");
		
		JButton deleteModuleButton = new JButton();
		deleteModuleButton.setActionCommand(ACTION_DELETEMODULEFROMNETWORK);
		deleteModuleButton.setIcon(ICON_DELETE_MODULE);
		deleteModuleButton.addActionListener(this);
		deleteModuleButton.setEnabled(true);
		deleteModuleButton.setToolTipText("Removes the selected module (and all its children) from the tree.");
		
		JButton runModulesButton = new JButton();
		runModulesButton.setActionCommand(ACTION_RUNMODULES);
		runModulesButton.setIcon(ICON_RUN);
		runModulesButton.addActionListener(this);
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
		editModuleButton.setToolTipText("Lets you edit or review the properties of the module that is currently chosen in the tree.");
		
		JButton saveTreeButton = new JButton();
		saveTreeButton.setActionCommand(ACTION_SAVENETWORK);
		saveTreeButton.setIcon(ICON_SAVE);
		saveTreeButton.addActionListener(this);
		saveTreeButton.setToolTipText("Lets you choose a file to save the module tree to.");
		
		JButton loadTreeButton = new JButton();
		loadTreeButton.setActionCommand(ACTION_LOADNETWORK);
		loadTreeButton.setIcon(ICON_LOAD);
		loadTreeButton.addActionListener(this);
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
		LogListCellRenderer renderer = new LogListCellRenderer();
		JList<PrettyLogRecord> messageList = new JList<PrettyLogRecord>(messageListModel);
		messageList.setCellRenderer(renderer);
		this.controller.getListLoggingHandler().setListModel(messageListModel);
		this.controller.getListLoggingHandler().getAutoScrollLists().add(messageList);
		messageListScrollPane.setViewportView(messageList);
		panel.add(messageListScrollPane, BorderLayout.CENTER);
		
		topSplitPane.setLeftComponent(splitPane);
		topSplitPane.setRightComponent(panel);
		topSplitPane.setResizeWeight(1d);
		frame.getContentPane().add(topSplitPane, BorderLayout.CENTER);
		
	}
	
	/**
	 * Updates the icon of all module frames according to their status.
	 */
	private void updateAllModuleFrameIcons(){
		Iterator<ModuleInternalFrame> moduleFrames = this.moduleFrameMap.values().iterator();
		while (moduleFrames.hasNext()){
			moduleFrames.next().updateStatusIcon();
		}
	}

	/* (non-Javadoc)
	 * @see parallelization.CallbackReceiverImpl#receiveCallback(java.lang.Object, parallelization.CallbackProcess, boolean)
	 */
	@Override
	public void receiveCallback(Thread process, Object processingResult, boolean repeat) {
		// Inserting a hook here -- update the displayed icons
		this.updateAllModuleFrameIcons();
		super.receiveCallback(process, processingResult, repeat);
	}

	/* (non-Javadoc)
	 * @see parallelization.CallbackReceiverImpl#receiveException(parallelization.CallbackProcess, java.lang.Exception)
	 */
	@Override
	public void receiveException(Thread process, Throwable exception) {
		// Inserting a hook here -- update the displayed icons
		this.updateAllModuleFrameIcons();
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
				
				final Map<Module,ModulePropertyEditor> modulePropertyEditorsMap = this.modulePropertyEditors;
						
				// Create new editor dialogue in separate thread
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							ModulePropertyEditor modulePropertyEditor = modulePropertyEditorsMap.get(selectedModuleFrame.getModule());
							if (modulePropertyEditor == null){
								modulePropertyEditor = new ModulePropertyEditor(selectedModuleFrame.getModule());
								modulePropertyEditorsMap.put(selectedModuleFrame.getModule(), modulePropertyEditor);
							}
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
				this.updateAllModuleFrameIcons();
				
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but I wasn't able to run the modules.", e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_STOPMODULES)){
			
			try {
				this.controller.getModuleNetwork().stopModules();
				this.updateAllModuleFrameIcons();
			} catch (Exception e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but I wasn't able to stop the modules.", e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_LOADNETWORK)){
			
			this.actionLoadModuleNetwork();
			this.updateAllModuleFrameIcons();
			
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
	
	private void actionLoadModuleNetwork() {
		try {
			
			// Instantiate a new file chooser
			final JFileChooser fileChooser = new JFileChooser();
			
			// Determine return value
			int returnVal = fileChooser.showOpenDialog(this.frame);
			
			// If the return value indicates approval, load the selected file
			if (returnVal==JFileChooser.APPROVE_OPTION){
				ModuleNetwork loadedModuleNetwork = this.controller.loadModuleNetworkFromFile(fileChooser.getSelectedFile());
				loadedModuleNetwork.addCallbackReceiver(this);
				
				// Loop over loaded modules and add graphical representation for each
				Iterator<Module> modules = loadedModuleNetwork.getModuleList().iterator();
				while (modules.hasNext()){
					
					// Determine next module within the list
					Module module = modules.next();
					
					// Add corresponding module frame
					this.addModuleFrame(module);
				}
				
				// Loop over constructed module frames (in order to draw port connection lines)
				Iterator<ModuleInternalFrame> moduleFrames = this.moduleFrameMap.values().iterator();
				while (moduleFrames.hasNext()){
					
					// Determine next frame in list
					ModuleInternalFrame moduleFrame = moduleFrames.next();
					
					// Loop over input port buttons
					Iterator<ModuleInputPortButton> inputButtons = moduleFrame.getInputButtons().iterator();
					while (inputButtons.hasNext()){
						
						// Determine next input port button in list
						ModuleInputPortButton inputButton = inputButtons.next();
						
						// Determine connected output port
						OutputPort outputPort = (OutputPort)((InputPort)inputButton.getPort()).getConnectedPort();
						
						// Not-so-elegant way to get to the respective output button (if present)
						ModuleInternalFrame connectedModuleFrame = null;
						if (outputPort != null){
							Module connectedModule = outputPort.getParent();
							connectedModuleFrame = this.moduleFrameMap.get(connectedModule);
						}
						
						// Check whether there is a connection to be drawn at all
						if (connectedModuleFrame != null){

							// Loop over the connected module's output buttons
							Iterator<ModuleOutputPortButton> connectedModuleOutputButtons = connectedModuleFrame.getOutputButtons().iterator();
							while (connectedModuleOutputButtons.hasNext()){
								ModuleOutputPortButton connectedModuleOutputButton = connectedModuleOutputButtons.next();
								
								// Check whether the output button's port is the one connected to our input port
								if (connectedModuleOutputButton.getPort().equals(outputPort)){
									// Have lines drawn between linked ports
									this.moduleConnectionGlasspane.link(inputButton, connectedModuleOutputButton);
									break;
								}
							}
							
						}
						
					}
				}
				
				frame.setTitle(WINDOWTITLE+fileChooser.getSelectedFile().getName());
			}
			
		} catch (Exception e1) {
			Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but I wasn't able to load the module tree.", e1);
		}
	}
	
	/**
	 * Returns true is in the process of linking two ports.
	 * @return
	 */
	private boolean isLinking(){
		return this.activeModulePortButton != null;
	}
	
	/**
	 * Sets variables to start the linking process
	 * @param button
	 */
	private void startLinking(AbstractModulePortButton button){
		this.activeModulePortButton = button;
		this.moduleConnectionGlasspane.setActiveLinkingPortButton(button);
	}
	
	/**
	 * Sets variables to stop the linking process
	 */
	private void stopLinking(){
		this.activeModulePortButton = null;
		this.moduleConnectionGlasspane.setActiveLinkingPortButton(null);
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
		if (this.isLinking()) {

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
				// Reset the reference to the active port button
				this.stopLinking();
			}

		} else {
			// No active port label present -- start new linking activity
			this.startLinking((AbstractModulePortButton) e.getSource());
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
		ModuleInternalFrame moduleFrame = new ModuleInternalFrame(module, this, this);

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
			
			// Close correspondent property editor
			ModulePropertyEditor editor = this.modulePropertyEditors.get(module);
			if (editor != null)
				editor.dispose();
			
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

	@Override
	public void mouseClicked(MouseEvent e) {
		
		// Only listen to right-clicks (button 3)
		if (e.getButton() != MouseEvent.BUTTON3)
			return;
		
		// Stop linking process (just in case)
		this.stopLinking();
		
		// Remove port connection(s) when respective port button is right-clicked
		Object source = e.getSource();
		if (!AbstractModulePortButton.class.isAssignableFrom(source.getClass())){
			Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "I just registered a mouse click for an object I do not know how to handle :-/ "+source.getClass().getCanonicalName());
			return;
		} else if (ModuleInputPortButton.class.isAssignableFrom(source.getClass())){
			ModuleInputPortButton button = (ModuleInputPortButton) source;
			this.moduleConnectionGlasspane.unlink(button);
			try {
				this.controller.getModuleNetwork().removeConnection((InputPort) button.getPort());
			} catch (NotFoundException e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but there was an error removing the port connection.", e1);
			}
		} else if (ModuleOutputPortButton.class.isAssignableFrom(source.getClass())){
			ModuleOutputPortButton button = (ModuleOutputPortButton) source;
			this.moduleConnectionGlasspane.unlink(button);
			try {
				this.controller.getModuleNetwork().removeConnection((OutputPort) button.getPort());
			} catch (NotFoundException e1) {
				Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Sorry, but there was an error removing the port connection.", e1);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
