package base.workbench;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;

/**
 * @see http://stackoverflow.com/a/12389479/909085
 */

public class ModuleNetworkGlasspane extends JComponent {

	private static final long serialVersionUID = -1423113285724582925L;
	private ConcurrentHashMap<ModuleInputPortButton, ModuleOutputPortButton> linked; // InputPortButton - OutputPortButton
	private JDesktopPane desktopPane;
	private AbstractModulePortButton activeLinkingPortButton = null;

    public ModuleNetworkGlasspane (JDesktopPane desktopPane)
    {
        super ();
        this.linked = new ConcurrentHashMap<ModuleInputPortButton, ModuleOutputPortButton> ();
        this.desktopPane = desktopPane;
    }

    public void link ( ModuleInputPortButton inputButton, ModuleOutputPortButton outputButton )
    {
    	this.linked.put ( inputButton, outputButton );
        repaint ();
        this.desktopPane.repaint();
    }

    public void unlink ( ModuleInputPortButton inputButton )
    {
    	this.linked.remove(inputButton);
        repaint ();
        this.desktopPane.repaint();
    }

    public void unlink ( final ModuleOutputPortButton outputButton )
    {
    	// Remove all links targeting the specified output button
    	this.linked.values().removeIf(new Predicate<ModuleOutputPortButton> () {

			@Override
			public boolean test(ModuleOutputPortButton t) {
				return t.equals(outputButton);
			}});
    	
    	// Repaint visuals
        repaint ();
        this.desktopPane.repaint();
    }

    protected void paintComponent ( Graphics g )
    {
        Graphics2D g2d = ( Graphics2D ) g;
        g2d.setStroke(new BasicStroke(3));
        g2d.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        g2d.setPaint ( Color.GREEN );
        for ( JComponent c1 : linked.keySet () )
        {
            Point p1 = getRectCenter ( getBoundsInWindow ( c1 ) );
            Point p2 = getRectCenter ( getBoundsInWindow ( this.linked.get ( c1 ) ) );
            g2d.drawLine ( p1.x, p1.y, p2.x, p2.y );
        }
        
        // Draw link from selected button to mouse cursor during linking activity
        /*if (this.activeLinkingPortButton != null){
        	g2d.setPaint ( Color.RED );
        	Point p1 = getRectCenter ( getBoundsInWindow ( this.activeLinkingPortButton ) );
        	g2d.drawLine ( p1.x, p1.y, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y );
        }*/
    }

    private Point getRectCenter ( Rectangle rect )
    {
        return new Point ( rect.x + rect.width / 2, rect.y + rect.height / 2 );
    }

    private Rectangle getBoundsInWindow ( Component component )
    {
        return getRelativeBounds ( component, this.desktopPane.getRootPane() );
    }

    private Rectangle getRelativeBounds ( Component component, Component relativeTo )
    {
        return new Rectangle ( getRelativeLocation ( component, relativeTo ),
                component.getSize () );
    }

    private Point getRelativeLocation ( Component component, Component relativeTo )
    {
        Point los = component.getLocationOnScreen ();
        Point rt = relativeTo.getLocationOnScreen ();
        return new Point ( los.x - rt.x, los.y - rt.y );
    }

    public boolean contains ( int x, int y )
    {
        return false;
    }

	/**
	 * @return the activeLinkingPortButton
	 */
	public AbstractModulePortButton getActiveLinkingPortButton() {
		return activeLinkingPortButton;
	}

	/**
	 * @param activeLinkingPortButton the activeLinkingPortButton to set
	 */
	public void setActiveLinkingPortButton(AbstractModulePortButton activeLinkingPortButton) {
		this.activeLinkingPortButton = activeLinkingPortButton;
	}
}
