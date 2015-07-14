/**
 * 
 */
package visualizationModules;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
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
public class ColourGraph extends ModuleImpl {

	// Property keys
	public static final String PROPERTYKEY_OUTPUTFILE = "Output file";
	public static final String PROPERTYKEY_IMAGEWIDTH = "Image width";
	public static final String PROPERTYKEY_IMAGEHEIGHT = "Image height";
	public static final String PROPERTYKEY_PIXELPERLEVEL = "Pixel per tree level";
	
	// Instance variables
	private KnotenKomparator knotenKomparator = new KnotenKomparator();
	private String outputFilePath;
	private int outputImageWidth;
	private int outputImageHeight;
	private boolean fehlerGemeldet = false;
	private double horizontalePixelProKnoten = 1d;
	private int pixelsPerLevel;

	/**
	 * @param callbackReceiver
	 * @param properties
	 * @throws Exception
	 */
	public ColourGraph(CallbackReceiver callbackReceiver, Properties properties)
			throws Exception {
		super(callbackReceiver, properties);

		// Define I/O
		this.getSupportedInputs().add(CharPipe.class);

		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_OUTPUTFILE,
				"Specifies the location of the output file (PNG or JPEG).");
		this.getPropertyDescriptions().put(PROPERTYKEY_IMAGEWIDTH,
				"Width of the output image in pixels.");
		this.getPropertyDescriptions().put(PROPERTYKEY_IMAGEHEIGHT,
				"Height of the output image in pixels.");
		this.getPropertyDescriptions().put(PROPERTYKEY_PIXELPERLEVEL,
				"Amount of pixels used for each tree level.");

