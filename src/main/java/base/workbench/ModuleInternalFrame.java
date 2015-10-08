package base.workbench;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import modules.InputPort;
import modules.Module;
import modules.OutputPort;

public class ModuleInternalFrame extends JInternalFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3148570615005814437L;
	public static final ImageIcon ICON_MODULE_QUEUED = new ImageIcon(ModuleInternalFrame.class.getResource("/icons/idle.gif"));
	public static final ImageIcon ICON_MODULE_RUNNING = new ImageIcon(ModuleInternalFrame.class.getResource("/icons/running.gif"));
	public static final ImageIcon ICON_MODULE_SUCCESSFUL = new ImageIcon(ModuleInternalFrame.class.getResource("/icons/clean.png"));
	public static final ImageIcon ICON_MODULE_FAILED = new ImageIcon(ModuleInternalFrame.class.getResource("/icons/error.png"));
	private static int openFrameCount = 0;
	//private static final int xOffset = 30, yOffset = 30; // TODO Better placing of new/loaded module frames
    private static final int ROWSIZE=20; 
    private static final int MAXLENGTH=8; // Maximum length of button text
    
    private Module module;
    private ActionListener actionListener;
    private List<ModuleInputPortButton> inputButtons = new ArrayList<ModuleInputPortButton>();
    private List<ModuleOutputPortButton> outputButtons = new ArrayList<ModuleOutputPortButton>();
    private MouseListener mouseListener;

    public ModuleInternalFrame(Module module, ActionListener actionListener, MouseListener mouseListener) {
        super("Document #" + (++openFrameCount), 
              true, //resizable
              true, //closable
              false, //maximizable
              false);//iconifiable

        this.module = module;
        this.actionListener = actionListener;
        this.setMouseListener(mouseListener);
        
        this.setTitle(module.getName());
        
        // Create the GUI and put it into the window

        // Determine amount of rows needed to accomodate the I/O module labels
        int labelRowsNeeded = Math.max(module.getInputPorts().size(), module.getOutputPorts().size());
        
        // Set the window size (according to amount of I/O ports we will have to display)
        setSize(200,50+(labelRowsNeeded*ROWSIZE));

        // Set the button font
        Font buttonFont = new Font("Monospace", Font.PLAIN, 10);
        
        //Set the window's location.
        //setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
        
        // Set layout and create different subpanels
        this.getContentPane().setLayout(new BorderLayout());
        
        // Create config button
        JButton configButton = new JButton("configure");
        configButton.setFont(buttonFont);
        configButton.setActionCommand(ModuleWorkbenchGui.ACTION_EDITMODULE);
        configButton.addActionListener(actionListener);
        this.getContentPane().add(configButton, BorderLayout.SOUTH);
        
        // Panels for I/O buttons
        JPanel inputPortPanel = new JPanel();
        JPanel outputPortPanel = new JPanel();
        GridLayout layout = new GridLayout(Math.max(module.getInputPorts().size(), module.getOutputPorts().size()),1);
        inputPortPanel.setLayout(layout);
        outputPortPanel.setLayout(layout);
        this.getContentPane().add(inputPortPanel, BorderLayout.WEST);
        this.getContentPane().add(outputPortPanel, BorderLayout.EAST);
        
        // Loop over input ports
        Iterator<InputPort> inputPorts = module.getInputPorts().values().iterator();
        while (inputPorts.hasNext()){
        	InputPort inputPort = inputPorts.next();
        	ModuleInputPortButton inputPortButton = new ModuleInputPortButton(inputPort, MAXLENGTH, buttonFont);
        	inputPortButton.setActionCommand(ModuleWorkbenchGui.ACTION_ACTIVATEPORT);
        	inputPortButton.addActionListener(this.actionListener);
        	inputPortButton.addMouseListener(mouseListener);
        	inputPortPanel.add(inputPortButton);
        	this.inputButtons.add(inputPortButton);
        }
        
        Iterator<OutputPort> outputPorts = module.getOutputPorts().values().iterator();
        while (outputPorts.hasNext()){
        	OutputPort outputPort = outputPorts.next();
        	ModuleOutputPortButton outputPortButton = new ModuleOutputPortButton(outputPort, MAXLENGTH, buttonFont);
        	outputPortButton.setActionCommand(ModuleWorkbenchGui.ACTION_ACTIVATEPORT);
        	outputPortButton.addActionListener(this.actionListener);
        	outputPortButton.addMouseListener(mouseListener);
        	outputPortPanel.add(outputPortButton);
        	this.outputButtons.add(outputPortButton);
        }
        
        this.updateStatusIcon();
    }
    
    /**
     * Checks the status of the module and sets the appropriate icon.
     */
    public void updateStatusIcon(){
    	// Set icon depending on module status
		if (this.module.getStatus() == Module.STATUSCODE_NOTYETRUN)
			this.setFrameIcon(ICON_MODULE_QUEUED);
		else if (this.module.getStatus() == Module.STATUSCODE_RUNNING) {
			this.setFrameIcon(ICON_MODULE_RUNNING);
			ICON_MODULE_RUNNING.setImageObserver(this.getParent().getParent());
		} else if (this.module.getStatus() == Module.STATUSCODE_SUCCESS)
			this.setFrameIcon(ICON_MODULE_SUCCESSFUL);
		else if (this.module.getStatus() == Module.STATUSCODE_FAILURE)
			this.setFrameIcon(ICON_MODULE_FAILED);
    }

	/**
	 * @return the module
	 */
	public Module getModule() {
		return module;
	}

	/**
	 * @return the inputButtons
	 */
	public List<ModuleInputPortButton> getInputButtons() {
		return inputButtons;
	}

	/**
	 * @return the outputButtons
	 */
	protected List<ModuleOutputPortButton> getOutputButtons() {
		return outputButtons;
	}

	/**
	 * @return the mouseListener
	 */
	public MouseListener getMouseListener() {
		return mouseListener;
	}

	/**
	 * @param mouseListener the mouseListener to set
	 */
	public void setMouseListener(MouseListener mouseListener) {
		this.mouseListener = mouseListener;
	}
}

