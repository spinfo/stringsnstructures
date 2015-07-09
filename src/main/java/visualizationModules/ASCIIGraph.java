/**
 * 
 */
package visualizationModules;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import modularization.CharPipe;
import modularization.ModuleImpl;
import parallelization.CallbackReceiver;
import treeBuilder.Knoten;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Creates an image file a with visual representation of the node distribution
 * within the input graph.
 * 
 * @author Marcel Boeing
 *
 */
public class ASCIIGraph extends ModuleImpl {
	
	// Instance variables
	KnotenKomparator knotenKomparator = new KnotenKomparator();

	/**
	 * @param callbackReceiver
	 * @param properties
	 * @throws Exception
	 */
	public ASCIIGraph(CallbackReceiver callbackReceiver, Properties properties)
			throws Exception {
		super(callbackReceiver, properties);

		// Define I/O
		this.getSupportedInputs().add(CharPipe.class);
		this.getSupportedOutputs().add(CharPipe.class);
		
		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "ASCIIGraph");
		
		// Add module description
		this.setDescription("Creates ASCII output with a visual representation of the node distribution within the input graph.");

	}

	/* (non-Javadoc)
	 * @see modularization.ModuleImpl#process()
	 */
	@Override
	public boolean process() throws Exception {
		
		// Instantiate JSON parser
		Gson gson = new GsonBuilder().create();
				
		// Wurzelknoten einlesen
		Knoten wurzelKnoten = gson.fromJson(this.getInputCharPipe().getInput(), Knoten.class);
		
		// Baummodell initialisieren
		DefaultTreeModel baum = this.insertIntoTreeModel(wurzelKnoten, null, null);
		
		// Graphen ausgeben
		DefaultMutableTreeNode baumWurzelKnoten = (DefaultMutableTreeNode) baum.getRoot();
		int zeichenProZeile = wurzelKnoten.getZaehler();
		int zeichenInAktuellerZeile = 0;

		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> baumKindKnotenListe = baumWurzelKnoten.breadthFirstEnumeration();
		while (baumKindKnotenListe.hasMoreElements()){
			DefaultMutableTreeNode baumKindKnoten = baumKindKnotenListe.nextElement();
			Knoten kindKnoten = (Knoten) baumKindKnoten.getUserObject();
			
			// Ggf. Position der Schreibmarke vorruecken
			if (baumKindKnoten.getParent() != null
					&& ((DefaultMutableTreeNode) baumKindKnoten.getParent())
							.getUserObject() != null
					&& Integer.class
							.isAssignableFrom(((DefaultMutableTreeNode) baumKindKnoten
									.getParent()).getUserObject().getClass())
					&& (Integer) ((DefaultMutableTreeNode) baumKindKnoten
							.getParent()).getUserObject() > zeichenInAktuellerZeile){
				
				int leerZeichenEinfuegen = (Integer) ((DefaultMutableTreeNode) baumKindKnoten
						.getParent()).getUserObject()-zeichenInAktuellerZeile;
				for (int i=0; i<leerZeichenEinfuegen; i++){
					this.outputToAllCharPipes(" ");
					zeichenInAktuellerZeile++;
				}
			}
			
			for (int i=0; i<kindKnoten.getZaehler(); i++)
				this.outputToAllCharPipes(kindKnoten.getName());
			// Position merken (etwas unelegant)
			baumKindKnoten.setUserObject(new Integer(zeichenInAktuellerZeile));
			zeichenInAktuellerZeile += kindKnoten.getZaehler();
			
			// Ggf. Zeilenumbruch einfuegen
			if (zeichenInAktuellerZeile >= zeichenProZeile){
				this.outputToAllCharPipes(" ["+zeichenInAktuellerZeile+"]\n");
				zeichenInAktuellerZeile = 0;
			}
		}
		this.closeAllOutputs();
		
		return true;
	}

	private DefaultTreeModel insertIntoTreeModel(Knoten knoten, DefaultMutableTreeNode elternBaumKnoten, DefaultTreeModel baum) throws IOException {
		
		DefaultMutableTreeNode baumKnoten = new DefaultMutableTreeNode(knoten);
		
		if (baum == null){
			baum = new DefaultTreeModel(baumKnoten);
		} else 
			baum.insertNodeInto(baumKnoten, elternBaumKnoten, elternBaumKnoten.getChildCount());
		
		// Kindknoten in TreeSet mit eigenem Comparator speichern (sortiert nach
		// Zaehlvariable der Knoten)
		TreeSet<Knoten> sortierteKindKnoten = new TreeSet<Knoten>(
				this.knotenKomparator);
		sortierteKindKnoten.addAll(knoten.getKinder().values());
		
		Iterator<Knoten> kindKnoten = sortierteKindKnoten.iterator();
		while (kindKnoten.hasNext()){
			this.insertIntoTreeModel(kindKnoten.next(), baumKnoten, baum);
		}
		
		return baum;
		
	}

}
