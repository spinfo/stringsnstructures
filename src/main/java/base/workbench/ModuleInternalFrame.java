package base.workbench;

import javax.swing.JInternalFrame;

import modules.Module;

public class ModuleInternalFrame extends JInternalFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3148570615005814437L;
	static int openFrameCount = 0;
    static final int xOffset = 30, yOffset = 30;
    
    private Module module;

    public ModuleInternalFrame(Module module) {
        super("Document #" + (++openFrameCount), 
              true, //resizable
              true, //closable
              false, //maximizable
              false);//iconifiable

        this.setModule(module);
        
        this.setTitle(module.getName());
        
        //...Create the GUI and put it in the window...

        //...Then set the window size or call pack...
        setSize(160,80);

        //Set the window's location.
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
    }

	/**
	 * @return the module
	 */
	public Module getModule() {
		return module;
	}

	/**
	 * @param module the module to set
	 */
	public void setModule(Module module) {
		this.module = module;
	}
}

