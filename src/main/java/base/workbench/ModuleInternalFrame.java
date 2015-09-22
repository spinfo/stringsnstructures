package base.workbench;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
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
	private static int openFrameCount = 0;
	private static final int xOffset = 30, yOffset = 30;
    private static final int ROWSIZE=20; 
    
    private Module module;
    private ActionListener actionListener;
    private List<ModuleInputPortButton> inputButtons = new ArrayList<ModuleInputPortButton>();
    private List<ModuleOutputPortButton> outputButtons = new ArrayList<ModuleOutputPortButton>();

    public ModuleInternalFrame(Module module, ActionListener actionListener) {
        super("Document #" + (++openFrameCount), 
              true, //resizable
              true, //closable
              false, //maximizable
              false);//iconifiable

        this.module = module;
        this.actionListener = actionListener;
        
        this.setTitle(module.getName());
        
        // Create the GUI and put it into the window

        // Determine amount of rows needed to accomodate the I/O module labels
        int labelRowsNeeded = Math.max(module.getInputPorts().size(), module.getOutputPorts().size());
        
        // Set the window size (according to amount of I/O ports we will have to display)
        setSize(160,60+(labelRowsNeeded*ROWSIZE));

        //Set the window's location.
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
        
        // Set layout and create different subpanels
        this.getContentPane().setLayout(new BorderLayout());
        JPanel inputPortPanel = new JPanel();
        JPanel outputPortPanel = new JPanel();
        inputPortPanel.setLayout(new BoxLayout(inputPortPanel,BoxLayout.PAGE_AXIS));
        outputPortPanel.setLayout(new BoxLayout(outputPortPanel,BoxLayout.PAGE_AXIS));
        this.getContentPane().add(inputPortPanel, BorderLayout.WEST);
        this.getContentPane().add(outputPortPanel, BorderLayout.EAST);
        
        // Add I/O port labels
        
        // Loop over input ports
        Iterator<InputPort> inputPorts = module.getInputPorts().values().iterator();
        while (inputPorts.hasNext()){
        	InputPort inputPort = inputPorts.next();
        	ModuleInputPortButton inputPortButton = new ModuleInputPortButton(inputPort);
        	inputPortButton.setActionCommand(ModuleWorkbenchGui.ACTION_ACTIVATEPORT);
        	inputPortButton.addActionListener(this.actionListener);
        	inputPortPanel.add(inputPortButton);
        	this.inputButtons.add(inputPortButton);
        }
        
        Iterator<OutputPort> outputPorts = module.getOutputPorts().values().iterator();
        while (outputPorts.hasNext()){
        	OutputPort outputPort = outputPorts.next();
        	ModuleOutputPortButton outputPortButton = new ModuleOutputPortButton(outputPort);
        	outputPortButton.setActionCommand(ModuleWorkbenchGui.ACTION_ACTIVATEPORT);
        	outputPortButton.addActionListener(this.actionListener);
        	outputPortPanel.add(outputPortButton);
        	this.outputButtons.add(outputPortButton);
        }
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
}

