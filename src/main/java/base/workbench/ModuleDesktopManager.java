package base.workbench;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import modules.InputPort;
import modules.Module;
import modules.OutputPort;
import modules.Pipe;

/**
 * See http://www.java2s.com/Code/Java/Swing-JFC/InterestingthingsusingJInternalFramesJDesktopPaneandDesktopManager2.htm
 */
public class ModuleDesktopManager extends DefaultDesktopManager {

	private static final long serialVersionUID = 3915844154537065196L;
	private ModuleNetworkGlasspane glasspane;
	
	/**
	 * Rearranges the internal frames connected to the specified one, very
	 * crudely guessing decent placement.
	 * @param frame Frame whose output nodes shall be rearranged
	 */
	public void rearrangeInternalFrame(ModuleInternalFrame frame, Map<Module,ModuleInternalFrame> moduleFrameMap){
		
		// Clone map (because we will need to change it)
		Map<Module,ModuleInternalFrame> moduleFrameMapLocal = new HashMap<Module,ModuleInternalFrame>(moduleFrameMap.size());
		moduleFrameMapLocal.putAll(moduleFrameMap);
		
		// Call private method with map clone
		rearrangeInternalFrame_changeMap(frame, moduleFrameMapLocal);
	}
	
	/**
	 * Rearranges the internal frames connected to the specified one, very
	 * crudely guessing decent placement. Makes changes to the given Map.
	 * @param frame Frame whose output nodes shall be rearranged
	 */
	private void rearrangeInternalFrame_changeMap(ModuleInternalFrame frame, Map<Module,ModuleInternalFrame> moduleFrameMap){
		
		// Set margins
		int xMargin = 10;
		int yMargin = 10;
		
		// Determine output nodes
		Iterator<ModuleOutputPortButton> outputButtons = frame.getOutputButtons().iterator();
		
		// Position connected frames relative to current one
		int yOffset=0;
		while(outputButtons.hasNext()){
			
			// Determine output button next in list
			ModuleOutputPortButton outputButton = outputButtons.next();
			
			// Determine output port
			OutputPort outputPort = (OutputPort)outputButton.getPort();
			
			// Determine pipe classes supported by this output port
			Iterator<Class<? extends Pipe>> pipeClasses = outputPort.getSupportedPipeClasses().values().iterator();
			
			// Loop over pipe classes
			while (pipeClasses.hasNext()){
				
				// Determine pipe class
				Class<? extends Pipe> pipeClass = pipeClasses.next();
				
				// Loop over connected pipes
				Iterator<Pipe> connectedPipes = outputPort.getPipes(pipeClass).iterator();
				while(connectedPipes.hasNext()){
					
					// Determine input port connected via this pipe
					Pipe connectedPipe = connectedPipes.next();
					InputPort connectedPort = (InputPort) outputPort.getConnectedPort(connectedPipe);
					
					// Determine connected module
					if (connectedPort != null){
						Module connectedModule = connectedPort.getParent();
						
						// Determine frame & remove it from map
						ModuleInternalFrame connectedFrame = moduleFrameMap.remove(connectedModule);
						
						// Check if null (has already been removed)
						if (connectedFrame == null)
							continue;
						
						// Place frame
						this.dragFrame(connectedFrame, new Double(xMargin+frame.getBounds().x+frame.getBounds().getWidth()).intValue(), yOffset+frame.getBounds().y);
						
						
						// Update offset
						yOffset += yMargin+connectedFrame.getBounds().getHeight();
						
						// Recurse
						this.rearrangeInternalFrame_changeMap(connectedFrame, moduleFrameMap);
						
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.DefaultDesktopManager#dragFrame(javax.swing.JComponent, int, int)
	 */
	@Override
	public void dragFrame(JComponent f, int x, int y) {
		JDesktopPane desk = null;
		if (f instanceof JInternalFrame) { // Deal only w/internal frames
			JInternalFrame frame = (JInternalFrame) f;
			desk = frame.getDesktopPane();
			Dimension d = desk.getSize();

			// Nothing all that fancy below, just figuring out how to adjust
			// to keep the frame on the desktop.
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
					y = d.height - frame.getHeight(); // flush against the
					// bottom
				}
			}
		}

		// Pass along the (possibly cropped) values to the normal drag handler.
		try {
			super.dragFrame(f, x, y);
		} catch (Exception e){
			/*
			 * FIXME: Strangely enough, that method sometimes throws a
			 * NullPointerException for no apparent reason.
			 */
		}
		if (desk!=null)
			desk.repaint();
	}

	/* (non-Javadoc)
	 * @see javax.swing.DefaultDesktopManager#endResizingFrame(javax.swing.JComponent)
	 */
	@Override
	public void endResizingFrame(JComponent f) {
		super.endResizingFrame(f);
		this.glasspane.setVisible(true);
		
	}

	/**
	 * @return the glasspane
	 */
	protected ModuleNetworkGlasspane getGlasspane() {
		return glasspane;
	}

	/**
	 * @param glasspane the glasspane to set
	 */
	protected void setGlasspane(ModuleNetworkGlasspane glasspane) {
		this.glasspane = glasspane;
	}

}
