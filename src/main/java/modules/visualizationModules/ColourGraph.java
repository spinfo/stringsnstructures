/**
 * 
 */
package modules.visualizationModules;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
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
public class ColourGraph extends ModuleImpl {

	// Property keys
	public static final String PROPERTYKEY_OUTPUTFILE = "Output file";
	public static final String PROPERTYKEY_IMAGEWIDTH = "Image width";
	public static final String PROPERTYKEY_IMAGEHEIGHT = "Image height";
	public static final String PROPERTYKEY_PIXELPERLEVEL = "Pixel per tree level";
	public static final String PROPERTYKEY_GLEICHVERTEILUNGBERECHNEN = "Ungleichverteilungen im Baum markieren";
	public static final String PROPERTYKEY_UNGLEICHVERTEILUNGANZEIGEMETHODE = "Markierungsart fuer Ungleichverteilung";
	public static final String PROPERTYKEY_GLEICHVERTEILUNGSSCHWELLWERT = "Schwellwert fuer Gleichverteilung";
	
	// Instance variables
	private final String INPUTID = "input";
	private KnotenKomparator knotenKomparator = new KnotenKomparator();
	private String outputFilePath;
	private int outputImageWidth;
	private int outputImageHeight;
	private boolean fehlerGemeldet = false;
	private boolean ungleichverteilungenMarkieren = false;
	private double horizontalePixelProKnoten = 1d;
	private int pixelsPerLevel;
	private String ungleichverteilungsanzeigemethode;
	private double gleichverteilungsschwellwert;

