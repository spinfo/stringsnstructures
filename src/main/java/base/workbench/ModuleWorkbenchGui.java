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
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
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
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import common.PrettyLogRecord;
import common.parallelization.CallbackReceiverImpl;
import modules.InputPort;
import modules.Module;
import modules.ModuleNetwork;
import modules.NotFoundException;
import modules.NotSupportedException;
import modules.OccupiedException;
import modules.OutputPort;
import javax.swing.JTextField;

/**
 * Provides a GUI to create/edit/run module trees.
 * @author Marcel Boeing
 *
 */

public class ModuleWorkbenchGui extends CallbackReceiverImpl implements InternalFrameListener, ActionListener, TreeSelectionListener, MouseListener, DocumentListener {
	
	private static final Logger LOGGER = Logger.getLogger(ModuleWorkbenchGui.class.getCanonicalName());
	
	// Keywords used to identify actions
	protected static final String ACTION_CLEARMODULENETWORK = "ACTION_CLEARMODULENETWORK";
	protected static final String ACTION_ADDMODULETONETWORK = "ACTION_ADDMODULETONETWORK";
	protected static final String ACTION_DELETEMODULEFROMNETWORK = "ACTION_DELETEMODULEFROMNETWORK";
	protected static final String ACTION_RUNMODULES = "ACTION_RUNMODULES";
	protected static final String ACTION_STOPMODULES = "ACTION_STOPMODULES";
	protected static final String ACTION_EDITMODULE = "ACTION_EDITMODULE";
	protected static final String ACTION_LOADNETWORK = "ACTION_LOADNETWORK";
	protected static final String ACTION_SAVENETWORK = "ACTION_SAVENETWORK";
	protected static final String ACTION_ACTIVATEPORT = "ACTION_ACTIVATEPORT";
	protected static final String ACTION_CLEARSEARCH = "ACTION_CLEARSEARCH";

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
	public static final ImageIcon ICON_CLEARLEFT = new ImageIcon(ModuleWorkbenchGui.class.getResource("/icons/clear_left.png"));
	
	// Standard text snippets
	public static final String WINDOWTITLE = "Module Workbench - ";
	public static final String WINDOWTITLE_NEWTREESUFFIX = "(new module network)";
	public static final String FILENAMESUFFIX = "exp";
	public static final String FILENAMESUFFIX_TITLE = "Experiments";
	public static final String MODULES_TITLE = "Modules";	
	
	/*
	 *  Local variables
	 */
	private JFrame mainGuiFrame; // Main GUI
	private ModuleWorkbenchController controller; // Controller handling the underlying module network
	private JDesktopPane moduleJDesktopPane; // Desktop pane containing the modules' frames displayed within the editor
	private Map<Module,ModuleInternalFrame> moduleFrameMap; // Map referencing which module belongs to which frame
	private JTree moduleTemplateTree; // List of available module templates
	private Module selectedModuleTemplate = null; // Template that is currently selected
	private ModuleInternalFrame selectedModuleFrame = null; // Module frame that is currently selected
	private AbstractModulePortButton activeModulePortButton = null; // Used to keep track of which module port button was pressed last 
	private ModuleNetworkGlasspane moduleConnectionGlasspane; // Glasspane to draw module port connections onto
	private Map<Module,ModulePropertyEditor> modulePropertyEditors; // Map to associate each module with its property editor dialogue
	private File lastChosenFile = null; // Used to keep track of where to store / load from within the file system
	private JTextField txtSearchmodulesfield;

