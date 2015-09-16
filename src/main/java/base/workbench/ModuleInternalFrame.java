package base.workbench;

import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
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

    public ModuleInternalFrame(Module module) {
        super("Document #" + (++openFrameCount), 
              true, //resizable
              true, //closable
              false, //maximizable
              false);//iconifiable

        this.module = module;
        
        this.setTitle(module.getName());
        
        // Create the GUI and put it into the window

        // Determine amount of rows needed to accomodate the I/O module labels
        int labelRowsNeeded = Math.max(module.getInputPorts().size(), module.getOutputPorts().size());
        
        // Set the window size (according to amount of I/O ports we will have to display)
        setSize(160,60+(labelRowsNeeded*ROWSIZE));

        //Set the window's location.
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
        
        // Add I/O port labels
        this.getContentPane().setLayout(new BorderLayout());
        
        // Loop over input ports
        Iterator<InputPort> inputPorts = module.getInputPorts().values().iterator();
        /*while (inputPorts.hasNext()){
        	InputPort inputPort = inputPorts.next();
        	this.getContentPane().add(new JLabel(s, JLabel.LEFT), BorderLayout.WEST);
        }*/
        
        Iterator<OutputPort> outputPorts = module.getOutputPorts().values().iterator();
        InputPort inputPort = null;
        OutputPort outputPort = null;
        try {
            inputPort = inputPorts.next();
        } catch (NoSuchElementException e){
        }
        try {
        	outputPort = outputPorts.next();
        } catch (NoSuchElementException e){
        }
        while (inputPort != null || outputPort != null){
        	JLabel inputLabel;
        	if (inputPort == null)
        		inputLabel = new JLabel();
        	else
        		inputLabel = new ModuleInputPortLabel(inputPort);
        	
        	JLabel outputLabel;
        	if (outputPort == null)
        		outputLabel = new JLabel();
        	else
        		outputLabel = new ModuleOutputPortLabel(outputPort);
        	
        	this.getContentPane().add(inputLabel);
        	this.getContentPane().add(outputLabel);
        	
        	try {
                inputPort = inputPorts.next();
            } catch (NoSuchElementException e){
            	inputPort = null;
            }
            try {
            	outputPort = outputPorts.next();
            } catch (NoSuchElementException e){
            	outputPort = null;
            }
        }
    }

	/**
	 * @return the module
	 */
	public Module getModule() {
		return module;
	}
}

