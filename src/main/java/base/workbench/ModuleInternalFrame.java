package base.workbench;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

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
        
        // Set layout and create different subpanels
        this.getContentPane().setLayout(new BorderLayout());
        JPanel inputPortPanel = new JPanel();
        JPanel outputPortPanel = new JPanel();
        inputPortPanel.setLayout(new BoxLayout(inputPortPanel,BoxLayout.PAGE_AXIS));
        outputPortPanel.setLayout(new BoxLayout(outputPortPanel,BoxLayout.PAGE_AXIS));
        this.getContentPane().add(inputPortPanel, BorderLayout.WEST);
        this.getContentPane().add(outputPortPanel, BorderLayout.EAST);
        
        
        // Create transfer handler
        ModulePortLabelTransferhandler transferhanlder = new ModulePortLabelTransferhandler();
        
        // Create mouse adapter
		MouseAdapter mouseAdapter = new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				System.out.println("pressed:"+evt.getSource().getClass().getSimpleName());
				JComponent comp = (JComponent) evt.getSource();
				TransferHandler th = comp.getTransferHandler();

				th.exportAsDrag(comp, evt, TransferHandler.LINK);
			}

			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseReleased(MouseEvent e) {
				System.out.println("released:"+e.getSource().getClass().getSimpleName());
				//((AbstractModulePortLabel)e.getSource()).getTransferHandler().exportAsDrag((AbstractModulePortLabel)e.getSource(), e, TransferHandler.LINK);
				super.mouseReleased(e);
			}

			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseDragged(MouseEvent e) {
				System.out.println("dragged:"+e.getSource().getClass().getSimpleName());
				// TODO Auto-generated method stub
				super.mouseDragged(e);
			}
		};
        
        // Add I/O port labels
        
        // Loop over input ports
        Iterator<InputPort> inputPorts = module.getInputPorts().values().iterator();
        while (inputPorts.hasNext()){
        	InputPort inputPort = inputPorts.next();
        	ModuleInputPortLabel inputPortLabel = new ModuleInputPortLabel(inputPort);
        	inputPortLabel.setTransferHandler(transferhanlder);
        	inputPortLabel.addMouseListener(mouseAdapter);
        	inputPortPanel.add(inputPortLabel);
        }
        
        Iterator<OutputPort> outputPorts = module.getOutputPorts().values().iterator();
        while (outputPorts.hasNext()){
        	OutputPort outputPort = outputPorts.next();
        	ModuleOutputPortLabel outputPortLabel = new ModuleOutputPortLabel(outputPort);
        	outputPortLabel.setTransferHandler(transferhanlder);
        	outputPortLabel.addMouseListener(mouseAdapter);
        	outputPortPanel.add(outputPortLabel);
        }
    }

	/**
	 * @return the module
	 */
	public Module getModule() {
		return module;
	}
}