		// Determine system properties (for setting default values that make
		// sense)
		String fs = System.getProperty("file.separator");
		String homedir = System.getProperty("user.home");

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"ColourGraph");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTPUTFILE,
				homedir + fs + "out.jpeg");
		this.getPropertyDefaultValues().put(PROPERTYKEY_IMAGEWIDTH, "640");
		this.getPropertyDefaultValues().put(PROPERTYKEY_IMAGEHEIGHT, "480");
		this.getPropertyDefaultValues().put(PROPERTYKEY_PIXELPERLEVEL, "10");
		
		// Add module description
		this.setDescription("Creates an image file a with visual representation of the node distribution within the input graph.");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see modularization.ModuleImpl#applyProperties()
	 */
	@Override
	public void applyProperties() throws Exception {
		if (this.getProperties().containsKey(PROPERTYKEY_OUTPUTFILE))
			this.outputFilePath = this.getProperties().getProperty(
					PROPERTYKEY_OUTPUTFILE);
		else
			this.outputFilePath = this.getPropertyDefaultValues().get(
					PROPERTYKEY_OUTPUTFILE);
		if (this.getProperties().containsKey(PROPERTYKEY_IMAGEWIDTH))
			this.outputImageWidth = Integer.valueOf(this.getProperties()
					.getProperty(PROPERTYKEY_IMAGEWIDTH));
		else if (this.getPropertyDefaultValues().containsKey(PROPERTYKEY_IMAGEWIDTH))
			this.outputImageWidth = Integer.valueOf(this
					.getPropertyDefaultValues().get(PROPERTYKEY_IMAGEWIDTH));
		if (this.getProperties().containsKey(PROPERTYKEY_IMAGEHEIGHT))
			this.outputImageHeight = Integer.valueOf(this.getProperties()
					.getProperty(PROPERTYKEY_IMAGEHEIGHT));
		else if (this.getPropertyDefaultValues().containsKey(PROPERTYKEY_IMAGEHEIGHT))
			this.outputImageHeight = Integer.valueOf(this
					.getPropertyDefaultValues().get(PROPERTYKEY_IMAGEHEIGHT));
		if (this.getProperties().containsKey(PROPERTYKEY_PIXELPERLEVEL))
			this.pixelsPerLevel = Integer.valueOf(this.getProperties()
					.getProperty(PROPERTYKEY_PIXELPERLEVEL));
		else if (this.getPropertyDefaultValues().containsKey(PROPERTYKEY_PIXELPERLEVEL))
			this.pixelsPerLevel = Integer.valueOf(this
					.getPropertyDefaultValues().get(PROPERTYKEY_PIXELPERLEVEL));
		super.applyProperties();
	}

	/* (non-Javadoc)
	 * @see modularization.ModuleImpl#process()
	 */
	@Override
	public boolean process() throws Exception {
		
		// Instantiate JSON parser
		Gson gson = new GsonBuilder().create();
		
		// Bild instanziieren
		BufferedImage bild = new BufferedImage(this.outputImageWidth, this.outputImageHeight, BufferedImage.TYPE_INT_RGB);
				
		// Wurzelknoten einlesen
		Knoten wurzelKnoten = gson.fromJson(this.getInputCharPipe().getInput(), Knoten.class);
		
		// Skalierung ermitteln
		this.horizontalePixelProKnoten = new Double(this.outputImageWidth)/new Double(wurzelKnoten.getZaehler());
		
		// Baummodell initialisieren
		DefaultTreeModel baum = this.insertIntoTreeModel(wurzelKnoten, null, null);
		
		// Graphen ausgeben
		DefaultMutableTreeNode baumWurzelKnoten = (DefaultMutableTreeNode) baum.getRoot();
		int zeichenProZeile = wurzelKnoten.getZaehler();
		int zeichenInAktuellerZeile = 0;
		int zeile = 0;

		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> baumKindKnotenListe = baumWurzelKnoten.breadthFirstEnumeration();
		while (baumKindKnotenListe.hasMoreElements()){
			DefaultMutableTreeNode baumKindKnoten = baumKindKnotenListe.nextElement();
			Knoten kindKnoten = (Knoten) baumKindKnoten.getUserObject();
			
			// Farbvariablen
			int r;
			int g;
			int b;
			
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
				
				// Farben uebernehmen
				r = metaKnoten.getR();
				g = metaKnoten.getG();
				b = metaKnoten.getB();
				
				// Farben aktualisieren, damit Geschwisterknoten einen dunkleren Farbton bekommen
				/*int naechstesR = metaKnoten.getR()-5;
				if (naechstesR<0)
					naechstesR=0;
				metaKnoten.setR(naechstesR);
				int naechstesG = metaKnoten.getG()-5;
				if (naechstesG<0)
					naechstesG=0;
				metaKnoten.setG(naechstesG);
				int naechstesB = metaKnoten.getB()-5;
				if (naechstesB<0)
					naechstesB=0;
				metaKnoten.setR(naechstesB);*/
				metaKnoten.setR(new Double(20+(Math.random() * 234)).intValue());
				metaKnoten.setG(new Double(20+(Math.random() * 234)).intValue());
				metaKnoten.setB(new Double(20+(Math.random() * 234)).intValue());
				
				// Ggf. Position vorschieben
				if (metaKnoten.getPosition()>zeichenInAktuellerZeile){
					int leerZeichenEinfuegen = metaKnoten.getPosition()-zeichenInAktuellerZeile;
					for (int i=0; i<leerZeichenEinfuegen; i++){
						this.zeichnePixel(bild, zeile, zeichenInAktuellerZeile, 0, 0, 0);
						zeichenInAktuellerZeile++;
					}
				}
				
				
			} else {
				// Farben randomisieren
				r = new Double(127+(Math.random() * 127)).intValue();
				g = new Double(127+(Math.random() * 127)).intValue();
				b = new Double(127+(Math.random() * 127)).intValue();
			}
			
			// Ausgabe
			for (int i=0; i<kindKnoten.getZaehler(); i++)
				this.zeichnePixel(bild, zeile, zeichenInAktuellerZeile+i, r, g, b);
			
			// Position und Farbe merken (etwas unelegant)
			baumKindKnoten.setUserObject(new MetaKnoten(kindKnoten, zeichenInAktuellerZeile, r, g, b));
			zeichenInAktuellerZeile += kindKnoten.getZaehler();
			
			// Ggf. Zeilenumbruch einfuegen
			if (zeichenInAktuellerZeile >= zeichenProZeile){
				zeile++;
				zeichenInAktuellerZeile = 0;
			}
		}
		
		// Pruefen, welches Format die Bilddatei haben soll
		String outputPath = this.outputFilePath;
		String endung;
		if (outputPath.endsWith(".png"))
			endung = "png";
		else if (outputPath.endsWith(".jpg")||outputPath.endsWith(".jpeg"))
			endung = "jpg";
		else {
			endung = "png";
			outputPath = outputPath.concat(".png");
		}
		
		// Bild schreiben
		FileImageOutputStream out = new FileImageOutputStream(new File(outputPath));
		ImageIO.write(bild, endung, out);
		out.close();
		
		return true;
	}
	
	/**
	 * Zeichnet einen Bildpunkt.
	 * @param bild
	 * @param zeile
	 * @param spalte
	 * @param red
	 * @param green
	 * @param blue
	 */
	private void zeichnePixel(BufferedImage bild, int zeile, int spalte, int red, int green, int blue){
		
		// Farbe umrechnen
		int rgb = (red << 16) | (green << 8) | blue;
		
		// Bildpunkt zeichnen
		this.zeichnePixel(bild, zeile, spalte, rgb);
	}
	
	/**
	 * Zeichnet einen Bildpunkt. Die horizontale Skalierung der Ausgabe wird automatisch gewaehlt.
	 * @param bild
	 * @param zeile
	 * @param spalte
	 * @param rgb
	 */
	private void zeichnePixel(BufferedImage bild, int zeile, int spalte, int rgb){
		
		// Bildpunkt/Flaeche zeichnen
		try {
			
			// Erste Zeile, in die gezeichnet werden soll
			int grundZeile = zeile*this.pixelsPerLevel;
			
			// Letzte Zeile, in die gezeichnet werden soll
			int endZeile = grundZeile+this.pixelsPerLevel;
			
			// Vertikal (Zeilen) iterieren
			for (int zeilenIndex=grundZeile; zeilenIndex<=endZeile; zeilenIndex++){
				
				// Erste Spalte, in die gezeichnet werden soll
				double grundSpalte = this.horizontalePixelProKnoten * spalte;
				
				// Letzte Spalte, in die gezeichnet werden soll
				double endSpalte = grundSpalte+this.horizontalePixelProKnoten;
				
				// Horizontal (Spalten) zeichnen
				for (double spaltenIndex = grundSpalte; spaltenIndex<=endSpalte; spaltenIndex++){
					int spaltenIndexGanzzahl = new Double(spaltenIndex).intValue();
					bild.setRGB(spaltenIndexGanzzahl, zeilenIndex, rgb);
				}
					
			}
		} catch (ArrayIndexOutOfBoundsException e){
			if (!fehlerGemeldet){
				fehlerGemeldet = true;
				Logger.getLogger("").log(Level.WARNING, "The tree is too large to fit into the image dimensions.", e);
			}
		}
	}

	private DefaultTreeModel insertIntoTreeModel(Knoten knoten, DefaultMutableTreeNode elternBaumKnoten, DefaultTreeModel baum) throws IOException {
		
DefaultMutableTreeNode baumKnoten = new DefaultMutableTreeNode(knoten);
		
		if (baum == null){
			baum = new DefaultTreeModel(baumKnoten);
		} else 
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
