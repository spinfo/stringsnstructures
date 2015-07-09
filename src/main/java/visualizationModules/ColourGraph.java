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
	String outputFilePath;
	int outputImageWidth;
	int outputImageHeight;
	int pixelsPerLevel;
	KnotenKomparator knotenKomparator = new KnotenKomparator();

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
				"Specifies the location of the output file (jpeg).");
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
	 * @see modularization.ModuleImpl#process()
	 */
	@Override
	public boolean process() throws Exception {

		// Instantiate JSON parser
		Gson gson = new GsonBuilder().create();
		
		// Bild instanziieren
		BufferedImage bild = new BufferedImage(this.outputImageWidth, this.outputImageHeight, BufferedImage.TYPE_INT_RGB);

		// Wurzelknoten einlesen
		Knoten wurzelKnoten = gson.fromJson(this.getInputCharPipe().getInput(),
				Knoten.class);

		// Graphen ausgeben
		this.paintImageFromGraphNodes(wurzelKnoten, bild, 0, 0, 255, 0, 0);

		// Bild schreiben
		FileImageOutputStream out = new FileImageOutputStream(new File(
				this.outputFilePath));
		ImageIO.write(bild, "png", out);
		out.close();
		return true;
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
		if (this.getProperties().containsKey(PROPERTYKEY_IMAGEWIDTH))
			this.outputImageWidth = Integer.valueOf(this.getProperties()
					.getProperty(PROPERTYKEY_IMAGEWIDTH));
		if (this.getProperties().containsKey(PROPERTYKEY_IMAGEHEIGHT))
			this.outputImageHeight = Integer.valueOf(this.getProperties()
					.getProperty(PROPERTYKEY_IMAGEHEIGHT));
		if (this.getProperties().containsKey(PROPERTYKEY_PIXELPERLEVEL))
			this.pixelsPerLevel = Integer.valueOf(this.getProperties()
					.getProperty(PROPERTYKEY_PIXELPERLEVEL));
		super.applyProperties();
	}

	/**
	 * Erstellt eine Graphik aus dem uebergebenen (Graphen)Knoten.
	 * 
	 * @param knoten
	 * @param bild
	 * @param zeilenIndex
	 * @param spaltenIndex
	 * @param r
	 * @param g
	 * @param b
	 * @param bildZeilenProGeneration
	 * @param horizontalePixelProZaehler
	 * @return Anzahl der vorgerueckten Spalten
	 */
	private int paintImageFromGraphNodes(Knoten knoten, BufferedImage bild,
			int zeilenIndex, int spaltenIndex, int r, int g, int b) {

		// Falls die Zeile ausserhalb des Bildes liegt, abbrechen
		if (zeilenIndex >= bild.getHeight())
			return 0;
		if (spaltenIndex >= bild.getWidth())
			return 0;

		// Bild einfaerben
		int spalte = spaltenIndex;
		System.out.print("schreibe in Zeile "+zeilenIndex+" von Spalte "+spalte);
		System.out.flush();
		int red = r;
		int green = g;
		int blue = b;
		int rgb = (red << 16) | (green << 8) | blue;
		for (; spalte < bild.getWidth() && spalte < spaltenIndex+knoten.getZaehler(); spalte ++) {
			// Set colour for all affected pixels
			bild.setRGB(spalte, zeilenIndex, rgb);
			System.out.print(knoten.getName());
		}
		System.out.println(" bis "+spalte);

		// Kindknoten in TreeSet mit eigenem Comparator speichern (sortiert nach
		// Zaehlvariable der Knoten)
		TreeSet<Knoten> sortierteKindKnoten = new TreeSet<Knoten>(
				this.knotenKomparator);
		sortierteKindKnoten.addAll(knoten.getKinder().values());

		// Methode rekursiv fuer Kindknoten aufrufen
		int kindSpalte = spaltenIndex;
		Iterator<Knoten> kindKnoten = sortierteKindKnoten.iterator();
		while (kindKnoten.hasNext()) {
			Knoten kind = kindKnoten.next();

			// Zeilen kolorieren
			kindSpalte += this.paintImageFromGraphNodes(kind, bild, zeilenIndex+1, kindSpalte, r, g, b);

			// Farbe aendern
			r = new Double(Math.random() * 254).intValue();
			g = new Double(Math.random() * 254).intValue();
			b = new Double(Math.random() * 254).intValue();
		}

		return spaltenIndex+spalte;

	}

}
