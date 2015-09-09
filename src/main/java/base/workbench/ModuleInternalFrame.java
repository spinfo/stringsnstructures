package base.workbench;

import javax.swing.JInternalFrame;

public class ModuleInternalFrame extends JInternalFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3148570615005814437L;
	static int openFrameCount = 0;
    static final int xOffset = 30, yOffset = 30;

    public ModuleInternalFrame() {
        super("Document #" + (++openFrameCount), 
              true, //resizable
              true, //closable
              false, //maximizable
              false);//iconifiable

        //...Create the GUI and put it in the window...

        //...Then set the window size or call pack...
        setSize(300,300);

        //Set the window's location.
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
    }
}

