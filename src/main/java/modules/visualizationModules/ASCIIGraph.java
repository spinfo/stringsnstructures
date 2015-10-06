/**
 * 
 */
package modules.visualizationModules;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.treeBuilder.Knoten;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.parallelization.CallbackReceiver;

/**
 * Creates an image file a with visual representation of the node distribution
 * within the input graph.
 * 
 * @author Marcel Boeing
 *
 */
public class ASCIIGraph extends ModuleImpl {
	
	// Property keys
	public static final String PROPERTYKEY_USEPARENTSYMBOL = "Biggest child uses parent symbol";
	
	// Instance variables
	private final String INPUTID = "input";
	private final String OUTPUTID = "output";
	KnotenKomparator knotenKomparator = new KnotenKomparator();
	boolean elternZeichenUebernehmen;

	/**
	 * @param callbackReceiver
	 * @param properties
	 * @throws Exception
	 */
	public ASCIIGraph(CallbackReceiver callbackReceiver, Properties properties)
			throws Exception {
		super(callbackReceiver, properties);

		// Define I/O
		InputPort inputPort = new InputPort(INPUTID, "JSON-encoded suffix trie.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUTID, "ASCII visualization.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);

		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_USEPARENTSYMBOL,
				"Has the biggest child of each node use that node's symbol (to visualize the flow of each path most used).");
		
		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "ASCIIGraph");
		this.getPropertyDefaultValues().put(PROPERTYKEY_USEPARENTSYMBOL, "true");
		
		// Add module description
		this.setDescription("Creates ASCII output with a visual representation of the node distribution within the input graph.");

	}

	/* (non-Javadoc)
	 * @see modularization.ModuleImpl#applyProperties()
	 */
	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		if (this.getProperties().containsKey(PROPERTYKEY_USEPARENTSYMBOL))
			this.elternZeichenUebernehmen = Boolean.valueOf(this.getProperties()
					.getProperty(PROPERTYKEY_USEPARENTSYMBOL));
		super.applyProperties();
	}

	/* (non-Javadoc)
	 * @see modularization.ModuleImpl#process()
	 */
	@Override
	public boolean process() throws Exception {
		
		// Instantiate JSON parser
		Gson gson = new GsonBuilder().create();
				
		// Wurzelknoten einlesen
		Knoten wurzelKnoten = gson.fromJson(this.getInputPorts().get(INPUTID).getInputReader(), Knoten.class);
		
		// Baummodell initialisieren
		DefaultTreeModel baum = this.insertIntoTreeModel(wurzelKnoten, null, null);
		
		// Graphen ausgeben
		DefaultMutableTreeNode baumWurzelKnoten = (DefaultMutableTreeNode) baum.getRoot();
		int zeichenProZeile = wurzelKnoten.getZaehler();
		int zeichenInAktuellerZeile = 0;

		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> baumKindKnotenListe = baumWurzelKnoten.breadthFirstEnumeration();
		while (baumKindKnotenListe.hasMoreElements()){
			
			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// Ermittle aktuelles Element
			DefaultMutableTreeNode baumKindKnoten = baumKindKnotenListe.nextElement();
			Knoten kindKnoten = (Knoten) baumKindKnoten.getUserObject();
			
			// Ggf. Position der Schreibmarke vorruecken
			if (baumKindKnoten.getParent() != null
					&& ((DefaultMutableTreeNode) baumKindKnoten.getParent())
							.getUserObject() != null
					&& MetaKnoten.class
							.isAssignableFrom(((DefaultMutableTreeNode) baumKindKnoten
									.getParent()).getUserObject().getClass())){
				
				// Metaknotenobjekt ermitteln
				MetaKnoten metaKnoten = (MetaKnoten) ((DefaultMutableTreeNode) baumKindKnoten
						.getParent()).getUserObject();
				
				// Zeichen vom Elternknoten uebernehmen (falls vorhanden und gewuenscht)
				if (elternZeichenUebernehmen){
					if (!metaKnoten.getKnoten().getName().isEmpty())
						kindKnoten.setName(metaKnoten.getKnoten().getName());
					
					// Zeichen des Elternknotens loeschen, damit Geschwisterknoten ihr eigenes verwenden
					metaKnoten.getKnoten().setName("");
				}
				
				// Ggf. Position vorschieben
				if (metaKnoten.getPosition()>zeichenInAktuellerZeile){
					int leerZeichenEinfuegen = metaKnoten.getPosition()-zeichenInAktuellerZeile;
					for (int i=0; i<leerZeichenEinfuegen; i++){
						this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(" ");
						zeichenInAktuellerZeile++;
					}
				}
			}
			
			for (int i=0; i<kindKnoten.getZaehler(); i++)
				this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(kindKnoten.getName());
			
			// Position merken (etwas unelegant)
			baumKindKnoten.setUserObject(new MetaKnoten(kindKnoten, zeichenInAktuellerZeile, 0, 0, 0));
			zeichenInAktuellerZeile += kindKnoten.getZaehler();
			
			// Ggf. Zeilenumbruch einfuegen
			if (zeichenInAktuellerZeile >= zeichenProZeile){
				this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(" ["+zeichenInAktuellerZeile+"]\n");
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
			//baum.insertNodeInto(baumKnoten, elternBaumKnoten, elternBaumKnoten.getChildCount());
			baum.insertNodeInto(baumKnoten, elternBaumKnoten, 0);
		
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
