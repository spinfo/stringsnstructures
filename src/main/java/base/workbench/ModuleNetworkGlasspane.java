package base.workbench;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JRootPane;

/**
 * @see http://stackoverflow.com/a/12389479/909085
 */

public class ModuleNetworkGlasspane extends JComponent {

	private static final long serialVersionUID = -1423113285724582925L;
	private Map<ModuleInputPortButton, ModuleOutputPortButton> linked; // InputPortButton - OutputPortButton
	private JDesktopPane desktopPane;

    public ModuleNetworkGlasspane (JDesktopPane desktopPane)
    {
        super ();
        linked = new HashMap<ModuleInputPortButton, ModuleOutputPortButton> ();
        this.desktopPane = desktopPane;
    }

    public void link ( ModuleInputPortButton inputButton, ModuleOutputPortButton outputButton )
    {
        linked.put ( inputButton, outputButton );
        repaint ();
        desktopPane.repaint();
        System.out.println("repaint1");
    }

    public void unlink ( ModuleInputPortButton inputButton)
    {
        linked.remove(inputButton);
        repaint ();
        desktopPane.repaint();
        System.out.println("repaint2");
    }

    protected void paintComponent ( Graphics g )
    {
        Graphics2D g2d = ( Graphics2D ) g;
        g2d.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        g2d.setPaint ( Color.GREEN );
        for ( JComponent c1 : linked.keySet () )
        {
            Point p1 = getRectCenter ( getBoundsInWindow ( c1 ) );
            Point p2 = getRectCenter ( getBoundsInWindow ( this.linked.get ( c1 ) ) );
            System.out.println("Drawn line from "+p1.x+":"+p1.y+" to "+p2.x+":"+p2.y); // DEBUG
            g2d.drawLine ( p1.x, p1.y, p2.x, p2.y );
        }

        //desktopPane.repaint();
    }

    private Point getRectCenter ( Rectangle rect )
    {
        return new Point ( rect.x + rect.width / 2, rect.y + rect.height / 2 );
    }

    private Rectangle getBoundsInWindow ( Component component )
    {
        //return getRelativeBounds ( component, getRootPaneAncestor ( component ) );
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

    private JRootPane getRootPaneAncestor ( Component c )
    {
        for ( Container p = c.getParent (); p != null; p = p.getParent () )
        {
            if ( p instanceof JDesktopPane )
            {
            	System.out.println("Rootpane: "+p.getClass().getName());
                return ( JRootPane ) p;
            }
        }
        return null;
    }

    public boolean contains ( int x, int y )
    {
        return false;
    }

    /*private static ModuleNetworkGlasspane linker;

    public static void main ( String[] args )
    {
        setupLookAndFeel ();

        JFrame frame = new JFrame ();

        linker = new ModuleNetworkGlasspane ();
        frame.setGlassPane ( linker );
        linker.setVisible ( true );

        JPanel content = new JPanel ();
        content.setLayout ( new GridLayout ( 10, 5, 5, 5 ) );
        content.setBorder ( BorderFactory.createEmptyBorder ( 5, 5, 5, 5 ) );
        frame.add ( content );

        for ( int i = 0; i < 50; i++ )
        {
            final JButton button = new JButton ( "Button" + i );
            button.addActionListener ( new ActionListener ()
            {
                public void actionPerformed ( ActionEvent e )
                {
                    link ( button );
                }
            } );
            content.add ( button );
        }

        frame.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
        frame.pack ();
        frame.setLocationRelativeTo ( null );
        frame.setVisible ( true );
    }

    private static void setupLookAndFeel ()
    {
        try
        {
            UIManager.setLookAndFeel ( UIManager.getSystemLookAndFeelClassName () );
        }
        catch ( ClassNotFoundException e )
        {
            e.printStackTrace ();
        }
        catch ( InstantiationException e )
        {
            e.printStackTrace ();
        }
        catch ( IllegalAccessException e )
        {
            e.printStackTrace ();
        }
        catch ( UnsupportedLookAndFeelException e )
        {
            e.printStackTrace ();
        }
    }*/
}