	/**
	 * Launch the application.
	 * @param args Program arguments
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// Set look and feel to crossplatform
			        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			        
			        // Instantiate controller that handles all modulenetwork actions
					ModuleWorkbenchController controller = new ModuleWorkbenchController();
					controller.setModuleNetwork(new ModuleNetwork());
					
					// Initialise GUI
					ModuleWorkbenchGui window = new ModuleWorkbenchGui(controller);
					controller.getModuleNetwork().addCallbackReceiver(window);
					window.mainGuiFrame.setIconImage(ICON_APP.getImage());
					window.mainGuiFrame.setVisible(true);
					
					Logger.getGlobal().log(Level.INFO, UserMessages.WORKBENCH_GUI_STARTED);
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
	 * Initialises the contents of the frame.
	 */
	private void initialize() {
		
		// Main GUI frame
		mainGuiFrame = new JFrame();
		mainGuiFrame.setBounds(100, 100, 850, 600);
		mainGuiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainGuiFrame.setTitle(WINDOWTITLE+WINDOWTITLE_NEWTREESUFFIX);
		
		// Splitpane that divides the GUI main area from the bottom message log display
		JSplitPane topSplitPane = new JSplitPane();
		topSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		
		// Splitpane that divides the available modules list from the module network editor
		JSplitPane splitPane = new JSplitPane();
		
		// Panel for the list of available modules
		JPanel availableModulesPanel = new JPanel();
		splitPane.setLeftComponent(availableModulesPanel);
		availableModulesPanel.setLayout(new BorderLayout(0, 0));
		
		// Initialise available modules tree
		//moduleTemplateList = new ToolTipJList<Module>(this.controller.getAvailableModules().values().toArray(new Module[this.controller.getAvailableModules().size()]));
		DefaultMutableTreeNode moduleTemplateTreeRootNode = new DefaultMutableTreeNode(MODULES_TITLE);
		DefaultTreeModel moduleTemplateTreeModel = new DefaultTreeModel(moduleTemplateTreeRootNode);
		moduleTemplateTree = new JTree(moduleTemplateTreeModel);
		moduleTemplateTree.setRowHeight(0);
		moduleTemplateTree.setCellRenderer(new ModuleJTreeCellRenderer());
		ToolTipManager.sharedInstance().registerComponent(moduleTemplateTree);
		moduleTemplateTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		// Map to keep track of created category label nodes (we only allow one level of categories within the module tree)
		Map<String,DefaultMutableTreeNode> categoryLabelMap = new TreeMap<String,DefaultMutableTreeNode>();
		
		// Loop over the controller's available modules list to determine existing categories
		Iterator<Module> moduleTemplateIterator = this.controller.getAvailableModules().values().iterator();
		while(moduleTemplateIterator.hasNext()){
			
			// Determine next module and its category
			Module module = moduleTemplateIterator.next();
			String categoryName = module.getCategory();
			
			// Create category node to attach the module to
			if (categoryName != null && !categoryLabelMap.containsKey(categoryName)) {
				DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(categoryName);
				categoryLabelMap.put(categoryName, categoryNode);
			}
		}
		
		// Create tree nodes for categories
		Iterator<String> categoryLabels = categoryLabelMap.keySet().iterator();
		while (categoryLabels.hasNext()){
			String categoryLabel = categoryLabels.next();
			moduleTemplateTreeModel.insertNodeInto(categoryLabelMap.get(categoryLabel), moduleTemplateTreeRootNode, moduleTemplateTreeRootNode.getChildCount());
		}
		
		// Loop over the controller's available modules list a second time to insert modules into our tree in an orderly fashion
		moduleTemplateIterator = this.controller.getAvailableModules().values().iterator();
		while(moduleTemplateIterator.hasNext()){
			
			// Determine next module and its category
			Module module = moduleTemplateIterator.next();
			String categoryName = module.getCategory();
			
			// Attach module to category node
			moduleTemplateTreeModel.insertNodeInto(new DefaultMutableTreeNode(module), categoryLabelMap.get(categoryName), categoryLabelMap.get(categoryName).getChildCount());
		}
		
		// Expand the first level of the module template tree
		moduleTemplateTree.expandPath(new TreePath(moduleTemplateTreeRootNode.getPath()));
		
		// Add selection listener
		moduleTemplateTree.addTreeSelectionListener(this);
		
		// Extend tooltip display time
		ToolTipManager.sharedInstance().setDismissDelay(20000); 
		
		// Scrollpane for the available modules list
		JScrollPane availableModulesScrollPane = new JScrollPane();
		availableModulesScrollPane.add(moduleTemplateTree);
		availableModulesScrollPane.setViewportView(moduleTemplateTree);
		availableModulesPanel.add(availableModulesScrollPane);
		
		JPanel panel_1 = new JPanel();
		availableModulesPanel.add(panel_1, BorderLayout.SOUTH);
		
		// Search field
		txtSearchmodulesfield = new JTextField();
		txtSearchmodulesfield.getDocument().addDocumentListener(this);
		panel_1.add(txtSearchmodulesfield);
		txtSearchmodulesfield.setColumns(10);
		
		// Clear search field button
		JButton btnClearsearchbutton = new JButton();
		btnClearsearchbutton.setActionCommand(ACTION_CLEARSEARCH);
		btnClearsearchbutton.addActionListener(this);
		btnClearsearchbutton.setToolTipText(UserMessages.CLEAR_SEARCH_FIELD);
		btnClearsearchbutton.setIcon(ICON_CLEARLEFT);
		panel_1.add(btnClearsearchbutton);
		
		// Panel for the module network editor
		JPanel moduleNetworkPanel = new JPanel();
		splitPane.setRightComponent(moduleNetworkPanel);
		moduleNetworkPanel.setLayout(new BorderLayout(0, 0));
		
		// Module network editor desktop pane
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
		mainGuiFrame.setGlassPane(this.moduleConnectionGlasspane);
		this.moduleConnectionGlasspane.setVisible(true);
		
		/*
		 * Editor toolbar
		 */
		
		// Add a toolbar for module network editor actions
		JToolBar toolBar = new JToolBar();
		toolBar.setOrientation(JToolBar.VERTICAL);
		moduleNetworkPanel.add(toolBar, BorderLayout.WEST);
		
		
		// Define toolbar buttons
		
		JButton startNewModuleTreeButton = new JButton();
		startNewModuleTreeButton.setActionCommand(ACTION_CLEARMODULENETWORK);
		startNewModuleTreeButton.setIcon(ICON_NEW_TREE);
		startNewModuleTreeButton.addActionListener(this);
		startNewModuleTreeButton.setToolTipText(UserMessages.CLEAR_MODULE_TREE);
		
		JButton addModuleButton = new JButton();
		addModuleButton.setActionCommand(ACTION_ADDMODULETONETWORK);
		addModuleButton.setIcon(ICON_ADD_MODULE);
		addModuleButton.addActionListener(this);
		addModuleButton.setToolTipText(UserMessages.ADD_MODULE);
		
		JButton deleteModuleButton = new JButton();
		deleteModuleButton.setActionCommand(ACTION_DELETEMODULEFROMNETWORK);
		deleteModuleButton.setIcon(ICON_DELETE_MODULE);
		deleteModuleButton.addActionListener(this);
		deleteModuleButton.setEnabled(true);
		deleteModuleButton.setToolTipText(UserMessages.REMOVE_MODULE);
		
		JButton runModulesButton = new JButton();
		runModulesButton.setActionCommand(ACTION_RUNMODULES);
		runModulesButton.setIcon(ICON_RUN);
		runModulesButton.addActionListener(this);
		runModulesButton.setToolTipText(UserMessages.RUN_MODULES);
		
		JButton stopModulesButton = new JButton();
		stopModulesButton.setActionCommand(ACTION_STOPMODULES);
		stopModulesButton.setIcon(ICON_STOP);
		stopModulesButton.addActionListener(this);
		stopModulesButton.setToolTipText(UserMessages.STOP_MODULES);
		
		JButton editModuleButton = new JButton();
		editModuleButton.setActionCommand(ACTION_EDITMODULE);
		editModuleButton.setIcon(ICON_EDIT_MODULE);
		editModuleButton.addActionListener(this);
		editModuleButton.setToolTipText(UserMessages.EDIT_MODULE_PROPERTIES);
		
		JButton saveTreeButton = new JButton();
		saveTreeButton.setActionCommand(ACTION_SAVENETWORK);
		saveTreeButton.setIcon(ICON_SAVE);
		saveTreeButton.addActionListener(this);
		saveTreeButton.setToolTipText(UserMessages.SAVE_TO_FILE);
		
		JButton loadTreeButton = new JButton();
		loadTreeButton.setActionCommand(ACTION_LOADNETWORK);
		loadTreeButton.setIcon(ICON_LOAD);
		loadTreeButton.addActionListener(this);
		loadTreeButton.setToolTipText(UserMessages.LOAD_FROM_FILE);
		
		toolBar.add(startNewModuleTreeButton);
		toolBar.add(addModuleButton);
		toolBar.add(deleteModuleButton);
		toolBar.add(runModulesButton);
		toolBar.add(stopModulesButton);
		toolBar.add(editModuleButton);
		toolBar.add(saveTreeButton);
		toolBar.add(loadTreeButton);
		
		/*
		 * Message log
		 */
		
		// Panel for message log display at the bottom
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		
		// Add a scrollpane
		JScrollPane messageListScrollPane = new JScrollPane();
		
		// Message log entries are of a special class and have got their own
		// renderer in order to display their contents more pleasing to the eye
		DefaultListModel<PrettyLogRecord> messageListModel = new DefaultListModel<PrettyLogRecord>();
		LogListCellRenderer renderer = new LogListCellRenderer();
		JList<PrettyLogRecord> messageList = new JList<PrettyLogRecord>(messageListModel);
		messageList.setCellRenderer(renderer);
		
		// Attach the message log list to the controller's logging handler
		this.controller.getListLoggingHandler().setListModel(messageListModel);
		this.controller.getListLoggingHandler().getAutoScrollLists().add(messageList);
		
		// Finalise display
		messageListScrollPane.setViewportView(messageList);
		panel.add(messageListScrollPane, BorderLayout.CENTER);
		topSplitPane.setLeftComponent(splitPane);
		topSplitPane.setRightComponent(panel);
		topSplitPane.setResizeWeight(1d);
		mainGuiFrame.getContentPane().add(topSplitPane, BorderLayout.CENTER);
		
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
					LOGGER.log(Level.WARNING, UserMessages.COULD_NOT_CLOSE_MODULE_FRAME, e1);
				}
			}
			
		} else if (e.getActionCommand().equals(ACTION_ADDMODULETONETWORK)){
			this.actionAddModule();
			
		} else if (e.getActionCommand().equals(ACTION_CLEARSEARCH)){
			this.txtSearchmodulesfield.setText("");
			
		} else if (e.getActionCommand().equals(ACTION_DELETEMODULEFROMNETWORK)){
			
			if (this.selectedModuleFrame == null){
				LOGGER.log(Level.WARNING, UserMessages.UNKNOWN_MODULE_TO_DELETE);
			} else {
				try {
					this.selectedModuleFrame.setClosed(true);
				} catch (PropertyVetoException e1) {
					LOGGER.log(Level.WARNING, UserMessages.COULD_NOT_CLOSE_MODULE_FRAME, e1);
					e1.printStackTrace();
				}
			}
			
		} else if (e.getActionCommand().equals(ACTION_EDITMODULE)){
			
			if (this.selectedModuleFrame == null){
				LOGGER.log(Level.WARNING, UserMessages.UNKNOWN_MODULE_TO_EDIT);
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
							LOGGER.log(Level.WARNING, UserMessages.unableToOpenEditor(selectedModuleFrame.getModule().getName()), e);
						}
					}
				});
				
			} catch (Exception e1) {
				LOGGER.log(Level.WARNING, UserMessages.UNABLE_TO_DISPLAY_EDITOR, e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_RUNMODULES)){
			
			try {
				// Reset the I/O ports of all modules
				this.controller.getModuleNetwork().resetModuleIO();
				// Run modules
				this.controller.getModuleNetwork().runModules();
				// Display the new status for all frames
				this.updateAllModuleFrameIcons();
				
			} catch (Exception e1) {
				LOGGER.log(Level.WARNING, UserMessages.UNABLE_TO_RUN_MODULES, e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_STOPMODULES)){
			
			try {
				this.controller.getModuleNetwork().stopModules();
				this.updateAllModuleFrameIcons();
			} catch (Exception e1) {
				LOGGER.log(Level.WARNING, UserMessages.UNABLE_TO_STOP_MODULES, e1);
			}
			
		} else if (e.getActionCommand().equals(ACTION_LOADNETWORK)){
			
			this.actionLoadModuleNetwork();
			this.updateAllModuleFrameIcons();
			
		} else if (e.getActionCommand().equals(ACTION_SAVENETWORK)){
			
			try {
				// Instantiate a new file chooser
				final JFileChooser fileChooser = new JFileChooser();
				
				// Set file filter
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
				        FILENAMESUFFIX_TITLE, FILENAMESUFFIX);
				fileChooser.setFileFilter(filter);
				
				// Set last chosen directory
				if (this.lastChosenFile != null)
					fileChooser.setSelectedFile(this.lastChosenFile);
				
				// Determine return value
				int returnVal = fileChooser.showSaveDialog(this.mainGuiFrame);
				
				// If the return value indicates approval, save module tree to the selected file
				if (returnVal==JFileChooser.APPROVE_OPTION){
					File selectedFile = fileChooser.getSelectedFile();
					if (!selectedFile.getName().endsWith(FILENAMESUFFIX) && !fileChooser.accept(selectedFile))
						selectedFile = new File(selectedFile.getAbsolutePath().concat("."+FILENAMESUFFIX));
					this.controller.saveModuleTreeToFile(selectedFile);
					this.lastChosenFile = selectedFile;
					this.mainGuiFrame.setTitle(WINDOWTITLE+fileChooser.getSelectedFile().getName());
				}
				
			} catch (Exception e1) {
				LOGGER.log(Level.WARNING, UserMessages.UNABLE_TO_SAVE_MODULES, e1);
			}
			
		} else {
			LOGGER.log(Level.WARNING, UserMessages.unknownCommand(e.getActionCommand()));
		}
	}
	
	private void actionLoadModuleNetwork() {
		try {
			
			// Instantiate a new file chooser
			final JFileChooser fileChooser = new JFileChooser();
			
			// Set file filter
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
			        FILENAMESUFFIX_TITLE, FILENAMESUFFIX);
			fileChooser.setFileFilter(filter);
			
			// Set last chosen directory
			if (this.lastChosenFile != null)
				fileChooser.setSelectedFile(this.lastChosenFile);
			
			// Determine return value
			int returnVal = fileChooser.showOpenDialog(this.mainGuiFrame);
			
			// If the return value indicates approval, load the selected file
			if (returnVal==JFileChooser.APPROVE_OPTION){
				
				// Determine selected file
				File selectedFile = fileChooser.getSelectedFile();
				
				// Remember last chosen file
				this.lastChosenFile = selectedFile;
				
				// Load file
				ModuleNetwork loadedModuleNetwork = this.controller.loadModuleNetworkFromFile(selectedFile);
				loadedModuleNetwork.addCallbackReceiver(this);
				
				// Loop over loaded modules and add graphical representation for each
				Iterator<Module> modules = loadedModuleNetwork.getModuleList().iterator();
				while (modules.hasNext()){
					
					// Determine next module within the list
					Module module = modules.next();
					
					// Add corresponding module frame
					this.addModuleFrame(module);
				}
				
				// We will need a frame without an input connection when arranging them later
				ModuleInternalFrame startingFrame = null;
				
				// Determine desktop manager
				ModuleDesktopManager desktopManager = (ModuleDesktopManager) this.moduleJDesktopPane.getDesktopManager();
				
				// Loop over constructed module frames (in order to draw port connection lines)
				Iterator<ModuleInternalFrame> moduleFrames = this.moduleFrameMap.values().iterator();
				while (moduleFrames.hasNext()){
					
					// Determine next frame in list
					ModuleInternalFrame moduleFrame = moduleFrames.next();
					
					// Flag to mark whether at least one of the inputs is connected
					boolean inputConnected = false;
					
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
							
							// Set connected flag
							inputConnected = true;

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
					
					// Determine if module has an input -- if it does not, we define it as our starting frame
					if (moduleFrame.getInputButtons().isEmpty() || !inputConnected){
						startingFrame = moduleFrame;
						
						// Arrange other frames in dependence of this one (has to be done with each starting frame)
						desktopManager.rearrangeInternalFrame(startingFrame, this.moduleFrameMap);
					}
						
				}
				
				// If no frame without input connection has been found, issue a warning
				if (startingFrame == null){
					LOGGER.log(Level.WARNING, UserMessages.UNKNOWN_MODULES_ROOT);
				}
				
				mainGuiFrame.setTitle(WINDOWTITLE+fileChooser.getSelectedFile().getName());
			}
			
		} catch (Exception e1) {
			LOGGER.log(Level.WARNING, UserMessages.UNABLE_TO_LOAD_MODULES, e1);
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
				LOGGER.log(Level.WARNING, UserMessages.unableToConnectPorts(e1.getMessage()));
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
			LOGGER.log(Level.WARNING, UserMessages.UNABLE_TO_ADD_MODULE, e);
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
				LOGGER.log(Level.INFO, UserMessages.moduleHasBeenRemoved(module.getName()));
			else
				LOGGER.log(Level.WARNING, UserMessages.UNABLE_TO_REMOVE_MODULE);
			
		} catch (Exception e1) {
			LOGGER.log(Level.WARNING, UserMessages.UNABLE_TO_REMOVE_MODULE, e1);
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
			LOGGER.log(Level.WARNING, UserMessages.unknownObjectClicked(source));
			return;
		} else if (ModuleInputPortButton.class.isAssignableFrom(source.getClass())){
			ModuleInputPortButton button = (ModuleInputPortButton) source;
			this.moduleConnectionGlasspane.unlink(button);
			try {
				this.controller.getModuleNetwork().removeConnection((InputPort) button.getPort());
			} catch (NotFoundException e1) {
				LOGGER.log(Level.WARNING, UserMessages.ERROR_WHEN_REMOVING_PORT_CONNECTION, e1);
			}
		} else if (ModuleOutputPortButton.class.isAssignableFrom(source.getClass())){
			ModuleOutputPortButton button = (ModuleOutputPortButton) source;
			this.moduleConnectionGlasspane.unlink(button);
			try {
				this.controller.getModuleNetwork().removeConnection((OutputPort) button.getPort());
			} catch (NotFoundException e1) {
				LOGGER.log(Level.WARNING, UserMessages.ERROR_WHEN_REMOVING_PORT_CONNECTION, e1);
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

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode selectedNode =  (DefaultMutableTreeNode)((JTree)e.getSource()).getLastSelectedPathComponent();
		if (selectedNode != null && selectedNode.getUserObject() != null && Module.class.isAssignableFrom(selectedNode.getUserObject().getClass()))
			this.selectedModuleTemplate = (Module) selectedNode.getUserObject();
		else
			this.selectedModuleTemplate = null;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		// Check whether the event originates from the module list search field
		if (e.getDocument().equals(txtSearchmodulesfield.getDocument())) {
			try {
				this.filterModuleList(e.getDocument().getText(0, e.getDocument().getLength()));
				this.moduleTemplateTree.revalidate();
				this.moduleTemplateTree.repaint();
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		// Check whether the event originates from the module list search field
		if (e.getDocument().equals(txtSearchmodulesfield.getDocument())) {
			try {
				this.filterModuleList(e.getDocument().getText(0, e.getDocument().getLength()));
				this.moduleTemplateTree.revalidate();
				this.moduleTemplateTree.repaint();
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}
	
	/**
	 * Filters the list of available modules according to the specified search string (nonmatching entries will be hidden).
	 * @param filterString String to filter by
	 */
	private void filterModuleList(String filterString) {

		// Check length of search string
		if (filterString == null || filterString.length() == 0) {
			// If the search string is empty, we will reset the module list
			// to display all available modules
			ModuleJTreeCellRenderer renderer = (ModuleJTreeCellRenderer) moduleTemplateTree.getCellRenderer();
			renderer.setFilterString(filterString);
			

		} else if (filterString.length() > 2) {
			// If the search string consists of at least three characters,
			// we will update the module list accordingly
			ModuleJTreeCellRenderer renderer = (ModuleJTreeCellRenderer) moduleTemplateTree.getCellRenderer();
			renderer.setFilterString(filterString);
		}

	}
}