	/**
	 * Constructor
	 * @param callbackReceiver Callback receiver instance
	 * @param properties Properties
	 * @throws Exception Thrown if something goes wrong
	 */
	public ColourGraph(CallbackReceiver callbackReceiver, Properties properties)
			throws Exception {
		super(callbackReceiver, properties);

		// Define I/O
		InputPort inputPort = new InputPort(INPUTID, "JSON-encoded suffix trie.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputPort);

		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_OUTPUTFILE,
				"Specifies the location of the output file (PNG or JPEG).");
		this.getPropertyDescriptions().put(PROPERTYKEY_IMAGEWIDTH,
				"Width of the output image in pixels.");
		this.getPropertyDescriptions().put(PROPERTYKEY_IMAGEHEIGHT,
				"Height of the output image in pixels.");
		this.getPropertyDescriptions().put(PROPERTYKEY_PIXELPERLEVEL,
				"Amount of pixels used for each tree level.");
		this.getPropertyDescriptions().put(PROPERTYKEY_GLEICHVERTEILUNGBERECHNEN,
				"Berechnet den Faktor eines Knotens, der anzeigt, wie gleich dessen Kinder verteilt sind (0>X>=1; 1 bed. absolut gleich verteilt).");
		this.getPropertyDescriptions().put(PROPERTYKEY_UNGLEICHVERTEILUNGANZEIGEMETHODE,
				"Legt fest, wie die Ungleichverteilung markiert wird (alpha|rot|alpha+rot|keine).");
		this.getPropertyDescriptions().put(PROPERTYKEY_GLEICHVERTEILUNGSSCHWELLWERT,
				"Schwellwert fuer Gleichverteilungsfaktor, oberhalb dessen Knoten ausgeblendet werden (0-1 [double]; 0 blendet alle aus, 1 keine).");

		// Determine system properties (for setting default values that make
		// sense)
		String fs = System.getProperty("file.separator");
		String homedir = System.getProperty("user.home");
		
		DateFormat formatierer = new SimpleDateFormat("yyyyMMdd");
		String dateiName = "exp_"+formatierer.format(new Date())+"-001_colourgraph.png";

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"ColourGraph");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTPUTFILE,
				homedir + fs + dateiName);
		this.getPropertyDefaultValues().put(PROPERTYKEY_IMAGEWIDTH, "640");
		this.getPropertyDefaultValues().put(PROPERTYKEY_IMAGEHEIGHT, "480");
		this.getPropertyDefaultValues().put(PROPERTYKEY_PIXELPERLEVEL, "10");
		this.getPropertyDefaultValues().put(PROPERTYKEY_GLEICHVERTEILUNGBERECHNEN, "false");
		this.getPropertyDefaultValues().put(PROPERTYKEY_UNGLEICHVERTEILUNGANZEIGEMETHODE, "alpha");
		this.getPropertyDefaultValues().put(PROPERTYKEY_GLEICHVERTEILUNGSSCHWELLWERT, "1.0");
		
		// Add module description
		this.setDescription("Creates an image file a with visual representation of the node distribution within the input graph.");
		// Add module category
		this.setCategory("Visualisation");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see modularization.ModuleImpl#applyProperties()
	 */
	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		
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
		if (this.getProperties().containsKey(PROPERTYKEY_GLEICHVERTEILUNGBERECHNEN))
			this.ungleichverteilungenMarkieren = Boolean.valueOf(this.getProperties()
					.getProperty(PROPERTYKEY_GLEICHVERTEILUNGBERECHNEN));
		if (this.getProperties().containsKey(PROPERTYKEY_UNGLEICHVERTEILUNGANZEIGEMETHODE))
			this.ungleichverteilungsanzeigemethode = this.getProperties().getProperty(
					PROPERTYKEY_UNGLEICHVERTEILUNGANZEIGEMETHODE);
		if (this.getProperties().containsKey(PROPERTYKEY_GLEICHVERTEILUNGSSCHWELLWERT))
			this.gleichverteilungsschwellwert = Double.valueOf(this.getProperties()
					.getProperty(PROPERTYKEY_GLEICHVERTEILUNGSSCHWELLWERT));
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
		BufferedImage bild = new BufferedImage(this.outputImageWidth, this.outputImageHeight, BufferedImage.TYPE_INT_ARGB);
		
		// Graphikobjekt instanziieren
		Graphics2D graphik = bild.createGraphics();
				
		// Wurzelknoten einlesen
		Knoten wurzelKnoten = gson.fromJson(this.getInputPorts().get(INPUTID).getInputReader(), Knoten.class);
		
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

			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// Ermittle aktuelles Element
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
						this.zeichnePixel(graphik, zeile, zeichenInAktuellerZeile, 0, 0, 0, 255);
						zeichenInAktuellerZeile++;
					}
				}
				
				
			} else {
				// Farben randomisieren
				r = new Double(127+(Math.random() * 127)).intValue();
				g = new Double(127+(Math.random() * 127)).intValue();
				b = new Double(127+(Math.random() * 127)).intValue();
			}
			
			int alpha = 255;
			
			// Ggf. Ungleichverteilung ermitteln
			if (this.ungleichverteilungenMarkieren){
				double zaehlerdurchschnittswert = new Double(kindKnoten.getZaehler()) / new Double(kindKnoten.getKinder().size());
				double gleichverteilungsFaktor = 1d; // Wertebereich 0<X<=1
				Iterator<Knoten> enkelKnotenListe = kindKnoten.getKinder().values().iterator();
				while(enkelKnotenListe.hasNext()){
					gleichverteilungsFaktor = gleichverteilungsFaktor*(enkelKnotenListe.next().getZaehler()/zaehlerdurchschnittswert);
				}
				// Farbwerte entsprechend der ermittelten Ungleichverteilung anpassen
				
				/*r = new Double(254*ungleichverteilungsfaktor).intValue();//(int) Math.floor(r * ungleichverteilungsfaktor);
				g = 0;//(int) Math.floor(g * ungleichverteilungsfaktor);
				b = 0;//(int) Math.floor(b * ungleichverteilungsfaktor);*/
				if (ungleichverteilungsanzeigemethode.equals("alpha"))
					alpha = new Double(255-(alpha * gleichverteilungsFaktor)).intValue();
				else if (ungleichverteilungsanzeigemethode.equals("rot")){
					r = (int)(255-(255 * gleichverteilungsFaktor));
					g = 0;
					b = 0;
				} else if (ungleichverteilungsanzeigemethode.equals("alpha+rot")){
					alpha = new Double(255-(alpha * gleichverteilungsFaktor)).intValue();
					r = (int)(255-(255 * gleichverteilungsFaktor));
					g = 0;
					b = 0;
				}
				
				// Elemente unterhalb des Schwellwerts transparent machen.
				if (gleichverteilungsFaktor>gleichverteilungsschwellwert){
					alpha = 0;
				}
			}
			
			// Ausgabe
			for (int i=0; i<kindKnoten.getZaehler(); i++)
				this.zeichnePixel(graphik, zeile, zeichenInAktuellerZeile+i, r, g, b, alpha);
			
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
		graphik.dispose();
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
	/*private void zeichnePixel(Graphics2D bild, int zeile, int spalte, int red, int green, int blue, int alpha){
		
		// Farbe umrechnen
		int rgb = (red << 16) | (green << 8) | blue;
		
		// Bildpunkt zeichnen
		this.zeichnePixel(bild, zeile, spalte, rgb, alpha);
	}*/
	
	/**
	 * Zeichnet einen Bildpunkt. Die horizontale Skalierung der Ausgabe wird automatisch gewaehlt.
	 * @param graphik
	 * @param zeile
	 * @param spalte
	 * @param rgb
	 */
	private void zeichnePixel(Graphics2D graphik, int zeile, int spalte, int red, int green, int blue, int alpha){
		
		// Bildpunkt/Flaeche zeichnen
		try {
			
			//graphik.setComposite(makeComposite((float)alpha/255f));
			
			// Erste Zeile, in die gezeichnet werden soll
			int grundZeile = zeile*this.pixelsPerLevel;
			
			// Letzte Zeile, in die gezeichnet werden soll
			//int endZeile = grundZeile+this.pixelsPerLevel;
			
			graphik.setColor(new Color(red,green,blue,alpha));
			int grundSpalte = new Double(this.horizontalePixelProKnoten * spalte).intValue();
			//int endSpalte = new Double(grundSpalte+this.horizontalePixelProKnoten).intValue();
			graphik.fillRect(grundSpalte, grundZeile, (int)Math.ceil(this.horizontalePixelProKnoten), this.pixelsPerLevel);
			
			// Vertikal (Zeilen) iterieren
			/*for (int zeilenIndex=grundZeile; zeilenIndex<=endZeile; zeilenIndex++){
				
				// Erste Spalte, in die gezeichnet werden soll
				double grundSpalte = this.horizontalePixelProKnoten * spalte;
				
				// Letzte Spalte, in die gezeichnet werden soll
				double endSpalte = grundSpalte+this.horizontalePixelProKnoten;
				
				// Horizontal (Spalten) zeichnen
				for (double spaltenIndex = grundSpalte; spaltenIndex<=endSpalte; spaltenIndex++){
					int spaltenIndexGanzzahl = new Double(spaltenIndex).intValue();
					bild.setRGB(spaltenIndexGanzzahl, zeilenIndex, rgb);
					
				}
					
			}*/
		} catch (ArrayIndexOutOfBoundsException e){
			if (!fehlerGemeldet){
				fehlerGemeldet = true;
				Logger.getLogger("").log(Level.WARNING, "The tree is too large to fit into the image dimensions.", e);
			}
		} catch (IllegalArgumentException e){
			if (!fehlerGemeldet){
				fehlerGemeldet = true;
				Logger.getLogger("").log(Level.WARNING, "Der Transparenzwert ist ausserhalb der Grenzwerte: "+alpha, e);
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
